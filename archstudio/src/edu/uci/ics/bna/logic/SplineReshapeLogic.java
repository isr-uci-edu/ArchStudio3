package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import edu.uci.ics.bna.*;
import edu.uci.ics.graphicsutils.BSpline;

public class SplineReshapeLogic extends ThingLogicAdapter implements SelectionTrackingListener{

	protected boolean autoMergeNearbyHandles;
	protected boolean doubleClickForNewHandle;
	protected boolean stickyEndpoints;
	
	protected SelectionTrackingLogic selectionTrackingLogic;
	
	public SplineReshapeLogic(SelectionTrackingLogic selectionTrackingLogic){
		this.selectionTrackingLogic = selectionTrackingLogic;
		selectionTrackingLogic.addSelectionTrackingListener(this);
		autoMergeNearbyHandles = true;
		doubleClickForNewHandle = true;
		stickyEndpoints = true;
	}
	
	public SplineReshapeLogic(SelectionTrackingLogic selectionTrackingLogic,
	boolean autoMergeNearbyHandles, boolean doubleClickForNewHandle, boolean stickyEndpoints){
		this.selectionTrackingLogic = selectionTrackingLogic;
		selectionTrackingLogic.addSelectionTrackingListener(this);
		this.autoMergeNearbyHandles = autoMergeNearbyHandles;
		this.doubleClickForNewHandle = doubleClickForNewHandle;
		this.stickyEndpoints = stickyEndpoints;
	}
	
	public void destroy() {
		if (currentlyResizingThing != null) {
			removeResizeHandles(bnaComponent.getModel(), currentlyResizingThing);
			currentlyResizingThing = null;
			currentlyResizingThingPoints = null;
		}
		grabbedThing = null;
		super.destroy();
	}
	
	public void init(){
		updateStickyLineCache(getBNAComponent());
	}
	
	public void selectionChanged(SelectionChangedEvent evt){
		checkSelections(bnaComponent.getModel());
	}

	protected Thing grabbedThing = null;
	protected Thing currentlyResizingThing = null;
	protected Point[] currentlyResizingThingPoints = null;
	protected int lastMouseX = -1;
	protected int lastMouseY = -1;
	
	public synchronized void bnaModelChanged(BNAModelEvent evt){
		int evtType = evt.getEventType();
		if(evtType == BNAModelEvent.THING_CHANGED){
			Thing targetThing = evt.getTargetThing();
			if(targetThing instanceof ISelectable){
				ThingEvent tevt = evt.getThingEvent();
				if(currentlyResizingThing != null){
					if(targetThing.getID().equals(currentlyResizingThing.getID())){
						Point[] points = ((IReshapableSpline)targetThing).getAllPoints();
						if(!Arrays.equals(points, currentlyResizingThingPoints)){
							currentlyResizingThingPoints = points;
							updateResizeHandles(evt.getSource(), currentlyResizingThing);
						}
					}
				}
			}
		}
	}
	
	public void removeResizeHandles(BNAModel m, Thing t){
		Set s = t.getSetProperty("$resizeHandleIDs");
		if(s.size() == 0){
			return;
		}
		for(Iterator it = s.iterator(); it.hasNext(); ){
			String resizeHandleID = (String)it.next();
			//System.out.println("Removing: " + resizeHandleID);
			m.removeThing(resizeHandleID);
		}
		t.removeProperty("$resizeHandleIDs");
	}
	
	public synchronized void updateResizeHandles(BNAModel m, Thing t){
		if(!(t instanceof IReshapableSpline)){
			return;
		}
		
		HashSet ns = new HashSet();		
		IReshapableSpline rst = (IReshapableSpline)t;
		Set currentResizeHandleIDs = t.getSetProperty("$resizeHandleIDs");
		int numPoints = rst.getNumPoints();
		
		if(currentResizeHandleIDs.size() == 0){
			//Must create new resize handles
			for(int i = 0; i < numPoints; i++){
				Point p = rst.getPointAt(i);
				ReshapeHandleThing rh = new ReshapeHandleThing();
				rh.setX(p.x);
				rh.setY(p.y);
				rh.setTargetThingID(t.getID());
				rh.setTargetThingIndex(i);
				rh.setOrientation(ReshapeHandleThing.ORIENTATION_CENTER);
				BNAUtils.setStackingPriority(rh, BNAUtils.STACKING_PRIORITY_ALWAYS_ON_TOP);

				rh.setColor(ReshapeHandleThing.DEFAULT_COLOR);
				if(t instanceof IStickyEndpointsSpline){
					if((i == 0) && (((IStickyEndpointsSpline)t).getFirstEndpointStuckToID() != null)){
						rh.setColor(Color.GREEN);
					}
					if((i == (numPoints - 1)) && (((IStickyEndpointsSpline)t).getSecondEndpointStuckToID() != null)){
						rh.setColor(Color.GREEN);
					}
				}
				
				m.addThing(rh);
				ns.add(rh.getID());
			}
		}
		else{
			BitSet bs = new BitSet(numPoints);
			for(Iterator it = currentResizeHandleIDs.iterator(); it.hasNext(); ){
				String rhID = (String)it.next();
				ReshapeHandleThing rh = (ReshapeHandleThing)m.getThing(rhID);
				Thing targetThing = m.getThing(rh.getTargetThingID());
				int targetThingIndex = rh.getTargetThingIndex();
				Point targetPoint = ((IReshapableSpline)targetThing).getPointAt(targetThingIndex);
				if(targetPoint != null){
					//Thread.dumpStack();
					rh.setX(targetPoint.x);
					rh.setY(targetPoint.y);

					rh.setColor(ReshapeHandleThing.DEFAULT_COLOR);
					if(targetThing instanceof IStickyEndpointsSpline){
						if((targetThingIndex == 0) && (((IStickyEndpointsSpline)targetThing).getFirstEndpointStuckToID() != null)){
							rh.setColor(Color.GREEN);
						}
						if((targetThingIndex == (numPoints - 1)) && (((IStickyEndpointsSpline)targetThing).getSecondEndpointStuckToID() != null)){
							rh.setColor(Color.GREEN);
						}
					}
					
					ns.add(rh.getID());
					bs.set(targetThingIndex, true);
				}
				else{
					m.removeThing(rh);
				}
			}
			for(int i = 0; i < numPoints; i++){
				if(!bs.get(i)){
					//Create new point
					Point p = rst.getPointAt(i);
					ReshapeHandleThing rh = new ReshapeHandleThing();
					rh.setX(p.x);
					rh.setY(p.y);
					rh.setTargetThingID(t.getID());
					rh.setTargetThingIndex(i);
					rh.setOrientation(ReshapeHandleThing.ORIENTATION_CENTER);
					BNAUtils.setStackingPriority(rh, BNAUtils.STACKING_PRIORITY_ALWAYS_ON_TOP);

					rh.setColor(ReshapeHandleThing.DEFAULT_COLOR);
					if(t instanceof IStickyEndpointsSpline){
						if((i == 0) && (((IStickyEndpointsSpline)t).getFirstEndpointStuckToID() != null)){
							rh.setColor(Color.GREEN);
						}
						if((i == (numPoints - 1)) && (((IStickyEndpointsSpline)t).getSecondEndpointStuckToID() != null)){
							rh.setColor(Color.GREEN);
						}
					}
					
					m.addThing(rh);
					ns.add(rh.getID());
				}
			}
		}

		t.replaceSetPropertyValues("$resizeHandleIDs", ns);
	}
	
	public void checkSelections(BNAModel m){
		Thing[] selectedThings = selectionTrackingLogic.getSelectedThings();
		
		if(selectedThings.length != 1){
			if(currentlyResizingThing != null){
				removeResizeHandles(m, currentlyResizingThing);
				currentlyResizingThing = null;
				currentlyResizingThingPoints = null;
			}
		}
		else{
			//There is exactly one selected thing.
			Thing selectedThing = selectedThings[0];
			if(currentlyResizingThing != selectedThing){
				if(currentlyResizingThing != null){
					removeResizeHandles(m, currentlyResizingThing);
				}
				if(selectedThing instanceof IReshapableSpline){
					currentlyResizingThing = selectedThing;
					currentlyResizingThingPoints = ((IReshapableSpline)selectedThing).getAllPoints();
				}
				else{
					currentlyResizingThing = null;
					currentlyResizingThingPoints = null;
				}
			}
			updateResizeHandles(m, currentlyResizingThing);
		}
	}

	public synchronized void mouseClicked(Thing t, MouseEvent evt, int worldX, int worldY){
		if (t instanceof IUserEditable) {
			IUserEditable et = (IUserEditable) t;
			if (!et.isUserEditable())
				return;
		}
		BNAComponent c = bnaComponent;
		if(doubleClickForNewHandle){
			if(evt.getClickCount() == 2){
				if(t instanceof IReshapableSpline){
					IReshapableSpline rst = (IReshapableSpline)t;
					CoordinateMapper cm = c.getCoordinateMapper();
					Point[] worldPoints = rst.getAllPoints();
					if(worldPoints.length == 0){
						return;
					}
					
					int lx = cm.worldXtoLocalX(worldX);
					int ly = cm.worldYtoLocalY(worldY);
			
					Point[] translatedPoints = new Point[worldPoints.length];
					for(int i = 0; i < worldPoints.length; i++){
						Point tp = new Point();
						tp.x = cm.worldXtoLocalX(worldPoints[i].x);
						tp.y = cm.worldYtoLocalY(worldPoints[i].y);
						translatedPoints[i] = tp;
					}
					
					if(translatedPoints.length == 1){
						int dist = (int)Point2D.Double.distance((double)lx, (double)ly, 
							(double)translatedPoints[0].x, (double)translatedPoints[0].y);
						if(dist < 4){
							rst.addPoint(new Point(worldX, worldY));
						}
						else{
							return;
						}
					}

					//Support for BSplines			
					int splineMode = rst.getSplineMode();
					if(splineMode == SplineThing.SPLINE_MODE_BSPLINE){
						translatedPoints = BSpline.bspline(translatedPoints, SplineThing.SPLINE_SMOOTHNESS);
					}
					
					for(int i = 1; i < translatedPoints.length; i++){
						int x1 = translatedPoints[i-1].x;
						int y1 = translatedPoints[i-1].y;
						int x2 = translatedPoints[i].x;
						int y2 = translatedPoints[i].y;
						
						int dist = (int)Line2D.Double.ptSegDist((double)x1, (double)y1, (double)x2, (double)y2, (double)lx, (double)ly);
						if(dist < 4){
							if(translatedPoints.length == worldPoints.length){
								rst.insertPointAt(new Point(worldX, worldY), i);
								break;
							}
							else{
								//It's a spline--the actual, drawn points are not
								//equivalent to the control points, so we have to
								//figure out which segment of the drawn spline corresponds
								//to which control point and figure out 
								//what control point to add the new point it after.
								
								//Note that this only works for our particular implementation
								//of Bsplines, where there are an equal amount of segments
								//on each side of a middle control point, e.g.:
								//X is a control point, x is a drawing point:
								
								//X------x--x--X--x--x------x--x--X--x--x------X
								
								//Here, there are two drawing points on every side of a control
								//point--this is SPLINE_SMOOTHNESS set to 4 (4 additional points
								//per midpoint).  This is also why SPLINE_SMOOTHNESS needs to be
								//an even number, by the way.
								
								//Thus, this probably wouldn't work with Bezier curves or whatnot
								//(but hey, who likes Bezier curves anyway?)
								 
								int seg = i - (SplineThing.SPLINE_SMOOTHNESS / 2) - 1;
								if(seg <= 0){
									rst.insertPointAt(new Point(worldX, worldY), 1);
									break;
								}
								else{
									seg = seg / (SplineThing.SPLINE_SMOOTHNESS + 1);
									rst.insertPointAt(new Point(worldX, worldY), seg + 2);										
									break;
								}
							}
						}
					}
					return;
				}
			}
		}
	}

	public synchronized void mousePressed(Thing t, MouseEvent evt, int worldX, int worldY){
		if (t instanceof IUserEditable) {
			IUserEditable et = (IUserEditable) t;
			if (!et.isUserEditable())
				return;
		}
		BNAComponent c = bnaComponent;
		if(evt.getButton() == MouseEvent.BUTTON1){
			grabbedThing = t;
			lastMouseX = evt.getX();
			lastMouseY = evt.getY();
			
			if(stickyEndpoints){
				if(t instanceof ReshapeHandleThing){
					updateStickyLineCache(c);
				}
			}
		}
	}
	
	public synchronized void mouseReleased(Thing t, MouseEvent evt, int worldX, int worldY){
		BNAComponent c = bnaComponent;
		if(autoMergeNearbyHandles){
			if((grabbedThing != null) && (grabbedThing instanceof ReshapeHandleThing)){
				ReshapeHandleThing rh = (ReshapeHandleThing)grabbedThing;
				String targetID = rh.getTargetThingID();
				Thing targetThing = c.getModel().getThing(targetID);
				if((targetThing != null) && (targetThing instanceof IReshapableSpline)){
					IReshapableSpline rst = (IReshapableSpline)targetThing;
					
					int indexOfCurrentHandle = rh.getTargetThingIndex();
					ReshapeHandleThing previousHandle = null;
					ReshapeHandleThing nextHandle = null;
					
					Set currentReshapeHandleIDs = targetThing.getSetProperty("$resizeHandleIDs");
					for(Iterator it = currentReshapeHandleIDs.iterator(); it.hasNext(); ){
						String rhID = (String)it.next();
						Thing targetRH = c.getModel().getThing(rhID);
						//System.out.println("Checking" + targetRH);
						if(targetRH != null){
							ReshapeHandleThing otherReshapeHandle = (ReshapeHandleThing)targetRH;
							int otherReshapeHandleIndex = otherReshapeHandle.getTargetThingIndex();
							if(otherReshapeHandleIndex == (indexOfCurrentHandle + 1)){
								nextHandle = otherReshapeHandle;
							}
							else if(otherReshapeHandleIndex == (indexOfCurrentHandle - 1)){
								previousHandle = otherReshapeHandle;
							}
						}
					}
					//System.out.println("previousHandle = " + previousHandle);
					//System.out.println("nextHandle = " + nextHandle);
					if((previousHandle != null) && (nextHandle != null)){
						//Don't meld endpoints
						int dist1 = (int)java.awt.geom.Point2D.distance(rh.getX(), rh.getY(), previousHandle.getX(), previousHandle.getY());
						if(dist1 < 7){
							rst.removePointAt(rh.getTargetThingIndex());
							removeResizeHandles(c.getModel(), targetThing);
							updateResizeHandles(c.getModel(), targetThing);
						}
						else{
							int dist2 = (int)java.awt.geom.Point2D.distance(rh.getX(), rh.getY(), nextHandle.getX(), nextHandle.getY());
							if(dist2 < 7){
								rst.removePointAt(rh.getTargetThingIndex());
								removeResizeHandles(c.getModel(), targetThing);
								updateResizeHandles(c.getModel(), targetThing);
							}
						}
					}
				}
			}
		}
		grabbedThing = null;
	}
	
	public Point snapToNormal(BNAComponent c, ReshapeHandleThing rh, IReshapableSpline rst, MouseEvent evt, int worldX, int worldY){
		int indexOfCurrentHandle = rh.getTargetThingIndex();
		ReshapeHandleThing previousHandle = null;
		ReshapeHandleThing nextHandle = null;
		
		Set currentReshapeHandleIDs = ((Thing)rst).getSetProperty("$resizeHandleIDs");
		for(Iterator it = currentReshapeHandleIDs.iterator(); it.hasNext(); ){
			String rhID = (String)it.next();
			Thing targetRH = c.getModel().getThing(rhID);
			//System.out.println("Checking" + targetRH);
			if(targetRH != null){
				ReshapeHandleThing otherReshapeHandle = (ReshapeHandleThing)targetRH;
				int otherReshapeHandleIndex = otherReshapeHandle.getTargetThingIndex();
				if(otherReshapeHandleIndex == (indexOfCurrentHandle + 1)){
					nextHandle = otherReshapeHandle;
				}
				else if(otherReshapeHandleIndex == (indexOfCurrentHandle - 1)){
					previousHandle = otherReshapeHandle;
				}
			}
		}
		
		if((previousHandle == null) && (nextHandle == null)){
			return new Point(worldX, worldY);
		}
		ReshapeHandleThing snapToHandle = nextHandle;
		if(snapToHandle == null){
			snapToHandle = previousHandle;
		}
		
		int dx = worldX - snapToHandle.getX();
		int dy = worldY - snapToHandle.getY();
		if(dx < 0) dx = -dx;
		if(dy < 0) dy = -dy;
		
		if(dx < dy){
			return new Point(snapToHandle.getX(), worldY);
		}
		else{
			return new Point(worldX, snapToHandle.getY());
		}		
	}
	
	protected java.util.HashMap stickyLineCache = null;
	
	protected void updateStickyLineCache(BNAComponent c){
		BNAModel m = c.getModel();
		if(stickyLineCache == null){
			stickyLineCache = new HashMap(m.getNumThings() / 3);
		}
		synchronized(stickyLineCache){
			stickyLineCache.clear();
			for(Iterator it = m.getThingIterator(); it.hasNext(); ){
				Thing t = (Thing)it.next();
				if(t instanceof IStickyBoxBounded){
					Rectangle r = ((IStickyBoxBounded)t).getStickyBox();
					Line2D.Double line1 = new Line2D.Double(r.x, r.y, r.x + r.width, r.y);
					Line2D.Double line2 = new Line2D.Double(r.x, r.y, r.x, r.y + r.height);
					Line2D.Double line3 = new Line2D.Double(r.x + r.width, r.y, r.x + r.width, r.y + r.height);
					Line2D.Double line4 = new Line2D.Double(r.x, r.y + r.height, r.x + r.width, r.y + r.height);
					stickyLineCache.put(line1, t);
					stickyLineCache.put(line2, t);
					stickyLineCache.put(line3, t);
					stickyLineCache.put(line4, t);
				}
			}
		}
	}
	
	class StickyBit{
		public Point stickyPoint;
		public Thing stickyThing;
	}
		
	protected StickyBit snapEndpointToStickyBox(BNAComponent c, ReshapeHandleThing rh, IReshapableSpline rst, MouseEvent evt, int worldX, int worldY){
		if(stickyLineCache == null){
			StickyBit sb = new StickyBit();
			sb.stickyPoint = new Point(worldX, worldY);
			sb.stickyThing = null;
			return sb;
		}
		synchronized(stickyLineCache){
			for(Iterator it = stickyLineCache.keySet().iterator(); it.hasNext(); ){
				Line2D line = (Line2D)it.next();
				int dist = (int)line.ptSegDist(worldX, worldY);
				if(dist < 10){
					if(line.getX1() == line.getX2()){
						//Vertical line
						StickyBit sb = new StickyBit();
						int wy = worldY;
						if(wy < line.getY1()){
							wy = (int)line.getY1();
						}
						if(wy > line.getY2()){
							wy = (int)line.getY2();
						}
						sb.stickyPoint = new Point((int)line.getX1(), wy);
						sb.stickyThing = (Thing)stickyLineCache.get(line);
						return sb;
					}
					else{
						//Horizontal line
						StickyBit sb = new StickyBit();
						int wx = worldX;
						if(wx < line.getX1()){
							wx = (int)line.getX1();
						}
						if(wx > line.getX2()){
							wx = (int)line.getX2();
						}
						sb.stickyPoint = new Point(wx, (int)line.getY1());
						sb.stickyThing = (Thing)stickyLineCache.get(line);
						return sb;
					}
				}
			}
			StickyBit sb = new StickyBit();
			sb.stickyPoint = new Point(worldX, worldY);
			sb.stickyThing = null;
			return sb;
		}
	}
	
	public void mouseDragged(Thing t, MouseEvent evt, int worldX, int worldY){
		BNAComponent c = bnaComponent;
		if(grabbedThing != null){
			if(grabbedThing instanceof ReshapeHandleThing){
				ReshapeHandleThing rh = (ReshapeHandleThing)grabbedThing;
				String targetID = rh.getTargetThingID();
				Thing targetThing = c.getModel().getThing(targetID);
				if((targetThing != null) && (targetThing instanceof IReshapableSpline)){
					IReshapableSpline rst = (IReshapableSpline)targetThing;
					int targetThingIndex = rh.getTargetThingIndex();
					
					Point pt = rst.getPointAt(targetThingIndex);
					pt.x = worldX;
					pt.y = worldY;
					
					if(BNAUtils.wasShiftPressed(evt)){
						Point p = snapToNormal(c, rh, rst, evt, worldX, worldY);
						rst.setPointAt(p, targetThingIndex);
					}
					else{
						if(!stickyEndpoints){
							rst.setPointAt(pt, targetThingIndex);
						}
						else{
							if(targetThing instanceof IStickyEndpointsSpline){									
								int numPoints = rst.getNumPoints();
								if((targetThingIndex == 0) || (targetThingIndex == (numPoints - 1))){
									//It's an endpoint
									StickyBit sb = snapEndpointToStickyBox(c, rh, rst, evt, worldX, worldY);
									Point p = sb.stickyPoint;
									if(targetThingIndex == 0){
										if(sb.stickyThing == null){
											((IStickyEndpointsSpline)targetThing).setFirstEndpointStuckToID(null);
										}
										else{
											((IStickyEndpointsSpline)targetThing).setFirstEndpointStuckToID(sb.stickyThing.getID());
										}
									}
									else{
										if(sb.stickyThing == null){
											((IStickyEndpointsSpline)targetThing).setSecondEndpointStuckToID(null);
										}
										else{
											//System.out.println("second endpoint stuck to " + sb.stickyThing);
											((IStickyEndpointsSpline)targetThing).setSecondEndpointStuckToID(sb.stickyThing.getID());
										}
									}
									rst.setPointAt(p, targetThingIndex);
								}
								else{
									rst.setPointAt(pt, targetThingIndex);
								}						
							}
							else{
								rst.setPointAt(pt, targetThingIndex);
							}						
						}
					}
				}
			}
		}
		lastMouseX = evt.getX();
		lastMouseY = evt.getY();
	}

}
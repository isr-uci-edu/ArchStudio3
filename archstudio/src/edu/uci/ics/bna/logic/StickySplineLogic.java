package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.BNAModel;
import edu.uci.ics.bna.BNAModelEvent;
import edu.uci.ics.bna.IStickyBoxBounded;
import edu.uci.ics.bna.IStickyEndpointsSpline;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.ThingEvent;
import edu.uci.ics.bna.ThingLogicAdapter;
import edu.uci.ics.bna.BNAUtils;

public class StickySplineLogic extends ThingLogicAdapter implements StickyBoxTrackingListener{
	
	//Maps endpoint things to a set of the splines that they are stuck to
	protected Map endpointIDToSplineMap = Collections.synchronizedMap(new HashMap());
	
	//Maps endpoint things to their last known sticky box (Rectangle) so we
	//can track its shape changes
	//protected Map endpointIDToStickyBoxMap = Collections.synchronizedMap(new HashMap());
	
	protected StickyBoxTrackingLogic stickyBoxTrackingLogic;
	
	public StickySplineLogic(StickyBoxTrackingLogic stickyBoxTrackingLogic){
		this.stickyBoxTrackingLogic = stickyBoxTrackingLogic;
		stickyBoxTrackingLogic.addStickyBoxTrackingListener(this);
	}
	
	protected HashSet getSplinesForEndpoint(Thing endpoint){
		HashSet s = (HashSet)endpointIDToSplineMap.get(endpoint.getID());
		if(s == null){
			s = new HashSet();
			endpointIDToSplineMap.put(endpoint.getID(), s);
		}
		return s;
	}
	
	//src = spline
	
	protected boolean splineStuckTo(IStickyEndpointsSpline spline, IStickyBoxBounded endpoint){
		String endpointID = ((Thing)endpoint).getID();
		String firstID = spline.getFirstEndpointStuckToID();
		if((firstID != null) && (firstID.equals(endpointID))){
			return true;
		}
		String secondID = spline.getSecondEndpointStuckToID();
		if((secondID != null) && (secondID.equals(endpointID))){
			return true;
		}
		return false;
	}
		
	protected void handleSplineEvent(BNAModel m, ThingEvent evt){
		Thing spline = evt.getTargetThing();
		String propertyName = evt.getPropertyName();
		if((propertyName.equals("firstEndpointStuckToID")) || (propertyName.equals("secondEndpointStuckToID"))){
			String oldID = (String)evt.getOldPropertyValue();
			String newID = (String)evt.getNewPropertyValue();
			
			if(oldID != null){
				Thing oldEndpoint = m.getThing(oldID);
				
				if(oldEndpoint != null){
					HashSet s = getSplinesForEndpoint(oldEndpoint);
					//We have to check if some other endpoint (not this one) is still stuck to the
					//endpoint or else we might remove the association inadvertently
					if(!splineStuckTo((IStickyEndpointsSpline)spline, (IStickyBoxBounded)oldEndpoint)){
						s.remove(spline);
					}
				
					if(s.size() == 0){
						endpointIDToSplineMap.remove(oldEndpoint.getID());
					}
					else{
						endpointIDToSplineMap.put(oldEndpoint.getID(), s);
						bnaComponent.getModel().stackAbove(oldEndpoint, spline);
					}
				}
			}
			if(newID != null){
				Thing newEndpoint = m.getThing(newID);
				if(newEndpoint != null){
					HashSet s = getSplinesForEndpoint(newEndpoint);
					s.add(spline);
					endpointIDToSplineMap.put(newEndpoint.getID(), s);
					bnaComponent.getModel().stackAbove(newEndpoint, spline);
				}
			}
		}
	}
	
	protected void handleEndpointEvent(BNAModel m, StickyBoxChangedEvent evt){
		IStickyBoxBounded sbbt = (IStickyBoxBounded)evt.getTargetThing();
		Rectangle newStickyBox = evt.getNewStickyBox();
		Rectangle oldStickyBox = evt.getOldStickyBox();
		if(newStickyBox == null){
			return;
		}
		
		//System.out.println("oldStickyBox=" + oldStickyBox);
		//System.out.println("newStickyBox=" + newStickyBox);
		
		if(oldStickyBox == null){
			return;
		}
		
		if(oldStickyBox.equals(newStickyBox)){
			return;
		}
		
		Set splines = new HashSet(getSplinesForEndpoint((Thing)sbbt));
		
		for(Iterator it = splines.iterator(); it.hasNext(); ){
			IStickyEndpointsSpline sest = (IStickyEndpointsSpline)it.next();
			String firstID = sest.getFirstEndpointStuckToID();
			if((firstID != null) && (firstID.equals(((Thing)sbbt).getID()))){
				Point p = sest.getPointAt(0);
				Point p2 = BNAUtils.scaleAndMoveBorderPoint(p, oldStickyBox, newStickyBox);
				sest.setPointAt(p2, 0);
			}
			
			String secondID= sest.getSecondEndpointStuckToID();
			if((secondID != null) && (secondID.equals(((Thing)sbbt).getID()))){
				int numPoints = sest.getNumPoints();
				Point p = sest.getPointAt(numPoints - 1);
				Point p2 = BNAUtils.scaleAndMoveBorderPoint(p, oldStickyBox, newStickyBox);
				sest.setPointAt(p2, numPoints - 1);
			}
		}
	}

	public void stickyBoxChanged(StickyBoxChangedEvent evt){
		handleEndpointEvent(bnaComponent.getModel(), evt);
	}
	
	public void init(){
		init(getBNAComponent().getModel());
	}
	
	protected void init(BNAModel m){
		for(Iterator it = m.getThingIterator(); it.hasNext(); ){
			Thing t = (Thing)it.next();
			if(t instanceof IStickyEndpointsSpline){
				String firstEndpointStuckToID = (String)t.getProperty("firstEndpointStuckToID");
				HashSet s1 = (HashSet)endpointIDToSplineMap.get(firstEndpointStuckToID);
				if(s1 == null){
					s1 = new HashSet();
				}
				s1.add(t);
				endpointIDToSplineMap.put(firstEndpointStuckToID, s1);

				String secondEndpointStuckToID = (String)t.getProperty("secondEndpointStuckToID");
				HashSet s2 = (HashSet)endpointIDToSplineMap.get(secondEndpointStuckToID);
				if(s2 == null){
					s2 = new HashSet();
				}
				s2.add(t);
				endpointIDToSplineMap.put(secondEndpointStuckToID, s2);
			}
		}
	}

	public void bnaModelChanged(BNAModelEvent evt){
		int eventType = evt.getEventType();
		if(eventType == BNAModelEvent.THING_CHANGED){
			Thing targetThing = evt.getTargetThing();
			if(targetThing instanceof IStickyEndpointsSpline){
				IStickyEndpointsSpline sest = (IStickyEndpointsSpline)targetThing;
				ThingEvent tevt = evt.getThingEvent();
				handleSplineEvent(evt.getSource(), tevt);
			}
		}
		else if(eventType == BNAModelEvent.THING_REMOVED){
			Thing removedThing = evt.getTargetThing();
			if(removedThing instanceof IStickyBoxBounded){
				HashSet splines = getSplinesForEndpoint(removedThing);
				String removedThingID = removedThing.getID();
				synchronized(splines){
					for(Iterator it = splines.iterator(); it.hasNext(); ){
						IStickyEndpointsSpline sest = (IStickyEndpointsSpline)it.next();
						String firstID = sest.getFirstEndpointStuckToID();
						String secondID = sest.getSecondEndpointStuckToID();
						if((firstID != null) && (firstID.equals(removedThingID))){
							sest.setFirstEndpointStuckToID(null);
						}
						if((secondID != null) && (secondID.equals(removedThingID))){
							sest.setSecondEndpointStuckToID(null);
						}
					}
				}
			}
		}
	}
}

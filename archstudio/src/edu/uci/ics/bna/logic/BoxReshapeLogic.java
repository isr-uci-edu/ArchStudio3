package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import edu.uci.ics.bna.*;

public class BoxReshapeLogic extends ThingLogicAdapter implements SelectionTrackingListener, BoundingBoxTrackingListener{

	protected Thing grabbedThing = null;
	protected Thing currentlyResizingThing = null;
	protected int lastMouseX = -1;
	protected int lastMouseY = -1;

	protected SelectionTrackingLogic selectionTrackingLogic;
	protected BoundingBoxTrackingLogic boundingBoxTrackingLogic;
	
	public BoxReshapeLogic(SelectionTrackingLogic selectionTrackingLogic, BoundingBoxTrackingLogic boundingBoxTrackingLogic){
		this.selectionTrackingLogic = selectionTrackingLogic;
		this.boundingBoxTrackingLogic = boundingBoxTrackingLogic;
	}
	
	public void init(){
		selectionTrackingLogic.addSelectionTrackingListener(this);
		boundingBoxTrackingLogic.addBoundingBoxTrackingListener(this);
		checkSelections(getBNAComponent().getModel());
	}
	
	public void destroy() {
		grabbedThing = null;
		selectionTrackingLogic.removeSelectionTrackingListener(this);
		boundingBoxTrackingLogic.removeBoundingBoxTrackingListener(this);
		super.destroy();
	}
	
	public void selectionChanged(SelectionChangedEvent evt){
		checkSelections(bnaComponent.getModel());
	}
	
	public void boundingBoxChanged(BoundingBoxChangedEvent evt){
		Thing targetThing = evt.getTargetThing();
		if(currentlyResizingThing != null){
			if(targetThing.getID().equals(currentlyResizingThing.getID())){
				updateResizeHandles(bnaComponent.getModel(), currentlyResizingThing);
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
			m.removeThing(resizeHandleID);
		}
		t.removeProperty("$resizeHandleIDs");
	}
	
	public void updateResizeHandles(BNAModel m, Thing t){
		if(!(t instanceof IBoxBounded)){
			return;
		}
		if(!(t instanceof IResizableBoxBounded)){
			return;
		}
		
		Set currentResizeHandleIDs = t.getSetProperty("$resizeHandleIDs");
		Rectangle r = BNAUtils.normalizeRectangle(((IBoxBounded)t).getBoundingBox());
		
		ReshapeHandleThing rhNW = null;
		ReshapeHandleThing rhNE = null;
		ReshapeHandleThing rhSW = null;
		ReshapeHandleThing rhSE = null;
		
		ReshapeHandleThing rhN = null;
		ReshapeHandleThing rhS = null;
		ReshapeHandleThing rhE = null;
		ReshapeHandleThing rhW = null;
			
		if(currentResizeHandleIDs.size() == 0){
			//Must create resize handles
			rhNW = new ReshapeHandleThing();
			rhNW.setOrientation(ReshapeHandleThing.ORIENTATION_NW);
			rhNW.setTargetThingID(t.getID());
			BNAUtils.setStackingPriority(rhNW, BNAUtils.STACKING_PRIORITY_ALWAYS_ON_TOP);
			m.addThing(rhNW);
		
			rhNE = new ReshapeHandleThing();
			rhNE.setOrientation(ReshapeHandleThing.ORIENTATION_NE);
			rhNE.setTargetThingID(t.getID());
			BNAUtils.setStackingPriority(rhNE, BNAUtils.STACKING_PRIORITY_ALWAYS_ON_TOP);
			m.addThing(rhNE);
			
			rhSW = new ReshapeHandleThing();
			rhSW.setOrientation(ReshapeHandleThing.ORIENTATION_SW);
			rhSW.setTargetThingID(t.getID());
			BNAUtils.setStackingPriority(rhSW, BNAUtils.STACKING_PRIORITY_ALWAYS_ON_TOP);
			m.addThing(rhSW);

			rhSE = new ReshapeHandleThing();
			rhSE.setOrientation(ReshapeHandleThing.ORIENTATION_SE);
			rhSE.setTargetThingID(t.getID());
			BNAUtils.setStackingPriority(rhSE, BNAUtils.STACKING_PRIORITY_ALWAYS_ON_TOP);
			m.addThing(rhSE);

			rhN = new ReshapeHandleThing();
			rhN.setOrientation(ReshapeHandleThing.ORIENTATION_N);
			rhN.setTargetThingID(t.getID());
			BNAUtils.setStackingPriority(rhN, BNAUtils.STACKING_PRIORITY_ALWAYS_ON_TOP);
			m.addThing(rhN);
			
			rhS = new ReshapeHandleThing();
			rhS.setOrientation(ReshapeHandleThing.ORIENTATION_S);
			rhS.setTargetThingID(t.getID());
			BNAUtils.setStackingPriority(rhS, BNAUtils.STACKING_PRIORITY_ALWAYS_ON_TOP);
			m.addThing(rhS);
			
			rhE = new ReshapeHandleThing();
			rhE.setOrientation(ReshapeHandleThing.ORIENTATION_E);
			rhE.setTargetThingID(t.getID());
			BNAUtils.setStackingPriority(rhE, BNAUtils.STACKING_PRIORITY_ALWAYS_ON_TOP);
			m.addThing(rhE);
			
			rhW = new ReshapeHandleThing();
			rhW.setOrientation(ReshapeHandleThing.ORIENTATION_W);
			rhW.setTargetThingID(t.getID());
			BNAUtils.setStackingPriority(rhW, BNAUtils.STACKING_PRIORITY_ALWAYS_ON_TOP);
			m.addThing(rhW);
		}
		else{
			for(Iterator it = currentResizeHandleIDs.iterator(); it.hasNext(); ){
				String id = (String)it.next();
				ReshapeHandleThing rh = (ReshapeHandleThing)m.getThing(id);
				if(rh == null){
					throw new RuntimeException("Missing reshape handle.");
				}
				switch(rh.getOrientation()){
				case ReshapeHandleThing.ORIENTATION_NW:
					rhNW = rh;
					break;
				case ReshapeHandleThing.ORIENTATION_NE:
					rhNE = rh;
					break;
				case ReshapeHandleThing.ORIENTATION_SW:
					rhSW = rh;
					break;
				case ReshapeHandleThing.ORIENTATION_SE:
					rhSE = rh;
					break;
				case ReshapeHandleThing.ORIENTATION_N:
					rhN = rh;
					break;
				case ReshapeHandleThing.ORIENTATION_E:
					rhE = rh;
					break;
				case ReshapeHandleThing.ORIENTATION_S:
					rhS = rh;
					break;
				case ReshapeHandleThing.ORIENTATION_W:
					rhW = rh;
					break;
				}
			}
		}
		
		rhNW.setX(r.x);
		rhNW.setY(r.y);
		rhNE.setX(r.x + r.width + 1);
		rhNE.setY(r.y);
		rhSW.setX(r.x);
		rhSW.setY(r.y + r.height + 1);
		rhSE.setX(r.x + r.width + 1);
		rhSE.setY(r.y + r.height + 1);
		rhN.setX(r.x + (r.width / 2));
		rhN.setY(r.y);
		rhS.setX(r.x + (r.width / 2));
		rhS.setY(r.y + r.height + 1);
		rhE.setX(r.x + r.width + 1);
		rhE.setY(r.y + (r.height / 2));
		rhW.setX(r.x);
		rhW.setY(r.y + (r.height / 2));
		
		HashSet ns = new HashSet();
		ns.add(rhNW.getID());
		ns.add(rhNE.getID());
		ns.add(rhSW.getID());
		ns.add(rhSE.getID());
		ns.add(rhN.getID());
		ns.add(rhE.getID());
		ns.add(rhS.getID());
		ns.add(rhW.getID());
				
		t.replaceSetPropertyValues("$resizeHandleIDs", ns);
	}
	
	public synchronized void checkSelections(BNAModel m){
		Thing[] selectedThings = selectionTrackingLogic.getSelectedThings();
		
		if(selectedThings.length != 1){
			if(currentlyResizingThing != null){
				removeResizeHandles(m, currentlyResizingThing);
				currentlyResizingThing = null;
			}
		}
		else{
			//There is exactly one selected thing.
			Thing selectedThing = selectedThings[0];
			if(currentlyResizingThing != selectedThing){
				if(currentlyResizingThing != null){
					removeResizeHandles(m, currentlyResizingThing);
				}
				if(selectedThing instanceof IResizableBoxBounded){
					currentlyResizingThing = selectedThing;
				}
				else{
					currentlyResizingThing = null;
				}
			}
			updateResizeHandles(m, currentlyResizingThing);
		}
	}


	public void mousePressed(Thing t, MouseEvent evt, int worldX, int worldY){
		if (t instanceof IUserEditable) {
			IUserEditable et = (IUserEditable) t;
			if (!et.isUserEditable())
				return;
		}
		if(evt.getButton() == MouseEvent.BUTTON1){
			grabbedThing = t;
			lastMouseX = evt.getX();
			lastMouseY = evt.getY();
		}
	}
	
	public void mouseReleased(Thing t, MouseEvent evt, int worldX, int worldY){
		grabbedThing = null;
	}
	
	public void mouseDragged(Thing t, MouseEvent evt, int worldX, int worldY){
		BNAComponent c = bnaComponent;
		if(grabbedThing != null){
			if(grabbedThing instanceof ReshapeHandleThing){
				ReshapeHandleThing rh = (ReshapeHandleThing)grabbedThing;
				String targetID = rh.getTargetThingID();
				Thing targetThing = c.getModel().getThing(targetID);
				if((targetThing != null) && (targetThing instanceof IResizableBoxBounded)){
					IResizableBoxBounded b = (IResizableBoxBounded)targetThing;
					
					Rectangle currentBoundingBox = b.getBoundingBox();
					Rectangle newBoundingBox = new Rectangle(currentBoundingBox);
					
					int curX1 = currentBoundingBox.x;
					int curY1 = currentBoundingBox.y;
					int curX2 = currentBoundingBox.x + currentBoundingBox.width;
					int curY2 = currentBoundingBox.y + currentBoundingBox.height;
					
					switch(rh.getOrientation()){
					case ReshapeHandleThing.ORIENTATION_NW:
						newBoundingBox.x = worldX + 1;
						newBoundingBox.y = worldY + 1;
						newBoundingBox.width = curX2 - newBoundingBox.x;
						newBoundingBox.height = curY2 - newBoundingBox.y;
						break;
					case ReshapeHandleThing.ORIENTATION_NE:
						//newBoundingBox.x = worldX + 1;
						newBoundingBox.y = worldY + 1;
						newBoundingBox.width = worldX - curX1;
						newBoundingBox.height = curY2 - newBoundingBox.y;
						break;
					case ReshapeHandleThing.ORIENTATION_SE:
						newBoundingBox.width = worldX - curX1;
						newBoundingBox.height = worldY - curY1;
						break;
					case ReshapeHandleThing.ORIENTATION_SW:
						newBoundingBox.x = worldX + 1;
						newBoundingBox.width = curX2 - newBoundingBox.x;
						newBoundingBox.height = worldY - curY1;
						break;
					case ReshapeHandleThing.ORIENTATION_N:
						newBoundingBox.y = worldY + 1;
						newBoundingBox.height = curY2 - newBoundingBox.y;
						break;
					case ReshapeHandleThing.ORIENTATION_S:
						newBoundingBox.height = worldY - curY1;
						break;
					case ReshapeHandleThing.ORIENTATION_W:
						newBoundingBox.x = worldX + 1;
						newBoundingBox.width = curX2 - newBoundingBox.x;
						break;
					case ReshapeHandleThing.ORIENTATION_E:
						newBoundingBox.width = worldX - curX1;
						break;
					}
					
					b.setBoundingBox(newBoundingBox);						
				}
			}
		}
		lastMouseX = evt.getX();
		lastMouseY = evt.getY();
	}
	

}
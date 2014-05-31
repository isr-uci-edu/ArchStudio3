package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.logic.*;

public class MoveEndpointLogic extends ThingLogicAdapter implements BoundingBoxTrackingListener{

	protected boolean initialized;

	protected Thing grabbedThing = null;
	protected int lastMouseButton = -1;
	protected int lastMouseX = -1;
	protected int lastMouseY = -1;
	
	protected BoundingBoxTrackingLogic boundingBoxTrackingLogic;
	
	//Maps IBoxBounded IDs to a set of the IDs of the endpoints that are stuck to them
	protected Map thingIDtoEndpointIDSetMap = Collections.synchronizedMap(new HashMap());
	
	public MoveEndpointLogic(BoundingBoxTrackingLogic boundingBoxTrackingLogic){
		this.boundingBoxTrackingLogic = boundingBoxTrackingLogic;
		boundingBoxTrackingLogic.addBoundingBoxTrackingListener(this);
	}

	public void destroy() {
		grabbedThing = null;
		initialized = false;
		super.destroy();
	}

	public void init(BNAModel m){
		for(Iterator it = m.getThingIterator(); it.hasNext(); ){
			Thing targetThing = (Thing)it.next();
			if(targetThing instanceof EndpointThing){
				String bbtID = ((EndpointThing)targetThing).getTargetThingID();
				if(bbtID != null){
					IBoxBounded bbt = (IBoxBounded)bnaComponent.getModel().getThing(bbtID);
					setStuckTo(bbt, (EndpointThing)targetThing);
					//moveEndpointTo((EndpointThing)targetThing, bbt.getBoundingBox(), ((EndpointThing)targetThing).getX(), ((EndpointThing)targetThing).getY());
					
					//moveEndpointTo((EndpointThing)targetThing, bbt.getBoundingBox(), CoordinateMapper.WORLD_CENTER_X, CoordinateMapper.WORLD_CENTER_Y);
					//bnaComponent.getModel().stackAbove(targetThing, bbt);
				}
			}
		}
		initialized = true;
	}
	
	public void init(){
		this.init(getBNAComponent().getModel());
	}
	
	public void bnaModelChanged(BNAModelEvent evt){
		int evtType = evt.getEventType();
		if(evtType == BNAModelEvent.THING_ADDED){
			Thing targetThing = evt.getTargetThing();
			if(targetThing instanceof EndpointThing){
				String bbtID = ((EndpointThing)targetThing).getTargetThingID();
				if(bbtID != null){
					IBoxBounded bbt = (IBoxBounded)bnaComponent.getModel().getThing(bbtID);
					setStuckTo(bbt, (EndpointThing)targetThing);
					moveEndpointTo((EndpointThing)targetThing, bbt.getBoundingBox(), 
						(bnaComponent == null) ? (DefaultCoordinateMapper.DEFAULT_WORLD_WIDTH / 2) :
						bnaComponent.getCoordinateMapper().getWorldCenterX(), 
						(bnaComponent == null) ? (DefaultCoordinateMapper.DEFAULT_WORLD_HEIGHT / 2) :
						bnaComponent.getCoordinateMapper().getWorldCenterX());
					bnaComponent.getModel().stackAbove(targetThing, bbt);
				}
			}
		}
		else if(evtType == BNAModelEvent.THING_CHANGED){
			Thing targetThing = evt.getTargetThing();
			if(targetThing instanceof EndpointThing){
				ThingEvent tevt = evt.getThingEvent();
				if(tevt.getPropertyName().equals("targetThingID")){
					String bbtID = ((EndpointThing)targetThing).getTargetThingID();
					if(bbtID != null){
						IBoxBounded bbt = (IBoxBounded)bnaComponent.getModel().getThing(bbtID);
						setStuckTo(bbt, (EndpointThing)targetThing);
						int epX = ((EndpointThing)targetThing).getX();
						int epY = ((EndpointThing)targetThing).getY();
						int epCenterX = epX + (((EndpointThing)targetThing).getWidth() / 2);
						int epCenterY = epY + (((EndpointThing)targetThing).getHeight() / 2);
						
						moveEndpointTo((EndpointThing)targetThing, bbt.getBoundingBox(), epCenterX, epCenterY);
						bnaComponent.getModel().stackAbove(targetThing, bbt);
					}
				}
			}
		}
		else if(evtType == BNAModelEvent.THING_REMOVED){
			Thing targetThing = evt.getTargetThing();
			if(targetThing instanceof EndpointThing){
				endpointRemoved((EndpointThing)targetThing);
			}
			if(targetThing instanceof IBoxBounded){
				String bbtID = targetThing.getID();
				synchronized(thingIDtoEndpointIDSetMap){
					EndpointThing[] endpoints = getStuckTo((IBoxBounded)targetThing);
					for(int i = 0; i < endpoints.length; i++){
						if(endpoints[i] == null)
							continue; // already removed (?)
						bnaComponent.getModel().removeThing(endpoints[i].getID());
					}
					thingIDtoEndpointIDSetMap.remove(bbtID);
				}
			}
		}
	}
	
	public void boundingBoxChanged(BoundingBoxChangedEvent evt){
		Rectangle oldBoundingBox = evt.getOldBoundingBox();
		Rectangle newBoundingBox = evt.getNewBoundingBox();
		if(oldBoundingBox == null){
			return;
		}
		if(newBoundingBox == null){
			return;
		}
			
		IBoxBounded bbt = (IBoxBounded)evt.getTargetThing();
		EndpointThing[] stuckEndpoints = getStuckTo(bbt);
		for(int i = 0; i < stuckEndpoints.length; i++){
			Rectangle oldStuckEndpointBoundingBox = stuckEndpoints[i].getBoundingBox();
			int oldCenterX = oldStuckEndpointBoundingBox.x + (oldStuckEndpointBoundingBox.width / 2);
			int oldCenterY = oldStuckEndpointBoundingBox.y + (oldStuckEndpointBoundingBox.height / 2);
			Point newCenterPoint = BNAUtils.scaleAndMoveBorderPoint(new Point(oldCenterX, oldCenterY), oldBoundingBox, newBoundingBox);
			moveEndpointTo(stuckEndpoints[i], newBoundingBox, newCenterPoint.x, newCenterPoint.y);
		}
	}
	
	protected EndpointThing[] getStuckTo(IBoxBounded boxThing){
		String id = boxThing.getID();
		HashSet s = (HashSet)thingIDtoEndpointIDSetMap.get(id);
		if(s == null){
			return new EndpointThing[0];
		}
		String[] endpointIDs = (String[])s.toArray(new String[0]);
		EndpointThing[] endpoints = new EndpointThing[endpointIDs.length];
		for(int i = 0; i < endpoints.length; i++){
			endpoints[i] = (EndpointThing)bnaComponent.getModel().getThing(endpointIDs[i]);
		}
		return endpoints;
	}
	
	protected void setStuckTo(IBoxBounded boxThing, EndpointThing endpointThing){
		String boxThingID = boxThing.getID();
		String endpointThingID = endpointThing.getID();
		synchronized(thingIDtoEndpointIDSetMap){
			HashSet s = (HashSet)thingIDtoEndpointIDSetMap.get(boxThingID);
			if(s == null){
				s = new HashSet();
			}
			s.add(endpointThingID);
			thingIDtoEndpointIDSetMap.put(boxThingID, s);
		}
	}
	
	protected void endpointRemoved(EndpointThing ep){
		String endpointID = ep.getID();
		synchronized(thingIDtoEndpointIDSetMap){
			for(Iterator it = thingIDtoEndpointIDSetMap.keySet().iterator(); it.hasNext(); ){
				String key = (String)it.next();
				HashSet s = (HashSet)thingIDtoEndpointIDSetMap.get(key);
				s.remove(endpointID);
			}
		}
	}

	public void mousePressed(Thing t, MouseEvent evt, int worldX, int worldY){
		if (t instanceof IUserEditable) {
			IUserEditable et = (IUserEditable) t;
			if (!et.isUserEditable())
				return;
		}
		grabbedThing = t;
		lastMouseButton = evt.getButton();
		lastMouseX = evt.getX();
		lastMouseY = evt.getY();
	}
	
	public void mouseReleased(Thing t, MouseEvent evt, int worldX, int worldY){
		grabbedThing = null;
	}
	
	public static void moveEndpointTo(EndpointThing ep, Rectangle r, int worldX, int worldY){
		Line2D[] lines = new Line2D[4];
		
		lines[0] = new Line2D.Double(r.x, r.y, r.x + r.width, r.y);
		lines[1] = new Line2D.Double(r.x, r.y, r.x, r.y + r.height);
		lines[2] = new Line2D.Double(r.x + r.width, r.y, r.x + r.width, r.y + r.height);
		lines[3] = new Line2D.Double(r.x, r.y + r.height, r.x + r.width, r.y + r.height);
		
		int closestLine = -1;
		int closestDist = Integer.MAX_VALUE;
		
		for(int i = 0; i < 4; i++){
			int dist = BNAUtils.round(lines[i].ptSegDist(worldX, worldY));
			if(dist < closestDist){
				closestLine = i;
				closestDist = dist;
			}
		}
		
		int epCenterWorldX;
		int epCenterWorldY;
		
		Line2D cline = lines[closestLine];
		if(cline.getX1() == cline.getX2()){
			//Vertical Line
			epCenterWorldX = (int)cline.getX1();
			epCenterWorldY = worldY;
			if(epCenterWorldY < cline.getY1()){
				epCenterWorldY = (int)cline.getY1();
			}
			else if(epCenterWorldY > (cline.getY2() - 1)){
				epCenterWorldY = (int)cline.getY2();
			}
		}
		else{
			//Horizontal line
			epCenterWorldY = (int)cline.getY1();
			epCenterWorldX = worldX;
			if(epCenterWorldX < cline.getX1()){
				epCenterWorldX = (int)cline.getX1();
			}
			else if(epCenterWorldX > (cline.getX2() - 1)){
				epCenterWorldX = (int)cline.getX2();
			}
		}
		
		int epWorldX = epCenterWorldX - (ep.getWidth() / 2);
		int epWorldY = epCenterWorldY - (ep.getHeight() / 2);
		
		ep.setX(epWorldX);
		ep.setY(epWorldY);
		switch(closestLine){
		case 0:
			ep.setOrientation(EndpointThing.ORIENTATION_N);
			break;
		case 1:
			ep.setOrientation(EndpointThing.ORIENTATION_W);
			break;
		case 2:
			ep.setOrientation(EndpointThing.ORIENTATION_E);
			break;
		case 3:
			ep.setOrientation(EndpointThing.ORIENTATION_S);
			break;
		}
	}
	
	public void mouseDragged(Thing t, MouseEvent evt, int worldX, int worldY){
		BNAComponent c = bnaComponent;
		if(lastMouseButton == MouseEvent.BUTTON1){
			if(grabbedThing != null){
				if(grabbedThing instanceof EndpointThing){
					EndpointThing ep = (EndpointThing)grabbedThing;
					String targetThingID = ep.getTargetThingID();
					if(targetThingID != null){
						Thing targetThing = c.getModel().getThing(targetThingID);
						if((targetThing != null) && (targetThing instanceof IBoxBounded)){
							IBoxBounded bbt = (IBoxBounded)targetThing;
							Rectangle r = bbt.getBoundingBox();
							moveEndpointTo(ep, r, worldX, worldY);
						}
					}
				}
			}
		}
		lastMouseX = evt.getX();
		lastMouseY = evt.getY();
	}
	

}
package archstudio.comp.archipelago.types;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.logic.*;
import edu.uci.ics.bna.thumbnail.Thumbnail;

public class MaintainInterfaceInterfaceMappingLogic extends ThingLogicAdapter /*implements CoordinateMapperListener*/{

	protected BoundingBoxTrackingLogic boundingBoxTrackingLogic;

	//Keeps all interface interface mappings
	protected Set interfaceInterfaceMappingSet;
	
	public MaintainInterfaceInterfaceMappingLogic(){
		interfaceInterfaceMappingSet = new HashSet();
	}

	public void init(){
		init(getBNAComponent().getModel());
	}
	
	double oldScale = -1.0d;
	public void coordinateMappingsChanged(CoordinateMapperEvent evt){
		double newScale = evt.getNewScale();
		if(newScale != oldScale){
			updateAllInterfaceInterfaceMappingThings();
		}
		oldScale = newScale;
	}

	public void init(BNAModel m){
		interfaceInterfaceMappingSet.clear();
		for(Iterator it = m.getThingIterator(); it.hasNext(); ){
			Thing targetThing = (Thing)it.next();
			if(targetThing instanceof InterfaceInterfaceMappingThing){
				interfaceInterfaceMappingSet.add(targetThing);
				updateInterfaceInterfaceMapping((InterfaceInterfaceMappingThing)targetThing);
			}
		}
	}
	
	/*
	public void destroy(){
		getBNAComponent().getCoordinateMapper().removeCoordinateMapperListener(this);
	}
	*/
	
	protected void updateInterfaceInterfaceMapping(InterfaceInterfaceMappingThing simt){
		BNAComponent c = super.getBNAComponent();
		if(c == null){
			return;
		}
		
		//System.out.println("updating iim " + simt);
		String brickThingID = simt.getBrickThingID();
		String outerInterfaceThingID = simt.getOuterInterfaceThingID();
		String innerInterfaceThingID = simt.getInnerInterfaceThingID();
		
		if((brickThingID == null) || (outerInterfaceThingID == null) || (innerInterfaceThingID == null)){
			return;
		}
		
		//System.out.println("got here 1");
		
		BrickThing bt = null;
		InterfaceThing oit = null;
		InterfaceThing iit = null;

		Thing t = c.getModel().getThing(brickThingID);
		if((t == null) || (!(t instanceof BrickThing))){
			return;
		}
		bt = (BrickThing)t;
		
		t = c.getModel().getThing(outerInterfaceThingID);
		if((t == null) || (!(t instanceof InterfaceThing))){
			return;
		}
		oit = (InterfaceThing)t;
		
		Rectangle oitBoundingBox = oit.getBoundingBox();
		int oitcx = oitBoundingBox.x + (oitBoundingBox.width / 2);
		int oitcy = oitBoundingBox.y + (oitBoundingBox.height / 2);
		
		simt.setFirstPoint(oitcx, oitcy);
		
		Rectangle thumbnailBox = BoxThingPeer.getThumbnailRectangle(bt, c.getCoordinateMapper());
		if(thumbnailBox == null){
			return;
		}
		
		//System.out.println("got here 2");

		Thumbnail thumbnail = bt.getThumbnail();
		thumbnail.setupCoordinateMapper(thumbnailBox.x, thumbnailBox.y, 
			thumbnailBox.width, thumbnailBox.height);
		
		t = thumbnail.getModel().getThing(innerInterfaceThingID);
		if((t == null) || (!(t instanceof InterfaceThing))){
			return;
		}
		iit = (InterfaceThing)t;
		
		//System.out.println("got here 3");
		
		Rectangle interfaceWorldBoundingBox = iit.getBoundingBox();
		
		int wcix = interfaceWorldBoundingBox.x + (interfaceWorldBoundingBox.width / 2);
		int wciy = interfaceWorldBoundingBox.y + (interfaceWorldBoundingBox.height / 2);
		
		//(wcix, wciy) is the center point in the thumbnail-world of the interface.
		//Luckily, the thumb has the magic coordinate mapper set up to map 
		//thumbnail world coordinates to current local coordinates.
		int lcix = thumbnail.getCoordinateMapper().worldXtoLocalX(wcix);
		int lciy = thumbnail.getCoordinateMapper().worldYtoLocalY(wciy);
		
		//Unfortunately, that coordinate mapper maps to coordinates relative
		//to the thumbnail box, so we have to transpose
		lcix += thumbnailBox.x;
		lciy += thumbnailBox.y;
		
		//System.out.println("got here 4");
		
		//(lcix, lciy) is the current-local centerpoint of the interface
		//Now we have to convert it to current-world coordinate.
		int cwcix = c.getCoordinateMapper().localXtoWorldX(lcix); 
		int cwciy = c.getCoordinateMapper().localYtoWorldY(lciy); 
		
		//System.out.println("cwcix = " + cwcix);
		//System.out.println("cwciy = " + cwciy);
		simt.setSecondPoint(cwcix, cwciy);
	}

	public void updateAllInterfaceInterfaceMappingThings(){
		for(Iterator it = interfaceInterfaceMappingSet.iterator(); it.hasNext(); ){
			InterfaceInterfaceMappingThing iimt = (InterfaceInterfaceMappingThing)it.next();
			//String iimtbtid = iimt.getBrickThingID();
			updateInterfaceInterfaceMapping(iimt);
		}
	}

	public void updateAllInterfaceInterfaceMappingThings(BrickThing bt){
		String btID = bt.getID();
		if(btID != null){
			InterfaceInterfaceMappingThing[] iimts = 
				(InterfaceInterfaceMappingThing[])interfaceInterfaceMappingSet.toArray(new InterfaceInterfaceMappingThing[0]);
			for(int i = 0; i < iimts.length; i++){
				String iimtbtid = iimts[i].getBrickThingID();
				if((iimtbtid != null) && iimtbtid.equals(btID)){
					updateInterfaceInterfaceMapping(iimts[i]);
				}
			}
		}
	}
	
	public void updateAllInterfaceInterfaceMappingThings(InterfaceThing oit){
		String oitID = oit.getID();
		if(oitID != null){
			InterfaceInterfaceMappingThing[] iimts = 
				(InterfaceInterfaceMappingThing[])interfaceInterfaceMappingSet.toArray(new InterfaceInterfaceMappingThing[0]);
			for(int i = 0; i < iimts.length; i++){
				String iimtbtid = iimts[i].getOuterInterfaceThingID();
				if((iimtbtid != null) && iimtbtid.equals(oitID)){
					updateInterfaceInterfaceMapping(iimts[i]);
				}
			}
		}
	}
	
	public void bnaModelChanged(BNAModelEvent evt){
		int evtType = evt.getEventType();
		if(evtType == BNAModelEvent.THING_ADDED){
			Thing targetThing = evt.getTargetThing();
			if(targetThing instanceof InterfaceInterfaceMappingThing){
				updateInterfaceInterfaceMapping((InterfaceInterfaceMappingThing)targetThing);
				interfaceInterfaceMappingSet.add(targetThing);
			}
			else if(targetThing instanceof BrickThing){
				updateAllInterfaceInterfaceMappingThings((BrickThing)targetThing);
			}
		}
		else if(evtType == BNAModelEvent.THING_CHANGED){
			Thing targetThing = evt.getTargetThing();
			if(targetThing instanceof InterfaceInterfaceMappingThing){
				ThingEvent tevt = evt.getThingEvent();
				if(tevt != null){
					String propName = tevt.getPropertyName();
					if(propName != null){
						if(propName.equals(InterfaceInterfaceMappingThing.BRICK_THING_ID_PROPERTY_NAME)){
							updateInterfaceInterfaceMapping((InterfaceInterfaceMappingThing)targetThing);
						}
						else if(propName.equals(InterfaceInterfaceMappingThing.OUTER_INTERFACE_THING_ID_PROPERTY_NAME)){
							updateInterfaceInterfaceMapping((InterfaceInterfaceMappingThing)targetThing);
						}
						else if(propName.equals(InterfaceInterfaceMappingThing.INNER_INTERFACE_THING_ID_PROPERTY_NAME)){
							updateInterfaceInterfaceMapping((InterfaceInterfaceMappingThing)targetThing);
						}
					}
				}
			}
			if(targetThing instanceof BrickThing){
				updateAllInterfaceInterfaceMappingThings((BrickThing)targetThing);
			}
			else if(targetThing instanceof InterfaceThing){
				updateAllInterfaceInterfaceMappingThings((InterfaceThing)targetThing);
			}
		}
		else if(evtType == BNAModelEvent.THING_REMOVED){
			Thing targetThing = evt.getTargetThing();
			if(targetThing == null) return;
			if(targetThing instanceof InterfaceInterfaceMappingThing){
				interfaceInterfaceMappingSet.remove(targetThing);
			}
		}
	}	
}
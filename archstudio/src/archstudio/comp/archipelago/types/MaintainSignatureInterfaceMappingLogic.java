package archstudio.comp.archipelago.types;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.logic.*;
import edu.uci.ics.bna.thumbnail.Thumbnail;

public class MaintainSignatureInterfaceMappingLogic extends ThingLogicAdapter /*implements CoordinateMapperListener*/{

	protected BoundingBoxTrackingLogic boundingBoxTrackingLogic;

	//Keeps all signature interface mappings
	protected Set signatureInterfaceMappingSet;
	
	public MaintainSignatureInterfaceMappingLogic(){
		signatureInterfaceMappingSet = new HashSet();
	}
	
	public void init(){
		init(getBNAComponent().getModel());
	}
	
	public void init(BNAModel m){
		signatureInterfaceMappingSet.clear();
		for(Iterator it = m.getThingIterator(); it.hasNext(); ){
			Thing targetThing = (Thing)it.next();
			if(targetThing instanceof SignatureInterfaceMappingThing){
				signatureInterfaceMappingSet.add(targetThing);
				updateSignatureInterfaceMapping((SignatureInterfaceMappingThing)targetThing);
			}
		}
	}
	
	/*
	public void destroy(){
		getBNAComponent().getCoordinateMapper().removeCoordinateMapperListener(this);
	}
	*/
	
	protected void updateSignatureInterfaceMapping(SignatureInterfaceMappingThing simt){
		BNAComponent c = super.getBNAComponent();
		if(c == null){
			return;
		}
		String brickTypeThingID = simt.getBrickTypeThingID();
		String signatureThingID = simt.getSignatureThingID();
		String interfaceThingID = simt.getInterfaceThingID();
		
		if((brickTypeThingID == null) || (signatureThingID == null) || (interfaceThingID == null)){
			return;
		}
		
		BrickTypeThing btt = null;
		SignatureThing st = null;
		InterfaceThing it = null;

		Thing t = c.getModel().getThing(brickTypeThingID);
		if((t == null) || (!(t instanceof BrickTypeThing))){
			return;
		}
		btt = (BrickTypeThing)t;
		
		t = c.getModel().getThing(signatureThingID);
		if((t == null) || (!(t instanceof SignatureThing))){
			return;
		}
		st = (SignatureThing)t;
		
		Rectangle stBoundingBox = st.getBoundingBox();
		int stcx = stBoundingBox.x + (stBoundingBox.width / 2);
		int stcy = stBoundingBox.y + (stBoundingBox.height / 2);
		
		simt.setFirstPoint(stcx, stcy);
		
		Rectangle thumbnailBox = BoxThingPeer.getThumbnailRectangle(btt, c.getCoordinateMapper());
		if(thumbnailBox == null){
			return;
		}
		
		Thumbnail thumbnail = btt.getThumbnail();
		thumbnail.setupCoordinateMapper(thumbnailBox.x, thumbnailBox.y, 
			thumbnailBox.width, thumbnailBox.height);
		
		t = thumbnail.getModel().getThing(interfaceThingID);
		if((t == null) || (!(t instanceof InterfaceThing))){
			return;
		}
		it = (InterfaceThing)t;
		
		Rectangle interfaceWorldBoundingBox = it.getBoundingBox();
		
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
		
		//(lcix, lciy) is the current-local centerpoint of the interface
		//Now we have to convert it to current-world coordinate.
		int cwcix = c.getCoordinateMapper().localXtoWorldX(lcix); 
		int cwciy = c.getCoordinateMapper().localYtoWorldY(lciy); 
		simt.setSecondPoint(cwcix, cwciy);
	}

	double oldScale = -1.0d;
	public void coordinateMappingsChanged(CoordinateMapperEvent evt){
		double newScale = evt.getNewScale();
		if(newScale != oldScale){
			updateAllSignatureInterfaceMappingThings();
		}
		oldScale = newScale;
	}

	public void updateAllSignatureInterfaceMappingThings(){
		for(Iterator it = signatureInterfaceMappingSet.iterator(); it.hasNext(); ){
			SignatureInterfaceMappingThing simt = (SignatureInterfaceMappingThing)it.next();
			String simtbtid = simt.getBrickTypeThingID();
			updateSignatureInterfaceMapping(simt);
		}
	}

	public void updateAllSignatureInterfaceMappingThings(BrickTypeThing btt){
		String bttID = btt.getID();
		if(bttID != null){
			SignatureInterfaceMappingThing[] simts = 
				(SignatureInterfaceMappingThing[])signatureInterfaceMappingSet.toArray(new SignatureInterfaceMappingThing[0]);
			for(int i = 0; i < simts.length; i++){
				String simtbtid = simts[i].getBrickTypeThingID();
				if((simtbtid != null) && simtbtid.equals(bttID)){
					updateSignatureInterfaceMapping(simts[i]);
				}
			}
		}
	}
	
	public void updateAllSignatureInterfaceMappingThings(SignatureThing st){
		String stID = st.getID();
		if(stID != null){
			SignatureInterfaceMappingThing[] simts = 
				(SignatureInterfaceMappingThing[])signatureInterfaceMappingSet.toArray(new SignatureInterfaceMappingThing[0]);
			for(int i = 0; i < simts.length; i++){
				String simtbtid = simts[i].getSignatureThingID();
				if((simtbtid != null) && simtbtid.equals(stID)){
					updateSignatureInterfaceMapping(simts[i]);
				}
			}
		}
	}
	
	public void bnaModelChanged(BNAModelEvent evt){
		int evtType = evt.getEventType();
		if(evtType == BNAModelEvent.THING_ADDED){
			Thing targetThing = evt.getTargetThing();
			if(targetThing instanceof SignatureInterfaceMappingThing){
				updateSignatureInterfaceMapping((SignatureInterfaceMappingThing)targetThing);
				signatureInterfaceMappingSet.add(targetThing);
			}
			else if(targetThing instanceof BrickTypeThing){
				updateAllSignatureInterfaceMappingThings((BrickTypeThing)targetThing);
			}
		}
		else if(evtType == BNAModelEvent.THING_CHANGED){
			Thing targetThing = evt.getTargetThing();
			if(targetThing instanceof SignatureInterfaceMappingThing){
				ThingEvent tevt = evt.getThingEvent();
				if(tevt != null){
					String propName = tevt.getPropertyName();
					if(propName != null){
						if(propName.equals(SignatureInterfaceMappingThing.BRICK_TYPE_THING_ID_PROPERTY_NAME)){
							updateSignatureInterfaceMapping((SignatureInterfaceMappingThing)targetThing);
						}
						else if(propName.equals(SignatureInterfaceMappingThing.SIGNATURE_THING_ID_PROPERTY_NAME)){
							updateSignatureInterfaceMapping((SignatureInterfaceMappingThing)targetThing);
						}
						else if(propName.equals(SignatureInterfaceMappingThing.INTERFACE_THING_ID_PROPERTY_NAME)){
							updateSignatureInterfaceMapping((SignatureInterfaceMappingThing)targetThing);
						}
					}
				}
			}
			if(targetThing instanceof BrickTypeThing){
				updateAllSignatureInterfaceMappingThings((BrickTypeThing)targetThing);
			}
			else if(targetThing instanceof SignatureThing){
				updateAllSignatureInterfaceMappingThings((SignatureThing)targetThing);
			}
		}
		else if(evtType == BNAModelEvent.THING_REMOVED){
			Thing targetThing = evt.getTargetThing();
			if(targetThing == null) return;
			if(targetThing instanceof SignatureInterfaceMappingThing){
				signatureInterfaceMappingSet.remove(targetThing);
			}
		}
	}	
}
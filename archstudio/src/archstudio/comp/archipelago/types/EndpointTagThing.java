package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;

import edu.uci.ics.bna.*;

public class EndpointTagThing extends TagThing implements IIndependentHinted{

	public EndpointTagThing(){
		super();
	}
	
	public EndpointTagThing(TagThing copyMe){
		super(copyMe);
	}
	
	public Class getPeerClass(){
		return EndpointTagThingPeer.class;
	}
		
	public String getXArchID(){
		return null;
		//return "#$#" + getClass().getName();
	}
	
	public void setXArchID(String xArchID){
		return;
	}
	
	public String[] getRenderingHintPropertyNames(){
		return new String[]{ANCHOR_POINT_PROPERTY_NAME, COLOR_PROPERTY_NAME, TRIM_COLOR_PROPERTY_NAME, ROTATION_ANGLE_PROPERTY_NAME, "text", MOVE_TOGETHER_MODE_PROPERTY_NAME};
	}
	
	public String[] getRefPropertyNames(){
		return new String[]{MOVE_TOGETHER_THING_ID_PROPERTY_NAME, INDICATOR_THING_ID_PROPERTY_NAME, TAGGED_THING_ID_PROPERTY_NAME};
	}
	
	public boolean shouldInstantiateIndependently(){
		return false;
	}
	
	public String getParentRefPropertyName(){
		return TAGGED_THING_ID_PROPERTY_NAME;
	}
}

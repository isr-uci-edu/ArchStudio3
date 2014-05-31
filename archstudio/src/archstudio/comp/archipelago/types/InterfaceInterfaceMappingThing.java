package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;

import edu.uci.ics.bna.*;

public class InterfaceInterfaceMappingThing extends MappingSplineThing{

	public static final String OUTER_INTERFACE_THING_ID_PROPERTY_NAME = "outerInterfaceThingID";
	public static final String INNER_INTERFACE_THING_ID_PROPERTY_NAME = "innerInterfaceThingID";
	public static final String BRICK_THING_ID_PROPERTY_NAME = "brickThingID";
	
	public InterfaceInterfaceMappingThing(){
		super();
	}
	
	public Class getPeerClass(){
		return InterfaceInterfaceMappingThingPeer.class;
	}
	
	public void setBrickThingID(String brickTypeID){
		setProperty(BRICK_THING_ID_PROPERTY_NAME, brickTypeID);
	}
	
	public String getBrickThingID(){
		return (String)getProperty(BRICK_THING_ID_PROPERTY_NAME);
	}
	
	public void setOuterInterfaceThingID(String outerInterfaceThingID){
		setProperty(OUTER_INTERFACE_THING_ID_PROPERTY_NAME, outerInterfaceThingID);
	}

	public String getOuterInterfaceThingID(){
		return (String)getProperty(OUTER_INTERFACE_THING_ID_PROPERTY_NAME);
	}

	//Note that this will likely be the ID of the thing in the
	//inner architecture's BNA model, not the outer architecture's
	public void setInnerInterfaceThingID(String innerInterfaceThingID){
		setProperty(INNER_INTERFACE_THING_ID_PROPERTY_NAME, innerInterfaceThingID);
	}

	public String getInnerInterfaceThingID(){
		return (String)getProperty(INNER_INTERFACE_THING_ID_PROPERTY_NAME);
	}
	
	public static final String[] renderingHintPropertyNames = new String[]{
	};
	
	public String[] getRenderingHintPropertyNames(){
		return renderingHintPropertyNames;
	}
	
	public void setXArchID(String xArchID){
		setProperty(IHinted.XARCHID_PROPERTY_NAME, xArchID);
	}
	
	public String getXArchID(){
		return (String)getProperty(IHinted.XARCHID_PROPERTY_NAME);
	}
}

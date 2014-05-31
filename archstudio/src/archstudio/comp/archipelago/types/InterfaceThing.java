package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;

import edu.uci.ics.bna.*;

public class InterfaceThing extends EndpointThing implements IHinted, IHasXadlType, IEditableDescription, IEditableDirection{
	
	public InterfaceThing(){
		super();
	}
	
	public Class getPeerClass(){
		return InterfaceThingPeer.class;
	}
	
	public static final String[] renderingHintPropertyNames = new String[]{
		"boundingBox", "orientation"
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
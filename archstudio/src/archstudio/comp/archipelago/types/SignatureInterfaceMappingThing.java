package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;

import edu.uci.ics.bna.*;

public class SignatureInterfaceMappingThing extends MappingSplineThing implements IHinted, IEditableDescription, IHasXadlType{

	public static final String INTERFACE_THING_ID_PROPERTY_NAME = "interfaceThingID";
	public static final String SIGNATURE_THING_ID_PROPERTY_NAME = "signatureThingID";
	public static final String BRICK_TYPE_THING_ID_PROPERTY_NAME = "brickTypeThingID";
	
	public SignatureInterfaceMappingThing(){
		super();
	}
	
	public Class getPeerClass(){
		return SignatureInterfaceMappingThingPeer.class;
	}
	
	public void setBrickTypeThingID(String brickTypeID){
		setProperty(BRICK_TYPE_THING_ID_PROPERTY_NAME, brickTypeID);
	}
	
	public String getBrickTypeThingID(){
		return (String)getProperty(BRICK_TYPE_THING_ID_PROPERTY_NAME);
	}
	
	public void setSignatureThingID(String signatureThingID){
		setProperty(SIGNATURE_THING_ID_PROPERTY_NAME, signatureThingID);
	}

	public String getSignatureThingID(){
		return (String)getProperty(SIGNATURE_THING_ID_PROPERTY_NAME);
	}

	//Note that this will likely be the ID of the thing in the
	//inner architecture's BNA model, not the outer architecture's
	public void setInterfaceThingID(String interfaceThingID){
		setProperty(INTERFACE_THING_ID_PROPERTY_NAME, interfaceThingID);
	}

	public String getInterfaceThingID(){
		return (String)getProperty(INTERFACE_THING_ID_PROPERTY_NAME);
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

package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;

import edu.uci.ics.bna.*;

public abstract class BrickThing extends BoxThing implements IHinted, IEditableDescription, IHasXadlType{

	public BrickThing(){
		super();
		setWrapLabel(true);
	}
	
	public abstract Class getPeerClass();

	public static final String[] renderingHintPropertyNames = new String[]{
		"boundingBox", "color", "inheritColorFromType"
	};
	
	public String[] getRenderingHintPropertyNames(){
		return renderingHintPropertyNames;
	}
	
	public void setInheritColorFromType(boolean inheritColor){
		setProperty("inheritColorFromType", inheritColor);
	}
	
	public boolean getInheritColorFromType(){
		Boolean b = (Boolean)getProperty("inheritColorFromType");
		if(b == null){
			return false;
		}
		return b.booleanValue();
	}
	
	public void setXArchID(String xArchID){
		setProperty(IHinted.XARCHID_PROPERTY_NAME, xArchID);
	}
	
	public String getXArchID(){
		return (String)getProperty(IHinted.XARCHID_PROPERTY_NAME);
	}
}

package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;

import edu.uci.ics.bna.*;

public abstract class BrickTypeThing extends BoxThing implements IHinted, IEditableDescription{
	
	public BrickTypeThing(){
		super();
		setWrapLabel(true);
	}
	
	public BrickTypeThing(BrickTypeThing copyMe){
		super(copyMe);
	}
	
	public abstract Class getPeerClass();
	
	public static final String[] renderingHintPropertyNames = new String[]{
		"boundingBox", "color"
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
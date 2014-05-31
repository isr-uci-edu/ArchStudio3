package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;

import edu.uci.ics.bna.*;

public class LinkThing extends SplineThing implements IHinted{
	
	public static final Color DEFAULT_COLOR = Color.BLACK;
	
	public LinkThing(){
		super();
		this.setColor(DEFAULT_COLOR);
	}
	
	public Class getPeerClass(){
		return LinkThingPeer.class;
	}
	
	public static final String[] renderingHintPropertyNames = new String[]{
		"numPoints", "point*", "splineMode", "color"
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
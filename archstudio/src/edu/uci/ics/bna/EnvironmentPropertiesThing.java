package edu.uci.ics.bna;

public class EnvironmentPropertiesThing extends NoThing{

	public static final String SCALE_PROPERTY_NAME = "__scale";
	public static final String WORLD_ORIGIN_Y_PROPERTY_NAME = "__worldOriginY";
	public static final String WORLD_ORIGIN_X_PROPERTY_NAME = "__worldOriginX";
	
	public EnvironmentPropertiesThing(){
		super("$$environmentProperties");
	}
	
	public Class getPeerClass(){
		return EnvironmentPropertiesThingPeer.class;
	}

	public static final String[] renderingHintPropertyNames = new String[]{
		WORLD_ORIGIN_X_PROPERTY_NAME, WORLD_ORIGIN_Y_PROPERTY_NAME, SCALE_PROPERTY_NAME
	};

	public String[] getRenderingHintPropertyNames(){
		return renderingHintPropertyNames;
	}

	public void setWorldOriginX(int worldOriginX){
		setProperty(WORLD_ORIGIN_X_PROPERTY_NAME, worldOriginX);
	}
	
	public int getWorldOriginX(){
		return getIntProperty(WORLD_ORIGIN_X_PROPERTY_NAME);
	}
	
	public void setWorldOriginY(int worldOriginY){
		setProperty(WORLD_ORIGIN_Y_PROPERTY_NAME, worldOriginY);
	}
	
	public int getWorldOriginY(){
		return getIntProperty(WORLD_ORIGIN_Y_PROPERTY_NAME);
	}
	
	public void setScale(double scale){
		setProperty(SCALE_PROPERTY_NAME, scale);
	}
	
	public double getScale(){
		return getDoubleProperty(SCALE_PROPERTY_NAME);
	}

	/*
	public void setXArchID(String xArchID){
		setProperty(IHinted.XARCHID_PROPERTY_NAME, xArchID);
	}
	
	public String getXArchID(){
		return (String)getProperty(IHinted.XARCHID_PROPERTY_NAME);
	}
	*/
}

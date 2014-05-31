package edu.uci.ics.bna;

import java.awt.Point;

public class UserNotificationThing extends AbstractThing implements IEphemeral{

	public static final String LABEL_PROPERTY_NAME = "label";
	public static final String INITIAL_POINT_LOCAL = "initialPointLocal";

	public UserNotificationThing(){
		super(c2.util.UIDGenerator.generateUID("UserNotificationThing"));
		setEphemeralTransparency(1.50f);
	}

	public UserNotificationThing(Thing copyMe) {
		super(copyMe);
	}

	public Class getPeerClass(){
		return UserNotificationThingPeer.class;
	}
	
	public void setLabel(String label){
		setProperty(LABEL_PROPERTY_NAME, label);
	}
	
	public String getLabel(){
		return (String)getProperty(LABEL_PROPERTY_NAME);
	}
	
	public void setEphemeralTransparency(float transparency){
		setProperty(EPHEMERAL_TRANSPARENCY_PROPERTY_NAME, transparency);
	}
	
	public float getEphemeralTransparency(){
		return getFloatProperty(EPHEMERAL_TRANSPARENCY_PROPERTY_NAME);
	}
	
	public void setInitialPointLocal(Point p){
		setProperty(INITIAL_POINT_LOCAL, p);
	}
	
	public Point getInitialPointLocal(){
		return (Point)getProperty(INITIAL_POINT_LOCAL);
	}
}

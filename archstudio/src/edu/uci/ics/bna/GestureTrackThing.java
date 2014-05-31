package edu.uci.ics.bna;

import java.awt.Point;

public class GestureTrackThing extends AbstractThing implements IEphemeral{

	public static final String POINT1_PROPERTY_NAME = "point1";
	public static final String POINT2_PROPERTY_NAME = "point2";

	public GestureTrackThing(){
		super(c2.util.UIDGenerator.generateUID("GestureTrackThing"));
		setEphemeralTransparency(0.35f);
	}

	public GestureTrackThing(Thing copyMe) {
		super(copyMe);
	}

	public Class getPeerClass(){
		return GestureTrackThingPeer.class;
	}
	
	public void setEphemeralTransparency(float transparency){
		setProperty(EPHEMERAL_TRANSPARENCY_PROPERTY_NAME, transparency);
	}
	
	public float getEphemeralTransparency(){
		return getFloatProperty(EPHEMERAL_TRANSPARENCY_PROPERTY_NAME);
	}

	public void setPoint1(Point p1){
		setProperty(POINT1_PROPERTY_NAME, p1);	
	}
	
	public Point getPoint1(){
		return (Point)getProperty(POINT1_PROPERTY_NAME);
	}
	
	public void setPoint2(Point p2){
		setProperty(POINT2_PROPERTY_NAME, p2);	
	}
	
	public Point getPoint2(){
		return (Point)getProperty(POINT2_PROPERTY_NAME);
	}

}

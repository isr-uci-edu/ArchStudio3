package edu.uci.ics.bna;

public interface IIndicator extends Thing{

	public static final String INDICATOR_POINT_PROPERTY_NAME = "indicatorPoint";
	public static final String INDICATOR_THING_ID_PROPERTY_NAME = "indicatorThingId";

	public void setIndicatorPoint(java.awt.Point indicatorPoint);
	public java.awt.Point getIndicatorPoint();
	
	public void setIndicatorThingId(String thingId);
	public String getIndicatorThingId();

}

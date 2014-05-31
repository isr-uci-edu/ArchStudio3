package edu.uci.ics.bna;

//Interface for Things that can be drawn translucently; usually
//by painting filled areas in Alpha Blending mode to make
//them appear as if they are glass.  See BoxThingPeer for an
//example of how elements can be Glassed.

public interface IGlassable extends Thing{

	public static final String GLASSED_PROPERTY_NAME = "glassed";
	public static final String GLASSED_TRANSPARENCY_PROPERTY_NAME = "glassedTransparency";
	
	public void setGlassed(boolean glassed);
	public boolean isGlassed();
	
	public void setGlassedTransparency(float transparency);
	public float getGlassedTransparency();
}

package edu.uci.ics.bna;

public interface IBoxBounded extends Thing{

	public static final String BOUNDING_BOX_PROPERTY_NAME = "boundingBox";
	
	//Get bounding box in world coordinates
	public java.awt.Rectangle getBoundingBox();

}

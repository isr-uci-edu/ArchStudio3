package edu.uci.ics.bna.swingthing;

import edu.uci.ics.bna.Thing;

public interface ILocalBoxBounded extends Thing{

	public static final String LOCAL_BOUNDING_BOX_PROPERTY_NAME = "localBoundingBox";
	
	//Get bounding box in local coordinates
	public java.awt.Rectangle getLocalBoundingBox();

}

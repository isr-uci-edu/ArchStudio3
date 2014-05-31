package edu.uci.ics.bna;

public interface IAnchored extends Thing{
	public static final String ANCHOR_POINT_PROPERTY_NAME = "anchorPoint";
	
	public java.awt.Point getAnchorPoint();
}

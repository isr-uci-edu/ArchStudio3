package edu.uci.ics.bna;

import java.awt.Stroke;

public interface IStroked extends Thing{

	public static final String STROKE_PROPERTY_NAME = "stroke";
	
	public Stroke getStroke();
	public void setStroke(Stroke s);

}

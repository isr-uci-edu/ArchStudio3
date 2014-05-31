package edu.uci.ics.bna;

import java.awt.Color;

public interface IColored extends Thing{

	public static final String COLOR_PROPERTY_NAME = "color";
	
	public Color getColor();
	public void setColor(Color c);

}

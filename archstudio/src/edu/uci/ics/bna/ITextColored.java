package edu.uci.ics.bna;

import java.awt.Color;

public interface ITextColored extends Thing{

	public static final String TEXT_COLOR_PROPERTY_NAME = "textColor";
	
	public Color getTextColor();
	public void setTextColor(Color c);

}

package edu.uci.ics.bna;

import java.awt.Color;

public interface ITrimColored extends Thing{

	public static final String TRIM_COLOR_PROPERTY_NAME = "trimColor";
	
	public Color getTrimColor();
	public void setTrimColor(Color c);

}

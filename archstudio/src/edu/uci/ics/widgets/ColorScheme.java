package edu.uci.ics.widgets;

import java.awt.Color;

public class ColorScheme{
	protected String name;
	protected Color[][] colorSets;
	
	public ColorScheme(String name, Color[][] colorSets){
		this.name = name;
		this.colorSets = colorSets;
	}
	
	public Color getColor(int set, int variant){
		Color[] colorSet = colorSets[set];
		return colorSet[variant];
	}
	
	public int getNumSets(){
		return colorSets.length;
	}
	
	public int getNumVariants(){
		return colorSets[0].length;
	}
	
	public String toString(){
		return name;
	}
	
}

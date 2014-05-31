package edu.uci.ics.bna;

import java.awt.Rectangle;

public class AlignUtils {
	private AlignUtils(){}

	public static void alignTops(Thing[] alignableThings){
		int alignAt = ((BoxThing)alignableThings[0]).getY();
		for(int i = 1; i < alignableThings.length; i++){
			Rectangle currentBounds = ((BoxThing)alignableThings[i]).getBoundingBox();
			currentBounds.setLocation(currentBounds.x, alignAt);
			((BoxThing)alignableThings[i]).setBoundingBox(currentBounds);
		}		
	}
	
	public static void alignHorizontalCenters(Thing[] alignableThings){
		int alignAt = (((BoxThing)alignableThings[0]).getY() + ((BoxThing)alignableThings[0]).getHeight() / 2);
		for(int i = 1; i < alignableThings.length; i++){
			Rectangle currentBounds = ((BoxThing)alignableThings[i]).getBoundingBox();
			currentBounds.setLocation(currentBounds.x, alignAt - (currentBounds.height / 2));
			((BoxThing)alignableThings[i]).setBoundingBox(currentBounds);
		}
	}
	
	public static void alignBottoms(Thing[] alignableThings){
		int alignAt = ((BoxThing)alignableThings[0]).getY2();
		for(int i = 1; i < alignableThings.length; i++){
			Rectangle currentBounds = ((BoxThing)alignableThings[i]).getBoundingBox();
			currentBounds.setLocation(currentBounds.x, alignAt - currentBounds.height);
			((BoxThing)alignableThings[i]).setBoundingBox(currentBounds);
		}		
	}

	public static void alignLefts(Thing[] alignableThings){
		int alignAt = ((BoxThing)alignableThings[0]).getX();
		for(int i = 1; i < alignableThings.length; i++){
			Rectangle currentBounds = ((BoxThing)alignableThings[i]).getBoundingBox();
			currentBounds.setLocation(alignAt, currentBounds.y);
			((BoxThing)alignableThings[i]).setBoundingBox(currentBounds);
		}
	}
	
	public static void alignVerticalCenters(Thing[] alignableThings){
		int alignAt = (((BoxThing)alignableThings[0]).getX() + ((BoxThing)alignableThings[0]).getWidth() / 2);
		for(int i = 1; i < alignableThings.length; i++){
			Rectangle currentBounds = ((BoxThing)alignableThings[i]).getBoundingBox();
			currentBounds.setLocation(alignAt - (currentBounds.width / 2), currentBounds.y);
			((BoxThing)alignableThings[i]).setBoundingBox(currentBounds);
		}
	}
	
	public static void alignRights(Thing[] alignableThings){
		int alignAt = ((BoxThing)alignableThings[0]).getX2();
		for(int i = 1; i < alignableThings.length; i++){
			Rectangle currentBounds = ((BoxThing)alignableThings[i]).getBoundingBox();
			currentBounds.setLocation(alignAt - currentBounds.width, currentBounds.y);
			((BoxThing)alignableThings[i]).setBoundingBox(currentBounds);
		}
	}
}

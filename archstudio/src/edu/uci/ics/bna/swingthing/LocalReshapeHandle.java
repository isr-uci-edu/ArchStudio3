package edu.uci.ics.bna.swingthing;

import java.awt.Cursor;

import edu.uci.ics.bna.*;

public class LocalReshapeHandle extends AbstractThing implements ICustomCursor{
	
	public static final int ORIENTATION_NW = 100;
	public static final int ORIENTATION_NE = 102;
	public static final int ORIENTATION_SE = 104;
	public static final int ORIENTATION_SW = 106;
	
	public static final int ORIENTATION_N = 108;
	public static final int ORIENTATION_E = 110;
	public static final int ORIENTATION_S = 112;
	public static final int ORIENTATION_W = 114;
	
	public static final int ORIENTATION_CENTER = 200;
	
	public static final java.awt.Color DEFAULT_COLOR = java.awt.Color.BLUE;
	
	public LocalReshapeHandle(){
		super(c2.util.UIDGenerator.generateUID("LocalReshapeHandle"));
		setX(0);
		setY(0);
		setOrientation(ORIENTATION_SE);
		setTargetThingID("None");
		setColor(DEFAULT_COLOR);
	}
	
	public Class getPeerClass(){
		return LocalReshapeHandlePeer.class;
	}
	
	public void setX(int x){
		setProperty("x", x);
	}
	
	public void setY(int y){
		setProperty("y", y);
	}
	
	public void setOrientation(int orientation){
		setProperty("orientation", orientation);
	}
	
	public void setTargetThingID(String targetThingID){
		setProperty("targetThingID", targetThingID);
	}
	
	public void setTargetThingIndex(int targetThingIndex){
		setProperty("targetThingIndex", targetThingIndex);
	}
	
	public int getX(){
		return getIntProperty("x");
	}
	
	public int getY(){
		return getIntProperty("y");
	}
	
	public int getOrientation(){
		return getIntProperty("orientation");
	}
	
	public int getCursorType(){
		switch(getOrientation()){
		case ORIENTATION_NW:
			return Cursor.NW_RESIZE_CURSOR;
		case ORIENTATION_NE:
			return Cursor.NE_RESIZE_CURSOR;
		case ORIENTATION_SE:
			return Cursor.SE_RESIZE_CURSOR;
		case ORIENTATION_SW:
			return Cursor.SW_RESIZE_CURSOR;
		case ORIENTATION_N:
			return Cursor.N_RESIZE_CURSOR;
		case ORIENTATION_S:
			return Cursor.S_RESIZE_CURSOR;
		case ORIENTATION_E:
			return Cursor.E_RESIZE_CURSOR;
		case ORIENTATION_W:
			return Cursor.W_RESIZE_CURSOR;
		case ORIENTATION_CENTER:
			return Cursor.MOVE_CURSOR;
		default:
			return Cursor.DEFAULT_CURSOR;
		}
	}
	
	public String getTargetThingID(){
		return (String)getProperty("targetThingID");
	}
	
	public int getTargetThingIndex(){
		return getIntProperty("targetThingIndex");
	}
	
	public void setColor(java.awt.Color c){
		setProperty("color", c);
	}
	
	public java.awt.Color getColor(){
		return (java.awt.Color)getProperty("color");
	}
}
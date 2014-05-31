package edu.uci.ics.bna;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;

import edu.uci.ics.bna.thumbnail.Thumbnail;

public class RotaterThing extends AbstractThing implements IBoxBounded, IRotatable, IChangeableAnchored, IMoveTogether{
	
	public RotaterThing(){
		super(c2.util.UIDGenerator.generateUID("RotaterThing"));
		setAdjustmentIncrement(15);
		setAnchorPoint(new Point(0, 0));
		setRadius(50);
		setMoveTogetherMode(IMoveTogether.MOVE_TOGETHER_TRACK_ANCHOR_POINT_FIRST);
	}
	
	public RotaterThing(RotaterThing copyMe){
		super(copyMe);
	}

	public Class getPeerClass(){
		return RotaterThingPeer.class;
	}
	
	public void setAnchorPoint(Point p){
		setProperty("anchorPoint", p);
		//System.out.println("anchorPoint set to " + p);
	}
	
	public Point getAnchorPoint(){
		return (Point)getProperty("anchorPoint");
	}
	
	public int getRadius(){
		return getIntProperty("radius");
	}
	
	public void setRadius(int radius){
		setProperty("radius", radius);
	}
	
	public Rectangle getBoundingBox(){
		Point anchorPoint = getAnchorPoint();
		int radius = getRadius();
		return new Rectangle(anchorPoint.x - radius, anchorPoint.y - radius, radius * 2, radius * 2);
	}
	
	public void addRotatedThingId(String rotatedThingID){
		addSetPropertyValue("rotatedThingIds", rotatedThingID);
	}
	
	public String[] getRotatedThingIds(){
		java.util.Set rotatedThingIdSet = getSetProperty("rotatedThingIds");
		if(rotatedThingIdSet == null) return null;
		return (String[])rotatedThingIdSet.toArray(new String[0]);
	}

	public void setRotationAngle(int degrees){
		setProperty("rotationAngle", degrees);
	}
	
	public int getRotationAngle(){
		return getIntProperty("rotationAngle");
	}

	public void moveRelative(int dx, int dy){
		//System.out.println(dx + ", " + dy);
 		Point p = getAnchorPoint();
 		Point p2 = new Point(p.x + dx, p.y + dy);
 		setAnchorPoint(p2);
	}
	
	public void setAdjustmentIncrement(int increment){
		setProperty("adjustmentIncrement", increment);
	}
	
	public int getAdjustmentIncrement(){
		return getIntProperty("adjustmentIncrement");
	}
	
	public void setMoveTogetherThingId(String thingId){
		setProperty(IMoveTogether.MOVE_TOGETHER_THING_ID_PROPERTY_NAME, thingId);
	}
	
	public String getMoveTogetherThingId(){
		return (String)getProperty(IMoveTogether.MOVE_TOGETHER_THING_ID_PROPERTY_NAME);
	}
	
	public void setMoveTogetherMode(int moveTogetherMode){
		setProperty(IMoveTogether.MOVE_TOGETHER_MODE_PROPERTY_NAME, moveTogetherMode);
	}
	
	public int getMoveTogetherMode(){
		return getIntProperty(IMoveTogether.MOVE_TOGETHER_MODE_PROPERTY_NAME);
	}
}

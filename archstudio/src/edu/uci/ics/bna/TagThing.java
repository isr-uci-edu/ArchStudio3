package edu.uci.ics.bna;

import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Color;
import java.awt.Font;

public class TagThing extends AbstractThing implements IBoxBounded, 
IToolTip, IIndicator, IColored, ITrimColored, IDragMovable, IMoveTogether, IRotatable, IChangeableAnchored{
	
	public static final String TAGGED_THING_ID_PROPERTY_NAME = "taggedThingId";
	
	public TagThing(){
		super(c2.util.UIDGenerator.generateUID("TagThing"));
		
		setText("[No Text]");
		setFont(BNAUtils.DEFAULT_FONT);
		setAnchorPoint(new Point(DefaultCoordinateMapper.DEFAULT_WORLD_WIDTH / 2, 
			DefaultCoordinateMapper.DEFAULT_WORLD_HEIGHT / 2));
		setRotationAngle(0);
		setColor(java.awt.Color.BLACK);
		setTrimColor(null);
		setMoveTogetherMode(MOVE_TOGETHER_TRACK_ANCHOR_POINT_FIRST);
	}
	
	public TagThing(TagThing copyMe){
		super(copyMe);
	}
	
	public Class getPeerClass(){
		return TagThingPeer.class;
	}
	
	public void setAnchorPoint(Point p){
		setProperty(ANCHOR_POINT_PROPERTY_NAME, p);
	}
	
	public Point getAnchorPoint(){
		return (Point)getProperty(ANCHOR_POINT_PROPERTY_NAME);
	}
	
	public void setAnchorPointX(int x){
		Point p = getAnchorPoint();
		p.x = x;
		setAnchorPoint(p);
	}
	
	public void setAnchorPointY(int y){
		Point p = getAnchorPoint();
		p.y = y;
		setAnchorPoint(p);
	}
	
	public Rectangle getBoundingBox(){
		Rectangle r = (Rectangle)getProperty("#boundingBox");
		return r;
	}
		
	public String getToolTipText(){
		return getText();
	}
	
	public void setText(String text){
		setProperty("text", text);
	}
	
	public String getText(){
		return (String)getProperty("text");
	}
	
	public void setColor(java.awt.Color c){
		setProperty(COLOR_PROPERTY_NAME, c);
	}
	
	public java.awt.Color getColor(){
		return (java.awt.Color)getProperty(COLOR_PROPERTY_NAME);
	}
	
	public void setTrimColor(java.awt.Color c){
		setProperty(TRIM_COLOR_PROPERTY_NAME, c);
	}
	
	public java.awt.Color getTrimColor(){
		return (java.awt.Color)getProperty(TRIM_COLOR_PROPERTY_NAME);
	}
	
 	public void moveRelative(int dx, int dy){
 		Point p = getAnchorPoint();
 		Point p2 = new Point(p.x + dx, p.y + dy);
 		setAnchorPoint(p2);
	}
 	
 	public void setTaggedThingId(String targetThingId){
 		setProperty(TAGGED_THING_ID_PROPERTY_NAME, targetThingId);
 		setIndicatorThingId(targetThingId);
 		setMoveTogetherThingId(targetThingId);
 	}
 	
 	public String getTaggedThingId(){
 		return (String)getProperty(TAGGED_THING_ID_PROPERTY_NAME);
 	}
	
	public void setIndicatorPoint(java.awt.Point indicatorPoint){
		setProperty(IIndicator.INDICATOR_POINT_PROPERTY_NAME, indicatorPoint);
	}
	
	public java.awt.Point getIndicatorPoint(){
		return (java.awt.Point) getProperty(IIndicator.INDICATOR_POINT_PROPERTY_NAME);
	}
	
	//This should not be called directly; call setTaggedThing instead
	public void setIndicatorThingId(String indicatorThingId){
		setProperty(IIndicator.INDICATOR_THING_ID_PROPERTY_NAME, indicatorThingId);
	}
	
	public String getIndicatorThingId(){
		return (String)getProperty(IIndicator.INDICATOR_THING_ID_PROPERTY_NAME);
	}
	
	public void setRotationAngle(int degrees){
		setProperty(ROTATION_ANGLE_PROPERTY_NAME, degrees);
	}
	
	public int getRotationAngle(){
		return getIntProperty(ROTATION_ANGLE_PROPERTY_NAME);
	}
	
	public void setFont(Font f){
		setProperty("font", f);
	}
	
	public Font getFont(){
		return (Font)getProperty("font");
	}
	
	//This should not be called directly; call setTaggedThing instead
	public void setMoveTogetherThingId(String thingId){
		setProperty(MOVE_TOGETHER_THING_ID_PROPERTY_NAME, thingId);
	}
	
	public String getMoveTogetherThingId(){
		return (String)getProperty(MOVE_TOGETHER_THING_ID_PROPERTY_NAME);
	}
	
	public void setMoveTogetherMode(int moveTogetherMode){
		setProperty(IMoveTogether.MOVE_TOGETHER_MODE_PROPERTY_NAME, moveTogetherMode);
	}
	
	public int getMoveTogetherMode(){
		return getIntProperty(IMoveTogether.MOVE_TOGETHER_MODE_PROPERTY_NAME);
	}
	
}

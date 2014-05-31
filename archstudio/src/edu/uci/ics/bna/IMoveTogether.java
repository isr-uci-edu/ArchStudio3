package edu.uci.ics.bna;

public interface IMoveTogether extends IMovable{

	public static final String MOVE_TOGETHER_THING_ID_PROPERTY_NAME = "moveTogetherThingId";
	public static final String MOVE_TOGETHER_MODE_PROPERTY_NAME = "moveTogetherMode";
	
	public static final int MOVE_TOGETHER_TRACK_BOUNDING_BOX_ONLY = 1010;
	public static final int MOVE_TOGETHER_TRACK_ANCHOR_POINT_ONLY = 1025;
	public static final int MOVE_TOGETHER_TRACK_BOUNDING_BOX_FIRST = 1030;
	public static final int MOVE_TOGETHER_TRACK_ANCHOR_POINT_FIRST = 1050;
	
	public void setMoveTogetherThingId(String thingId);
	public String getMoveTogetherThingId();
	public int getMoveTogetherMode();

}

package edu.uci.ics.bna;

public interface IDirectional{

	public static final int TOWARD_NEITHER = 0;
	public static final int TOWARD_ENDPOINT_1 = 1;
	public static final int TOWARD_ENDPOINT_2 = 2;
	public static final int TOWARD_BOTH = TOWARD_ENDPOINT_1 | TOWARD_ENDPOINT_2;
	
	public static final String DIRECTION_PROPERTY_NAME = "directionMask";

	public void setDirection(int directionMask);	
	public int getDirection();
}

package edu.uci.ics.bna;

public interface IRotatable extends Thing{
	
	public static final String ROTATION_ANGLE_PROPERTY_NAME = "rotationAngle";

	public void setRotationAngle(int degrees);
	public int getRotationAngle();
}

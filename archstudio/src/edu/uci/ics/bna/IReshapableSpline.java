package edu.uci.ics.bna;

import java.awt.Point;

public interface IReshapableSpline extends Thing{
	public static final int SPLINE_MODE_RECTILINEAR = 100;
	public static final int SPLINE_MODE_BSPLINE = 200;
	
	//Change the following value at your peril. If you do change it,
	//make it an even number >= 2.  Higher = smoother.
	public static final int SPLINE_SMOOTHNESS = 8;
	
	public Point getPointAt(int index);
	public void setPointAt(Point p, int index);
	public void addPoint(Point p);
	public void insertPointAt(Point p, int index);
	public void removePointAt(int index);
	public int getNumPoints();
	public Point[] getAllPoints();
	public void setSplineMode(int splineMode);
	public int getSplineMode();
	
}

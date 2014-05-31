package edu.uci.ics.bna;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;

public class SplineThing extends AbstractThing implements IDragMovable, ISelectable,
IReshapableSpline, IDirectional, IOffset, IBoxBounded, IMarqueeSelectable,
IStickyEndpointsSpline, IToolTip, IStroked, IColored, IVisible, IUserEditable{

	public static final String SECOND_ENDPOINT_STUCK_TO_ID_PROPERTY_NAME = "secondEndpointStuckToID";
	public static final String FIRST_ENDPOINT_STUCK_TO_ID_PROPERTY_NAME = "firstEndpointStuckToID";
	public static final String SPLINE_MODE_PROPERTY_NAME = "splineMode";
	public static final String ARROWHEAD_SIZE_PROPERTY_NAME = "arrowheadSize";

	public SplineThing(){
		super(c2.util.UIDGenerator.generateUID("SplineThing"));
		setNumPoints(0);
		
		setSelected(false);
		setColor(java.awt.Color.BLACK);
		setOffset(0);
		setSplineMode(SPLINE_MODE_RECTILINEAR);
		setDirection(IDirectional.TOWARD_NEITHER);
		setArrowheadSize(10);
		
		setVisible(true);
		setUserEditable(true);
	}
	
	public Class getPeerClass(){
		return SplineThingPeer.class;
	}
	
	public void setSplineMode(int splineMode){
		setProperty(SPLINE_MODE_PROPERTY_NAME, splineMode);
	}
	
	public int getSplineMode(){
		return getIntProperty(SPLINE_MODE_PROPERTY_NAME);
	}
	
	public void setNumPoints(int numPoints){
		setProperty("numPoints", numPoints);
	}
	
	public int getNumPoints(){
		synchronized(props){
			int numPoints = getIntProperty("numPoints");
			return numPoints;
		}
	}
	
	public Point getPointAt(int index){
		synchronized(props){
			Point p = (Point)getProperty("point" + index);
			if(p == null) return null;
			return new Point(p);
		}
	}
	
	public Point[] getAllPoints(){
		synchronized(props){
			int numPoints = getNumPoints();
			Point[] points = new Point[numPoints];
			for(int i = 0; i < numPoints; i++){
				points[i] = getPointAt(i);
			}
			return points;
		}
	}
	
	public void addPoint(Point p){
		synchronized(props){
			int numPoints = getNumPoints();
			setProperty("point" + numPoints, p);
			setProperty("numPoints", numPoints + 1);
		}
	}
	
	public void setPointAt(Point p, int index){
		synchronized(props){
			int numPoints = getNumPoints();
			if(index >= numPoints){
				addPoint(p);
				return;
			}
			setProperty("point" + index, p);
		}
	}
	
	public void insertPointAt(Point p, int index){
		synchronized(props){
			int numPoints = getNumPoints();
			if(index >= numPoints){
				addPoint(p);
				return;
			}
			for(int i = numPoints - 1; i >= index; i--){
				Point ep = getPointAt(i);
				setProperty("point" + (i + 1), ep);
			}
			setProperty("point" + index, p);
			setProperty("numPoints", numPoints + 1);
		}
	}
	
	public void removePointAt(int index){
		synchronized(props){
			int numPoints = getNumPoints();
			if(index >= numPoints){
				return;
			}
			setProperty("numPoints", numPoints - 1);
			for(int i = index; i < numPoints; i++){
				Point nextPoint = getPointAt(i+1);
				if(nextPoint == null){
					//Last point
					removeProperty("point" + i);
				}
				else{
					setProperty("point" + i, nextPoint);
				}
			}
		}
	}
	
	public void setFirstEndpointStuckToID(String firstEndpointStuckToID){
		setProperty(FIRST_ENDPOINT_STUCK_TO_ID_PROPERTY_NAME, firstEndpointStuckToID);
	}
	
	public void setSecondEndpointStuckToID(String secondEndpointStuckToID){
		setProperty(SECOND_ENDPOINT_STUCK_TO_ID_PROPERTY_NAME, secondEndpointStuckToID);
	}
	
	public String getFirstEndpointStuckToID(){
		return (String)getProperty(FIRST_ENDPOINT_STUCK_TO_ID_PROPERTY_NAME);
	}
	
	public String getSecondEndpointStuckToID(){
		return (String)getProperty(SECOND_ENDPOINT_STUCK_TO_ID_PROPERTY_NAME);
	}
	
	public void setOffset(int offset){
		setProperty(OFFSET_PROPERTY_NAME, offset);
	}
	
	public int getOffset(){
		return getIntProperty(OFFSET_PROPERTY_NAME);
	}
	
	public void setToolTipText(String toolTip){
		setProperty(TOOLTIP_TEXT_PROPERTY_NAME, toolTip);
	}
	
	public String getToolTipText(){
		return (String)getProperty(TOOLTIP_TEXT_PROPERTY_NAME);
	}
	
	public void setColor(java.awt.Color c){
		setProperty(COLOR_PROPERTY_NAME, c);
	}
	
	public java.awt.Color getColor(){
		return (java.awt.Color)getProperty(COLOR_PROPERTY_NAME);
	}
	
	public void setSelected(boolean selected){
		setProperty(SELECTED_PROPERTY_NAME, selected);
	}
	
	public boolean isSelected(){
		return getBooleanProperty(SELECTED_PROPERTY_NAME);
	}
	
	public void moveRelative(int dx, int dy){
		synchronized(props){
			int numPoints = getNumPoints();
			for(int i = 0; i < numPoints; i++){
				if((i == 0) && (getFirstEndpointStuckToID() != null)){
					continue;
				}
				if((i == (numPoints - 1)) && (getSecondEndpointStuckToID() != null)){
					continue;
				}
				Point p = getPointAt(i);
				p.x += dx;
				p.y += dy;
				setPointAt(p, i);
			}
		}
	}
	
	public Rectangle getBoundingBox(){
		synchronized(props){
			Point[] allPoints = getAllPoints();
			int minX = Integer.MAX_VALUE;
			int minY = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE;
			int maxY = Integer.MIN_VALUE;
			for(int i = 0; i < allPoints.length; i++){
				if(allPoints[i].x < minX) minX = allPoints[i].x;
				if(allPoints[i].x > maxX) maxX = allPoints[i].x;
				if(allPoints[i].y < minY) minY = allPoints[i].y;
				if(allPoints[i].y > maxY) maxY = allPoints[i].y;
			}
			return new Rectangle(minX, minY, maxX-minX, maxY-minY);
		}
	}

	public void setStroke(Stroke s){
		setProperty(IStroked.STROKE_PROPERTY_NAME, s);
	}
	
	public Stroke getStroke(){
		return (Stroke)getProperty(IStroked.STROKE_PROPERTY_NAME);
	}

	public void setDirection(int directionalMask){
		setProperty(IDirectional.DIRECTION_PROPERTY_NAME, directionalMask);
	}
	
	public int getDirection(){
		return getIntProperty(IDirectional.DIRECTION_PROPERTY_NAME);
	}
	
	public void setArrowheadSize(int size){
		setProperty(ARROWHEAD_SIZE_PROPERTY_NAME, size);
	}
	
	public int getArrowheadSize(){
		return getIntProperty(ARROWHEAD_SIZE_PROPERTY_NAME);
	}
	
	public void setVisible(boolean visible){
		setProperty(IVisible.VISIBLE_PROPERTY_NAME, visible);
	}
	
	public boolean isVisible() {
		return getBooleanProperty(IVisible.VISIBLE_PROPERTY_NAME);
	}
	
	public void setUserEditable(boolean userEditable){
		setProperty(IUserEditable.USER_EDITABLE_PROPERTY_NAME, userEditable);
	}
	
	public boolean isUserEditable() {
		return getBooleanProperty(IUserEditable.USER_EDITABLE_PROPERTY_NAME);
	}	
}
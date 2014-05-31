package edu.uci.ics.bna;

import java.awt.*;
import java.awt.geom.*;
import edu.uci.ics.graphicsutils.BSpline;
import edu.uci.ics.widgets.WidgetUtils;

public class SplineThingPeer extends ThingPeer{
	
	private SplineThing t;
	
	public SplineThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof SplineThing)){
			throw new IllegalArgumentException("SplineThingPeer can only peer for SplineThing");
		}
		this.t = (SplineThing)t;
	}
	
	protected Color getColor(){
		return t.getColor();
	}
	
	protected Stroke getStroke(){
		return t.getStroke();
	}
	
	public void draw(Graphics2D g, CoordinateMapper cm){
		synchronized(t.propertyLockObject){
			Color c = getColor();
			Point[] worldPoints = t.getAllPoints();
			if(worldPoints.length == 0){
				return;
			}
			
			Point[] translatedPoints = new Point[worldPoints.length];
			for(int i = 0; i < worldPoints.length; i++){
				Point tp = new Point();
				tp.x = cm.worldXtoLocalX(worldPoints[i].x);
				tp.y = cm.worldYtoLocalY(worldPoints[i].y);
				translatedPoints[i] = tp;
			}
			if(translatedPoints.length == 1){
				g.drawLine(translatedPoints[0].x, translatedPoints[0].y, translatedPoints[0].x, translatedPoints[0].y);
				return;
			}
			
			//Support for BSplines
			int splineMode = t.getSplineMode();
			if(splineMode == SplineThing.SPLINE_MODE_BSPLINE){
				translatedPoints = BSpline.bspline(translatedPoints, SplineThing.SPLINE_SMOOTHNESS);
			}
			
			int directionMask = t.getDirection();

			for(int i = 1; i < translatedPoints.length; i++){
				int x1 = translatedPoints[i-1].x;
				int y1 = translatedPoints[i-1].y;
				int x2 = translatedPoints[i].x;
				int y2 = translatedPoints[i].y;
				if(!t.isSelected()){
					g.setColor(c);
					Stroke newStroke = getStroke();
					Stroke oldStroke = null;
					if(newStroke != null){
						oldStroke = g.getStroke();
						g.setStroke(newStroke);
					}
					g.drawLine(x1, y1, x2, y2);
					if(newStroke != null){
						g.setStroke(oldStroke);
					}
				}
				else{
					int offset = t.getOffset();
					MarqueeUtils.drawArbitraryOffsetLine(g, x1, y1, x2, y2, offset);
				}

				if((directionMask & IDirectional.TOWARD_ENDPOINT_1) != 0){
					if(i == 1){
						//Draw arrowhead
						Point p1 = new Point(x1, y1);
						int jointPointDist = BNAUtils.round(cm.getScale() * t.getArrowheadSize());
						Point jointPoint = WidgetUtils.calcPointOnLineAtDist(p1, new Point(x2, y2), jointPointDist);
						int dx = jointPoint.x - p1.x;
						int dy = jointPoint.y - p1.y;
						Point p2 = new Point(jointPoint.x - dy, jointPoint.y + dx);
						Point p3 = new Point(jointPoint.x + dy, jointPoint.y - dx);
						
						Polygon arrowhead = new Polygon();
						arrowhead.addPoint(p1.x, p1.y);
						arrowhead.addPoint(p2.x, p2.y);
						arrowhead.addPoint(p3.x, p3.y);
						g.setColor(c);
						g.fill(arrowhead);
					}
				}
				if((directionMask & IDirectional.TOWARD_ENDPOINT_2) != 0){
					if(i == (translatedPoints.length - 1)){
						//Draw arrowhead
						Point p1 = new Point(x2, y2);
						int jointPointDist = BNAUtils.round(cm.getScale() * t.getArrowheadSize());
						Point jointPoint = WidgetUtils.calcPointOnLineAtDist(p1, new Point(x1, y1), jointPointDist);
						int dx = jointPoint.x - p1.x;
						int dy = jointPoint.y - p1.y;
						Point p2 = new Point(jointPoint.x - dy, jointPoint.y + dx);
						Point p3 = new Point(jointPoint.x + dy, jointPoint.y - dx);
						
						Polygon arrowhead = new Polygon();
						arrowhead.addPoint(p1.x, p1.y);
						arrowhead.addPoint(p2.x, p2.y);
						arrowhead.addPoint(p3.x, p3.y);
						g.setColor(c);
						g.fill(arrowhead);
					}
				}
			}
		}
	}
	
	public boolean isInThing(Graphics2D g, CoordinateMapper cm, int worldX, int worldY){
		synchronized(t.propertyLockObject){
			Point[] worldPoints = t.getAllPoints();
			if(worldPoints.length == 0){
				return false;
			}
			
			int lx = cm.worldXtoLocalX(worldX);
			int ly = cm.worldYtoLocalY(worldY);
	
			Point[] translatedPoints = new Point[worldPoints.length];
			for(int i = 0; i < worldPoints.length; i++){
				Point tp = new Point();
				tp.x = cm.worldXtoLocalX(worldPoints[i].x);
				tp.y = cm.worldYtoLocalY(worldPoints[i].y);
				translatedPoints[i] = tp;
			}

			//Support for BSplines			
			int splineMode = t.getSplineMode();
			if(splineMode == SplineThing.SPLINE_MODE_BSPLINE){
				translatedPoints = BSpline.bspline(translatedPoints, SplineThing.SPLINE_SMOOTHNESS);
			}

			if(translatedPoints.length == 1){
				int dist = (int)Point2D.Double.distance((double)lx, (double)ly, 
					(double)translatedPoints[0].x, (double)translatedPoints[0].y);
				if(dist < 4){
					return true;
				}
				else{
					return false;
				}
			}
			
			for(int i = 1; i < translatedPoints.length; i++){
				int x1 = translatedPoints[i-1].x;
				int y1 = translatedPoints[i-1].y;
				int x2 = translatedPoints[i].x;
				int y2 = translatedPoints[i].y;
				
				int dist = (int)Line2D.Double.ptSegDist((double)x1, (double)y1, (double)x2, (double)y2, (double)lx, (double)ly);
				if(dist < 4){
					return true;
				}
			}
			return false;
		}
	}
	
	public Rectangle getLocalBoundingBox(Graphics2D g, CoordinateMapper cm){
		Rectangle r = BNAUtils.worldRectangleToLocalRectangle(cm, t.getBoundingBox());

		//The box might be stroked with a thick stroke, so we have to figure the
		//bounding box for the stroked shape.
		Stroke stroke = getStroke();
		if(stroke != null){
			Shape s = stroke.createStrokedShape(r);
			r = s.getBounds();
		}
		if(t.getDirection() > 0){
			int localArrowheadSize = BNAUtils.round(cm.getScale() * t.getArrowheadSize());
			r = WidgetUtils.expand(r, localArrowheadSize);
		}

		if(r.width < 1) r.width = 1;
		if(r.height < 1) r.height = 1;
		return r;
	}
}
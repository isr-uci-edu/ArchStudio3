package edu.uci.ics.bna;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

public class TagThingPeer extends ThingPeer{
	
	private TagThing t;
	
	public TagThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof TagThing)){
			throw new IllegalArgumentException("TagThingPeer can only peer for TagThing");
		}
		this.t = (TagThing)t;
	}
	
	public void thingChanged(ThingEvent thingEvent){
	}
	
	public void draw(Graphics2D g, CoordinateMapper cm){
		if(t == null) return;
		
		String text = t.getText();
		if(text == null){
			return;
		}

		Point anchorPoint = t.getAnchorPoint();
		int wx = anchorPoint.x;
		int wy = anchorPoint.y;
		
		Font baseFont = t.getFont();
		Font f = baseFont;
		int rotationDegrees = t.getRotationAngle();
		float rotationRadians = BNAUtils.degreesToRadians(rotationDegrees);
		AffineTransform rotateTransform = AffineTransform.getRotateInstance(rotationRadians);
		if(rotationDegrees != 0){
			f = f.deriveFont(rotateTransform);
		}
		
		TextLayout textLayout = new TextLayout(text, baseFont, g.getFontRenderContext());
		java.awt.geom.Rectangle2D baseBounds = textLayout.getBounds();
		Rectangle unrotatedBoundsWorld = new Rectangle(wx + BNAUtils.round(baseBounds.getX()) - 1, 
			wy + BNAUtils.round(baseBounds.getY()) - 1, 
			BNAUtils.round(baseBounds.getWidth()) + 2, 
			BNAUtils.round(baseBounds.getHeight()) + 2);
		Shape boundingShapeWorld = unrotatedBoundsWorld;
		Rectangle textBoundsWorld = unrotatedBoundsWorld;
		if(rotationDegrees != 0){
			AffineTransform t1 = AffineTransform.getRotateInstance(rotationRadians, unrotatedBoundsWorld.x, unrotatedBoundsWorld.y + unrotatedBoundsWorld.height);
			boundingShapeWorld = t1.createTransformedShape(unrotatedBoundsWorld);
			textBoundsWorld = boundingShapeWorld.getBounds();
		}
		
		t.setProperty("#boundingBox", textBoundsWorld);

		//Scale the font
		double scale = cm.getScale();
		f = f.deriveFont((float)scale * f.getSize2D());

		g = (Graphics2D)g.create();
		g.setColor(t.getColor());

		g.setFont(f);

		int lx = cm.worldXtoLocalX(wx);
		int ly = cm.worldYtoLocalY(wy);
		g.drawString(text, lx, ly);
		
		String indicatedThingID = t.getIndicatorThingId();
		if(indicatedThingID != null){
			Point indicatorPoint = t.getIndicatorPoint();
			if(indicatorPoint != null){
				double ipx = cm.worldXtoLocalX(indicatorPoint.x);
				double ipy = cm.worldYtoLocalY(indicatorPoint.y);

				Rectangle unrotatedBoundsLocal = BNAUtils.worldRectangleToLocalRectangle(cm, unrotatedBoundsWorld);
				unrotatedBoundsLocal.x -= 1;
				unrotatedBoundsLocal.y -= 1;
				unrotatedBoundsLocal.width += 2;
				unrotatedBoundsLocal.height += 2;
				
				Line2D.Double centerLineLocal = new Line2D.Double(unrotatedBoundsLocal.x,
					unrotatedBoundsLocal.y + unrotatedBoundsLocal.height / 2,
					unrotatedBoundsLocal.x + unrotatedBoundsLocal.width,
					unrotatedBoundsLocal.y + unrotatedBoundsLocal.height / 2);
				
				//Shape boundingShapeLocal = unrotatedBoundsLocal;
				Shape boundingShapeLocal = centerLineLocal;
				if(rotationDegrees != 0){
					AffineTransform t2 = AffineTransform.getRotateInstance(rotationRadians, unrotatedBoundsLocal.x + 1, unrotatedBoundsLocal.y + 1 + unrotatedBoundsLocal.height - 2);
					boundingShapeLocal = t2.createTransformedShape(boundingShapeLocal);
					//textBoundsLocal = boundingShapeLocal.getBounds();
				}
				
				Shape s = boundingShapeLocal;
				
				Point[] possiblePoints = BNAUtils.getAllPoints(s);
				Point closestPoint = possiblePoints[0];
				double mindist = Point2D.distance(ipx, ipy, closestPoint.x, closestPoint.y);
				for(int i = 1; i < possiblePoints.length; i++){
					double dist = Point2D.distance(ipx, ipy, possiblePoints[i].x, possiblePoints[i].y);
					if(dist < mindist){
						closestPoint = possiblePoints[i];
						mindist = dist;
					}
				}
				
				g.drawLine(closestPoint.x, closestPoint.y, cm.worldXtoLocalX(indicatorPoint.x), cm.worldYtoLocalY(indicatorPoint.y));
			}
		}
		
	}
	
	public boolean isInThing(Graphics2D g, CoordinateMapper cm, int worldX, int worldY){
		String text = t.getText();
		Point anchorPoint = t.getAnchorPoint();
		int wx = anchorPoint.x;
		int wy = anchorPoint.y;
		
		Font baseFont = t.getFont();
		Font f = baseFont;
		int rotationDegrees = t.getRotationAngle();
		float rotationRadians = BNAUtils.degreesToRadians(rotationDegrees);
		AffineTransform rotateTransform = AffineTransform.getRotateInstance(rotationRadians);
		if(rotationDegrees != 0){
			f = f.deriveFont(rotateTransform);
		}
		
		TextLayout textLayout = new TextLayout(text, baseFont, g.getFontRenderContext());
		java.awt.geom.Rectangle2D baseBounds = textLayout.getBounds();
		Rectangle unrotatedBoundsWorld = new Rectangle(wx + BNAUtils.round(baseBounds.getX()) - 1, 
			wy + BNAUtils.round(baseBounds.getY()) - 1, 
			BNAUtils.round(baseBounds.getWidth()) + 2, 
			BNAUtils.round(baseBounds.getHeight()) + 2);

		Rectangle unrotatedBoundsLocal = BNAUtils.worldRectangleToLocalRectangle(cm, unrotatedBoundsWorld);
		Shape boundingShapeLocal = unrotatedBoundsLocal;
		if(rotationDegrees != 0){
			AffineTransform t2 = AffineTransform.getRotateInstance(rotationRadians, unrotatedBoundsLocal.x + 1, unrotatedBoundsLocal.y + 1 + unrotatedBoundsLocal.height - 2);
			boundingShapeLocal = t2.createTransformedShape(boundingShapeLocal);
		}
		
		int lx2 = cm.worldXtoLocalX(worldX);
		int ly2 = cm.worldYtoLocalY(worldY);
		return boundingShapeLocal.contains(lx2, ly2);
	}
	
	public Rectangle getLocalBoundingBox(Graphics2D g, CoordinateMapper cm){
		if(t.getBoundingBox() == null) return null;
		
		Rectangle r = BNAUtils.worldRectangleToLocalRectangle(cm, t.getBoundingBox());
		if(t.getIndicatorThingId() != null){
			Point p = t.getIndicatorPoint();
			if(p != null){
				int x = cm.worldXtoLocalX(p.x);
				int y = cm.worldYtoLocalY(p.y);
				r.add(x, y);
			}
		}
		r.x -= 10;
		r.y -= 10;
		r.width += 20;
		r.height += 20;
		return r;
		/*
		int lx = cm.worldXtoLocalX(t.getX());
		int ly = cm.worldYtoLocalY(t.getY());
		String label = t.getLabel();
		
		Font f = BNAUtils.DEFAULT_FONT;
		Font derivedFont = f.deriveFont(AffineTransform.getScaleInstance(cm.getScale(), cm.getScale()));

		FontRenderContext frc = g.getFontRenderContext();
		Rectangle2D stringBounds = derivedFont.getStringBounds(label, frc);
		LineMetrics lm = derivedFont.getLineMetrics(label, frc);
		
		int localX1 = lx;
		int localY1 = ly - (int)lm.getAscent();
		int localX2 = localX1 + ((int)stringBounds.getWidth());
		int localY2 = localY1 + ((int)stringBounds.getHeight());
		return new Rectangle(localX1, localY1, localX2-localX1, localY2-localY1);
		*/
	}
	
}
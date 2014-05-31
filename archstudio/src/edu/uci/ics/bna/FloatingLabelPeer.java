package edu.uci.ics.bna;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;

public class FloatingLabelPeer extends ThingPeer{
	
	private FloatingLabel t;
	
	public FloatingLabelPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof FloatingLabel)){
			throw new IllegalArgumentException("FloatingLabelPeer can only peer for FloatingLabel");
		}
		this.t = (FloatingLabel)t;
	}
	
	public void draw(Graphics2D g, CoordinateMapper cm){
		int lx = cm.worldXtoLocalX(t.getX());
		int ly = cm.worldYtoLocalY(t.getY());
		
		g.setColor(t.getColor());

		String label = t.getLabel();
		
		Font f = BNAUtils.DEFAULT_FONT;
		Font derivedFont = f.deriveFont(AffineTransform.getScaleInstance(cm.getScale(), cm.getScale()));
		g.setFont(derivedFont);
		g.drawString(label, lx, ly);
		
		FontRenderContext frc = g.getFontRenderContext();
		Rectangle2D stringBounds = derivedFont.getStringBounds(label, frc);
		LineMetrics lm = derivedFont.getLineMetrics(label, frc);
		
		int localX1 = lx;
		int localY1 = ly - (int)lm.getAscent();
		int localX2 = localX1 + ((int)stringBounds.getWidth());
		int localY2 = localY1 + ((int)stringBounds.getHeight());
		
		if(t.isSelected()){
			MarqueeUtils.drawMarqueeRectangle(g, localX1, localY1, localX2 - localX1, localY2 - localY1, t.getOffset());
		}
		
		Rectangle boundingBox = new Rectangle();
		boundingBox.x = cm.localXtoWorldX(localX1);
		boundingBox.y = cm.localYtoWorldY(localY1);
		int wx2 = cm.localXtoWorldX(localX2);
		int wy2 = cm.localYtoWorldY(localY2);
		boundingBox.width = (wx2 - boundingBox.x);
		boundingBox.height = (wy2 - boundingBox.y);
		t.setProperty("#width", boundingBox.width);
		t.setProperty("#height", boundingBox.height);
	}
	
	public boolean isInThing(Graphics2D g, CoordinateMapper cm, int worldX, int worldY){
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
		
		/*
		System.out.println("Lx1=" + localX1);
		System.out.println("Lx2=" + localX2);
		System.out.println("Ly1=" + localY1);
		System.out.println("Ly2=" + localY2);
		*/
		
		//Okay, we have the bounding box, in local coordinates, of the drawn string.
		//Now we just need to find out if the coordinate in question is in that box.
		
		int checkLocalX = cm.worldXtoLocalX(worldX);
		int checkLocalY = cm.worldYtoLocalY(worldY);
		
		/*
		System.out.println("CheckLocalX = " + checkLocalX);
		System.out.println("CheckLocalY = " + checkLocalY);
		*/
		
		return
			(checkLocalX >= localX1) &&
			(checkLocalX <= localX2) &&
			(checkLocalY >= localY1) &&
			(checkLocalY <= localY2);
	}
	
	public Rectangle getLocalBoundingBox(Graphics2D g, CoordinateMapper cm){
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
	}
}
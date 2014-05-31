package edu.uci.ics.bna;

import java.awt.*;
import java.awt.geom.*;

import edu.uci.ics.bna.thumbnail.Thumbnail;

public class RotaterThingPeer extends ThingPeer{
	private RotaterThing t;
	
	public static final int WIDGET_WIDTH = 8;
	
	public RotaterThingPeer(BNAComponent c, Thing thing){
		super(c, thing);
		if(!(thing instanceof RotaterThing)){
			throw new IllegalArgumentException("RotaterThingPeer can only peer for RotaterThing");
		}
		t = (RotaterThing)thing;
	}
	
	private static Shape getWidgetShape(Rectangle localBounds){
		Shape outerEllipse = new Ellipse2D.Double(localBounds.x, localBounds.y, localBounds.width, localBounds.height);
		Shape innerEllipse = new Ellipse2D.Double(localBounds.x + WIDGET_WIDTH, localBounds.y + WIDGET_WIDTH, localBounds.width - (WIDGET_WIDTH * 2), localBounds.height - (WIDGET_WIDTH * 2));
		GeneralPath gp = new GeneralPath(outerEllipse);
		gp.setWindingRule(GeneralPath.WIND_EVEN_ODD);
		gp.append(innerEllipse, false);
		return gp;
	}

	public void draw(Graphics2D g, CoordinateMapper cm){
		Rectangle worldBounds = t.getBoundingBox();
		Rectangle localBounds = BNAUtils.worldRectangleToLocalRectangle(cm, worldBounds);

		g.setColor(Color.BLACK);

		Shape gp = getWidgetShape(localBounds);
		
		Composite originalComposite = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.20f));
		g.fill(gp);
		g.setComposite(originalComposite);
		g.draw(gp);
		
		g.setColor(Color.RED);
		
		Arc2D.Double arc = new Arc2D.Double(localBounds.x + (WIDGET_WIDTH / 2), localBounds.y + (WIDGET_WIDTH / 2), localBounds.width - WIDGET_WIDTH, localBounds.height - WIDGET_WIDTH, 360 - t.getRotationAngle(), 1, Arc2D.PIE);
		Point2D endpoint = arc.getStartPoint();
		
		Ellipse2D.Double borderEllipse = new Ellipse2D.Double(endpoint.getX() - (WIDGET_WIDTH / 2), endpoint.getY() - (WIDGET_WIDTH / 2), WIDGET_WIDTH, WIDGET_WIDTH);
		
		originalComposite = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.60f));
		g.fill(arc);
		g.fill(borderEllipse);
		g.setComposite(originalComposite);

		//g.fillArc(localBounds.x + (WIDGET_WIDTH / 2), localBounds.y + (WIDGET_WIDTH / 2), localBounds.width +  WIDGET_WIDTH, localBounds.height + WIDGET_WIDTH, 360 - t.getRotationAngle(), 2);
		
		g.setColor(Color.BLACK);
		Font f = BNAUtils.DEFAULT_FONT;
		g.setFont(f);
		
		int angle = t.getRotationAngle();
		angle %= 360;
		while(angle < 0) angle += 360;
		g.drawString("" + angle, localBounds.x + 2, localBounds.y + localBounds.height - 5);
	}
	
	public boolean isInThing(Graphics2D g, CoordinateMapper cm, int worldX, int worldY){
		//		System.out.println("Checking " + worldX + ", " + worldY);
		//		System.out.println("BoxThing " + b.getX1() + ", " + b.getY1() + ", " + b.getX2() + ", " + b.getY2());
		Rectangle worldBounds = t.getBoundingBox();
		Shape s = getWidgetShape(worldBounds);
		return s.contains(worldX, worldY);
	}
	
	public Rectangle getLocalBoundingBox(Graphics2D g, CoordinateMapper cm){
		Rectangle localBoundingBox = BNAUtils.worldRectangleToLocalRectangle(cm, t.getBoundingBox());
		return localBoundingBox;
	}
}

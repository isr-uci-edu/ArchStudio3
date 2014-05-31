package edu.uci.ics.bna;

import java.awt.*;
import java.awt.geom.*;

public class GestureTrackThingPeer extends ThingPeer{
	
	private GestureTrackThing gt;
	
	protected int lx1, ly1, lx2, ly2;
	
	public GestureTrackThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof GestureTrackThing)){
			throw new IllegalArgumentException("GestureTrackThingPeer can only peer for GestureTrackThing");
		}
		gt = (GestureTrackThing)t;
	}

	final Stroke stroke = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	 
	public void draw(Graphics2D g, CoordinateMapper cm){
		Point p1 = gt.getPoint1();
		Point p2 = gt.getPoint2();
		
		if((p1 == null) || (p2 == null)){
			return;
		}
		
		lx1 = cm.worldXtoLocalX(p1.x);
		ly1 = cm.worldYtoLocalY(p1.y);
		
		lx2 = cm.worldXtoLocalX(p2.x);
		ly2 = cm.worldYtoLocalY(p2.y);
		
		
		Paint originalPaint = g.getPaint();
		Stroke originalStroke = g.getStroke();		
		Composite originalComposite = g.getComposite();

		Color color = Color.RED;
		g.setColor(color);
		g.setPaint(color);
		g.setStroke(stroke);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, gt.getEphemeralTransparency()));
		
		g.drawLine(lx1, ly1, lx2, ly2);
		
		g.setComposite(originalComposite);
		g.setStroke(originalStroke);
		g.setPaint(originalPaint);
	}

	public boolean isInThing(Graphics2D g, CoordinateMapper cm, int worldX, int worldY){
		return false;
	}

	public Rectangle getLocalBoundingBox(Graphics2D g, CoordinateMapper cm){
		Point p1 = gt.getPoint1();
		Point p2 = gt.getPoint2();
		
		if((p1 == null) || (p2 == null)){
			return BNAUtils.NONEXISTENT_RECTANGLE;
		}
		
		lx1 = cm.worldXtoLocalX(p1.x);
		ly1 = cm.worldYtoLocalY(p1.y);
		
		lx2 = cm.worldXtoLocalX(p2.x);
		ly2 = cm.worldYtoLocalY(p2.y);
		
		lx1 -= ((BasicStroke)stroke).getLineWidth();
		ly1 -= ((BasicStroke)stroke).getLineWidth();
		lx2 += ((BasicStroke)stroke).getLineWidth() * 2;
		ly2 += ((BasicStroke)stroke).getLineWidth() * 2;
		Rectangle r = new Rectangle(lx1, ly1, lx2-lx1, ly2-ly1);
		r = BNAUtils.normalizeRectangle(r); 
		return r; 
	}

}

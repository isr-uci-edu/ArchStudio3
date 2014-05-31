package edu.uci.ics.bna;

import java.awt.*;
import java.awt.geom.*;

public class GlowboxThingPeer extends ThingPeer{
	
	private GlowboxThing b;
	
	protected int lx1, ly1, lx2, ly2, lw, lh;
	
	public GlowboxThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof GlowboxThing)){
			throw new IllegalArgumentException("GlowboxThingPeer can only peer for GlowboxThing");
		}
		b = (GlowboxThing)t;
	}

	//private Rectangle tmpRect = new Rectangle();
	
	public void draw(Graphics2D g, CoordinateMapper cm){
		lx1 = cm.worldXtoLocalX(b.getX());
		ly1 = cm.worldYtoLocalY(b.getY());
		
		lx2 = cm.worldXtoLocalX(b.getX2());
		ly2 = cm.worldYtoLocalY(b.getY2());
		
		lw = lx2 - lx1;
		lh = ly2 - ly1;
		
		Color color = b.getColor();
		g.setColor(color);
		g.drawRect(lx1, ly1, lw, lh);
		
		int offset = b.getOffset();
		
		int l2x1 = lx1 - offset;
		int l2y1 = ly1 - offset;
		int l2x2 = lx2 + offset;
		int l2y2 = ly2 + offset;
		int l2w = l2x2 - l2x1;
		int l2h = l2y2 - l2y1;
		
		Composite originalComposite = g.getComposite();
		g.setPaint(color);
		float divFactor = (float)offset + 1;
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f / divFactor));
		g.drawRect(l2x1, l2y1, l2w, l2h);
		g.setComposite(originalComposite);
	}
	
	public boolean isInThing(Graphics2D g, CoordinateMapper cm, int worldX, int worldY){
		return false;
	}

	public Rectangle getLocalBoundingBox(Graphics2D g, CoordinateMapper cm){
		lx1 = cm.worldXtoLocalX(b.getX());
		ly1 = cm.worldYtoLocalY(b.getY());
		
		lx2 = cm.worldXtoLocalX(b.getX2());
		ly2 = cm.worldYtoLocalY(b.getY2());
		
		lw = lx2 - lx1;
		lh = ly2 - ly1;
		
		int offset = b.getOffset();
		
		int l2x1 = lx1 - offset;
		int l2y1 = ly1 - offset;
		int l2x2 = lx2 + offset;
		int l2y2 = ly2 + offset;
		int l2w = l2x2 - l2x1;
		int l2h = l2y2 - l2y1;
		
		return new Rectangle(l2x1, l2y1, l2w, l2h);
	}

}

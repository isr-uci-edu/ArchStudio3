package edu.uci.ics.bna;

import java.awt.*;

public class DotMarqueeThingPeer extends ThingPeer{
	
	private DotMarqueeThing t;
	
	public DotMarqueeThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof DotMarqueeThing)){
			throw new IllegalArgumentException("BoxThingPeer can only peer for BoxThing");
		}
		this.t = (DotMarqueeThing)t;
	}
	
	public void draw(Graphics2D g, CoordinateMapper cm){
		int lx1 = cm.worldXtoLocalX(t.getX1());
		int ly1 = cm.worldYtoLocalY(t.getY1());
		
		int lx2 = cm.worldXtoLocalX(t.getX2());
		int ly2 = cm.worldYtoLocalY(t.getY2());
		
		int initOffset = t.getOffset();
		
		MarqueeUtils.drawMarqueeRectangle(g, lx1, ly1, lx2-lx1, ly2-ly1, initOffset);
	}
	
	public boolean isInThing(Graphics2D g, CoordinateMapper cm, int worldX, int worldY){
//		System.out.println("Checking " + worldX + ", " + worldY);
//		System.out.println("BoxThing " + b.getX1() + ", " + b.getY1() + ", " + b.getX2() + ", " + b.getY2());
		return false;
/*
		return((worldX >= t.getX1()) &&
			(worldX <= t.getX2()) &&
			(worldY >= t.getY1()) &&
			(worldY <= t.getY2()));
			*/
	}
	
	public Rectangle getLocalBoundingBox(Graphics2D g, CoordinateMapper cm){
		return BNAUtils.worldRectangleToLocalRectangle(cm, t.getBoundingBox());
	}
}
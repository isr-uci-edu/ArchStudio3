package edu.uci.ics.bna;

import java.awt.*;

public class NoThingPeer extends ThingPeer{
	
	public NoThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof NoThing)){
			throw new IllegalArgumentException("NoThingPeer can only peer for NoThing");
		}
	}
	
	public void draw(Graphics2D g, CoordinateMapper cm){}
	public boolean isInThing(Graphics2D g, CoordinateMapper cm, int worldX, int worldY){
		return false;
	}
	
	public Rectangle getLocalBoundingBox(Graphics2D g, CoordinateMapper cm){
		return BNAUtils.NONEXISTENT_RECTANGLE;
	}
	
}

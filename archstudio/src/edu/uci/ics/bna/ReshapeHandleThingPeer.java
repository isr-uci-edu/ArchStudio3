package edu.uci.ics.bna;

import java.awt.*;

public class ReshapeHandleThingPeer extends ThingPeer{
	
	private ReshapeHandleThing t;
	
	public ReshapeHandleThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof ReshapeHandleThing)){
			throw new IllegalArgumentException("ReshapeHandleThingPeer can only peer for ReshapeHandleThing");
		}
		this.t = (ReshapeHandleThing)t;
	}
	
	private Rectangle getRectangle(Graphics2D g, CoordinateMapper cm){
		int lx = cm.worldXtoLocalX(t.getX());
		int ly = cm.worldYtoLocalY(t.getY());
		
		int orientation = t.getOrientation();
		Rectangle r = null;
		switch(orientation){
		case ReshapeHandleThing.ORIENTATION_N:
			r = new Rectangle(cm.localXtoWorldX(lx - 2), cm.localYtoWorldY(ly - 4), 5, 5);
			break;
		case ReshapeHandleThing.ORIENTATION_S:
			r = new Rectangle(cm.localXtoWorldX(lx - 2), cm.localYtoWorldY(ly), 5, 5);
			break;
		case ReshapeHandleThing.ORIENTATION_E:
			r = new Rectangle(cm.localXtoWorldX(lx), cm.localYtoWorldY(ly - 2), 5, 5);
			break;
		case ReshapeHandleThing.ORIENTATION_W:
			r = new Rectangle(cm.localXtoWorldX(lx - 4), cm.localYtoWorldY(ly - 2), 5, 5);
			break;
		case ReshapeHandleThing.ORIENTATION_NW:
			r = new Rectangle(cm.localXtoWorldX(lx - 4), cm.localYtoWorldY(ly - 4), 5, 5);
			break;
		case ReshapeHandleThing.ORIENTATION_SE:
			r = new Rectangle(cm.localXtoWorldX(lx), cm.localYtoWorldY(ly), 5, 5);
			break;
		case ReshapeHandleThing.ORIENTATION_NE:
			r = new Rectangle(cm.localXtoWorldX(lx), cm.localYtoWorldY(ly - 4), 5, 5);
			break;
		case ReshapeHandleThing.ORIENTATION_SW:
			r = new Rectangle(cm.localXtoWorldX(lx - 4), cm.localYtoWorldY(ly), 5, 5);
			break;
		case ReshapeHandleThing.ORIENTATION_CENTER:
			r = new Rectangle(cm.localXtoWorldX(lx - 2), cm.localYtoWorldY(ly - 2), 5, 5);
			break;
		}
		return r;
	}
	
	public void draw(Graphics2D g, CoordinateMapper cm){
		Rectangle r = getRectangle(g, cm);
		g.setColor(t.getColor());
		g.fillRect(cm.worldXtoLocalX(r.x), cm.worldYtoLocalY(r.y), 5, 5);
	}
	
	public boolean isInThing(Graphics2D g, CoordinateMapper cm, int worldX, int worldY){
		Rectangle r = getRectangle(g, cm);
		int checkLocalX = cm.worldXtoLocalX(worldX);
		int checkLocalY = cm.worldYtoLocalY(worldY);
		
		r.x = cm.worldXtoLocalX(r.x);
		r.y = cm.worldYtoLocalY(r.y);
		
		return((checkLocalX >= r.x) &&
			(checkLocalX <= (r.x + r.width)) &&
			(checkLocalY >= r.y) &&
			(checkLocalY <= (r.y + r.height)));
	}
	
	
	public Rectangle getLocalBoundingBox(Graphics2D g, CoordinateMapper cm){
		Rectangle r = getRectangle(g, cm);
		r.x = cm.worldXtoLocalX(r.x);
		r.y = cm.worldYtoLocalY(r.y);
		return r;
	}
}
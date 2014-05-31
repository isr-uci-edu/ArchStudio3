package edu.uci.ics.bna.swingthing;

import edu.uci.ics.bna.*;

import java.awt.*;

public class LocalReshapeHandlePeer extends ThingPeer{
	
	private LocalReshapeHandle t;
	
	public LocalReshapeHandlePeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof LocalReshapeHandle)){
			throw new IllegalArgumentException("LocalReshapeHandlePeer can only peer for LocalReshapeHandle");
		}
		this.t = (LocalReshapeHandle)t;
	}
	
	private Rectangle getLocalRectangle(Graphics2D g, CoordinateMapper cm){
		int lx = t.getX();
		int ly = t.getY();
		
		int orientation = t.getOrientation();
		Rectangle r = null;
		switch(orientation){
		case LocalReshapeHandle.ORIENTATION_N:
			r = new Rectangle(lx - 2, ly - 4, 5, 5);
			break;
		case LocalReshapeHandle.ORIENTATION_S:
			r = new Rectangle(lx - 2, ly, 5, 5);
			break;
		case LocalReshapeHandle.ORIENTATION_E:
			r = new Rectangle(lx, ly - 2, 5, 5);
			break;
		case LocalReshapeHandle.ORIENTATION_W:
			r = new Rectangle(lx - 4, ly - 2, 5, 5);
			break;
		case LocalReshapeHandle.ORIENTATION_NW:
			r = new Rectangle(lx - 4, ly - 4, 5, 5);
			break;
		case LocalReshapeHandle.ORIENTATION_SE:
			r = new Rectangle(lx, ly, 5, 5);
			break;
		case LocalReshapeHandle.ORIENTATION_NE:
			r = new Rectangle(lx, ly - 4, 5, 5);
			break;
		case LocalReshapeHandle.ORIENTATION_SW:
			r = new Rectangle(lx - 4, ly, 5, 5);
			break;
		case LocalReshapeHandle.ORIENTATION_CENTER:
			r = new Rectangle(lx - 2, ly - 2, 5, 5);
			break;
		}
		return r;
	}
	
	public void draw(Graphics2D g, CoordinateMapper cm){
		Rectangle r = getLocalRectangle(g, cm);
		g.setColor(t.getColor());
		g.fillRect(r.x, r.y, 5, 5);
	}
	
	public boolean isInThing(Graphics2D g, CoordinateMapper cm, int worldX, int worldY){
		Rectangle r = getLocalRectangle(g, cm);
		int checkLocalX = cm.worldXtoLocalX(worldX);
		int checkLocalY = cm.worldYtoLocalY(worldY);
		
		//r.x = r.x;
		//r.y = r.y;
		
		return((checkLocalX >= r.x) &&
			(checkLocalX <= (r.x + r.width)) &&
			(checkLocalY >= r.y) &&
			(checkLocalY <= (r.y + r.height)));
	}
	
	public Rectangle getLocalBoundingBox(Graphics2D g, CoordinateMapper cm){
		return getLocalRectangle(g, cm);
	}

}
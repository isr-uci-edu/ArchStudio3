package edu.uci.ics.bna;

import java.awt.*;
import java.awt.geom.*;

import edu.uci.ics.bna.thumbnail.Thumbnail;

public class OutlineBoxThingPeer extends ThingPeer{
	
	private OutlineBoxThing b;
	
	protected int lx1, ly1, lx2, ly2, lw, lh;
	
	public OutlineBoxThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof OutlineBoxThing)){
			throw new IllegalArgumentException("OutlineBoxThingPeer can only peer for OutlineBoxThing");
		}
		b = (OutlineBoxThing)t;
	}
	
	public void draw(Graphics2D g, CoordinateMapper cm){
		lx1 = cm.worldXtoLocalX(b.getX());
		ly1 = cm.worldYtoLocalY(b.getY());
		
		lx2 = cm.worldXtoLocalX(b.getX2());
		ly2 = cm.worldYtoLocalY(b.getY2());
		
		lw = lx2 - lx1;
		lh = ly2 - ly1;
		
		Color trimColor = b.getTrimColor();
		g.setColor(trimColor);
		Stroke newStroke = b.getStroke();
		Stroke oldStroke = null;
		if(newStroke != null){
			oldStroke = g.getStroke();
			g.setStroke(newStroke);
		}
		g.drawRect(lx1, ly1, lw, lh);
		if(newStroke != null){
			g.setStroke(oldStroke);
		}
	}
	
	public boolean isInThing(Graphics2D g, CoordinateMapper cm, int worldX, int worldY){
		/*return((worldX >= b.getX()) &&
			(worldX <= b.getX2()) &&
			(worldY >= b.getY()) &&
			(worldY <= b.getY2()));*/
		return false;
	}
	
	public Rectangle getLocalBoundingBox(Graphics2D g, CoordinateMapper cm){
		Rectangle localBoundingBox = BNAUtils.worldRectangleToLocalRectangle(cm, b.getBoundingBox());
		Stroke stroke = b.getStroke();
		
		//The box might be stroked with a thick stroke, so we have to figure the
		//bounding box for the stroked shape.
		if(stroke != null){
			Shape s = stroke.createStrokedShape(localBoundingBox);
			localBoundingBox = s.getBounds();
		}
		return new Rectangle(localBoundingBox);
	}
}

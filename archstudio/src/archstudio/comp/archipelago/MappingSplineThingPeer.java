package archstudio.comp.archipelago;

import java.awt.*;
import java.awt.geom.*;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.BNAUtils;
import edu.uci.ics.bna.CoordinateMapper;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.ThingPeer;

public class MappingSplineThingPeer extends ThingPeer{
	
	private MappingSplineThing t;
	
	public MappingSplineThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof MappingSplineThing)){
			throw new IllegalArgumentException("MappingSplineThingPeer can only peer for MappingSplineThing");
		}
		this.t = (MappingSplineThing)t;
	}
	
	final Stroke lineStroke = new BasicStroke(1.0f,
		BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[]{3.0f, 3.0f}, 0.0f);
	
	public void draw(Graphics2D g, CoordinateMapper cm){
		synchronized(t.propertyLockObject){
			Point worldPoint1 = t.getFirstPoint();
			Point worldPoint2 = t.getSecondPoint();
			
			if((worldPoint1 == null) || (worldPoint2 == null)){
				return;
			}
			
			int lx1 = cm.worldXtoLocalX(worldPoint1.x);
			int ly1 = cm.worldYtoLocalY(worldPoint1.y);
			
			int lx2 = cm.worldXtoLocalX(worldPoint2.x);
			int ly2 = cm.worldYtoLocalY(worldPoint2.y);
			
			Color oldColor = g.getColor();
			Paint oldPaint = g.getPaint();
			Stroke oldStroke = g.getStroke();
			g.setColor(Color.BLACK);
			g.setStroke(lineStroke);
			g.drawLine(lx1, ly1, lx2, ly2);
			g.setStroke(oldStroke);
			g.setPaint(oldPaint);
			g.setPaint(oldColor);
		}
	}
	
	public boolean isInThing(Graphics2D g, CoordinateMapper cm, int worldX, int worldY){
		synchronized(t.propertyLockObject){
			Point worldPoint1 = t.getFirstPoint();
			Point worldPoint2 = t.getSecondPoint();
			
			if((worldPoint1 == null) || (worldPoint2 == null)){
				return false;
			}
			
			int lx1 = cm.worldXtoLocalX(worldPoint1.x);
			int ly1 = cm.worldYtoLocalY(worldPoint1.y);
			
			int lx2 = cm.worldXtoLocalX(worldPoint2.x);
			int ly2 = cm.worldYtoLocalY(worldPoint2.y);
			
			int lx = cm.worldXtoLocalX(worldX);
			int ly = cm.worldYtoLocalY(worldY);
			
			int dist = (int)Line2D.Double.ptSegDist((double)lx1, (double)ly1, (double)lx2, (double)ly2, (double)lx, (double)ly);
			if(dist < 4){
				return true;
			}
			return false;
		}
	}
	
	public Rectangle getLocalBoundingBox(Graphics2D g, CoordinateMapper cm) {
		Point worldPoint1 = t.getFirstPoint();
		Point worldPoint2 = t.getSecondPoint();
			
		if((worldPoint1 == null) || (worldPoint2 == null)){
			return BNAUtils.NONEXISTENT_RECTANGLE;
		}
			
		int lx1 = cm.worldXtoLocalX(worldPoint1.x);
		int ly1 = cm.worldYtoLocalY(worldPoint1.y);
			
		int lx2 = cm.worldXtoLocalX(worldPoint2.x);
		int ly2 = cm.worldYtoLocalY(worldPoint2.y);
		
		Rectangle r = new Rectangle(lx1, ly1, lx2-lx1, ly2-ly1);
		r = BNAUtils.normalizeRectangle(r);
		if(r.width == 0) r.width = 1;
		if(r.height == 0) r.height = 1;
		return r;
	}
}
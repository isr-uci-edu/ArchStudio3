package edu.uci.ics.bna;

import java.awt.*;

import edu.uci.ics.widgets.WidgetUtils;

public class EndpointThingPeer extends ThingPeer{
	
	private EndpointThing t;
	
	public EndpointThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof EndpointThing)){
			throw new IllegalArgumentException("EndpointThingPeer can only peer for EndpointThing");
		}
		this.t = (EndpointThing)t;
	}
	
	private Rectangle tmpRect = new Rectangle();
	
	protected boolean isVisible(){
		return t.isVisible();
	}
	
	public void draw(Graphics2D g, CoordinateMapper cm){
		if(!isVisible())
			return;
		
		int wx1 = t.getX();
		int wy1 = t.getY();
		
		int wx2 = t.getX2();
		int wy2 = t.getY2();
		
		int lx1 = cm.worldXtoLocalX(wx1);
		int ly1 = cm.worldYtoLocalY(wy1);
		
		int lx2 = cm.worldXtoLocalX(wx2);
		int ly2 = cm.worldYtoLocalY(wy2);
		
		int lw = lx2 - lx1;
		int lh = ly2 - ly1;
		
		Color color = t.getColor();
		g.setColor(color);
		g.fillRect(lx1, ly1, lw, lh);

		g.setColor(t.getTrimColor());

		Stroke newStroke = t.getStroke();
		Stroke oldStroke = null;
		if(newStroke != null){
			oldStroke = g.getStroke();
			g.setStroke(newStroke);
		}
		g.drawRect(lx1, ly1, lw, lh);
		if(newStroke != null){
			g.setStroke(oldStroke);
		}

		int flow = t.getFlow();
		if(flow != EndpointThing.FLOW_NONE){
			int orientation = t.getOrientation();
			
			if(
				((flow == EndpointThing.FLOW_IN) && (orientation == EndpointThing.ORIENTATION_N)) ||
				((flow == EndpointThing.FLOW_OUT) && (orientation == EndpointThing.ORIENTATION_S))
			){
				Polygon p = WidgetUtils.createIsocolesTriangle(new Rectangle(lx1 + 1, ly1 + 1, lw - 2, lh - 2), WidgetUtils.FACING_SOUTH);
				g.fill(p);
			}
			else if(
				((flow == EndpointThing.FLOW_OUT) && (orientation == EndpointThing.ORIENTATION_N)) ||
				((flow == EndpointThing.FLOW_IN) && (orientation == EndpointThing.ORIENTATION_S))
			){
				Polygon p = WidgetUtils.createIsocolesTriangle(new Rectangle(lx1 + 1, ly1 + 1, lw - 2, lh - 2), WidgetUtils.FACING_NORTH);
				g.fill(p);
			}
			else if(
				((flow == EndpointThing.FLOW_OUT) && (orientation == EndpointThing.ORIENTATION_W)) ||
				((flow == EndpointThing.FLOW_IN) && (orientation == EndpointThing.ORIENTATION_E))
			){
				Polygon p = WidgetUtils.createIsocolesTriangle(new Rectangle(lx1 + 1, ly1 + 1, lw - 2, lh - 2), WidgetUtils.FACING_WEST);
				g.fill(p);
			}
			else if(
				((flow == EndpointThing.FLOW_IN) && (orientation == EndpointThing.ORIENTATION_W)) ||
				((flow == EndpointThing.FLOW_OUT) && (orientation == EndpointThing.ORIENTATION_E))
			){
				Polygon p = WidgetUtils.createIsocolesTriangle(new Rectangle(lx1 + 1, ly1 + 1, lw - 2, lh - 2), WidgetUtils.FACING_EAST);
				g.fill(p);
			}
			else if(
				((flow == EndpointThing.FLOW_INOUT) && (orientation == EndpointThing.ORIENTATION_N)) ||
				((flow == EndpointThing.FLOW_INOUT) && (orientation == EndpointThing.ORIENTATION_S))
			){
				Polygon p = WidgetUtils.createIsocolesTriangle(new Rectangle(lx1 + 1, ly1 + 1, lw - 2, (lh - 2) / 2), WidgetUtils.FACING_NORTH);
				g.fill(p);
				p = WidgetUtils.createIsocolesTriangle(new Rectangle(lx1 + 1, ly1 + 1 + (lh / 2), lw - 2, ((lh - 2) / 2) - 1), WidgetUtils.FACING_SOUTH);
				g.fill(p);
			}
			else if(
				((flow == EndpointThing.FLOW_INOUT) && (orientation == EndpointThing.ORIENTATION_W)) ||
				((flow == EndpointThing.FLOW_INOUT) && (orientation == EndpointThing.ORIENTATION_E))
			){
				Polygon p = WidgetUtils.createIsocolesTriangle(new Rectangle(lx1 + 1, ly1 + 1, (lw - 2) / 2, lh - 2), WidgetUtils.FACING_WEST);
				g.fill(p);
				p = WidgetUtils.createIsocolesTriangle(new Rectangle(lx1 + 1 + (lw / 2), ly1 + 1, ((lw - 2) / 2) - 1, lh - 2), WidgetUtils.FACING_EAST);
				g.fill(p);
			}
		}
	}
	
	public boolean isInThing(Graphics2D g, CoordinateMapper cm, int worldX, int worldY){
		if(!isVisible())
			return false;
		
//		System.out.println("Checking " + worldX + ", " + worldY);
//		System.out.println("BoxThing " + b.getX1() + ", " + b.getY1() + ", " + b.getX2() + ", " + b.getY2());
		
		return((worldX >= t.getX()) &&
			(worldX <= t.getX2()) &&
			(worldY >= t.getY()) &&
			(worldY <= t.getY2()));
	}

	public Rectangle getLocalBoundingBox(Graphics2D g, CoordinateMapper cm){
		if(!isVisible())
			return new Rectangle();
		
		return BNAUtils.worldRectangleToLocalRectangle(cm, t.getBoundingBox());
	}
}
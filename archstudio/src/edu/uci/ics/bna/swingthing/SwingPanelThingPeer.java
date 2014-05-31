package edu.uci.ics.bna.swingthing;

import java.awt.*;

import javax.swing.*;

import edu.uci.ics.bna.*;

public class SwingPanelThingPeer extends ThingPeer implements BNAModelListener{
	
	private SwingPanelThing spt;
	
	protected int lx1, ly1, lx2, ly2, lw, lh;

	public SwingPanelThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof SwingPanelThing)){
			throw new IllegalArgumentException("SwingPanelThingPeer can only peer for SwingPanelThing");
		}
		spt = (SwingPanelThing)t;
		c.getModel().addBNAModelListener(this);
	}
	
	JComponent addedPanel = null;
	
	public void bnaModelChanged(BNAModelEvent evt){
		if(evt.getEventType() == BNAModelEvent.THING_ADDED){
			if(evt.getTargetThing() == spt){
				addedPanel = spt.getPanel();
				bnaComponent.add(addedPanel);
			}
		}
		if(evt.getEventType() == BNAModelEvent.THING_CHANGED){
			if(evt.getTargetThing() == spt){
				ThingEvent tevt = evt.getThingEvent();
				if(tevt != null){
					if(tevt.getPropertyName().equals(SwingPanelThing.PANEL_PROPERTY_NAME)){
						bnaComponent.remove(addedPanel);
						addedPanel = spt.getPanel();
						bnaComponent.add(addedPanel);
					}
				}
			}
		}
		if(evt.getEventType() == BNAModelEvent.THING_REMOVED){
			if(evt.getTargetThing() == spt){
				if(addedPanel != null){
					bnaComponent.remove(addedPanel);
					bnaComponent.getModel().removeBNAModelListener(this);
				}
			}			
		}
	}

	

	public void draw(Graphics2D g, CoordinateMapper cm){
		JComponent p = spt.getPanel();
		if(p == null) return;
		
		if(addedPanel == null){
			addedPanel = p;
			bnaComponent.add(addedPanel);
		}

		lx1 = spt.getX();
		ly1 = spt.getY();
		
		lx2 = spt.getX2();
		ly2 = spt.getY2();
		
		lw = lx2 - lx1;
		lh = ly2 - ly1;
		
		p.setBounds(lx1+1, ly1+1, lw-1, lh-1);

		//LayoutManager lm = p.getLayout();
		//if(lm != null){
		//	lm.layoutContainer(p);
		//}

		//This validate should handle the above commented out lines
		p.validate();
		
		/*
		Color color = spt.getColor();
		g.setColor(color);
		//g.fillRect(lx1, ly1, lw, lh);
		
		g.fill3DRect(lx1, ly1, lw, lh, true);

		g.setColor(spt.getTrimColor());
		g.drawRect(lx1, ly1, lw, lh);
		
		String label = spt.getLabel();
		if(label != null){
			g.setColor(spt.getTrimColor());
			int inset = lw / 20;
			Rectangle fontBoundRect = new Rectangle(lx1 + inset, ly1, lw - inset - inset, lh);
			BNAUtils.renderBoundedString(BNAUtils.DEFAULT_FONT, label, g, fontBoundRect, true);
		}
		*/
		
		Point ip = spt.getIndicatorPoint();
		if(ip != null){
			//System.out.println("getting indicator shape");
			Shape s = getIndicatorShape(cm);
			g.setPaint(spt.getTrimColor());
			g.fill(s);
		}
	}
	
	protected Shape getIndicatorShape(CoordinateMapper cm){
		Point p = spt.getIndicatorPoint();
		if(p == null) return null;
		
		//Let's do everything in local coordinates, so we'll convert the
		//only world coordinate (ix, iy) to local
		
		int ix = (int)p.getX();
		ix = cm.worldXtoLocalX(ix);
		int iy = (int)p.getY();
		iy = cm.worldYtoLocalY(iy);
		
		int x1 = spt.getX();
		int x2 = spt.getX2();
		
		if(x1 > x2){
			int temp = x1;
			x1 = x2;
			x2 = temp;
		}
		
		int y1 = spt.getY();
		int y2 = spt.getY2();
		
		if(y1 > y2){
			int temp = y1;
			y1 = y2;
			y2 = temp;
		}
		
		int cx = ((x2 - x1) / 2) + x1;
		int cy = ((y2 - y1) / 2) + y1;
		
		int ulX, ulY, urX, urY, llX, llY, lrX, lrY;
		
		ulX = cx - 5; if(ulX < x1) ulX = x1;
		ulY = cy - 5; if(ulY < y1) ulY = y1;
		
		urX = cx + 5; if(urX > x2) urX = x2;
		urY = cy - 5; if(urY < y1) urY = y1;
		
		llX = cx - 5; if(llX < x1) llX = x1;
		llY = cy + 5; if(llY > y2) llY = y2;
		
		lrX = cx + 5; if(lrX > x2) lrX = x2;
		lrY = cy + 5; if(lrY > y2) lrY = y2;
		
		//Now figure out which corner to move to the indicator point.
		if((ix < cx) && (iy < cy)){
			ulX = ix;
			ulY = iy;
		}
		else if((ix < cx) && (iy >= cy)){
			llX = ix;
			llY = iy;
		}
		else if((ix >= cx) && (iy < cy)){
			urX = ix;
			urY = iy;
		}
		else{
			lrX = ix;
			lrY = iy;
		}
		
		/*
		ulX = cm.worldXtoLocalX(ulX);
		urX = cm.worldXtoLocalX(urX);
		llX = cm.worldXtoLocalX(llX);
		lrX = cm.worldXtoLocalX(lrX);

		ulY = cm.worldYtoLocalY(ulY);
		urY = cm.worldYtoLocalY(urY);
		llY = cm.worldYtoLocalY(llY);
		lrY = cm.worldYtoLocalY(lrY);
		*/
		
		//Now make a shape.
		Polygon poly = new Polygon();
		poly.addPoint(ulX, ulY);
		poly.addPoint(urX, urY);
		poly.addPoint(lrX, lrY);
		poly.addPoint(llX, llY);
		
		return poly;
	}
	
	public boolean isInThing(Graphics2D g, CoordinateMapper cm, int worldX, int worldY){
		//		System.out.println("Checking " + worldX + ", " + worldY);
		//		System.out.println("BoxThing " + b.getX1() + ", " + b.getY1() + ", " + b.getX2() + ", " + b.getY2());
		
		int localX = cm.worldXtoLocalX(worldX);
		int localY = cm.worldYtoLocalY(worldY);
		
		return((localX >= spt.getX()) &&
			(localX <= spt.getX2()) &&
			(localY >= spt.getY()) &&
			(localY <= spt.getY2()));
	}
	
	/*
	public void draw(Graphics2D g, CoordinateMapper cm){
		JComponent p = spt.getPanel();
		if(p == null) return;
		
		if(addedPanel == null){
			addedPanel = p;
			bnaComponent.add(addedPanel);
		}

		lx1 = spt.getX();
		ly1 = spt.getY();
		
		lx2 = spt.getX2();
		ly2 = spt.getY2();
		
		lw = lx2 - lx1;
		lh = ly2 - ly1;
		
		//Color color = spt.getColor();
		//g.setColor(color);
		p.setBounds(lx1+1, ly1+1, lw-1, lh-1);

		LayoutManager lm = p.getLayout();
		if(lm != null){
			lm.layoutContainer(p);
		}
		
		//if(!spt.isSelected()){
			g.setColor(spt.getTrimColor());
			g.drawRect(lx1, ly1, lw, lh);
		//}
		//else{
			//MarqueeUtils.drawMarqueeRectangle(g, lx1, ly1, lw, lh, spt.getOffset());
		//}
			
		Point pt = spt.getIndicatorPoint();
		if(pt != null){
			Shape s = getIndicatorShape(cm);
			g.setPaint(Color.BLACK);
			g.fill(s);
		}
		
	}
	*/
	
	public Rectangle getLocalBoundingBox(Graphics2D g, CoordinateMapper cm){
		Rectangle localBoundingBox = new Rectangle(spt.getLocalBoundingBox());
		Point ip = spt.getIndicatorPoint();
		if(ip != null){
			int ipx = cm.worldXtoLocalX(ip.x);
			int ipy = cm.worldYtoLocalY(ip.y);
			localBoundingBox.add(ipx, ipy);
		}
		return new Rectangle(localBoundingBox);
	}
	
}
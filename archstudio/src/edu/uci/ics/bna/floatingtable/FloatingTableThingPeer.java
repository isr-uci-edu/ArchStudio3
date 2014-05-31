package edu.uci.ics.bna.floatingtable;

import edu.uci.ics.bna.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;

public class FloatingTableThingPeer extends ThingPeer{
	
	private FloatingTableThing ta;
	
	public static final int GRAPHICS_SCALING = 100;
	public static final int IMAGE_SCALING = 200;
	 
	public static final int drawMode = GRAPHICS_SCALING;
	public static final boolean ANTIALIAS_TEXT = true;
	
	protected int lx1, ly1, lx2, ly2, lw, lh;
	
	public FloatingTableThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof FloatingTableThing)){
			throw new IllegalArgumentException("FloatingTableThingPeer can only peer for FloatingTableThing");
		}
		ta = (FloatingTableThing)t;
	}

	protected static JLabel getJLabel(TableData td, int width, int height){
		String tableHtml;
		if(td == null){
			tableHtml = "<html>(No Property Data)</html>";
		}
		else{
			tableHtml = "<html>" + td.getHtml(width, height) + "</html>";
		}
		JLabel label = new JLabel(tableHtml);
		return label;
	}
	
	public static Dimension getPreferredSize(TableData td){
		Dimension d = new Dimension(getJLabel(td, 0, 0).getPreferredSize());
		//d.width+= 5;
		//d.height++;
		return d;
	}

	public void draw(Graphics2D g, CoordinateMapper cm){
		if(drawMode == IMAGE_SCALING){
			drawWithImageScaling(g, cm);
		}
		else if(drawMode == GRAPHICS_SCALING){
			drawWithGraphicsScaling(g, cm);
		}
		else{
			throw new RuntimeException("Invalid draw mode.");
		}
	}

	public void drawWithImageScaling(Graphics2D g, CoordinateMapper cm){
		lx1 = cm.worldXtoLocalX(ta.getX());
		ly1 = cm.worldYtoLocalY(ta.getY());
		
		lx2 = cm.worldXtoLocalX(ta.getX2());
		ly2 = cm.worldYtoLocalY(ta.getY2());
		
		lw = lx2 - lx1;
		lh = ly2 - ly1;
		
		int ww = ta.getX2() - ta.getX();
		int wh = ta.getY2() - ta.getY();
		
		float transparency = ta.getTransparency();
		Composite originalComposite = g.getComposite();
		if(transparency != 1.0f){
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
		}

		Color color = ta.getColor();
		g.setColor(color);
		g.fillRect(lx1, ly1, lw, lh);
		
		TableData td = ta.getTableData();
		
		JLabel label = getJLabel(td, 0, 0);
		label.setSize(ww, wh);

		BufferedImage ghostImage = new BufferedImage(ww-1, wh-1, BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D g2 = (Graphics2D)ghostImage.createGraphics();
		
		//AffineTransform oldTransform = g2.getTransform();
		//if(cm.getScale() != 1.0d){
		//	g2.setTransform(AffineTransform.getScaleInstance(cm.getScale(), cm.getScale()));
		//}
		label.paint(g2);
		
		//AffineTransform oldTransform2 = g.getTransform();
		//if(cm.getScale() != 1.0d){
		//	g.setTransform(AffineTransform.getScaleInstance(cm.getScale(), cm.getScale()));
		//}
		
		Image img = ghostImage.getScaledInstance(lw, lh, BufferedImage.SCALE_SMOOTH);
		g.drawImage(img, lx1, ly1, null);
		//g.setTransform(oldTransform2);
		
		if(transparency != 1.0f){
			g.setComposite(originalComposite);
		}

		/*
		if(!b.isSelected()){
			g.setColor(b.getTrimColor());
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
		else{
			MarqueeUtils.drawMarqueeRectangle(g, lx1, ly1, lw, lh, b.getOffset());
		}
		*/
		
		color = ta.getTrimColor();

		Point p = ta.getIndicatorPoint();
		if(p != null){
			Shape s = getIndicatorShape(cm);
			Area shapeArea = new Area(s);
			shapeArea.subtract(new Area(new Rectangle(lx1, ly1, lw, lh)));
			g.setPaint(color);
			//g.fill(s);
			g.fill(shapeArea);
		}
	}
	
	public void drawWithGraphicsScaling(Graphics2D g, CoordinateMapper cm){
		lx1 = cm.worldXtoLocalX(ta.getX());
		ly1 = cm.worldYtoLocalY(ta.getY());
		
		lx2 = cm.worldXtoLocalX(ta.getX2());
		ly2 = cm.worldYtoLocalY(ta.getY2());
		
		lw = lx2 - lx1;
		lh = ly2 - ly1;
		
		int ww = ta.getX2() - ta.getX();
		int wh = ta.getY2() - ta.getY();
		
		float transparency = ta.getTransparency();
		Composite originalComposite = g.getComposite();
		if(transparency != 1.0f){
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
		}

		Color color = ta.getColor();
		g.setColor(color);
		g.fillRect(lx1, ly1, lw, lh);
		
		TableData td = ta.getTableData();
		
		JLabel label = getJLabel(td, 0, 0);
		//label.setSize(ww, wh);
		Dimension labelSize = label.getPreferredSize();
		//labelSize.width += 5;
		label.setSize(labelSize);

		BufferedImage ghostImage = new BufferedImage(lw, lh, BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D g2 = (Graphics2D)ghostImage.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
			ANTIALIAS_TEXT ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		
		AffineTransform oldTransform = g2.getTransform();
		if(cm.getScale() != 1.0d){
			g2.setTransform(AffineTransform.getScaleInstance(cm.getScale(), cm.getScale()));
		}
		label.paint(g2);
		
		//AffineTransform oldTransform2 = g.getTransform();
		//if(cm.getScale() != 1.0d){
		//	g.setTransform(AffineTransform.getScaleInstance(cm.getScale(), cm.getScale()));
		//}
		
		g.drawImage(ghostImage, lx1, ly1, null);
		//g.setTransform(oldTransform2);
		
		if(transparency != 1.0f){
			g.setComposite(originalComposite);
		}

		/*
		if(!b.isSelected()){
			g.setColor(b.getTrimColor());
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
		else{
			MarqueeUtils.drawMarqueeRectangle(g, lx1, ly1, lw, lh, b.getOffset());
		}
		*/
		
		color = ta.getTrimColor();

		Point p = ta.getIndicatorPoint();
		if(p != null){
			Shape s = getIndicatorShape(cm);
			Area shapeArea = new Area(s);
			shapeArea.subtract(new Area(new Rectangle(lx1, ly1, lw, lh)));
			g.setPaint(color);
			//g.fill(s);
			g.fill(shapeArea);
		}
	}

	protected Shape getIndicatorShape(CoordinateMapper cm){
		Point p = ta.getIndicatorPoint();
		if(p == null) return null;
		
		int ix = (int)p.getX();
		int iy = (int)p.getY();
		
		int x1 = ta.getX();
		int x2 = ta.getX2();
		
		if(x1 > x2){
			int temp = x1;
			x1 = x2;
			x2 = temp;
		}
		
		int y1 = ta.getY();
		int y2 = ta.getY2();
		
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
		
		ulX = cm.worldXtoLocalX(ulX);
		urX = cm.worldXtoLocalX(urX);
		llX = cm.worldXtoLocalX(llX);
		lrX = cm.worldXtoLocalX(lrX);

		ulY = cm.worldYtoLocalY(ulY);
		urY = cm.worldYtoLocalY(urY);
		llY = cm.worldYtoLocalY(llY);
		lrY = cm.worldYtoLocalY(lrY);
		
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
		
		return((worldX >= ta.getX()) &&
			(worldX <= ta.getX2()) &&
			(worldY >= ta.getY()) &&
			(worldY <= ta.getY2()));
	}
	
	public Rectangle getLocalBoundingBox(Graphics2D g, CoordinateMapper cm){
		Rectangle localBoundingBox = BNAUtils.worldRectangleToLocalRectangle(cm, ta.getBoundingBox());
		Stroke stroke = ta.getStroke();
		
		//The box might be stroked with a thick stroke, so we have to figure the
		//bounding box for the stroked shape.
		if(stroke != null){
			Shape s = stroke.createStrokedShape(localBoundingBox);
			localBoundingBox = s.getBounds();
		}

		Point ip = ta.getIndicatorPoint();
		if(ip != null){
			int ipx = cm.worldXtoLocalX(ip.x);
			int ipy = cm.worldYtoLocalY(ip.y);
			localBoundingBox.add(ipx, ipy);
		}
		return new Rectangle(localBoundingBox);
	}
}

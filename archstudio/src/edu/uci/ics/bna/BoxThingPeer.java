package edu.uci.ics.bna;

import java.awt.*;
import java.awt.geom.*;

import edu.uci.ics.bna.thumbnail.Thumbnail;

public class BoxThingPeer extends ThingPeer{
	
	private BoxThing b;
	
	protected int lx1, ly1, lx2, ly2, lw, lh;
	
	public BoxThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof BoxThing)){
			throw new IllegalArgumentException("BoxThingPeer can only peer for BoxThing");
		}
		b = (BoxThing)t;
	}
	
	public Font getFont(){
		return BNAUtils.DEFAULT_FONT;
	}

	public static Rectangle getThumbnailRectangle(BoxThing b, CoordinateMapper cm){
		int lx1 = cm.worldXtoLocalX(b.getX());
		int ly1 = cm.worldYtoLocalY(b.getY());
		
		int lx2 = cm.worldXtoLocalX(b.getX2());
		int ly2 = cm.worldYtoLocalY(b.getY2());
		
		int lw = lx2 - lx1;
		int lh = ly2 - ly1;

		Thumbnail thumbnail = b.getThumbnail();
		if(thumbnail == null) return null;
		
		int thumbnailInset = b.getThumbnailInset();
		//Let's calculate the world bounds of the thumbnail in this world.
		Rectangle worldBox = new Rectangle(b.getBoundingBox());
		worldBox.x += thumbnailInset;
		worldBox.y += thumbnailInset;
		worldBox.width -= (thumbnailInset * 2);
		worldBox.height -= (thumbnailInset * 2);
			
		//Now let's see how big the thumbnail's world is
		Rectangle thumbnailWorldBounds = thumbnail.getModelBounds();
			
		//We want to make sure the thumbnail doesn't get bigger than
		//its own world bounds.
			
		if(worldBox.width > thumbnailWorldBounds.width){
			int dw = worldBox.width - thumbnailWorldBounds.width;
			worldBox.x += dw / 2;
			worldBox.width -= dw;
		}
		if(worldBox.height > thumbnailWorldBounds.height){
			int dh = worldBox.height - thumbnailWorldBounds.height;
			worldBox.y += dh / 2;
			worldBox.height -= dh;
		}
			
		//Now we want to convert that world box to a local box
		int wbx2 = worldBox.x + worldBox.width;
		int wby2 = worldBox.y + worldBox.height;
			
		int tx = cm.worldXtoLocalX(worldBox.x);
		int ty = cm.worldYtoLocalY(worldBox.y);
		int tw = cm.worldXtoLocalX(wbx2) - tx;
		int th = cm.worldYtoLocalY(wby2) - ty;

		Dimension d = thumbnail.getThumbnailDimensions(tx, ty, tw, th);
		if(d.width < tw){
			tx += (tw - d.width) / 2;
		}
		if(d.height < th){
			ty += (th - d.height) / 2;
		}

		return new Rectangle(tx, ty, tw, th);
	}

	private Rectangle tmpRect = new Rectangle();
	
	private Thumbnail currentThumbnail = null;
	private PeerCache myPeerCache = null;
	
	protected boolean isVisible(){
		return b.isVisible();
	}
	
	public void draw(Graphics2D g, CoordinateMapper cm){
		if(!isVisible())
			return;
		
		lx1 = cm.worldXtoLocalX(b.getX());
		ly1 = cm.worldYtoLocalY(b.getY());
		
		lx2 = cm.worldXtoLocalX(b.getX2());
		ly2 = cm.worldYtoLocalY(b.getY2());
		
		lw = lx2 - lx1;
		lh = ly2 - ly1;
		
		Color color = b.getColor();
		g.setColor(color);
		//g.fillRect(lx1, ly1, lw, lh);
		
		Composite originalComposite = g.getComposite();
		if(b.isGlassed()){
			g.setPaint(color);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, b.getGlassedTransparency()));
		}
		g.fill3DRect(lx1, ly1, lw, lh, true);
		
		if((bnaComponent != null) && (bnaComponent.shouldGradientGraphics())){
			tmpRect.x = lx1+1;
			tmpRect.y = ly1+1;
			tmpRect.width = lw-2;
			tmpRect.height = lh-2;

			Paint oldPaint = g.getPaint();
			g.setPaint(new GradientPaint(lx1+1, ly1+1, color, lx1+1 + lw-2, ly1 + 1 + lh-2, color.darker()));
			g.fill(tmpRect);
			g.setPaint(oldPaint);
		}
		
		Thumbnail originalThumbnail = b.getThumbnail();
		
		Thumbnail thumbnail = null;
		if(originalThumbnail != null){
			//The creation of the new thumbnail here
			//with the copy of the model and this peer's peer cache
			//is to support infinite recursion of thumbnails. Yes,
			//this is indeed the Wrongest Thing Evar[TM], but it
			//actually works.  Infinite recursion is stopped when
			//the thumbnail gets too small to see.
			
			if(currentThumbnail != originalThumbnail){
				currentThumbnail = originalThumbnail;
				myPeerCache = new PeerCache(bnaComponent);
			}

			thumbnail = new Thumbnail(bnaComponent, originalThumbnail.getModel());
			thumbnail.setPeerCache(myPeerCache);
		}
		else{
			currentThumbnail = null;
			myPeerCache = null;
		}
		
		if(thumbnail != null){
			int thumbnailInset = b.getThumbnailInset();
			//Let's calculate the world bounds of the thumbnail in this world.
			Rectangle worldBox = new Rectangle(b.getBoundingBox());
			worldBox.x += thumbnailInset;
			worldBox.y += thumbnailInset;
			worldBox.width -= (thumbnailInset * 2);
			worldBox.height -= (thumbnailInset * 2);
			
			//Now let's see how big the thumbnail's world is
			Rectangle thumbnailWorldBounds = thumbnail.getModelBounds();
			
			//We want to make sure the thumbnail doesn't get bigger than
			//its own world bounds.
			
			if(worldBox.width > thumbnailWorldBounds.width){
				int dw = worldBox.width - thumbnailWorldBounds.width;
				worldBox.x += dw / 2;
				worldBox.width -= dw;
			}
			if(worldBox.height > thumbnailWorldBounds.height){
				int dh = worldBox.height - thumbnailWorldBounds.height;
				worldBox.y += dh / 2;
				worldBox.height -= dh;
			}
			
			//Now we want to convert that world box to a local box
			int wbx2 = worldBox.x + worldBox.width;
			int wby2 = worldBox.y + worldBox.height;
			
			int tx = cm.worldXtoLocalX(worldBox.x);
			int ty = cm.worldYtoLocalY(worldBox.y);
			int tw = cm.worldXtoLocalX(wbx2) - tx;
			int th = cm.worldYtoLocalY(wby2) - ty;
			
			/*
			int horizontalThumbnailInset = BNAUtils.round((double)b.getThumbnailInset() * cm.getScale());
			int verticalThumbnailInset = BNAUtils.round((double)b.getThumbnailInset() * cm.getScale());

			int tx = lx1 + horizontalThumbnailInset;
			int ty = ly1 + verticalThumbnailInset;
			int tw = lw - horizontalThumbnailInset - horizontalThumbnailInset;
			int th = lh - verticalThumbnailInset - verticalThumbnailInset;
			*/
			
			if((tw > 0) && (th > 0)){
				//Center the thumbnail in the box
				Dimension d = thumbnail.getThumbnailDimensions(tx, ty, tw, th);
				if(d.width < tw){
					tx += (tw - d.width) / 2;
				}
				if(d.height < th){
					ty += (th - d.height) / 2;
				}
				//Draw the thumbnail
				thumbnail.drawThumbnail(g, tx, ty, tw, th);
				Composite pc = g.getComposite();
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.40f));
				g.fill3DRect(lx1, ly1, lw, lh, true);
				g.setComposite(pc);
			}
		}
		
		if(b.isGlassed()){
			if(originalComposite != null){
				g.setComposite(originalComposite);
			}
		}

		Color trimColor = b.getTrimColor();
		if(!b.isSelected()){
			g.setColor(trimColor);
			Stroke newStroke = b.getStroke();
			Stroke oldStroke = null;
			if(newStroke != null){
				oldStroke = g.getStroke();
				g.setStroke(newStroke);
			}
			g.drawRect(lx1, ly1, lw, lh);
			if(b.getDoubleBorder() && (lw > 4)){
				g.drawRect(lx1 + 2, ly1 + 2, lw - 4, lh - 4);
			}
			if(newStroke != null){
				g.setStroke(oldStroke);
			}
		}
		else{
			MarqueeUtils.drawMarqueeRectangle(g, lx1, ly1, lw, lh, b.getOffset());
			if(b.getDoubleBorder() && (lw > 4)){
				g.setColor(trimColor);
				Stroke newStroke = b.getStroke();
				Stroke oldStroke = null;
				if(newStroke != null){
					oldStroke = g.getStroke();
					g.setStroke(newStroke);
				}
				g.drawRect(lx1 + 2, ly1 + 2, lw - 4, lh - 4);
				if(newStroke != null){
					g.setStroke(oldStroke);
				}
			}
		}
		
		String label = b.getLabel();
		boolean wrapped = b.getWrapLabel();
		if(label != null){
			g.setColor(b.getTextColor());
			boolean dontIncrease = true;
			boolean keepWordsIntact = true;
			boolean wrapAtEOL = true;
			if(!wrapped){
				int inset = lw / 20;
				if(b.getDoubleBorder() && (lw > 4)){
					inset += 2;
				}
				Rectangle fontBoundRect = new Rectangle(lx1 + inset, ly1 + inset, lw - inset - inset, lh - inset - inset);
				/* If there's a thumbnail it's probably going to interact poorly (graphically)
				 * with the label; so we can draw a 'buffer' pixel in the background color
				 * around each side of the label to make it much more readable without
				 * damaging the thumbnail too badly or making the label look crappy. */ 
				if(thumbnail != null){
					g.setColor(b.getColor());
					fontBoundRect.x--;
					BNAUtils.renderBoundedString(getFont(), label, g, fontBoundRect, BNAUtils.VALIGN_TOP, dontIncrease);
					fontBoundRect.x++;
					fontBoundRect.y--;
					BNAUtils.renderBoundedString(getFont(), label, g, fontBoundRect, BNAUtils.VALIGN_TOP, dontIncrease);
					fontBoundRect.y++;
					fontBoundRect.x++;
					BNAUtils.renderBoundedString(getFont(), label, g, fontBoundRect, BNAUtils.VALIGN_TOP, dontIncrease);
					fontBoundRect.x--;
					fontBoundRect.y++;
					BNAUtils.renderBoundedString(getFont(), label, g, fontBoundRect, BNAUtils.VALIGN_TOP, dontIncrease);
					g.setColor(b.getTextColor());
					fontBoundRect.y--;
					BNAUtils.renderBoundedString(getFont(), label, g, fontBoundRect, BNAUtils.VALIGN_TOP, dontIncrease);
				}
				else{
					BNAUtils.renderBoundedString(getFont(), label, g, fontBoundRect, BNAUtils.VALIGN_MIDDLE, dontIncrease);
				}
			}
			else {
				int inset = 1;
				if(b.getDoubleBorder() && (lw > 4)){
					inset += 2;
				}
				Rectangle fontBoundRect = new Rectangle(lx1 + inset, ly1 + inset, lw - inset - inset, lh - inset - inset);
				if(thumbnail != null){
					BNAUtils.renderWrappedBoundedString(getFont(), label, g, b.getColor(), b.getTextColor(), fontBoundRect, BNAUtils.VALIGN_TOP, dontIncrease, keepWordsIntact, wrapAtEOL);
				}
				else
					BNAUtils.renderWrappedBoundedString(getFont(), label, g, null, b.getTextColor(), fontBoundRect, BNAUtils.VALIGN_MIDDLE, dontIncrease, keepWordsIntact, wrapAtEOL);
			}
		}
		
		Point p = b.getIndicatorPoint();
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
		Point p = b.getIndicatorPoint();
		if(p == null) return null;
		
		int ix = (int)p.getX();
		int iy = (int)p.getY();
		
		int x1 = b.getX();
		int x2 = b.getX2();
		
		if(x1 > x2){
			int temp = x1;
			x1 = x2;
			x2 = temp;
		}
		
		int y1 = b.getY();
		int y2 = b.getY2();
		
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
		if(!isVisible())
			return false;
		
		//		System.out.println("Checking " + worldX + ", " + worldY);
		//		System.out.println("BoxThing " + b.getX1() + ", " + b.getY1() + ", " + b.getX2() + ", " + b.getY2());
		
		return((worldX >= b.getX()) &&
			(worldX <= b.getX2()) &&
			(worldY >= b.getY()) &&
			(worldY <= b.getY2()));
	}
	
	public Rectangle getLocalBoundingBox(Graphics2D g, CoordinateMapper cm){
		if(!isVisible())
			return new Rectangle();
		
		Rectangle localBoundingBox = BNAUtils.worldRectangleToLocalRectangle(cm, b.getBoundingBox());
		Stroke stroke = b.getStroke();
		
		//The box might be stroked with a thick stroke, so we have to figure the
		//bounding box for the stroked shape.
		if(stroke != null){
			Shape s = stroke.createStrokedShape(localBoundingBox);
			localBoundingBox = s.getBounds();
		}

		Point ip = b.getIndicatorPoint();
		if(ip != null){
			int ipx = cm.worldXtoLocalX(ip.x);
			int ipy = cm.worldYtoLocalY(ip.y);
			localBoundingBox.add(ipx, ipy);
		}
		return new Rectangle(localBoundingBox);
	}
}

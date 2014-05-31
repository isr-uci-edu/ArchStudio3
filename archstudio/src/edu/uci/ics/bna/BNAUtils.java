package edu.uci.ics.bna;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.text.AttributedString;
import javax.swing.*;

import edu.uci.ics.widgets.WidgetUtils;

public class BNAUtils{
	
	public static final Font DEFAULT_FONT = getDefaultFont();
	
	public static final int VALIGN_TOP = 100;
	public static final int VALIGN_MIDDLE = 200;
	public static final int VALIGN_BOTTOM = 300;
	
	public static final int STACKING_PRIORITY_ALWAYS_ON_TOP = 1001;
	public static final int STACKING_PRIORITY_MIDDLE = 1002;
	public static final int STACKING_PRIORITY_ALWAYS_ON_BOTTOM = 1003;
	
	public static final Rectangle NONEXISTENT_RECTANGLE = new Rectangle(Integer.MIN_VALUE, Integer.MIN_VALUE, 0, 0);

	private static Font getDefaultFont(){
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] availableFamilies = env.getAvailableFontFamilyNames();
		for(int i = 0; i < availableFamilies.length; i++){
			if(availableFamilies[i].equals("Arial")){
				return new Font("Arial", Font.PLAIN, 12);
			}
		}
		return new Font("SansSerif", Font.PLAIN, 12);
	}
	
	public static int round(double d){
		return (int)Math.round(d);
	}
	
	public static boolean wasControlPressed(MouseEvent evt){
		int mods = evt.getModifiersEx();
		return ((mods & InputEvent.CTRL_DOWN_MASK) != 0) ||
			((mods & InputEvent.CTRL_MASK) != 0);
	}
	
	public static boolean wasShiftPressed(MouseEvent evt){
		int mods = evt.getModifiersEx();
		return ((mods & InputEvent.SHIFT_DOWN_MASK) != 0) ||
			((mods & InputEvent.SHIFT_MASK) != 0);
	}		
	
	public static void setStackingPriority(Thing t, int stackingPriority){
		t.setProperty("$stackingPriority", stackingPriority);
	}
	
	public static int getStackingPriority(Thing t){
		try{
			return t.getIntProperty("$stackingPriority");
		}
		catch(Exception e){
			return STACKING_PRIORITY_MIDDLE;
		}
	}
	/*
	public static void renderBoundedString(Font baseFont, String s, Graphics2D g, int x, int y, int desiredWidth){
		FontRenderContext frc = g.getFontRenderContext();
		Rectangle2D stringBounds = baseFont.getStringBounds(s, frc);
		int origWidth = (int)stringBounds.getWidth();
		double scale = ((double)desiredWidth) / ((double)origWidth);
		AffineTransform scalar = AffineTransform.getScaleInstance(scale, scale);
		Font derivedFont = baseFont.deriveFont(scalar);
		g.setFont(derivedFont);
		g.drawString(s, x, y);
	}
	*/
	
	//private static final int APPLE_WORKAROUND_NO = 1;
	//private static final int APPLE_WORKAROUND_YES = 2;
	
	//private static int appleWorkaroundStatus;

	/*
	static{
		appleWorkaroundStatus = APPLE_WORKAROUND_NO;
		String vendor = System.getProperty("java.vendor");
		if (vendor.equals("Apple Computer, Inc.")) {
			String mrjVersion = System.getProperty("mrj.version");
			if (mrjVersion != null){
				try {
					double d = Double.parseDouble(mrjVersion);
					if (d <= 117.1) {
						appleWorkaroundStatus = APPLE_WORKAROUND_YES;
					}
				}
				catch (NumberFormatException nfe) {}
			}
		}
	}
	*/
	
	public static void renderBoundedString(Font baseFont, String s, Graphics2D g, java.awt.Rectangle bounds, int valign, boolean dontIncrease){
		FontRenderContext frc = g.getFontRenderContext();
		Rectangle2D stringBounds = baseFont.getStringBounds(s, frc);
		int origWidth = (int)stringBounds.getWidth();
		double scale = ((double)bounds.getWidth()) / ((double)origWidth);
		scale -= 0.02d;
		
		Font derivedFont;
		if((dontIncrease) && (scale > 1.0d)){
			derivedFont = baseFont;
		}
		else{
			//AffineTransform scalar = AffineTransform.getScaleInstance(scale, scale);

			derivedFont = baseFont.deriveFont((float)scale * baseFont.getSize2D());

			//The following block was in use when we were working around an Apple MRJ
			//bug by using a different call to deriveFont(), but since Apple hasn't
			//gotten around to fixing it and it's hard to tell whether the bug exists
			//or not we're just gonna use the alternate call all the time.
			
			//if(appleWorkaroundStatus == APPLE_WORKAROUND_YES){
			//	derivedFont = baseFont.deriveFont((float)scale * baseFont.getSize2D());
			//}
			//else{
			//	derivedFont = baseFont.deriveFont(scalar);
			//}
		}
		
		Rectangle2D newStringBounds = derivedFont.getStringBounds(s, frc);
		LineMetrics newStringLineMetrics = derivedFont.getLineMetrics(s, frc);
		int ascent = (int)newStringLineMetrics.getAscent();
		
		int newHeight = (int)newStringBounds.getHeight();
		
		int boundsHeight = (int)bounds.getHeight();
		int y = 0;
		if(valign == VALIGN_TOP){
			y = (ascent) + (int)bounds.getY();
		}
		else if(valign == VALIGN_MIDDLE){
			y = (boundsHeight / 2) + (ascent / 2) + (int)bounds.getY();
		}
		else if(valign == VALIGN_BOTTOM){
			y = boundsHeight - ascent + (int)bounds.getY();
		}
		else throw new IllegalArgumentException("Illegal vertical allignment");
		
		int x = (int)bounds.getX();
		
		if((dontIncrease) && (scale > 1.0d)){
			int newWidth = (int)newStringBounds.getWidth();
			int rectWidth = (int)bounds.getWidth();
			x += ((rectWidth / 2) - (newWidth / 2));
		}
		
		g.setFont(derivedFont);
		g.drawString(s, x, y);
	}
	
	public static void renderBoundedString(Font baseFont, String s, Graphics2D g, java.awt.Rectangle bounds, int valign){
		renderBoundedString(baseFont, s, g, bounds, valign, false);
	}
	
	// Return a Point2D, rather than a Dimension2D because 
	// Dimension2D doesn't have a float implementation
	private static float getHeight(String string, LineBreakMeasurer measurer, int offsetLimit, boolean requireNextWord, boolean wrapAtEOL, float maxWidth, float maxHeight, float epsilon){
		if(maxWidth <= epsilon || maxHeight <= epsilon)
			return -1;
		
		float height = 0;
		measurer.setPosition(0);
		while (measurer.getPosition() < offsetLimit) {
			TextLayout layout;
			if(wrapAtEOL)
				layout = nextLayoutEOL(string, measurer, maxWidth, offsetLimit, requireNextWord);
			else
				layout = measurer.nextLayout(maxWidth, offsetLimit, requireNextWord);
				
			if(layout == null)
				return -1;
			if(layout.getVisibleAdvance() > maxWidth)
				return -1;
			height += layout.getAscent() + layout.getDescent();
			if(height >= maxHeight)
				return -1;
			if(measurer.getPosition() < offsetLimit)
				height += layout.getLeading();
		}
		return height;
	}
	
	private static TextLayout nextLayoutEOL(String string, LineBreakMeasurer measurer, float maxWidth, int offsetLimit, boolean requireNextWord){
		int offsetLimitEOL;
		try{
			offsetLimitEOL = string.indexOf('\n', measurer.getPosition());
			if(offsetLimitEOL < 0)
				offsetLimitEOL = offsetLimit;
			else{
				offsetLimitEOL ++;
				if(offsetLimitEOL > offsetLimit)
					offsetLimitEOL = offsetLimit;
				else if(offsetLimitEOL == offsetLimit)
					offsetLimitEOL = offsetLimit  - 1;
			}
			if(offsetLimitEOL > measurer.getPosition())
				return measurer.nextLayout(maxWidth, offsetLimitEOL, requireNextWord);
			else
				return measurer.nextLayout(maxWidth, offsetLimit, requireNextWord);
		}catch(Exception e){
			System.err.println(e);
			return measurer.nextLayout(maxWidth, offsetLimit, requireNextWord);
		}
	}
	
	public static void renderWrappedBoundedString(Font baseFont, String s, Graphics2D g, Color edge, Color color, java.awt.Rectangle bounds, int valign, boolean dontIncrease, boolean keepWordsIntact, boolean wrapAtEOL){
		try{
			
			if(s.length() == 0)
				return;
			
			final float epsilon = 0.05f;
			
			FontRenderContext frc = g.getFontRenderContext();
			AttributedString as = new AttributedString(s);
			LineBreakMeasurer measurer = new LineBreakMeasurer(as.getIterator(), frc);
			
			float minScale = 0.0f; // should always fit
			float maxScale = 1.0f; // may be too big
			
			// it not increasing, just check that a scale of 1 works
			if(dontIncrease){
				float scale = 1f;
				Font derivedFont = baseFont.deriveFont(baseFont.getSize2D() * scale);
				as.addAttribute(TextAttribute.FONT, derivedFont);
				measurer = new LineBreakMeasurer(as.getIterator(), frc);
				float height = getHeight(s, measurer, s.length(), keepWordsIntact, wrapAtEOL, bounds.width, bounds.height, epsilon); 
				if(height >= 0)
					minScale = maxScale = 1f; 
			}
			
			// if increasing, look for a maxScale that's too big
			else {
				while(true){
					float scale = maxScale;
					Font derivedFont = baseFont.deriveFont(baseFont.getSize2D() * scale);
					as.addAttribute(TextAttribute.FONT, derivedFont);
					measurer = new LineBreakMeasurer(as.getIterator(), frc);
					float height = getHeight(s, measurer, s.length(), keepWordsIntact, wrapAtEOL, bounds.width, bounds.height, epsilon); 
					if(height < 0){
						break;
					}
					else{
						minScale = maxScale;
						maxScale *= 3f;
					}
				}
			}
			
			// now, do a binary search within the remaining range
			while(maxScale - minScale >= epsilon){
				
				// check that the minScale is not too big
				if(dontIncrease && minScale >= 1f)
					break;
				
				float scale = (minScale + maxScale) / 2;
				
				// check that the scale is not too small
				if(scale <= epsilon)
					break;
				
				Font derivedFont = baseFont.deriveFont(baseFont.getSize2D() * scale);
				as.addAttribute(TextAttribute.FONT, derivedFont);
				measurer = new LineBreakMeasurer(as.getIterator(), frc);
				float height = getHeight(s, measurer, s.length(), keepWordsIntact, wrapAtEOL, bounds.width, bounds.height, epsilon); 
				
				if(height < 0){
					maxScale = scale;
				}
				else{
					minScale = scale;
				}
			}
			
			float scale = minScale; // = (minScale + maxScale) / 2 - epsilon;
			if(dontIncrease && scale > 1f)
				scale = 1f;
			if(scale < epsilon)
				return;
			
			Font derivedFont = baseFont.deriveFont(baseFont.getSize2D() * scale);
			as.addAttribute(TextAttribute.FONT, derivedFont);
			measurer = new LineBreakMeasurer(as.getIterator(), frc);
			float height = getHeight(s, measurer, s.length(), keepWordsIntact, wrapAtEOL, bounds.width, bounds.height, epsilon); 

			if(height >= 0){
				float x = bounds.x;
				float y = bounds.y;
				switch(valign){
				default:
				case VALIGN_TOP:
					break;
				case VALIGN_MIDDLE:
					y += ((float)bounds.height - height) / 2f;
					break;
				case VALIGN_BOTTOM:
					y += (float)bounds.height - height;
					break;
				}
				
				int step = edge != null ? 0 : 5;
				while(true){
					switch(step++){
					case 0:
						g.setColor(edge);
						y -= 1;
						break;
					case 1:
						x += 1;
						y += 1;
						break;
					case 2:
						x -= 1;
						y += 1;
						break;
					case 3:
						x -= 1;
						y -= 1;
						break;
					case 4:
						x += 1;
						continue;
					case 5:
						g.setColor(color);
						break;
					case 6:
						return;
					}
					
					float ya = y;
					measurer.setPosition(0);
					while (measurer.getPosition() < s.length()) {
						TextLayout layout;
						if(wrapAtEOL)
							layout = nextLayoutEOL(s, measurer, bounds.width, s.length(), keepWordsIntact);
						else
							layout = measurer.nextLayout(bounds.width, s.length(), keepWordsIntact);
						ya += layout.getAscent();
						float xa = x + ((float)bounds.width - layout.getVisibleAdvance()) / 2f; 
						layout.draw(g, xa, ya);
						ya += layout.getDescent() + layout.getLeading();
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			return;
		}
	}
	
	public static Rectangle normalizeRectangle(Rectangle r){
		Rectangle normalizedRect = new Rectangle();
		normalizedRect.x = r.x;
		normalizedRect.y = r.y;
		
		if(r.width >= 0){
			normalizedRect.width = r.width;
		}
		else{
			normalizedRect.width = -r.width;
			normalizedRect.x = r.x + r.width;
		}
		
		if(r.height >= 0){
			normalizedRect.height = r.height;
		}
		else{
			normalizedRect.height = -r.height;
			normalizedRect.y = r.y + r.height;
		}
		return normalizedRect;
	}
	
	public static boolean isWithin(Rectangle outsideRect, int x, int y){
		Rectangle out = normalizeRectangle(outsideRect);
		int x1 = out.x;
		int x2 = out.x + out.width;
		int y1 = out.y;
		int y2 = out.y + out.height;
		
		return
			(x >= x1) &&
			(x <= x2) &&
			(y >= y1) &&
			(y <= y2);
	}
	
	public static boolean isWithin(Rectangle outsideRect, Rectangle insideRect){
		Rectangle in = normalizeRectangle(insideRect);
		
		return 
			isWithin(outsideRect, in.x, in.y) &&
			isWithin(outsideRect, in.x + in.width, in.y) &&
			isWithin(outsideRect, in.x, in.y + in.height) &&
			isWithin(outsideRect, in.x + in.width, in.y + in.height);
	}
	
	public static Point scaleAndMoveBorderPoint(Point p, Rectangle oldRect, Rectangle newRect){
		int ox1 = oldRect.x;
		int ox2 = oldRect.x + oldRect.width;
		int oy1 = oldRect.y;
		int oy2 = oldRect.y + oldRect.height;
		int ow = oldRect.width;
		int oh = oldRect.height;
		
		int nx1 = newRect.x;
		int nx2 = newRect.x + newRect.width;
		int ny1 = newRect.y;
		int ny2 = newRect.y + newRect.height;
		int nw = newRect.width;
		int nh = newRect.height;
		
		int dw = nw - ow;
		int dh = nh - oh;
		
		double sx = (double)nw / (double)ow;
		double sy = (double)nh / (double)oh;
		
		int dx = nx1 - ox1;
		int dy = ny1 - oy1;
		
		Point p2 = new Point(p.x, p.y);
		
		if(p.y == oldRect.y){
			//It's on the top rail
			
			//Keep it on the top rail
			p2.y = newRect.y;
			
			//Old distance from the left rail
			int dist = p.x - oldRect.x;
			
			if(dw != 0){
				//Scale that distance
				dist = BNAUtils.round((double)dist * sx);
			}
			
			//Also perform translation
			p2.x = newRect.x + dist;
		}
		else if((p.y == (oldRect.y + oldRect.height - 1)) ||
			(p.y == (oldRect.y + oldRect.height))){
			//It's on the bottom rail
			
			//Keep it on the bottom rail
			p2.y = newRect.y + newRect.height - 1;
			
			//Old distance from the left rail
			int dist = p.x - oldRect.x;
			
			if(dw != 0){
				//Scale that distance
				dist = BNAUtils.round((double)dist * sx);
			}
			
			//Also perform translation
			p2.x = newRect.x + dist;
		}
		else if(p.x == oldRect.x){
			//It's on the left rail
			
			//Keep it on the left rail
			p2.x = newRect.x;
			
			//Old distance from the top rail
			int dist = p.y - oldRect.y;
			
			if(dh != 0){
				//Scale that distance
				dist = BNAUtils.round((double)dist * sy);
			}
			
			//Also perform translation
			p2.y = newRect.y + dist;
		}
		else if((p.x == (oldRect.x + oldRect.width - 1)) ||
			(p.x == (oldRect.x + oldRect.width))){
			//It's on the right rail
			
			//Keep it on the right rail
			p2.x = newRect.x + newRect.width - 1;
			
			//Old distance from the top rail
			int dist = p.y - oldRect.y;
			
			if(dh != 0){
				//Scale that distance
				dist = BNAUtils.round((double)dist * sy);
			}
			
			//Also perform translation
			p2.y = newRect.y + dist;
		}
		
		//Normalize
		if(p2.x < newRect.x){
			p2.x = newRect.x;
		}
		if(p2.x >= (newRect.x + newRect.width)){
			p2.x = newRect.x + newRect.width - 1;
		}
		if(p2.y < newRect.y){
			p2.y = newRect.y;
		}
		if(p2.y >= (newRect.y + newRect.height)){
			p2.y = newRect.y + newRect.height - 1;
		}
		
		return p2;
	}
	
	public static void showUserNotificationUL(BNAComponent c, String notification){
		showUserNotificationUL(c.getModel(), notification);
	}

	public static void showUserNotificationUL(BNAModel m, String notification){
		UserNotificationThing unt = new UserNotificationThing();
		unt.setLabel(notification);
		unt.setInitialPointLocal(new Point(5, 5));
			
		BNAUtils.setStackingPriority(unt, BNAUtils.STACKING_PRIORITY_ALWAYS_ON_TOP);
		m.addThing(unt);
	}
	
	public static Rectangle worldRectangleToLocalRectangle(CoordinateMapper cm, Rectangle worldRectangle){
		Rectangle localRectangle = new Rectangle();
		localRectangle.x = cm.worldXtoLocalX(worldRectangle.x);
		localRectangle.y = cm.worldYtoLocalY(worldRectangle.y);
		int lx2 = cm.worldXtoLocalX(worldRectangle.x + worldRectangle.width);
		int ly2 = cm.worldYtoLocalY(worldRectangle.y + worldRectangle.height);
		localRectangle.width = lx2 - localRectangle.x;
		localRectangle.height = ly2 - localRectangle.y;
		return localRectangle;
	}
	
	
	public static EnvironmentPropertiesThing getEnvironmentPropertiesThing(BNAModel m){
		Thing t = m.getThing("$$environmentProperties");
		if(t == null){
			return null;
		}
		else if(t instanceof EnvironmentPropertiesThing){
			return (EnvironmentPropertiesThing)t;
		}
		else{
			throw new RuntimeException("Environment properties class is wrong.");
		}
	}

	public void setEnvironmentProperty(BNAModel m, String propertyName, Object propertyValue){
		boolean foundEpt = true;
		EnvironmentPropertiesThing ept = getEnvironmentPropertiesThing(m);
		if(ept == null){
			foundEpt = false;
			ept = new EnvironmentPropertiesThing();
		}
		ept.setProperty(propertyName, propertyValue);
		if(!foundEpt){
			m.addThing(ept);
		}
	}
	
	public Object getEnvironmentProperty(BNAModel m, String propertyName){
		EnvironmentPropertiesThing ept = getEnvironmentPropertiesThing(m);
		if(ept == null){
			return null;
		}
		else{
			return ept.getProperty(propertyName);
		}
	}
	
	
	private static float[] deg2rad = null;
	public static float degreesToRadians(int degrees){
		while(degrees < 0) degrees += 360;
		degrees = degrees % 360;
		if(deg2rad == null){
			deg2rad = new float[360];
			for(int i = 0; i < 360; i++){
				deg2rad[i] = i * ((float)Math.PI / 180f);
			}
		}
		return deg2rad[degrees];
	}
	
	public static Point[] getAllPoints(Shape s){
		java.util.List list = new java.util.ArrayList(20);
		double[] pt = new double[6];
		PathIterator it = s.getPathIterator(null);
		while(!it.isDone()){
			int segType = it.currentSegment(pt);
			if(segType != PathIterator.SEG_CLOSE){
				int x = (int)pt[0];
				int y = (int)pt[1];
				list.add(new Point(x, y));
			}
			it.next();
		}
		return (Point[])list.toArray(new Point[0]);
	}
	
	public static Point[] getExtremePoints(Shape s){
		Point minxpoint = new Point();
		int minx = Integer.MAX_VALUE;
		Point maxxpoint = new Point();
		int maxx = Integer.MIN_VALUE;
		Point minypoint = new Point();
		int miny = Integer.MAX_VALUE;
		Point maxypoint = new Point();
		int maxy = Integer.MIN_VALUE;
		
		double[] pt = new double[6];
		PathIterator it = s.getPathIterator(null);
		while(!it.isDone()){
			int segType = it.currentSegment(pt);
			if(segType != PathIterator.SEG_CLOSE){
				int x = (int)pt[0];
				int y = (int)pt[1];
				if(x < minx){ minxpoint.x = x; minxpoint.y = y; minx = x; }
				if(x > maxx){ maxxpoint.x = x; maxxpoint.y = y; maxx = x; }
				if(y < miny){ minypoint.x = x; minypoint.y = y; miny = y; }
				if(y > maxy){ maxypoint.x = x; maxypoint.y = y; maxy = y; }
			}
			it.next();
		}
		return new Point[]{minxpoint, maxxpoint, minypoint, maxypoint};
	}
	
	/**
	 * Determines if two objects are equal.  Two <code>null</code>
	 * objects are considered to be equal.  If only one of the
	 * objects is non-<code>null</code>, they are not equal.
	 * If both are non-null, then this returns object1.equals(object2);
	 * @param object1 First object to compare.
	 * @param object2 Second object to compare.
	 * @return <code>true</code> or <code>false</code> according
	 * to the above description.
	 */
	public static boolean objNullEq(Object object1, Object object2){
		if((object1 == null) && (object2 == null)){
			return true;
		}
		if((object1 == null) && (object2 != null)){
			return false;
		}
		if((object1 != null) && (object2 == null)){
			return false;
		}
		return object1.equals(object2);
	}
	
	/**
	 * This calls <code>draw</code> on a Thing's peer as if it
	 * were being drawn on the given BNAComponent, however it draws
	 * it with an impossible clip so no such drawing actually occurs.
	 * This is useful in limited circumstances; for example, when the
	 * peer sets some property on the component (e.g. if the bounding box
	 * of the component is set by the peer when drawing because it cannot
	 * be determined by the data in the Thing alone).
	 *  
	 * @param c The BNAComponent on which this Thing might be drawn
	 * @param t The Thing to fake-draw
	 */
	public static void fakeDraw(BNAComponent c, Thing t){
		if(c == null) return;
		ThingPeer tp = c.getPeer(t);
		if(tp != null){
			Graphics2D g2d = (Graphics2D)c.getGraphics().create(
				NONEXISTENT_RECTANGLE.x, NONEXISTENT_RECTANGLE.y, 
				NONEXISTENT_RECTANGLE.width, NONEXISTENT_RECTANGLE.height);
			tp.draw(g2d, c.getCoordinateMapper());
		}
	}
	
}

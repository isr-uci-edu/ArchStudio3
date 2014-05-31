package edu.uci.ics.bna;

import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.SwingUtilities;

import edu.uci.ics.widgets.Colors;
import edu.uci.ics.widgets.WidgetUtils;

public class UserNotificationThingPeer extends ThingPeer{
	
	private UserNotificationThing unt;
	
	protected int lx1, ly1, lx2, ly2;
	
	public UserNotificationThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof UserNotificationThing)){
			throw new IllegalArgumentException("UserNotificationThingPeer can only peer for UserNotificationThing");
		}
		unt = (UserNotificationThing)t;
	}

	public void draw(Graphics2D g, CoordinateMapper cm){
		String label = unt.getLabel();
		if(label == null){
			return;
		}
		
		StringTokenizer st = new StringTokenizer(label, "\n");
		int numLines = st.countTokens();
		String[] lines = new String[numLines];
		for(int i = 0; i < numLines; i++){
			lines[i] = st.nextToken();
		}
		
		Font f = WidgetUtils.SANSSERIF_PLAIN_MEDIUM_FONT;
		Rectangle2D[] bounds = new Rectangle2D[numLines];
		for(int i = 0; i < numLines; i++){
			bounds[i] = f.getStringBounds(lines[i], g.getFontRenderContext());
		}
		
		int sumOfHeights = 0;
		int maxWidth = -1;
		for(int i = 0; i < numLines; i++){
			sumOfHeights += bounds[i].getHeight() + 1;
			int width = (int)bounds[i].getWidth();
			if(width > maxWidth){
				maxWidth = width;
			}
		}
		
		int cWidth = bnaComponent.getWidth();
		int cHeight = bnaComponent.getHeight();
		
		int boxX = cWidth - maxWidth - 6;
		int boxY = cHeight - sumOfHeights - (numLines * 2) - 2;
		int boxWidth = cWidth - boxX;
		int boxHeight = cHeight - boxY;
		
		Point overridingInitialPoint = unt.getInitialPointLocal();
		if(overridingInitialPoint != null){
			boxX = overridingInitialPoint.x;
			boxY = overridingInitialPoint.y;
		}
		
		Paint originalPaint = g.getPaint();
		Stroke originalStroke = g.getStroke();		
		Composite originalComposite = g.getComposite();

		Color color = Colors.PALE_WEAK_BLUE;
		g.setColor(color);
		g.setPaint(color);
		
		float transparency = unt.getEphemeralTransparency();
		if(transparency > 1.0){
			transparency = 1.0f + (1.0f - transparency);
		}
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
		
		g.fill3DRect(boxX, boxY, boxWidth, boxHeight, true);

		color = Color.BLACK;
		g.setColor(color);
		g.setPaint(color);

		g.setFont(f);
		int drawY = boxY + (int)bounds[0].getHeight() + 2;
		for(int i = 0; i < numLines; i++){
			g.drawString(lines[i], boxX + 2, drawY);
			if(i < (bounds.length - 1)){
				drawY += bounds[i+1].getHeight() + 2;
			}
		}
		
		g.setComposite(originalComposite);
		g.setStroke(originalStroke);
		g.setPaint(originalPaint);
	}

	public boolean isInThing(Graphics2D g, CoordinateMapper cm, int worldX, int worldY){
		return false;
	}
	
	public Rectangle getLocalBoundingBox(Graphics2D g, CoordinateMapper cm) {
		String label = unt.getLabel();
		if(label == null){
			return BNAUtils.NONEXISTENT_RECTANGLE;
		}
		
		if(g == null){
			return null;
		}
		
		StringTokenizer st = new StringTokenizer(label, "\n");
		int numLines = st.countTokens();
		String[] lines = new String[numLines];
		for(int i = 0; i < numLines; i++){
			lines[i] = st.nextToken();
		}
		
		Font f = WidgetUtils.SANSSERIF_PLAIN_MEDIUM_FONT;
		Rectangle2D[] bounds = new Rectangle2D[numLines];
		for(int i = 0; i < numLines; i++){
			bounds[i] = f.getStringBounds(lines[i], g.getFontRenderContext());
		}
		
		int sumOfHeights = 0;
		int maxWidth = -1;
		for(int i = 0; i < numLines; i++){
			sumOfHeights += bounds[i].getHeight() + 1;
			int width = (int)bounds[i].getWidth();
			if(width > maxWidth){
				maxWidth = width;
			}
		}
		
		int cWidth = bnaComponent.getWidth();
		int cHeight = bnaComponent.getHeight();
		
		int boxX = cWidth - maxWidth - 6;
		int boxY = cHeight - sumOfHeights - (numLines * 2) - 2;
		int boxWidth = cWidth - boxX;
		int boxHeight = cHeight - boxY;
		
		Point overridingInitialPoint = unt.getInitialPointLocal();
		if(overridingInitialPoint != null){
			boxX = overridingInitialPoint.x;
			boxY = overridingInitialPoint.y;
		}
		
		return new Rectangle(boxX, boxY, boxWidth, boxHeight);
	}
}

package edu.uci.ics.bna;

import java.awt.*;
import java.awt.geom.*;

import edu.uci.ics.widgets.WidgetUtils;

public class MarqueeUtils{

	public static double lineLen (Point Point1, Point Point2) {
		return Math.sqrt(sq((Point2.getX()-Point1.getX())) +
			sq((Point2.getY()-Point1.getY())));		
	}
	
	public static double sq(double val) {
		return val * val;
	}
	
	public static void drawArbitraryOffsetLine(Graphics2D g, int lx1, int ly1, int lx2, int ly2, int initOffset){
		Point p1 = new Point(lx1, ly1);
		Point p2 = new Point(lx2, ly2);
		
		int offset = initOffset;
		int lineLen = (int)lineLen(p1, p2);
		for(int i = 0; i < lineLen; i++){
			Point lp = WidgetUtils.calcPointOnLineAtDist(p1, p2, i);
			int xCoord = lp.x;
			int yCoord = lp.y;
			
			if(offset == 0){
				g.setColor(Color.BLACK);
				g.fillRect(xCoord, yCoord, 1, 1);
			}
			else if(offset == 1){
				g.setColor(Color.GRAY);
				g.fillRect(xCoord, yCoord, 1, 1);
			}
			else if(offset == 2){
				g.setColor(Color.WHITE);
				g.fillRect(xCoord, yCoord, 1, 1);
			}
			offset++;
			if(offset == 6) offset = 0;			
		}
	}
	
	public static void drawHorizontalOffsetLine(Graphics2D g, int lx1, int ly1, int len, int initOffset){
		int offset = initOffset;
		boolean reverse = false;
		if(len < 0){
			len = -len;
			reverse = true;
		}
		for(int i = 0; i < len; i++){
			int xCoord = reverse ? (lx1 - i) : lx1 + i;
			int yCoord = ly1;
			if(offset == 0){
				g.setColor(Color.BLACK);
				g.fillRect(xCoord, yCoord, 1, 1);
			}
			else if(offset == 1){
				g.setColor(Color.GRAY);
				g.fillRect(xCoord, yCoord, 1, 1);
			}
			else if(offset == 2){
				g.setColor(Color.WHITE);
				g.fillRect(xCoord, yCoord, 1, 1);
			}
			offset++;
			if(offset == 6) offset = 0;
		}
	}

	public static void drawVerticalOffsetLine(Graphics2D g, int lx1, int ly1, int len, int initOffset){
		int offset = initOffset;
		boolean reverse = false;
		if(len < 0){
			len = -len;
			reverse = true;
		}
		for(int i = 0; i < len; i++){
			int xCoord = lx1;
			int yCoord = reverse ? (ly1 - i) : ly1 + i;
			if(offset == 0){
				g.setColor(Color.BLACK);
				g.fillRect(xCoord, yCoord, 1, 1);
			}
			else if(offset == 1){
				g.setColor(Color.GRAY);
				g.fillRect(xCoord, yCoord, 1, 1);
			}
			else if(offset == 2){
				g.setColor(Color.WHITE);
				g.fillRect(xCoord, yCoord, 1, 1);
			}
			offset++;
			if(offset == 6) offset = 0;
		}
	}

	/*
	static final float[] dashArray = new float[]{3.0f};
	public static final Stroke dashedStrokes[] = new Stroke[]
		{
			new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 
		 	0.0f, dashArray, 0),
			new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 
			0.0f, dashArray, 1),
			new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 
			0.0f, dashArray, 2),
			new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 
			0.0f, dashArray, 3),
			new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 
			0.0f, dashArray, 4),
			new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 
			0.0f, dashArray, 5)
		};
	*/ 	

	public static void drawMarqueeRectangle(Graphics2D g, int lx1, int ly1, int lw, int lh, int initOffset){
		/*
		Stroke savedStroke = g.getStroke();
		g.setStroke(dashedStrokes[initOffset]);
		g.drawRect(lx1, ly1, lw, lh);
		g.setStroke(savedStroke);
		if(true) return;
		*/
		
		int lx2 = lx1 + lw;
		int ly2 = ly1 + lh;
			
		drawHorizontalOffsetLine(g, lx1, ly1, lw, initOffset);
		initOffset += lw;
		initOffset %= 6;
		
		drawVerticalOffsetLine(g, lx2, ly1, lh, initOffset);
		initOffset += lw;
		initOffset %= 6;
		
		drawHorizontalOffsetLine(g, lx2, ly2, -lw, initOffset);
		initOffset += lw;
		initOffset %= 6;
		
		drawVerticalOffsetLine(g, lx1, ly2, -lh, initOffset);
	}
	

}

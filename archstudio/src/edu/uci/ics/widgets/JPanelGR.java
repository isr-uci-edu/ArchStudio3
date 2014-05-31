package edu.uci.ics.widgets;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

import java.awt.Paint;

public class JPanelGR extends JPanelPT{

	protected Color ulColor;
	protected Color lrColor;

	public JPanelGR(Color ulColor, Color lrColor){
		super();
		this.ulColor = ulColor;
		this.lrColor = lrColor;
		setBackground(null);
	}
	
	public void paintComponent(Graphics g){
		if(!isOpaque()){
			super.paintComponent(g);
			return;
		}
		
		Graphics2D g2d = (Graphics2D)g.create();
		Rectangle visRect = this.getBounds();
		Rectangle normalizedRectangle = new Rectangle(0, 0, visRect.width, visRect.height);
		
		g2d.setPaint(new GradientPaint(
			new Point2D.Double(0, 0), 
			ulColor, 
			new Point2D.Double(visRect.width, visRect.height),
			lrColor));
		g2d.fillRect(visRect.x, visRect.y, visRect.width, visRect.height);
		super.paintComponent(g2d);
	}
	
	/*
	public void paintComponent(Graphics g){
		if(!isOpaque()){
			super.paintComponent(g);
			return;
		}
		
		Graphics2D g2d = (Graphics2D)g;
		Rectangle visRect = this.getVisibleRect();
		this.paint = new GradientPaint(
			new Point2D.Double(visRect.x, visRect.y), 
			ulColor, 
			new Point2D.Double(visRect.x + visRect.width, visRect.y + visRect.height),
			lrColor);
		super.paintComponent(g);
	}
	*/

}

package edu.uci.ics.widgets;

import java.awt.*;
import javax.swing.*;

public class JPanelPT extends JPanel{

	protected Paint paint = null;

	public JPanelPT(){
		super();
	}
	
	public JPanelPT(Paint paint){
		this();
		setPaint(paint);
	}
	
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		super.paintComponent(g);
		
		if(!isOpaque()){
			return;
		}
		
		Rectangle r = this.getVisibleRect();
		Paint oldPaint = g2d.getPaint();
		g2d.setPaint(paint);
		g2d.fill(r);
		g2d.setPaint(oldPaint);
	}
	
	public void setPaint(Paint paint){
		this.paint = paint;
		repaint();
	}
	
	public Paint getPaint(){
		return paint;
	}

}

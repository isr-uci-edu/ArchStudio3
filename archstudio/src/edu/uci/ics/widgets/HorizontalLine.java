package edu.uci.ics.widgets;

import java.awt.*;

import javax.swing.*;

public class HorizontalLine extends JComponent{
	public Dimension getPreferredSize(){
		return new Dimension((int)super.getPreferredSize().getWidth(), 3);
	}
	
	public Dimension getMinimumSize(){
		return new Dimension (0, 3);
	}
	
	public Dimension getMaximumSize(){
		return new Dimension((int)super.getMaximumSize().getWidth(), 3);
	}
	
	public void paint(Graphics g){
		//super.paint(g);
		g.setColor(Color.black);
		g.drawLine(1, 1, getWidth() - 2, 1);
		g.setColor(Color.white);
		g.drawLine(1, 2, getWidth() - 2, 2);
	}
}

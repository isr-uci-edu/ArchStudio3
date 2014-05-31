package edu.uci.ics.widgets.windowheader;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import edu.uci.ics.widgets.*;

public class CloseButton extends CustomButton {

	public CloseButton() {
		super();
	}

	public void drawIcon(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		Rectangle bounds = getVisibleRect();
		bounds.width--;
		bounds.height--;
		
		g2d.drawLine(bounds.x + 3, bounds.y + 3, bounds.x + bounds.width - 4, bounds.y + bounds.height - 4);
		g2d.drawLine(bounds.x + 4, bounds.y + 3, bounds.x + bounds.width - 4, bounds.y + bounds.height - 5);
		g2d.drawLine(bounds.x + 3, bounds.y + 4, bounds.x + bounds.width - 5, bounds.y + bounds.height - 4);
		
		g2d.drawLine(bounds.x + 3, bounds.y + bounds.height - 4, bounds.x + bounds.width - 4, bounds.y + 3);
		g2d.drawLine(bounds.x + 4, bounds.y + bounds.height - 4, bounds.x + bounds.width - 4, bounds.y + 4);
		g2d.drawLine(bounds.x + 3, bounds.y + bounds.height - 5, bounds.x + bounds.width - 5, bounds.y + 3);
		
	}
	

}

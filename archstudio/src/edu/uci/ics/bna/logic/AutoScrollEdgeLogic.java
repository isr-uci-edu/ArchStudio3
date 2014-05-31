package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.CoordinateMapper;
import edu.uci.ics.bna.DefaultCoordinateMapper;
import edu.uci.ics.bna.IDragMovable;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.ThingLogicAdapter;

/**
 * Note: this doesn't work.
 * 
 * @author edashofy
 */
public class AutoScrollEdgeLogic extends ThingLogicAdapter{

	protected boolean mouseDown = false;
	protected int lastMouseButton = -1;
	protected int lastMouseX = -1;
	protected int lastMouseY = -1;
	
	public void mousePressed(Thing t, MouseEvent evt, int worldX, int worldY){
		if(evt.getButton() == MouseEvent.BUTTON1){
			mouseDown = true;
			lastMouseX = evt.getX();
			lastMouseY = evt.getY();
		}
	}
	
	public void mouseReleased(Thing t, MouseEvent evt, int worldX, int worldY){
		if(evt.getButton() == MouseEvent.BUTTON1){
			mouseDown = false;
		}
	}
	
	public void mouseDragged(Thing t, MouseEvent evt, int worldX, int worldY){
		BNAComponent c = bnaComponent;
		CoordinateMapper cm = c.getCoordinateMapper();
		Dimension d = c.getSize();
		
		int dx = 0;
		int dy = 0;
		if(mouseDown){
			int x = evt.getX();
			int y = evt.getY();
			
			if(x < 5) dx = -5;
			if(y < 5) dy = -5;
			
			if(x > (d.width - 5)) dx = 5;
			if(y > (d.height - 5)) dy = 5;
		}
		
		if((dx != 0) || (dy != 0)){
			int dwx = cm.localXtoWorldX(dx) - cm.localXtoWorldX(0);
			int dwy = cm.localYtoWorldY(dy) - cm.localYtoWorldY(0);
			
			if(cm instanceof DefaultCoordinateMapper){
				((DefaultCoordinateMapper)cm).repositionRelative(dwx, dwy);
			}
		}
	}

}

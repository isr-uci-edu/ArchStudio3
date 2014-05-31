package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.CoordinateMapper;
import edu.uci.ics.bna.IDragMovable;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.ThingLogicAdapter;

public class DragMovableLogic extends ThingLogicAdapter{

	protected Thing grabbedThing = null;
	protected int lastMouseButton = -1;
	protected int lastMouseX = -1;
	protected int lastMouseY = -1;

	public void destroy() {
		grabbedThing = null;
		super.destroy();
	}
	
	public void mousePressed(Thing t, MouseEvent evt, int worldX, int worldY){
		grabbedThing = t;
		lastMouseButton = evt.getButton();
		lastMouseX = evt.getX();
		lastMouseY = evt.getY();
	}
	
	public void mouseReleased(Thing t, MouseEvent evt, int worldX, int worldY){
		grabbedThing = null;
	}
	
	public void mouseDragged(Thing t, MouseEvent evt, int worldX, int worldY){
		BNAComponent c = bnaComponent;
		if(lastMouseButton == MouseEvent.BUTTON1){
			if(grabbedThing != null){
				if(grabbedThing instanceof IDragMovable){
					IDragMovable b = (IDragMovable)grabbedThing;
					CoordinateMapper cm = c.getCoordinateMapper();
					int dwx = cm.localXtoWorldX(evt.getX()) - cm.localXtoWorldX(lastMouseX);
					int dwy = cm.localYtoWorldY(evt.getY()) - cm.localYtoWorldY(lastMouseY);
					
					b.moveRelative(dwx, dwy);
				}
			}
		}
		lastMouseX = evt.getX();
		lastMouseY = evt.getY();
	}
	

}

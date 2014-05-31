package edu.uci.ics.bna.swingthing;

import java.awt.*;
import java.awt.event.*;

import edu.uci.ics.bna.*;

public class LocalDragMovableLogic extends ThingLogicAdapter{

	protected Thing grabbedThing = null;
	protected int lastMouseButton = -1;
	protected int lastMouseX = -1;
	protected int lastMouseY = -1;
	
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
				if(grabbedThing instanceof ILocalDragMovable){
					ILocalDragMovable b = (ILocalDragMovable)grabbedThing;
					CoordinateMapper cm = c.getCoordinateMapper();
					int dwx = evt.getX() - lastMouseX;
					int dwy = evt.getY() - lastMouseY;
					
					b.localMoveRelative(dwx, dwy);
				}
			}
		}
		lastMouseX = evt.getX();
		lastMouseY = evt.getY();
	}
	

}

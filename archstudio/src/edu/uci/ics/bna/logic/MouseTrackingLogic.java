package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.ThingLogicAdapter;

public class MouseTrackingLogic extends ThingLogicAdapter{

	protected int lx = -1;
	protected int ly = -1;
	protected int wx = -1;
	protected int wy = -1;

	public void mouseMoved(Thing t, MouseEvent evt, int worldX, int worldY){
		wx = worldX;
		wy = worldY;
		lx = evt.getX();
		ly = evt.getY();
	}
	
	public int getLastWorldX(){
		return wx;
	}
	
	public int getLastWorldY(){
		return wy;
	}
	
	public int getLastLocalX(){
		return lx;
	}

	public int getLastLocalY(){
		return ly;
	}

}

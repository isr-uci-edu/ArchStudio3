package edu.uci.ics.bna;

import java.awt.*;
import java.awt.event.*;

public class ThingLogicAdapter implements ThingLogic{

	protected BNAComponent bnaComponent;
	
	public void bnaModelChanged(BNAModelEvent evt){}
	public void mouseEntered(Thing t, MouseEvent evt, int worldX, int worldY){}
	public void mouseExited(Thing t, MouseEvent evt, int worldX, int worldY){}
	public void mouseClicked(Thing t, MouseEvent evt, int worldX, int worldY){}
	public void mousePressed(Thing t, MouseEvent evt, int worldX, int worldY){}
	public void mouseReleased(Thing t, MouseEvent evt, int worldX, int worldY){}
	public void mouseDragged(Thing t, MouseEvent evt, int worldX, int worldY){}
	public void mouseMoved(Thing t, MouseEvent evt, int worldX, int worldY){}
	
	public void focusGained(FocusEvent evt){}
	public void focusLost(FocusEvent evt){}

	public void keyPressed(KeyEvent evt){}
	public void keyReleased(KeyEvent evt){}
	public void keyTyped(KeyEvent evt){}
	
	public void init(){}
	public void destroy(){}
	
	public void setComponent(BNAComponent newBNAComponent){
		if(this.bnaComponent != null){
			destroy();
		}
		this.bnaComponent = newBNAComponent;
		if(newBNAComponent != null){
			init();
		}
	}
	
	public BNAComponent getBNAComponent(){
		return this.bnaComponent;
	}
}
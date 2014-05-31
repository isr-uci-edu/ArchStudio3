package edu.uci.ics.bna;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.FocusEvent;

public interface ThingLogic extends BNAModelListener{
	
	public void mouseEntered(Thing t, MouseEvent evt, int worldX, int worldY);
	public void mouseExited(Thing t, MouseEvent evt, int worldX, int worldY);
	public void mouseClicked(Thing t, MouseEvent evt, int worldX, int worldY);
	public void mousePressed(Thing t, MouseEvent evt, int worldX, int worldY);
	public void mouseReleased(Thing t, MouseEvent evt, int worldX, int worldY);
	public void mouseDragged(Thing t, MouseEvent evt, int worldX, int worldY);
	public void mouseMoved(Thing t, MouseEvent evt, int worldX, int worldY);
	
	public void focusGained(FocusEvent evt);
	public void focusLost(FocusEvent evt);
	
	public void keyPressed(KeyEvent evt);
	public void keyReleased(KeyEvent evt);
	public void keyTyped(KeyEvent evt);
	
	public void setComponent(BNAComponent bnaComponent);
	public BNAComponent getBNAComponent();

}

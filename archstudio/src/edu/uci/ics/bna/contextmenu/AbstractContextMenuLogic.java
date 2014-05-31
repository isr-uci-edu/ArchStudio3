package edu.uci.ics.bna.contextmenu;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.ThingLogic;
import edu.uci.ics.bna.ThingLogicAdapter;

public abstract class AbstractContextMenuLogic extends ThingLogicAdapter implements ThingLogic{

	public void mousePressed(Thing t, MouseEvent evt, int worldX, int worldY){
		checkAndDoPopup(t, evt, worldX, worldY);
	}
	
	public void mouseReleased(Thing t, MouseEvent evt, int worldX, int worldY){
		checkAndDoPopup(t, evt, worldX, worldY);
	}

	protected void checkAndDoPopup(Thing t, MouseEvent evt, int worldX, int worldY){
		if(evt.isPopupTrigger()){
			JPopupMenu contextMenu = getContextMenu(t, worldX, worldY);
			if(contextMenu != null){
				contextMenu.show(evt.getComponent(), evt.getX(), evt.getY());
			}
		}
	}

	public abstract JPopupMenu getContextMenu(Thing t, int worldX, int worldY);

}

package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import edu.uci.ics.bna.*;

public class OneClickSelectionLogic extends ThingLogicAdapter{

	public void mouseClicked(Thing t, MouseEvent evt, int worldX, int worldY){
		if (t instanceof IUserEditable) {
			IUserEditable et = (IUserEditable) t;
			if (!et.isUserEditable())
				return;
		}
	//public void mousePressed(Thing t, MouseEvent evt, int worldX, int worldY){
		BNAComponent c = bnaComponent;
		if(evt.getButton() == MouseEvent.BUTTON1){
			if((t != null) && (t instanceof ISelectable)){
				boolean controlPressed = BNAUtils.wasControlPressed(evt);
				boolean shiftPressed = BNAUtils.wasShiftPressed(evt);
				
				if((!controlPressed) && (!shiftPressed)){
					SelectionUtils.removeAllSelections(c);
					((ISelectable)t).setSelected(true);
				}
				else if((controlPressed) && (!shiftPressed)){
					//Toggle selection
					((ISelectable)t).setSelected(!((ISelectable)t).isSelected());
				}
				else if((shiftPressed) && (!controlPressed)){
					//Add to selection
					((ISelectable)t).setSelected(true);
				}
				else if((shiftPressed) && (controlPressed)){
					//Subtract from selection
					((ISelectable)t).setSelected(false);
				}
			}
			else if(t == null){
				SelectionUtils.removeAllSelections(c);
			}
		}
	}
	
}
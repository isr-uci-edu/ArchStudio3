package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.BNAModel;
import edu.uci.ics.bna.DefaultBNAModel;
import edu.uci.ics.bna.CoordinateMapper;
import edu.uci.ics.bna.IDragMovable;
import edu.uci.ics.bna.ISelectable;
import edu.uci.ics.bna.SelectionUtils;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.ThingLogic;

public class DragMovableSelectionLogic extends DragMovableLogic implements ThingLogic{

	public synchronized void mouseDragged(Thing t, MouseEvent evt, int worldX, int worldY){
		BNAComponent c = bnaComponent;
		if(lastMouseButton == MouseEvent.BUTTON1){
			if(grabbedThing != null){
				if(grabbedThing instanceof IDragMovable){
					if(grabbedThing instanceof ISelectable){
						if(!((ISelectable)grabbedThing).isSelected()){
							SelectionUtils.removeAllSelections(c);
						}
					}
					else{
						SelectionUtils.removeAllSelections(c);
					}
					ArrayList allSelectedThings = new ArrayList();
					for(Iterator it = c.getModel().getThingIterator(); it.hasNext(); ){
						Thing otherThing = (Thing)it.next();
						if(otherThing instanceof ISelectable){
							if(((ISelectable)otherThing).isSelected()){
								allSelectedThings.add(otherThing);
							}
						}
					}
					if(!allSelectedThings.contains(grabbedThing)){
						allSelectedThings.add(grabbedThing);
					}
					Thing[] ts = (Thing[])allSelectedThings.toArray(new Thing[0]);
					
					CoordinateMapper cm = c.getCoordinateMapper();
					int dwx = cm.localXtoWorldX(evt.getX()) - cm.localXtoWorldX(lastMouseX);
					int dwy = cm.localYtoWorldY(evt.getY()) - cm.localYtoWorldY(lastMouseY);
					
					try{
						c.getModel().beginBulkChange();
						for(int i = 0; i < ts.length; i++){
							if(ts[i] instanceof IDragMovable){
								((IDragMovable)ts[i]).moveRelative(dwx, dwy);
							}
						}
						BNAModel m = c.getModel();
						if(m instanceof DefaultBNAModel){
							((DefaultBNAModel)m).waitForProcessing();
						}
					}finally{
						c.getModel().endBulkChange();
					}
				}
			}
		}
		lastMouseX = evt.getX();
		lastMouseY = evt.getY();
	}
	

}
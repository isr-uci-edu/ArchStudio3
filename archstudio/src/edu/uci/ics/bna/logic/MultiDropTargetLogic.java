package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.ThingLogicAdapter;

import java.awt.dnd.*;
import java.awt.datatransfer.*;

public class MultiDropTargetLogic extends ThingLogicAdapter{
	protected Vector dropTargetLogics = new Vector();
	
	public void init(){
		getBNAComponent().setDropTarget(new DropTarget(getBNAComponent(), new LogicDropTargetListener()));
	}
	
	public void addDropTargetLogic(AbstractDropTargetLogic adtl){
		adtl.setMultiDropTargetLogic(this);
		dropTargetLogics.add(adtl);
	}
	
	public void removeDropTargetLogic(AbstractDropTargetLogic adtl){
		adtl.setMultiDropTargetLogic(null);
		dropTargetLogics.remove(adtl);
	}
	
	protected AbstractDropTargetLogic[] getDropTargetLogics(){
		return (AbstractDropTargetLogic[])dropTargetLogics.toArray(new AbstractDropTargetLogic[dropTargetLogics.size()]);
	}
	
	class LogicDropTargetListener implements DropTargetListener{
		public void dragEnter(DropTargetDragEvent dtde){
			BNAComponent c = getBNAComponent();
			if(c != null){
				int lx = dtde.getLocation().x;
				int ly = dtde.getLocation().y;
				AbstractDropTargetLogic[] adtls = getDropTargetLogics();
				for(int i = 0; i < adtls.length; i++){
					adtls[i].dragEnter(dtde, c.getThingAt(lx, ly),
						c.getCoordinateMapper().localXtoWorldX(lx),
						c.getCoordinateMapper().localYtoWorldY(ly));
				}
			} 
		}

		public void dragExit(DropTargetEvent dte){
			BNAComponent c = getBNAComponent();
			if(c != null){
				AbstractDropTargetLogic[] adtls = getDropTargetLogics();
				for(int i = 0; i < adtls.length; i++){
					adtls[i].dragExit(dte);
				}
			}
		}
	
		public void dragOver(DropTargetDragEvent dtde){
			BNAComponent c = getBNAComponent();
			if(c != null){
				int lx = dtde.getLocation().x;
				int ly = dtde.getLocation().y;
				AbstractDropTargetLogic[] adtls = getDropTargetLogics();
				for(int i = 0; i < adtls.length; i++){
					adtls[i].dragOver(dtde, c.getThingAt(lx, ly),
						c.getCoordinateMapper().localXtoWorldX(lx),
						c.getCoordinateMapper().localYtoWorldY(ly));
				}
			} 
		}
		
		public void drop(DropTargetDropEvent dtde){
			BNAComponent c = getBNAComponent();
			if(c != null){
				int lx = dtde.getLocation().x;
				int ly = dtde.getLocation().y;
				AbstractDropTargetLogic[] adtls = getDropTargetLogics();
				for(int i = 0; i < adtls.length; i++){
					boolean accepted = adtls[i].drop(dtde, c.getThingAt(lx, ly),
						c.getCoordinateMapper().localXtoWorldX(lx),
						c.getCoordinateMapper().localYtoWorldY(ly));
					if(accepted){
						dtde.acceptDrop(dtde.getDropAction());
						return;
					}
				}
				//Nobody accepted the drop.
				dtde.rejectDrop();
				return;
			} 
		}
		
		public void dropActionChanged(DropTargetDragEvent dtde){
			BNAComponent c = getBNAComponent();
			if(c != null){
				int lx = dtde.getLocation().x;
				int ly = dtde.getLocation().y;
				AbstractDropTargetLogic[] adtls = getDropTargetLogics();
				for(int i = 0; i < adtls.length; i++){
					adtls[i].dropActionChanged(dtde, c.getThingAt(lx, ly),
						c.getCoordinateMapper().localXtoWorldX(lx),
						c.getCoordinateMapper().localYtoWorldY(ly));
				}
			} 
		}
	}
	
}

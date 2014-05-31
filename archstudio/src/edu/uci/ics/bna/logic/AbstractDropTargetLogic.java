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

public abstract class AbstractDropTargetLogic /*extends ThingLogicAdapter*/{

	protected MultiDropTargetLogic dtl;
	
	/*
	public void setComponent(BNAComponent bnaComponent){
		super.setComponent(bnaComponent);
		if(bnaComponent != null){
			bnaComponent.setDropTarget(new DropTarget(bnaComponent, new LogicDropTargetListener()));
		}
	}
	*/
	
	public void setMultiDropTargetLogic(MultiDropTargetLogic dtl){
		this.dtl = dtl;
	}
	
	public BNAComponent getBNAComponent(){
		if(dtl == null) return null;
		return dtl.getBNAComponent();
	}
	
	public void dragEnter(DropTargetDragEvent dtde, Thing thingOnTop, int worldX, int worldY){}
	public void dragExit(DropTargetEvent dtde){}
	public void dragOver(DropTargetDragEvent dtde, Thing thingOnTop, int worldX, int worldY){}
	
	//Must return true to accept drop, false if not.
	public boolean drop(DropTargetDropEvent dtde, Thing thingOnTop, int worldX, int worldY){
		return false;
	}
	public void dropActionChanged(DropTargetDragEvent dtde, Thing thingOnTop, int worldX, int worldY){}
	
	/*
	class LogicDropTargetListener implements DropTargetListener{
		public void dragEnter(DropTargetDragEvent dtde){
			BNAComponent c = getBNAComponent();
			if(c != null){
				int lx = dtde.getLocation().x;
				int ly = dtde.getLocation().y;
				AbstractDropTargetLogic.this.dragEnter(dtde, c.getThingAt(lx, ly),
					c.getCoordinateMapper().localXtoWorldX(lx),
					c.getCoordinateMapper().localYtoWorldY(ly));
			} 
		}

		public void dragExit(DropTargetEvent dte){
			BNAComponent c = getBNAComponent();
			if(c != null){
				AbstractDropTargetLogic.this.dragExit(dte);
			}
		}
	
		public void dragOver(DropTargetDragEvent dtde){
			BNAComponent c = getBNAComponent();
			if(c != null){
				int lx = dtde.getLocation().x;
				int ly = dtde.getLocation().y;
				AbstractDropTargetLogic.this.dragOver(dtde, c.getThingAt(lx, ly),
					c.getCoordinateMapper().localXtoWorldX(lx),
					c.getCoordinateMapper().localYtoWorldY(ly));
			} 
		}
		
		public void drop(DropTargetDropEvent dtde){
			BNAComponent c = getBNAComponent();
			if(c != null){
				int lx = dtde.getLocation().x;
				int ly = dtde.getLocation().y;
				AbstractDropTargetLogic.this.drop(dtde, c.getThingAt(lx, ly),
					c.getCoordinateMapper().localXtoWorldX(lx),
					c.getCoordinateMapper().localYtoWorldY(ly));
			} 
		}
		
		public void dropActionChanged(DropTargetDragEvent dtde){
			BNAComponent c = getBNAComponent();
			if(c != null){
				int lx = dtde.getLocation().x;
				int ly = dtde.getLocation().y;
				AbstractDropTargetLogic.this.dropActionChanged(dtde, c.getThingAt(lx, ly),
					c.getCoordinateMapper().localXtoWorldX(lx),
					c.getCoordinateMapper().localYtoWorldY(ly));
			} 
		}
	}
	*/
	
}

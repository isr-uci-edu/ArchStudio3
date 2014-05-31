package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.IDragAndDroppable;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.ThingLogicAdapter;

import java.awt.dnd.*;
import java.awt.datatransfer.*;

public class DragAndDropLogic extends ThingLogicAdapter{

	protected static Vector participatingComponents = new Vector();
	protected static Thing grabbedThing = null;
	protected static String sourceBNAComponentID = null;

	protected int lastMouseButton = -1;
	protected int lastMouseX = -1;
	protected int lastMouseY = -1;
	
	protected Vector dropHandlers = new Vector();
	
	public void init(){
		BNAComponent bnaComponent = getBNAComponent();
		if(!participatingComponents.contains(bnaComponent)){
			participatingComponents.addElement(bnaComponent);
		}
		getBNAComponent().setTransferHandler(new javax.swing.TransferHandler("DNDThing"));
		DropTarget dt = new DropTarget(bnaComponent, new BNADropTargetListener());
	}
	
	public void destroy(){
		participatingComponents.remove(getBNAComponent());
	}
	
	protected static DataFlavor getThingFlavor(Transferable t){
		DataFlavor[] availableFlavors = t.getTransferDataFlavors();
		for(int i = 0; i < availableFlavors.length; i++){
			if(availableFlavors[i].getRepresentationClass().equals(edu.uci.ics.bna.Thing.class)){
				return availableFlavors[i];
			}
		}
		return null;
	}
	
	final DataFlavor thingDataFlavor = new DataFlavor(edu.uci.ics.bna.Thing.class, "BNA Thing");
	class BNADropTargetListener extends DropTargetAdapter{
		public void drop(DropTargetDropEvent dtde){
			Transferable t = dtde.getTransferable();
			DataFlavor thingDataFlavor = getThingFlavor(t);
			if(t.isDataFlavorSupported(thingDataFlavor)){
				try{
					Thing thing = (Thing)t.getTransferData(thingDataFlavor);
					String dndSourceID = (String)thing.getProperty("#dndsourceid");
					thing.removeProperty("#dndsourceid");
					int worldX = bnaComponent.getCoordinateMapper().localXtoWorldX(dtde.getLocation().x);
					int worldY = bnaComponent.getCoordinateMapper().localYtoWorldY(dtde.getLocation().y);
					for(int i = 0; i < dropHandlers.size(); i++){
						((DropHandler)dropHandlers.elementAt(i)).handleDrop(dndSourceID, bnaComponent, thing, worldX, worldY);
					}
				}
				catch(UnsupportedFlavorException ufe){
				}
				catch(java.io.IOException e){
				}
			}
			dtde.dropComplete(true);
		}
	}
	
	public void mousePressed(Thing t, MouseEvent evt, int worldX, int worldY){
		if(t instanceof IDragAndDroppable){
			if(evt.getButton() == MouseEvent.BUTTON1){
				javax.swing.JComponent c = (javax.swing.JComponent)evt.getSource();
				javax.swing.TransferHandler th = c.getTransferHandler();
				th.exportAsDrag(c, evt, javax.swing.TransferHandler.COPY);
			}
		}
	}
	
	public void addDropHandler(DropHandler dl){
		synchronized(dropHandlers){
			dropHandlers.addElement(dl);
		}
	}
	
	public void removeDropHandler(DropHandler dl){
		synchronized(dropHandlers){
			dropHandlers.removeElement(dl);
		}
	}		
	
	public void mouseReleased(Thing t, MouseEvent evt, int worldX, int worldY){
		if(grabbedThing != null){
			for(int i = 0; i < participatingComponents.size(); i++){
				((BNAComponent)participatingComponents.elementAt(i)).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
		grabbedThing = null;
	}
	
	public void mouseDragged(Thing t, MouseEvent evt, int worldX, int worldY){
		if(grabbedThing != null){
		}
	}
	
}

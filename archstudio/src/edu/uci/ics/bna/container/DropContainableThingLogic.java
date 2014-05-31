package edu.uci.ics.bna.container;

import java.awt.event.MouseEvent;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.logic.*;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Iterator;

public class DropContainableThingLogic extends ThingLogicAdapter {
	
	protected Thing grabbedThing;
	protected int lastMouseButton;
	protected int lastMouseX;
	protected int lastMouseY;
	protected boolean movedStuff = false;
	
	protected SelectionTrackingLogic stl = null;
	
	public DropContainableThingLogic(SelectionTrackingLogic stl){
		this.stl = stl;
	}
	
	public void mousePressed(Thing t, MouseEvent evt, int worldX, int worldY){
		grabbedThing = t;
		lastMouseButton = evt.getButton();
		lastMouseX = evt.getX();
		lastMouseY = evt.getY();
		movedStuff = false;
	}
	
	public synchronized void mouseDragged(Thing t, MouseEvent evt, int worldX, int worldY){
		BNAComponent c = bnaComponent;
		if(lastMouseButton == MouseEvent.BUTTON1){
			if(grabbedThing != null){
				movedStuff = true;
				
				BNAModel bm = c.getModel();
				for(Iterator it = bm.getThingIterator(); it.hasNext(); ){
					Thing thing = (Thing)it.next();
					if(thing instanceof ContainerThing){
						if(thing == grabbedThing){
							continue;
						}
						Rectangle boundingBox = ((ContainerThing)thing).getBoundingBox();
						if(boundingBox.contains(worldX, worldY)){
							((ContainerThing)thing).setColor(Color.LIGHT_GRAY);
						}
						else{
							((ContainerThing)thing).setColor(Color.GRAY);
						}
					}
				}
			}
		}
	}

	public void mouseReleased(Thing t, MouseEvent evt, int worldX, int worldY){
		BNAComponent c = bnaComponent;
		
		if(movedStuff){
			System.out.println("grabbedThing: " + grabbedThing + " was moved");
		}

		if(lastMouseButton == MouseEvent.BUTTON1){
			if(grabbedThing != null){
				BNAModel bm = c.getModel();
				for(Iterator it = bm.getThingIterator(); it.hasNext(); ){
					Thing thing = (Thing)it.next();
					if(thing instanceof ContainerThing){
						if(thing == grabbedThing){
							continue;
						}
						Rectangle boundingBox = ((ContainerThing)thing).getBoundingBox();
						if(boundingBox.contains(worldX, worldY)){
							System.out.println("DROP IN CONTAINER! MAN IN SUIT!");
							
							if(grabbedThing instanceof IBoxBounded){
								Rectangle gtbb = ((IBoxBounded)grabbedThing).getBoundingBox();
								Rectangle newBoundingBox = new Rectangle(boundingBox);
								newBoundingBox = newBoundingBox.union(gtbb);
								((ContainerThing)thing).setBoundingBox(newBoundingBox);
							}
							
						}
					}
				}
			}
		}
		
		movedStuff = false;
		grabbedThing = null;
	}
	
}

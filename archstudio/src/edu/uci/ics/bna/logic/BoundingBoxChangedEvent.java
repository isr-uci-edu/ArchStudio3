package edu.uci.ics.bna.logic;

import java.awt.Rectangle;

import edu.uci.ics.bna.Thing;

public class BoundingBoxChangedEvent{
	
	protected BoundingBoxTrackingLogic source;
	protected Thing targetThing;
	protected Rectangle oldBoundingBox;
	protected Rectangle newBoundingBox;
	
	public BoundingBoxChangedEvent(BoundingBoxTrackingLogic source, Thing targetThing, Rectangle oldBoundingBox, Rectangle newBoundingBox){
		this.source = source;
		this.targetThing = targetThing;
		this.oldBoundingBox = oldBoundingBox;
		this.newBoundingBox = newBoundingBox;
	}
	
	public BoundingBoxTrackingLogic getSource(){
		return source;
	}
	
	public Thing getTargetThing(){
		return targetThing;
	}
	
	public Rectangle getOldBoundingBox(){
		return oldBoundingBox;
	}
	
	public Rectangle getNewBoundingBox(){
		return newBoundingBox;
	}
}

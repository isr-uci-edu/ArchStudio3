package edu.uci.ics.bna.swingthing;

import edu.uci.ics.bna.*;

import java.awt.Rectangle;

public class LocalBoundingBoxChangedEvent{
	
	protected LocalBoundingBoxTrackingLogic source;
	protected Thing targetThing;
	protected Rectangle oldLocalBoundingBox;
	protected Rectangle newLocalBoundingBox;
	
	public LocalBoundingBoxChangedEvent(LocalBoundingBoxTrackingLogic source, Thing targetThing, Rectangle oldLocalBoundingBox, Rectangle newLocalBoundingBox){
		this.source = source;
		this.targetThing = targetThing;
		this.oldLocalBoundingBox = oldLocalBoundingBox;
		this.newLocalBoundingBox = newLocalBoundingBox;
	}
	
	public LocalBoundingBoxTrackingLogic getSource(){
		return source;
	}
	
	public Thing getTargetThing(){
		return targetThing;
	}
	
	public Rectangle getOldLocalBoundingBox(){
		return oldLocalBoundingBox;
	}
	
	public Rectangle getNewLocalBoundingBox(){
		return newLocalBoundingBox;
	}
}

package edu.uci.ics.bna.logic;

import java.awt.Rectangle;

import edu.uci.ics.bna.Thing;

public class StickyBoxChangedEvent{
	
	protected StickyBoxTrackingLogic source;
	protected Thing targetThing;
	protected Rectangle oldStickyBox;
	protected Rectangle newStickyBox;
	
	public StickyBoxChangedEvent(StickyBoxTrackingLogic source, Thing targetThing, Rectangle oldStickyBox, Rectangle newStickyBox){
		this.source = source;
		this.targetThing = targetThing;
		this.oldStickyBox = oldStickyBox;
		this.newStickyBox = newStickyBox;
	}
	
	public StickyBoxTrackingLogic getSource(){
		return source;
	}
	
	public Thing getTargetThing(){
		return targetThing;
	}
	
	public Rectangle getOldStickyBox(){
		return oldStickyBox;
	}
	
	public Rectangle getNewStickyBox(){
		return newStickyBox;
	}
}
package edu.uci.ics.bna.logic;

import java.awt.Point;

import edu.uci.ics.bna.Thing;

public class AnchorPointChangedEvent{
	
	protected AnchorPointTrackingLogic source;
	protected Thing targetThing;
	protected Point oldAnchorPoint;
	protected Point newAnchorPoint;
	
	public AnchorPointChangedEvent(AnchorPointTrackingLogic source, Thing targetThing, Point oldAnchorPoint, Point newAnchorPoint){
		this.source = source;
		this.targetThing = targetThing;
		this.oldAnchorPoint = oldAnchorPoint;
		this.newAnchorPoint = newAnchorPoint;
	}
	
	public AnchorPointTrackingLogic getSource(){
		return source;
	}
	
	public Thing getTargetThing(){
		return targetThing;
	}
	
	public Point getOldAnchorPoint(){
		return oldAnchorPoint;
	}
	
	public Point getNewAnchorPoint(){
		return newAnchorPoint;
	}
}

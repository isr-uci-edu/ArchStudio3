package edu.uci.ics.bna.logic;

import edu.uci.ics.bna.Thing;

public class SelectionChangedEvent{

	public static final int THING_SELECTED = 100;
	public static final int THING_DESELECTED = 105;
	
	protected SelectionTrackingLogic source;
	protected int eventType;
	protected Thing targetThing;
	
	public SelectionChangedEvent(SelectionTrackingLogic source, int eventType, Thing targetThing){
		this.source = source;
		this.eventType = eventType;
		this.targetThing = targetThing;
	}
	
	public SelectionTrackingLogic getSource(){
		return source;
	}
	
	public int getEventType(){
		return eventType;
	}
	
	public Thing getTargetThing(){
		return targetThing;
	}

}

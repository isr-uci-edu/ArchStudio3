package edu.uci.ics.bna.logic;

import java.awt.Rectangle;
import java.util.*;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.BNAModel;
import edu.uci.ics.bna.BNAModelEvent;
import edu.uci.ics.bna.IBoxBounded;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.ThingLogicAdapter;

public abstract class ThingTrackingLogic extends ThingLogicAdapter{

	protected Set trackedThings = new HashSet();
	
	//Should return 'true' if this thing is being tracked by
	//this ThingTrackingLogic
	public abstract boolean isTracking(Thing t);
	
	protected boolean initialized = false;
	
	protected void init(BNAModel m){
		synchronized(trackedThings){
			for(Iterator it = m.getThingIterator(); it.hasNext(); ){
				Thing t = (Thing)it.next();
				if(isTracking(t)){
					trackedThings.add(t);
				}
				initialized = true;
			}
		}
	}
	
	public void init(){
		init(getBNAComponent().getModel());
	}
	
	public void bnaModelChanged(BNAModelEvent evt){
		synchronized(trackedThings){
			if(!initialized){
				init(evt.getSource());
			}
			if(evt.getEventType() == BNAModelEvent.THING_ADDED){
				Thing addedThing = evt.getTargetThing();
				if(isTracking(addedThing)){
					trackedThings.add(addedThing);
				}
			}
			else if(evt.getEventType() == BNAModelEvent.THING_REMOVED){
				Thing removedThing = evt.getTargetThing();
				if(removedThing != null){
					if(isTracking(removedThing)){
						trackedThings.remove(removedThing);
					}
				}
			}
		}
	}	
	
	final Thing[] emptyThingArray = new Thing[0];
	public Thing[] getTrackedThings(){
		synchronized(trackedThings){
			return (Thing[])trackedThings.toArray(emptyThingArray);
		}
	}
}

package edu.uci.ics.bna.logic;

import java.awt.Rectangle;
import java.util.*;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.BNAModel;
import edu.uci.ics.bna.BNAModelEvent;
import edu.uci.ics.bna.IBoxBounded;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.ThingLogicAdapter;

public class BoundingBoxTrackingLogic extends ThingLogicAdapter{

	protected Map idToBoundingBoxMap = Collections.synchronizedMap(new HashMap());
	protected boolean initialized = false;

	protected Vector boundingBoxTrackingListeners = new Vector();
	
	public void addBoundingBoxTrackingListener(BoundingBoxTrackingListener l){
		boundingBoxTrackingListeners.addElement(l);
	}
	
	public void removeBoundingBoxTrackingListener(BoundingBoxTrackingListener l){
		boundingBoxTrackingListeners.removeElement(l);
	}
	
	protected void fireBoundingBoxChangedEvent(Thing targetThing, Rectangle oldBoundingBox, Rectangle newBoundingBox){
		List l = null;
		synchronized(boundingBoxTrackingListeners){
			l = new ArrayList(boundingBoxTrackingListeners);
		}
		BoundingBoxChangedEvent evt = new BoundingBoxChangedEvent(this, targetThing, oldBoundingBox, newBoundingBox);
		
		for(int i = 0; i < l.size(); i++){
			((BoundingBoxTrackingListener)l.get(i)).boundingBoxChanged(evt);
		}
	}

	protected void init(BNAModel m){
		synchronized(idToBoundingBoxMap){
			for(Iterator it = m.getThingIterator(); it.hasNext(); ){
				Thing t = (Thing)it.next();
				if(t instanceof IBoxBounded){
					IBoxBounded st = (IBoxBounded)t;
					idToBoundingBoxMap.put(st.getID(), st.getBoundingBox());
				}
			}
			initialized = true;
		}
	}
	
	public void init(){
		init(getBNAComponent().getModel());
	}
	
	public void bnaModelChanged(BNAModelEvent evt){
		if(!initialized){
			init(evt.getSource());
		}
		if(evt.getEventType() == BNAModelEvent.THING_ADDED){
			Thing addedThing = evt.getTargetThing();
			if(addedThing instanceof IBoxBounded){
				IBoxBounded st = (IBoxBounded)addedThing;
				Rectangle oldBoundingBox = (Rectangle)idToBoundingBoxMap.get(st.getID());
				Rectangle newBoundingBox = st.getBoundingBox();
				idToBoundingBoxMap.put(st.getID(), newBoundingBox);
				fireBoundingBoxChangedEvent(st, oldBoundingBox, newBoundingBox);
			}
		}
		else if(evt.getEventType() == BNAModelEvent.THING_REMOVED){
			if(idToBoundingBoxMap.get(evt.getTargetThing().getID()) != null){
				Rectangle oldBoundingBox = (Rectangle)idToBoundingBoxMap.get(evt.getTargetThing().getID());
				idToBoundingBoxMap.remove(evt.getTargetThing().getID());
				fireBoundingBoxChangedEvent(evt.getTargetThing(), oldBoundingBox, null);
			}
		}
		else if(evt.getEventType() == BNAModelEvent.THING_CHANGED){
			Thing changedThing = evt.getTargetThing();
			if(changedThing instanceof IBoxBounded){
				IBoxBounded st = (IBoxBounded)changedThing;
				
				Rectangle oldBoundingBox = (Rectangle)idToBoundingBoxMap.get(st.getID());
				Rectangle newBoundingBox = st.getBoundingBox();
				
				boolean boundingBoxChanged = false;
				if((oldBoundingBox == null) && (newBoundingBox == null)){
					boundingBoxChanged = false;
				}
				else if((oldBoundingBox == null) && (newBoundingBox != null)){
					boundingBoxChanged = true;
				}
				else if((oldBoundingBox != null) && (newBoundingBox == null)){
					boundingBoxChanged = true;
				}
				else{
					boundingBoxChanged = !oldBoundingBox.equals(newBoundingBox); 
				}
				
				if(boundingBoxChanged){
					idToBoundingBoxMap.put(st.getID(), newBoundingBox);
					fireBoundingBoxChangedEvent(evt.getTargetThing(), oldBoundingBox, newBoundingBox);
				}
			}
		}
	}
	
}
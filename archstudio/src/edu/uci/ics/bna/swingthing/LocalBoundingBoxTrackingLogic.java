package edu.uci.ics.bna.swingthing;

import edu.uci.ics.bna.*;

import java.awt.Rectangle;
import java.util.*;

public class LocalBoundingBoxTrackingLogic extends ThingLogicAdapter{

	protected Map idToBoundingBoxMap = Collections.synchronizedMap(new HashMap());
	protected boolean initialized = false;

	protected Vector boundingBoxTrackingListeners = new Vector();
	
	public void addLocalBoundingBoxTrackingListener(LocalBoundingBoxTrackingListener l){
		boundingBoxTrackingListeners.addElement(l);
	}
	
	public void removeLocalBoundingBoxTrackingListener(LocalBoundingBoxTrackingListener l){
		boundingBoxTrackingListeners.removeElement(l);
	}
	
	protected void fireLocalBoundingBoxChangedEvent(Thing targetThing, Rectangle oldBoundingBox, Rectangle newBoundingBox){
		List l = null;
		synchronized(boundingBoxTrackingListeners){
			l = new ArrayList(boundingBoxTrackingListeners);
		}
		LocalBoundingBoxChangedEvent evt = new LocalBoundingBoxChangedEvent(this, targetThing, oldBoundingBox, newBoundingBox);
		
		for(int i = 0; i < l.size(); i++){
			((LocalBoundingBoxTrackingListener)l.get(i)).localBoundingBoxChanged(evt);
		}
	}

	protected void init(BNAModel m){
		synchronized(idToBoundingBoxMap){
			for(Iterator it = m.getThingIterator(); it.hasNext(); ){
				Thing t = (Thing)it.next();
				if(t instanceof ILocalBoxBounded){
					ILocalBoxBounded st = (ILocalBoxBounded)t;
					idToBoundingBoxMap.put(st.getID(), st.getLocalBoundingBox());
				}
			}
			initialized = true;
		}
	}
	
	public void init(){
		init(getBNAComponent().getModel());
	}
	
	public synchronized void bnaModelChanged(BNAModelEvent evt){
		if(!initialized){
			init(evt.getSource());
		}
		if(evt.getEventType() == BNAModelEvent.THING_ADDED){
			Thing addedThing = evt.getTargetThing();
			if(addedThing instanceof ILocalBoxBounded){
				ILocalBoxBounded st = (ILocalBoxBounded)addedThing;
				Rectangle oldBoundingBox = (Rectangle)idToBoundingBoxMap.get(st.getID());
				Rectangle newBoundingBox = st.getLocalBoundingBox();
				idToBoundingBoxMap.put(st.getID(), newBoundingBox);
				fireLocalBoundingBoxChangedEvent(st, oldBoundingBox, newBoundingBox);
			}
		}
		else if(evt.getEventType() == BNAModelEvent.THING_REMOVED){
			if(idToBoundingBoxMap.get(evt.getTargetThing().getID()) != null){
				Rectangle oldBoundingBox = (Rectangle)idToBoundingBoxMap.get(evt.getTargetThing().getID());
				idToBoundingBoxMap.remove(evt.getTargetThing().getID());
				fireLocalBoundingBoxChangedEvent(evt.getTargetThing(), oldBoundingBox, null);
			}
		}
		else if(evt.getEventType() == BNAModelEvent.THING_CHANGED){
			Thing changedThing = evt.getTargetThing();
			if(changedThing instanceof ILocalBoxBounded){
				ILocalBoxBounded st = (ILocalBoxBounded)changedThing;
				
				Rectangle oldBoundingBox = (Rectangle)idToBoundingBoxMap.get(st.getID());
				Rectangle newBoundingBox = st.getLocalBoundingBox();
				
				if(oldBoundingBox == null){
					return;
				}
				else if(!oldBoundingBox.equals(newBoundingBox)){
					idToBoundingBoxMap.put(st.getID(), newBoundingBox);
					fireLocalBoundingBoxChangedEvent(evt.getTargetThing(), oldBoundingBox, newBoundingBox);
				}
			}
		}
	}
	
}
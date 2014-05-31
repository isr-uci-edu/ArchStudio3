package edu.uci.ics.bna.logic;

import java.awt.Rectangle;
import java.util.*;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.BNAModel;
import edu.uci.ics.bna.BNAModelEvent;
import edu.uci.ics.bna.IStickyBoxBounded;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.ThingLogicAdapter;

public class StickyBoxTrackingLogic extends ThingLogicAdapter{

	protected Map idToStickyBoxMap = Collections.synchronizedMap(new HashMap());
	protected boolean initialized = false;

	protected Vector stickyBoxTrackingListeners = new Vector();
	
	public void addStickyBoxTrackingListener(StickyBoxTrackingListener l){
		stickyBoxTrackingListeners.addElement(l);
	}
	
	public void removeStickyBoxTrackingListener(StickyBoxTrackingListener l){
		stickyBoxTrackingListeners.removeElement(l);
	}
	
	protected void fireStickyBoxChangedEvent(Thing targetThing, Rectangle oldStickyBox, Rectangle newStickyBox){
		List l = null;
		synchronized(stickyBoxTrackingListeners){
			l = new ArrayList(stickyBoxTrackingListeners);
		}
		StickyBoxChangedEvent evt = new StickyBoxChangedEvent(this, targetThing, oldStickyBox, newStickyBox);
		
		for(int i = 0; i < l.size(); i++){
			((StickyBoxTrackingListener)l.get(i)).stickyBoxChanged(evt);
		}
	}

	protected void init(BNAModel m){
		synchronized(idToStickyBoxMap){
			for(Iterator it = m.getThingIterator(); it.hasNext(); ){
				Thing t = (Thing)it.next();
				if(t instanceof IStickyBoxBounded){
					IStickyBoxBounded st = (IStickyBoxBounded)t;
					idToStickyBoxMap.put(st.getID(), st.getStickyBox());
				}
			}
			initialized = true;
		}
	}
	
	public void init(){
		init(getBNAComponent().getModel());
	}
	
	public void bnaModelChanged(BNAModelEvent evt){
		synchronized(idToStickyBoxMap){
			if(!initialized){
				init(evt.getSource());
			}
			if(evt.getEventType() == BNAModelEvent.THING_ADDED){
				Thing addedThing = evt.getTargetThing();
				if(addedThing instanceof IStickyBoxBounded){
					IStickyBoxBounded st = (IStickyBoxBounded)addedThing;
					Rectangle oldStickyBox = (Rectangle)idToStickyBoxMap.get(st.getID());
					Rectangle newStickyBox = st.getStickyBox();
					idToStickyBoxMap.put(st.getID(), newStickyBox);
					fireStickyBoxChangedEvent(st, oldStickyBox, newStickyBox);
				}
			}
			else if(evt.getEventType() == BNAModelEvent.THING_REMOVED){
				if(idToStickyBoxMap.get(evt.getTargetThing().getID()) != null){
					Rectangle oldStickyBox = (Rectangle)idToStickyBoxMap.get(evt.getTargetThing().getID());
					idToStickyBoxMap.remove(evt.getTargetThing().getID());
					fireStickyBoxChangedEvent(evt.getTargetThing(), oldStickyBox, null);
				}
			}
			else if(evt.getEventType() == BNAModelEvent.THING_CHANGED){
				Thing changedThing = evt.getTargetThing();
				if(changedThing instanceof IStickyBoxBounded){
					IStickyBoxBounded st = (IStickyBoxBounded)changedThing;
					
					Rectangle oldStickyBox = (Rectangle)idToStickyBoxMap.get(st.getID());
					Rectangle newStickyBox = st.getStickyBox();
					if((oldStickyBox == null) || (!oldStickyBox.equals(newStickyBox))){
						idToStickyBoxMap.put(st.getID(), newStickyBox);
						fireStickyBoxChangedEvent(evt.getTargetThing(), oldStickyBox, newStickyBox);
					}
				}
			}
		}
	}
	
}
package edu.uci.ics.bna.logic;

import java.awt.Point;
import java.util.*;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.BNAModel;
import edu.uci.ics.bna.BNAModelEvent;
import edu.uci.ics.bna.IAnchored;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.ThingLogicAdapter;

public class AnchorPointTrackingLogic extends ThingLogicAdapter{

	protected Map idToAnchorPointMap = Collections.synchronizedMap(new HashMap());
	protected boolean initialized = false;

	protected Vector anchorPointTrackingListeners = new Vector();
	
	public void addAnchorPointTrackingListener(AnchorPointTrackingListener l){
		anchorPointTrackingListeners.addElement(l);
	}
	
	public void removeAnchorPointTrackingListener(AnchorPointTrackingListener l){
		anchorPointTrackingListeners.removeElement(l);
	}
	
	protected void fireAnchorPointChangedEvent(Thing targetThing, Point oldAnchorPoint, Point newAnchorPoint){
		List l = null;
		synchronized(anchorPointTrackingListeners){
			l = new ArrayList(anchorPointTrackingListeners);
		}
		AnchorPointChangedEvent evt = new AnchorPointChangedEvent(this, targetThing, oldAnchorPoint, newAnchorPoint);
		
		for(int i = 0; i < l.size(); i++){
			((AnchorPointTrackingListener)l.get(i)).anchorPointChanged(evt);
		}
	}

	protected void init(BNAModel m){
		synchronized(idToAnchorPointMap){
			for(Iterator it = m.getThingIterator(); it.hasNext(); ){
				Thing t = (Thing)it.next();
				if(t instanceof IAnchored){
					IAnchored st = (IAnchored)t;
					idToAnchorPointMap.put(st.getID(), st.getAnchorPoint());
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
			if(addedThing instanceof IAnchored){
				IAnchored st = (IAnchored)addedThing;
				Point oldAnchorPoint = (Point)idToAnchorPointMap.get(st.getID());
				Point newAnchorPoint = st.getAnchorPoint();
				idToAnchorPointMap.put(st.getID(), newAnchorPoint);
				fireAnchorPointChangedEvent(st, oldAnchorPoint, newAnchorPoint);
			}
		}
		else if(evt.getEventType() == BNAModelEvent.THING_REMOVED){
			if(idToAnchorPointMap.get(evt.getTargetThing().getID()) != null){
				Point oldAnchorPoint = (Point)idToAnchorPointMap.get(evt.getTargetThing().getID());
				idToAnchorPointMap.remove(evt.getTargetThing().getID());
				fireAnchorPointChangedEvent(evt.getTargetThing(), oldAnchorPoint, null);
			}
		}
		else if(evt.getEventType() == BNAModelEvent.THING_CHANGED){
			Thing changedThing = evt.getTargetThing();
			if(changedThing instanceof IAnchored){
				IAnchored st = (IAnchored)changedThing;
				
				Point oldAnchorPoint = (Point)idToAnchorPointMap.get(st.getID());
				Point newAnchorPoint = st.getAnchorPoint();
				
				boolean anchorPointChanged = false;
				if((oldAnchorPoint == null) && (newAnchorPoint == null)){
					anchorPointChanged = false;
				}
				else if((oldAnchorPoint == null) && (newAnchorPoint != null)){
					anchorPointChanged = true;
				}
				else if((oldAnchorPoint != null) && (newAnchorPoint == null)){
					anchorPointChanged = true;
				}
				else{
					anchorPointChanged = !oldAnchorPoint.equals(newAnchorPoint); 
				}
				
				if(anchorPointChanged){
					idToAnchorPointMap.put(st.getID(), newAnchorPoint);
					fireAnchorPointChangedEvent(evt.getTargetThing(), oldAnchorPoint, newAnchorPoint);
				}
			}
		}
	}
	
}
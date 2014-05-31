package edu.uci.ics.bna.logic;

import java.util.*;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.BNAModel;
import edu.uci.ics.bna.BNAModelEvent;
import edu.uci.ics.bna.ISelectable;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.ThingLogicAdapter;

public class SelectionTrackingLogic extends ThingLogicAdapter{

	protected Set selectedThingIDsSet = Collections.synchronizedSet(new HashSet());
	protected boolean initialized = false;

	protected Vector selectionTrackingListeners = new Vector();
	
	public void addSelectionTrackingListener(SelectionTrackingListener l){
		selectionTrackingListeners.addElement(l);
	}
	
	public void removeSelectionTrackingListener(SelectionTrackingListener l){
		selectionTrackingListeners.removeElement(l);
	}
	
	protected void fireSelectionChangedEvent(int eventType, Thing targetThing){
		List l = null;
		synchronized(selectionTrackingListeners){
			l = new ArrayList(selectionTrackingListeners);
		}
		SelectionChangedEvent evt = new SelectionChangedEvent(this, eventType, targetThing);
		
		for(int i = 0; i < l.size(); i++){
			((SelectionTrackingListener)l.get(i)).selectionChanged(evt);
		}
	}

	protected void init(BNAModel m){
		synchronized(selectedThingIDsSet){
			for(Iterator it = m.getThingIterator(); it.hasNext(); ){
				Thing t = (Thing)it.next();
				if(t instanceof ISelectable){
					ISelectable st = (ISelectable)t;
					if(st.isSelected()){
						selectedThingIDsSet.add(st.getID());
					}
				}
			}
			initialized = true;
		}
	}
	
	public void init(){
		this.init(getBNAComponent().getModel());
	}
	
	public void bnaModelChanged(BNAModelEvent evt){
		if(!initialized){
			init(evt.getSource());
		}
		if(evt.getEventType() == BNAModelEvent.THING_ADDED){
			Thing addedThing = evt.getTargetThing();
			if(addedThing instanceof ISelectable){
				ISelectable st = (ISelectable)addedThing;
				if(st.isSelected()){
					selectedThingIDsSet.add(st.getID());
					fireSelectionChangedEvent(SelectionChangedEvent.THING_SELECTED, st);
				}
			}
		}
		else if(evt.getEventType() == BNAModelEvent.THING_REMOVED){
			if(selectedThingIDsSet.contains(evt.getTargetThing().getID())){
				selectedThingIDsSet.remove(evt.getTargetThing().getID());
				fireSelectionChangedEvent(SelectionChangedEvent.THING_DESELECTED, evt.getTargetThing());
			}
		}
		else if(evt.getEventType() == BNAModelEvent.THING_CHANGED){
			Thing changedThing = evt.getTargetThing();
			if(changedThing instanceof ISelectable){
				ISelectable st = (ISelectable)changedThing;
				if(st.isSelected()){
					if(!selectedThingIDsSet.contains(st.getID())){
						selectedThingIDsSet.add(st.getID());
						fireSelectionChangedEvent(SelectionChangedEvent.THING_SELECTED, st);
					}
				}
				else{
					if(selectedThingIDsSet.contains(st.getID())){
						selectedThingIDsSet.remove(st.getID());
						fireSelectionChangedEvent(SelectionChangedEvent.THING_DESELECTED, st);
					}
				}
			}
		}
	}
	
	public String[] getSelectedThingIDs(){
		return (String[])selectedThingIDsSet.toArray(new String[0]);
	}
	
	public Thing[] getSelectedThings(){
		synchronized(selectedThingIDsSet){
			Thing[] ta = new Thing[selectedThingIDsSet.size()];
			
			int i = 0;
			for(Iterator it = selectedThingIDsSet.iterator(); it.hasNext(); ){
				String id = (String)it.next();
				ta[i] = bnaComponent.getModel().getThing(id);
				i++;
			}
			return ta;
		}
	}
}

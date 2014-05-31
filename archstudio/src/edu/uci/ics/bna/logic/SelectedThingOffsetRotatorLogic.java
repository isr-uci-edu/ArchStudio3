package edu.uci.ics.bna.logic;

import java.util.Iterator;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.BNAModelEvent;
import edu.uci.ics.bna.IOffset;
import edu.uci.ics.bna.ISelectable;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.ThingLogicAdapter;

public class SelectedThingOffsetRotatorLogic extends ThingLogicAdapter implements SelectionTrackingListener{

	protected SelectionTrackingLogic stl = null;
	protected OffsetRotatorLogic rotator = null;
	
	public SelectedThingOffsetRotatorLogic(OffsetRotatorLogic rotator, SelectionTrackingLogic stl){
		this.rotator = rotator;
		this.stl = stl;
		stl.addSelectionTrackingListener(this);
	}

	public void destroy(){
		stl.removeSelectionTrackingListener(this);
	}
	
	public void selectionChanged(SelectionChangedEvent evt){
		if(evt.getEventType() == SelectionChangedEvent.THING_SELECTED){
			Thing t = evt.getTargetThing();
			if(t instanceof IOffset){
				if(rotator != null){
					rotator.addThingWithOffset((IOffset)t);
				}
			}
		}
		else if(evt.getEventType() == SelectionChangedEvent.THING_DESELECTED){
			Thing t = evt.getTargetThing();
			if(t instanceof IOffset){
				if(rotator != null){
					rotator.removeThingWithOffset((IOffset)t);
				}
			}
		}
	}
	
	public void init(){
		for(Iterator it = getBNAComponent().getModel().getThingIterator(); it.hasNext(); ){
			Thing t = (Thing)it.next();
			if((t instanceof ISelectable) && (t instanceof IOffset)){
				if(((ISelectable)t).isSelected()){
					if(rotator != null){
						rotator.addThingWithOffset((IOffset)t);
					}
				}
			}
		}
	}

	public void bnaModelChanged(BNAModelEvent evt){
		if(evt.getEventType() == BNAModelEvent.THING_ADDED){
			Thing t = evt.getTargetThing();
			if(t != null){
				if((t instanceof ISelectable) && (t instanceof IOffset)){
					if(((ISelectable)t).isSelected()){
						if(rotator != null){
							rotator.addThingWithOffset((IOffset)t);
						}
					}
				}
			}
		}
		else if(evt.getEventType() == BNAModelEvent.THING_REMOVING){
			Thing t = evt.getTargetThing();
			if(t != null){
				if((t instanceof ISelectable) && (t instanceof IOffset)){
					if(rotator != null){
						rotator.removeThingWithOffset((IOffset)t);
					}
				}
			}
		}
	}
}

package edu.uci.ics.bna.logic;

import java.util.Iterator;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.BNAModelEvent;
import edu.uci.ics.bna.DotMarqueeThing;
import edu.uci.ics.bna.IOffset;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.ThingLogicAdapter;

public class DotMarqueeOffsetRotatorLogic extends ThingLogicAdapter{

	protected OffsetRotatorLogic rotator = null;
	
	public DotMarqueeOffsetRotatorLogic(OffsetRotatorLogic rotator){
		this.rotator = rotator;
	}
	
	public void init(){
		for(Iterator it = getBNAComponent().getModel().getThingIterator(); it.hasNext(); ){
			Thing t = (Thing)it.next();
			if(t instanceof DotMarqueeThing){
				rotator.addThingWithOffset((IOffset)t);
			}
		}
	}
	
	public void bnaModelChanged(BNAModelEvent evt){
		if(evt.getEventType() == BNAModelEvent.THING_ADDED){
			Thing t = evt.getTargetThing();
			if(t != null){
				if(t instanceof DotMarqueeThing){
					if(rotator != null){
						rotator.addThingWithOffset((IOffset)t);
					}
				}
			}
		}
		else if(evt.getEventType() == BNAModelEvent.THING_REMOVING){
			Thing t = evt.getTargetThing();
			if(t != null){
				if(t instanceof DotMarqueeThing){
					if(rotator != null){
						rotator.removeThingWithOffset((IOffset)t);
					}
				}
			}
		}
	}

}

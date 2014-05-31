
package edu.uci.ics.bna.logic;

import edu.uci.ics.bna.TagThing;
import edu.uci.ics.bna.Thing;

public class TagThingTrackingLogic extends ThingTrackingLogic{
	public boolean isTracking(Thing t){
		return (t instanceof TagThing);
	}
}

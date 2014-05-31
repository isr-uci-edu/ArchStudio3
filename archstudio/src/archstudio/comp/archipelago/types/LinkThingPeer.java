package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;
import edu.uci.ics.bna.*;

public class LinkThingPeer extends SplineThingPeer{
	
	public LinkThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof LinkThing)){
			throw new IllegalArgumentException("LinkThingPeer can only peer for LinkThing");
		}
	}
}
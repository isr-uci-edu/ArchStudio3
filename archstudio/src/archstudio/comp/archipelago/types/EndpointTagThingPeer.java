package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;
import edu.uci.ics.bna.*;

public class EndpointTagThingPeer extends TagThingPeer{
	
	private EndpointTagThing t;
	
	public EndpointTagThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof EndpointTagThing)){
			throw new IllegalArgumentException("EndpointTagThingPeer can only peer for EndpointTagThing");
		}
		this.t = (EndpointTagThing)t;
	}

}
package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;
import edu.uci.ics.bna.*;

public class InterfaceTypeThingPeer extends EndpointThingPeer{
	
	public InterfaceTypeThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof InterfaceTypeThing)){
			throw new IllegalArgumentException("InterfaceTypeThingPeer can only peer for InterfaceTypeThing");
		}
	}
}
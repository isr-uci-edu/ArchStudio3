package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;
import edu.uci.ics.bna.*;

public class InterfaceThingPeer extends EndpointThingPeer{
	
	public InterfaceThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof InterfaceThing)){
			throw new IllegalArgumentException("InterfaceThingPeer can only peer for InterfaceThing");
		}
	}
}
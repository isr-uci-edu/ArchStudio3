package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;
import edu.uci.ics.bna.*;

public class InterfaceInterfaceMappingThingPeer extends MappingSplineThingPeer{
	
	private InterfaceInterfaceMappingThing t;
	
	public InterfaceInterfaceMappingThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof InterfaceInterfaceMappingThing)){
			throw new IllegalArgumentException("InterfaceInterfaceMappingThingPeer can only peer for InterfaceInterfaceMappingThing");
		}
		this.t = (InterfaceInterfaceMappingThing)t;
	}

}
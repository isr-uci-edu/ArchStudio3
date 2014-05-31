package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;
import edu.uci.ics.bna.*;

public class SignatureInterfaceMappingThingPeer extends MappingSplineThingPeer{
	
	private SignatureInterfaceMappingThing t;
	
	public SignatureInterfaceMappingThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof SignatureInterfaceMappingThing)){
			throw new IllegalArgumentException("SignatureInterfaceMappingThingPeer can only peer for SignatureInterfaceMappingThing");
		}
		this.t = (SignatureInterfaceMappingThing)t;
	}

}
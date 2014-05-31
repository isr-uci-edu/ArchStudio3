package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;

import edu.uci.ics.bna.*;

public class SignatureThingPeer extends EndpointThingPeer{
	
	public SignatureThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof SignatureThing)){
			throw new IllegalArgumentException("SignatureThingPeer can only peer for SignatureThing");
		}
	}
	
}
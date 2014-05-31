package edu.uci.ics.bna;

import archstudio.comp.archipelago.*;

import java.awt.*;
import edu.uci.ics.bna.*;

public class EnvironmentPropertiesThingPeer extends NoThingPeer{
	
	private EnvironmentPropertiesThing t;
	
	public EnvironmentPropertiesThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof EnvironmentPropertiesThing)){
			throw new IllegalArgumentException("EnvironmentPropertiesThingPeer can only peer for EnvironmentPropertiesThing");
		}
		this.t = (EnvironmentPropertiesThing)t;
	}
	

}
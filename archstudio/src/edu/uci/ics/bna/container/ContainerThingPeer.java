package edu.uci.ics.bna.container;

import archstudio.comp.archipelago.*;

import java.awt.*;

import javax.swing.JComponent;

import edu.uci.ics.bna.*;

public class ContainerThingPeer extends BoxThingPeer{
	
	private ContainerThing t;
	
	public ContainerThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof ContainerThing)){
			throw new IllegalArgumentException("ComponentThingPeer can only peer for ComponentThing");
		}
		this.t = (ContainerThing)t;
	}



}
package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;

import edu.uci.ics.bna.*;

public class ComponentTypeThing extends BrickTypeThing{
	
	public ComponentTypeThing(){
		super();
		this.setColor(Color.CYAN);
	}
	
	public ComponentTypeThing(ComponentTypeThing copyMe){
		super(copyMe);
	}
	
	public Class getPeerClass(){
		return ComponentTypeThingPeer.class;
	}

}
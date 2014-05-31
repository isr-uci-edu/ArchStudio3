package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;

import edu.uci.ics.bna.*;

public class ComponentTypeThingPeer extends BoxThingPeer{
	
	private ComponentTypeThing t;
	
	protected Font defaultFont = null;
	
	public Font getFont(){
		if(defaultFont != null){
			return defaultFont;
		}
		return super.getFont();
	}

	public ComponentTypeThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof ComponentTypeThing)){
			throw new IllegalArgumentException("ComponentTypeThingPeer can only peer for ComponentTypeThing");
		}
		this.t = (ComponentTypeThing)t;
		defaultFont = (Font)c.getProperty("defaultFont");
	}
	

}
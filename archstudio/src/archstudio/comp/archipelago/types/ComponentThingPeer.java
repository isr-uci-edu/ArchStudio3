package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;
import edu.uci.ics.bna.*;

public class ComponentThingPeer extends BoxThingPeer{
	
	private ComponentThing t;
	
	protected Font defaultFont = null;
	
	public Font getFont(){
		if(defaultFont != null){
			return defaultFont;
		}
		return super.getFont();
	}
	
	public ComponentThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof ComponentThing)){
			throw new IllegalArgumentException("ComponentThingPeer can only peer for ComponentThing");
		}
		this.t = (ComponentThing)t;
		defaultFont = (Font)c.getProperty("defaultFont");
	}

}
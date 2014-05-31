package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;

import edu.uci.ics.bna.*;

public class ConnectorThingPeer extends BoxThingPeer{
	
	private ConnectorThing t;
	
	protected Font defaultFont = null;
	
	public Font getFont(){
		if(defaultFont != null){
			return defaultFont;
		}
		return super.getFont();
	}

	public ConnectorThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof ConnectorThing)){
			throw new IllegalArgumentException("ConnectorThingPeer can only peer for ConnectorThing");
		}
		this.t = (ConnectorThing)t;
		defaultFont = (Font)c.getProperty("defaultFont");
	}

	
}
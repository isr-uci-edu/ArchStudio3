package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;

import edu.uci.ics.bna.*;

public class ConnectorTypeThingPeer extends BoxThingPeer{
	
	private ConnectorTypeThing t;
	
	protected Font defaultFont = null;
	
	public Font getFont(){
		if(defaultFont != null){
			return defaultFont;
		}
		return super.getFont();
	}

	public ConnectorTypeThingPeer(BNAComponent c, Thing t){
		super(c, t);
		if(!(t instanceof ConnectorTypeThing)){
			throw new IllegalArgumentException("ConnectorTypeThingPeer can only peer for ConnectorTypeThing");
		}
		this.t = (ConnectorTypeThing)t;
		defaultFont = (Font)c.getProperty("defaultFont");
	}
	
}
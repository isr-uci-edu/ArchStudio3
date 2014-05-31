package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;

import edu.uci.ics.bna.*;

public class ConnectorThing extends BrickThing{

	public static final Color DEFAULT_COLOR = Color.MAGENTA;
	public static final Color DEFAULT_TRIM_COLOR = Color.BLACK;

	public ConnectorThing(){
		super();
		this.setColor(DEFAULT_COLOR);
		this.setTrimColor(DEFAULT_TRIM_COLOR);
	}
	
	public Class getPeerClass(){
		return ConnectorThingPeer.class;
	}
	
}
package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;

import edu.uci.ics.bna.*;

public class ComponentThing extends BrickThing{

	public static final Color DEFAULT_COLOR = Color.CYAN;
	public static final Color DEFAULT_TRIM_COLOR = Color.BLACK;

	public ComponentThing(){
		super();
		this.setColor(DEFAULT_COLOR);
		this.setTrimColor(DEFAULT_TRIM_COLOR);
		//this.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
	}
	
	public Class getPeerClass(){
		return ComponentThingPeer.class;
	}
}

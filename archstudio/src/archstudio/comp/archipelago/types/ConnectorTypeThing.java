package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;

import edu.uci.ics.bna.*;

public class ConnectorTypeThing extends BrickTypeThing implements IHasXadlType{
	
	public ConnectorTypeThing(){
		super();
		this.setColor(Color.MAGENTA);
	}
	
	public ConnectorTypeThing(ConnectorTypeThing copyMe){
		super(copyMe);
	}
	
	public Class getPeerClass(){
		return ConnectorTypeThingPeer.class;
	}
	
}
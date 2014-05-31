package archstudio.comp.archipelago.types;

import edu.uci.ics.xarchutils.ObjRef;

public abstract class BrickMappingLogicAdapter implements BrickMappingLogicListener {

	public void componentUpdating(ObjRef brickRef, ComponentThing ct){}
	public void componentUpdated(ObjRef brickRef, ComponentThing ct) {}
	public void componentRemoving(ObjRef brickRef, ComponentThing ct){}
	public void componentRemoved(ObjRef brickRef, ComponentThing ct){}

	public void connectorUpdating(ObjRef brickRef, ConnectorThing ct){}
	public void connectorUpdated(ObjRef brickRef, ConnectorThing ct){}
	public void connectorRemoving(ObjRef brickRef, ConnectorThing ct){}
	public void connectorRemoved(ObjRef brickRef, ConnectorThing ct){}

}

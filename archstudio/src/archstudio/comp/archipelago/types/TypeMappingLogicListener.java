package archstudio.comp.archipelago.types;

import edu.uci.ics.xarchutils.ObjRef;

public interface TypeMappingLogicListener{

	public void componentTypeUpdating(ObjRef brickTypeRef, ComponentTypeThing ct);
	public void componentTypeUpdated(ObjRef brickTypeRef, ComponentTypeThing ct);
	public void componentTypeRemoving(ObjRef brickTypeRef, ComponentTypeThing ct);
	public void componentTypeRemoved(ObjRef brickTypeRef, ComponentTypeThing ct);
	
	public void connectorTypeUpdating(ObjRef brickTypeRef, ConnectorTypeThing ct);
	public void connectorTypeUpdated(ObjRef brickTypeRef, ConnectorTypeThing ct);
	public void connectorTypeRemoving(ObjRef brickTypeRef, ConnectorTypeThing ct);
	public void connectorTypeRemoved(ObjRef brickTypeRef, ConnectorTypeThing ct);

	public void interfaceTypeUpdating(ObjRef interfaceTypeRef, InterfaceTypeThing it);
	public void interfaceTypeUpdated(ObjRef interfaceTypeRef, InterfaceTypeThing it);
	public void interfaceTypeRemoving(ObjRef interfaceTypeRef, InterfaceTypeThing it);
	public void interfaceTypeRemoved(ObjRef interfaceTypeRef, InterfaceTypeThing it);
}

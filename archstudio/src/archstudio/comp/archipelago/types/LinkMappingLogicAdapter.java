package archstudio.comp.archipelago.types;

import edu.uci.ics.xarchutils.ObjRef;

public abstract class LinkMappingLogicAdapter implements LinkMappingLogicListener{

	public void linkUpdating(ObjRef linkRef, LinkThing lt){}
	public void linkUpdated(ObjRef linkRef, LinkThing lt){}
	public void linkRemoving(ObjRef linkRef, LinkThing lt){}
	public void linkRemoved(ObjRef linkRef, LinkThing lt){}

}

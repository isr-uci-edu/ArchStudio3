package archstudio.comp.archipelago;

import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.floatingtable.TableData;
import edu.uci.ics.xarchutils.ObjRef;

public interface PropertyTablePlugin {
	public void addProperties(Thing t, ObjRef thingRef, TableData td);
}

package archstudio.comp.archipelago;

import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.floatingtable.*;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchFlatInterface;

public abstract class AbstractPropertyTablePlugin implements PropertyTablePlugin{

	protected XArchFlatInterface xarch;
	protected TwoColumnTableUtils tableUtils;
	
	public AbstractPropertyTablePlugin(XArchFlatInterface xarch){
		this.tableUtils = new TwoColumnTableUtils(TableThemes.DEFAULT_THEME);
		this.xarch = xarch;
	}
	
	public abstract void addProperties(Thing t, ObjRef thingRef, TableData td);

}

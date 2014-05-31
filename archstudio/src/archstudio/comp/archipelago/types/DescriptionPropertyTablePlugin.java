package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.floatingtable.*;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchFlatInterface;

public class DescriptionPropertyTablePlugin extends AbstractPropertyTablePlugin implements PropertyTablePlugin{

	public DescriptionPropertyTablePlugin(XArchFlatInterface xarch){
		super(xarch);
	}

	public void addProperties(Thing t, ObjRef thingRef, TableData td){
		String description = XadlUtils.getDescription(xarch, thingRef);
		if(description != null){
			td.addRow(tableUtils.createHeaderRow(description));
		}
	}

}

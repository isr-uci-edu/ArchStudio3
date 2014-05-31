package archstudio.comp.archipelago.options;

import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.floatingtable.*;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchFlatInterface;
import archstudio.comp.archipelago.AbstractPropertyTablePlugin;
import archstudio.comp.booleannotation.IBooleanNotation;

public class OptionsPropertyTablePlugin extends AbstractPropertyTablePlugin{

	protected IBooleanNotation bni;

	public OptionsPropertyTablePlugin(XArchFlatInterface xarch, IBooleanNotation bni){
		super(xarch);
		this.bni = bni;
	}
	
	public void addProperties(Thing t, ObjRef thingRef, TableData td){
		try{
			ObjRef optionalRef = (ObjRef)xarch.get(thingRef, "optional");
			if(optionalRef != null){
				ObjRef guardRef = (ObjRef)xarch.get(optionalRef, "guard");
				if(guardRef != null){
					td.addRow(tableUtils.createSubheadRow("Optional"));
					String guardString = bni.booleanGuardToString(optionalRef);
					if(guardString == null){
						guardString = "(None)";
					}
					td.addRow(tableUtils.createBodyRow("Guard", guardString));
				}
			}
		}
		catch(Exception e){
			return;
		}
	}
}

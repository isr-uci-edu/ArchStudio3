package archstudio.comp.archipelago.types;

import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.floatingtable.*;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchFlatInterface;
import archstudio.comp.archipelago.AbstractPropertyTablePlugin;

public class StructurePropertyTablePlugin extends AbstractPropertyTablePlugin{

	public StructurePropertyTablePlugin(XArchFlatInterface xarch){
		super(xarch);
	}
	
	public void addProperties(Thing t, ObjRef thingRef, TableData td){
		if(t instanceof BrickThing){
			String kindOfThing = "(Unknown)";
			if(t instanceof ComponentThing){
				kindOfThing = "Component";
			}
			else if(t instanceof ConnectorThing){
				kindOfThing = "Connector";
			}
			td.addRow(tableUtils.createSubheadRow(kindOfThing));
		}
		else if(t instanceof InterfaceThing){
			String kindOfThing = "Interface";
			td.addRow(tableUtils.createSubheadRow(kindOfThing));
		}
		else if(t instanceof LinkThing){
			String kindOfThing = "Link";
			td.addRow(tableUtils.createSubheadRow(kindOfThing));
		}
		
		if((t instanceof ComponentThing) ||
		(t instanceof ConnectorThing) ||
		(t instanceof InterfaceThing)){
			String typeDescription = "(No Type)";
			ObjRef typeRef = XadlUtils.resolveXLink(xarch, thingRef, "type");
			if(typeRef != null){
				typeDescription = XadlUtils.getDescription(xarch, typeRef);
				if(typeDescription == null){
					typeDescription = "(Invalid)";
				}
			}
			td.addRow(tableUtils.createBodyRow("Type", typeDescription));
		}
	}

}

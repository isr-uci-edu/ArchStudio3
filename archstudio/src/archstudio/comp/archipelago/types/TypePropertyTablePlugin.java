package archstudio.comp.archipelago.types;

import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.floatingtable.*;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchFlatInterface;
import archstudio.comp.archipelago.AbstractPropertyTablePlugin;

public class TypePropertyTablePlugin extends AbstractPropertyTablePlugin{

	public TypePropertyTablePlugin(XArchFlatInterface xarch){
		super(xarch);
	}
	
	public void addProperties(Thing t, ObjRef thingRef, TableData td){
		if(t instanceof BrickTypeThing){
			String kindOfThing = "(Unknown)";
			if(t instanceof ComponentTypeThing){
				kindOfThing = "Component Type";
			}
			else if(t instanceof ConnectorTypeThing){
				kindOfThing = "Connector Type";
			}
			td.addRow(tableUtils.createSubheadRow(kindOfThing));
		}
		else if(t instanceof SignatureThing){
			String kindOfThing = "Signature";
			td.addRow(tableUtils.createSubheadRow(kindOfThing));
		}
		else if(t instanceof InterfaceTypeThing){
			String kindOfThing = "Interface Type";
			td.addRow(tableUtils.createSubheadRow(kindOfThing));
		}
		
		if(t instanceof SignatureThing){
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

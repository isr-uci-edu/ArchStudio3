package archstudio.comp.archipelago.variants;

import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.floatingtable.*;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchFlatInterface;
import archstudio.comp.archipelago.AbstractPropertyTablePlugin;
import archstudio.comp.archipelago.types.BrickTypeThing;
import archstudio.comp.booleannotation.IBooleanNotation;

public class VariantsPropertyTablePlugin extends AbstractPropertyTablePlugin{

	protected IBooleanNotation bni;

	public VariantsPropertyTablePlugin(XArchFlatInterface xarch, IBooleanNotation bni){
		super(xarch);
		this.bni = bni;
	}
	
	public void addProperties(Thing t, ObjRef thingRef, TableData td){
		if(t instanceof BrickTypeThing){
			try{
				ObjRef[] variantRefs = xarch.getAll(thingRef, "variant");
				if((variantRefs != null) && (variantRefs.length > 0)){
					td.addRow(tableUtils.createSubheadRow("Variant"));
					
					for(int i = 0; i < variantRefs.length; i++){
						String variantTypeDescription = "(Unknown Type)";
						ObjRef variantTypeRef = XadlUtils.resolveXLink(xarch, variantRefs[i], "variantType");
						if(variantTypeRef != null){
							variantTypeDescription = XadlUtils.getDescription(xarch, variantTypeRef);
							if(variantTypeDescription == null){
								variantTypeDescription = "(Unknown Type)";
							}
						}
						
						ObjRef guardRef = (ObjRef)xarch.get(variantRefs[i], "guard");
						String guardString = bni.booleanGuardToString(variantRefs[i]);
						if(guardString == null){
							guardString = "(No Guard)";
						}
						
						int vtNum = i + 1;
						td.addRows(tableUtils.createMultiValueBodyRows("Variant" + vtNum, new String[]{variantTypeDescription, guardString}));
					}
				}
			}
			catch(Exception e){
				return;
			}
		}
	}
}

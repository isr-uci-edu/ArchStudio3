package archstudio.comp.archipelago.types.et;

import java.util.*;

import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchBulkQuery;
import edu.uci.ics.xarchutils.XArchBulkQueryResultProxy;
import edu.uci.ics.xarchutils.XArchBulkQueryResults;
import edu.uci.ics.xarchutils.XArchFlatInterface;
import edu.uci.ics.xarchutils.XArchFlatQueryInterface;

public class EnclosingTypeUtils{

	public static ObjRef[] getEnclosingTypes(XArchFlatInterface xarch, 
	ObjRef xArchRef, ObjRef archStructureRef){
		ObjRef typesContextRef = xarch.createContext(xArchRef, "types");

		XArchBulkQuery q = new XArchBulkQuery(xArchRef);
		q.addQueryPath("archStructure*/id");
		
		q.addQueryPath("archTypes/componentType*/id");
		q.addQueryPath("archTypes/componentType*/description/value");
		q.addQueryPath("archTypes/componentType*/subArchitecture/archStructure/type");
		q.addQueryPath("archTypes/componentType*/subArchitecture/archStructure/href");

		q.addQueryPath("archTypes/connectorType*/id");
		q.addQueryPath("archTypes/connectorType*/description/value");
		q.addQueryPath("archTypes/connectorType*/subArchitecture/archStructure/type");
		q.addQueryPath("archTypes/connectorType*/subArchitecture/archStructure/href");
		
		XArchBulkQueryResults qr = xarch.bulkQuery(q);
		XArchFlatQueryInterface xarchbulk = new XArchBulkQueryResultProxy(xarch, qr);
		
		List enclosingTypesList = new ArrayList();

		String structureID = XadlUtils.getID(xarch, archStructureRef);
		if(structureID != null){
			ObjRef archTypesRef = xarchbulk.getElement(typesContextRef, "archTypes", xArchRef);
			if(archTypesRef != null){
				ObjRef[] componentTypeRefs = xarchbulk.getAll(archTypesRef, "componentType");
				ObjRef[] connectorTypeRefs = xarchbulk.getAll(archTypesRef, "connectorType");
				for(int i = 0; i < componentTypeRefs.length + connectorTypeRefs.length; i++){
					ObjRef typeRef = (i < componentTypeRefs.length) ? componentTypeRefs[i] : connectorTypeRefs[i - componentTypeRefs.length];
					ObjRef subArchitectureRef = (ObjRef)xarchbulk.get(typeRef, "subArchitecture");
					if(subArchitectureRef != null){
						ObjRef enclosingType = XadlUtils.resolveXLink(xarchbulk, subArchitectureRef, "archStructure");
						if(enclosingType != null){
							String enclosingTypeID = XadlUtils.getID(xarchbulk, enclosingType);
							if((enclosingTypeID != null) && (enclosingTypeID.equals(structureID))){
								enclosingTypesList.add(typeRef);
							}
						}
					}
				}
			}
		}
		return (ObjRef[])enclosingTypesList.toArray(new ObjRef[0]);
		
	}
}

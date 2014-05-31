package archstudio.comp.archipelago;

import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;

import edu.uci.ics.bna.*;
import edu.uci.ics.xarchutils.*;

import java.util.*;

public class ThingIDMap{
	
	protected Map xarchReftoThingIDMap = Collections.synchronizedMap(new HashMap());
	protected Map thingIDtoXarchRefMap = Collections.synchronizedMap(new HashMap());
	
	public void clearMaps(){
		xarchReftoThingIDMap.clear();
		thingIDtoXarchRefMap.clear();
	}
	
	public void mapRefToID(ObjRef ref, String thingID){
		if(ref == null) throw new NullPointerException();
		if(thingID == null) throw new NullPointerException();
		xarchReftoThingIDMap.remove(ref);
		thingIDtoXarchRefMap.remove(thingID);
		xarchReftoThingIDMap.put(ref, thingID);
		thingIDtoXarchRefMap.put(thingID, ref);
	}
	
	public void unmapRef(ObjRef ref) {
		if(ref == null) throw new NullPointerException();
		String thingID = (String) xarchReftoThingIDMap.remove(ref);
		if (thingID != null)
			thingIDtoXarchRefMap.remove(thingID);
	}
	
	public String getThingID(ObjRef ref){
		return (String)xarchReftoThingIDMap.get(ref);
	}
	
	public ObjRef getXArchRef(String thingID){
		return (ObjRef)thingIDtoXarchRefMap.get(thingID);
	}

	public String toString(){
		return "ThingIDMap{xarchRefToThingIDMap=" + xarchReftoThingIDMap + ";thingIDtoXarchRefMap=" + thingIDtoXarchRefMap + "}";	
	}
}

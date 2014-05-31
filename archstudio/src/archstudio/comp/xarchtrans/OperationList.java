package archstudio.comp.xarchtrans;

import java.util.*;
import edu.uci.ics.xarchutils.*;

class OperationList{

	public ArrayList list;
	
	public OperationList(){
		list = new ArrayList();
	}

	public void recordAdd(ObjRef baseObjectRef, String typeOfThing, ObjRef thingToAddRef){
		ArrayList cmdList = new ArrayList();
		cmdList.add("add1");
		cmdList.add(baseObjectRef);
		cmdList.add(typeOfThing);
		cmdList.add(thingToAddRef);
		list.add(cmdList);
	}

	public void recordAdd(ObjRef baseObjectRef, String typeOfThing, ObjRef[] thingsToAddRefs){
		ArrayList cmdList = new ArrayList();
		cmdList.add("add2");
		cmdList.add(baseObjectRef);
		cmdList.add(typeOfThing);
		cmdList.add(thingsToAddRefs);
		list.add(cmdList);		
	}
	
	public void recordClear(ObjRef baseObjectRef, String typeOfThing){
		ArrayList cmdList = new ArrayList();
		cmdList.add("clear");
		cmdList.add(baseObjectRef);
		cmdList.add(typeOfThing);
		list.add(cmdList);
	}
	
	public void recordRemove(ObjRef baseObjectRef, String typeOfThing, ObjRef thingToRemove){
		ArrayList cmdList = new ArrayList();
		cmdList.add("remove1");
		cmdList.add(baseObjectRef);
		cmdList.add(typeOfThing);
		cmdList.add(thingToRemove);
		list.add(cmdList);
	}
	
	public void recordRemove(ObjRef baseObjectRef, String typeOfThing, ObjRef[] thingsToRemove){
		ArrayList cmdList = new ArrayList();
		cmdList.add("remove2");
		cmdList.add(baseObjectRef);
		cmdList.add(typeOfThing);
		cmdList.add(thingsToRemove);
		list.add(cmdList);
	}
	
	public void recordSet(ObjRef baseObjectRef, String typeOfThing, String value){
		ArrayList cmdList = new ArrayList();
		cmdList.add("set1");
		cmdList.add(baseObjectRef);
		cmdList.add(typeOfThing);
		cmdList.add(value);
		list.add(cmdList);
	}
	
	public void recordSet(ObjRef baseObjectRef, String typeOfThing, ObjRef value){
		ArrayList cmdList = new ArrayList();
		cmdList.add("set2");
		cmdList.add(baseObjectRef);
		cmdList.add(typeOfThing);
		cmdList.add(value);
		list.add(cmdList);
	}
	
	public void recordPromoteTo(ObjRef contextObjectRef, String promotionTarget, ObjRef targetObjectRef){
		ArrayList cmdList = new ArrayList();
		cmdList.add("promoteTo");
		cmdList.add(contextObjectRef);
		cmdList.add(promotionTarget);
		cmdList.add(targetObjectRef);
		list.add(cmdList);
	}
	
	public void recordRecontextualize(ObjRef contextObject, String typeOfThing, ObjRef targetObject){
		ArrayList cmdList = new ArrayList();
		cmdList.add("recontextualize");
		cmdList.add(contextObject);
		cmdList.add(typeOfThing);
		cmdList.add(targetObject);
		list.add(cmdList);
	}

	public void playbackList(XArchFlatInterface xarch){
		for(Iterator it = list.iterator(); it.hasNext(); ){
			ArrayList cmdList = (ArrayList)it.next();
			String cmdName = (String)cmdList.get(0);
			if(cmdName.equals("add1")){
				xarch.add((ObjRef)cmdList.get(1), (String)cmdList.get(2), (ObjRef)cmdList.get(3));
			}
			else if(cmdName.equals("add2")){
				xarch.add((ObjRef)cmdList.get(1), (String)cmdList.get(2), (ObjRef[])cmdList.get(3));
			}
			else if(cmdName.equals("clear")){
				xarch.clear((ObjRef)cmdList.get(1), (String)cmdList.get(2));
			}
			else if(cmdName.equals("remove1")){
				xarch.remove((ObjRef)cmdList.get(1), (String)cmdList.get(2), (ObjRef)cmdList.get(3));
			}
			else if(cmdName.equals("remove2")){
				xarch.remove((ObjRef)cmdList.get(1), (String)cmdList.get(2), (ObjRef[])cmdList.get(3));
			}
			else if(cmdName.equals("set1")){
				xarch.set((ObjRef)cmdList.get(1), (String)cmdList.get(2), (String)cmdList.get(3));
			}
			else if(cmdName.equals("set2")){
				xarch.set((ObjRef)cmdList.get(1), (String)cmdList.get(2), (ObjRef)cmdList.get(3));
			}
			else if(cmdName.equals("promoteTo")){
				xarch.promoteTo((ObjRef)cmdList.get(1), (String)cmdList.get(2), (ObjRef)cmdList.get(3));
			}
			else if(cmdName.equals("recontextualize")){
				xarch.recontextualize((ObjRef)cmdList.get(1), (String)cmdList.get(2), (ObjRef)cmdList.get(3));
			}
		}
	}

}

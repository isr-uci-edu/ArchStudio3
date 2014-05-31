package archstudio.comp.xarchtrans;

import edu.uci.ics.xarchutils.*;

public interface XArchFlatTransactionsInterface extends XArchFlatInterface{

	public Transaction createTransaction(ObjRef xArchRef);
	public void commit(Transaction t);
	public void rollback(Transaction t);
	
	public void add(Transaction t, ObjRef baseObjectRef, String typeOfThing, ObjRef thingToAddRef);
	public void add(Transaction t, ObjRef baseObjectRef, String typeOfThing, ObjRef[] thingsToAddRefs);
	public void clear(Transaction t, ObjRef baseObjectRef, String typeOfThing);
	public void remove(Transaction t, ObjRef baseObjectRef, String typeOfThing, ObjRef thingToRemove);
	public void remove(Transaction t, ObjRef baseObjectRef, String typeOfThing, ObjRef[] thingsToRemove);
	public void set(Transaction t, ObjRef baseObjectRef, String typeOfThing, String value);
	public void set(Transaction t, ObjRef baseObjectRef, String typeOfThing, ObjRef value);
	public void promoteTo(Transaction t, ObjRef contextObjectRef, String promotionTarget, ObjRef targetObjectRef);
	public void recontextualize(Transaction t, ObjRef contextObject, String typeOfThing, ObjRef targetObject);


}

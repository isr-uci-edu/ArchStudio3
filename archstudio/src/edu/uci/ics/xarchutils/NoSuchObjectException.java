package edu.uci.ics.xarchutils;

public class NoSuchObjectException extends RuntimeException{

	private ObjRef ref;
	
	public NoSuchObjectException(ObjRef objRef){
		this.ref = objRef;
	}
	
	public String toString(){
		if(ref == null){
			return "Attempt to de-reference a null object reference.";
		}
		return "No such object with identifier: " + ref.toString();
	}

}


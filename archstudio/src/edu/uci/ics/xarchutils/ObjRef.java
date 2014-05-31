package edu.uci.ics.xarchutils;

public final class ObjRef implements java.io.Serializable{

	private static long nextUidNum = 1000;
	
	private int hc;
	private String uid;
	
	public ObjRef(){
		uid = "obj" + nextUidNum++;
		hc = uid.hashCode();
	}
	
	public ObjRef(String uid){
		this.uid = uid;
		hc = uid.hashCode();
	}
	
	public String getUID(){
		return uid;
	}
	
	public ObjRef duplicate(){
		return new ObjRef(uid);
	}
	
	public int hashCode(){
		return hc;
	}
	
	public boolean equals(Object otherObjRef){
		//if(otherObjRef == null){
		//	throw new NullPointerException();
		//}
		
		if(!(otherObjRef instanceof ObjRef)){
			return false;
		}
		
		return ((ObjRef)otherObjRef).uid.equals(uid);
	}

	public String toString(){
		return "ObjRef[" + uid + "]";
	}
}


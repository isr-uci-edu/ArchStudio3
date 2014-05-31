package edu.uci.ics.xarchutils;

public class XArchFileEvent implements java.io.Serializable{

	public static final int XARCH_CREATED_EVENT = 50;
	public static final int XARCH_OPENED_EVENT = 100;
	public static final int XARCH_CLOSED_EVENT = 200;
	public static final int XARCH_SAVED_EVENT = 300;
	public static final int XARCH_RENAMED_EVENT = 400;
	
	private int eventType;
	private String url;
	private String asurl = null;
	private ObjRef xArchRef = null;
	
	public XArchFileEvent(int eventType, String url){
		this.eventType = eventType;
		this.url = url;
	}
	
	public XArchFileEvent(int eventType, String url, String asURL){
		this.eventType = eventType;
		this.url = url;
		this.asurl = asURL;
	}
	
	public XArchFileEvent(int eventType, String url, ObjRef xArchRef){
		this.eventType = eventType;
		this.url = url;
		this.xArchRef = xArchRef;
	}
	
	public XArchFileEvent(int eventType, String url, String asURL, ObjRef xArchRef){
		this.eventType = eventType;
		this.url = url;
		this.asurl = asURL;
		this.xArchRef = xArchRef;
	}
	
	public XArchFileEvent(XArchFileEvent eventToCopy, ObjRef xArchRef){
		this.eventType = eventToCopy.eventType;
		this.url = eventToCopy.url;
		this.asurl = eventToCopy.asurl;
		this.xArchRef = xArchRef;
	}
	
	public int getEventType(){
		return eventType;
	}
	
	public String getURL(){
		return url;
	}
	
	public String getAsURL(){
		return asurl;
	}
	
	public ObjRef getXArchRef(){
		return xArchRef;
	}

}


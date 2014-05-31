package edu.uci.ics.xarchutils;

import java.util.*;

class ContextCache {

	static class ContextCacheKey{
		private ObjRef xArchRef;
		private String contextKind;
		
		public ContextCacheKey(ObjRef xArchRef, String contextKind){
			this.xArchRef = xArchRef;
			this.contextKind = contextKind;
		}
		
		public ObjRef getXArchRef(){
			return xArchRef;
		}
		
		public String getContextKind(){
			return contextKind;
		}
		
		public int hashCode(){
			return xArchRef.hashCode() ^ contextKind.hashCode();
		}
		
		public boolean equals(Object o){
			if(!(o instanceof ContextCacheKey)){
				return false;
			}
			ContextCacheKey cck = (ContextCacheKey)o;
			return cck.xArchRef.equals(xArchRef) && cck.contextKind.equals(contextKind);
		}
	}

	private Map map = Collections.synchronizedMap(new HashMap()); 

	public ContextCache(){
		super();
	}
	
	public void put(ObjRef xArchRef, String contextKind, ObjRef contextRef){
		map.put(new ContextCacheKey(xArchRef, contextKind), contextRef);
	}
	
	public ObjRef get(ObjRef xArchRef, String contextKind){
		return (ObjRef)map.get(new ContextCacheKey(xArchRef, contextKind));
	}
	
	public void removeAll(ObjRef xArchRef){
		ArrayList doomedEntries = new ArrayList();
		for(Iterator it = map.keySet().iterator(); it.hasNext(); ){
			ContextCacheKey cck = (ContextCacheKey)it.next();
			if(cck.getXArchRef().equals(xArchRef)){
				doomedEntries.add(cck);
			}
		}
		for(Iterator it = doomedEntries.iterator(); it.hasNext(); ){
			ContextCacheKey cck = (ContextCacheKey)it.next();
			map.remove(cck);
		}
		
	}

}

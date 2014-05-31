package edu.uci.ics.xarchutils;

import java.util.*;

import edu.uci.isr.xarch.XArchInstanceMetadata;
import edu.uci.isr.xarch.XArchTypeMetadata;

public class XArchBulkQueryResultProxy implements XArchFlatQueryInterface{

	protected boolean cachesBuilt = false;
	protected Map refToNodeMap = new HashMap();
	protected Map idToNodeMap = new HashMap();
	
	protected XArchFlatQueryInterface realxarch;
	protected XArchBulkQueryResults qr;

	protected ObjRef xArchRef;
	
	public XArchBulkQueryResultProxy(XArchFlatQueryInterface realxarch, XArchBulkQueryResults qr){
		if(realxarch == null){
			throw new IllegalArgumentException("A backing XArchFlatQueryInterface is required for this proxy to maintain proper semantics.");
		}
		this.realxarch = realxarch;
		this.qr = qr;
		this.xArchRef = qr.getXArchRef();
		checkAndBuildCaches();
	}
	
	private XArchBulkQueryResultNode getNode(ObjRef ref){
		return (XArchBulkQueryResultNode)refToNodeMap.get(ref);
	}
	
	private XArchBulkQueryResultNode getNode(String refID){
		return (XArchBulkQueryResultNode)idToNodeMap.get(refID);
	}	

	public void setRealStore(XArchFlatQueryInterface realXArch){
		this.realxarch = realXArch;
	}
	
	public XArchFlatQueryInterface getRealStore(){
		return this.realxarch;
	}

	private void checkAndBuildCaches(){
		if(!cachesBuilt){
			cacheNode(qr);
			cachesBuilt = true;
		}
	}
	
	private void cacheNode(XArchBulkQueryResultNode node){
		if(node.getNodeType() == XArchBulkQueryResultNode.NODETYPE_OBJREF){
			refToNodeMap.put(node.getObjRef(), node);
			String id = node.getRefID();
			if(id != null){
				idToNodeMap.put(id, node);
			}
		}
		XArchBulkQueryResultNode[] children = node.getChildren();
		for(int i = 0; i < children.length; i++){
			cacheNode(children[i]);
		}
	}

	public Object get(ObjRef baseRef, String tagName){
		//checkAndBuildCaches();
		tagName = XArchFlatImpl.capFirstLetter(tagName);

		XArchBulkQueryResultNode baseNode = getNode(baseRef);
		//System.out.println("baseNode = " + baseNode);
		XArchBulkQueryResultNode[] children = baseNode.getChildren(tagName);
		if(children.length == 0){
			return null; 
		}
		int nodeType = children[0].getNodeType();
		if(children[0].getNodeType() == XArchBulkQueryResultNode.NODETYPE_OBJREF){
			return children[0].getObjRef(); 
		}
		else if(children[0].getNodeType() == XArchBulkQueryResultNode.NODETYPE_STRING){
			return children[0].getString();
		}
		else{
			//this shouldn't happen
			throw new RuntimeException("Invalid node type in result tree"); 
		}
	}
	
	public ObjRef get(ObjRef baseObjectRef, String typeOfThing, String id){
		//checkAndBuildCaches();
		String tagName = XArchFlatImpl.capFirstLetter(typeOfThing);
		XArchBulkQueryResultNode baseNode = getNode(baseObjectRef);
		XArchBulkQueryResultNode[] children = baseNode.getChildren(tagName);
		
		if(children.length == 0){
			return null;
		}
		
		for(int i = 0; i < children.length; i++){
			int nodeType = children[0].getNodeType();
			if(children[i].getNodeType() == XArchBulkQueryResultNode.NODETYPE_OBJREF){
				String refID = children[i].getRefID();
				if((refID != null) && (refID.equals(id))){
					return children[i].getObjRef();
				}
			}
		}
		return null;
	}

	public ObjRef[] get(ObjRef baseObjectRef, String typeOfThing, String[] ids) {
		//checkAndBuildCaches();
		String tagName = XArchFlatImpl.capFirstLetter(typeOfThing);
		XArchBulkQueryResultNode baseNode = getNode(baseObjectRef);
		XArchBulkQueryResultNode[] children = baseNode.getChildren(tagName);
		
		ArrayList matchingRefs = new ArrayList();
		
		if(children.length == 0){
			return new ObjRef[0];
		}
		
		for(int i = 0; i < children.length; i++){
			int nodeType = children[0].getNodeType();
			if(children[i].getNodeType() == XArchBulkQueryResultNode.NODETYPE_OBJREF){
				String refID = children[i].getRefID();
				if(refID != null){
					for(int j = 0; j < ids.length; j++){
						if(refID.equals(ids[j])){
							matchingRefs.add(children[i].getObjRef());
						}
					}
				}
			}
		}
		return (ObjRef[])matchingRefs.toArray(new ObjRef[0]);
	}

	
	public ObjRef[] getAll(ObjRef baseRef, String tagName){
		//checkAndBuildCaches();
		tagName = XArchFlatImpl.capFirstLetter(tagName);
		XArchBulkQueryResultNode baseNode = getNode(baseRef);
		XArchBulkQueryResultNode[] children = baseNode.getChildren(tagName);
		
		if(children.length == 0){
			return new ObjRef[0]; 
		}
		
		ObjRef[] refs = new ObjRef[children.length];
		for(int i = 0; i < refs.length; i++){
			int nodeType = children[0].getNodeType();
			if(children[i].getNodeType() == XArchBulkQueryResultNode.NODETYPE_OBJREF){
				refs[i] = children[i].getObjRef(); 
			}
			else if(children[i].getNodeType() == XArchBulkQueryResultNode.NODETYPE_STRING){
				//this shouldn't happen
				throw new IllegalArgumentException("Cannot call getAll on a string type."); 
			}
			else{
				//this shouldn't happen
				throw new RuntimeException("Invalid node type in result tree"); 
			}
		}
		return refs;
	}
	
	public ObjRef getXArch(ObjRef baseRef){
		if(getNode(baseRef) != null){
			return xArchRef;
		}
		else{
			return null;
		}
	}
	
	public ObjRef getParent(ObjRef baseRef){
		//checkAndBuildCaches();
		XArchBulkQueryResultNode baseNode = getNode(baseRef);
		if(baseNode == null){
			throw new RuntimeException("Base reference does not exist in bulk query results: " + baseRef);
		}
		XArchBulkQueryResultNode parentNode = baseNode.getParent();
		if(parentNode != null){
			if(parentNode.getNodeType() == XArchBulkQueryResultNode.NODETYPE_OBJREF){
				return parentNode.getObjRef();
			}
			else{
				//this shouldn't happen
				throw new RuntimeException("Invalid node type in result tree"); 
			}
		}
		return null;
	}
	
	public boolean hasAncestor(ObjRef childRef, ObjRef ancestorRef){
		//checkAndBuildCaches();
		ObjRef currentRef = childRef;
		while(true){
			if(currentRef == null){
				return false;
			}
			if(currentRef.equals(ancestorRef)){
				return true;
			}
			currentRef = getParent(currentRef);
		}
	}

	public ObjRef[] getAllAncestors(ObjRef baseRef){
		ArrayList l = new ArrayList();
		l.add(baseRef);
		while(true){
			ObjRef parent = getParent(baseRef);
			if(parent != null){
				l.add(parent);
				baseRef = parent;
			}
			else{
				return (ObjRef[])l.toArray(new ObjRef[0]);
			}
		}
	}

	public XArchTypeMetadata getTypeMetadata(ObjRef baseRef){
		XArchBulkQueryResultNode baseNode = getNode(baseRef);
		return baseNode.getTypeMetadata();
	}
	
	public XArchInstanceMetadata getInstanceMetadata(ObjRef baseRef){
		XArchBulkQueryResultNode baseNode = getNode(baseRef);
		return baseNode.getInstanceMetadata();
	}
	
	public String getType(ObjRef baseRef){
		//checkAndBuildCaches();
		XArchBulkQueryResultNode baseNode = getNode(baseRef);
		return baseNode.getRefClass().getName();
	}

	public boolean isInstanceOf(ObjRef baseRef, String className){
		//checkAndBuildCaches();
		XArchBulkQueryResultNode baseNode = getNode(baseRef);
		try{
			Class c = Class.forName(className);
			return (c.isAssignableFrom(baseNode.getRefClass()));
		}
		catch(ClassNotFoundException e){
			throw new IllegalArgumentException("Invalid class name: " + className);
		}
	}

	public ObjRef getByID(ObjRef xArchRef, String id){
		if(xArchRef.equals(this.xArchRef)){
			XArchBulkQueryResultNode node = (XArchBulkQueryResultNode)idToNodeMap.get(id);
			if(node == null){
				if(realxarch != null){
					return realxarch.getByID(xArchRef, id);
				}
				else{
					return null;
				}
			}
			else{
				return node.getObjRef();
			}
		}
		else{
			return realxarch.getByID(xArchRef, id);
		}
	}
	
	public ObjRef getByID(String id){
		XArchBulkQueryResultNode node = (XArchBulkQueryResultNode)idToNodeMap.get(id);
		if(node == null){
			if(realxarch != null){
				return realxarch.getByID(xArchRef, id);
			}
			else{
				return null;
			}
		}
		else{
			return node.getObjRef();
		}
	}
	
	public String getElementName(ObjRef baseRef){
		XArchBulkQueryResultNode node = (XArchBulkQueryResultNode)refToNodeMap.get(baseRef);
		if(node == null){
			if(realxarch != null){
				return realxarch.getElementName(baseRef);
			}
			else{
				return null;
			}
		}
		else{
			return node.getTagName();
		}
	}

	public XArchPath getXArchPath(ObjRef baseRef){
		XArchBulkQueryResultNode node = getNode(baseRef);
		if(node == null){
			if(realxarch != null){
				return realxarch.getXArchPath(baseRef);
			}
			else{
				return null;
			}
		}
		else{
			return node.getXArchPath();
		}
	}

	public ObjRef getElement(ObjRef contextObjectRef, String typeOfThing, ObjRef xArchObjectRef){
		typeOfThing = XArchFlatImpl.capFirstLetter(typeOfThing);

		ObjRef[] objectRefs = getAll(xArchObjectRef, typeOfThing);
		if(objectRefs.length == 0) return null;
		else return objectRefs[0];
		
	}

	public ObjRef[] getAllElements(ObjRef contextObjectRef, String typeOfThing, ObjRef xArchObjectRef){
		typeOfThing = XArchFlatImpl.capFirstLetter(typeOfThing);
		
		//This works because of how the query processor processes xArch objects...
		ObjRef[] objectRefs = getAll(xArchObjectRef, typeOfThing);
		return objectRefs;
	}

	public boolean isAttached(ObjRef childRef){
		return hasAncestor(childRef, xArchRef);
	}

	public boolean isEqual(ObjRef baseObjectRef, ObjRef thingToCheck){
		XArchBulkQueryResultNode baseNode = getNode(baseObjectRef);
		XArchBulkQueryResultNode targetNode = getNode(thingToCheck);
		String baseID = baseNode.getRefID();
		String targetID = targetNode.getRefID();
		if(baseID == null){
			return false;
		}
		if(targetID == null){
			return false;
		}
		
		return baseID.equals(targetID);
	}

	public boolean isEquivalent(ObjRef baseObjectRef, ObjRef thingToCheck){
		return realxarch.isEquivalent(baseObjectRef, thingToCheck);
	}

	public ObjRef resolveHref(ObjRef xArchRef, String href){
		if(xArchRef == null){
			throw new IllegalArgumentException("xArchRef is null in resolveHref");
		}
		if(href == null){
			return null;
		}
		if(!xArchRef.equals(this.xArchRef)){
			return realxarch.resolveHref(xArchRef, href);
		}
		href = href.trim();
		if(href.startsWith("#")){
			String targetID = href.substring(1);
			//This will go to the backing store if it 
			//fails to find it in the bulk results.
			return getByID(targetID);
		}
		else{
			return realxarch.resolveHref(xArchRef, href);
		}
	}

	public ObjRef resolveXArchPath(ObjRef xArchRef, XArchPath xArchPath){
		return realxarch.resolveXArchPath(xArchRef, xArchPath);
	}

	public ObjRef[] getReferences(ObjRef xArchRef, String id){
		return realxarch.getReferences(xArchRef, id);
	}
	
	public boolean has(ObjRef baseObjectRef, String typeOfThing, ObjRef valueToCheck){
		//checkAndBuildCaches();
		typeOfThing = XArchFlatImpl.capFirstLetter(typeOfThing);
		ObjRef[] childRefs = getAll(baseObjectRef, typeOfThing);
		for(int i = 0; i < childRefs.length; i++){
			if(childRefs[i].equals(valueToCheck)){
				return true;
			}
		}
		return false;
	}

	public boolean[] has(ObjRef baseObjectRef, String typeOfThing, ObjRef[] thingsToCheck){
		//checkAndBuildCaches();
		typeOfThing = XArchFlatImpl.capFirstLetter(typeOfThing);
		boolean[] results = new boolean[thingsToCheck.length];
		for(int i = 0; i < thingsToCheck.length; i++){
			results[i] = has(baseObjectRef, typeOfThing, thingsToCheck[i]);
		}
		return results;
	}

	public boolean has(ObjRef baseObjectRef, String typeOfThing, String valueToCheck){
		//checkAndBuildCaches();
		typeOfThing = XArchFlatImpl.capFirstLetter(typeOfThing);
		String childValue = (String)get(baseObjectRef, typeOfThing);
		if(childValue == null){
			if(valueToCheck == null){
				return true;
			}
			return false;
		}
		return childValue.equals(valueToCheck);
	}

	public boolean hasAll(ObjRef baseObjectRef, String typeOfThing,	ObjRef[] valuesToCheck){
		//checkAndBuildCaches();
		typeOfThing = XArchFlatImpl.capFirstLetter(typeOfThing);
		for(int i = 0; i < valuesToCheck.length; i++){
			boolean result = has(baseObjectRef, typeOfThing, valuesToCheck[i]);
			if(result == false){
				return false;
			}
		}
		return true;
	}


}

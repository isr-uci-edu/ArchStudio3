package edu.uci.ics.xarchutils;

import java.util.*;

import edu.uci.isr.xarch.XArchInstanceMetadata;
import edu.uci.isr.xarch.XArchTypeMetadata;

class XArchBulkQueryResultNode {

	public static final int NODETYPE_OBJREF = 100;
	public static final int NODETYPE_STRING = 200;

	protected String tagName;
	protected XArchBulkQueryResultNode parent;
	protected List children;

	protected ObjRef ref;
	protected XArchTypeMetadata typeMetadata;
	protected XArchInstanceMetadata instanceMetadata;
	protected Class refClass;
	protected String string;
	protected String refID;
	protected XArchPath xArchPath;
	
	protected int nodeType;

	public XArchBulkQueryResultNode(String tagName, ObjRef ref, XArchTypeMetadata typeMetadata, XArchInstanceMetadata instanceMetadata, Class refClass, String refID, XArchPath xArchPath){
		nodeType = NODETYPE_OBJREF;
		this.tagName = XArchFlatImpl.capFirstLetter(tagName);
		this.ref = ref;
		this.typeMetadata = typeMetadata;
		this.instanceMetadata = instanceMetadata;
		this.refClass = refClass;
		this.refID = refID;
		this.string = null;
		this.xArchPath = xArchPath;
		parent = null;
		children = new ArrayList();
	}
	
	public XArchBulkQueryResultNode(String tagName, String string){
		nodeType = NODETYPE_STRING;
		this.tagName = XArchFlatImpl.capFirstLetter(tagName);
		this.ref = null;
		this.typeMetadata = null;
		this.instanceMetadata = null;
		this.refClass = null;
		this.string = string;
		this.xArchPath = null;
		parent = null;
		children = new ArrayList();
	}

	public XArchPath getXArchPath(){
		return this.xArchPath;
	}

	public String getRefID(){
		return refID;
	}
	
	public XArchTypeMetadata getTypeMetadata() {
		return typeMetadata;
	}

	public XArchInstanceMetadata getInstanceMetadata() {
		return instanceMetadata;
	}

	public int getNodeType(){
		return nodeType;
	}

	public String getTagName(){
		return tagName;
	}
	
	private void setParent(XArchBulkQueryResultNode parent){
		this.parent = parent;
	}
	
	public XArchBulkQueryResultNode getParent(){
		return parent;
	}
	
	public void addChild(XArchBulkQueryResultNode child){
		synchronized(children){
			children.add(child);
			child.setParent(this);
		}
	}
	
	public void removeChild(XArchBulkQueryResultNode child){
		synchronized(children){
			children.remove(child);
			child.setParent(null);
		}
	}
	
	public XArchBulkQueryResultNode[] getChildren(String tagName){
		synchronized(children){
			//tagName = XArchFlatImpl.capFirstLetter(tagName);
			ArrayList cl = new ArrayList();
			for(Iterator it = children.iterator(); it.hasNext(); ){
				XArchBulkQueryResultNode child = (XArchBulkQueryResultNode)it.next();
				if(child.getTagName().equals(tagName)){
					cl.add(child);
				}
			}
			return (XArchBulkQueryResultNode[])cl.toArray(new XArchBulkQueryResultNode[0]);
		}
	}
	
	public XArchBulkQueryResultNode[] getChildren(){
		return (XArchBulkQueryResultNode[])children.toArray(new XArchBulkQueryResultNode[0]);
	}
	
	public ObjRef getObjRef(){
		return ref;
	}
	
	public Class getRefClass(){
		return refClass;
	}
	
	public String getString(){
		//System.out.println("call!");
		return string;
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(tagName);
		sb.append("=");
		if(getNodeType() == NODETYPE_OBJREF){
			sb.append(ref);
			sb.append(";class=");
			sb.append(refClass);
			sb.append(";id=");
			sb.append(refID);
		}
		else if(getNodeType() == NODETYPE_STRING){
			sb.append(string);
		}
		sb.append(";children={");
		XArchBulkQueryResultNode[] children = getChildren();
		for(int i = 0; i < children.length; i++){
			if(i != 0){
				sb.append(",");
			}
			sb.append(children[i].toString());
		}
		sb.append("};");
		return sb.toString();
	}
}

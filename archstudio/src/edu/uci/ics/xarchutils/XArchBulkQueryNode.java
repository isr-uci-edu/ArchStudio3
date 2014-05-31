package edu.uci.ics.xarchutils;

import java.util.*;

class XArchBulkQueryNode implements java.io.Serializable{
	protected XArchBulkQueryNode parent;
	protected List children;

	protected String tagName;
	protected boolean isPlural;

	public XArchBulkQueryNode(String tagName, boolean isPlural){
		this.tagName = XArchFlatImpl.capFirstLetter(tagName);
		this.isPlural = isPlural;
		parent = null;
		children = new ArrayList();
	}
	
	private void setParent(XArchBulkQueryNode parent){
		this.parent = parent;
	}
	
	public XArchBulkQueryNode getParent(){
		return parent;
	}
	
	public void addChild(XArchBulkQueryNode child){
		children.add(child);
		child.setParent(this);
	}
	
	public void removeChild(XArchBulkQueryNode child){
		children.remove(child);
		child.setParent(null);
	}
	
	public XArchBulkQueryNode[] getChildren(){
		return (XArchBulkQueryNode[])children.toArray(new XArchBulkQueryNode[0]);
	}

	public String getTagName(){
		return tagName;
	}
	
	public boolean isPlural(){
		return isPlural;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(tagName);
		if(isPlural){
			sb.append("*");
		}
		XArchBulkQueryNode[] children = getChildren();
		if(children.length > 0){
			sb.append("/");
			if(children.length == 1){
				sb.append(children[0].toString());
			}
			else{
				sb.append("{");
				for(int i = 0; i < children.length; i++){
					if(i != 0) sb.append(",");
					sb.append(children[i].toString());
				}
				sb.append("}");
			}
		}
		return sb.toString();
	}
}

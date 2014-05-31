package edu.uci.ics.xarchutils;

import java.util.*;

public class XArchBulkQuery extends XArchBulkQueryNode implements java.io.Serializable{

	protected ObjRef queryRootRef;
	protected List children;

	public XArchBulkQuery(ObjRef queryRootRef){
		super(null, false);
		this.queryRootRef = queryRootRef;
	}
	
	public ObjRef getQueryRootRef(){
		return queryRootRef;
	}
	
	public void addQueryPath(String queryPath){
		String[] pathElements = queryPath.split("/");
		XArchBulkQueryNode currentQueryNode = null;
		for(int i = 0; i < pathElements.length; i++){
			String elt = pathElements[i].trim();
			if(elt.length() == 0){
				continue;
			}
			elt = XArchFlatImpl.capFirstLetter(elt);
			if(elt.equals("*")){
				throw new IllegalArgumentException("Path element cannot be '*'.");
			}
			XArchBulkQueryNode newQN = null;
			if(elt.endsWith("*")){
				elt = elt.substring(0, elt.length() - 1);
				newQN = new XArchBulkQueryNode(elt, true);
			}
			else{
				newQN = new XArchBulkQueryNode(elt, false);
			}
			
			boolean alreadyExists = false;
			if(currentQueryNode == null){
				currentQueryNode = this;
			}
			XArchBulkQueryNode[] existingNodes = currentQueryNode.getChildren();
			for(int j = 0; j < existingNodes.length; j++){
				if(existingNodes[j].getTagName().equals(elt)){
					alreadyExists = true;
					newQN = existingNodes[j];
					break;
				}
			}
			if(!alreadyExists){
				currentQueryNode.addChild(newQN);
			}
			currentQueryNode = newQN;
		}
	}

}

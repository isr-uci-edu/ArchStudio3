package edu.uci.ics.bna;

import java.util.*;

public class ThingTree{
	//The ThingTree isn't a proper tree.  At its base, it's a list of Nodes,
	//each of which (by default) contains one Thing.  However, each node
	//may, in addition to its basic Thing, contain a list of child Things
	//that will be drawn stacked directly atop the parent Thing in depth-first fashion
	
	protected ArrayList rootNodeList = null;
	protected int size = 0;
	
	protected ArrayList cachedList = new ArrayList();
	
	public ThingTree(){
		rootNodeList = new ArrayList();
	}
	
	public void add(Thing t){
		ThingNode newNode = new ThingNode();
		newNode.thing = t;
		rootNodeList.add(newNode);
		size++;
		updateListCache();
	}
	
	public void add(Thing t, Thing parent){
		/*
		System.out.println();
		System.out.println("Adding item.");
		System.out.println("Looking for parent: " + parent);
		System.out.println();
		System.out.println("rootlist size = " + rootNodeList.size());
		System.out.println("size = " + size);
		*/
		ThingNode parentNode = findThingNode(parent);
		if(parentNode != null){
			ThingNode newNode = new ThingNode();
			newNode.thing = t;
			parentNode.childList.add(newNode);
			size++;
		}
		else{
			System.err.println("ThingTree:add() WARNING: Parent not found.");
			System.err.println("  - Perhaps the parent has a different stacking priority?");
			/*
			System.err.println("Thing: " + t);
			System.err.println("Parent: " + parent);
			System.err.println("List: " + rootNodeList);
			*/
			if(true) throw new RuntimeException();
			
			ThingNode newNode = new ThingNode();
			newNode.thing = t;
			rootNodeList.add(newNode);
			size++;
		}
		updateListCache();
	}
	
	public void dumpThingTree(Thing t){
		ThingNode tn = findThingNode(t);
		if(tn == null) return;
		dumpThingNode(tn, 0);
	}
	
	protected void dumpThingNode(ThingNode tn, int indent){
		for(int i = 0; i < indent; i++){
			System.out.print(' ');
		}
		System.out.println(tn.thing.getID());
		for(Iterator it = tn.childList.iterator(); it.hasNext(); ){
			ThingNode childNode = (ThingNode)it.next();
			dumpThingNode(childNode, indent + 2);
		}
	}
	
	
	protected ThingNode findThingNode(Thing t){
		return findThingNode(t, rootNodeList);
	}
	
	protected ThingNode findThingNode(Thing t, ArrayList l){
		int len = l.size();
		for(int i = 0; i < len; i++){
			ThingNode n = (ThingNode)l.get(i);
			if(n.thing.equals(t)){
				return n;
			}
			else{
				ThingNode foundNode = findThingNode(t, n.childList);
				if(foundNode != null){
					return foundNode;
				}
			}
		}
		return null;
	}
	
	public boolean bringToFront(Thing thing){
		return bringToFront(thing, rootNodeList);
	}
	
	protected boolean bringToFront(Thing thing, ArrayList nodeList){
		int len = nodeList.size();
		int thingIndex = -1;
		for(int i = 0; i < len; i++){
			ThingNode n = (ThingNode)nodeList.get(i);
			if(n.thing.equals(thing)){
				thingIndex = i;
				ThingNode tn = (ThingNode)nodeList.remove(thingIndex);
				nodeList.add(nodeList.size(), tn);
				updateListCache();
				return true;
			}
		}
		
		//Didn't find it in the given list, check all the child node lists
		for(int i = 0; i < len; i++){
			ThingNode n = (ThingNode)nodeList.get(i);
			if(bringToFront(thing, n.childList)){
				return true;
			}
		}
		return false;
	}
	
	public boolean sendToBack(Thing thing){
		return sendToBack(thing, rootNodeList);
	}
	
	protected boolean sendToBack(Thing thing, ArrayList nodeList){
		int len = nodeList.size();
		int thingIndex = -1;
		for(int i = 0; i < len; i++){
			ThingNode n = (ThingNode)nodeList.get(i);
			if(n.thing.equals(thing)){
				thingIndex = i;
				ThingNode tn = (ThingNode)nodeList.remove(thingIndex);
				nodeList.add(0, tn);
				updateListCache();
				return true;
			}
		}
		
		//Didn't find it in the given list, check all the child node lists
		for(int i = 0; i < len; i++){
			ThingNode n = (ThingNode)nodeList.get(i);
			if(sendToBack(thing, n.childList)){
				return true;
			}
		}
		return false;
	}
	
	
	
	public boolean moveAfter(Thing firstThing, Thing secondThing){
		return moveAfter(firstThing, secondThing, rootNodeList);
	}
	
	protected boolean moveAfter(Thing firstThing, Thing secondThing, ArrayList nodeList){
		int len = nodeList.size();
		int firstThingIndex = -1;
		int secondThingIndex = -1;
		for(int i = 0; i < len; i++){
			ThingNode n = (ThingNode)nodeList.get(i);
			if(n.thing.equals(firstThing)){
				firstThingIndex = i;
			}
			else if(n.thing.equals(secondThing)){
				secondThingIndex = i;
			}
		}
		
		if((firstThingIndex != -1) && (secondThingIndex != -1)){
			//System.out.println("Found firstThing: " + firstThing);
			//System.out.println("Index = " + firstThingIndex);
			//System.out.println("Found secondThing: " + secondThing);
			//System.out.println("Index = " + secondThingIndex);
			//System.out.println();
			
			if(firstThingIndex < secondThingIndex){
				return true;
			}
			else{
				ThingNode secondThingNode = (ThingNode)nodeList.get(secondThingIndex);
				nodeList.remove(secondThingIndex);
				nodeList.add(secondThingNode);
				updateListCache();
				return true;
			}
		}
		
		//Didn't find it in the given list, check all the child node lists
		for(int i = 0; i < len; i++){
			ThingNode n = (ThingNode)nodeList.get(i);
			if(moveAfter(firstThing, secondThing, n.childList)){
				return true;
			}
		}
		return false;
	}
	
	public boolean contains(Thing t){
		return findThingNode(t) != null;	
	}
	
	public boolean remove(Thing t){
		return remove(t, rootNodeList);
	}
	
	//Removes just the given Thing; its children are all promoted to
	//replace the parent in-line
	protected boolean remove(Thing t, ArrayList l){
		int len = l.size();
		for(int i = 0; i < len; i++){
			ThingNode n = (ThingNode)l.get(i);
			if(n.thing.equals(t)){
				l.remove(i);
				l.addAll(i, n.childList);
				size--;
				updateListCache();
				return true;
			}
			else{
				if(remove(t, n.childList)){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean removeWithChildren(Thing t){
		return removeWithChildren(t, rootNodeList);
	}
	
	//Removes just the given Thing; its children are removed too
	protected boolean removeWithChildren(Thing t, ArrayList l){
		int len = l.size();
		for(int i = 0; i < len; i++){
			ThingNode n = (ThingNode)l.get(i);
			if(n.thing.equals(t)){
				l.remove(i);
				size -= (getChildCount(n) + 1);
				updateListCache();
				return true;
			}
			else{
				if(removeWithChildren(t, n.childList)){
					return true;
				}
			}
		}
		return false;
	}
	
	public int size(){
		return size;
	}
	
	private void updateListCache(){
		cachedList = new ArrayList(size);
		appendToThingList(cachedList, rootNodeList);
	}
	
	protected void appendToThingList(List thingList, List nodeList){
		int nodeListLen = nodeList.size();
		for(int i = 0; i < nodeListLen; i++){
			ThingNode n = (ThingNode)nodeList.get(i);
			thingList.add(n.thing);
			appendToThingList(thingList, n.childList);
		}
	}
	
	public List asList(){
		return cachedList;
	}
	
	public Iterator iterator(){
		return asList().iterator();
	}
	
	private int getChildCount(ThingNode tn){
		int childCount = tn.childList.size();
		for(int i = 0; i < tn.childList.size(); i++){
			childCount += getChildCount((ThingNode)tn.childList.get(i));
		}
		return childCount;
	}
	
	
	private class ThingNode{
		public Thing thing = null; 
		public ArrayList childList = new ArrayList();
		
		public String toString(){
			//return "ThingNode{thing=" + thing + "];";
			return "ThingNode{thing=" + thing + "; childList=" + childList +"};";
		}
	}
}

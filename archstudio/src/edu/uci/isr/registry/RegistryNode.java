package edu.uci.isr.registry;

import java.util.*;

public class RegistryNode{
	
	//A registry node may or may not have child nodes.  Each node may have 0+ RegistryEntries,
	//which are name+multi-value pairs
	
	protected String nodeName;
	//Maps entry keys to RegistryEntries
	protected Hashtable entries;
	protected Hashtable childNodes;
	
	//Creates a root registry node
	public RegistryNode(){
		this(null);
	}
	
	public RegistryNode(String nodeName){
		this.nodeName = nodeName;
		this.entries = new Hashtable();
		this.childNodes = new Hashtable();
	}
	
	public boolean isRoot(){
		return nodeName == null;
	}
	
	//Returns null if this node is the root.
	public String getName(){
		return nodeName;
	}
	
	public void putChild(RegistryNode node){
		if(node.getName() == null){
			throw new IllegalArgumentException("Can't add a root registry element as a child.");
		}
		childNodes.put(node.getName(), node);
	}
	
	public RegistryNode getChild(String name){
		return (RegistryNode)childNodes.get(name);
	}
	
	public void removeChild(String name){
		childNodes.remove(name);
	}
	
	public RegistryNode[] getAllChildren(){
		Object[] arr = childNodes.values().toArray();
		RegistryNode[] rns = new RegistryNode[arr.length];
		for(int i = 0; i < arr.length; i++){
			rns[i] = (RegistryNode)arr[i];
		}
		return rns;
	}
	
	public void removeAllChildren(){
		childNodes.clear();
	}
	
	public boolean hasChild(String name){
		return getChild(name) != null;
	}
	
	public void putEntry(RegistryEntry re){
		if(isRoot()){
			throw new IllegalArgumentException("Root elements cannot have entries.");
		}
		entries.put(re.getKey(), re);
	}
	
	public RegistryEntry getEntry(String name){
		return (RegistryEntry)entries.get(name);
	}
	
	public RegistryEntry[] getAllEntries(){
		Object[] arr = entries.values().toArray();
		RegistryEntry[] res = new RegistryEntry[arr.length];
		for(int i = 0; i < arr.length; i++){
			res[i] = (RegistryEntry)arr[i];
		}
		return res;
	}
	
	public void removeAllEntries(){
		if(isRoot()){
			throw new IllegalArgumentException("Root elements cannot have entries.");
		}
		entries.clear();
	}
	
	public boolean hasEntry(String name){
		return getEntry(name) != null;
	}
}


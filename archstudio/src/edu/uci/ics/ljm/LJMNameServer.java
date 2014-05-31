package edu.uci.ics.ljm;

import java.io.*;
import java.net.*;
import java.util.*;

public class LJMNameServer implements LJMNaming{

	public static final int DEFAULT_PORT = 0xc3c3;
	public static final String NAMING_NAME = "__NAMING";
	
	protected Hashtable nameTable;
	
	public LJMNameServer() throws IOException{
		this(DEFAULT_PORT);
	}
	
	public LJMNameServer(int port) throws IOException{
		LJMServer nameServer = new LJMServer(port);
		nameServer.deploy(NAMING_NAME, this);
		nameTable = new Hashtable();
	}
	
	public void bind(String name, LJMEndpoint where){
		//System.out.println("Bound " + where + " to " + name);
		nameTable.put(name, where);
	}
	
	public void unbind(String name){
		nameTable.remove(name);
	}
	
	public LJMEndpoint lookup(String name){
		//System.out.println("NAMESERVER: Looking up : " + name);
		//System.out.println("Found = " + (nameTable.get(name) != null));
		return (LJMEndpoint)nameTable.get(name);
	}

}


package edu.uci.isr.registry;

import java.io.*;
import java.util.*;

public class RegistryUtils{

	protected static RegistryNode registry = null;
	
	public static RegistryNode getOrCreateChild(RegistryNode registryNode, String childPath){
		RegistryNode rn = registryNode;
		for(StringTokenizer st = new StringTokenizer(childPath, "/"); st.hasMoreElements(); ){
			String token = st.nextToken().trim();
			if(!token.equals("")){
				RegistryNode child = rn.getChild(token);
				if(child == null){
					child = new RegistryNode(token);
					rn.putChild(child);
				}
				rn = child;
			}
		}
		return rn;
	}
		
	public static RegistryNode getChild(RegistryNode registryNode, String childPath){
		RegistryNode rn = registryNode;
		for(StringTokenizer st = new StringTokenizer(childPath, "/"); st.hasMoreElements(); ){
			String token = st.nextToken().trim();
			if(!token.equals("")){
				RegistryNode child = rn.getChild(token);
				if(child == null){
					return null;
				}
				rn = child;
			}
		}
		return rn;
	}
	
	protected static RegistryNode loadRegistry(String filename) throws RegistryParseException{
		File f = new File(filename);
		if(!f.exists()){
			return null;
		}
		else if(!f.canRead()){
			throw new RegistryParseException("Can't read registry file.");
		}
		RegistryNode reg = null;
		FileInputStream fis = null;
		try{
			fis = new FileInputStream(f);
			TextRegistrySerializer trs = new TextRegistrySerializer();
			reg = trs.deserialize(fis);
		}
		catch(Exception e){
			e.printStackTrace();
			throw new RegistryParseException(e.getMessage());
		}
		try{
			if(fis != null){
				fis.close();
			}
		}
		catch(IOException ioe){
		}
		return reg;
	}
	
	public static RegistryNode loadRegistry() throws RegistryParseException{
		//Looks everywhere for the registry file...
		//First, check the system properties:
		if(registry != null){
			return registry;
		}
		
		String registryFileLocation = System.getProperty("isr.registry");
		if(registryFileLocation != null){
			registry = loadRegistry(registryFileLocation);
			if(registry != null){
				return registry;
			}
		}
		
		//Next, check the user's home directory...
		registryFileLocation = System.getProperty("user.home") + System.getProperty("file.separator") + "_isr_registry.xml";
		//System.out.println("Registry file is: " + registryFileLocation);
		registry = loadRegistry(registryFileLocation);
		//System.out.println("Registry loaded from file is: " + registry);
		if(registry != null){
			return registry;
		}
		
		//Next, check the local directory
		registryFileLocation = "_isr_registry.xml";
		registry = loadRegistry(registryFileLocation);
		if(registry != null){
			return registry;
		}

		//Out of ideas here...
		return registry;
	}
	
	public static RegistryNode loadOrCreateRegistry() throws RegistryParseException{
		RegistryNode r = loadRegistry();
		if(r != null){
			return r;
		}
		registry = new RegistryNode();
		saveRegistry();
		return registry;
	}
		
	public static synchronized void saveRegistry(){
		//First, check the system properties:
		String registryFileLocation = System.getProperty("isr.registry");
		if(registryFileLocation == null){
			registryFileLocation = System.getProperty("user.home") + System.getProperty("file.separator") + "_isr_registry.xml";
		}
		File dir = new File(System.getProperty("user.home"));
		File f = new File(registryFileLocation);
		if(!dir.canWrite()){
			throw new RuntimeException("Can't write registry to file: " + f);
		}
		
		TextRegistrySerializer trs = new TextRegistrySerializer();
		String serReg = trs.serialize(registry);
		FileOutputStream fos = null;
		try{
			fos = new FileOutputStream(f);
			fos.write(serReg.getBytes());
		}
		catch(IOException ioe){
			if(fos != null){
				try{
					fos.close();
				}
				catch(IOException ioe2){
				}
			}
			throw new RuntimeException("Can't write registry to file: " + f);
		}
		try{
			fos.close();
		}
		catch(IOException ioe2){
		}
	}
}


package edu.uci.ics.ljm;

import java.io.*;
import java.lang.reflect.*;
import java.lang.reflect.Proxy;
import java.net.*;

public class LJMDeployment{

	private static int refCount = 0;
	private static LJMServer objectServer = null;
	private static LJMEndpoint thisEndpoint = null;
	
	private static void createObjectServer() throws IOException, UnknownHostException{
		if(objectServer == null){
			objectServer = new LJMServer();
			InetAddress localHost = InetAddress.getLocalHost();
			int port = objectServer.getPort();
			thisEndpoint = new LJMEndpoint(localHost, port, null);
		}
	}
	
	private static void destroyObjectServer(){
		if(objectServer != null){
			objectServer.destroy();
			objectServer = null;
			thisEndpoint = null;
		}
	}

	public static void deploy(String objectName, Object o) throws LJMException{
		deploy(objectName, o, LJMNameServer.DEFAULT_PORT);
	}
	
	public static void deploy(String objectName, Object o, String host) throws LJMException{
		deploy(objectName, o, host, LJMNameServer.DEFAULT_PORT);
	}
	
	public static void deploy(String objectName, Object o, int port) throws LJMException{
		try{
			InetAddress hostAddress = InetAddress.getLocalHost();
			LJMEndpoint nsEndpoint = new LJMEndpoint(hostAddress, port, LJMNameServer.NAMING_NAME);
			deploy(objectName, o, nsEndpoint);
		}
		catch(UnknownHostException e){
			throw new LJMException("Can't resolve local host.");
		}
	}

	public static void deploy(String objectName, Object o, String host, int port) throws LJMException{
		try{
			InetAddress hostAddress = InetAddress.getByName(host);
			LJMEndpoint nsEndpoint = new LJMEndpoint(hostAddress, port, LJMNameServer.NAMING_NAME);
			deploy(objectName, o, nsEndpoint);
		}
		catch(UnknownHostException e){
			throw new LJMException("Can't resolve remote host: " + host);
		}
	}
	
	private static void deploy(String objectName, Object o, LJMEndpoint nameServerEndpoint) throws LJMException{
		if(objectServer == null){
			try{
				createObjectServer();
			}
			catch(UnknownHostException uhe){
				throw new LJMException("Couldn't resolve local host: " + uhe.toString());
			}
			catch(IOException ioe){
				throw new LJMException("Couldn't create object server: " + ioe.toString());
			}
		}
		
		LJMProxyInvoker namingProxyInvoker = new LJMProxyInvoker(
			nameServerEndpoint);
		
		LJMNaming nameServer = (LJMNaming)Proxy.newProxyInstance(LJMNaming.class.getClassLoader(),
			new Class[] { LJMNaming.class }, namingProxyInvoker);

		
		try{
			LJMEndpoint endpt = new LJMEndpoint(thisEndpoint);
			endpt.setObjectName(objectName);
			nameServer.bind(objectName, endpt);
		}
		catch(LJMException ljme){
			//If the call failed, and we're trying to bind to a nameserver
			//on the local machine, we'll try to create a nameserver on
			//this machine to solve the problem.
			if(nameServerEndpoint.getHost().equals(thisEndpoint.getHost())){
				try{
					nameServer = new LJMNameServer(nameServerEndpoint.getPort());
					LJMEndpoint endpt = new LJMEndpoint(thisEndpoint);
					endpt.setObjectName(objectName);
					nameServer.bind(objectName, endpt);
				}
				catch(Exception e){
					if(refCount == 0){
						destroyObjectServer();
					}
					throw ljme;
				}
			}
		}
		
		objectServer.deploy(objectName, o);
		refCount++;
	}
		
	public static void undeploy(String objectName) throws LJMException{
		undeploy(objectName, LJMNameServer.DEFAULT_PORT);
	}
	
	public static void undeploy(String objectName, String host) throws LJMException{
		undeploy(objectName, host, LJMNameServer.DEFAULT_PORT);
	}
	
	public static void undeploy(String objectName, int port) throws LJMException{
		try{
			InetAddress hostAddress = InetAddress.getLocalHost();
			LJMEndpoint nsEndpoint = new LJMEndpoint(hostAddress, port, LJMNameServer.NAMING_NAME);
			undeploy(objectName, nsEndpoint);
		}
		catch(UnknownHostException e){
			throw new LJMException("Can't resolve local host.");
		}
	}

	public static void undeploy(String objectName, String host, int port) throws LJMException{
		try{
			InetAddress hostAddress = InetAddress.getByName(host);
			LJMEndpoint nsEndpoint = new LJMEndpoint(hostAddress, port, LJMNameServer.NAMING_NAME);
			undeploy(objectName, nsEndpoint);
		}
		catch(UnknownHostException e){
			throw new LJMException("Can't resolve remote host: " + host);
		}
	}
	
	private static void undeploy(String objectName, LJMEndpoint nameServerEndpoint){
		objectServer.undeploy(objectName);
		
		LJMProxyInvoker namingProxyInvoker = new LJMProxyInvoker(
			nameServerEndpoint);
		
		LJMNaming nameServer = (LJMNaming)Proxy.newProxyInstance(LJMNaming.class.getClassLoader(),
			new Class[] { LJMNaming.class }, namingProxyInvoker);

		try{
			nameServer.unbind(objectName);
		}
		catch(LJMException ljme){
			//not much we can do here.
		}
		refCount--;
		if(refCount == 0){
			destroyObjectServer();
		}
		
	}
	
}


package edu.uci.ics.ljm;

import java.net.*;
import java.lang.reflect.*;
import java.lang.reflect.Proxy;

public class LJMProxyFactory{

	protected static LJMNaming nameServer = null;
	
	public static Object createProxy(String host, String objectName, Class[] interfaceClasses) throws LJMException{
		try{
			InetAddress remoteAddress = InetAddress.getByName(host);
			return createProxy(remoteAddress, LJMNameServer.DEFAULT_PORT, objectName, interfaceClasses);
		}
		catch(UnknownHostException e){
			throw new LJMException("Invalid host name.");
		}
	}
	
	public static Object createProxy(InetAddress host, String objectName, Class[] interfaceClasses) throws LJMException{
		return createProxy(host, LJMNameServer.DEFAULT_PORT, objectName, interfaceClasses);
	}
	
	public static Object createProxy(String host, int port, String objectName, Class[] interfaceClasses) throws LJMException{
		try{
			InetAddress remoteAddress = InetAddress.getByName(host);
			return createProxy(remoteAddress, port, objectName, interfaceClasses);
		}
		catch(UnknownHostException e){
			throw new LJMException("Invalid host name.");
		}
	}
	
	public static Object createProxy(InetAddress host, int port, String objectName, Class[] interfaceClasses) throws LJMException{
		if(nameServer == null){
			connectToNameServer(host, port);
		}
		
		LJMEndpoint endpt = nameServer.lookup(objectName);
		if(endpt == null){
			throw new LJMException("Object " + objectName + " was not bound on host " + host + ".", true);
		}
		
		LJMEndpoint namingEndpoint = new LJMEndpoint(host, port, LJMNameServer.NAMING_NAME);
		
		//System.out.println("In proxyFactory; objectName was: " + objectName);
		LJMProxyInvoker proxyInvoker = new LJMProxyInvoker(namingEndpoint, objectName, interfaceClasses, endpt);
		return Proxy.newProxyInstance(LJMNaming.class.getClassLoader(),
			interfaceClasses, proxyInvoker);		
	}
	
	protected static void connectToNameServer(InetAddress host, int port) throws LJMException{
		LJMProxyInvoker namingProxyInvoker = new LJMProxyInvoker(
			new LJMEndpoint(host, port, LJMNameServer.NAMING_NAME));
		
		nameServer = (LJMNaming)Proxy.newProxyInstance(LJMNaming.class.getClassLoader(),
			new Class[] { LJMNaming.class }, namingProxyInvoker);
	}
 

}


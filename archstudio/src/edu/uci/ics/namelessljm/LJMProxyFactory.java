package edu.uci.ics.namelessljm;

import java.net.*;
import java.lang.reflect.*;
import java.lang.reflect.Proxy;

public class LJMProxyFactory{

	public static Object createProxy(String host, int port, String objectName, Class[] interfaceClasses) throws LJMException{
		//System.out.println("Creating new proxy for : " + host + ":" + port + "[" + objectName + "]");
		//new Throwable().printStackTrace();
		try{
			InetAddress remoteAddress = InetAddress.getByName(host);
			return createProxy(remoteAddress, port, objectName, interfaceClasses);
		}
		catch(UnknownHostException e){
			throw new LJMException("Invalid host name.");
		}
	}
	
	public static Object createProxy(InetAddress host, int port, String objectName, Class[] interfaceClasses) throws LJMException{
		LJMProxyInvoker proxyInvoker = new LJMProxyInvoker(objectName, interfaceClasses,
			new LJMEndpoint(host, port, objectName));
		return Proxy.newProxyInstance(LJMProxyFactory.class.getClassLoader(),
			interfaceClasses, proxyInvoker);		
	}
	
}


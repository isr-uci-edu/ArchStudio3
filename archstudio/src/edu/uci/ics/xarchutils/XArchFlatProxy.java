package edu.uci.ics.xarchutils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.w3c.dom.Node;

import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchFlatInterface;
import edu.uci.isr.xarch.DOMBased;
import edu.uci.isr.xarch.IXArchElement;
import edu.uci.isr.xarch.XArchPropertyMetadata;
import edu.uci.isr.xarch.XArchTypeMetadata;

public class XArchFlatProxy implements InvocationHandler, XArchFlatProxyInterface {
	
	private XArchFlatProxyHandleInterface handle = new XArchFlatProxyHandleInterface(){
		public ObjRef getObjRef() {
			return objRef;
		}
		
		public void setObjRef(ObjRef objRef) {
			XArchFlatProxy.this.objRef = objRef;
		}
		
		public XArchFlatInterface getXArch() {
			return xArch;
		}
		
		public void setXArch(XArchFlatInterface xArch) {
			XArchFlatProxy.this.xArch = xArch;
		}
	};
	
	private ObjRef objRef;
	private XArchFlatInterface xArch;
	
	public XArchFlatProxy(XArchFlatInterface xArch, ObjRef objRef) {
		this.xArch = xArch;
		this.objRef = objRef;
	}
	
	public String toString(){
		return objRef.toString();
//		try{
//			XArchTypeMetadata type = xArch.getTypeMetadata(objRef);
//			StringBuffer sb = new StringBuffer();
//			sb.append(objRef).append('=');
//			sb.append(type.getPackageName()).append('.').append(type.getTypeName()).append('{');
//			for (Iterator i = type.getProperties(); i.hasNext();) {
//				XArchPropertyMetadata prop = (XArchPropertyMetadata) i.next();
//				String name = prop.getName();
//				sb.append(name).append('=');
//				
//				switch(prop.getType()){
//				
//				case XArchPropertyMetadata.ATTRIBUTE:
//				case XArchPropertyMetadata.ELEMENT:
//					sb.append(xArch.get(objRef, name));
//					break;
//					
//				case XArchPropertyMetadata.ELEMENT_MANY:
//					sb.append(Arrays.asList(xArch.getAll(objRef, name)));
//					break;
//				}
//				
//				if(i.hasNext())
//					sb.append(", ");
//			}
//			sb.append('}');
//			return sb.toString();
//		}catch(RuntimeException e){
//			e.printStackTrace();
//			throw e;
//		}
	}
	
	private Object checkResult(Object result) {
		if (result instanceof ObjRef)
			return XArchFlatProxyUtils.proxy(xArch, (ObjRef) result);
		return result;
	}
	
	public Object invoke(Object proxy, Method method, Object[] args)
	throws Throwable {
		
		String methodName = method.getName();
		// System.err.println(method);
		
		if(method.getDeclaringClass().isInstance(this)) {
			
			// check this object for the method first
			return method.invoke(this, args);
			
		} else if (methodName.startsWith("add")) {
			
			if(args[0] instanceof IXArchElement){
				String typeOfThing = methodName.substring(3);
				xArch.add(objRef, typeOfThing, XArchFlatProxyUtils.getObjRef(args[0]));
				return null;
				
			} else if(args[0] instanceof Collection) {
				String typeOfThing = methodName.substring(3, methodName.length() - 1);
				xArch.add(objRef, typeOfThing, XArchFlatProxyUtils.getObjRefs((Collection)args[0]));
				return null;
			}
			
		} else if (methodName.startsWith("clear")) {
			
			String typeOfThing = methodName.substring(5);
			xArch.clear(objRef, typeOfThing);
			return null;
			
		} else if (methodName.startsWith("createContext")) {
			
			// String typeOfThing = methodName.substring(13);
			return checkResult(xArch.createContext(objRef, (String)args[0]));
			
		} else if (methodName.startsWith("create") && methodName.endsWith("Element")) {
			
			// TODO: check for create[typeOfThing-Element][!Element]
			
			String typeOfThing = methodName.substring(6, methodName.length() - 7);
			return checkResult(xArch.createElement(objRef, typeOfThing));
			
		} else if (methodName.startsWith("create")) {
			
			String typeOfThing = methodName.substring(6);
			return checkResult(xArch.create(objRef, typeOfThing));
			
		} else if (methodName.equals("getTypeMetadata")){
			
			return xArch.getTypeMetadata(objRef);
			
		} else if (methodName.equals("getInstanceMetadata")){
			
			return xArch.getInstanceMetadata(objRef);
			
		} else if (methodName.startsWith("getAll")) {
			
			String typeOfThing = methodName.substring(6, methodName.length() - 1);
			return XArchFlatProxyUtils.proxy(xArch, xArch.getAll(objRef, typeOfThing));
			
		} else if (methodName.startsWith("get")) {
			
			if(args == null || args.length == 0) {
				String typeOfThing = methodName.substring(3);
				return checkResult(xArch.get(objRef, typeOfThing));
				
			} else if(args[0] instanceof String) {
				String typeOfThing = methodName.substring(3);
				return checkResult(xArch.get(objRef, typeOfThing, (String)args[0]));
				
			} else if(args[0] instanceof Collection) {
				String typeOfThing = methodName.substring(3, methodName.length() - 1);
				return checkResult(xArch.get(objRef, typeOfThing, (String[])((Collection)args[0]).toArray()));
			}
			
		} else if (methodName.startsWith("hasAll")) {
			
			String typeOfThing = methodName.substring(6, methodName.length() - 1);
			return xArch.hasAll(objRef, typeOfThing, XArchFlatProxyUtils.getObjRefs((Collection)args[0])) ? Boolean.TRUE : Boolean.FALSE;
			
		} else if (methodName.startsWith("has")) {
			
			if(args[0] instanceof String) {
				String typeOfThing = methodName.substring(3);
				return xArch.has(objRef, typeOfThing, (String)args[0]) ? Boolean.TRUE : Boolean.FALSE;
				
			} else if(args[0] instanceof IXArchElement){
				String typeOfThing = methodName.substring(3);
				return xArch.has(objRef, typeOfThing, XArchFlatProxyUtils.getObjRef(args[0])) ? Boolean.TRUE : Boolean.FALSE;
				
			} else if(args[0] instanceof Collection) {
				String typeOfThing = methodName.substring(3, methodName.length() - 1);
				boolean[] bools = xArch.has(objRef, typeOfThing, XArchFlatProxyUtils.getObjRefs((Collection)args[0]));
				Boolean[] result = new Boolean[bools.length];
				for (int i = 0; i < bools.length; i++)
					result[i] = bools[i] ? Boolean.TRUE : Boolean.FALSE;
				return Arrays.asList(result);
			}
			
		} else if (methodName.equals("isEqual")) {
			
			return xArch.isEqual(objRef, XArchFlatProxyUtils.getObjRef(args[0])) ? Boolean.TRUE : Boolean.FALSE;
			
		} else if (methodName.equals("isEquivalent")) {
			
			return xArch.isEquivalent(objRef, XArchFlatProxyUtils.getObjRef(args[0])) ? Boolean.TRUE : Boolean.FALSE;
			
		} else if (methodName.startsWith("promoteTo")) {
			
			String typeOfThing = methodName.substring(9);
			return checkResult(xArch.promoteTo(objRef, typeOfThing, XArchFlatProxyUtils.getObjRef(args[0])));
			
		} else if (methodName.startsWith("recontextualize")) {
			
			String typeOfThing = methodName.substring(15);
			return checkResult(xArch.recontextualize(objRef, typeOfThing, XArchFlatProxyUtils.getObjRef(args[0])));
			
		} else if (methodName.startsWith("remove")) {
			
			if(args[0] instanceof IXArchElement){
				String typeOfThing = methodName.substring(6);
				xArch.remove(objRef, typeOfThing, XArchFlatProxyUtils.getObjRef(args[0]));
				return null;
				
			} else if(args[0] instanceof Collection) {
				String typeOfThing = methodName.substring(6, methodName.length() - 1);
				xArch.remove(objRef, typeOfThing, XArchFlatProxyUtils.getObjRefs((Collection)args[0]));
				return null;
			} 			
			
		} else if (methodName.startsWith("set")) {
			
			if(args[0] instanceof String) {
				String typeOfThing = methodName.substring(3);
				xArch.set(objRef, typeOfThing, (String)args[0]);
				return null;
				
			} else if(args[0] instanceof IXArchElement){
				String typeOfThing = methodName.substring(3);
				xArch.set(objRef, typeOfThing, XArchFlatProxyUtils.getObjRef(args[0]));
				return null;
			}
			
		}
		
		throw new UnsupportedOperationException("Cannot proxy " + method);
	}
	
	
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(obj instanceof ObjRef)
			return objRef.equals(obj);
		if(obj instanceof IXArchElement)
			return objRef.equals(XArchFlatProxyUtils.getObjRef(obj));
		
		return false;
	}
	
	public int hashCode() {
		return objRef.hashCode();
	}
	
	public XArchFlatProxyHandleInterface getProxyHandle(){
		return handle;
	}
}
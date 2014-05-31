package edu.uci.ics.xarchutils;

import java.lang.reflect.*;
import java.util.*;

class MethodCache{

	//Maps Class objects to Maps.  These Maps map method names (Strings) to methods.
	private Map methodMap = new HashMap();

	protected void cacheClass(Class c){
		synchronized(c){
			Map classMap = new HashMap();
			
			Method[] methods = c.getDeclaredMethods();
			for(int i = 0; i < methods.length; i++){
				classMap.put(methods[i].getName(), methods[i]);
			}
			methodMap.put(c, classMap);
		}
	}
	
	public Method getMethod(Class c, String name){
		Map classMap = (Map)methodMap.get(c);

		if(classMap == null){
			cacheClass(c);
			classMap = (Map)methodMap.get(c);
		}
		Method m = (Method)classMap.get(name);
		if(m == null){
			Class superClass = c.getSuperclass();
			if(superClass != null){
				return getMethod(superClass, name);
			}
		}
		
		return m;
	}
	
}

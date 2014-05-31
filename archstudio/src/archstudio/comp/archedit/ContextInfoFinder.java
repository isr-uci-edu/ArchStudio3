package archstudio.comp.archedit;

import java.lang.reflect.*;
import java.util.*;

import edu.uci.isr.xarch.*;

public class ContextInfoFinder{
	
	private static boolean inited = false;

	private static Hashtable promotionsTable;
	private static Vector topLevelElements;
	
	public static Collection getTopLevelElements(){
		init();
		return topLevelElements;
	}
	
	public static Collection getAvailablePromotions(Class c){
		init();
		Vector v = (Vector)promotionsTable.get(c.getName());
		if(v == null){
			try{
				String implClassName = c.getName();
				String packageName = implClassName.substring(0, implClassName.lastIndexOf(".") + 1);
				String implClassShortName = implClassName.substring(implClassName.lastIndexOf(".") + 1);
				String basicClassName = implClassShortName.substring(0, implClassShortName.length() - 4);
				String interfaceClassName = packageName + "I" + basicClassName;
				v = (Vector)promotionsTable.get(interfaceClassName);
			}
			catch(Exception e){
				v = null;
			}
		}
		
		if(v == null){
			return new Vector();
		}
		else{
			return v;
		}
	}
	
	private static String capFirstLetter(String s){
		if(s == null){
			return null;
		}
		else if(s.length() == 0){
			return "";
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append(Character.toUpperCase(s.charAt(0)));
		sb.append(s.substring(1));
		return sb.toString();
	}
	
	public static synchronized void init(){
		if(inited){
			return;
		}
		try{
			promotionsTable = new Hashtable();
			topLevelElements = new Vector();
			
			String[] packageNames = XArchUtils.getPackageNames();
			for(int p = 0; p < packageNames.length; p++){
				String shortPackageName = packageNames[p].substring(packageNames[p].lastIndexOf(".") + 1);
				String contextClassName = packageNames[p] + "." + capFirstLetter(shortPackageName) + "Context";
				Class contextClass = Class.forName(contextClassName);
				
				Method[] allMethods = contextClass.getMethods();
				for(int i = 0; i < allMethods.length; i++){
					Method m = allMethods[i];
					String methodName = m.getName();
					if(methodName.startsWith("promoteTo")){
						Class returnType = m.getReturnType();
						Class[] paramTypes = m.getParameterTypes();
						if(paramTypes.length == 1){
							addPromotion(paramTypes[0].getName(), packageNames[p], returnType.getName(), methodName.substring(9));
						}
					}
					else if(methodName.startsWith("create")){
						if(methodName.endsWith("Element")){
							Class returnType = m.getReturnType();
							Class[] paramTypes = m.getParameterTypes();
							if(paramTypes.length == 0){
								topLevelElements.addElement(new ElementInfo(packageNames[p], returnType.getName(), methodName.substring(6, methodName.length() - 7)));
							}
						}
					}
				}
			}
		}
		catch(ClassNotFoundException e){
			throw new RuntimeException(e.toString());
		}
		
		inited = true;
	}
	
	private static void addPromotion(String baseClassName, String promotionPackageName, String promotionClassName, String displayName){
		Vector v = (Vector)promotionsTable.get(baseClassName);
		if(v == null){
			v = new Vector();
			v.addElement(new ElementInfo(promotionPackageName, promotionClassName, displayName));
			promotionsTable.put(baseClassName, v);
		}
		else{
			v.addElement(new ElementInfo(promotionPackageName, promotionClassName, displayName));
		}
	}
	
	
}



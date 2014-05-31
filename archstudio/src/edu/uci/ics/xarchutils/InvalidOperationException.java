package edu.uci.ics.xarchutils;

public class InvalidOperationException extends RuntimeException{

	private Class targetClass;
	private String operationName;
	private Class[] paramList;
	
	public InvalidOperationException(String operationName){
		this.targetClass = null;
		this.operationName = operationName;
		this.paramList = null;
	}
	
	public InvalidOperationException(Class targetClass, String operationName){
		this.targetClass = targetClass;
		this.operationName = operationName;
		this.paramList = null;
	}
	
	public InvalidOperationException(Class targetClass, String operationName, Class[] paramList){
		this.targetClass = targetClass;
		this.operationName = operationName;
		this.paramList = paramList;
	}
		
	public String toString(){
		if(targetClass == null){
			return "Invalid operation: " + operationName;
		}
		if(paramList == null){
			return "Invalid operation: Could not find method " + operationName + " on class " + targetClass.getName();
		}
		StringBuffer sb = new StringBuffer();
		sb.append("Invalid operation: Could not find method: " + operationName + "(");
		for(int i = 0; i < paramList.length; i++){
			if(i > 0){
				sb.append(", ");
			}
			sb.append(paramList[i].getName());
		}
		sb.append("); on class ");
		sb.append(targetClass.getName());
		return sb.toString();
	}

}


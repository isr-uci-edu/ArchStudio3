package edu.uci.ics.xarchutils;

public class InvalidObjectTypeException extends RuntimeException{

	private Class expectedType = null;
	private Class actualType = null;
	
	public InvalidObjectTypeException(){
	}
		
	public InvalidObjectTypeException(Class expectedType, Class actualType){
		this.expectedType = expectedType;
		this.actualType = actualType;
	}
	
	public String toString(){
		if(expectedType == null){
			return "Invalid object type.";
		}
		else{
			return "Invalid object type.  Expected " + expectedType.getName() + "; encountered " + actualType.getName();
		}
	}

}


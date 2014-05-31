package edu.uci.ics.ljm;

public class LJMException extends RuntimeException implements java.io.Serializable{

	protected boolean isTransient = false;
	
	public LJMException(String description){
		super(description);
		isTransient = false;
	}
	
	public LJMException(String description, boolean isTransient){
		super(description);
		this.isTransient = isTransient;
	}
	
	public boolean isTransient(){
		return isTransient;
	}
	
	public void setTransient(boolean isTransient){
		this.isTransient = isTransient;
	}
	
	

}


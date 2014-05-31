package edu.uci.ics.namelessljm;

public class LJMException extends RuntimeException implements java.io.Serializable{

	protected boolean isTransient = false;
	protected Throwable targetException = null;
	
	public LJMException(String description){
		super(description);
		isTransient = false;
	}

	public LJMException(String description, Throwable targetException){
		super(description);
		isTransient = false;
		this.targetException = targetException;
	}		
	
	public LJMException(String description, boolean isTransient){
		super(description);
		this.isTransient = isTransient;
	}
	
	public LJMException(String description, Throwable targetException, boolean isTransient){
		super(description);
		this.targetException = targetException;
		this.isTransient = isTransient;
	}
	
	public Throwable getTargetException(){
		return targetException;
	}
	
	public boolean isTransient(){
		return isTransient;
	}
	
	public void setTransient(boolean isTransient){
		this.isTransient = isTransient;
	}
	
	

}


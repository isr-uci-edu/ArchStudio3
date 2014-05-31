package archstudio.comp.xarchtrans;

public class OperationInterruptedException extends java.lang.RuntimeException{

	private InterruptedException ie;
	
	public OperationInterruptedException(InterruptedException e){
		super((Throwable)e);
	}

}

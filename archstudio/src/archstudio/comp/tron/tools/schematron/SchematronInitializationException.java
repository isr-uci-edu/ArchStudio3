package archstudio.comp.tron.tools.schematron;

public class SchematronInitializationException extends Exception{
	public SchematronInitializationException(){
		super();
	}
	public SchematronInitializationException(String message){
		super(message);
	}
	public SchematronInitializationException(String message, Throwable cause){
		super(message, cause);
	}
	public SchematronInitializationException(Throwable cause){
		super(cause);
	}
}

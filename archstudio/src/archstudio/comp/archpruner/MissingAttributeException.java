package archstudio.comp.archpruner;


/**
 *  This exception should be thrown whenever an attribute of an element is expected but does not exist.  
 *
 *	@author Christopher Van der Westhuizen
 */
public class MissingAttributeException extends Exception implements java.io.Serializable
{
	/**
	 * Constructs a new exception with null as its detail message.
	 */
	public MissingAttributeException() {}
	
	/**
	 * Constructs a new exception with the specified detail message.
	 *
	 * @param msg the detail message. The detail message is saved for later retrieval by the Throwable.getMessage() method.
	 */
	public MissingAttributeException(String msg)
	{
		super(msg);
	}
}
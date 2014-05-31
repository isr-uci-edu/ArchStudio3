package archstudio.comp.archpruner;

/**
 * This exception should be thrown when a provided Id has no element associated with it in the architecture.
 *
 * @author Christopher Van der Westhuizen
 */

public class InvalidElementIDException extends Exception implements java.io.Serializable
{
	/**
	 * Constructs a new exception with null as its detail message.
	 */
	public InvalidElementIDException() {}
	
	/**
	 * Constructs a new exception with the specified detail message.
	 *
	 * @param msg the detail message. The detail message is saved for later retrieval by the Throwable.getMessage() method.
	 */
	public InvalidElementIDException(String msg)
	{
		super(msg);
	}
}
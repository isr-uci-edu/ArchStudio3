package archstudio.comp.archpruner;

/**
 * This exception is thrown whenever a provided URL is invalid.  This could be the case when, for example,
 * a provided URL does not point to an open xArch document.  
 *
 * @author Christopher Van der Westhuizen
 */

public class InvalidURLException extends Exception implements java.io.Serializable
{
	/**
	 * Constructs a new exception with null as its detail message.
	 */
	public InvalidURLException() {}
	
	/**
	 * Constructs a new exception with the specified detail message.
     *
     * @param msg the detail message. The detail message is saved for later retrieval by the Throwable.getMessage() method.
	 */
	public InvalidURLException(String msg)
	{
		super(msg);
	}
}
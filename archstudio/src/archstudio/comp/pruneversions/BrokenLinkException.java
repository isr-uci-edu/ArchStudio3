package archstudio.comp.pruneversions;


/**
 *	This exception should be thrown whenever a link cannot be resolved.  For example, an Href not resolving to a valid type.
 *
 *	@author Christopher Van der Westhuizen
 */
public class BrokenLinkException extends Exception implements java.io.Serializable
{
	/**
	 * Constructs a new exception with null as its detail message.
	 */
	BrokenLinkException() {}
	
	/**
	 * Constructs a new exception with the specified detail message.
	 *
	 * @param msg the detail message. The detail message is saved for later retrieval by the Throwable.getMessage() method.
	 */
	BrokenLinkException(String msg)
	{
		super(msg);
	}
}

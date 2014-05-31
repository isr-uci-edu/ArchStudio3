

package archstudio.comp.archdiff;

/**
 * ParseXArchException
 * An exception that is thrown whenever an XArch element is not succesfully parsed from a 
 * file or URL containing an XArch document.
 */
public class ParseXArchException extends Exception implements java.io.Serializable
{
	public ParseXArchException() {}
	public ParseXArchException(String msg)
	{
		super(msg);
	}
}
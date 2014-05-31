// Ping Chen
// BrokenLinkException.java

package archstudio.comp.pladiff;

/*
 * This Exception is thrown when we encounter a link that resolved to NULL.
 * in another words: a broken link
 */

public class BrokenLinkException extends Exception
{
	public BrokenLinkException( String msg )
	{
		super( msg );
	}
}
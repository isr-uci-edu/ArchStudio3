// Ping Chen
// ArchDiffException.java

package archstudio.comp.pladiff;

/*
 * This Exception is thrown when we encounter a problem
 * while performing the diff algorithm.
 */

public class PLADiffException extends Exception
{
	public PLADiffException( String msg )
	{
		super( msg );
	}
}
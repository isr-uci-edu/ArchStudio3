package archstudio.comp.archmerge;

/**
 *
 * ArchMergeException is thrown when the merge algorithm encounters some problem that it
 * could not recover from.
 */
public class ArchMergeException extends Exception implements java.io.Serializable
{
	public ArchMergeException( String msg )
	{
		super( msg );
	}
}


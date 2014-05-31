package archstudio.comp.archmerge;

/**
 *
 * InvalidLocationException is thrown whenever the arch merge component tries to open an
 * xArch at the specified location, but no open document was found.
 */
public class InvalidLocationException extends Exception implements java.io.Serializable
{
	public InvalidLocationException( String msg )
	{
		super( msg );
	}
}



// Ping Chen
// InvalidURLException.java

package archstudio.comp.selector;

/**
 *
 * InvalidURLException is thrown whenever the selector component encounters a
 * problem with some URL.
 */
public class InvalidURLException extends Exception
{
    public InvalidURLException( String msg )
    {
        super( msg );
    }
}

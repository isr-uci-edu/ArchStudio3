// Ping Chen
// MissingElementException.java

package archstudio.comp.pladiff;

/**
 *
 * The MissingElementException is thrown whenever the program encounters
 * an architectural element that is lacking some necessary elements.
 */
public class MissingElementException extends Exception
{
    public MissingElementException( String msg )
    {
        super( msg );
    }
}

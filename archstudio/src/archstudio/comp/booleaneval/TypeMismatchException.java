// Ping Chen
// TypeMismatchException.java

package archstudio.comp.booleaneval;

/**
 *
 * TypeMismatchException is thrown when the program tries to type cast a variable's
 * value into the incorrect type.
 * In another words, it is thrown when the types do not match.
 */
public class TypeMismatchException extends Exception
{
    public TypeMismatchException( String msg )
    {
        super( msg );
    }
}

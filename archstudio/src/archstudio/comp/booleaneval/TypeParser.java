// Ping Chen
// TypeParser.java

package archstudio.comp.booleaneval;

import java.util.Date;
import java.util.Locale;
import java.text.DateFormat;
//import java.text.ParsePosition;

/**
 * The TypeParser class takes in a string representation of a value and parses into
 * the appropriate Object. 
 *
 * @author Ping Hsin Chen <A HREF="mailto:pingc@hotmail.com">(pingc@hotmail.com)</A>
 */
 
public class TypeParser
{
    // This converts a string to a date, given any valid date format 
    // Eric's magic. Horrible names :-(
	private static Date getDateVal( String s )
	    throws NoSuchTypeException
	{
		java.util.Date d = null;
		d = getDateVal( s, DateFormat.FULL );
		if( d != null )
		{
			return d;
		}
		
		d = getDateVal( s, DateFormat.LONG );
		if( d != null )
		{
			return d;
		}

		d = getDateVal( s, DateFormat.MEDIUM );
		if( d != null )
		{
			return d;
		}

		d = getDateVal( s, DateFormat.SHORT );
		if( d != null )
		{
			return d;
		}
		//System.out.println( s );
		// does not match any type
		throw new NoSuchTypeException( "Unable to parse " + s 
			+ ": invalid Date format." );
	}

    // this is another private helper function.  It will return null
    // if the date is bad
	private static Date getDateVal( String s, int format )
	{
		try
		{
			DateFormat df = DateFormat.getDateInstance( format, Locale.US );
			
			//I finally figured out what "lenient" means--this means that dates like
			//"Feburary 95, 1999" will be the 95th day after Feb 1, 1999.  It doesn't 
			//allow off-formats at all, as the Java documentation describes!!!
			df.setLenient( false );
			Date newDate = ( Date )df.parse( s );
			if(newDate != null)
			{
				return new Date( newDate.getTime( ) );
			}
			else 
			    return null;
		}
		catch( java.text.ParseException e )
		{
			
			//System.out.println(e);
			return null;
		}
	}
    /**
     * This is the function that will perform the parsing from string to the appropriate
     * Object.
     * @param value The string representation of the values.
     *              Dates are represented by #MM/DD/YYYY#.
     *              Strings are represented by "String".
     *              And Doubles are just numbers.
     * @return Object The object representation of the value passed in.
     *                  The possible return types are String, Date, and Double.
     * @exception NoSuchTypeException Thrown if there is an error in parsing.
     */
    public static Object parse( String value )  
        throws NoSuchTypeException
    {
        if( value.length( ) == 0 )
            throw  new NoSuchTypeException( "Attempt to parse an empty string." );
        switch( value.charAt( 0 ) )
        {
            case '\"':
                if( value.endsWith( "\"" ) &&
                    value.length( ) > 2) // makes sure the string is properly formed
                    return value.substring( 1, value.length( ) - 1 );
                else
                    throw new NoSuchTypeException( value + 
                        " is an invalid represenation of a string.  Missing \"." );
            case '#':
                if( value.endsWith( "#" ) &&
                    value.length( ) > 2 )  // makes sure the date is properly formed
                    return getDateVal( value.substring( 1, value.length( ) - 1 ) );
                else
                    throw new NoSuchTypeException( value + 
                        " is an invalid representation of a date.  Missing #." );
            default: // a number
                try
                {
                    return new Double( value );
                }
                catch( NumberFormatException e)
                {
                    throw new NoSuchTypeException( value + 
                        " is an invalid representation of a number." );
                }
        }                
    }
}
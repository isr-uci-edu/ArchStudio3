// Ping Chen
// SymbolTable.java

package archstudio.comp.booleaneval;

import java.util.Hashtable;
import java.util.Date;
import java.util.Enumeration;
import java.io.*;
import java.util.*;
import java.text.DateFormat;

/**
 * SymbolTable, this will act as the customized symbol table used to pass in variables
 * and their associated value.  Namely, it is a container class that stores a name-value
 * pair.  The value is wrapped by an internal class TypeValuePair.  TypeValuePair
 * stores not only the value, but it stores the type as well.  
 * The symbol table is implemented with the java predefined hashtable.  It uses the 
 * name as the key.
 * 
 * @see archstudio.comp.booleaneval.TypeParser
 * @author Ping Hsin Chen <A HREF="mailto:pingc@hotmail.com">(pingc@hotmail.com)</A>
 * @updated by Matt Critchlow and Lewis Chiang August, 2002
 *	
*/

public class SymbolTable implements java.io.Serializable
{
	
	/**
	 * This is the integer value to represent type Double
	 */
	public static final int DOUBLE = 100;
	
	/**
	 * This integer value represents the type String
	 */
	public static final int STRING = 101;
	
	/**
	 * This integer value represents the type Date
	 */
	public static final int DATE   = 102;
	
	/** 
	 * TypeValuePair will store the value of a variable as an object.
	 * The type of the value is stored as an integer constant
	 * The only valid types are doubles (real numbers), strings, and dates.
	 */
	public static class TypeValuePair
	{
		
		private Object value;
		private int type;
		
		/* testing only!*/
		protected String name;
		
		public TypeValuePair( int newType, Object newVal )
		{
			value = newVal;
			type = newType;
		}
		
		// testing only!
		public String toString( )
		{
			return "( " + name + ", " + value.toString( ) + " )";
		}
	}
	
	private Hashtable myTable;
	
	/**
	 * Default constructor
	 */
	public SymbolTable( )
	{
		myTable = new Hashtable( );
	}
	
	/**
	 * Constructor that allows user to specify the size of the symbol table
	 *
	 * @param newSize The size of the symbol table.
	 */
	public SymbolTable( int newSize )
	{
		myTable = new Hashtable( newSize );
	}
	
	/**
	 * This function stores the name-value pair into the symbol table.  It will 
	 * override any previous value associated with this variable.
	 *
	 * @param variable The name of the variable
	 * @param value The value associated with the variable name that is passed in.
	 *              
	 * @exception NoSuchTypeException This exception is thrown if the type passed
	 *                                in is not one of the valid data types
	 *
	 */
	public void put( String variable, String value )
		throws NoSuchTypeException
	{
		Object val = TypeParser.parse( value );
		
		TypeValuePair temp; 
		if( val instanceof Date )
		{
			temp = new TypeValuePair( DATE, val );
			temp.name = variable;
			myTable.put( variable, temp );
		}
		else if( val instanceof Double )
		{
			temp = new TypeValuePair( DOUBLE, val );
			temp.name = variable;
			myTable.put( variable, temp );
		}
		else if( val instanceof String )
		{
			temp = new TypeValuePair( STRING, val );
			temp.name = variable;
			myTable.put( variable, temp );
		}
	}
	/**
	 * This function retrieves the value associated with a particular variable name.
	 * This is the general get function that will return the object.
	 *
	 * @param variable The variable whose value you wish to retrieve.
	 * @return Object representing the value associated with the variable passed in. Null if it doesn't exist
	 */
	public Object get( String variable ) 
	{
		TypeValuePair temp = ( TypeValuePair ) myTable.get( variable );
		if( temp != null )
		{
			return temp.value;
		}
		else 
			return null;
	}
	/**
	 * This is a specialized get functin that returns Doubles.  
	 *
	 * @param variable The name of the double variable whose value you wish to retrieve.
	 * @return A Double object to represent the value of the variable passed in.
	 * @exception TypeMismatchException This exception is thrown when the variable is not of type Double.
	 **/
	public Double getDouble( String variable )
		throws TypeMismatchException
	{
		TypeValuePair temp = ( TypeValuePair )myTable.get( variable );
		if( temp != null )
		{
			if( temp.type == DOUBLE )
			{
				return ( Double )temp.value;
			}
			else
			{
				throw new TypeMismatchException( "Variable " + variable + " is not of type Double." );
			}
		}
		else
			return null;
	}
	
	/**
	 * This is a specialized get functin that returns Strings.  
	 *
	 * @param variable The name of the string variable whose value you wish to retrieve.
	 * @return A String object to represent the value of the variable passed in. Null if it doesn't exist.
	 * @exception TypeMismatchException This exception is thrown when the variable is not of type String.
	 **/
	public String getString( String variable )
		throws TypeMismatchException
	{
		TypeValuePair temp = ( TypeValuePair )myTable.get( variable );
		if( temp != null )
		{
			if( temp.type == STRING )
			{
				return ( String )temp.value;
			}
			else
			{
				throw new TypeMismatchException( "Variable " + variable + " is not of type String." );
			}
		}
		else
			return null;
	}
	
	/**
	 * This is a specialized get functin that only returns Dates.  
	 *
	 * @param variable The name of the Date variable whose value you wish to retrieve.
	 * @return A Date object to represent the value of the variable passed in. Null if the variable 
	 *          doesn't exist
	 * @exception TypeMismatchException This exception is thrown when the variable is not of type Date.
	 **/
	public Date getDate( String variable )
		throws TypeMismatchException
	{
		TypeValuePair temp = ( TypeValuePair )myTable.get( variable );
		if( temp != null )
		{
			if( temp.type == DATE )
			{
				return ( Date )temp.value;
			}
			else
			{
				throw new TypeMismatchException( "Variable " + variable + " is not of type Date." );
			}
		}
		else
			return null;
	}
	
	/**
	 * This function will return an integer representation of the type of the variable.
	 *
	 * @param variable The variable whose type you wish to get
	 * @return DOUBLE if the variable is of type Double.
	 *         STRING if the variable is of type String.
	 *         DATE if the variable is of type Date.
	 * @exception NoSuchVariableException Thrown if the variable is not in the symbol table
	 */              
	public int getType( String variable )
		throws NoSuchVariableException
	{
		TypeValuePair temp = ( TypeValuePair )myTable.get( variable );
		if( temp != null )
		{
			return temp.type;
		}
		else 
			throw new NoSuchVariableException( "Unable to find variable: " + variable );
	}
	/**
	 * This function removes the name-value pair associated with the variable. 
	 * It will do nothing if the variable does not exist.
	 *
	 * @param variable The name of the variable whose value you wish to remove.
	 */
	public void remove( String variable )
	{
		myTable.remove( variable );
	}    
	
	/**
	 * This function checks to see if a particular variable is stored within the symbol
	 * table.  
	 *
	 * @param variable The name of the variable to check
	 * @return True if the variable is in the symbol table, false otherwise
	 */
	public boolean isPresent( String variable )
	{
		return myTable.containsKey( variable );
	}
	
	/**
	 * This function returns the size of the symbol table
	 *
	 * @return The size
	 */
	public int size( )
	{
		return myTable.size( );
	}
	
	// this is mainly for debugging purpose
	public String toString( )
	{
		String output = "";
		Enumeration temp = myTable.elements();
		while( temp.hasMoreElements( ) )
		{
			output += temp.nextElement().toString( ) + '\n';
		}
		return output;
	}
	
	// writeFile takes in the filepath to write the contents of the symbol table to
	// as well as a prepared String[] for printing
	public boolean writeFile(String fileToWrite, String[] file)
	{
		try
		{
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileToWrite)));
			for(int i = 0; i< size(); i++)
			{
				String temp = file[i];
				out.println(temp);
			}
			out.close();
		}
		catch(Exception e)
		{
			System.out.println(e);
			return false;
		}
		return true;
	}
	public boolean isEmpty()
	{
		return myTable.isEmpty();
	}
	
	public void clearTable()
	{
		myTable.clear();
	}
	
	// listTable() stores each name-value pair into a string array which will
	// be used for print out the contents of the table to a file in writeFile()
	public String[] listTable()
	{
		try
		{
			String[] tableArray = new String[size()];
			int i = 0;
			String valueToWrite = "";
			String output = "";
			Enumeration temp = myTable.elements();
			String tempName = "";
			Object tempValue = null;
			while( temp.hasMoreElements( ) )
			{
				StringBuffer buff = new StringBuffer(10000);
				output = temp.nextElement().toString( ) + '\n'; 
				StringTokenizer token = new StringTokenizer(output, ",");
				String test = token.nextToken();
				String test2 = token.nextToken();
				StringTokenizer token2 = new StringTokenizer(test, " ");
				tempName = token2.nextToken();
				tempName = token2.nextToken();
				int valueType = getType(tempName);
				if (valueType == DATE)
				{ 
					Date date = getDate(tempName);
					tempValue = DateFormat.getDateTimeInstance().format(date);
					buff.append(tempName + " = " + "#" + tempValue + "#");
					tableArray[i] = buff.toString();
					
				}
				if (valueType == STRING)
				{ 
					tempValue = getString(tempName);
					buff.append(tempName);
					buff.append(" = ");
					buff.append("\"" + tempValue + "\"" );
					tableArray[i] = buff.toString();
				}	
				if (valueType == DOUBLE)
				{ 
					tempValue = getDouble(tempName);
					buff.append(tempName + " = " + tempValue);
					tableArray[i] = buff.toString();
				}
				i++;
			}
			return tableArray;
		}
		catch (Exception e)
		{
			System.out.println(e);
			return null;
		}
	}
	
	// getVariable() returns all the keys(names) of the symbol table in the form of a
	// string array
	public String[] getVariables()
	{
		Enumeration temp = myTable.keys();
		String keySet[] = new String[myTable.size()];
		int i =0;
		String output = "";
		while( temp.hasMoreElements( ) )
		{
			output = temp.nextElement().toString( ).trim();
			keySet[i] = output;
			i++;
		}
		return keySet;
	}
}
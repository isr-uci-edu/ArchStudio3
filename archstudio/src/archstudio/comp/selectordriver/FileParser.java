package archstudio.comp.selectordriver;

/**
 * This class is used to parser an input file and create the symbol table with the data
 * stored in the file.  The input file must have the following properties:
 * Lines beginning with // are ignored (comments)
 * The data is separated from the name by a =
 * Example: integer = 123 or product="Model-231"
 * Note: the dates must be surrounded by # # eg. #1/12/02#
 *       the strings must be surrounded by " " eg. "Hello"
 *       doubles are just plain numbers
 * @see archstudio.comp.booleaneval.TypeParser archstudio.comp.booleaneval.SymbolTable
 * @Author: Ping Hsin Chen
 */

import archstudio.comp.booleaneval.*;
import java.io.*;
import java.util.StringTokenizer;

public class FileParser
{
	public FileParser( )
	{
	}
	
	/**
	 * This function is responsible for parsing the input read in from the file
	 * and then adding those symbols into the a new symbol table
	 * 
	 * @param fileName This is the path of the file to be parsed
	 * @return SymbolTable The new symbol table created from the file.
	 *
	 * @exception FileNotFoundException If the path specified is not a correct file
	 * @exception IOException If the program encountered problems with input stream
	 */
	public static SymbolTable createTable( String fileName )
		throws FileNotFoundException, IOException, Exception
	{
		BufferedReader br = new BufferedReader( new FileReader( fileName ) );
		
		SymbolTable symTab = new SymbolTable( 50 );
		int lineNum = 1;  // this is to help printing out error messages
		String line;
		
		line = br.readLine( );
		// makes sure we haven't reached the end of file
		while( line != null )
		{
			line = line.trim();    // removes extra white space at beginning and end
			// ignores empty lines and comments
			if( !line.equals( "" ) && !line.startsWith( "//" ) )
			{
				// the tokenizer WILL return =
				StringTokenizer st = new StringTokenizer( line, "=", true );

				if( st.countTokens() >= 2 )	
				{
					String name = st.nextToken( ).trim( );
					st.nextToken( ); // dumps the first =
					
					String variable = st.nextToken( );
					// this will concat the rest of the tokens into the variable
					// string
					while( st.hasMoreTokens( ) )
						variable += st.nextToken( );
					// makes sure that variable doesn't have starting or ending white spaces
					symTab.put( name, variable.trim( ) );
				}
				else
					throw new Exception( "Error parsing input file on line " + lineNum +
						": " + line );
				
			}
			line = br.readLine( );
			lineNum++;
		}
		br.close( );
		return symTab;
	}
	
}

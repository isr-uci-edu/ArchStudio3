// Ping Chen
// ElementCollector.java

package archstudio.comp.selector;

import edu.uci.ics.xarchutils.*;  // this is to use the xArchADT

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

/**
 * This is the implementation of a simple collector class.  It will collect elements
 * of various types (Component, Connector, .. ) and allow them to be removed with a 
 * single function call.  This saves on the numbers of calls to xArchADT and increases efficiency
 *
 * @author Ping Hsin Chen <A HREF="mailto:pingc@hotmail.com">(pingc@hotmail.com)</A>
 */
public class ElementCollector
{
	public static int DEFAULT_NUM_OF_TYPES = 5;
	public static int DEFAULT_NUM_OF_ELEMENTS = 50;
	public static int DEFAULT_NUM_OF_PARENTS = 25;
	
	protected XArchFlatInterface xArch;
	
	// This stores the expected number of elements per type. (Size of the arrays for each type)
	private int numOfElements;
	// This stores the number of expected element types
	private int numOfElementTypes;
	
	// This stores all the elements
	// The hashtable uses the parent element as the key.  Each key then stores a separate
	// hash table which is keyed by the type of thing, which is associated with 
	// a Vector storing all the elements of the particular type.
	private Hashtable collection;
	
	/**
	 * Constructor.
	 * @param xArchInst The reference to xarch ADT
	 * @param numberOfElementTypes The number of types we are expecting.
	 * @param numberOfElements The number of elements we are expecting per type
	 */
	public ElementCollector( XArchFlatInterface xArchInst, int numberOfElementTypes, int numberOfElements )
	{
		init( xArchInst, numberOfElementTypes, numberOfElements );
	}
	
	/**
	* Constructor.
	* @param xArchInst The reference to xarch ADT
	*/
	public ElementCollector( XArchFlatInterface xArchInst )
	{
		init( xArchInst, DEFAULT_NUM_OF_TYPES, DEFAULT_NUM_OF_ELEMENTS ); 
	}
		
	// Helper function to initialize all the data members
	private void init( XArchFlatInterface xArchInst, int numberOfElementTypes, int numberOfElements )
	{
		xArch = xArchInst;
		numOfElements = numberOfElements;
		numOfElementTypes = numberOfElementTypes;
		collection = new Hashtable( DEFAULT_NUM_OF_PARENTS );
	}
	
	/**
 	 * This function adds the specified element into the collection.
	 *
	 * @param parent The ObjRef of the parent of the element that needs to be removed
	 * @param elementType This is the type of the element, this is the same string that would be
	 * 			used in a call to xArch ADT's remove
	 * @param element This is the object ref to the action element we are adding to the collection
	 */
	public void addElement( ObjRef parent, String elementType, ObjRef elementToAdd )
	{
		/*
		try
		{
			System.out.println( "Removing: " + elementType + ' ' + xArch.get( elementToAdd, "Id" ) );
		}
		catch( Exception e )
		{
			System.out.println( e );
		}
		*/
		
		// This gets all the children elements that needs to be removed under the given parent
		Hashtable children = ( Hashtable )collection.get( parent );
		Vector elements = null;
		
		// This parent has never been encountered, so we need to create it.
		if( children == null )
		{
			children = new Hashtable( numOfElementTypes );
			elements = new Vector( numOfElements );
			elements.add( elementToAdd );
		
			// now associate the type with the list of elements to remove
			children.put( elementType, elements );
			// now add the children hashtable to the general hash table under the parent
			collection.put( parent, children );
		}
		// we've seen this parent before
		else
		{
			elements = ( Vector )children.get( elementType );
			// check to see if we've encountered this type under this parent
			if( elements == null )
			{
				// we've never countered this type before, so create a new list of elements
				// to remove
				elements = new Vector( numOfElements );
				elements.add( elementToAdd );
				
				// now associate the type with the list of elements to remove
				children.put( elementType, elements );
			}
			else
			{
				// we already have a list for this parent and type. so just add it.
				elements.add( elementToAdd );
			}
		}
	}
	
	/**
 	 * This function calls xArch ADT and removes all the elements that are currently in the
	 * collection.  
	 */
	public void removeAll( )
	{
		Hashtable children;
		Vector elements;
		ObjRef parent;
		String typeOfThing;
		ObjRef[] thingsToRemove;
		
		// first iterate through all the parents
		for( Enumeration allParents = collection.keys( ); allParents.hasMoreElements( ); )
		{
			parent = ( ObjRef )allParents.nextElement( );
			
			// gets all the children nodes of this parent 
			children = ( Hashtable )collection.get( parent );
			
			// now iterate through all types under this parent
			for( Enumeration allTypes = children.keys( ); allTypes.hasMoreElements( ); )
			{
				typeOfThing = ( String )allTypes.nextElement( );
				elements = ( Vector ) children.get( typeOfThing );
				
				thingsToRemove = new ObjRef[elements.size( )];
				elements.toArray( thingsToRemove );
				
				try
				{
					/*
					System.out.println( "Removing from parent: " + xArch.get( parent, "Id" ) + 
						typeOfThing + " with size " + thingsToRemove.length );
					*/
					xArch.remove( parent, typeOfThing, thingsToRemove );
				}
				catch( Exception e )
				{
					System.err.println( e );
				}				
			}
		}
	}
}
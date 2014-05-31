
package archstudio.comp.archmerge;

import archstudio.comp.xarchtrans.*;

import edu.uci.ics.xarchutils.*;   
import java.util.Vector;

/**
 * ArchMerge - This class will implement the actual merge algorithm
 * The main purpose for the creation of this class is to make ArchMergeImpl reentrant
 */

public class ArchMerge
{
	// This is for debugging
	private final static boolean DEBUG = true;
	
	//private ObjRef archRef;        		  	  // this is the ref for the archInstance
	
	// This is the xarch flat interface to support transactions
	private XArchFlatTransactionsInterface xArchTrans;
	
	private Vector removes;             // vector of remove elements
	private Vector adds;				// vector of add elements
	
	// this is the array containing the types of elements we check in archStructure
	private static final String[] STRUCT_ELEMENTS = {"Component", "Connector", "Link"};
	
	// this is the array containing the types of elements we check in archType
	private static final String[] TYPE_ELEMENTS = {"ComponentType", "ConnectorType", "InterfaceType"}; 
	
	// references to the contexts
	private ObjRef typesContextRef;
	
	private final static int DEFAULT_SIZE = 100;
	
	public ArchMerge( XArchFlatTransactionsInterface xArchInst )
	{		
		xArchTrans = xArchInst;
		removes = new Vector( 50 );
		adds = new Vector( 50 );
	}
	
	/**
	 * This is the function that provides the merging.  The algorithm
	 * will go through and remove all the elements that are in the diff document that are
	 * also in the structure document.  Then add all the elements from the diff document to the 
	 * structure document.  
	 * The ordering is of vital importance, always remove first, then add.
	 * Note: the algorithm will not add elements from the diff document that are already in the 
	 * structure doc, and it will simply ignore element that are suppose to be removed but doesn't 
	 * exist in the structure doc.
	 *
	 * @param diffRef This is the reference to the diff doc
	 * @param archRef This is the reference to the arch doc
	 * @exception ArchMergeException Thrown when the merge algorithm encounters some unrecoverable problem.
	 */
	public void merge( ObjRef diffRef, ObjRef archRef )
		throws ArchMergeException
	{				
		/* context stuff*/
		
		// Make a context with the docs
		ObjRef diffContextRef = xArchTrans.createContext( diffRef, "Diff");
		typesContextRef = xArchTrans.createContext( archRef, "Types" );
		
		// we now need to break the diff element down to the diff parts and break it into add and remove
		ObjRef diffElement = xArchTrans.getElement( diffContextRef, "Diff", diffRef );
		if( diffElement != null )
		{
			parse( diffElement );		
		}
		else
			throw new ArchMergeException( 
			"The diff document specified does not contain a Diff element." );

		// we are ready to start adding and removing, so we need to create a transaction
		Transaction t = xArchTrans.createTransaction( archRef );
		// the parts of the merge.  we need to remove first, then add
		// this is because of sometimes the diff could contain instruction to remove 
		// an old component, and add a new component with the same ID
		debug( "new transaction... got it!" );
		
		performRemove( t, archRef );
		
		debug( "Done remove" );
		
		performAdd( t, archRef );
		
		debug( "Done add" );
		
		xArchTrans.commit( t );
	}
	
	// this function takes in a diff element, and breaks up the diff parts into add and remove
	// (adds those elements into the appropriate vector)
	private void parse( ObjRef diffElement )
		throws ArchMergeException
	{
		ObjRef[] diffParts = xArchTrans.getAll( diffElement, "DiffPart" );
		
		if( diffParts != null )
		{
			for( int i = 0; i < diffParts.length; i++ )
			{
				// we check to see if this diff part is a remove, we assume it won't have both
				// a remove and an add
				ObjRef temp = ( ObjRef ) xArchTrans.get( diffParts[i], "Remove" );
				if( temp != null )
				{
					removes.add( temp );
					// we are done with this iteration
					continue;
				}
				// its not a remove, so we check to see if its an add
				temp = ( ObjRef ) xArchTrans.get( diffParts[i], "Add" );
				if( temp != null )
				{
					adds.add( temp );
				}
				// neither remove nor add, ERROR!
				else
					throw new ArchMergeException( "A diff part does not contain an add or a remove." );
			}
		}
		debug( "Done parsing" + diffParts.length + " diff parts" );
		debug( removes.size( ) + " removes" );
		debug( adds.size( ) + " adds" );
	}

	// this function handles the removal of elements, it iterates through each remove's ID and
	// it gets the element we want to remove by the ID and then checks to see what type the element
	// is then we remove it
	// t - the transaction that the removes will belong to
	// archRef - the doc we are trying to merge with
	// exception raised if the id is null in the remove part
	private void performRemove( Transaction t, ObjRef archRef )
		throws ArchMergeException
	{
		int size = removes.size( );
		// iterates through all the remove elements
		for( int i = 0; i < size; i++ )
		{
			String id =  ( String )xArchTrans.get( ( ObjRef ) removes.elementAt( i ), "RemoveId" );
			if( id != null )
			{
				debug( "Removing: " + id );
				
				removeID( id, archRef, t );			
			}
			else
				throw new ArchMergeException( "A remove part is missing its ID." );
		}
	}	
	
	private boolean isInRemoveSet(String idToCheck){
		int size = removes.size( );
		// iterates through all the remove elements
		for( int i = 0; i < size; i++ )
		{
			String id =  ( String )xArchTrans.get( ( ObjRef ) removes.elementAt( i ), "RemoveId" );
			if( id != null )
			{
				if(idToCheck.equals(id)){
					return true;
				}
			}
		}
		return false;
	}
	
	// This will first check what type of element this ID identifies and them proceeds
	// to remove that element.  
	// id - the ID of the element to check
	// archRef - the architecture to check in
	// t - the transaction that this remove belongs to
	private void removeID( String id, ObjRef archRef, Transaction t )
	{
		ObjRef result;
		ObjRef parent;
		ObjRef[] structures = xArchTrans.getAllElements( typesContextRef, "archStructure", archRef );
		if( structures != null )
		{
			for( int i = 0; i < structures.length; ++i )
			{
				// iterate throguh the types of things we want to check for
				// in the archStructure
				for( int j = 0; j < STRUCT_ELEMENTS.length; j++ )
				{
					result = xArchTrans.get( structures[i], STRUCT_ELEMENTS[j], id );
					// we found an element matching this ID
					if( result != null )
					{
						debug( "Found " + STRUCT_ELEMENTS[j] );
						
						parent = xArchTrans.getParent( result );
						
						debug( "Found parent" );
						
						xArchTrans.remove( t, parent, STRUCT_ELEMENTS[j], result );
						// we removed it, so we are done
						debug( "Returning from removeID" );
						
						return;
					}
				}
			}
		}
		// not in structure, check for types
        ObjRef[] types = xArchTrans.getAllElements( typesContextRef, "archTypes", archRef );
		if( types != null )
		{
			for( int i = 0; i < types.length; ++i )
			{
				// iterate throguh the types of things we want to check for
				// in the archType
				for( int j = 0; j < TYPE_ELEMENTS.length; j++ )
				{					
					result = xArchTrans.get( types[i], TYPE_ELEMENTS[j], id );
					// found an element that matched
					if( result != null )
					{
						debug( "Found " + TYPE_ELEMENTS[j] );
						
						parent = xArchTrans.getParent( result );
						xArchTrans.remove( t, parent, TYPE_ELEMENTS[j], result );
						// removed, done
						
						debug( "Returning from removeID" );
						return;
					}
				}
			}
		}
	}
	
	// This function is responsible for handling all the add parts.
	// It will iterate through each add part and
	// first find out what type of element we need to add, and then
	// adds its to the corresponding arch types or arch structure
	// t - the transaction that these adds are part of
	// archRef - the document we are adding to
	private void performAdd( Transaction t, ObjRef archRef )
	{
		int size = adds.size( );
		for( int i = 0; i < size; ++i )
		{
			// this is the add part
			ObjRef addPart = ( ObjRef ) adds.elementAt( i );
			addElement( addPart, archRef, t );
		}
	}
	
	// This function will take in the add part and checks the type and then
	// add it to either archType or archStructure
	// add - the add part that we are currently processing
	// archRef - The architecture we are adding to
	// t - the current transaction
	private void addElement( ObjRef add, ObjRef archRef, Transaction t )
	{
		// this is the element we want to add
		ObjRef element;
		// searchs to see if this add is one of the elements arch structure
		for( int j = 0; j < STRUCT_ELEMENTS.length; j++ )
		{
			element = ( ObjRef )xArchTrans.get( add, STRUCT_ELEMENTS[j] );
			// ok, we have identified what we need to add
			if( element != null )
			{
				// it is an element in arch structure
				// if there are multiple structures, we can't handle it right now
				// so just add it to the first one
				ObjRef archStruct = xArchTrans.getElement( typesContextRef, "archStructure",
					archRef );
				
				element = xArchTrans.recontextualize( typesContextRef, 
					STRUCT_ELEMENTS[j], element );
				// first make sure this element isn't already in the structure
				String id = (String)xArchTrans.get(element, "Id");
				
				// first make sure this element isn't already in the structure
				ObjRef eltWithSameId = null;
				
				// unless we've already removed it as part of this transaction
				if(!isInRemoveSet(id)){
					try{
						eltWithSameId = (ObjRef)xArchTrans.get(archStruct, STRUCT_ELEMENTS[j], id);
					}
					catch(Exception e){}
				}
				
				//if( !xArchTrans.has( archType, TYPE_ELEMENTS[j], element ) )
				if(eltWithSameId == null)
				//if( !xArchTrans.has( archStruct, STRUCT_ELEMENTS[j], element ) )
				{		
					xArchTrans.add( t, archStruct, STRUCT_ELEMENTS[j], element );
					return;
				}
				// if it was already in it, we simply ignore it
			}
		}
		// checks to see if its one of the elements in archtypes
		for( int j = 0; j < TYPE_ELEMENTS.length; j++ )
		{
			element = ( ObjRef )xArchTrans.get( add, TYPE_ELEMENTS[j] );
			// ok, we have identified what we need to add
			if( element != null )
			{
				// it is an element in arch structure
				// if there are multiple structures, we can't handle it right now
				// so just add it to the first one
				ObjRef archType = xArchTrans.getElement( typesContextRef, "archTypes",
					archRef );
				
				element = xArchTrans.recontextualize( typesContextRef, 
					TYPE_ELEMENTS[j], element );
				
				String id = (String)xArchTrans.get(element, "Id");
				// first make sure this element isn't already in the structure
				ObjRef eltWithSameId = null;
				try{
					eltWithSameId = xArchTrans.get(archType, TYPE_ELEMENTS[j], id);
				}
				catch(Exception e){}
				
				//if( !xArchTrans.has( archType, TYPE_ELEMENTS[j], element ) )
				if(eltWithSameId == null)
				{		
					xArchTrans.add( t, archType, TYPE_ELEMENTS[j], element );
					return;
				}
				// if it was already in it, we simply ignore it
			}
		}
	}
	
	// debug function
	private void debug( String s )
	{
		if( DEBUG )
			System.out.println( s );
	}
	
}

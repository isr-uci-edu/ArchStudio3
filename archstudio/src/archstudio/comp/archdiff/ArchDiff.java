
package archstudio.comp.archdiff;

// to support xArchADT use
import edu.uci.ics.xarchutils.*;

// Standard Imports
import java.util.*;
import java.io.*;

/**
 * This class provides all the diffing services.  However, this class is non-reentrant so
 * should never be called.
 * @author Christopher Van der Westhuizen
 */

public class ArchDiff
{

	// interface through which DOM-based xArch libraries can be accessed
	protected XArchFlatInterface xarch; 
	
	private String origArchURI;      // URI of original arch structure
	private String newArchURI;       // URI of new arch structure
	private String diffArchURI;      // URI of difference
	
	// Object Refs 
	private ObjRef oldXArchRef;
	private ObjRef newXArchRef;
	private ObjRef origArchStructureRef;
	private ObjRef [] origArchStructureRefs;
	private ObjRef newArchStructureRef;	
	private ObjRef [] newArchStructureRefs;
	private ObjRef origArchTypesRef;
	private ObjRef [] origArchTypesRefs;
	private ObjRef newArchTypesRef;
	private ObjRef [] newArchTypesRefs;
	private ObjRef diffElementRef;	
	private ObjRef diffArchRef;
	private ObjRef diffContextRef;
	private ObjRef newTypesContextRef;
	private ObjRef oldTypesContextRef;
	
	/*
	Collections of elements from the original structure	
	 */

	// Original Structure refs
	private ObjRef [] origComponentRefs;
	private ObjRef [] origConnectorRefs;
	private ObjRef [] origGroupRefs;
	private ObjRef [] origLinkRefs;	
	
	// Original Types refs
	private ObjRef [] origComponentTypeRefs;
	private ObjRef [] origConnectorTypeRefs;
	private ObjRef [] origInterfaceTypeRefs;

	/*
	Collections of elements from the new structure		
	 */
	
	// New Structure refs
	private ObjRef [] newComponentRefs;
	private ObjRef [] newConnectorRefs;
	private ObjRef [] newGroupRefs;
	private ObjRef [] newLinkRefs;	
	
	// New Types refs
	private ObjRef [] newComponentTypeRefs;
	private ObjRef [] newConnectorTypeRefs;
	private ObjRef [] newInterfaceTypeRefs;
	
	public static void main(String[] args){
		if(args.length != 2){
			System.err.println("Arg error.");
			return;
		}

		try{
			XArchFlatInterface flat = new XArchFlatImpl();
			ObjRef r1 = flat.parseFromFile(args[0]);
			ObjRef r2 = flat.parseFromFile(args[1]);
			String u1 = flat.getXArchURI(r1);
			String u2 = flat.getXArchURI(r2);
			ArchDiff d = new ArchDiff(flat);
			d.diff(u1, u2, "urn:diff");
			ObjRef rd = flat.getOpenXArch("urn:diff");
			String ser = flat.serialize(rd);
			System.out.println(ser);
		}
		catch(Exception e){
			e.printStackTrace();
			return;
		}
	}
	
	/*-----------------------------------------------------------*
	public StructureDiff
	purpose: constructor
	 *-----------------------------------------------------------*/
	public ArchDiff( XArchFlatInterface xArchInst )
	{
		xarch = xArchInst;
	}
	

	/** Making a call on diff will perform the differencing algorithm on the two provided
	 * architectures and the output will be sent to diffArchURI, the diff description.
	 * Note the architectures should be open.
	 * WARNING: This function is non-reentrant! 
	 *
	 * @param origArchURI location that holds original architecture description.
	 * @param newArchURI location that holds new architecture description.
	 * @param diffArchURI location that holds the diff description.
	 *
	 * @exception ParseXArchException This exception is thrown when the documents could not be parsed for some reason
	 * @see package archstudio.comp.archdiff;
	 */
	public void diff(String oURI, String nURI, String dURI)
		throws ParseXArchException
	{
	    // Obtain our URIs
		origArchURI = oURI;
		newArchURI = nURI;		
		diffArchURI = dURI;
		
		oldXArchRef = xarch.getOpenXArch( origArchURI );  
		newXArchRef = xarch.getOpenXArch( newArchURI );	
		
		// Parse out URIs provided
		parse();
		
		// Create a new diffXArch
		diffArchRef = xarch.createXArch(diffArchURI);	
		
		// Make a context with this XArch
		diffContextRef = xarch.createContext(diffArchRef, "Diff");
		
		// Make a top element IDiff element
		diffElementRef = xarch.createElement(diffContextRef, "Diff");	
		// Diff the documents
		performDiff();
		
		// Add this diff element to the overall diff description		
		xarch.add(diffArchRef, "Object", diffElementRef);
	}

	/*-----------------------------------------------------------*
	private void performDiff()
	purpose: perform the differencing algorithm on each of the
	elements in the old architecture and the new 
	architecture	
	notes: achieves this by calling removeElements and addElements,
	two helper functions for diffing.
	 *-----------------------------------------------------------*/
	private void performDiff()
	{		
		// Find elements to be removed
		removeElements(origComponentRefs, "Component");
		removeElements(origConnectorRefs, "Connector");
		removeElements(origGroupRefs, "Group");
		removeElements(origLinkRefs, "Link");
		removeElements(origComponentTypeRefs, "ComponentType");
		removeElements(origConnectorTypeRefs, "ConnectorType");		
		removeElements(origInterfaceTypeRefs, "InterfaceType");

		
		// Find elements to be added, and add them
		addElements(newComponentRefs, "Component");
		addElements(newConnectorRefs, "Connector");
		addElements(newGroupRefs, "Group");
		addElements(newLinkRefs, "Link");
		addElements(newComponentTypeRefs, "ComponentType");
		addElements(newConnectorTypeRefs, "ConnectorType");
		addElements(newInterfaceTypeRefs, "InterfaceType");
	}	
	
	/*-----------------------------------------------------------------------------*
	private void removeElements(ObjRef [] origElementRefs, String elementType )
	purpose: Adds IRemove elements to the diff description of the objects that was removed
	inputs: ObjRef [] origElementRefs - array of element ObjRefs.  The element is specified by elementType
	String elementType - String representing the type that the array is of (component, connecter...)
	output: void	
	notes: Searches through provided list to find elements that were removed.  
	Once such elements are found, IRemove elements will be created and
	added to the diff.  If, however, an element is found that is in both
	architectures but is not equivalent internally then perform a remove followed
	by an add.
	 *-----------------------------------------------------------------------------*/
	private void removeElements(ObjRef [] origElementRefs, String elementType)
	{
		/*
		Check each of the IDs in the first structure and look for them
		in the second structure. If they don't exist then they were removed.
		Also, check to see if the elements that do exist are different in
		the original and new architecture.
		 */
		
		if(origElementRefs == null)
			return;
		
		int size = origElementRefs.length;
		for(int i = 0; i < size; ++i)
		{
			ObjRef origElementRef = origElementRefs[i];
			String id = (String)xarch.get(origElementRef, "Id");
			
			// look for an element in the new architecture with the same id
			ObjRef newElementRef = xarch.getByID(newXArchRef, id);
			
			// if element is found
			if(newElementRef != null)
			{
				String origType = xarch.getType( origElementRef );
				String newType = xarch.getType( newElementRef );
				
				// if they aren't the same type, we also do a remove-add
				if( !origType.equals( newType ) )
				{
					// Remove the old element
					removeElement(id);			
					
					// Add the new element
					addElement(newElementRef, elementType);
				}
				// same type but if the two elements differ in some way
				else if( !xarch.isEquivalent(origElementRef, newElementRef))
				{					
					// Remove the old element
					removeElement(id);			
					
					// Add the new element
					addElement(newElementRef, elementType);
				}
			}
			// element is not in the second architecture
			else
			{
				// Remove
				removeElement(id);
			}
		}
	}
	
	/*-----------------------------------------------------------------------------*
	private void removeElement(String id)
	purpose: Creates a IRemove element and adds it to the overarching IDiffElement
	inputs: String id - id of element to remove
	output: void	
	 *-----------------------------------------------------------------------------*/
	private void removeElement(String id)
	{
		ObjRef removeElementRef = xarch.create(diffContextRef, "Remove");
		xarch.set(removeElementRef, "RemoveId", id);
		ObjRef diffPartRef = xarch.create(diffContextRef, "DiffPart");
		xarch.set(diffPartRef, "Remove", removeElementRef);
		xarch.add(diffElementRef, "DiffPart", diffPartRef);		
	}
	
	/*-------------------------------------------------------------------*
	private void addElements(ObjRef [] newElementRefs, String elementType)
	purpose: Adds IAdd elements to the diff description
	inputs: ObjRef [] newElementRefs -  array of ObjRefs to inspect for 
	adding
	String elementType - type of the elements in the above 
	mentioned array
	output: void	
	notes: Looks through a specified set of element refs to find 
	elements to add.  When an element to be added is found, 
	it is recontextualized into the diff context and added in.
	 *-------------------------------------------------------------------*/
	private void addElements(ObjRef [] newElementRefs, String elementType)
	{
		/*
		Check each of the elements of the new architecture and see if they exist 
		in the first architecture. This way we can see if new elements have been introduced
		or not.
		 */
		
		if(newElementRefs == null)
			return;
		
		int size = newElementRefs.length;
		for(int i = 0; i < size; ++i)
		{
			ObjRef newElementRef = newElementRefs[i];
			String id = (String)xarch.get(newElementRef, "Id");
			
			// Look for id in original structure
			ObjRef origElementRef = xarch.getByID(oldXArchRef, id);
			if(origElementRef == null)
			{
				// Element doesn't exist in original arch so add it
				addElement(newElementRef, elementType);
			}			
		}
	}
	
	/*-------------------------------------------------------------------*
	private void addElement(ObjRef origElementRef, ObjRef newElementRef)
	purpose: Adds an IAdd element to the diff description
	inputs: ObjRef newElementRef - element being added
	String elementType - the type of the element being added
	output: void	
	 *-------------------------------------------------------------------*/
	private void addElement(ObjRef newElementRef, String elementType)
	{
		ObjRef addElementRef = xarch.create(diffContextRef, "Add");
		
		newElementRef = xarch.recontextualize(diffContextRef, elementType, newElementRef);
		xarch.set(addElementRef, elementType, newElementRef);
		ObjRef diffPartRef = xarch.create(diffContextRef, "DiffPart");
		xarch.set(diffPartRef, "Add", addElementRef);
		xarch.add(diffElementRef, "DiffPart", diffPartRef); 
	}
	
	/*-----------------------------------------------------------------------------*
	 * appendObjRefs.
	 * 
	 * This function takes in two lists of ObjRefs and appends them into one list
	 * Input parameters should be actual arrays to ObjRefs. If null, the lists
	 * will be made of size 0.
	 * Returns: The combined list
	 *-----------------------------------------------------------------------------*/
	private ObjRef [] appendObjRefs(ObjRef [] currentRefs, ObjRef [] refsToAdd)
	{		
		ObjRef [] newRefs;
		
		// Check for null parameters
		if(currentRefs == null)
			currentRefs = new ObjRef[0];
		if(refsToAdd == null)
			refsToAdd = new ObjRef[0];
		
		// Determine the size of the list to make
		int size;
		size = currentRefs.length + refsToAdd.length;
		
		// Construct an ObjRef array of just the right size
		newRefs = new ObjRef[size];
		
		// Insert into the list elements from both provided lists
		int i;
		for(i = 0; i < currentRefs.length; ++i)
			newRefs[i] = currentRefs[i];
		for(int j = 0; i < size; ++i, ++j)
			newRefs[i] = refsToAdd[j];
		
		return newRefs;
	}
	
	/*-----------------------------------------------------------*
	private void parse()
	purpose: Parses two URIs out into a collection of 
	component, connector, group, and link elements
	inputs: void
	output: void
	 *-----------------------------------------------------------*/
	private void parse()
		throws ParseXArchException
	{   
		/*
		Parse contents of URI
		 */      			
		if(oldXArchRef == null)
		{
			throw new ParseXArchException(origArchURI + " did not produce an xarch reference");
		}

		// types context made from oldXArch
		oldTypesContextRef = xarch.createContext(oldXArchRef, "Types");
		
		// get archstructure and archtypes
		origArchStructureRefs = xarch.getAllElements(oldTypesContextRef, "ArchStructure", oldXArchRef);		
		origArchTypesRefs = xarch.getAllElements(oldTypesContextRef, "ArchTypes", oldXArchRef);
		
		// Iterate over all structures
		int size = origArchStructureRefs.length;
		for(int i = 0; i < size; ++i)
		{
			origArchStructureRef = origArchStructureRefs[i];
			
			// Get collections of all the original archStructure elements
			origComponentRefs = appendObjRefs(origComponentRefs, xarch.getAll(origArchStructureRef, "Component"));
			origConnectorRefs = appendObjRefs(origConnectorRefs, xarch.getAll(origArchStructureRef, "Connector"));
			origGroupRefs = appendObjRefs(origGroupRefs, xarch.getAll(origArchStructureRef, "Group"));
			origLinkRefs = appendObjRefs(origLinkRefs, xarch.getAll(origArchStructureRef, "Link"));
		}
		
		// Get original type elements		
		size = origArchTypesRefs.length;
		
		for(int i = 0; i < size; ++i)
		{
			
			origArchTypesRef = origArchTypesRefs[i];
			
			origComponentTypeRefs = appendObjRefs(origComponentTypeRefs, xarch.getAll(origArchTypesRef, "ComponentType"));
			origConnectorTypeRefs = appendObjRefs(origConnectorTypeRefs, xarch.getAll(origArchTypesRef, "ConnectorType"));
			origInterfaceTypeRefs = appendObjRefs(origInterfaceTypeRefs, xarch.getAll(origArchTypesRef, "InterfaceType"));
		}

		
		/*
		Parse contents of new arch
		 */ 		
		if(newXArchRef == null)
		{
			throw new ParseXArchException(newArchURI + " did not produce an xarch reference");
		}

		// make types context with new xArch
		newTypesContextRef = xarch.createContext(newXArchRef, "Types");
		
		// get archstructure and archtypes
		newArchStructureRefs = xarch.getAllElements(newTypesContextRef, "ArchStructure", newXArchRef);
		newArchTypesRefs = xarch.getAllElements(newTypesContextRef, "ArchTypes", newXArchRef);
	
		size = newArchStructureRefs.length;
		for(int i = 0; i < size; ++i)
		{
			newArchStructureRef = newArchStructureRefs[i];
			
			// get collections of all the new archStructure elements
			newComponentRefs = appendObjRefs(newComponentRefs, xarch.getAll(newArchStructureRef, "Component"));			
			newConnectorRefs = appendObjRefs(newConnectorRefs, xarch.getAll(newArchStructureRef, "Connector"));
			newGroupRefs = appendObjRefs(newGroupRefs, xarch.getAll(newArchStructureRef, "Group"));
			newLinkRefs = appendObjRefs(newLinkRefs, xarch.getAll(newArchStructureRef, "Link"));
		}
		
		// get new type elements
		size = newArchTypesRefs.length;
		for(int i = 0; i < size; ++i)
		{
			newArchTypesRef = newArchTypesRefs[i];
			
			newComponentTypeRefs = appendObjRefs(newComponentTypeRefs, xarch.getAll(newArchTypesRef, "ComponentType"));
			newConnectorTypeRefs = appendObjRefs(newConnectorTypeRefs, xarch.getAll(newArchTypesRef, "ConnectorType"));
			newInterfaceTypeRefs = appendObjRefs(newInterfaceTypeRefs, xarch.getAll(newArchTypesRef, "InterfaceType"));		
		}
	}
}



// PLADiffImpl.java

package archstudio.comp.pladiff;

import edu.uci.ics.xarchutils.*;  // this is to use the xArchADT

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Implementation of the Diffing service.  PLADiff is responsible for taking two architecture
 * descriptions and producing a diff description.  A diff description contains the instructions
 * necessary to transform the first architecture into the second through a series of adds and removals.
 * @author Ping H. Chen [pchen@ics.uci.edu] and Matt Critchlow [critchlm@ics.uci.edu]
 */
public class PLADiffImpl implements IPLADiff
{

	// These integers will serve as the flag for typeOfElement
	// This is to allow us to factor out common code yet
	// still be able to identify what type of element
	// we are working with so we can call the proper functions.
	protected static final int COMPONENT 		= 100;
	protected static final int CONNECTOR 		= 101;
	protected static final int LINK      		= 102;
	protected static final int INTERFACE 		= 103;
	protected static final int SIGNATURE 		= 104;
	protected static final int VARIANT   		= 105;
	protected static final int SIG_INTF_MAPPING = 106;
	protected static final int SUB_STRUCTURE    = 107;
	protected static final int OPTIONAL         = 108;
	protected static final int TYPE				= 109;


	//currently we are assuming this is passed in to the main diff method
	protected ObjRef           origArchRef;     //document reference
	protected ObjRef		   newArchRef;      // reference to the new document
	protected ObjRef           diffContext;     //context for diff


	protected GuardToString    guardUtil;		// This is the guard utility class
												// it'll be able to convert guards to string
	//protected ObjRef           typesContext;        //context for type

	// this is the local reference to the xArchADT
	protected XArchFlatInterface xArch;

	// this class allows us to convert boolean guards to strings
	protected GuardToString guardToString;

	public PLADiffImpl( XArchFlatInterface xArchIntf )
	{
	    xArch = xArchIntf;
		guardUtil = new GuardToString( xArch );
	}

	/**
	 * This is the main entry point for the diff algorithm.  It will calculate
	 * the architectural difference between two architectures specified.  It specifies
	 * the difference by creating a "diff" doc at the specified address.
	 * The diff doc is a heirarchical list of addition and removals.
	 * Each level is represents by a "diffPart".  Each diffPart represents
	 * a structural level (ArchStructure), so all the changes within a diffPart
	 * are applied to elements within that ArchStructure.
	 *
	 * @param origFileLocation This is the URL of the original file (openned)
	 *		that we are diffing on.
	 * @param newFileLocation This is the URL of the new file (openned)
	 *		that we are diffing against.
	 * @param diffDocLocation This is the URL where the diff document will be created at.
	 * @param origStartingID This is the ID of the element we should start the diffing process
	 * 		on in the original document.
	 * @param newStartingID This is the ID of the element we should start the diffing process
	 * 		on in the new document.
	 * @param isStructural True if the starting IDs refers to a ArchStructure. False otherwise
	 *
	 * @exception PLADiffException This exception is thrown when the diff encounters
	 * 		some (internal) error that its not able to recover from.
	 * @exception MissingElementException This exception is thrown when the algorithm
	 *      cannot find a required element in the architecture description.
	 * @exception BrokenLinkException This exception is thrown when a href resolves to null
	 */
	public void diff( String origFileLocation, String newFileLocation,
		String diffDocLocation, String origStartingID, String newStartingID,
		boolean isStructural )
		throws PLADiffException, MissingElementException, BrokenLinkException
	{
		// Get the architectures
		origArchRef = xArch.getOpenXArch( origFileLocation );
		newArchRef = xArch.getOpenXArch( newFileLocation );

		if( origArchRef == null )
		{
			throw new PLADiffException( "Error: " + origFileLocation +
				" does not refer to an open document." );
		}
		if( newArchRef == null )
		{
			throw new PLADiffException( "Error: " + newFileLocation +
				" does not refer to an open document." );
		}

		// get the starting elements
		ObjRef origStartingPoint = xArch.getByID( origArchRef, origStartingID );
		ObjRef newStartingPoint = xArch.getByID( newArchRef, newStartingID );

		if( origStartingPoint == null )
		{
			throw new MissingElementException( "Error: missing starting element " +
				origStartingID + " in original architecture." );
		}
		if( newStartingPoint == null )
		{
			throw new MissingElementException( "Error: missing starting element " +
				newStartingID + " in new architecture." );
		}

		// Create a new diffXArch
		ObjRef diffArchRef = xArch.createXArch( diffDocLocation );
		// Make a context with this XArch
		diffContext = xArch.createContext( diffArchRef, "Pladiff" );
		// Make a top element IDiff element
		ObjRef diffElement = xArch.createElement(diffContext, "PLADiff");
		// Add this diff element to the overall diff description
		xArch.add( diffArchRef, "Object", diffElement );

		ObjRef diffPart;
		boolean isDifferent;

		// Note: the root diffPart also contains information
		// about the starting point.  This information is needed
		// for the merge step to make sure we are applying the diff
		// to the proper starting point.
		if( isStructural )
		{
			diffPart = createDiffPart( STRUCTURAL_STARTING_POINT );
			isDifferent = diffStructure( origStartingPoint, newStartingPoint, diffPart );
		}
		else
		{
			diffPart = createDiffPart( TYPE_STARTING_POINT );

			// the diffType function needs a description just incase it needs to create
			// a new diffPart.  At this point, the root is the only description
			// we can give.
			isDifferent = diffType( origStartingPoint, newStartingPoint, diffPart,
				TYPE_STARTING_POINT );
		}

		// If changes were made, we connect the diffPart
		if( isDifferent )
		{
			xArch.set( diffElement, "DiffPart", diffPart );
		}
	}

	// ************************** Diff Algorithm functions *********************

	// This function performs the diff operation on archStructures.  Namely it compares
	// components, connector, links, and interfaces of the components/connectors.
	//
	// origStruct - reference to the original structure
	// newStruct - reference to the new structure we are comparing against.
	// diffPart - The diffPart that these additions and removals belong to.
	// 		So this is the diffPart for this ArchStructural-level.
	// Returns true if the 2 structures aren't the same (changes were made to the diffPart)
	protected boolean diffStructure( ObjRef origStruct, ObjRef newStruct, ObjRef diffPart )
		throws PLADiffException, MissingElementException, BrokenLinkException
	{
		//System.out.println( "Entering Diff Structure" );
		boolean isDifferent, tempDifferent;
		// components
		ObjRef[] origComps = xArch.getAll( origStruct, "Component" );
		ObjRef[] newComps = xArch.getAll( newStruct, "Component" );
		// has type, has interface, don't create new diffPart
		isDifferent = diffElementArray( origComps, newComps, COMPONENT,
			diffPart, true, true, false, null );
		System.gc();
		//System.out.println( "---------------------------Done with Components" );
		// connectors
		ObjRef[] origConns = xArch.getAll( origStruct, "Connector" );
		ObjRef[] newConns = xArch.getAll( newStruct, "Connector" );
		// has type, has interface, don't create new diffPart
		tempDifferent = diffElementArray( origConns, newConns, CONNECTOR,
			diffPart, true, true, false, null );
		System.gc();
		//System.out.println( "---------------------------Done with Connectors" );
		isDifferent = isDifferent || tempDifferent;

		// links
		ObjRef[] origLinks = xArch.getAll( origStruct, "Link" );
		ObjRef[] newLinks = xArch.getAll( newStruct, "Link" );
		// No type, no interface, don't create new diffPart
		tempDifferent = diffElementArray( origLinks, newLinks, LINK,
			diffPart, false, false, false, null );

		//System.out.println( "Done with links" );
		isDifferent = isDifferent || tempDifferent;
		//System.out.println( "Exiting Diff Structure" );
		return isDifferent;
	}

	// This function compares two elements based on its optional tag.
	// This will calculate the add/remove of optionals in the diff and add it
	// to the diff part passed in.  The desc is the desc of the original element
	// and the new element (it is assumed that they are the same).
	// This returns true if the two elements are different and changes were made to the
	// diff part
	protected boolean diffOptionalElements( ObjRef origElement, ObjRef newElement, ObjRef diffPart,
		String diffLocation )
		throws PLADiffException, MissingElementException, BrokenLinkException
	{
		ObjRef origOptional = null;
		ObjRef newOptional = null;
		boolean isDifferent = true;

		if( isOptional( origElement ) )
		{
			origOptional = ( ObjRef )xArch.get( origElement, "Optional" );
		}
		if( isOptional( newElement ) )
		{
			newOptional = ( ObjRef )xArch.get( newElement, "Optional" );
		}
		// now perform the diff
		if( origOptional != null && newOptional == null )
		{
			// the original has an optional, but the new one does not
			// so we remove
			addRemoveElement( diffPart, origOptional, OPTIONAL, diffLocation, origArchRef );
		}
		else if( origOptional == null && newOptional != null )
		{
			// the original doesn't have an optional, but the new one does
			addNewElement( diffPart, newOptional, OPTIONAL, diffLocation, newArchRef );
		}
		else if( origOptional != null && newOptional != null )
		{
			// both aren't null, so do deep compare of the guards/optionals
			if( !isSameOptional( origOptional, newOptional ) )
			{
				// they aren't the same, so do a replace
				addRemoveElement( diffPart, origOptional, OPTIONAL, diffLocation, origArchRef );
				// add in the new element
				addNewElement( diffPart, newOptional, OPTIONAL, diffLocation, newArchRef );
			}
			else
			{
				// same guards and optional
				isDifferent = false;
			}
		}
		else if( origOptional == null && newOptional == null )
		{
			// both doesn't have it
			// so they really aren't different
			isDifferent = false;
		}

		return isDifferent;
	}

	// This function performs the diff operation on types.  Namely it compares
	// singatures, signature-interface mappings, variants, and substructures.
	//
	// origType - reference to the original type
	// newType - reference to the new type we are comparing against.
	// diffPart - The diffPart that these additions and removals belong to.
	// 		So this is the diffPart for this ArchStructural-level.
	// diffLocation - This is the string that identifies where this type belongs.
	// 		So this is the description of the component/connector that is of the type
	//		we are diffing on.  This string is used to identify substructures and creating new
	//		diffParts as well as identifying where the add/remove of type elements go
	//
	// Returns true if the 2 types aren't the same (changes were made to the diffPart)
	protected boolean diffType( ObjRef origType, ObjRef newType, ObjRef diffPart,
		String diffLocation )
		throws PLADiffException, MissingElementException, BrokenLinkException
	{
		//System.out.println( "Entering Diff Type" );
		boolean isDifferent, tempDifferent;


		ObjRef[] origSigs = null;
		ObjRef[] newSigs = null;
		ObjRef[] origVariants = null;
		ObjRef[] newVariants = null;
		ObjRef origSubStruct = null;
		ObjRef newSubStruct = null;

		// get everything on the original type - if it exists
		if( origType != null )
		{
			origSigs = xArch.getAll( origType, "Signature" );
			origVariants = xArch.getAll( origType, "Variant" );
			origSubStruct = ( ObjRef )xArch.get( origType, "SubArchitecture" );
		}
		// get everything on the new type - if it exists
		if( newType != null )
		{
			newSigs = xArch.getAll( newType, "Signature" );
			newVariants = xArch.getAll( newType, "Variant" );
			newSubStruct = ( ObjRef )xArch.get( newType, "SubArchitecture" );
		}

		// Signature
		// No type, no interface, don't create new diffPart
		// sigs do have types, but we ignore them for the purpose
		// of diff
		isDifferent = diffElementArray( origSigs, newSigs, SIGNATURE,
			diffPart, false, false, false, diffLocation );

		// variants
		// has type but no interface.  Create new diff part
		tempDifferent = diffElementArray( origVariants, newVariants, VARIANT,
			diffPart, true, false, true, diffLocation );

		isDifferent = isDifferent || tempDifferent;

		// substructure
		tempDifferent = diffSubStructure( origSubStruct, newSubStruct,
			diffPart, diffLocation );
		//System.out.println( "Exiting Diff Type" );
		return isDifferent || tempDifferent;
	}

	// This function handles the diffing of substructures.  There are 4 cases to consider:
	// 1. original has a substructure new doesn't
	// 2. The original doesn't have a substructure but the new does
	// 3. Both doesn't have substructure
	// 4. Both has substructure
	// It takes in the 2 sub structures to compare, the diffPart we are operating in (the diffPart
	// that we put the add/remove into), and also a string representing the
	// location.
	// It returns true if the 2 types are different.
	protected boolean diffSubStructure( ObjRef origSubStruct, ObjRef newSubStruct, ObjRef diffPart,
		String diffLocation )
		throws PLADiffException, MissingElementException, BrokenLinkException
	{
		boolean isDifferent = false, tempDifferent;

		if( origSubStruct != null && newSubStruct == null )
		{
			// since the new one doesn't have a sub structure, we remove it
			addRemoveElement( diffPart, origSubStruct, SUB_STRUCTURE, diffLocation, origArchRef );
			isDifferent = true;
		}
		else if( origSubStruct == null && newSubStruct != null )
		{
			// the new one has, but old doesn't.  
			
			// First we diff on the Sig. Intf Mappings
			ObjRef[] newSigIntfMappings = xArch.getAll( newSubStruct,
				"SignatureInterfaceMapping" );
			// no type, no interface, don't create new diff part
			diffElementArray( null, newSigIntfMappings,
				SIG_INTF_MAPPING, diffPart, false, false, false, diffLocation );
			
			// now we add the new sub structure link
			addNewElement( diffPart, newSubStruct, SUB_STRUCTURE, diffLocation, newArchRef );
			isDifferent = true;
		}
		else if( origSubStruct != null && newSubStruct != null )
		{
			// both has substructures
			ObjRef[] origSigIntfMappings = xArch.getAll( origSubStruct,
				"SignatureInterfaceMapping" );
			ObjRef[] newSigIntfMappings = xArch.getAll( newSubStruct,
				"SignatureInterfaceMapping" );

			// no type, no interface, don't create new diff part
			isDifferent = diffElementArray( origSigIntfMappings, newSigIntfMappings,
				SIG_INTF_MAPPING, diffPart, false, false, false, diffLocation );

			// now create a new diffPart for the new level in the sub archstructure
			// we use the name passed in since that represents the element
			// that contains this type with subarchstructure
			ObjRef newDiffPart = createDiffPart( diffLocation );

			ObjRef origStructureLink = ( ObjRef )xArch.get( origSubStruct, "ArchStructure" );
			ObjRef newStructureLink = ( ObjRef )xArch.get( newSubStruct, "ArchStructure" );

			if( origStructureLink == null )
			{
				throw new MissingElementException( "Error: Sub-Structure inside of " +
					diffLocation + " in original document is missing its ArchStructure link." );
			}
			if( newStructureLink == null )
			{
				throw new MissingElementException( "Error: Sub-Structure inside of " +
					diffLocation + " in new document is missing its ArchStructure link." );
			}
			tempDifferent = diffStructure( resolveLink( origStructureLink, origArchRef ),
				resolveLink( newStructureLink, newArchRef ), newDiffPart );

			if( tempDifferent )
			{
				// there was a difference, so connect the new diffPart up
				xArch.add( diffPart, "DiffPart", newDiffPart );
				isDifferent = true;
			}
		}
		// both doesn't have it, do nothing
		return isDifferent;
	}
	// **********  Helper functions for adding elements to the diff *********

	/** This helper method will allow a Remove element to be added to a given diffPart
	 * Given the type of the element (typeOfElement) , different attributes will comprise the Remove element.
	 * Also, if the element is a type element, a diffLocation will be added to the Remove element.
	 *
	 * @param diffPart The diffPart to add the Remove element to.
	 * @param elementToRemove The element which will be added to the Remove element.
	 * @param typeOfElement The type of elementToRemove. i.e component, connector, ect.
	 * @param diffLocation location that holds the diff description. Only used for type elements
	 */
	protected void addRemoveElement( ObjRef diffPart, ObjRef elementToRemove, int typeOfElement,
		String diffLocation, ObjRef archRef)
		throws PLADiffException, MissingElementException, BrokenLinkException
	{
		ObjRef removeElementRef = xArch.create(diffContext, "Remove");
		String description = "";
		switch( typeOfElement )
		{
			//for structural element, create a description to be added to the structural element
			case COMPONENT:
			{
				ObjRef removeStructuralElementRef = xArch.create( diffContext, "RemoveStructuralEntity" );
				ObjRef componentDescriptionRef = xArch.create( diffContext, "Description" );
				ObjRef componentDescription = ( ObjRef )xArch.get( elementToRemove, "Description" );

				//make sure the element has a description
				if( componentDescription == null )
					throw new MissingElementException( "Error: " + componentDescription + ' ' + ( String )
					xArch.get( componentDescription, "Id" ) + " does not have a description" );


				description = ( String )xArch.get( componentDescription, "Value" );
				xArch.set( componentDescriptionRef, "Value", description );
				xArch.set( removeStructuralElementRef, "Component", componentDescriptionRef );
				xArch.set( removeElementRef, "RemoveStructuralEntity", removeStructuralElementRef );
				xArch.add( diffPart, "Remove", removeElementRef );
				break;
			}
			case CONNECTOR:
			{
				ObjRef removeStructuralElementRef = xArch.create( diffContext, "RemoveStructuralEntity" );
				ObjRef connectorDescriptionRef = xArch.create( diffContext, "Description" );
				ObjRef connectorDescription = ( ObjRef )xArch.get( elementToRemove, "Description" );

				//make sure the element has a description
				if( connectorDescription == null )
					throw new MissingElementException( "Error: " + connectorDescription + ' ' + ( String )
					xArch.get( connectorDescription, "Id" ) + " does not have a description" );

				description = ( String )xArch.get( connectorDescription, "Value" );
				xArch.set( connectorDescriptionRef, "Value", description );
				xArch.set( removeStructuralElementRef, "Connector", connectorDescriptionRef );
				xArch.set( removeElementRef, "RemoveStructuralEntity", removeStructuralElementRef );
				xArch.add( diffPart, "Remove", removeElementRef );
				break;
			}

			case LINK:
			{
				ObjRef removeStructuralElementRef = xArch.create( diffContext, "RemoveStructuralEntity" );
				ObjRef linkDescriptionRef = xArch.create( diffContext, "Description" );
				ObjRef linkDescription = ( ObjRef )xArch.get( elementToRemove, "Description" );

				//make sure the element has a description
				if( linkDescription == null )
					throw new MissingElementException( "Error: " + linkDescription + ' ' + ( String )
					xArch.get( linkDescription, "Id" ) + " does not have a description" );

				description = ( String )xArch.get( linkDescription, "Value" );
				xArch.set( linkDescriptionRef, "Value", description );
				xArch.set( removeStructuralElementRef, "Link", linkDescriptionRef );
				xArch.set( removeElementRef, "RemoveStructuralEntity", removeStructuralElementRef );
				xArch.add( diffPart, "Remove", removeElementRef );
				break;
			}

			//interfaces are a special case stuctural element, we need the element and interface descriptions to be added
			//to the structural entity
			case INTERFACE:
			{
			    //System.out.println( "Got to remove interface" );

				ObjRef removeStructuralElementRef = xArch.create( diffContext, "RemoveStructuralEntity" );
				ObjRef removeInterfaceRef = xArch.create( diffContext, "RemoveInterface" );
				ObjRef interfaceDescriptionRef = xArch.create( diffContext, "Description" );
				ObjRef elementDescriptionRef = xArch.create( diffContext, "Description" );

				ObjRef interfaceDescription = ( ObjRef )xArch.get( elementToRemove, "Description" );

				//make sure the element has a description
				if( interfaceDescription == null )
					throw new MissingElementException( "Error: " + interfaceDescription + ' ' + ( String )
					xArch.get( interfaceDescription, "Id" ) + " does not have a description" );


				description = ( String )xArch.get( interfaceDescription, "Value" );

                //System.out.println( "Interface Desc: " + description );
                //System.out.println( "Element Desc: " + diffLocation );

				xArch.set( elementDescriptionRef, "Value", diffLocation );
				xArch.set( interfaceDescriptionRef, "Value", description );
				xArch.set( removeInterfaceRef, "InterfaceDescription", interfaceDescriptionRef );
				xArch.set( removeInterfaceRef, "ElementDescription", elementDescriptionRef );
				xArch.set( removeStructuralElementRef, "RemoveInterface", removeInterfaceRef );
				xArch.set( removeElementRef, "RemoveStructuralEntity", removeStructuralElementRef );
				xArch.add( diffPart, "Remove", removeElementRef );
				break;
			}

			case OPTIONAL:
			{
				//System.out.println( "Got to remove interface" );

				ObjRef removeStructuralElementRef = xArch.create( diffContext, "RemoveStructuralEntity" );
				ObjRef removeOptionalRef = xArch.create( diffContext, "RemoveOptional" );
				ObjRef optionalDescriptionRef = xArch.create( diffContext, "Description" );
				ObjRef elementDescriptionRef = xArch.create( diffContext, "Description" );

				description = this.getDescription( elementToRemove, OPTIONAL, archRef );

				//System.out.println( "Interface Desc: " + description );
				//System.out.println( "Element Desc: " + diffLocation );

				xArch.set( elementDescriptionRef, "Value", diffLocation );
				xArch.set( optionalDescriptionRef, "Value", description );
				xArch.set( removeOptionalRef, "OptionalDescription", optionalDescriptionRef );
				xArch.set( removeOptionalRef, "ElementDescription", elementDescriptionRef );
				xArch.set( removeStructuralElementRef, "RemoveOptional", removeOptionalRef );
				xArch.set( removeElementRef, "RemoveStructuralEntity", removeStructuralElementRef );
				xArch.add( diffPart, "Remove", removeElementRef );
				break;
			}
			//Diff Locations must be added as attributes to all type entities
			case SIGNATURE:
			{
				ObjRef removeTypeElementRef = xArch.create( diffContext, "RemoveTypeEntity" );
				ObjRef signatureDescriptionRef = xArch.create( diffContext, "Description" );
				ObjRef signatureDescription = ( ObjRef )xArch.get( elementToRemove, "Description" );

				//need to create new diffLocation for the diffPart
				ObjRef diffLocationRef = xArch.create( diffContext, "DiffLocation" );
				ObjRef diffLocationDescriptionRef = xArch.create( diffContext, "Description" );


				//make sure the element has a description
				if( signatureDescription == null )
					throw new MissingElementException( "Error: " + signatureDescription + ' ' + ( String )
					xArch.get( signatureDescription, "Id" ) + " does not have a description" );

				description = ( String )xArch.get( signatureDescription, "Value" );

				xArch.set( diffLocationDescriptionRef, "Value", diffLocation );
				xArch.set( signatureDescriptionRef, "Value", description );
				xArch.set( removeTypeElementRef, "Signature", signatureDescriptionRef );
				xArch.set( diffLocationRef, "Location", diffLocationDescriptionRef );
				xArch.set( removeTypeElementRef, "DiffLocation", diffLocationRef );
				xArch.set( removeElementRef, "RemoveTypeEntity", removeTypeElementRef );
				xArch.add( diffPart, "Remove", removeElementRef );
				break;
			}

			case VARIANT:
			{
				ObjRef removeTypeElementRef = xArch.create( diffContext, "RemoveTypeEntity" );
				ObjRef variantDescriptionRef = xArch.create( diffContext, "Description" );
				ObjRef variantDescription = ( ObjRef ) xArch.get( elementToRemove, "Guard" );

				//need to create new diffLocation for the diffPart
				ObjRef diffLocationRef = xArch.create( diffContext, "DiffLocation" );
				ObjRef diffLocationDescriptionRef = xArch.create( diffContext, "Description" );

				//make sure the element has a description
				if( variantDescription == null )
					throw new MissingElementException( "Error: " + variantDescription + ' ' + ( String )
					xArch.get( variantDescription, "Id" ) + " does not have a guard" );

				description = guardUtil.guardToString( variantDescription );

				xArch.set( diffLocationDescriptionRef, "Value", diffLocation );
				xArch.set( variantDescriptionRef, "Value", description );
				xArch.set( removeTypeElementRef, "Variant", variantDescriptionRef );
				xArch.set( diffLocationRef, "Location", diffLocationDescriptionRef );
				xArch.set( removeTypeElementRef, "DiffLocation", diffLocationRef );
				xArch.set( removeElementRef, "RemoveTypeEntity", removeTypeElementRef );
				xArch.add( diffPart, "Remove", removeElementRef );
				break;
			}

			case SIG_INTF_MAPPING:
			{
				ObjRef removeTypeElementRef = xArch.create( diffContext, "RemoveTypeEntity" );
				ObjRef removeSIMRef = xArch.create( diffContext, "RemoveSignatureInterfaceMapping" );
				ObjRef innerIFaceDescriptionRef = xArch.create( diffContext, "Description" );
				ObjRef outerSignatureDescriptionRef = xArch.create( diffContext, "Description" );
				ObjRef diffLocationRef = xArch.create( diffContext, "DiffLocation" );
				ObjRef diffLocationDescriptionRef = xArch.create( diffContext, "Description" );


				//we need references to the inner interface and outer signature of the SIM
				ObjRef innerIFaceLinkRef = ( ObjRef )xArch.get( elementToRemove, "InnerInterface" );
				ObjRef outerSignatureLinkRef = ( ObjRef )xArch.get( elementToRemove, "OuterSignature" );

				//make sure the SIM has a valid interface link
				if(innerIFaceLinkRef == null)
					throw new MissingElementException( "Error: " + innerIFaceLinkRef + ' ' + ( String )
					xArch.get( innerIFaceLinkRef, "Id" ) + " does not have a valid XML link" );

				//make sure the SIM has a valid signature link
				if(outerSignatureLinkRef == null)
					throw new MissingElementException( "Error: " + outerSignatureLinkRef + ' ' + ( String )
					xArch.get( outerSignatureLinkRef, "Id" ) + " does not have a valid XML link" );


				//resolve the links to the interface and signature
				ObjRef innerIFaceRef = resolveLink( innerIFaceLinkRef, archRef );
				ObjRef outerSignatureRef = resolveLink( outerSignatureLinkRef, archRef );

				//make sure the interface exists
				if(innerIFaceRef == null)
					throw new MissingElementException( "Error: " + innerIFaceLinkRef + ' ' + ( String )
					xArch.get( innerIFaceLinkRef, "Id" ) + " does not point to a valid interface" );

				//make sure the signature exists
				if(outerSignatureLinkRef == null)
					throw new MissingElementException( "Error: " + outerSignatureLinkRef + ' ' + ( String )
					xArch.get( outerSignatureLinkRef, "Id" ) + " does not point to a valid signature" );


				//get the descriptions of the interface and signature
				ObjRef innerIFaceDescription = ( ObjRef )xArch.get( innerIFaceRef, "Description" );
				ObjRef outerSignatureDescription = ( ObjRef )xArch.get( outerSignatureRef, "Description" );

				//make sure the interface has a description
				if(innerIFaceDescription == null)
					throw new MissingElementException( "Error: " + innerIFaceDescription + ' ' + ( String )
					xArch.get( innerIFaceDescription, "Id" ) + " does not have a description" );

				//make sure the signature has a description
				if(outerSignatureDescription == null)
					throw new MissingElementException( "Error: " + outerSignatureDescription + ' ' + ( String )
					xArch.get( outerSignatureDescription, "Id" ) + " does not have a description" );


				//additional string for signature value
				String elementValue = ( String )xArch.get( innerIFaceDescription, "Value" );
				description = ( String )xArch.get( outerSignatureDescription, "Value" );


				xArch.set( diffLocationDescriptionRef, "Value", diffLocation );
				xArch.set( innerIFaceDescriptionRef, "Value", elementValue );
				xArch.set( outerSignatureDescriptionRef, "Value", description );
				xArch.set( removeSIMRef, "InnerInterfaceDescription", innerIFaceDescriptionRef );
				xArch.set( removeSIMRef, "OuterSignatureDescription", outerSignatureDescriptionRef );
				xArch.set( removeTypeElementRef, "RemoveSignatureInterfaceMapping", removeSIMRef );
				xArch.set( diffLocationRef, "Location", diffLocationDescriptionRef );
				xArch.set( removeTypeElementRef, "DiffLocation", diffLocationRef );
				xArch.set( removeElementRef, "RemoveTypeEntity", removeTypeElementRef );
				xArch.add( diffPart, "Remove", removeElementRef );
				break;
			}
			case SUB_STRUCTURE:
			{
				ObjRef removeTypeElementRef = xArch.create( diffContext, "RemoveTypeEntity" );
				ObjRef subStructureDescriptionRef = xArch.create( diffContext, "Description" );
				ObjRef diffLocationDescriptionRef = xArch.create( diffContext, "Description" );
                ObjRef diffLocationRef = xArch.create( diffContext, "DiffLocation" );

				xArch.set( diffLocationDescriptionRef, "Value", diffLocation );
				xArch.set( subStructureDescriptionRef, "Value", diffLocation );
				xArch.set( removeTypeElementRef, "SubArchitecture", subStructureDescriptionRef );
				xArch.set( diffLocationRef, "Location", diffLocationDescriptionRef );
				xArch.set( removeTypeElementRef, "DiffLocation", diffLocationRef );
				xArch.set( removeElementRef, "RemoveTypeEntity", removeTypeElementRef );
				xArch.add( diffPart, "Remove", removeElementRef );
				break;
			}
			default:
				throw new PLADiffException( "Error: " + elementToRemove + ' ' + ( String )
					xArch.get( elementToRemove, "Id" ) + " is not a legal element to remove" );
		}
	}

	/** This helper method will allow the elementToAdd to be added to a given diffPart
	 * Given the type of the element typeOfElement, element must be recontextualized
	 * to the diff context so that they can exist properly in the DiffPart.
	 * the entire element will be added to the diffPart.
	 *
	 * @param diffPart The diffPart to add the Remove element to.
	 * @param element The element which will be added to the Add element.
	 * @param typeOfElement The type of elementToAdd. i.e component, connector, ect.
	 * @param diffLocation location that holds the diff description. Only used for type elements.
	 */
	protected void addNewElement( ObjRef diffPart, ObjRef element, int typeOfElement,
		String diffLocation, ObjRef archRef)
		throws PLADiffException, MissingElementException, BrokenLinkException
	{
	    ObjRef addElementRef = xArch.create(diffContext, "Add");
		// first we have to clone the element before we can recontextualize it
		ObjRef elementToAdd = xArch.cloneElement( element, edu.uci.isr.xarch.IXArchElement.DEPTH_INFINITY );
		
		String description = "";
		switch( typeOfElement )
		{
			case COMPONENT:
			{
				ObjRef addStructuralElementRef = xArch.create( diffContext, "AddStructuralEntity" );
				ObjRef addComponentRef = xArch.recontextualize(diffContext, "Component", elementToAdd);

				xArch.set( addStructuralElementRef, "Component", addComponentRef );
				xArch.set( addElementRef, "AddStructuralEntity", addStructuralElementRef );
				xArch.add( diffPart, "Add", addElementRef );
				break;
			}

			case CONNECTOR:
			{
				ObjRef addStructuralElementRef = xArch.create( diffContext, "AddStructuralEntity" );
				ObjRef addConnectorRef = xArch.recontextualize(diffContext, "Connector", elementToAdd);

				xArch.set( addStructuralElementRef, "Connector", addConnectorRef );
				xArch.set( addElementRef, "AddStructuralEntity", addStructuralElementRef );
				xArch.add( diffPart, "Add", addElementRef );
				break;
			}

			case LINK:
			{
				ObjRef addStructuralElementRef = xArch.create( diffContext, "AddStructuralEntity" );
				ObjRef addLink = xArch.create( diffContext, "AddLink" );
				ObjRef interfaceEndPoint1 = xArch.create( diffContext, "InterfaceEndPoint" );
				ObjRef interfaceEndPoint2 = xArch.create( diffContext, "InterfaceEndPoint" );
				ObjRef interfaceDescription1 = xArch.create( diffContext, "Description" );
				ObjRef interfaceDescription2 = xArch.create( diffContext, "Description" );
				ObjRef elementDescription1 = xArch.create( diffContext, "Description" );
				ObjRef elementDescription2 = xArch.create( diffContext, "Description" );


				ObjRef point1, point2;

				//get both points the link has
				ObjRef[] points = xArch.getAll( elementToAdd, "Point" );
				point1 = points[0];
				point2 = points[1];
				ObjRef anchorOnInterfaceRef1 = (ObjRef) xArch.get( point1, "AnchorOnInterface" );
				ObjRef anchorOnInterfaceRef2 = (ObjRef) xArch.get( point2, "AnchorOnInterface" );

				//get the interfaces on both sides of the link
				ObjRef interface1 = resolveLink( anchorOnInterfaceRef1, archRef );
				ObjRef interface2 = resolveLink( anchorOnInterfaceRef2, archRef );
				String interfaceValue1 = getDescription( interface1, INTERFACE, archRef );
				String interfaceValue2 = getDescription( interface2, INTERFACE, archRef );

				//get the connecting elements on the interfaces
				ObjRef connectingElement1 = xArch.getParent( interface1 );
				ObjRef connectingElement2 = xArch.getParent( interface2 );

				//pass in component flag, since it doesn't conceptually matter if it is a connector or not
				String elementValue1 = getDescription( connectingElement1, COMPONENT, archRef );
				String elementValue2 = getDescription( connectingElement2, COMPONENT, archRef );

				ObjRef addLinkRef = xArch.recontextualize(diffContext, "Link", elementToAdd);
				xArch.set( elementDescription1, "Value", elementValue1 );
				xArch.set( elementDescription2, "Value", elementValue2 );
				xArch.set( interfaceDescription1, "Value", interfaceValue1 );
				xArch.set( interfaceDescription2, "Value", interfaceValue2 );
				xArch.set( interfaceEndPoint1, "ConnectingElementDescription", elementDescription1 );
				xArch.set( interfaceEndPoint1, "InterfaceDescription", interfaceDescription1 );
				xArch.set( interfaceEndPoint2, "ConnectingElementDescription", elementDescription2 );
				xArch.set( interfaceEndPoint2, "InterfaceDescription", interfaceDescription2 );
				xArch.set( addLink, "Link", addLinkRef );
				xArch.add( addLink, "InterfaceEndPoint", interfaceEndPoint1 );
				xArch.add( addLink, "InterfaceEndPoint", interfaceEndPoint2 );
				xArch.set( addStructuralElementRef, "AddLink", addLink );
				xArch.set( addElementRef, "AddStructuralEntity", addStructuralElementRef );
				xArch.add( diffPart, "Add", addElementRef );
				break;
			}
			//interfaces are a special case stuctural element, we need the element and interface descriptions to be added
			//to the structural entity
			case INTERFACE:
			{
				ObjRef addStructuralElementRef = xArch.create( diffContext, "AddStructuralEntity" );
				ObjRef addInterfaceElementRef = xArch.create( diffContext, "AddInterface" );
				ObjRef addElementDescriptionRef = xArch.create( diffContext, "Description" );
				ObjRef addInterfaceRef = xArch.recontextualize(diffContext, "Interface", elementToAdd);

				xArch.set( addElementDescriptionRef, "Value", diffLocation );
				xArch.set( addInterfaceElementRef, "Interface", addInterfaceRef );
				xArch.set( addInterfaceElementRef, "ElementDescription", addElementDescriptionRef );
				xArch.set( addStructuralElementRef, "AddInterface", addInterfaceElementRef );
				xArch.set( addElementRef, "AddStructuralEntity", addStructuralElementRef );
				xArch.add( diffPart, "Add", addElementRef );
				break;
			}

			//here we add the
			case OPTIONAL:
			{
				ObjRef addStructuralElementRef = xArch.create( diffContext, "AddStructuralEntity" );
				ObjRef addOptionalElementRef = xArch.create( diffContext, "AddOptional" );
				ObjRef addElementDescriptionRef = xArch.create( diffContext, "Description" );
				ObjRef addOptionalRef = xArch.recontextualize(diffContext, "Optional", elementToAdd );

				xArch.set( addElementDescriptionRef, "Value", diffLocation );
				xArch.set( addOptionalElementRef, "ElementDescription", addElementDescriptionRef );
				xArch.set( addOptionalElementRef, "Optional", addOptionalRef );
				xArch.set( addStructuralElementRef, "AddOptional", addOptionalElementRef );
				xArch.set( addElementRef, "AddStructuralEntity", addStructuralElementRef );
				xArch.add( diffPart, "Add", addElementRef );
				break;
			}

			//Diff Locations must be added as attributes to all type entities
			case SIGNATURE:
			{
				ObjRef addTypeElementRef = xArch.create( diffContext, "AddTypeEntity" );
				ObjRef addSignatureRef = xArch.recontextualize(diffContext, "Signature", elementToAdd);
				ObjRef diffLocationRef = xArch.create( diffContext, "DiffLocation" );
				ObjRef diffLocationDescriptionRef = xArch.create( diffContext, "Description" );


				xArch.set( diffLocationDescriptionRef, "Value", diffLocation );
				xArch.set( addTypeElementRef, "Signature", addSignatureRef );
				xArch.set( diffLocationRef, "Location", diffLocationDescriptionRef );
				xArch.set( addTypeElementRef, "DiffLocation", diffLocationRef );
				xArch.set( addElementRef, "AddTypeEntity", addTypeElementRef );
				xArch.add( diffPart, "Add", addElementRef );
				break;
			}

			case VARIANT:
			{
				ObjRef addTypeElementRef = xArch.create( diffContext, "AddTypeEntity" );
				ObjRef addVariantRef = xArch.recontextualize(diffContext, "Variant", elementToAdd );
				ObjRef diffLocationRef = xArch.create( diffContext, "DiffLocation" );
				ObjRef diffLocationDescriptionRef = xArch.create( diffContext, "Description" );


				xArch.set( diffLocationDescriptionRef, "Value", diffLocation );
				xArch.set( addTypeElementRef, "Variant", addVariantRef );
				xArch.set( diffLocationRef, "Location", diffLocationDescriptionRef );
				xArch.set( addTypeElementRef, "DiffLocation", diffLocationRef );
				xArch.set( addElementRef, "AddTypeEntity", addTypeElementRef );
				xArch.add( diffPart, "Add", addElementRef );
				break;
			}

			case SIG_INTF_MAPPING:
			{
				ObjRef addTypeElementRef = xArch.create( diffContext, "AddTypeEntity" );
				ObjRef addSignatureInterfaceMappingRef = xArch.create( diffContext, "AddSignatureInterfaceMapping" );
				ObjRef interfaceEndPoint = xArch.create( diffContext, "InterfaceEndPoint" );
				ObjRef signatureEndPoint = xArch.create( diffContext, "SignatureEndPoint" );
				ObjRef interfaceDescription = xArch.create( diffContext, "Description" );
				ObjRef ifaceElementDescription = xArch.create( diffContext, "Description" );
				ObjRef signatureDescription = xArch.create( diffContext, "Description" );
				ObjRef sigElementDescription = xArch.create( diffContext, "Description" );

				ObjRef innerInterfaceRefLink = (ObjRef) xArch.get( elementToAdd, "InnerInterface" );
				ObjRef outerSignatureRefLink = (ObjRef) xArch.get( elementToAdd, "OuterSignature" );
				//make sure the SIM has a valid interface link
				if(innerInterfaceRefLink == null)
					throw new MissingElementException( "Error: " + innerInterfaceRefLink + ' ' + ( String )
					xArch.get( innerInterfaceRefLink, "Id" ) + " does not have a valid XML link" );

				//make sure the SIM has a valid signature link
				if(outerSignatureRefLink == null)
					throw new MissingElementException( "Error: " + outerSignatureRefLink + ' ' + ( String )
					xArch.get( outerSignatureRefLink, "Id" ) + " does not have a valid XML link" );


				//resolve the links to the interface and signature
				ObjRef innerIFaceRef = resolveLink( innerInterfaceRefLink, archRef );
				ObjRef outerSignatureRef = resolveLink( outerSignatureRefLink, archRef );

				String innerInterfaceValue = getDescription( innerIFaceRef, INTERFACE, archRef );
				String outerSignatureValue = getDescription( outerSignatureRef, SIGNATURE, archRef );

				String interfaceElementValue = getDescription( xArch.getParent( innerIFaceRef ), COMPONENT, archRef );


				ObjRef diffLocationRef = xArch.create( diffContext, "DiffLocation" );
				ObjRef diffLocationDescriptionRef = xArch.create( diffContext, "Description" );

				xArch.set( diffLocationDescriptionRef, "Value", diffLocation );
				//set the interface information
				xArch.set( interfaceDescription, "Value", innerInterfaceValue );
				xArch.set( ifaceElementDescription, "Value", interfaceElementValue );
				xArch.set( interfaceEndPoint, "InterfaceDescription", interfaceDescription );
				xArch.set( interfaceEndPoint, "ConnectingElementDescription", ifaceElementDescription );
				xArch.set( addSignatureInterfaceMappingRef, "InterfaceEndPoint", interfaceEndPoint );

				//set the signature information
				xArch.set( signatureDescription, "Value", outerSignatureValue );
				xArch.set( sigElementDescription, "Value", diffLocation );
				xArch.set( signatureEndPoint, "SignatureDescription", signatureDescription );
				xArch.set( signatureEndPoint, "OuterElementDescription", sigElementDescription );
				xArch.set( addSignatureInterfaceMappingRef, "SignatureEndPoint", signatureEndPoint );

				//add the signature interface mapping
				ObjRef addSIMRef = xArch.recontextualize(diffContext, "SignatureInterfaceMapping", elementToAdd );
				xArch.set( addSignatureInterfaceMappingRef, "SignatureInterfaceMapping", addSIMRef );
				xArch.set( addTypeElementRef, "AddSignatureInterfaceMapping", addSignatureInterfaceMappingRef );
				xArch.set( diffLocationRef, "Location", diffLocationDescriptionRef );
				xArch.set( addTypeElementRef, "DiffLocation", diffLocationRef );
				xArch.set( addElementRef, "AddTypeEntity", addTypeElementRef );
				xArch.add( diffPart, "Add", addElementRef );
				break;
			}

			case SUB_STRUCTURE:
			{
				ObjRef addTypeElementRef = xArch.create( diffContext, "AddTypeEntity" );
				ObjRef addSubStructure = xArch.create( diffContext, "Description" );
				
				ObjRef archLink = ( ObjRef )xArch.get( element, "ArchStructure" );
				String href = ( String )xArch.get( archLink, "Href" );
				// when we are adding new subarchitectures, it always exist in the new doc
				ObjRef subArch = xArch.resolveHref( newArchRef, href );
				ObjRef subArchDescRef = ( ObjRef )xArch.get( subArch, "Description" );
				String subArchDesc = ( String )xArch.get( subArchDescRef, "Value" );
				
				ObjRef diffLocationRef = xArch.create( diffContext, "DiffLocation" );
				ObjRef diffLocationDescriptionRef = xArch.create( diffContext, "Description" );

				xArch.set( diffLocationDescriptionRef, "Value", diffLocation );
				xArch.set( addSubStructure, "Value", subArchDesc );
				
				xArch.set( diffLocationRef, "Location", diffLocationDescriptionRef );
				xArch.set( addTypeElementRef, "DiffLocation", diffLocationRef );
				xArch.set( addTypeElementRef, "SubArchitecture", addSubStructure );
				
				xArch.set( addElementRef, "AddTypeEntity", addTypeElementRef );
				xArch.add( diffPart, "Add", addElementRef );
				break;
			}
			default:
				throw new PLADiffException( "Error: " + elementToAdd + ' ' + ( String )
					xArch.get( elementToAdd, "Id" ) + " is not a legal element to remove" );
		}
	}

	// *************************** General Helper Functions *****************

	// This function creates a diff part with the given name (location).
	// The location could either be the description of a variant or of
	// some other element such as connector or component.
	// Note: The diffPart returned is NOT connected.
	protected ObjRef createDiffPart( String location )
	{
		// First create the diff part
		ObjRef diffPart = xArch.create( diffContext, "DiffPart" );

		// now create the diff location and then add it to the diff part
		ObjRef diffLocation = xArch.create( diffContext, "DiffLocation" );
		ObjRef desc = xArch.create( diffContext, "Description" );
		xArch.set( desc, "Value", location );

		// now hook everything up
		xArch.set( diffLocation, "Location", desc );
		xArch.set( diffPart, "DiffLocation", diffLocation );

		return diffPart;
	}

	// This function will compare the elements in the array.  The elements are
	// of the type specified by the type of element flag.  If an element with the
	// same description exists in both origElements and newElements, then a
	// deeper comparison is made (whatever is appropriate for the type of element).
	// If it only exist in the origElements, then add a remove instruction to the diffPart.
	// If an element only exist in the newElements, then add an add instruction to the diffPart
	//
	// *Note: if one of the arrays passed in is null, they will be set to 0 element
	// 		arrays so we can continue diffing
	// origElements - The array of the original elements
	// newElements - The array of the new elements to compare against
	// typeOfElement - The integer flag specifying what type of elements we
	//		are dealing with
	// diffPart - This is the diff part that all addition and removal instructions
	// 		should be added to.
	// hasType - Flag specifying whether or not this element has a type.  If it does,
	//		then we would need to call diffType on it.
	// hasInterfaces - If the element has interfaces, the function will actually
	//		recurse on itself comparing the lists of interfaces
	// createNewDiffPart - If this flag is true, then we will create a new diff part
	// 		with the name specified.  And this new diff part will be used
	//		to call diffType with.  Otherwise, the diffPart passed in will be passed into
	//		diffType.
	// diffLocation - This is the description of the structural element (component/connector)
	// 		that we are performing a diff inside of.  So this is the name that would be
	//		used to create new diffparts as well as the name used to identify the location of
	//		add/remove type entities and interfaces.
	// The function returns true if changes were made to the diffPart (the elements
	//		weren't all the same).
	protected boolean diffElementArray( ObjRef[] origElements, ObjRef[] newElements,
		int typeOfElement, ObjRef diffPart, boolean hasType, boolean hasInterfaces,
		boolean createNewDiffPart, String diffLocation )
		throws PLADiffException, BrokenLinkException, MissingElementException
	{
		//System.out.println( "Entering Diff Element Array!!");
		boolean found, isChanged = false;
		boolean tempChanged = false;
		boolean newDiffPartChanged = false;
		//String origDesc, newDesc;
		ObjRef newDiffPart = null;

		// Check the inputs, sets them to 0 element arrays if they are null
		if( origElements == null )
		{
		    origElements = new ObjRef[0];
		}
		if( newElements == null )
		{
		    newElements = new ObjRef[0];
		}

		// create the new diffPart if necessary
		// we create it outside the for-loop to prevent multiple diffparts being created
		if( createNewDiffPart && ( origElements.length != 0 && newElements.length != 0 ) )
		{
			// create the new diff part with the
			// name passed in (used in variants...)
			newDiffPart = createDiffPart( diffLocation );
		}
		// this hashtable is used to store all the unique new elements
		// Its keyed on the description and the value is the actual ObjRef
		Hashtable elementsToAdd = new Hashtable( newElements.length );
		
		// make an array to store all the description of the elements
		String[] origDesc = new String[origElements.length];
		String[] newDesc = new String[newElements.length];
		
		for( int i = 0; i < origElements.length; i++)
		{
			origDesc[i] = getDescription( origElements[i], typeOfElement, origArchRef );
		}
		// first add all the new elements to the table
		for( int i = 0; i < newElements.length; i++ )
		{
			newDesc[i] = getDescription( newElements[i], typeOfElement, newArchRef );
			elementsToAdd.put( newDesc[i], newElements[i] );
		}

		for( int i = 0; i < origElements.length; i++ )
		{
			// reset found, we are just starting to look.
			found = false;

			for( int j = 0; j < newElements.length; j++ )
			{
				//System.out.println( "**** " + i + " and " + j );
		
				// if the old and new elements have the same name
				if( origDesc[i].equals( newDesc[j] ) )
				{
					// we've found one matching the name
					found = true;

					// since the original architecture also has this element, we
					// don't want to add
					elementsToAdd.remove( newDesc[j] );

					// these elements have interfaces, so
					// we need to recursively diff on those
					if( hasInterfaces )
					{
						ObjRef[] origIntf, newIntf;

						origIntf = xArch.getAll( origElements[i], "Interface" );
						newIntf = xArch.getAll( newElements[j], "Interface" );

						// no type, is interface, don't create new diffPart
						// interfaces do have types, but we ignore them for the purpose
						// of diff
						tempChanged = diffElementArray( origIntf, newIntf, INTERFACE,
							diffPart, false, false, false, newDesc[j] );
						isChanged = isChanged || tempChanged;
					}
					// if it has a type, then check the type
					if( hasType )
					{
						// since it has type (comp, conn), we need to do fine-grained diffing on
						// the optionality of this element as well
						tempChanged = diffOptionalElements( origElements[i], newElements[j],
							diffPart, newDesc[j] );
						isChanged = tempChanged || isChanged;


						ObjRef origType = getType( origElements[i], typeOfElement, origArchRef );
						ObjRef newType = getType( newElements[j], typeOfElement, newArchRef );

						// we don't just do simple replace, we actually do deep
						// diff on the type
						if( createNewDiffPart && newDiffPart != null )
						{
							// since we created a new diff part, we now use it
							// to perform the deep diff on the types that the variant
							// points to.
							tempChanged = diffType( origType, newType, newDiffPart,
								newDesc[j] );

							newDiffPartChanged = newDiffPartChanged || tempChanged;

						}
						else
						{
							// just use the current diff part (used with just regular comp, conn)
							tempChanged = diffType( origType, newType, diffPart, newDesc[j] );

							isChanged = isChanged || tempChanged;
						}		
					}
					// if it doesn't have a type, then we need to do
					// deep-equals.  make sure they are actually the same element
					else if( !isSameElement( origElements[i], newElements[j], typeOfElement ) )
					{
					    // System.out.println( "replacing: " + typeOfElementToString( typeOfElement ) );

						// they aren't the same

						// remove old element
						addRemoveElement( diffPart, origElements[i], typeOfElement, diffLocation,
							origArchRef);
						// add in the new element
						addNewElement( diffPart, newElements[j], typeOfElement, diffLocation,
							newArchRef);

						isChanged = true;
					}
					break;
				}
			}

			if( !found )
			{
				// remove the orig element
				addRemoveElement( diffPart, origElements[i], typeOfElement, diffLocation,
					origArchRef);
				isChanged = true;
			}
		}
		// add all the elements left in the hash table.
		if( elementsToAdd.size( ) != 0 )
		{
			isChanged = true;
			Enumeration e = elementsToAdd.elements( );
			while( e.hasMoreElements( ) )
			{
				addNewElement( diffPart, ( ObjRef )e.nextElement( ),
					typeOfElement, diffLocation, newArchRef );
			}
		}

		// if elements were added to the new diff part, we can add it
		if( newDiffPartChanged )
		{
			xArch.add( diffPart, "DiffPart", newDiffPart );
			isChanged = true;
		}
		elementsToAdd = null;
		//System.out.println( "Exiting Diff Element Array!!");
		return isChanged;
	}

	// This function returns the string representation of
	// the type of element.
	protected String typeOfElementToString( int typeOfElement )
		throws PLADiffException
	{
		String temp;

		switch( typeOfElement )
		{
			case COMPONENT:
				temp = "Component";
				break;
			case CONNECTOR:
				temp = "Connector";
				break;
			case LINK:
				temp = "Link";
				break;
			case INTERFACE:
				temp = "Interface";
				break;
			case SIGNATURE:
				temp = "Signature";
				break;
			case VARIANT:
				temp = "Variant";
				break;
			case SIG_INTF_MAPPING:
				temp = "SignatureInterfaceMapping";
				break;
			case SUB_STRUCTURE:
				temp = "SubStructure";
				break;
			case OPTIONAL:
				temp = "Optional";
				break;
			case TYPE:
				temp = "Type";
				break;
			default:
				throw new PLADiffException( "Error: Unable to get string for typeOfElement " +
				    typeOfElement );
		}

		return temp;
	}

	// This function will get the objref to the actual type element
	// for the element passed in.
	protected ObjRef getType( ObjRef element, int typeOfElement, ObjRef archRef  )
		throws PLADiffException, MissingElementException, BrokenLinkException
	{
		ObjRef type = null;
		ObjRef typeLink;

		switch( typeOfElement )
		{
			case COMPONENT:
			case CONNECTOR:
			case INTERFACE:
			case SIGNATURE:
				typeLink = ( ObjRef )xArch.get( element, "Type" );

				if( typeLink != null )
				{
					type = resolveLink( typeLink, archRef );
				}
				else
				{
					System.err.println( "Warning: Architectural element " +
						( String )xArch.get( element, "Id" ) +
						" is missing its type." );
				}
				break;
			case VARIANT:
				typeLink = ( ObjRef )xArch.get( element, "VariantType" );
				if( typeLink != null )
				{
					type = resolveLink( typeLink, archRef );
				}
				else
				{
					ObjRef parent = xArch.getParent( element );
					System.err.println( "Warning: A variant inside " +
						( String )xArch.get( parent, "Id" ) +
						" is missing its type." );
				}
				break;

			default:
				throw new PLADiffException( "Error: Unable to get string for typeOfElement " +
					typeOfElement );
		}
		return type;
	}
	// This function gets the description of the element passed in.
	// The element does not support a description by default in XArchLibs,
	// then the appropriate description would be automatically generated.
	//
	// Note: the archRef is passed in so the links can be resolved properly based on
	// the right document
	protected String getDescription( ObjRef element, int typeOfElement, ObjRef archRef )
	    throws PLADiffException, BrokenLinkException, MissingElementException
	{
		String desc = null;
		ObjRef descRef;

		switch( typeOfElement )
		{
			// These elements have a description tag on their own,
			// so all we have to do is get it.
			case COMPONENT:
			case CONNECTOR:
			case LINK:
			case INTERFACE:
			case SIGNATURE:
			case TYPE:
			{
				descRef = (ObjRef) xArch.get( element, "Description" );

				if( descRef == null )
				{
				    String temp = typeOfElementToString( typeOfElement );
				    throw new MissingElementException( "Error: " + temp + " "
				        + ( String )xArch.get( element, "Id" ) +
				        " is missing its descrption." );
				}
				desc = ( String ) xArch.get( descRef, "Value" );
				break;
			}
			case VARIANT:
			case OPTIONAL:
			{
				// The description for variants is the guards
				ObjRef guard = ( ObjRef )xArch.get( element, "Guard" );

				if( guard == null )
				{
					ObjRef parent = xArch.getParent( element );
				    throw new MissingElementException( "Error: A variant inside "
				        + ( String )xArch.get( parent, "Id" ) +
				        " is missing its guard." );
				}

				desc = guardUtil.guardToString( guard );
				break;
			}
			case SIG_INTF_MAPPING:
			{
				// The description is a concat of the
				// descriptions of the outer sig and inner intf.

				// gets the links
				ObjRef outerSigLink = ( ObjRef ) xArch.get( element, "OuterSignature" );

				if( outerSigLink != null )
				{
					ObjRef innerIntfLink = ( ObjRef )xArch.get( element, "InnerInterface" );

					if( innerIntfLink != null )
					{
						ObjRef outerSig = resolveLink( outerSigLink, archRef);
						ObjRef innerIntf = resolveLink( innerIntfLink, archRef );

						descRef = ( ObjRef ) xArch.get( outerSig, "Description" );

						if( descRef != null )
						{
						    desc = ( String ) xArch.get( descRef, "Value" );

						    descRef = ( ObjRef ) xArch.get( innerIntf, "Description" );
						    if( descRef != null )
						    {
						        desc += " " + ( String ) xArch.get( descRef, "Value" );
						    }
						}

						if( descRef == null )
						{
							// sub struct
							ObjRef parent = xArch.getParent( element );
							// type
							ObjRef grandParent = xArch.getParent( parent );
						    throw new MissingElementException( "Error: SignatureInterfaceMapping inside "
						        + ( String )xArch.get( grandParent, "Id" ) +
						        " is missing descrption(s) on its endpoint(s)." );
						}
					}
					else
					{
						// sub struct
						ObjRef parent = xArch.getParent( element );
						// type
						ObjRef grandParent = xArch.getParent( parent );
						throw new MissingElementException( "Error: SignatureInterfaceMapping inside "
							+ ( String )xArch.get( grandParent, "Id" ) +
							" is missing its inner interface." );
					}
				}
				else
				{
					// sub struct
					ObjRef parent = xArch.getParent( element );
					// type
					ObjRef grandParent = xArch.getParent( parent );
					throw new MissingElementException( "Error: SignatureInterfaceMapping inside "
						+ ( String )xArch.get( grandParent, "Id" ) +
						" is missing its outer signature." );
				}
				break;
			}
			default:
			    throw new PLADiffException( "Error: Invalid getDescription call with typeOfElement " +
						typeOfElement + "." );
		}

		return desc;
	}

    // This function takes an element such as a component and first extracts the
	// link tag corresponding to the type parameter.  It then resolves the link
	// and returns the corresponding ObjRef
	//
	// link - the acutal ObjRef to the link that needs to be resolved
	// returns the ObjRef of the resolved href
	protected ObjRef resolveLink( ObjRef link, ObjRef archRef )
		throws BrokenLinkException, MissingElementException
	{
		String href = null;
		ObjRef result = null;

		href = ( String )xArch.get( link, "Href" );
		if( href != null )
		{
			result = ( ObjRef )xArch.resolveHref( archRef, href );

			if( result == null )
				throw new BrokenLinkException( "Error: Unable to resolve link: " +
					href );
		}
		else
		{
			throw new MissingElementException( "Error: Missing href for xml link " + link );
		}
		return result;
	}

	// This function returns whether or not the 2 elements passed in
	// are the same (semantically)
	protected boolean isSameElement( ObjRef origElement, ObjRef newElement,
	    int typeOfElement )
	    throws PLADiffException, MissingElementException, BrokenLinkException
	{
		boolean isSame = false;
		String type1;
		String type2;

		type1 = xArch.getType( origElement );
		type2 = xArch.getType( newElement );

		if( type1.equals( type2 ) )
		{
			isSame = true;
			// if they are both optional elements, check the optionality
			// This is the same for everyone
			if( isOptional( origElement ) )
			{
				// since the types of original and the new elements are the same.
				// if the original element has an optional, so will the new one
				ObjRef origOptional, newOptional;
				origOptional = ( ObjRef )xArch.get( origElement, "Optional" );
				newOptional = ( ObjRef )xArch.get( newElement, "Optional" );

				isSame = isSameOptional( origOptional, newOptional );
			}
			//isSame = true;

			switch( typeOfElement )
			{
				case INTERFACE:
				case SIGNATURE:
					if( isSame )
					{
						// check direction and type.
						ObjRef origType = getType( origElement, typeOfElement, origArchRef );
						ObjRef newType = getType( newElement, typeOfElement, newArchRef );

						// they are the same if they have the same type AND
						// same direction
						isSame = xArch.isEquivalent( origType, newType ) &&
								 isSameDirection( origElement, newElement );
					}
					break;
				case LINK:
				case SIG_INTF_MAPPING:
					// we don't check the endpoints, optional/guard already checked
					break;
				default:
					throw new PLADiffException( "Error: Invalid comparison call on typeOfElement " +
						typeOfElement + "." );
			}
		}
		return isSame;
	}

	// This function returns true if the element passed in is some
	// form of an optional element
	protected boolean isOptional( ObjRef element )
	{
		return 	xArch.isInstanceOf( element, "edu.uci.isr.xarch.options.IOptionalComponent" ) ||
				xArch.isInstanceOf( element, "edu.uci.isr.xarch.options.IOptionalConnector" ) ||
				xArch.isInstanceOf( element, "edu.uci.isr.xarch.options.IOptionalLink" )      ||
			   	xArch.isInstanceOf( element, "edu.uci.isr.xarch.options.IOptionalInterface" ) ||
			  	xArch.isInstanceOf( element, "edu.uci.isr.xarch.options.IOptionalSignature" ) ||
				xArch.isInstanceOf( element, "edu.uci.isr.xarch.options.IOptionalSignatureInterfaceMapping" );
	}

	// This function takes in two elements with direction (i.e. interfaces, signatures)
	// and checks to see if they have the same direction (both in, out, or inout).
	protected boolean isSameDirection( ObjRef origElement, ObjRef newElement )
		throws MissingElementException
	{
		ObjRef origDirection, newDirection;
		boolean isSame = false;
		if( origElement != null && newElement != null )
		{
			origDirection = ( ObjRef ) xArch.get( origElement, "Direction" );
			if( origDirection == null )
			{
				throw new MissingElementException( "Error: Element " +
					( String )xArch.get( origElement, "Id" ) +
					" in original architecture is missing a direction." );
			}

			newDirection = ( ObjRef )xArch.get( newElement, "Direction" );
			if( origDirection == null )
			{
				throw new MissingElementException( "Error: Element " +
					( String )xArch.get( newElement, "Id" ) +
					" in new architecture is missing a direction." );
			}

			String origValue, newValue;

			origValue = ( String )xArch.get( origDirection, "Value" );
			newValue = ( String )xArch.get( newDirection, "Value" );

			isSame = origValue.equals( newValue );
		}
		return isSame;
	}

	// This function takes in two optional and checks to see if they are
	// the same optional (i.e. do they have the same guard?).
	// This function will return false if one of the optionals is null
	protected boolean isSameOptional( ObjRef origOptional, ObjRef newOptional )
	    throws MissingElementException
	{
	    boolean isSame = false;

		ObjRef origGuard, newGuard;

		if( origOptional != null && newOptional != null )
		{
			origGuard = ( ObjRef ) xArch.get( origOptional, "Guard" );
			newGuard = ( ObjRef ) xArch.get( newOptional, "Guard" );

			if( origGuard == null )
			{
				ObjRef parent = xArch.getParent( origOptional );
			    throw new MissingElementException( "Error: In original doc, optional element inside " +
			        ( String )xArch.get( parent, "Id" ) + " is missing its guard." );
			}
			if( newGuard == null )
			{
				ObjRef parent = xArch.getParent( newOptional );
			    throw new MissingElementException( "Error: In new doc, optional element inside " +
			        ( String )xArch.get( parent, "Id" ) + " is missing its guard." );
			}

			// now check to see if they are the same guards
		    String guard1 = guardUtil.guardToString( origGuard );
            String guard2 = guardUtil.guardToString( newGuard );
    	    //isSame = guard1.equals( guard2 );
			isSame = xArch.isEquivalent( origGuard, newGuard );
		}
		// they are both null
		else if( origOptional == null && newOptional == null )
		{
			isSame = true;
		}
		// else, do nothing.  false by default

		return isSame;
	}
}
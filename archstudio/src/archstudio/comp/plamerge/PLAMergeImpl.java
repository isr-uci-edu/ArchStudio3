package archstudio.comp.plamerge;

// Add support for xArchADT
import edu.uci.ics.xarchutils.*;

import c2.fw.*;

// Standard imports
import java.util.*;
import java.io.*;

// Import for unique identifier generation
import c2.util.UIDGenerator;


public class PLAMergeImpl implements IPLAMerge
{
	// Helper classes for our link table which will contain a link as value,
	// and a vector of two InterfaceEndPoint objects.
	private class InterfaceEndPoint
	{
		String elementDescription;
		String interfaceDescription;
	}

	// Similar deal for signature interface mappings (SIMs)
	private class SignatureEndPoint
	{
		String elementDescription;
		String signatureDescription;
	}

	// This inner helper class acts as a means to collect together all the arguments
	// that would be needed by most of the methods in PLAMergeImpl.
	// The advantage to using this class is to group together variables that will
	// be used by a particular instance of use of the PLAMerge interface and allowing
	// for reentrant code.
	private class MergeArguments
	{
		public ObjRef diffArch;			// reference to diff document
		public ObjRef mergeArch;		// reference to xArch element of architecture being merged into
		public Hashtable linksTable;	// table to keep track of all links and their InterfaceEndPoints
		public Hashtable simsTable;		// table to keep track of all SIMs and their InterfaceEndPoints and SignatureEndPoints
		public Vector typesList;		// list to keep track of all types that were changed
	}

	// Utility to convert guards to strings
	GuardToString guardUtil;

	// Required to generate unique IDs
	protected UIDGenerator generator;

	// Interface through which DOM-based xArch libraries can be accessed
	protected XArchFlatInterface xarch;

	// Size for the hashtable
	private static final int SIZE = 100;

	/**
	 * Constructor for the PLAMergeImpl class.
	 *
	 * @param xArch		The interface to the DOM-based xArch libraries.
	 */
	public PLAMergeImpl(XArchFlatInterface xArch)
	{
		xarch = xArch;
		guardUtil = new GuardToString(xarch);
		generator = new UIDGenerator();
	}

	/**
	 * This is the function that is responsible for calling the merge algorithm.
	 *
	 * The PLAMerge component starts at a specified location (either a Type or an ArchStructure) and adds and removes the respective elements to the location.
         * The merging algorithm first makes the appropriate changs to the root DiffPart.  It finds the proper location based on the Locationelement defined in the PLA diff 
         * document. Then the algorithm processes the corresponding adds and removes within that Diff Location.  If the current DiffPart has another DiffPart embedded in it, 
         * then the merge algorithm processes that DiffPart recursively.  It searches through the elements in the current location and finds the new component that it must 
         * modify specified by the Location element.  Once found, the subsequent adds and removes are made within this location.  This processes continues recursively till 
         * all DiffParts are processed.
	 *
	 * @param diffURL 		This is the URL of the xADL document that holds the diff
	 * @param archURL		This is the URL of the xADL document that will have the diff merged into it
	 * @param targetArchURL This is the URL of the new xADL document that will be created and store the merged architecture
	 * @param startingID 	This is the ID of the element that the merge algorithm should start from
	 * @param isStructural 	If this is true then the startingID is that of an archStruct, otherwise it is an
	 *							ID to a type
	 *
	 * @throws InvalidURLException 			If the provided URL to open an xArch document is invalid
	 * @throws InvalidElementIDException 	If the provided starting ID is invalid
	 */
	public void merge(String diffURL, String archURL, String targetArchURL, String startingID, boolean isStructural)
		throws InvalidURLException, InvalidElementIDException, MissingElementException,
		MissingAttributeException, BrokenLinkException
	{		
		ObjRef diffArch = xarch.getOpenXArch(diffURL);
		if(diffArch == null)
		{
			throw new InvalidURLException("Error: The URL \"" + diffURL + "\" does not point to an open xArch");
		}

		ObjRef origArch = xarch.getOpenXArch(archURL);
		if(origArch == null)
		{
			throw new InvalidURLException("Error: The URL \"" + archURL + "\" does not point to an open xArch");
		}

		if(targetArchURL == null || targetArchURL.length() == 0)
		{
			throw new InvalidURLException("Error: The provided target URL is invalid.  Please provide a non-empty URL");
		}

		// generate a random ID for the location of the cloned diff document
		String tempDiff = UIDGenerator.generateUID();

		MergeArguments arguments = new MergeArguments();
		arguments.linksTable = new Hashtable(SIZE);
		arguments.simsTable = new Hashtable(SIZE);
		arguments.typesList = new Vector(SIZE);
		arguments.diffArch  = xarch.cloneXArch(diffArch, tempDiff);
		arguments.mergeArch = xarch.cloneXArch(origArch, targetArchURL);
		
		mergeArch(arguments, startingID, isStructural);

		xarch.close(tempDiff);
	}

	// This method is a helper to the merge method. This method takes the provided parameters and
	// decides whether to start merging on a structure or on a type.
	private void mergeArch(MergeArguments arguments, String startingID, boolean isStructural)
		throws InvalidElementIDException, MissingElementException, MissingAttributeException, BrokenLinkException
	{
		ObjRef startingElement = xarch.getByID(arguments.mergeArch, startingID);

		// Check if element exists
		if(startingElement == null)
		{
			throw new InvalidElementIDException("Error: No element exists for the ID \"" + startingID + "\"");
		}

		ObjRef diffContext = xarch.createContext(arguments.diffArch, "Pladiff");
		ObjRef diff = xarch.getElement(diffContext, "PLADiff", arguments.diffArch);
		//ObjRef diff = (ObjRef)xarch.get(diffContext, "Diff");
		ObjRef diffPart = (ObjRef)xarch.get(diff, "DiffPart");

		if(diffPart == null)
			return;
			
		if(isStructural)
		{
			mergeStructure(startingElement, diffPart, arguments);
		}
		else
		{
			mergeType(startingElement, diffPart, arguments);
		}
		
	}

	private void mergeStructure(ObjRef structure, ObjRef diffPart, MergeArguments arguments)
		throws MissingElementException, MissingAttributeException, BrokenLinkException
	{
		// Get all the necessary data for merging
		ObjRef[] compRefs = xarch.getAll(structure, "Component");
		ObjRef[] connRefs = xarch.getAll(structure, "Connector");
		ObjRef[] linkRefs = xarch.getAll(structure, "Link");
		ObjRef[] compconnRefs = getCompConnRefs(structure, compRefs, connRefs);

		//types contains the ObjRefs for those types that need fixing of their SIMs.
		//We should only be adding to types, when we add/remove a compoennt/connector/interface/signature
		Vector types = new Vector();
		Vector simsToAdd = new Vector(); //simsToAdd is addTypeEntity
		Vector sigsToRemove = new Vector(); //used to delay the removal of sigs

		// Get all Removes in the diffPart and iterate over them
		ObjRef[] removeRefs = xarch.getAll(diffPart, "Remove");
		for(int i = 0; i < removeRefs.length; ++i)
		{
			ObjRef removeRef = removeRefs[i];

			ObjRef removeStructuralEntityRef = (ObjRef)xarch.get(removeRef, "RemoveStructuralEntity");
			ObjRef removeTypeEntityRef = (ObjRef)xarch.get(removeRef, "RemoveTypeEntity");

			// If this remove is the removal of a structural entity
			if(removeStructuralEntityRef != null)
			{
				ObjRef comp = (ObjRef)xarch.get(removeStructuralEntityRef, "Component");
				ObjRef conn = (ObjRef)xarch.get(removeStructuralEntityRef, "Connector");
				ObjRef link = (ObjRef)xarch.get(removeStructuralEntityRef, "Link");
				ObjRef removeInterface = (ObjRef)xarch.get(removeStructuralEntityRef, "RemoveInterface");
				ObjRef removeOptional = (ObjRef)xarch.get(removeStructuralEntityRef, "RemoveOptional");
				if(comp != null)
				{
					removeElement(true, comp, compRefs, "Component", arguments);
				}
				else if(conn != null)
				{
					removeElement(true, conn, connRefs, "Connector", arguments);
				}
				else if(link != null)
				{
					removeElement(true, link, linkRefs, "Link", arguments);
				}
				else if(removeInterface != null)
				{
					removeElement(true, removeInterface, compconnRefs, "Interface", arguments);
				}
				else if(removeOptional != null)
				{
					removeElement(true, removeOptional, compconnRefs, "Optional", arguments);
				}
			}

			// If this remove is the removal of a type-based entity
			else if(removeTypeEntityRef != null)
			{
				if(xarch.get(removeTypeEntityRef, "Signature") != null)
				{
				    sigsToRemove.add(removeTypeEntityRef);
					//removeElement(true, removeTypeEntityRef, compconnRefs, "Signature", arguments);
				}
				else if(xarch.get(removeTypeEntityRef, "SubArchitecture") != null)
				{
					removeElement(true, removeTypeEntityRef, compconnRefs, "SubArchitecture", arguments);
				}
				else if(xarch.get(removeTypeEntityRef, "Variant") != null)
				{
					removeElement(true, removeTypeEntityRef, compconnRefs, "Variant", arguments);
				}
				else if(xarch.get(removeTypeEntityRef, "RemoveSignatureInterfaceMapping") != null)
				{
					removeElement(true, removeTypeEntityRef, compconnRefs, "SignatureInterfaceMapping", arguments);
				}
			}
		}

		//delay the removal of signatures to avoid case where sigs are removed before connected SIMs
		for(int i = 0; i < sigsToRemove.size(); i++)
		{
			removeElement(true, (ObjRef)sigsToRemove.get(i), compconnRefs, "Signature", arguments);
		}

		// Now deal with all Adds
		ObjRef[] addRefs = xarch.getAll(diffPart, "Add");
		for(int i = 0; i < addRefs.length; ++i)
		{
			ObjRef addRef = addRefs[i];
			ObjRef addStructuralEntityRef = (ObjRef)xarch.get(addRef, "AddStructuralEntity");
			ObjRef addTypeEntityRef = (ObjRef)xarch.get(addRef, "AddTypeEntity");

			// If this is the addition of a structural entity
			if(addStructuralEntityRef != null)
			{
				ObjRef comp = (ObjRef)xarch.get(addStructuralEntityRef, "Component");
				ObjRef conn = (ObjRef)xarch.get(addStructuralEntityRef, "Connector");
				ObjRef addInterface = (ObjRef)xarch.get(addStructuralEntityRef, "AddInterface");
				ObjRef addLink = (ObjRef)xarch.get(addStructuralEntityRef, "AddLink");
				ObjRef addOptional = (ObjRef)xarch.get(addStructuralEntityRef, "AddOptional");

				if(comp != null)
				{
					addElement(true, structure, comp, compRefs, "Component", arguments);
				}
				else if(conn != null)
				{
					addElement(true, structure, conn, connRefs, "Connector", arguments);
				}
				else if(addInterface != null)
				{
					addElement(true, structure, addInterface, compconnRefs, "Interface", arguments);
				}
				else if(addLink != null)
				{
					addElement(true, structure, addLink, linkRefs, "Link", arguments);
				}
				else if(addOptional != null)
				{
					addElement(true, structure, addOptional, compconnRefs, "Optional", arguments);
				}
			}
			// If this is the addition of a type entity
			else if(addTypeEntityRef != null)
			{
				if(xarch.get(addTypeEntityRef, "AddSignatureInterfaceMapping") != null)
				{
					simsToAdd.add(addTypeEntityRef);
				}
				else if(xarch.get(addTypeEntityRef, "Signature") != null)
				{
					addElement(true, structure, addTypeEntityRef, compconnRefs, "Signature", arguments);
				}
				else if(xarch.get(addTypeEntityRef, "SubArchitecture") != null)
				{
					addElement(true, structure, addTypeEntityRef, compconnRefs, "SubArchitecture", arguments);
				}
				else if(xarch.get(addTypeEntityRef, "Variant") != null)
				{
					addElement(true, structure, addTypeEntityRef, compconnRefs, "Variant", arguments);
				}
			}
		}

		//delays adding of sims
		for(int i = 0; i < simsToAdd.size(); i++)
		{
			addElement(true, structure, (ObjRef)simsToAdd.get(i), compconnRefs, "SignatureInterfaceMapping", arguments);
		}

		ObjRef[] diffPartRefs = xarch.getAll(diffPart, "DiffPart");
		for(int i = 0; i < diffPartRefs.length; ++i)
		{
			ObjRef diffPartRef = diffPartRefs[i];
			ObjRef diffLocation = (ObjRef)xarch.get(diffPartRef, "DiffLocation");
			ObjRef location = (ObjRef)xarch.get(diffLocation, "Location");
			ObjRef compconnRef = null;

			// Find a component/connector that has the same description as the location specified by the diffpart
			for(int j = 0; j < compconnRefs.length; ++j)
			{
				if(xarch.has(compconnRefs[j], "Description", location))
				{
					compconnRef = compconnRefs[j];
					break;
				}
			}

			ObjRef linkRef = (ObjRef)xarch.get(compconnRef, "Type");
			ObjRef typeRef = resolveLink(arguments.mergeArch, linkRef, (String)xarch.get(compconnRef, "Id"));
			
			ObjRef variants[] = xarch.getAll( typeRef, "Variant" );

			if( variants != null && variants.length != 0 )
			{
				// there are variatns so we merge into variant type
				mergeVariantType(typeRef, diffPartRef, arguments);
			}
			else
			{
				ObjRef subStructure = (ObjRef)xarch.get(typeRef, "SubArchitecture");
				ObjRef archStrucLink = (ObjRef)xarch.get(subStructure, "ArchStructure" );
				String archStrucHref = (String)xarch.get(archStrucLink, "Href");
				ObjRef archStructure = xarch.resolveHref(arguments.mergeArch, archStrucHref);
				mergeStructure(archStructure, diffPartRef, arguments);
			}
		}

		// Check for non-connecting links in this structure
		fixStructure(structure, arguments.linksTable);
		
		fixTypes(arguments.typesList, arguments);
	}

	// Resolves a link to an element and returns it
	//
	// arch - The xArch ref of the architecture where the link and other connecting elements exist
	// link - Reference to the link from the connecting parent element
	// parentID - Id of the element requesting the resolved link
	private ObjRef resolveLink(ObjRef arch, ObjRef link, String parentID)
		throws MissingElementException, MissingAttributeException, BrokenLinkException
	{
		String hRef = "";
		ObjRef resultRef;

		if(link == null)
		{
			throw new MissingElementException("Error: Missing link-containing element in element with id: " + parentID);
		}

		hRef = (String)xarch.get(link, "Href");
		if(hRef == null)
		{
			throw new MissingAttributeException("Error: Missing Href on element found within item at id: " + parentID);
		}

		resultRef = xarch.resolveHref(arch, hRef);
		if(resultRef == null)
		{
			throw new BrokenLinkException("Error: Broken link. Corresponding link with Href: \"" + hRef + "\" does not exist");
		}

		return resultRef;
	}

	// This method returns a list of references to all components and connectors in a given structure
	//
	// structure - The structure of interest to get the components and connectors
	private ObjRef[] getCompConnRefs(ObjRef structure)
	{
		ObjRef[] compRefs = xarch.getAll(structure, "Component");
		ObjRef[] connRefs = xarch.getAll(structure, "Connector");
		return getCompConnRefs(structure, compRefs, connRefs);
	}

	// This method returns a list of references to all components and connectors in a given structure
	//
	// structure - The structure of interest to get the components and connectors
	// compRefs  - The collection of component refs to use
	private ObjRef[] getCompConnRefs(ObjRef structure, ObjRef[] compRefs, ObjRef[] connRefs)
	{
		ObjRef[] compconnRefs;

		// Construct an array containing all the component and connector refs
		// Note: this is potentially a waste of space, maybe optimize later.
		ArrayList compconnList = new ArrayList(Arrays.asList(compRefs));
		compconnList.addAll(Arrays.asList(connRefs));
		compconnRefs = new ObjRef[compconnList.size()];
		compconnList.toArray(compconnRefs);

		return compconnRefs;
	}

	// isStructural - Whether this is structural or a type entity to add to
	// container	- A structure or type to merge into
	// add			- IAdd with add information
	// refs			- Object refs to inspect to add into.  The objrefs passed in depend on the typeOfThing passed in.
	// typeOfThing	- The type of thing we're trying to add, "Component", etc.
	// arguments	- The MergeArguments object
	private void addElement(boolean isStructural, ObjRef container, ObjRef add, ObjRef[] refs, String typeOfThing, MergeArguments arguments)
		throws MissingElementException, MissingAttributeException, BrokenLinkException
	{
		// create the types context
		ObjRef typesContext = xarch.createContext(arguments.mergeArch, "types");
		ObjRef variantsContext = xarch.createContext(arguments.mergeArch, "variants");
		ObjRef optionsContext = xarch.createContext(arguments.mergeArch, "options");
		
		
		// If we are adding an element while merging into a structure
		if(isStructural)
		{			
			if(typeOfThing.equals("Component") ||
				typeOfThing.equals("Connector"))
			{				
				// recontextualize
				add = xarch.recontextualize(typesContext, typeOfThing, add);		
				
				// Get element description
				ObjRef addDesc = (ObjRef)xarch.get(add, "Description");
				String addDescValue = (String)xarch.get(addDesc, "Value");
				
				// Check to see if element exists or not in this structure
				for(int i = 0; i < refs.length; ++i)
				{
					ObjRef elementDesc = (ObjRef)xarch.get(refs[i], "Description");
					String elementDescValue = (String)xarch.get(elementDesc, "Value");
					
					if(addDescValue.equals(elementDescValue))
					{
						// Get container description
						ObjRef containerDesc = (ObjRef)xarch.get(container, "Description");
						String containerDescValue = (String)xarch.get(containerDesc, "Value");
	
						// Notify user of error and leave method
						issueAddExistingElementWarning(typeOfThing, elementDescValue, containerDescValue);
						return;
					}				
				}										
				
				// Add element to structure
				xarch.add(container, typeOfThing, add);
			}
			else if(typeOfThing.equals("Interface"))
			{												
				// Add is considered to be an AddInterface so extract element description and actual IInterface
				ObjRef elementDescriptionRef = (ObjRef)xarch.get(add, "ElementDescription");
				ObjRef interfaceRef = (ObjRef)xarch.get(add, "Interface");
								
				// recontextualize
				interfaceRef = xarch.recontextualize(typesContext, typeOfThing, interfaceRef);

				// Description of interface to look for
				ObjRef interfaceDesc = (ObjRef)xarch.get(interfaceRef, "Description");
				String interfaceDescription = (String)xarch.get(interfaceDesc, "Value");
	
				// Find an element with a description matching the element description and add the interface to it
				for(int i = 0; i < refs.length; ++i)
				{
					if(xarch.has(refs[i], "Description", elementDescriptionRef))
					{
						// check if interface already exists
						ObjRef[] intfsToCheck = xarch.getAll(refs[i], "Interface");
						for(int j = 0; j < intfsToCheck.length; j++)
						{
							// Description of current interface
							ObjRef intf2CheckDesc = (ObjRef)xarch.get(intfsToCheck[j], "Description");
							String intf2CheckDescription = (String)xarch.get(intf2CheckDesc, "Value");
							
							if(intf2CheckDescription.equals(interfaceDescription))
							{
								// Get container description
								ObjRef containerDesc = (ObjRef)xarch.get(container, "Description");
								String containerDescValue = (String)xarch.get(containerDesc, "Value");
	
								// Notify user of error and leave method
								issueAddExistingElementWarning(typeOfThing, interfaceDescription, containerDescValue);
								return;
							}
						}
						xarch.add(refs[i], "Interface", interfaceRef);
						return;
					}
				}
				//get description of container
				ObjRef containerDesc = (ObjRef)xarch.get(container, "Description");
				String containerDescValue = (String)xarch.get(containerDesc, "Value");
				issueElementNotFoundWarning(typeOfThing, (String)xarch.get(elementDescriptionRef, "Value"), containerDescValue);
			}
			else if(typeOfThing.equals("Link"))
			{								
				ObjRef linkRef = (ObjRef)xarch.get(add, "Link");
				
				// recontextualize
				linkRef = xarch.recontextualize(typesContext, typeOfThing, linkRef);
				
				// check to see if link already exists
				ObjRef linkDesc = (ObjRef)xarch.get(linkRef, "Description");
				String linkDescription = (String)xarch.get(linkDesc, "Value");
				for (int i = 0; i < refs.length; i++)
				{
					ObjRef desc2Check = (ObjRef)xarch.get(refs[i], "Description");
					String description2Check = (String)xarch.get(desc2Check, "Value");
					if(description2Check.equals(linkDescription))
					{
						// Get container description
						ObjRef containerDesc = (ObjRef)xarch.get(container, "Description");
						String containerDescValue = (String)xarch.get(containerDesc, "Value");
	
						// Notify user of error and leave method
						issueAddExistingElementWarning(typeOfThing, linkDescription, containerDescValue);
						return;
					}
				}
				
				xarch.add(container, typeOfThing, linkRef);
	
				// Add links' endpoints to the linksTable when adding a link
				addToLinksTable(add, arguments);
			}	
			else if(typeOfThing.equals("Optional"))
			{
				ObjRef optionalRef = (ObjRef)xarch.get(add, "Optional");
				ObjRef elementDesc = (ObjRef)xarch.get(add, "ElementDescription");
				
				// recontextualize 
				optionalRef = xarch.recontextualize(optionsContext, typeOfThing, optionalRef);
				
				// find element with the element description and then set the optional to that element
				for(int i = 0; i < refs.length; ++i)
				{
					if(xarch.has(refs[i], "Description", elementDesc))
					{
						// Check if an optional element already exists
						if(xarch.get(refs[i], "Optional") != null)
						{
							String elementDescValue = (String)xarch.get(elementDesc, "Value");
							issueAddExistingElementWarning(typeOfThing, "", elementDescValue);
							return;
						}

						xarch.set(refs[i], "Optional", optionalRef);
						return;
					}
				}
				//get container description
				ObjRef containerDesc = (ObjRef)xarch.get(container, "Description");
				String containerDescValue = (String)xarch.get(containerDesc, "Value");
				issueElementNotFoundWarning(typeOfThing, (String)xarch.get(elementDesc, "Value"), containerDescValue);
			}
	
			else if(typeOfThing.equals("Signature") ||
				typeOfThing.equals("SubArchitecture") ||
				typeOfThing.equals("Variant") ||
				typeOfThing.equals("SignatureInterfaceMapping"))
			{
				// add is considered to be a AddTypeEntity								
				ObjRef diffLocationRef = (ObjRef)xarch.get(add, "DiffLocation");
				ObjRef locationRef = (ObjRef)xarch.get(diffLocationRef, "Location");
				for(int i = 0; i < refs.length; ++i)
				{
					if(xarch.has(refs[i], "Description", locationRef))
					{
						ObjRef xmlLinkRef = (ObjRef)xarch.get(refs[i], "Type");
						ObjRef typeRef = resolveLink(arguments.mergeArch, xmlLinkRef, (String)xarch.get(refs[i], "Id"));
						addTypeElement(typeRef, add, typeOfThing, arguments);
						return;
					}
				}		
			}
		}
		
		// We are merging into a variant type, not a structure, at this point
		else
		{
			if(typeOfThing.equals("Signature") ||
				typeOfThing.equals("SubArchitecture") ||
				typeOfThing.equals("Variant") ||
				typeOfThing.equals("SignatureInterfaceMapping"))
			{
				// add is considered to be a AddTypeEntity				
				ObjRef diffLocationRef = (ObjRef)xarch.get(add, "DiffLocation");
				ObjRef locationRef = (ObjRef)xarch.get(diffLocationRef, "Location");
				String locationValueRef = (String)xarch.get(locationRef, "Value");
				for(int i = 0; i < refs.length; ++i)
				{
					ObjRef guardRef = (ObjRef)xarch.get(refs[i], "Guard");
					String guardValueRef = guardUtil.guardToString(guardRef);		
					if(guardValueRef.equals(locationValueRef))
					{
						ObjRef xmlLinkRef = (ObjRef)xarch.get(refs[i], "VariantType");
						ObjRef typeRef = resolveLink(arguments.mergeArch, xmlLinkRef, guardValueRef);
						addTypeElement(typeRef, add, typeOfThing, arguments);
					}
				}
			}
		}

		// Note: possibly make sure links descriptions adhere to Menage... and possibly not.
		
	}
	
	private void addTypeElement(ObjRef typeRef, ObjRef add, String typeOfThing, MergeArguments arguments)
		throws MissingElementException, BrokenLinkException, MissingAttributeException
	{
		// create the types context
		ObjRef typesContext = xarch.createContext(arguments.mergeArch, "types");
		ObjRef variantsContext = xarch.createContext(arguments.mergeArch, "variants");
		ObjRef instanceContext = xarch.createContext(arguments.mergeArch, "instance");
		
		if(typeOfThing.equals("Signature"))
		{
			ObjRef elementRef = (ObjRef)xarch.get(add, typeOfThing);
			
			// recontextualize
			elementRef = xarch.recontextualize(typesContext, typeOfThing, elementRef);
			
			// Description of this signature
			ObjRef descRef = (ObjRef)xarch.get(elementRef, "Description");
			String descRefValue = (String)xarch.get(descRef, "Value");
			
			// Check if this signature exists already in this type
			ObjRef[] signatureRefs = xarch.getAll(typeRef, "Signature");
			for(int j = 0; j < signatureRefs.length; ++j)
			{				
				ObjRef sigDesc = (ObjRef)xarch.get(signatureRefs[j], "Description");
				String sigDescValue = (String)xarch.get(sigDesc, "Value");
				if(descRefValue.equals(sigDescValue))
				{
					ObjRef typeDesc = (ObjRef)xarch.get(typeRef, "Description");
					String typeDescValue = (String)xarch.get(typeDesc, "Value");
					issueAddExistingElementWarning(typeOfThing, descRefValue, typeDescValue);
					return;
				}
			}
				
			xarch.add(typeRef, typeOfThing, elementRef);
		}
		else if(typeOfThing.equals("Variant"))
		{
			ObjRef elementRef = (ObjRef)xarch.get(add, typeOfThing);
			
			// recontextualize
			elementRef = xarch.recontextualize(variantsContext, typeOfThing, elementRef);
			
			// Get guard of this variant
			ObjRef guardRef = (ObjRef)xarch.get(elementRef, "Guard");
			String guardRefValue = guardUtil.guardToString(guardRef);
			
			// Check if this variant exists already in this type
			ObjRef[] variantRefs = xarch.getAll(typeRef, "Variant");
			for(int j = 0; j < variantRefs.length; ++j)
			{
				ObjRef varGuard = (ObjRef)xarch.get(variantRefs[j], "Guard");
				String varGuardValue = guardUtil.guardToString(varGuard);
				if(guardRefValue.equals(varGuardValue))
				{
					ObjRef typeDesc = (ObjRef)xarch.get(typeRef, "Description");
					String typeDescValue = (String)xarch.get(typeDesc, "Value");
					issueAddExistingElementWarning(typeOfThing, guardRefValue, typeDescValue);
					return;
				}
			}
			
			xarch.add(typeRef, typeOfThing, elementRef);
		}
		else if(typeOfThing.equals("SubArchitecture"))
		{
			ObjRef elementRef = (ObjRef)xarch.get(add, typeOfThing);
			String subArchDesc = (String)xarch.get(elementRef, "Value");
			// Check if type already has a SubArchitecture
			if(xarch.get(typeRef, typeOfThing) != null)
			{
				ObjRef typeDesc = (ObjRef)xarch.get(typeRef, "Description");
				String typeDescValue = (String)xarch.get(typeDesc, "Value");
				issueAddExistingElementWarning(typeOfThing, subArchDesc, typeDescValue);
				return;
			}
			
			ObjRef[] archStructures = xarch.getAllElements(typesContext, "ArchStructure", 
				arguments.mergeArch);				
			int i;
			ObjRef archDescRef;
			String archDesc;
			boolean found = false;
			for(i = 0; i < archStructures.length; i++)
			{
				archDescRef = (ObjRef)xarch.get(archStructures[i], "Description");
				archDesc = (String)xarch.get(archDescRef, "Value");
				if(subArchDesc.equals(archDesc))
				{
					found = true;
					break;
				}
			}
			if( found )
			{
				// we found the arch structure so we first make a copy of it
				ObjRef dupSubArch = xarch.cloneElement(archStructures[i], 
					edu.uci.isr.xarch.IXArchElement.DEPTH_INFINITY);

				// add it to the document
				xarch.add( arguments.mergeArch, "Object", dupSubArch );

				String subArchID = (String)xarch.get(dupSubArch, "Id");
				
				ObjRef subArchRef = xarch.create(typesContext, "SubArchitecture");
				ObjRef archLinkRef = xarch.create(instanceContext, "XMLLink");
				
				// set the href as #id of the duplicated subarchitecture
				xarch.set(archLinkRef, "Href", "#" + subArchID);
				xarch.set(archLinkRef, "Type", "simple");
				
				// now set the XML link to the sub architecture
				xarch.set(subArchRef, "ArchStructure", archLinkRef);
				
				// now connect the sub architecture with the type
				xarch.set(typeRef, "SubArchitecture", subArchRef);
				
				// now regenerate the description of the sub architecture based on the new type info
				fixArchStructureDescription(dupSubArch, arguments.mergeArch);
				
				// Add all of the links in the duplicated architecture to the linksTable (they
				// need to be fixed)
				ObjRef[] newLinks = xarch.getAll(dupSubArch, "Link");
				
				//System.out.println("Number of links: " + newLinks.length);				
				for(int j = 0; j < newLinks.length; ++j)
				{
					addToLinksTable(newLinks[j], arguments, true);
				}
				fixStructure(dupSubArch, arguments.linksTable);
			}
			else
			{
				issueElementNotFoundWarning( typeOfThing, subArchDesc, "document" ); 
			}
		}
		else if(typeOfThing.equals("SignatureInterfaceMapping"))
		{
			ObjRef addSIMRef = (ObjRef)xarch.get(add, "AddSignatureInterfaceMapping");
			ObjRef simRef = (ObjRef)xarch.get(addSIMRef, typeOfThing);
			simRef = xarch.recontextualize(typesContext, typeOfThing, simRef);
			//get the subarchitecture and add the SIM to the subarchitecture
			ObjRef subArch = (ObjRef)xarch.get(typeRef, "SubArchitecture");
			if(subArch == null)
			{
				System.out.println("Could not add Mapping, SubArchitecture does not exist");
				return;
			}
			
			// Check if this SIM exists in the specified subarchitecture already
			String simDesc = generateSIMDescription(simRef, arguments);
			ObjRef[] simRefs = xarch.getAll(subArch, typeOfThing);
			for(int i = 0; i < simRefs.length; ++i)
			{
				String simToCheckDesc = generateSIMDescription(simRefs[i], arguments);
				if(simDesc.equals(simToCheckDesc))
				{
					// Sub archs do not have descriptions
					// If it exists issue a warning and leave the method (i.e., don't add SIM)
					ObjRef typeDesc = (ObjRef)xarch.get(typeRef, "Description");
					String typeDescValue = (String)xarch.get(typeDesc, "Value");
					issueAddExistingElementWarning(typeOfThing, simDesc, typeDescValue);
					return;
				}
			}

			xarch.add(subArch, typeOfThing, simRef);
			//add to sims table
			addToSIMSTable(addSIMRef, arguments);
			//add to types list
			arguments.typesList.add(typeRef);
		}
	}
	
	// This method adds a Link's endpoints to the linksTable
	//
	// addLink		- A reference to a IAddLink
	// arguments	- The merge arguments
	private void addToLinksTable(ObjRef addLink, MergeArguments arguments)
	{
		Vector endPointRefs = new Vector(Arrays.asList(xarch.getAll(addLink, "InterfaceEndPoint")));
		ObjRef linkRef = (ObjRef)xarch.get(addLink, "Link");
		int size = endPointRefs.size();
		Vector endPoints = new Vector(size);
		for(int i = 0; i < size; ++i)
		{
			ObjRef endPointRef = (ObjRef)endPointRefs.get(i);
			InterfaceEndPoint endPoint = new InterfaceEndPoint();
			ObjRef elementDescriptionRef = (ObjRef)xarch.get(endPointRef, "ConnectingElementDescription");
			endPoint.elementDescription = (String)xarch.get(elementDescriptionRef, "Value");
			ObjRef interfaceDescriptionRef = (ObjRef)xarch.get(endPointRef, "InterfaceDescription");
			endPoint.interfaceDescription = (String)xarch.get(interfaceDescriptionRef, "Value");
			endPoints.add(endPoint);
		}
		arguments.linksTable.put(xarch.get(linkRef, "Id"), endPoints);
		
		//System.out.println("In addToLInksTable, linkRef is = " + linkRef);
	}
	
	/*
	 * Adds the link passed in to the linksTable. The boolean is only there to distinguish from the other addToLinkstable
	 * function
	 */
	private void addToLinksTable(ObjRef link, MergeArguments arguments, boolean something)
	{
		Vector vector = new Vector();
		InterfaceEndPoint interfaceEndPoint1 = new InterfaceEndPoint();
		InterfaceEndPoint interfaceEndPoint2 = new InterfaceEndPoint();
		
		ObjRef[] points = xarch.getAll(link, "Point");
		ObjRef anchor1 = (ObjRef)xarch.get(points[0], "AnchorOnInterface");
		String href1 = (String)xarch.get(anchor1, "Href");
		ObjRef interface1 = xarch.resolveHref(arguments.mergeArch, href1);
		ObjRef parent1 = xarch.getParent(interface1);
		ObjRef intface1Desc = (ObjRef)xarch.get(interface1, "Description");
		ObjRef parent1Desc = (ObjRef)xarch.get(parent1, "Description");
		String intface1Description = (String)xarch.get(intface1Desc, "Value");
		String parent1Description = (String)xarch.get(parent1Desc, "Value");
		
		ObjRef anchor2 = (ObjRef)xarch.get(points[1], "AnchorOnInterface");
		String href2 = (String)xarch.get(anchor2, "Href");
		ObjRef interface2 = xarch.resolveHref(arguments.mergeArch, href2);
		ObjRef parent2 = xarch.getParent(interface2);
		ObjRef intface2Desc = (ObjRef)xarch.get(interface2, "Description");
		ObjRef parent2Desc = (ObjRef)xarch.get(parent2, "Description");
		String intface2Description = (String)xarch.get(intface2Desc, "Value");
		String parent2Description = (String)xarch.get(parent2Desc, "Value");
		
		interfaceEndPoint1.elementDescription = parent1Description; 
		interfaceEndPoint1.interfaceDescription = intface1Description;
		interfaceEndPoint2.elementDescription = parent2Description;
		interfaceEndPoint2.interfaceDescription = intface2Description;
		vector.add(interfaceEndPoint1);
		vector.add(interfaceEndPoint2);
		arguments.linksTable.put(xarch.get(link, "Id"), vector);
	}
	
	/*
	 * Adds the signature interface mapping passed in to the simsTable that is
	 * is passed in. The boolean is only there to distinguish from the other Add to sim table
	 * function
	 */
	private void addToSIMSTable(ObjRef sigIntMapping, MergeArguments arguments, boolean something)
	{
		ObjRef outerSigLink = (ObjRef)xarch.get(sigIntMapping, "OuterSignature");
		String outSigHref = (String)xarch.get(outerSigLink, "Href");
		ObjRef signature = xarch.resolveHref(arguments.mergeArch, outSigHref);
		ObjRef sigDesc = (ObjRef)xarch.get(signature, "Description");
		String sigDescription = (String)xarch.get(sigDesc, "Value");
		
		ObjRef innerIntfaceLink = (ObjRef)xarch.get(sigIntMapping, "InnerInterface");
		String innerIntHref = (String)xarch.get(innerIntfaceLink, "Href");
		ObjRef innerInterface = xarch.resolveHref(arguments.mergeArch, innerIntHref);
		
		ObjRef parent = xarch.getParent(innerInterface);
		ObjRef parentDesc = (ObjRef)xarch.get(parent, "Description");
		String parentDescription = (String)xarch.get(parentDesc, "Value");
		ObjRef intfaceDesc = (ObjRef)xarch.get(innerInterface, "Description");
		String intfaceDescription = (String)xarch.get(intfaceDesc, "Value");
		
		SignatureEndPoint signatureEndPoint = new SignatureEndPoint();
		//we actually do not need this element description
		signatureEndPoint.elementDescription = null;
		signatureEndPoint.signatureDescription = sigDescription;
		InterfaceEndPoint interfaceEndPoint = new InterfaceEndPoint();
		interfaceEndPoint.elementDescription = parentDescription;
		interfaceEndPoint.interfaceDescription = intfaceDescription;
		Vector vector = new Vector();
		vector.add(signatureEndPoint);
		vector.add(interfaceEndPoint);
		//inorder to make the sigIntMappings unique we are concatenating the
		//href of outersig and href of innerint
		ObjRef outerSig = (ObjRef)xarch.get(sigIntMapping, "OuterSignature");
		ObjRef innerInt = (ObjRef)xarch.get(sigIntMapping, "InnerInterface");
		arguments.simsTable.put((String)xarch.get(outerSig, "Href") + (String)xarch.get(innerInt, "Href"), vector);
	}
	
	// This method adds a SIM's endpoints to the simsTable
	//
	// addSIMRef	- A reference to a IAddSignatureInterfaceMapping
	// arguments	- The merge arguments
	private void addToSIMSTable(ObjRef addSIMRef, MergeArguments arguments)
	{
		Vector simEndPoints = new Vector();
		ObjRef simRef = (ObjRef)xarch.get(addSIMRef, "SignatureInterfaceMapping");
		
		ObjRef interfaceEndPointRef = (ObjRef)xarch.get(addSIMRef, "InterfaceEndPoint");
		InterfaceEndPoint interfaceEndPoint = new InterfaceEndPoint();
		ObjRef elementDescriptionRef = (ObjRef)xarch.get(interfaceEndPointRef, "ConnectingElementDescription");
		ObjRef interfaceDescriptionRef = (ObjRef)xarch.get(interfaceEndPointRef, "InterfaceDescription");
		interfaceEndPoint.elementDescription = (String)xarch.get(elementDescriptionRef, "Value");
		interfaceEndPoint.interfaceDescription = (String)xarch.get(interfaceDescriptionRef, "Value");
		simEndPoints.add(interfaceEndPoint);
		
		ObjRef signatureEndPointRef = (ObjRef)xarch.get(addSIMRef, "SignatureEndPoint");
		SignatureEndPoint signatureEndPoint = new SignatureEndPoint();
		ObjRef outerElementDescriptionRef = (ObjRef)xarch.get(signatureEndPointRef, "OuterElementDescription");
		ObjRef signatureDescriptionRef = (ObjRef)xarch.get(signatureEndPointRef, "SignatureDescription");
		signatureEndPoint.elementDescription = (String)xarch.get(outerElementDescriptionRef, "Value");
		signatureEndPoint.signatureDescription = (String)xarch.get(signatureDescriptionRef, "Value");
		simEndPoints.add(signatureEndPoint);
		
		//inorder to make the sigIntMappings unique we are concatenating the
		//href of outersig and href of innerint
		ObjRef outerSig = (ObjRef)xarch.get(simRef, "OuterSignature");
		ObjRef innerInt = (ObjRef)xarch.get(simRef, "InnerInterface");
		arguments.simsTable.put((String)xarch.get(outerSig, "Href") + (String)xarch.get(innerInt, "Href"), simEndPoints);
	}

	private void removeElement(boolean isRemoveFromStructure, ObjRef remove, ObjRef[] refs, String typeOfThing, MergeArguments arguments)
	throws MissingElementException
	{
		//remove is the description of the component, connector or link
		if(typeOfThing.equals("Component") ||
			typeOfThing.equals("Connector") ||
			typeOfThing.equals("Link"))
		{
			boolean removeFound = false; //flag to see if thing to remove exists
			String remDescription = (String)xarch.get(remove, "Value");
			for(int i = 0; i < refs.length; i++)
			{
				ObjRef desc = (ObjRef)xarch.get(refs[i], "Description");
				String refDesc = (String)xarch.get(desc, "Value");
				if(refDesc.equals(remDescription))
				{
					//description matches, remove element from parent
					if(typeOfThing.equals("Component") || typeOfThing.equals("Connector"))
					{
						ObjRef xmlLinkRef = (ObjRef)xarch.get(refs[i], "Type");
						ObjRef typeRef = null;
						try
						{
							typeRef = resolveLink(arguments.mergeArch, xmlLinkRef, (String)xarch.get(refs[i], "Id"));
						}
						catch(Exception e)
						{
							System.out.println(e);
							return;
						}
						/**
						 *If we are removing a component or connector we need
						 *to interate through their interfaces, get any links 
						 *or signature interface mappings that are connected 
						 *and add them to the proper table. 
						 */
						 ObjRef[] interfaces = xarch.getAll(refs[i], "Interface");
						 for(int j = 0; j < interfaces.length; j++)
						 {
						 	String interfaceId = (String)xarch.get(interfaces[j], "Id");
							ObjRef[] interfaceLinks = xarch.getReferences(arguments.mergeArch, interfaceId);
							for (int k = 0; k != interfaceLinks.length; ++k)
							{
								if (xarch.isAttached(interfaceLinks[k]))
								{
									ObjRef parent = xarch.getParent(interfaceLinks[k]);
									String className = xarch.getType(parent);
	
									if (xarch.isInstanceOf(parent, "edu.uci.isr.xarch.types.ISignatureInterfaceMapping"))
									{
										//parent is the ObjRef of the signatureInterfaceMapping that we need to add
										addToSIMSTable(parent, arguments, true);
										arguments.typesList.add(typeRef);
									}
									else
									{
										//else is a link and need to put it in the linksTable
										ObjRef link = xarch.getParent(parent);
										addToLinksTable(link, arguments, true);
									}
								}
							}
						 }
					}		
					ObjRef structureRef = xarch.getParent(refs[i]);
					removeFound = true;		
					xarch.remove(structureRef, typeOfThing, refs[i]);
					break;
				}
			}
			if(!removeFound)
			{
				//get the sructure
				ObjRef structureRef = xarch.getParent(refs[0]);
				ObjRef structureDesc = (ObjRef)xarch.get(structureRef, "Description");
				String structureDescValue = (String)xarch.get(structureDesc, "Value");
				issueRemoveNonexistingElementWarning(typeOfThing, remDescription, structureDescValue);
			}
		}
		//type of thing is interface. Remove in this case is the RemoveInterface
		if(typeOfThing.equals("Interface"))
		{
			ObjRef elementDesc = (ObjRef)xarch.get(remove, "ElementDescription");
			String elementDescription = (String)xarch.get(elementDesc, "Value");
			ObjRef interfaceDesc = (ObjRef)xarch.get(remove, "InterfaceDescription");
			String interfaceDescription = (String)xarch.get(interfaceDesc, "Value");
			boolean found = false;
	
			for(int i = 0; i < refs.length && found==false; i++)
			{
				//find the element with the given description
				ObjRef refDesc = (ObjRef)xarch.get(refs[i], "Description");
				String refDescription = (String)xarch.get(refDesc, "Value");
				if(elementDescription.equals(refDescription))
				{
					ObjRef[] interfaces = xarch.getAll(refs[i], "Interface");
					for(int j = 0; j < interfaces.length && found==false; j++)
					{
						ObjRef refIntfaceDesc = (ObjRef)xarch.get(interfaces[j], "Description");
						String refIntfaceDescription = (String)xarch.get(refIntfaceDesc, "Value");
						if(interfaceDescription.equals(refIntfaceDescription))
						{
							//found it! now remove it
							//but before that get all links that are connected to this
							//interface
							String interfaceId = (String)xarch.get(interfaces[j], "Id");
							ObjRef[] interfaceLinks = xarch.getReferences(arguments.mergeArch, interfaceId);
							for (int k = 0; k != interfaceLinks.length; ++k)
							{
								if (xarch.isAttached(interfaceLinks[k]))
								{
									ObjRef parent = xarch.getParent(interfaceLinks[k]);
									String className = xarch.getType(parent);
	
									if (xarch.isInstanceOf(parent, "edu.uci.isr.xarch.types.ISignatureInterfaceMapping"))
									{
										//parent is the ObjRef of the signatureInterfaceMapping that we need to add
										addToSIMSTable(parent, arguments, true);
										ObjRef xmlLinkRef = (ObjRef)xarch.get(refs[i], "Type");
										ObjRef typeRef = null;
										try
										{
											typeRef = resolveLink(arguments.mergeArch, xmlLinkRef, (String)xarch.get(refs[i], "Id"));
										}
										catch(Exception e)
										{
											System.out.println(e);
										}
										arguments.typesList.add(typeRef);
									}
									else
									{
										//else is a link and need to put it in the linksTable
										ObjRef link = xarch.getParent(parent);
										addToLinksTable(link, arguments, true);
									}
								}
							}
							found = true;
							xarch.remove(refs[i], typeOfThing, interfaces[j]);
						}
					}
				}
			}
			if(!found)
			{
				issueRemoveNonexistingElementWarning(typeOfThing, interfaceDescription, elementDescription);
			}
		}
		//type of thing is Optional, so remove is the RemoveOptional
		if(typeOfThing.equals("Optional"))
		{
			ObjRef elemDesc = (ObjRef)xarch.get(remove, "ElementDescription");
			String remDescription = (String)xarch.get(elemDesc, "Value");
			for(int i = 0; i < refs.length; i++)
			{
				ObjRef desc = (ObjRef)xarch.get(refs[i], "Description");
				String refDesc = (String)xarch.get(desc, "Value");
				if(refDesc.equals(remDescription))
				{
					//description matches, get the optional and remove it
					//I am not sure why we have the description of the
					//Optional in the RemoveOptional type in the schema
					//See if Optional already exists, if so, issue warning
					if(xarch.get(refs[i], "Optional") == null)
					{
							issueRemoveNonexistingElementWarning(typeOfThing, "", remDescription);
						return;
					}
					xarch.clear(refs[i], typeOfThing);
					break;
				}
			}
		}

		//if signature, SIM, variant, or sub architecture then the remove is a
		//RemoveTypeEntity
		if(typeOfThing.equals("Signature") ||
			typeOfThing.equals("SubArchitecture") ||
			typeOfThing.equals("Variant") ||
			typeOfThing.equals("SignatureInterfaceMapping"))
		{
			//get the diffLocation
			ObjRef diffLoc = (ObjRef)xarch.get(remove, "DiffLocation");
			ObjRef location = (ObjRef)xarch.get(diffLoc, "Location");
			String diffLocation = (String)xarch.get(location, "Value");
			for(int i = 0; i < refs.length; i++)
			{
				if(isRemoveFromStructure)
				{
					//get description of the refs
					ObjRef refDesc = (ObjRef)xarch.get(refs[i], "Description");
					String refDescription = (String)xarch.get(refDesc, "Value");
					if(refDescription.equals(diffLocation))
					{
						ObjRef type = (ObjRef)xarch.get(refs[i], "Type");
						String typeHref = (String)xarch.get(type, "Href");
						ObjRef refType = xarch.resolveHref(arguments.mergeArch, typeHref);
						ObjRef typeOfThingDesc;
						if(typeOfThing.equals("SignatureInterfaceMapping"))
							typeOfThingDesc = (ObjRef)xarch.get(remove, "RemoveSignatureInterfaceMapping");
						else
							typeOfThingDesc = (ObjRef)xarch.get(remove, typeOfThing);
						removeTypeElement(refType, typeOfThingDesc, typeOfThing, arguments);
					}
				}
				else
				{
					//we are dealing with a variant

					//first find the variant to go into
					// The description for variants is the guards
					ObjRef guard = (ObjRef)xarch.get(refs[i], "Guard");
					String variantDescription = guardUtil.guardToString(guard);
					if(variantDescription.equals(diffLocation))
					{
						ObjRef typeLink = (ObjRef)xarch.get(refs[i], "VariantType");
						String typeHref = (String)xarch.get(typeLink, "Href");
						ObjRef variantType = xarch.resolveHref(arguments.mergeArch, typeHref);
						ObjRef typeOfThingDesc;
						if(typeOfThing.equals("SignatureInterfaceMapping"))
							typeOfThingDesc = (ObjRef)xarch.get(remove, "RemoveSignatureInterfaceMapping");
						else
							typeOfThingDesc = (ObjRef)xarch.get(remove, typeOfThing);
						removeTypeElement(variantType, typeOfThingDesc, typeOfThing, arguments);
					}
				}
			}
		}
	}

	private void removeTypeElement( ObjRef type, ObjRef descriptionToRemove, String typeOfThing, MergeArguments arguments)
	{
		if(typeOfThing.equals("Signature"))
		{
			ObjRef[] refs = xarch.getAll( type, typeOfThing );
			String description = (String)xarch.get(descriptionToRemove, "Value");
			boolean found = false;
			for(int i = 0; i < refs.length; i++)
			{
				ObjRef refDesc = (ObjRef)xarch.get(refs[i], "Description");
				String refDescription = (String)xarch.get(refDesc, "Value");
				if(refDescription.equals(description))
				{
					String signatureId = (String)xarch.get(refs[i], "Id");
					ObjRef[] sigLinks = xarch.getReferences(arguments.mergeArch, signatureId);
					for (int k = 0; k != sigLinks.length; ++k)
					{
						if (xarch.isAttached(sigLinks[k]))
						{
							ObjRef parent = xarch.getParent(sigLinks[k]);
							String className = xarch.getType(parent);

							if (xarch.isInstanceOf(parent, "edu.uci.isr.xarch.types.ISignatureInterfaceMapping"))
							{
								//parent is the ObjRef of the signatureInterfaceMapping that we need to add
								addToSIMSTable(parent, arguments, true);
								arguments.typesList.add(type);
							}
						}
					}
					found = true;
					xarch.remove(type, typeOfThing, refs[i]);
					break;
				}
			}
			if(!found)
			{
				//get type description
				ObjRef typeDesc = (ObjRef)xarch.get(type, "Description");
				String typeDescValue = (String)xarch.get(typeDesc, "Value");
				issueRemoveNonexistingElementWarning(typeOfThing, description, typeDescValue);
			}
		}
		if(typeOfThing.equals("SubArchitecture"))
		{
			//descriptionToRemove is irrelavant
			//we will need to remove the archStructure as well
			ObjRef subArch = (ObjRef)xarch.get(type, typeOfThing);
			if(subArch == null)
			{
				//get type description
				ObjRef typeDesc = (ObjRef)xarch.get(type, "Description");
				String typeDescValue = (String)xarch.get(typeDesc, "Value");	
				issueRemoveNonexistingElementWarning(typeOfThing, "", typeDescValue);
				return;
			}
			ObjRef archStrucLink = (ObjRef)xarch.get(subArch, "ArchStructure");
			String archStrucHref = (String)xarch.get(archStrucLink, "Href");
			ObjRef archStruc = xarch.resolveHref(arguments.mergeArch, archStrucHref);
			xarch.remove(arguments.mergeArch, "Object", archStruc);
			//now clear the subarch
			xarch.clear(subArch, "ArchStructure");
			xarch.clear(type, typeOfThing);
		}
		if(typeOfThing.equals("Variant"))
		{
			ObjRef[] refs = xarch.getAll( type, typeOfThing );
			String description = (String)xarch.get(descriptionToRemove, "Value");
			boolean found = false;;
			for(int i = 0 ; i < refs.length; i++)
			{
				ObjRef guard = (ObjRef)xarch.get(refs[i], "Guard");
				String refDescription = guardUtil.guardToString(guard);
				if(refDescription.equals(description))
				{
					found = true;
					xarch.remove(type, typeOfThing, refs[i]);
				} 
			}
			if(!found)
			{
				//get type description
				ObjRef typeDesc = (ObjRef)xarch.get(type, "Description");
				String typeDescValue = (String)xarch.get(typeDesc, "Value");
				issueRemoveNonexistingElementWarning(typeOfThing, description, typeDescValue);
				return;
			}
		}
		if(typeOfThing.equals("SignatureInterfaceMapping"))
		{
			//in this case the descriptionToRemove is a RemoveSignatureInterfaceMapping
			ObjRef sigDesc = (ObjRef)xarch.get(descriptionToRemove, "OuterSignatureDescription");
			String sigDescription = (String)xarch.get(sigDesc, "Value");
			ObjRef intDesc = (ObjRef)xarch.get(descriptionToRemove, "InnerInterfaceDescription");
			String intDescription = (String)xarch.get(intDesc, "Value");
			//System.out.println( "Sig: " + sigDescription + "Intf: " + intDescription );
			boolean found = false;
			ObjRef subArch = (ObjRef)xarch.get(type, "SubArchitecture");
			ObjRef[] refs = xarch.getAll(subArch, typeOfThing);
			for(int i = 0; i < refs.length && found==false; i++)
			{
				ObjRef outerSigLink = (ObjRef)xarch.get(refs[i], "OuterSignature");
				String outerSigHref = (String)xarch.get(outerSigLink, "Href");
				ObjRef outerSig = xarch.resolveHref(arguments.mergeArch, outerSigHref);
				ObjRef outerSigDesc = (ObjRef)xarch.get(outerSig, "Description");
				String outerSigDescription = (String)xarch.get(outerSigDesc, "Value");

				ObjRef innerIntfaceLink = (ObjRef)xarch.get(refs[i], "InnerInterface");
				String innerIntfaceHref = (String)xarch.get(innerIntfaceLink, "Href");
				ObjRef innerIntface = xarch.resolveHref(arguments.mergeArch, innerIntfaceHref);
				ObjRef innerIntfaceDesc = (ObjRef)xarch.get(innerIntface, "Description");
				String innerIntfaceDescription = (String)xarch.get(innerIntfaceDesc, "Value");

				if(sigDescription.equals(outerSigDescription) &&
					intDescription.equals(innerIntfaceDescription))
				{
					found = true;
					//remove the signature interface mapping
					xarch.remove(subArch, typeOfThing, refs[i]);
				}
			}
			if(!found)
			{
				//get type description
				ObjRef typeDesc = (ObjRef)xarch.get(type, "Description");
				String typeDescValue = (String)xarch.get(typeDesc, "Value");
				issueRemoveNonexistingElementWarning(typeOfThing, sigDesc+"**TO**"+intDesc, typeDescValue);
			}
		}
	}

	private void mergeVariantType(ObjRef type, ObjRef diffPart, MergeArguments arguments)
		throws MissingElementException, MissingAttributeException, BrokenLinkException
	{
		// Working inside a VariantType
		// Only worry about {Add, Remove}TypeEntity.
		// The DiffLocation on these elements will refer to the Guard of the Variant
		// which needs to be updated.
		ObjRef[] variantRefs = xarch.getAll(type, "Variant");
		ObjRef[] diffParts= xarch.getAll(diffPart, "DiffPart");
		ObjRef[] removes = xarch.getAll(diffPart, "Remove");
		ObjRef[] adds = xarch.getAll(diffPart, "Add");

		Vector simsToAdd = new Vector();
		Vector sigsToRemove = new Vector();

		for(int i = 0; i < removes.length; i++)
		{
			//the following will call removeElement with false parameter for isStructure.
			//This will insure that they get removed from the coresponding variant.
			ObjRef removeTypeEntity = (ObjRef)xarch.get(removes[i], "RemoveTypeEntity");

			//if removes[i] is a signature
			if(xarch.get(removeTypeEntity, "Signature") != null)
			{
			    sigsToRemove.add(removeTypeEntity);
				//removeElement(false, removeTypeEntity, variantRefs, "Signature", arguments);
			}

			if(xarch.get(removeTypeEntity, "SubArchitecture") != null)
				removeElement(false , removeTypeEntity, variantRefs, "SubArchitecture", arguments );

			if(xarch.get(removeTypeEntity, "Variant") != null)
				removeElement(false, removeTypeEntity, variantRefs, "Variant", arguments);

			if(xarch.get(removeTypeEntity, "RemoveSignatureInterfaceMapping") != null)
				removeElement(false, removeTypeEntity, variantRefs, "SignatureInterfaceMapping", arguments);
		}

		//delay removing of sigs to avoid case where sigs removed before SIMs
		for(int i = 0; i < sigsToRemove.size(); i++)
		{
		    removeElement(false, (ObjRef)sigsToRemove.get(i), variantRefs, "Signature", arguments);
		}

		for(int i = 0; i < adds.length; i++)
		{
			ObjRef addTypeEntity = (ObjRef)xarch.get(adds[i], "AddTypeEntity");

			if(xarch.get(addTypeEntity, "Signature") != null)
				addElement(false, type, addTypeEntity, variantRefs, "Signature", arguments);

			if(xarch.get(addTypeEntity, "SubArchitecture") != null)
				addElement(false, type, addTypeEntity, variantRefs, "SubArchitecture", arguments);

			if(xarch.get(addTypeEntity, "Variant") != null)
				addElement(false, type, addTypeEntity, variantRefs, "Variant", arguments);

			if(xarch.get(addTypeEntity, "AddSignatureInterfaceMapping") != null)
				simsToAdd.add(addTypeEntity);
		}
		
		for(int i = 0; i < simsToAdd.size(); i++)
		{
			addElement(false, type, (ObjRef)simsToAdd.get(i), variantRefs, "SignatureInterfaceMapping", arguments);
		}

		for(int i = 0; i < diffParts.length; i++)
		{
			ObjRef diffLocation = (ObjRef)xarch.get(diffParts[i], "DiffLocation");
			ObjRef locationDesc = (ObjRef)xarch.get(diffLocation, "Location");
			String location = (String)xarch.get(locationDesc, "Value");


			ObjRef variantRef = null;

			for(int j = 0; j < variantRefs.length; j++)
			{
				ObjRef guard = (ObjRef)xarch.get(variantRefs[j], "Guard");
				String variantGuard = guardUtil.guardToString(guard);
				if(variantGuard.equals(location))
					variantRef = variantRefs[j];
			}

			ObjRef variantTypeLink = (ObjRef)xarch.get(variantRef, "VariantType");
			String variantTypeHref = (String)xarch.get(variantTypeLink, "Href");
			ObjRef typeRef = xarch.resolveHref(arguments.mergeArch, variantTypeHref);

			//check if the type has a substructure
			ObjRef subArch = (ObjRef)xarch.get(typeRef, "SubArchitecture");
			if(subArch != null)
			{
				//get the archstructure
				ObjRef archStrucLink = (ObjRef)xarch.get(subArch, "ArchStructure");
				String archStrucHref = (String)xarch.get(archStrucLink, "Href");
				ObjRef archStructure = xarch.resolveHref(arguments.mergeArch, archStrucHref);
				mergeStructure(archStructure, diffParts[i], arguments);
			}
			else
				mergeVariantType(typeRef, diffParts[i], arguments);
		}
	}

	void mergeType(ObjRef type, ObjRef diffPart, MergeArguments arguments)
		throws MissingElementException, MissingAttributeException, BrokenLinkException
	{
		ObjRef[] diffParts = xarch.getAll(diffPart, "DiffPart");
		ObjRef[] removes = xarch.getAll(diffPart, "Remove");
		ObjRef[] adds = xarch.getAll(diffPart, "Add");
		// types containas the vector of types that need fixing. For this
		// function this will only be current type.
		Vector types = new Vector();
		Vector simsToAdd = new Vector(); //the list of sims (addtypeentity) that need to be added later
		Vector sigsToRemove = new Vector(); //the list of sigs (Description)

		ObjRef[] variantRefs = xarch.getAll(type, "Variant");


		for(int i = 0; i < removes.length; i++)
		{
			//the following will call removeElement with false parameter for isStructure.
			//This will insure that they get removed from the coresponding variant.
			ObjRef removeTypeEntity = (ObjRef)xarch.get(removes[i], "RemoveTypeEntity");

			//if removes[i] is a signature
			if(xarch.get(removeTypeEntity, "Signature") != null)
			{
				ObjRef signatureDesc = (ObjRef)xarch.get(removeTypeEntity, "Signature");
				sigsToRemove.add(signatureDesc);
				//removeTypeElement(type, signatureDesc, "Signature", arguments);
			}

			if(xarch.get(removeTypeEntity, "SubArchitecture") != null)
			{
				ObjRef subArch = (ObjRef)xarch.get(removeTypeEntity, "SubArchitecture");
				removeTypeElement(type, subArch, "SubArchitecture", arguments);
			}

			if(xarch.get(removeTypeEntity, "Variant") != null)
			{
				ObjRef variantDesc = (ObjRef)xarch.get(removeTypeEntity, "Variant");
				removeTypeElement(type, variantDesc, "Variant", arguments);
			}

			if(xarch.get(removeTypeEntity, "RemoveSignatureInterfaceMapping") != null)
			{
				ObjRef removeSIM = (ObjRef)xarch.get(removeTypeEntity, "RemoveSignatureInterfaceMapping");
				removeTypeElement(type, removeSIM, "SignatureInterfaceMapping", arguments);
			}
		}

		//delay the removal of sigs to avoid case where signatures are removed before SIMs
		for(int i = 0; i < sigsToRemove.size(); i++)
		{
		    removeTypeElement(type, (ObjRef)sigsToRemove.get(i), "Signature", arguments);
		}

		for(int i = 0; i < adds.length; i++)
		{
			ObjRef addTypeEntity = (ObjRef)xarch.get(adds[i], "AddTypeEntity");

			if(xarch.get(addTypeEntity, "Signature") != null)
				addTypeElement(type, addTypeEntity, "Signature", arguments);

			if(xarch.get(addTypeEntity, "SubArchitecture") != null)
				addTypeElement(type, addTypeEntity, "SubArchitecture", arguments);

			if(xarch.get(addTypeEntity, "Variant") != null)
				addTypeElement(type, addTypeEntity, "Variant", arguments);

			if(xarch.get(addTypeEntity, "AddSignatureInterfaceMapping") != null)
				//delay the adding of sims till after we have added everything else
				simsToAdd.add(addTypeEntity);
		}
		
		//add all the sims
		for(int i = 0; i < simsToAdd.size(); i++)
		{
			addTypeElement(type, (ObjRef)simsToAdd.get(i), "SignatureInterfaceMapping", arguments);
		}

		//if there are no variants then we can just merge the substructure for its
		//diff part. There should only be one diffPart in this case.
		ObjRef typeSubArch = (ObjRef)xarch.get(type, "SubArchitecture");
		if(variantRefs.length == 0)
		{
			//if there is no subStructure then we are done
			if(typeSubArch == null)
				;
			else
			{
				//get the archStructure
				ObjRef typeArchStrucLink = (ObjRef)xarch.get(typeSubArch, "ArchStructure");
				String typeArchStrucHref = (String)xarch.get(typeArchStrucLink, "Href");
				ObjRef typeArchStruc = xarch.resolveHref(arguments.mergeArch, typeArchStrucHref);
				for(int i = 0; i < diffParts.length; i++)
				{
					mergeStructure(typeArchStruc, diffParts[i], arguments);
				}
			}
		}
		else
		{
			for(int i = 0; i < diffParts.length; i++)
			{
				ObjRef diffLocation = (ObjRef)xarch.get(diffParts[i], "DiffLocation");
				ObjRef locationDesc = (ObjRef)xarch.get(diffLocation, "Location");
				String location = (String)xarch.get(locationDesc, "Value");
				
				/* This is the special case we have to handle in case the diffpart is 
					corresponding to the root type that we are starting from.
				*/
				if(location.equals(archstudio.comp.pladiff.IPLADiff.TYPE_STARTING_POINT))
				{
					mergeVariantType(type, diffParts[i], arguments);
					continue;
				}

				ObjRef variantRef = null;

				for(int j = 0; j < variantRefs.length; j++)
				{
					ObjRef guard = (ObjRef)xarch.get(variantRefs[j], "Guard");
					String variantGuard = guardUtil.guardToString(guard);
					if(variantGuard.equals(location))
						variantRef = variantRefs[j];
				}

				ObjRef variantTypeLink = (ObjRef)xarch.get(variantRef, "VariantType");
				String variantTypeHref = (String)xarch.get(variantTypeLink, "Href");
				ObjRef typeRef = xarch.resolveHref(arguments.mergeArch, variantTypeHref);

				//check if the type has a substructure
				ObjRef subArch = (ObjRef)xarch.get(typeRef, "SubArchitecture");
				if(subArch != null)
				{
					//get the archstructure
					ObjRef archStrucLink = (ObjRef)xarch.get(subArch, "ArchStructure");
					String archStrucHref = (String)xarch.get(archStrucLink, "Href");
					ObjRef archStructure = xarch.resolveHref(arguments.mergeArch, archStrucHref);
					mergeStructure(archStructure, diffParts[i], arguments);
				}
				else
					mergeVariantType(typeRef, diffParts[i], arguments);
			}
		}
		fixTypes(arguments.typesList, arguments);
	}

	private void fixStructure(ObjRef structure, Hashtable linksTable)
		throws MissingElementException
	{
		ObjRef[] linkRefs = xarch.getAll(structure, "Link");
		ObjRef[] compconnRefs = getCompConnRefs(structure);
		
		ObjRef linkRef;

		//System.out.println("Number of link refs: " + linkRefs.length);

		for(int i = 0; i < linkRefs.length; ++i)
		{
			linkRef = linkRefs[i];
			
			//System.out.println("In fixStructure, linkRef is = " + linkRef);		

			// Get the vector of endpoints associated with the link.  If none, then that means
			// this link was never recorded as potentially needing to be relinked
			Vector linkEndPoints = (Vector)linksTable.get(xarch.get(linkRef, "Id"));
						
			if(linkEndPoints == null)
			{
				//System.out.println("linkEndPoint [" + i + "] is null");
				continue;
			}
			
			// Make sure that both descriptions in the InterfaceEndPoints of the link are found within the
			// current structure.
			ObjRef[] pointRefs = xarch.getAll(linkRef, "Point");
			int size = linkEndPoints.size();
			
			ObjRef desc = (ObjRef)xarch.get(linkRef, "Description");
			//System.out.println("Link description: " + (String)xarch.get(desc, "Value"));
			//System.out.println("Number of linkEndPoints: " + size);
			
			for(int j = 0; j < size; ++j)
			{
				// Get the current InterfaceEndPoint and find the corresponding interface within the current structure
				// to match the link up to.  Then add a reference to that interface to the point.
				InterfaceEndPoint interfaceEndPoint = (InterfaceEndPoint)linkEndPoints.get(j);
				ObjRef interfaceXmlLinkRef = findInterfaceEndPointForLink(compconnRefs, interfaceEndPoint);
				
				// If the required interface does not exist in the current archStructure
				if(interfaceXmlLinkRef == null)
				{
					throw new MissingElementException("The structure with id \"" + (String)xarch.get(structure, "Id") + "\" does not have an element with the description \"" + interfaceEndPoint.elementDescription + "\" that also has an interface with description \"" + interfaceEndPoint.interfaceDescription + "\"");
				}
				
				ObjRef xmlLinkRef = (ObjRef)xarch.get(pointRefs[j], "AnchorOnInterface");
				String interfaceID = (String)xarch.get(interfaceXmlLinkRef, "Id");
				xarch.set(xmlLinkRef, "Href", "#"+interfaceID);
			}
		}
	}
	
	// This method finds a interface that would serve as an end point for a link.  The method searches a list of 
	// provided element references for an interface that would match the description specified in the interface end point
	//
	// compconnRefs		- A list of component/connector references that contain interfaces to inspect.
	// interfaceEndPoint- A description of the interface to search for.
	private ObjRef findInterfaceEndPointForLink(ObjRef[] compconnRefs, InterfaceEndPoint interfaceEndPoint)
	{
		ObjRef elementRef;
		ObjRef interfaceRef;
		for(int i = 0; i < compconnRefs.length; ++i)
		{
			elementRef = compconnRefs[i];
			ObjRef descriptionRef = (ObjRef)xarch.get(elementRef, "Description");
			String descValue = (String)xarch.get(descriptionRef, "Value");
			if(descValue.equals(interfaceEndPoint.elementDescription))
			{
				ObjRef[] interfaceRefs = xarch.getAll(elementRef, "Interface");				
				for(int j = 0; j < interfaceRefs.length; ++j)
				{
					interfaceRef = interfaceRefs[j];
					descriptionRef = (ObjRef)xarch.get(interfaceRef, "Description");
					descValue = (String)xarch.get(descriptionRef, "Value");
					if(descValue.equals(interfaceEndPoint.interfaceDescription))
					{
						return interfaceRef;
					}
				}
			}
		}

		return null;
	}
	
	/**
	 *Finds the signature in the given signatures that matches the description given in 
	 *endPoint
	 */
	private ObjRef findSignatureEndPoint(ObjRef[] signatures, SignatureEndPoint endPoint)
	{
		for(int i = 0 ; i < signatures.length; i++)
		{
			ObjRef sigDesc = (ObjRef)xarch.get(signatures[i], "Description");
			String sigDescription = (String)xarch.get(sigDesc, "Value");
			if(sigDescription.equals(endPoint.signatureDescription))
				return signatures[i];
		}
		return null;
	}

	/**
	 *This method will fix the signatureInterfaceMapping links for every given 
	 *type in the Vector types.
	 *@param types list of types that need their interfaceMappings fixed. This list
	 *is determined from mergeStructure.
	 *@param arguments MergeArguments.
	 */
	private void fixTypes(Vector types, MergeArguments arguments)
		throws MissingElementException
	{
		/**
		 *this is actually fix types from the list of types that are pased in.
		 *The simsTable is the entire table from all the merge calls. We need all
		 *the simsTable because needs to be persistent for back tracking. This funcation
		 *only fixes the sims within the list of types.
		 */
		 for(int i = 0 ; i < types.size(); i++)
		 {
		 	ObjRef type = (ObjRef)types.elementAt(i);
		 	ObjRef[] signatures = xarch.getAll(type, "Signature");
		 	ObjRef subArchitecture = (ObjRef)xarch.get(type, "SubArchitecture");
		 	ObjRef[] sigIntMappings = xarch.getAll(subArchitecture, "SignatureInterfaceMapping");
		 	ObjRef archStrucLink = (ObjRef)xarch.get(subArchitecture, "ArchStructure");
		 	String archStrucHref = (String)xarch.get(archStrucLink, "Href");
		 	ObjRef archStructure = xarch.resolveHref(arguments.mergeArch, archStrucHref);
		 	ObjRef[] compConnRefs = getCompConnRefs(archStructure);
		 	for(int j = 0; j < sigIntMappings.length; j++)
		 	{
				ObjRef outerSig = (ObjRef)xarch.get(sigIntMappings[j], "OuterSignature");
				ObjRef innerInt = (ObjRef)xarch.get(sigIntMappings[j], "InnerInterface");
		 		Vector mappingsEndPoints = 	(Vector)arguments.simsTable.get((String)xarch.get(outerSig, "Href") + (String)xarch.get(innerInt, "Href"));
		 		//if end points is null then this mapping is not considered for fixing
		 		if(mappingsEndPoints == null)
		 			continue;
		 		
		 		String outerSigHref = "";
		 		String innerIntfaceHref = "";
		 		//find the signature that matches the description from the signatureEndPoint
		 		for(int k = 0; k < mappingsEndPoints.size(); k++)
		 		{
		 			if(mappingsEndPoints.elementAt(k) instanceof SignatureEndPoint)
		 			{
		 				SignatureEndPoint sigEndPoint = (SignatureEndPoint)mappingsEndPoints.elementAt(k);
		 				ObjRef signature = findSignatureEndPoint(signatures, sigEndPoint);
						if(signature == null)
						{
							throw new MissingElementException("Missing signature that needs to hook with signature interface mapping");
						}
		 				String sigId = (String)xarch.get(signature, "Id");
		 				outerSigHref = "#"+sigId;
		 			}
		 			else //the endpoint is an interface end point
		 			{
		 				InterfaceEndPoint interfaceEndPoint = (InterfaceEndPoint)mappingsEndPoints.elementAt(k);
		 				ObjRef intface = findInterfaceEndPointForLink(compConnRefs, interfaceEndPoint);
						if(intface == null)
						{
							throw new MissingElementException("Missing interface that needs to hook with signature interface mapping");
						}
		 				String interfaceId = (String)xarch.get(intface, "Id");
		 				innerIntfaceHref = "#"+interfaceId;
		 			}
		 		}
		 		//outerSigHref and innerIntfaceHref should now have the updated Hrefs for the 
		 		//mapping. So we now just set the new Hrefs on teh mappings.
		 		ObjRef outerSigLink = (ObjRef)xarch.get(sigIntMappings[j], "OuterSignature");
		 		ObjRef innerInterfaceLink = (ObjRef)xarch.get(sigIntMappings[j], "InnerInterface");
		 		xarch.set(outerSigLink, "Href", outerSigHref);
		 		xarch.set(innerInterfaceLink, "Href", innerIntfaceHref);
		 	}
		 }
	}
	
	// This method is used to generate a description for a signature interface mapping
	// Returns: a string representation of the signature interface mapping
	private String generateSIMDescription(ObjRef element, MergeArguments arguments)
		throws MissingElementException, BrokenLinkException, MissingAttributeException
	{
		String desc = null;
		ObjRef descRef = null;
		
		// The description is a concat of the
		// descriptions of the outer sig and inner intf.
		
		// gets the links
		ObjRef outerSigLink = ( ObjRef ) xarch.get( element, "OuterSignature" );
		
		if( outerSigLink != null )
		{
			ObjRef innerIntfLink = ( ObjRef )xarch.get( element, "InnerInterface" );
			
			if( innerIntfLink != null )
			{
				ObjRef outerSig = resolveLink(arguments.mergeArch, outerSigLink, "");
				ObjRef innerIntf = resolveLink(arguments.mergeArch, innerIntfLink, "");
				
				descRef = ( ObjRef ) xarch.get( outerSig, "Description" );
				
				if( descRef != null )
				{
					desc = ( String ) xarch.get( descRef, "Value" );
					
					descRef = ( ObjRef ) xarch.get( innerIntf, "Description" );
					if( descRef != null )
					{
						desc += " " + ( String ) xarch.get( descRef, "Value" );
					}
				}
				
				if( descRef == null )
				{
					// sub struct
					ObjRef parent = xarch.getParent( element );
					// type
					ObjRef grandParent = xarch.getParent( parent );
					throw new MissingElementException( "Error: SignatureInterfaceMapping inside "
						+ ( String )xarch.get( grandParent, "Id" ) +
						" is missing descrption(s) on its endpoint(s)." );
				}
			}
			else
			{
				// sub struct
				ObjRef parent = xarch.getParent( element );
				// type
				ObjRef grandParent = xarch.getParent( parent );
				throw new MissingElementException( "Error: SignatureInterfaceMapping inside "
					+ ( String )xarch.get( grandParent, "Id" ) +
					" is missing its inner interface." );
			}
		}
		else
		{
			// sub struct
			ObjRef parent = xarch.getParent( element );
			// type
			ObjRef grandParent = xarch.getParent( parent );
			throw new MissingElementException( "Error: SignatureInterfaceMapping inside "
				+ ( String )xarch.get( grandParent, "Id" ) +
				" is missing its outer signature." );
		}
		
		return desc;
	}
	
	// This method issues a warning to the user that an element being added already exists
	//
	// elementType		- The type of element being added
	// elementDesc		- The description of the element being added
	// containerDesc	- The description of the container the element is being added into
	private void issueAddExistingElementWarning(String elementType, String elementDesc, String containerDesc)
	{
		System.out.println("***** Warning - Unable to add: " + elementType + " \"" + elementDesc + "\" already exists in \"" + containerDesc + "\" *****");
	}
	
	// This method issues a warning to the user that an element being removed does not exist
	//
	// elementType		- The type of element being removed
	// elementDesc		- The description of the element being removed
	// containerDesc	- The description of the container the element is being removed from
	private void issueRemoveNonexistingElementWarning(String elementType, String elementDesc, String containerDesc)
	{
		System.out.println("***** Warning - Unable to remove: " + elementType + " \"" + elementDesc + "\" does not exist in \"" + containerDesc + "\" *****");
	}
	
	// This method issues a warning to the user that an element in the diff instruction was not found
	//
	// elementType		- The type of element
	// elementDesc		- The description of the element 
	// containerDesc	- The description of the container the element
	private void issueElementNotFoundWarning(String elementType, String elementDesc, String containerDesc)
	{
		System.out.println("***** Warning - Unable to find: " + elementType + " \"" + elementDesc + "\" in \"" + containerDesc + "\" *****");
	}
	
	// This method generates the proper description for an arch structure with the type and version information
	//
	// archStruc		- The arch structure whose description is being generated for
	// document			- The IXArch of the document this archstructure belongs to.
	public void fixArchStructureDescription(ObjRef archStruc, ObjRef document)
	{
		String id = (String)xarch.get(archStruc, "Id");
		ObjRef[]  references = xarch.getReferences(document, id);
		for(int i = 0; i < references.length; i++)
		{
			if(xarch.isAttached(references[i]))
			{
				ObjRef subarch = xarch.getParent(references[i]);
				ObjRef parentType = xarch.getParent(subarch);
				ObjRef versionGraphLink = (ObjRef)xarch.get(parentType, "VersionGraphNode");
				String href = (String)xarch.get(versionGraphLink, "Href");
				ObjRef node = xarch.resolveHref(document, href);
				ObjRef versionID = (ObjRef)xarch.get(node, "VersionID");
				String version = (String)xarch.get(versionID, "Value");
				ObjRef desc = (ObjRef)xarch.get(parentType, "Description");
				String description = (String)xarch.get(desc, "Value");
				String newValue = "ArchStrcuture for Type " + description + " Version " + version;
				ObjRef archDesc = (ObjRef)xarch.get(archStruc, "Description");
				xarch.set(archDesc, "Value", newValue);
			}
		}
	}
}








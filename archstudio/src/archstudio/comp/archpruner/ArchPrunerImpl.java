
package archstudio.comp.archpruner;

// Add support for xArchADT
import edu.uci.ics.xarchutils.*;

import c2.fw.*;

// Standard imports
import java.util.*;
import java.io.*;


/**
 * This is the class that provides the implementation of the Pruner. The Pruner is responsible 
 * for taking an architecture and removing any unneeded archStructures and archTypes.
 * The end result is an architecture that consists only of archStructures, found in types
 * as substructure (where needed), and archTypes that are types of elements within archStructures.
 * Any disjoint archTypes and archStructures will then not be included.  This could be used, for
 * example, after an architecture has been run through a selector.  The larger architecture will
 * only have a subset selected from it and the rest will then be removed after being run through
 * the pruner.
 *
 * @author Christopher Van der Westhuizen
 */

public class ArchPrunerImpl implements IArchPruner, MessageProvider
{
	// This inner helper class acts as a means to collect together all the arguments
	// that would be needed by most of the methods in ArchPrunerImpl.
	// The advantage to using this class is to group together variables that will
	// be used by a particular instance of use of the ArchPruner interface and allowing
	// for reentrant code.
	public class PrunerArguments
	{
		public ObjRef arch;				// reference to the xArch element of the architecture being examined
		public Hashtable visitedTypes;	// hashtable that keeps track of all the types that have 
										// been visited.  Hashtable stores IDs of those types
		public Hashtable visitedStructures;
		public Hashtable visitedVersionNodes;	// holds all version nodes of visited types
		
		public String targetURL;
		public String msgID;
		public double lowerBound;
		public double upperBound;
	}
	
	// Interface through which DOM-based xArch libraries can be accessed
	protected XArchFlatInterface xarch; 
	
	// Size for the hashtable
	private static final int SIZE = 100;
	
	// Size for upper bound of progress bar
	private static final double UPPER_BOUND = 100;
	
	protected Vector messageListeners = new Vector();
	
	/**
	 * This is the constructor for the Pruner
	 *
	 * @param xArch	A reference to the XArchFlatInterface that allows for the accessing of DOM-based xArch libraries
	 */
	public ArchPrunerImpl(XArchFlatInterface xArch)
	{
		xarch = xArch;
	}
	
	/**
	 * This is the function that is responsible for calling the pruning algorithm.  The algorithm
	 * will hierarchically traverse through the architectural elements and discover which elements
	 * are not linked up to architecture and need to be "pruned."  All elements in the document
	 * are cloned before pruning so that the original document is not altered.  The result is a document
	 * containing only the structures and types used in the architecture. Additionally, the pruner 
	 * removes any version graph whose internal nodes are not referenced by any type included in the 
	 * selected architecture.
	 *
	 * @param archURL 		This is the URL of the xADL document that needs to be pruned
	 * @param targetArchURL This is the URL of the new xADL document that will be created and store the pruned architecture
	 * @param startingID 	This is the ID of the element that the pruning algorithm should start from
	 * @param isStructural 	If this is true then the startingID is that of an archStruct, otherwise it is an
	 *							ID to a type
	 * @param msgID			The ID of the C2Component that creates and calls this pruning service.  The ID is then used
	 *							by the requesting component to determine whether or not to care about messages
	 *							it receives from this service.  This parameter can be set to null if the component
	 *							is not concerned with identifying a message's origin.
	 *
	 * @throws InvalidURLException 			If the provided URL to open an xArch document is invalid
	 * @throws InvalidElementIDException 	If the provided starting ID is invalid
	 * @throws MissingElementException		If an expected element is missing
	 * @throws MissingAttributeException	If an expected attribute on an element is missing
	 * @throws BrokenLinkException			If a link between elements does not match up or exist
	 */
	public void prune(String archURL, String targetArchURL, String startingID, boolean isStructural, String msgID)
		throws InvalidURLException, InvalidElementIDException, MissingElementException, 
			  	MissingAttributeException, BrokenLinkException
	{
		// Get a reference to the open architecture
		ObjRef origArch = xarch.getOpenXArch(archURL);
		if(origArch == null)
		{
			throw new InvalidURLException("Error: The URL \"" + archURL + "\" does not point to an open xArch");
		}
		
		if(targetArchURL == null || targetArchURL.length() == 0)
		{
			throw new InvalidURLException("Error: The provided target URL is invalid.  Please provide a non-empty URL");
		}
		
		// Construct PrunerArguments object and make call on pruneArch
		PrunerArguments args = new PrunerArguments();
		args.visitedTypes = new Hashtable(SIZE);
		args.visitedStructures = new Hashtable(SIZE);
		args.visitedVersionNodes = new Hashtable(SIZE);
		args.arch = xarch.cloneXArch(origArch, targetArchURL);
		args.targetURL = targetArchURL;
		args.msgID = msgID;
		args.lowerBound = 0;
		args.upperBound = UPPER_BOUND;
		
		pruneArch(args, startingID, isStructural);
	}
	
	
	// Performs prune on an architecture.
    //
    // arguments - A collection of arguments specific to this xArch pruning
    // startingID - This is the ID of the element that the pruning algorithm should start from
    // isStructural - If this is true then the startingID is that of an archStruct, otherwise it is an
	//			ID to a type
	private void pruneArch(PrunerArguments arguments, String startingID, boolean isStructural)
		throws InvalidElementIDException, MissingElementException, MissingAttributeException, BrokenLinkException
	{
		// Start pruning on the element of the starting id
		ObjRef startElement = xarch.getByID(arguments.arch, startingID);
		
		// Check if the element exists or not
		if(startElement == null)
		{
			throw new InvalidElementIDException("Error: No element exists for the ID \"" + startingID + "\"");
		}
		
		// Fire initial progress message
		fireMessageSent(createNewStatusMessage(arguments));
		
		// Based on value of isStructural value, start on either a Structure or Type
		if(isStructural)
		{
			pruneStructure(startElement, arguments);
			arguments.visitedStructures.put(startingID, startingID);
		}
		else // is type
		{
			pruneType(startElement, arguments);			
			addTypeInfoToVisited(startElement, startingID, arguments);
		}
		
		// Now that all necessary structures and types have been tagged, remove all elements
		// (either ArchStructure or Types) that are not used in the xArch document.
		// This is the actual "pruning" step.
		removeElements(arguments);
		
		// After removal fire complete progress bar message
		arguments.lowerBound = arguments.upperBound;
		fireMessageSent(createNewStatusMessage(arguments));
	}
	
	// Adds the type id to visited list along with any associated version id to a visited list for version nodes
	//
	// typeRef - Reference to type to add to visited list
	// typeID - Id of type
    // arguments - A collection of arguments specific to this xArch pruning
	private void addTypeInfoToVisited(ObjRef typeRef, String typeID, PrunerArguments arguments)
		throws MissingElementException, MissingAttributeException, BrokenLinkException
	{
		arguments.visitedTypes.put(typeID, typeID);	// Add the id to visited list of types
		
		// Add version node id to visited list
		if(xarch.isInstanceOf(typeRef, "edu.uci.isr.xarch.versions.IVariantComponentTypeImplVers") || 
			xarch.isInstanceOf(typeRef, "edu.uci.isr.xarch.versions.IVariantConnectorTypeImplVers")||
			xarch.isInstanceOf(typeRef, "edu.uci.isr.xarch.versions.IInterfaceTypeImplVers"))
		{			
			ObjRef versionNodeLinkRef = (ObjRef)xarch.get(typeRef, "versionGraphNode");
			
			if(versionNodeLinkRef != null)
			{
				ObjRef versionNodeRef = resolveLink(arguments.arch, versionNodeLinkRef, typeID);
				String nodeID = (String)xarch.get(versionNodeRef, "Id");
				
				if(!arguments.visitedVersionNodes.containsKey(nodeID))
				{
					arguments.visitedVersionNodes.put(nodeID, nodeID);
				}
			}
		}
	}
	
	// This method is responsible for examining the Components and Connectors and 
	// calling a prune operation on the elements' respective types.
	//
	// structure - The reference to this ArchStructure element
    // arguments - A collection of arguments specific to this xArch pruning
	private void pruneStructure(ObjRef structure, PrunerArguments arguments)
		throws MissingElementException, MissingAttributeException, BrokenLinkException
	{		
		// Get all child elements
		ObjRef [] componentRefs = xarch.getAll(structure, "Component");
		if(componentRefs != null)
		{
			examineStructureElements(componentRefs, arguments);
		}
		
		ObjRef [] connectorRefs = xarch.getAll(structure, "Connector");
		if(connectorRefs != null)
		{
			examineStructureElements(connectorRefs, arguments);
		}
	}
	
	// This method examines Connector and Component elements and tags their respective types if necessary.
	// Acts as a helper to pruneStructure()
	//
	// elements - list of either components or connectors
	// arguments - A collection of arguments specific to this xArch pruning
	private void examineStructureElements(ObjRef [] elements, PrunerArguments arguments)
		throws MissingElementException, MissingAttributeException, BrokenLinkException
	{		
		ObjRef xmlLink;
		String hRef = "";
		ObjRef typeRef;
		String typeID;
		String interfaceID;
		
		// Traverse each component and connector element in structure
		int size = elements.length;
		for(int i = 0; i < size; ++i)
		{
			// Check interfaces of element
			ObjRef [] interfaces = xarch.getAll(elements[i], "Interface");
			for(int j = 0; j < interfaces.length; ++j)
			{
				// Get link to interfaceType
				xmlLink = (ObjRef)xarch.get(interfaces[j], "Type");
				typeRef = resolveLink(arguments.arch, xmlLink, (String)xarch.get(interfaces[j], "Id"));
				typeID = (String)xarch.get(typeRef, "Id");
				
				// If this type is not already in the visited list
				if(!arguments.visitedTypes.containsKey(typeID))
				{
					// InterfaceTypes are the simple case, no further inspection necessary so just tag them
					//arguments.visitedTypes.put(typeID, typeID);
					addTypeInfoToVisited(typeRef, typeID, arguments);
				}
			}
			
			// Get link to (Connector/Component)Type
			xmlLink = (ObjRef)xarch.get(elements[i], "Type");		
			typeRef = resolveLink(arguments.arch, xmlLink, (String)xarch.get(elements[i], "Id"));
			typeID = (String)xarch.get(typeRef, "Id");
			
			// If this type is not already in the visited list
			if(!arguments.visitedTypes.containsKey(typeID))
			{				
				// First prune that type
				pruneType(typeRef, arguments);
				
				// ... then add to visited list
				//arguments.visitedTypes.put(typeID, typeID);
				addTypeInfoToVisited(typeRef, typeID, arguments);
			}			
		}
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
	
	// This method inspects a type for an internal substructure and calls the prune operation on that substructure if one exists.
	// This method is only called on types that are either ComponentTypes or ConnectorTypes
	//
	// type - A reference to a type, either a ComponentType or ConnectorType.  InterfaceTypes should not appear here.
	// arguments - A collection of arguments specific to this xArch pruning
	private void pruneType(ObjRef type, PrunerArguments arguments)
		throws MissingElementException, MissingAttributeException, BrokenLinkException
	{
		String id = "";
		
		// If this has VariantTypes, check each variant
		if(xarch.isInstanceOf(type, "edu.uci.isr.xarch.versions.IVariantComponentTypeImplVers") || 
			xarch.isInstanceOf(type, "edu.uci.isr.xarch.versions.IVariantConnectorTypeImplVers"))
		{
			ObjRef [] variantRefs = xarch.getAll(type, "Variant");
			
			// Probably an error if it is null
			if(variantRefs != null)
			{
				for(int i = 0; i < variantRefs.length; ++i)
				{
					ObjRef variantTypeLink = (ObjRef)xarch.get(variantRefs[i], "VariantType");
					ObjRef variantTypeRef = resolveLink(arguments.arch, variantTypeLink, (String)xarch.get(type, "Id"));
					
					// If not visited, check this
					id = (String)xarch.get(variantTypeRef, "Id");
					if(!arguments.visitedTypes.containsKey(id))
					{
						pruneType(variantTypeRef, arguments);
						
						//arguments.visitedTypes.put(id, id);
						addTypeInfoToVisited(variantTypeRef, id, arguments);
					}
				}
			}
		}
		
		// Regardless, of being a variant type or not, check for substructure
		ObjRef subArchRef = (ObjRef)xarch.get(type, "SubArchitecture");
		if(subArchRef != null)
		{
			// Get a link to the substructure 
			ObjRef structLinkRef = (ObjRef)xarch.get(subArchRef, "ArchStructure");
			ObjRef archStructRef = resolveLink(arguments.arch, structLinkRef, (String)xarch.get(type, "Id"));//(String)xarch.get(subArchRef, "Id"));
			
			// Add to visited list if necessary
			id = (String)xarch.get(archStructRef, "Id");
			if(!arguments.visitedStructures.containsKey(id))
			{
				// Prune on that substructure
				pruneStructure(archStructRef, arguments);
				
				arguments.visitedStructures.put(id, id);
			}
		}
	}
	
	// This method removes from the architecture all structures and types that are not needed.  It does so by calling
	// two helper methods
	//
	// arguments - A collection of arguments specific to this xArch pruning
	private void removeElements(PrunerArguments arguments)
	{
		ObjRef typesContextRef = xarch.createContext(arguments.arch, "types");		
		ObjRef versionsContextRef = xarch.createContext(arguments.arch, "versions");
		ObjRef [] archStructures = xarch.getAllElements(typesContextRef, "archStructure", arguments.arch);	// List of all structures
		ObjRef [] archTypes = xarch.getAllElements(typesContextRef, "archTypes", arguments.arch);	// Get list of all types
		ObjRef [] archVersions = xarch.getAllElements(versionsContextRef, "archVersions", arguments.arch);
		
		ObjRef [] componentTypes;
		ObjRef [] connectorTypes;
		ObjRef [] interfaceTypes;
		ObjRef [] componentTypeVersionGraphs;
		ObjRef [] connectorTypeVersionGraphs;
		ObjRef [] interfaceTypeVersionGraphs;
		
		// To perform the progressbar update it is necessary to know how many structure and type elements there
		// are in the xADL document.  Visiting each one would result in an update to the progress bar.
		int numElements = archStructures.length;
		
		// Sum up all the type elements
		int size = archTypes.length;
		for(int i = 0; i < size; ++i)
		{
			// Get all ComponentTypes
			componentTypes = xarch.getAll(archTypes[i], "ComponentType");
			numElements += componentTypes.length;
			
			// Get all ConnectorTypes
			connectorTypes = xarch.getAll(archTypes[i], "ConnectorType");
			numElements += connectorTypes.length;
			
			// Get all InterfaceTypes
			interfaceTypes = xarch.getAll(archTypes[i], "InterfaceType");
			numElements += interfaceTypes.length;
		}
		
		// Update progressAmount by number of versionGraphs
		size = archVersions.length;
		for(int i = 0; i < size; ++i)
		{
			// All ComponentTypeVersionGraphs
			componentTypeVersionGraphs = xarch.getAll(archVersions[i], "ComponentTypeVersionGraph");
			numElements += componentTypeVersionGraphs.length;
			
			// All ConnectorTypeVersionGraphs
			connectorTypeVersionGraphs = xarch.getAll(archVersions[i], "ConnectorTypeVersionGraph");
			numElements += connectorTypeVersionGraphs.length;
			
			// All InterfaceTypeVersionGraphs
			interfaceTypeVersionGraphs = xarch.getAll(archVersions[i], "InterfaceTypeVersionGraph");			
			numElements += interfaceTypeVersionGraphs.length;
		}

		// Get the amount to increase the progress bar by for each change
		double progressAmount = UPPER_BOUND / numElements;
		
		// Remove structures, types, and versionGraphs
		removeStructures(arguments, archStructures, progressAmount);
		removeTypes(arguments, archTypes, progressAmount);
		removeVersionGraphs(arguments, archVersions, progressAmount);
	}
	
	// This method is a helper method to removeElements() and assists by removing the structures of non-tagged (non-visited) structures
	//
	// arguments - A collection of arguments specific to this xArch pruning
	// archStructures - A list of all the ArchStructures in the xADL document
	// progressAmount - The amount to increase the progress bar by for each change
	private void removeStructures(PrunerArguments arguments, ObjRef [] archStructures, double progressAmount)
	{
		Vector structsToRemove = new Vector();
		
		int size = archStructures.length;
		String id = "";
		
		// Traverse along structures and remove any that aren't tagged
		for(int i = 0; i < size; ++i)
		{
			id = (String)xarch.get(archStructures[i], "Id");
			if(!arguments.visitedStructures.containsKey(id))
			{
				// Get list of non-tagged structures
				structsToRemove.add(archStructures[i]);
			}
			
			arguments.lowerBound += progressAmount;
			fireMessageSent(createNewStatusMessage(arguments));
		}

		// Remove all the non-tagged ArchStructures.  By making one call to the xarchlibraries we have a slight efficiency gain
		ObjRef [] nonTaggedStructs = new ObjRef[structsToRemove.size()];
		structsToRemove.toArray(nonTaggedStructs);
		xarch.remove(arguments.arch, "Object", nonTaggedStructs);
	}
	
	// This method is a helper method to removeElements() and assists by removing the Types of non-tagged (non-visisted) types
	//
    // arguments - A collection of arguments specific to this xArch pruning
	// archTypes - A list of all the ArchTypes in the xADL document
	// progressAmount - The amount to increase the progress bar by for each change
	private void removeTypes(PrunerArguments arguments, ObjRef [] archTypes, double progressAmount)
	{
		ObjRef [] componentTypes;
		ObjRef [] connectorTypes;
		ObjRef [] interfaceTypes;
		
		int size = archTypes.length;
		String id = "";
		for(int i = 0; i < size; ++i)
		{
			// Get all ComponentTypes
			componentTypes = xarch.getAll(archTypes[i], "ComponentType");
			removeTypesHelper(componentTypes, archTypes[i], "ComponentType", arguments, progressAmount);
			
			// Get all ConnectorTypes
			connectorTypes = xarch.getAll(archTypes[i], "ConnectorType");
			removeTypesHelper(connectorTypes, archTypes[i], "ConnectorType", arguments, progressAmount);
			
			// Get all InterfaceTypes
			interfaceTypes = xarch.getAll(archTypes[i], "InterfaceType");
			removeTypesHelper(interfaceTypes, archTypes[i], "InterfaceType", arguments, progressAmount);
		}
	}
	
	// Helper method for removeTypes
	// Removes a list of element types for a particular type: either ComponentType, ConnectorType, or InterfaceType
	//
	// elementTypes - A list of element types that are to be inspected for removal
	// archTypesRef - Reference to the ArchTypes element that contains the list of types in question
	// typeName - A String specifying the name of the type that is to be removed (either ComponentType, ConnectorType, or InterfaceType)
    // arguments - A collection of arguments specific to this xArch pruning
	// progressAmount - The amount to increase the progress bar by for each change
	private void removeTypesHelper(ObjRef [] elementTypes, ObjRef archTypesRef, String typeName, PrunerArguments arguments, double progressAmount)
	{
		String id = "";
		Vector typesToRemove = new Vector();
		
		for(int j = 0; j < elementTypes.length; ++j)
		{
			id = (String)xarch.get(elementTypes[j], "Id");
			
			// If this type is not tagged, remove it
			if(!arguments.visitedTypes.containsKey(id))
			{
				// Collect all non-tagged types
				typesToRemove.add(elementTypes[j]);
			}
			
			arguments.lowerBound += progressAmount;
			fireMessageSent(createNewStatusMessage(arguments));
		}
		
		// Remove this set of non-tagged types.  Making one call to xarchlibraries instead of many is done for an efficiency gain.
		ObjRef [] nonTaggedTypes = new ObjRef[typesToRemove.size()];
		typesToRemove.toArray(nonTaggedTypes);
		xarch.remove(archTypesRef, typeName, nonTaggedTypes);
	}
	
	
	// Method that removes all the versionGraphs that do not have any nodes which are version nodes of types
	// used in the selected product architecture.
	//
    // arguments - A collection of arguments specific to this xArch pruning
	// archVersions - Collection of archVersion elements
	// progressAmount - The amount to increase the progress bar by for each change
	private void removeVersionGraphs(PrunerArguments arguments, ObjRef [] archVersions, double progressAmount)
	{
		// Get all ArchVersions from this xArch
		ObjRef [] componentTypeVersionGraphs;
		ObjRef [] connectorTypeVersionGraphs;
		ObjRef [] interfaceTypeVersionGraphs;
		
		for(int i = 0; i < archVersions.length; ++i)
		{
			// All ComponentTypeVersionGraphs
			componentTypeVersionGraphs = xarch.getAll(archVersions[i], "ComponentTypeVersionGraph");
			removeVersionGraphsHelper(componentTypeVersionGraphs, archVersions[i], "ComponentTypeVersionGraph", arguments, progressAmount);
			
			// All ConnectorTypeVersionGraphs
			connectorTypeVersionGraphs = xarch.getAll(archVersions[i], "ConnectorTypeVersionGraph");
			removeVersionGraphsHelper(connectorTypeVersionGraphs, archVersions[i], "ConnectorTypeVersionGraph", arguments, progressAmount);
			
			// All InterfaceTypeVersionGraphs
			interfaceTypeVersionGraphs = xarch.getAll(archVersions[i], "InterfaceTypeVersionGraph");
			removeVersionGraphsHelper(interfaceTypeVersionGraphs, archVersions[i], "InterfaceTypeVersionGraph", arguments, progressAmount);
		}		
	}
	
	// Helper method for removeVersionGraphs
	// This method inspects all version graphs (either ComponentVersionGraphs, ConnectorVersionGraphs or InterfaceVersionGraphs)
	// and removes those that don't have nodes referenced by typeImplVers types.
	//
	// versionGraphs - Collection of versionGraphs to inspect
	// archVersionsRef - Reference to an archVersion element that contains all the elements found in versionGraphs
	// versionGraphName - String specifying the name of the versionGraph that is to be removed (either ComponentVersionGraph, ConnectorVersionGraph, or InterfaceVersionGraph)
    // arguments - A collection of arguments specific to this xArch pruning
	// progressAmount - The amount to increase the progress bar by for each change
	private void removeVersionGraphsHelper(ObjRef [] versionGraphs, ObjRef archVersionsRef, String versionGraphName, PrunerArguments arguments, double progressAmount)
	{
		// For each Graph, check the internal nodes to see if they are tagged or not.  If 
		// at least one is tagged that means we can't touch the version graph.  If none are tagged
		// then the graph should be removed from the ArchVersions element.  		
		Vector graphsToRemove = new Vector();
		
		for(int i = 0; i < versionGraphs.length; ++i)
		{
			if(shouldVersionGraphBeRemoved(versionGraphs[i], arguments))
			{
				graphsToRemove.add(versionGraphs[i]);
			}
			
			arguments.lowerBound += progressAmount;
			fireMessageSent(createNewStatusMessage(arguments));
		}
		
		// Remove all graphs at once
		ObjRef [] nonTaggedGraphs = new ObjRef[graphsToRemove.size()];		
		graphsToRemove.toArray(nonTaggedGraphs);		
		xarch.remove(archVersionsRef, versionGraphName, nonTaggedGraphs);
	}
	
	// This method determines if a particular versionGraph should be removed from the pruned xADL document or not.
	//
	// versionGraphRef - A reference to a single versionGraph
    // arguments - A collection of arguments specific to this xArch pruning
	private boolean shouldVersionGraphBeRemoved(ObjRef versionGraphRef, PrunerArguments arguments)
	{
		ObjRef [] nodes = xarch.getAll(versionGraphRef, "node");
		
		String id = "";
		for(int i = 0; i < nodes.length; ++i)
		{
			id = (String)xarch.get(nodes[i], "Id");
			if(arguments.visitedVersionNodes.containsKey(id))
				return false;
		}

		// No node is tagged and therefore it is safe to remove this entire version graph
		return true;
	}
	
	/*------------------------------------*
	 * Message Related Functionality
	 *------------------------------------*/
	
	// This method creates and returns a new message of the type 
	// ArchPrunerStatusMessage.
	//
    // arguments - A collection of arguments specific to this xArch pruning
	private ArchPrunerStatusMessage createNewStatusMessage(PrunerArguments arguments)
	{
		int currentValue = (int)arguments.lowerBound;
		int upperBound= (int)arguments.upperBound;
		
		// Make sure lower bound is not greater than upper bound
		if(currentValue > upperBound)
		{
			currentValue = upperBound;
		}
		
		return new ArchPrunerStatusMessage(arguments.targetURL, arguments.msgID, currentValue, 
			upperBound, false, false, null);
	}
	
	/** 
     * This method sends off a provided message through all of the message listeners
	 *
	 * @param m A message to be sent
	 */
	protected void fireMessageSent(Message m)
	{
		int size = messageListeners.size();
		for(int i = 0; i < size; ++i)
		{
			((MessageListener)messageListeners.elementAt(i)).messageSent(m);
		}
	}
	
	/**
     * This method adds a message listener to the list of message listeners
	 *
	 * @param l Message listener
     */
	public void addMessageListener(MessageListener l)
	{
		messageListeners.add(l);
	}
	
	/**
	 * This method removes a message listener from the list of message listeners
	 *
	 * @param l Message listener
     */
	public void removeMessageListener(MessageListener l)
	{
		messageListeners.remove(l);
	}
}
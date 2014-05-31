package archstudio.comp.pruneversions;


// Add support for xArchADT
import edu.uci.ics.xarchutils.*;

import c2.fw.*;

// Standard imports
import java.util.*;
import java.io.*;


/**
 * This is the class that provides the implementation of the PruneVersions service. The 
 * PruneVersions service is responsible 
 * for taking an architecture and removing any unneeded archStructures and archTypes.
 * The end result is an architecture that consists only of archStructures, found in types
 * as substructure (where needed), and archTypes that are types of elements within archStructures.
 * Any disjoint archTypes and archStructures will then not be included.  This could be used, for
 * example, after an architecture has been run through a selector.  The larger architecture will
 * only have a subset selected from it and the rest will then be removed after being run through
 * the pruner.
 *
 * @author Christopher Van der Westhuizen <A HREF="mailto:vanderwe@uci.edu">(vanderwe@uci.edu)</A> 
 */

public class PruneVersionsImpl implements IPruneVersions, MessageProvider
{
	// This inner helper class acts as a means to collect together all the arguments
	// that would be needed by most of the methods in PruneVersionsImpl.
	// The advantage to using this class is to group together variables that will
	// be used by a particular instance of the PruneVerions interface and allowing
	// for reentrant code.
	private class PruneVersionsArguments
	{
		public ObjRef arch;				// reference to the xArch element of the architecture being examined
		public String targetURL;		// URL of the target document
		public String msgID;
		public double lowerBound;		// lower bound of current pruning progress
		public double upperBound;		// upper bound of current pruning progress
		public double progressAmount;	// the amount by which to update the progress bar for every increment
	}
	
	// Interface through which DOM-based xArch libraries can be accessed
	protected XArchFlatInterface xarch; 	

	// Size of list for holding message listeners
	private static final int NUM_MESSAGES = 1;
	
	// Size for upper bound of progress bar
	private static final double UPPER_BOUND = 100;
	
	// List of message listeners
	protected Vector messageListeners = new Vector(NUM_MESSAGES);
	
	/**
	 * This is the constructor for PruneVersions
	 *
	 * @param xArch	A reference to the XArchFlatInterface that allows for the accessing of DOM-based xArch libraries
	 */
	public PruneVersionsImpl(XArchFlatInterface xArch)
	{
		xarch = xArch;
	}
	
	
	/**
	 * This is the function that is responsible for calling the "prune versions" algorithm.  The algorithm
	 * will visit all types and remove any links to nodes in versionGraphs.  Additionally, 
	 * the PruneVersions service will remove all archVersion tags in the xADL document.  The entire document
	 * is cloned before pruning so that pruning is performed on a copy of the document.  The result is a document
	 * containing only the structures and types used in the architecture without any version information. 
	 *
	 * @param archURL 		This is the URL of the xADL document that needs to be pruned
	 * @param targetArchURL This is the URL of the new xADL document that will be created and store the pruned architecture
	 * @param msgID			The ID of the component that creates and calls this pruning service.  The ID is then used
	 *							by the requesting component to determine whether or not to care about messages
	 *							it receives from this service.  This parameter can be set to null if the component
	 *							is not concerned with identifying a message's origin.
	 *
	 * @throws InvalidURLException 	If the provided URL to open an xArch document is invalid
	 */	
	public void pruneVersions(String archURL, String targetArchURL, String msgID)
		throws InvalidURLException
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
		
		// Create and initialize a PruneVersionsArguments object
		PruneVersionsArguments args = new PruneVersionsArguments();
		args.arch = xarch.cloneXArch(origArch, targetArchURL);
		args.targetURL = targetArchURL;
		args.msgID = msgID;
		args.lowerBound = 0;
		args.upperBound = UPPER_BOUND;
		
		// Now perform pruneVersions algorithm
		pruneArch(args);
	}
			
	// This method calls helper functions that take care of pruning version information from a xADL document
	// This is performed in two steps: 1. Removing version info from types; 2. Removing archVersion elements
	// from the xADL document.
	//
    // arguments - A collection of arguments specific to this xArch pruning
	private void pruneArch(PruneVersionsArguments arguments)
	{
		// Fire initial progress message
		fireMessageSent(createNewStatusMessage(arguments));
		
		// Go through all types and remove links
		removeTypeVersionLinks(arguments);
		
		// Remove all archVersions elements		
		removeArchVersions(arguments);

		// Fire complete progress message
		arguments.lowerBound = arguments.upperBound;
		fireMessageSent(createNewStatusMessage(arguments));
	}
	
	// This method is responsible for removing version information from types
	//
    // arguments - A collection of arguments specific to this xArch pruning
	private void removeTypeVersionLinks(PruneVersionsArguments arguments)
	{
		ObjRef typesContextRef = xarch.createContext(arguments.arch, "types");
		ObjRef [] archTypes = xarch.getAllElements(typesContextRef, "archTypes", arguments.arch);
		ObjRef [] componentTypeRefs;
		ObjRef [] connectorTypeRefs;
		ObjRef [] interfaceTypeRefs;
		
		Vector compTypes = new Vector();
		Vector connTypes = new Vector();
		Vector intTypes = new Vector();
		
		// Value to hold total number of Types in document
		int numElements = 0;
		
		// Calculate number of types to be used in progressAmount computation
		// The majority of the work being done is in updating Types.  Minimal
		// work is required for the removal of archVersions and, as such,
		// it is only necessary to perform progress updates based on Type operations
		for(int i = 0; i < archTypes.length; ++i)
		{
			componentTypeRefs = xarch.getAll(archTypes[i], "ComponentType");
			numElements += componentTypeRefs.length;
			compTypes.addAll(Arrays.asList(componentTypeRefs));
			
			connectorTypeRefs = xarch.getAll(archTypes[i], "ConnectorType");
			numElements += connectorTypeRefs.length;
			connTypes.addAll(Arrays.asList(connectorTypeRefs));
			
			interfaceTypeRefs = xarch.getAll(archTypes[i], "InterfaceType");
			numElements += interfaceTypeRefs.length;
			intTypes.addAll(Arrays.asList(interfaceTypeRefs));
		}
		
		// Convert all Vectors of Types into ObjRef arrays
		componentTypeRefs = new ObjRef[compTypes.size()];
		compTypes.toArray(componentTypeRefs);
		
		connectorTypeRefs = new ObjRef[connTypes.size()];
		connTypes.toArray(connectorTypeRefs);
		
		interfaceTypeRefs = new ObjRef[intTypes.size()];
		intTypes.toArray(interfaceTypeRefs);
		
		// Calculate progress amount value that will change with each operation in the prune versions algorithm
		arguments.progressAmount = UPPER_BOUND / numElements;

		// Remove each of the three different types {components, connectors, interfaces}
		removeTypeVersionLinksHelper(componentTypeRefs, "ComponentType", arguments);
		removeTypeVersionLinksHelper(connectorTypeRefs, "ConnectorType", arguments);
		removeTypeVersionLinksHelper(interfaceTypeRefs, "InterfaceType", arguments);
	}
	
	// This method assists the removeTypeVersionLinks method by inspecting a set of ComponentTypes, ConnectorTypes, and InterfaceTypes and then
	// clearing the versionGraphNode element of the respective type
	//
	// elementTypes - Collection of types that are to be inspected
	// elementName - String name representing what types are passed into the method: either ComponentTyeps, ConnectorTypes, or InterfaceTypes
    // arguments - A collection of arguments specific to this xArch pruning
	private void removeTypeVersionLinksHelper(ObjRef [] elementTypes, String elementName, PruneVersionsArguments arguments)
	{
		for(int i = 0; i < elementTypes.length; ++i)
		{
			// If this is a ImplVers type, i.e. with versioning information
			if(xarch.isInstanceOf(elementTypes[i], "edu.uci.isr.xarch.versions.IVariantComponentTypeImplVers") ||
			   xarch.isInstanceOf(elementTypes[i], "edu.uci.isr.xarch.versions.IVariantConnectorTypeImplVers") ||
			   xarch.isInstanceOf(elementTypes[i], "edu.uci.isr.xarch.versions.IInterfaceTypeImplVers"))
			{
				// Clear the VersionGraphNode element
				xarch.clear(elementTypes[i], "VersionGraphNode");
			}
			
			arguments.lowerBound += arguments.progressAmount;
			fireMessageSent(createNewStatusMessage(arguments));
		}
	}

	// This method is responsible for removing all archVersion elements in the document
	//
    // arguments - A collection of arguments specific to this xArch pruning
	private void removeArchVersions(PruneVersionsArguments arguments)
	{
		ObjRef versionsContextRef = xarch.createContext(arguments.arch, "versions");
		ObjRef [] archVersions = xarch.getAllElements(versionsContextRef, "archVersions", arguments.arch);
		
		if(archVersions != null)
		{
			xarch.remove(arguments.arch, "Object", archVersions);
		}
	}


	/*------------------------------------*
	 * Message Related Functionality
	 *------------------------------------*/
	
	// This method creates and returns a new message of the type 
	// PruneVersionsStatusMessage.
	//
	// arguments - A collection of arguments specific to this xArch pruning
	private PruneVersionsStatusMessage createNewStatusMessage(PruneVersionsArguments arguments)
	{
		int currentValue = (int)arguments.lowerBound;
		int upperBound= (int)arguments.upperBound;
		
		// Make sure lower bound is not greater than upper bound
		if(currentValue > upperBound)
		{
			currentValue = upperBound;
		}
		
		return new PruneVersionsStatusMessage(arguments.targetURL, arguments.msgID, currentValue, 
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
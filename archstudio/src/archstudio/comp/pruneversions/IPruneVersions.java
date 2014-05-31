package archstudio.comp.pruneversions;

/**
 * Interface that provides the PrunesVersions service.  PruneVersions is responsible for taking a xADL
 * file and then removing all archVersion elements and any links to the nodes in those versionGraphs.
 *
 * @author Christopher Van der Westhuizen
 */

public interface IPruneVersions
{
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
		throws InvalidURLException;
}
package archstudio.comp.plamerge;


/**
 * Interface that provides the Merge service.  PLAMerge is responsible for taking a diff
 * file and applying the diff instructions to a target xArch document; in doing so,
 * the target xArch document has its architecture "updated" with the chages specified by
 * the diff.
 *
 * @author Christopher Van der Westhuizen, Akash Garg, Ping Chen, Matt Critchlow
 */
public interface IPLAMerge
{
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
				MissingAttributeException, BrokenLinkException;
}
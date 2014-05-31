
package archstudio.comp.pladiff;

/**
 * Interface that provides the Diffing service.  PLADiff is responsible for taking two architecture 
 * descriptions and producing a diff description.  A diff description contains the instructions 
 * necessary to transform the first architecture into the second through a series of adds and removals.  
 * @author Ping H. Chen, Matt Critchlow, Akash Garg, Chris Van der Westhuizen
 */

public interface IPLADiff
{
	/**
	 * This constant is used to denote that the root DiffPart should be
	 * applied to an ArchStructure
	 */
	public static final String STRUCTURAL_STARTING_POINT = "*Root - Structural*";
	
	/**
	 * This constant is used to denote that the root DiffPart should be
	 * applied to an Type
	 */
	public static final String TYPE_STARTING_POINT = "*Root - Type*";
	
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
		throws PLADiffException, MissingElementException, BrokenLinkException;
}
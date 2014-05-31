
package archstudio.comp.archmerge;

/**
 * IArchMerge - This is the interface exposing the functions provided by
 * the arch merge algorithm.  The merge algorithm will merge a design time
 * diff document with a structure document.  The merge process will be implemented
 * so that it is carried out in a transaction.
 * Note: Currently this doesn't handle group and subarchitecture
 * @see package archstudio.comp.archdiff;
 * @author Ping Hsin Chen <A HREF="mailto:pingc@hotmail.com">(pingc@hotmail.com)</A> 
 */

public interface IArchMerge
{
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
	 * @param diffURL The location of the diff document.  The file should already be OPEN
	 * @param archURL The location of the structure document.  The file hsould already be OPEN.
	 * @exception InvalidLocationException Thrown when the component could not find an open diff or instance doc
	 *				at the location specified.
	 * @exception ArchMergeException Thrown when the merge algorithm encounters some unrecoverable problem.
	 */
	public void merge( String diffURL, String archURL )
		throws InvalidLocationException, ArchMergeException;
}

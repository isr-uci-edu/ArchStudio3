
package archstudio.comp.archmerge;

import archstudio.comp.xarchtrans.*;  // this is to use the xarch trans
import edu.uci.ics.xarchutils.*;      

/**
 * ArchMergeImpl - this is the implementation for IArchMerge.  This class will provide all
 * the functionality required for merging a design time diff with a structure doc.
 *
 * @see package archstudio.comp.archdiff;
 * @author Ping Hsin Chen <A HREF="mailto:pingc@hotmail.com">(pingc@hotmail.com)</A> 
 */

public class ArchMergeImpl implements IArchMerge
{

	// this is the local reference to the xArch transactoins interface
	private XArchFlatTransactionsInterface xArchTrans;
	/**
	 * The constructor, takes in the proxy for the xArchADT
	 */
	public ArchMergeImpl( XArchFlatTransactionsInterface xArchInst )
	{
		xArchTrans = xArchInst;
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
	 * @param diffURL The location of the diff document.  The file should already be OPEN
	 * @param archURL The location of the structure document.  The file hsould already be OPEN.
	 * @exception InvalidLocationException Thrown when the component could not find an open diff or instance doc
	 *				at the location specified.
	 * @exception ArchMergeException Thrown when the merge algorithm encounters some unrecoverable problem.
	 */
	public void merge( String diffURL, String archURL )
		throws InvalidLocationException, ArchMergeException
	{		
		ObjRef diffRef = xArchTrans.getOpenXArch( diffURL );
		ObjRef archRef = xArchTrans.getOpenXArch( archURL );
		
		if( diffRef == null )
			throw new InvalidLocationException( "No diff document was found at: " + diffURL );
		else if( archRef == null )
			throw new InvalidLocationException( "No structure document was found at: " + archURL );
		
		ArchMerge merger = new ArchMerge( xArchTrans );
		
		merger.merge( diffRef, archRef );
	}
}

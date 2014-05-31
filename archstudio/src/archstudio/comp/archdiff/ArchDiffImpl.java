

/**
 * This class provides the implementation for IArchDiff.  This is the implementation of the 
 * IArchDiff interface.  This class provides all the functionalities required for diffing 2 xADL 
 * documents.  
 * @author Christopher Van der Westhuizen
 */

package archstudio.comp.archdiff;

import edu.uci.ics.xarchutils.*;  // this is to use the xArchADT

public class ArchDiffImpl implements IArchDiff
{
	protected XArchFlatInterface xArch;
	
	public ArchDiffImpl( XArchFlatInterface xArchInst )
	{
		xArch = xArchInst;
	}
	
	/** Making a call on diff will perform the differencing algorithm on the two provided
	 * architectures and the output will be sent to diffArchURI, the diff description.
	 * Note the architectures should be open.
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
		// this is the object that will actually carry out all the diff action.
		// it is in its own object to make the code reentrant.
		ArchDiff archDiff = new ArchDiff( xArch );
		
		archDiff.diff( oURI, nURI, dURI );
	}
}

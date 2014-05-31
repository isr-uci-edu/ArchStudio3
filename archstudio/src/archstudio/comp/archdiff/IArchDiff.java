

package archstudio.comp.archdiff;

/**
 * Interface that provides the Diffing service.  ArchDiff is responsible for taking two architecture 
 * descriptions and producing a diff description.  A diff description contains the instructions 
 * necessary to transform the first architecture into the second through a series of adds and removals.  
 * @author Christopher Van der Westhuizen
 */

import java.io.*;
//import org.xml.sax.*;

public interface IArchDiff
{
	/**
	 * Making a call on diff will perform the differencing algorithm on the two provided
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
    public void diff(String origArchURI, String newArchURI, String diffArchURI) 
		throws ParseXArchException;
}
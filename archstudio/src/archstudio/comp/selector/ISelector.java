// Ping Chen
// ISelector.java

package archstudio.comp.selector;

import archstudio.comp.booleaneval.*;

/**
 * This is the Selector interface.  The selector is reponsible for selecting
 * what variant and optional elements should be instantiated based on the 
 * variables and their states that are passed in.  It will create a new xADL 
 * document with the appropriate components instantiated.
 * 
 *
 * @author Ping Hsin Chen <A HREF="mailto:pingc@hotmail.com">(pingc@hotmail.com)</A>
 */

public interface ISelector
{
	/**
	 * This is the function that will call the selection algorithm.  It will try to 
	 * select the appropriate variant and optional element based on the symbol table
	 * passed in.  If part of a guard can not be resolved, it will leave the partial 
	 * guard in place (the algorithm will resolve the parts of the guard whose conditions
	 * are met).  All elements are cloned before selection.
	 * The after fully resolving a variant type, all elements of that variant type will 
	 * be modified to be of the new resolved type.
	 * Note: Only 1 variant can be true at any 1 time.
	 * 
	 * @param archURL This is the url of the xADL architecture to be selected.
	 * @param newArchURL This is the url of the new xADL architecture that will be created based on selection algorithm.
	 * @param symbolTable This is the table that contains all the variables and their values.
	 * @param startingID This is the ID of element the selection process should start from
	 * @param isStructural True if the ID passed in is a archStruct, False if the ID
	 * 		corresponds to a type & version
	 * @param msgID This is the ID of the component requesting the selection service. 
	 * 		This is used so that the status messages will carry this ID thus allowing the
	 *		requesting component to only process status messages that he cares about.
	 * 		This field may be null.
	 *
	 * @exception InvalidURLException This exception is thrown when the string passed in
	 *      for the architecture is not an already openned architecture.
	 * @exception MissingElementException This exception is thrown when the selector
	 *      cannot find a required element in the architecture description.
	 * @exception NoSuchTypeException This exception is thrown when it encounters
	 *      an unknown/invalid type when evaluating.
	 * @exception TypeMismatchException This exception is thrown when the type
	 *      of the operands do not match during an evaluation.
	 * @exception VariantEvaluationException This exception is thrown when a variant type 
	 * 		has an improper (too many or no) variants that evaluated to true.
  	 * @exception BrokenLinkException This exception is thrown when a href resolves to null
	 */
    public void select( String archURL, String newArchURL, SymbolTable table,
						String startingID, boolean isStructural, String msgID )
        throws InvalidURLException, MissingElementException,
        NoSuchTypeException, TypeMismatchException, VariantEvaluationException,
		BrokenLinkException;
        
}
// Ping Chen
// IBooleanEval.java

package archstudio.comp.booleaneval;

import edu.uci.ics.xarchutils.ObjRef;  

/**
 * This is the BooleanEval interface.  The boolean evaluator is responsible for
 * taking in a subtree in the xADL document representing an boolean expression and
 * evaluating the expression to be either true, false, or a partial evaluation.
 * Notes: When comparing strings, it always ignores cases
 *
 * @author Ping Hsin Chen <A HREF="mailto:pingc@hotmail.com">(pingc@hotmail.com)</A>
 */

public interface IBooleanEval
{
    /**
     * This function will evaluate the boolean expression passed in
     * It will try to go through all the possible expressions defined
     * and attempt to evaluate based on the symbol table passed in. The expression
	 * is also cloned.  Tbe original expression is left unchanged
	 * Note: It will ignore case when evaluating strings.  
     *
     * @param exp This is ObjRef to the subtree in the document that contains the boolean
     *            expression that needs to be evaluated.  This subtree is never modified.
     * @param symTable This is the table that contains all the variables and their values.
     *
     * @return ObjRef pointing to a modified version of the cloned expression.  This
     *         expression can only be TRUE, FALSE, or a pruned version of the cloned
     *         expression if the not all variables can be resolved
     *
     * @exception MissingElementException This exception is thrown when the evaluator
     *      cannot find a required element in the expression.
     * @exception NoSuchTypeException This exception is thrown when it encounters
     *      an unknown/invalid type when evaluating.
     * @exception TypeMismatchException This exception is thrown when the type
     *      of the operands do not match during an evaluation.
     */
    public ObjRef eval( ObjRef exp, SymbolTable symTable )
        throws MissingElementException, NoSuchTypeException, TypeMismatchException;   
        
    /**
     * This function takes in a bool reference and returns true or false.
     *
     * @param bool The Bool INSIDE a boolean expression
     *
     * @return The value of the bool (true/false)
     *
     * @exception TypeMismatchException thrown if the element pointed by the bool is not
                  a boolean expression
     * @exception MissingElementException thrown if the element pointed by the bool does
     *            not have a "value" field or is null
     */
    public boolean boolValue( ObjRef bool )
        throws MissingElementException, TypeMismatchException;
}

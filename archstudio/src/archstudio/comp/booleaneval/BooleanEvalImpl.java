// Ping Chen
// BooleanEvalImpl.java

/**
 * This class is the implementation for the IBooleanEval.  It will be able to 
 * evaluate a given boolean expression.  
 */

package archstudio.comp.booleaneval;

import edu.uci.ics.xarchutils.*; 
import java.util.Date;
//import edu.uci.ics.xarchutils.*;  // this is to use the xArchADT

public class BooleanEvalImpl implements IBooleanEval
{
    private XArchFlatInterface xArch;  // this is the local reference to the xArchADT
	
	// NO GLOBAL!!! BAD BAD BAD!
    //private SymbolTable symbolTable;   // this is the local reference to the symbol table
    //private ObjRef boolContext;        // this is the context for bool guards
    
    public BooleanEvalImpl( XArchFlatInterface xArchInst )
    {
        xArch = xArchInst;
        
    }
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
        throws MissingElementException, NoSuchTypeException, TypeMismatchException
    {
        SymbolTable symbolTable = symTable;
        // we need the whole document because we want to create a boolean context
        // we need the boolean context to create new boolean expressions
        ObjRef iXArch = xArch.getXArch( exp );
		if( iXArch == null )
		{
			throw new MissingElementException( 
				"The expression does not belong to an IXArch element." );
		}
        ObjRef boolContext = xArch.createContext( iXArch, "Boolguard" );
        
        // need to make a copy of the expression passed in
		ObjRef newTree = xArch.cloneElement( exp, edu.uci.isr.xarch.IXArchElement.DEPTH_INFINITY );
        // grabs exp's parent
		ObjRef parent = ( ObjRef )xArch.getParent( exp );
		// we need to have exp's parent (BooleanGuard) to point to mini-exp (newTree)
		if( parent != null )
		{
			// parent is a BooleanGuard so it only has a BooleanExp
			xArch.clear( parent, "BooleanExp" );
			// sets the new expression as the child of BooleanGuard
			xArch.set( parent, "BooleanExp", newTree );
			evalExp( newTree, symbolTable, boolContext );
		
			return newTree;
		}
		// missing parents
		throw new MissingElementException( "Expression: " + exp + " is missing a parent element." );
    }
    
    /**
	 * This function takes in a bool reference and returns true or false.
	 *
	 * @param ObjRef for the Bool INSIDE a boolean expression
	 *
	 * @return The value of the bool (true/false)
	 *
	 * @exception TypeMismatchException thrown if the element pointed by the bool is not
	 *		a boolean expression
	 * @exception MissingElementException thrown if the element pointed by the bool does
	 *            not have a "value" field or is null
	 */
    public boolean boolValue( ObjRef bool )
        throws MissingElementException, TypeMismatchException
    {
        if( xArch.isInstanceOf( bool, "edu.uci.isr.xarch.boolguard.IBool" ) )
        {
            // just return value of the bool
            String value = ( String )xArch.get( bool, "Value" );
            // makes sure the value is there          
            // true
            if( value != null && value.equalsIgnoreCase( 
                edu.uci.isr.xarch.boolguard.IBool.ENUM_TRUE ) )
                return true;
            // false
            else if( value != null && value.equalsIgnoreCase( 
                edu.uci.isr.xarch.boolguard.IBool.ENUM_FALSE ) )
                return  false;
            // unknown value is stored
            throw  new MissingElementException( "Bool " + bool + 
                " does not have a value stored." ); 
        }
        else 
            throw new TypeMismatchException( "ObjRef: " + bool + 
				" is not a Boolean value" );
    }

    // This function is what will parse and see what type of the expression it is
    // and then determine the value of the expression. NOte, this WILL modify the
    // exp passed in!!!!
    private void evalExp( ObjRef exp, SymbolTable symbolTable, ObjRef boolContext )
        throws MissingElementException, NoSuchTypeException, TypeMismatchException
    {
        // It will first try to determine what the operator in this expression
        // is.
        ObjRef and = ( ObjRef ) xArch.get( exp, "And" );
        // check for and
        if( and != null )
        {
            handleAnd( and, exp, symbolTable, boolContext );
            return;
        }
        // operator is or
        ObjRef or = ( ObjRef ) xArch.get( exp, "Or" );
        if( or != null )
        {
            handleOr( or, exp, symbolTable, boolContext );
            return;
        }
        // check for equals operator
        ObjRef equals = ( ObjRef ) xArch.get( exp, "Equals" );
        if( equals != null )
        {
        	handleEquals( equals, exp, symbolTable, boolContext );
        	return;
        }
        // operator greater than
        ObjRef greaterThan = ( ObjRef ) xArch.get( exp, "GreaterThan" );
        if( greaterThan != null )
        {
            handleGreaterThan( greaterThan, exp, symbolTable, boolContext );
            return;
        }
        // operator greater than or equals to
        ObjRef greaterThanOrEq = ( ObjRef ) xArch.get( exp, "GreaterThanOrEquals" );
        if( greaterThanOrEq != null )
        {
            handleGreaterThanEq( greaterThanOrEq, exp, symbolTable, boolContext );
            return;
        }
        // operator is less than
        ObjRef lessThan = ( ObjRef ) xArch.get( exp, "LessThan" );
        if( lessThan != null )
        {
            handleLessThan( lessThan, exp, symbolTable, boolContext );
            return;
        }
        // operator is less than or equals to
        ObjRef lessThanOrEq = ( ObjRef ) xArch.get( exp, "LessThanOrEquals" );
        if( lessThanOrEq != null )
        {
            handleLessThanEq( lessThanOrEq, exp, symbolTable, boolContext );
            return;
        }
        // operator is not
        ObjRef not = ( ObjRef ) xArch.get( exp, "Not" );
        if( not != null )
        {
            handleNot( not, exp, symbolTable, boolContext );
            return;
        }
        // operator is not equals
        ObjRef notEquals = ( ObjRef ) xArch.get( exp, "NotEquals" );
        if( notEquals != null )
        {
            handleNotEquals( notEquals, exp, symbolTable, boolContext );   
            return;
        }
        // operator is in range
        ObjRef inRange = ( ObjRef ) xArch.get( exp, "InRange" );
        if( inRange != null )
        {
            handleInRange( inRange, exp, symbolTable, boolContext );
            return;
        }
        // operator is in set
        ObjRef inSet = ( ObjRef ) xArch.get( exp, "InSet" );
        if( inSet != null )
        {
            handleInSet( inSet, exp, symbolTable, boolContext );
            return;
        }
        // check for bool type
        ObjRef bool = ( ObjRef ) xArch.get( exp, "Bool" );
        if( bool != null )
        {
            // just stop, we don't need to modify it at all since it is a bool value
            return;             
        }
        // parenthesis....
        ObjRef paren = ( ObjRef ) xArch.get( exp, "ParenExp" );
        if( paren != null )
        {
            handleParen( paren, exp, symbolTable, boolContext );
            return;
        }
        // does turning ObjRef to a string help!?
        throw new MissingElementException( "Error when evaluating expression " + 
            exp + ":  missing a valid operator." );
    }
    
    // this is just a simple helper function that will extract the symbol
    // ObjRef and then extract the symbol's name as a string.  
    // It will then return the value of that symbol in the symbol table.
    // Null if its not there.  It will
    // throw the appropriate exceptions if something is missing
    // this is mainly for the left hand side of the operator
    private Object getLeftOperand( ObjRef op, SymbolTable symbolTable )
        throws MissingElementException
    {
        ObjRef variable = ( ObjRef ) xArch.get( op, "Symbol" );
        // check to see symbol exists
        if( variable == null )
            throw new MissingElementException( "Operator: " + op + " is missing its " +
                "symbol" );
        String name = ( String )xArch.get( variable, "Value" );
        // check to see the symbol contains a value
        if( name == null || name.equals( "" ) )
            throw new MissingElementException( "Operator: " + op 
				+ " is missing its symbol name for left hand operand." );
        
        return symbolTable.get( name );

    }
    
    // this is a helper function that will extract the value of the right hand
    // operand.  It will first determine if it is another  variable or if it is
    // just a value
    private Object getRightOperand( ObjRef op, SymbolTable symbolTable )
        throws MissingElementException, NoSuchTypeException
    {
        // checks to see if the right hand operand is a another symbol
        ObjRef sym2 = ( ObjRef )xArch.get( op, "Symbol2" );
        if( sym2 != null )
        {
            String sym2Name = ( String )xArch.get( sym2, "Value" );
            // makes sure the symbol has a value - "name"
            if( sym2Name == null || sym2Name.equals( "" ) )
                throw new MissingElementException( "Operator: " + op + 
                    " is missing its symbol name for its right hand operand." );
            else
			{
                return symbolTable.get( sym2Name );
			}
        }
        
        ObjRef valRef = ( ObjRef )xArch.get( op, "Value" );
        // checks to see if it stores a value
        // we don't have to worry about the checks falling through,
        // if the operand is a symbol, it would have exited already by
        // either an exception or a return statement.
        if( valRef != null )
        {
            String value = ( String )xArch.get( valRef, "Value" );
            
            if( value != null && !value.equals( "" ) )
            {
                return TypeParser.parse( value );
            }
            else 
                throw new MissingElementException( "Operator: " + op + 
                    " is missing its value for its right hand operand." );
        }
        else
            throw new MissingElementException( "Operator: " + op + 
                " is missing its right hand operand (symbol or value)." );
    }
    
    // this function will clear the original expression and set the expression 
    // to the boolean value passed in
    private void setBool( ObjRef exp, String original, String value, ObjRef boolContext )
    {
        // clear the original expression
        xArch.clear( exp, original );
        
        // creates a new bool expression with value passed in
        ObjRef bool = xArch.create( boolContext, "Bool" );
        xArch.set( bool, "Value", value );
        // we now set the expression to point to the new bool value
        xArch.set( exp, "Bool", bool );
    }
    
    // this is the helper function for the boolean and
    // it takes in both the or expression as well as the boolean expression
    // containing the and.  This is so that we can prune the expression if necessary
    private void handleAnd( ObjRef and, ObjRef exp, SymbolTable symbolTable, ObjRef boolContext )
        throws NoSuchTypeException, MissingElementException, TypeMismatchException
    {
        ObjRef left = ( ObjRef )xArch.get( and, "BooleanExp1" );
        ObjRef right = ( ObjRef )xArch.get( and, "BooleanExp2" );
        
        // makes sure both expressions exists
        if( left == null || right == null )
            throw new MissingElementException( "Operator and " + and + 
                " is missing left/right boolean expression." );
                
        // the result of the left expression
        evalExp( left, symbolTable, boolContext );
        ObjRef leftResult = ( ObjRef )xArch.get( left, "Bool" );
        // short circuit out, don't have to eval the right exp
        if( leftResult != null && !boolValue( leftResult ) )
        {		
            // whole expression is false
            xArch.clear( exp, "And" );             // need to clear the current exp
            xArch.set( exp, "Bool", leftResult );  // since left result is false
            
            return;  // stops
        }
        
        // result of the right expression
        evalExp( right, symbolTable, boolContext );
        ObjRef rightResult = ( ObjRef )xArch.get( right, "Bool" );
        // as long as one expression is false, "and" returns false
        if( rightResult != null && !boolValue( rightResult ) )
        {
            xArch.clear( exp, "And" );   // need to clear the current exp
            // since the right was false, the whole exp is false
            xArch.set( exp, "Bool", rightResult );
            
            return;
        }
        
        // as long as one of the results were not a boolean, its partial eval
        if( leftResult == null || rightResult == null )
        {
            // both expressions are unknown, so we can't prune any part of it
            if( leftResult == null && rightResult == null )
            {
                return;
            }
            // only the left or right (but not both) expression is unknown
            else
            {
                // gets the reference to the element that contains the current expression
                ObjRef parent = xArch.getParent( exp );
                
                ObjRef unknown;
                // checks to see which expression contains the unknown expression
                if( leftResult == null )
                    unknown = left;
                else
                    unknown = right;
                
				if( parent == null )
				{
					throw new MissingElementException( "Boolean Expression And " +
						exp + " is missing its parent." );
				}
				// now we check to see if the parent is a BooleanGuard, Not, or ParenExp
				if( xArch.isInstanceOf( parent, "edu.uci.isr.xarch.boolguard.IBooleanGuard" ) ||
					xArch.isInstanceOf( parent, "edu.uci.isr.xarch.boolguard.INot" ) ||
					xArch.isInstanceOf( parent, "edu.uci.isr.xarch.boolguard.IParen" ) )
				{
					// these elements ONLY have "BooleanExp"
					xArch.clear( parent, "BooleanExp" );
					xArch.set( parent, "BooleanExp", unknown );
				}
				// the parent must be And/Or 
				else if( xArch.isInstanceOf( parent, "edu.uci.isr.xarch.boolguard.IAnd" ) ||
					xArch.isInstanceOf( parent, "edu.uci.isr.xarch.boolguard.IOr" ) )
				{
					ObjRef exp1 = ( ObjRef ) xArch.get( parent, "BooleanExp1" );
					if( exp1 != null && exp1.equals( unknown ) )
					{
						// the unknown part of the expression is under BooleanExp1
						// so we replace it
						xArch.clear( parent, "BooleanExp1" );
						xArch.set( parent, "BooleanExp1", unknown );
						// done, so return
						return;
					}
					
					ObjRef exp2 = ( ObjRef ) xArch.get( parent, "BooleanExp2" );
					if( exp2 != null && exp2.equals( unknown ) )
					{
						// the unknown part of the expression is BooleanExp2, so we replace that
						xArch.clear( parent, "BooleanExp2" );
						xArch.set( parent, "BooleanExp2", unknown );
						return;
					}
				}
                else
                    throw new MissingElementException( "Boolean Expression And " +
                        exp + " is missing a valid parent.  Unable to reconnect And." );
            }
        }
        // both true, return true
        else
        {
            xArch.clear( exp, "And" );
            // any one of the results since both are true
            xArch.set( exp, "Bool", leftResult ); 
        }
    }
    
    // this is the helper function for the boolean or
    // it takes in both the or expression as well as the boolean expression
    // containing the or.  This is so that we can prune the expression if necessary
    private void handleOr( ObjRef or, ObjRef exp, SymbolTable symbolTable, ObjRef boolContext )
        throws NoSuchTypeException, MissingElementException, TypeMismatchException
    {
        ObjRef left = ( ObjRef )xArch.get( or, "BooleanExp1" );
        ObjRef right = ( ObjRef )xArch.get( or, "BooleanExp2" );
        
        // makes sure both expressions exists
        if( left == null || right == null )
            throw new MissingElementException( "Operator or " + or + 
                " is missing left/right boolean expression." );
                
        // the result of the left expression
        evalExp( left, symbolTable, boolContext );
        ObjRef leftResult = ( ObjRef )xArch.get( left, "Bool" );
        // short circuit out, don't have to eval the right exp
        if( leftResult != null && boolValue( leftResult ) )
        {
            xArch.clear( exp, "Or" );     // need to clear the current exp
            // since the left is true, the whole or is true
            xArch.set( exp, "Bool", leftResult );
            
            return;
        }
        
        // result of the right expression
        evalExp( right, symbolTable, boolContext );
        ObjRef rightResult = ( ObjRef )xArch.get( right, "Bool" );
        
        // as long as one expression is true, "or" returns true
        if( rightResult != null && boolValue( rightResult ) )
        {
            xArch.clear( exp, "Or" );     // need to clear the current exp
            xArch.set( exp, "Bool", rightResult );
            
            return;
        }

        // partial eval, since one of the expressions isn't a bool
        if( rightResult == null || leftResult == null )
        {
            // both expressions are unknown, so we can't prune any part of it
            if( leftResult == null && rightResult == null )
            {
                return;
            }
            else
            {
                ObjRef unknown;
                // checks to see which expression contains the unknown expression
                if( leftResult == null )
                    unknown = left;
                else
                    unknown = right;
                    
                // gets the reference to the element that contains the current expression
                ObjRef parent = xArch.getParent( exp );
				if( parent == null )
				{
					throw new MissingElementException( "Boolean Expression Or " +
						exp + " is missing its parent." );
				}
				// now we check to see if the parent is a BooleanGuard, Not, or ParenExp
				if( xArch.isInstanceOf( parent, "edu.uci.isr.xarch.boolguard.IBooleanGuard" ) ||
					xArch.isInstanceOf( parent, "edu.uci.isr.xarch.boolguard.INot" ) ||
					xArch.isInstanceOf( parent, "edu.uci.isr.xarch.boolguard.IParen" ) )
				{
					// these elements ONLY have "BooleanExp"
					xArch.clear( parent, "BooleanExp" );
					xArch.set( parent, "BooleanExp", unknown );
				}
				// the parent must be And/Or 
				else if( xArch.isInstanceOf( parent, "edu.uci.isr.xarch.boolguard.IAnd" ) ||
					xArch.isInstanceOf( parent, "edu.uci.isr.xarch.boolguard.IOr" ) )
				{
					ObjRef exp1 = ( ObjRef ) xArch.get( parent, "BooleanExp1" );
					if( exp1 != null && exp1.equals( unknown ) )
					{
						// the unknown part of the expression is under BooleanExp1
						// so we replace it
						xArch.clear( parent, "BooleanExp1" );
						xArch.set( parent, "BooleanExp1", unknown );
						// done, so return
						return;
					}
					
					ObjRef exp2 = ( ObjRef ) xArch.get( parent, "BooleanExp2" );
					if( exp2 != null && exp2.equals( unknown ) )
					{
						// the unknown part of the expression is BooleanExp2, so we replace that
						xArch.clear( parent, "BooleanExp2" );
						xArch.set( parent, "BooleanExp2", unknown );
						return;
					}
				}
				else
					throw new MissingElementException( "Boolean Expression Or" +
						exp + "is missing a valid parent.  Unable to reconnect Or." 	);
            }
        }        
        // both expressions were false
        else 
        {
            xArch.clear( exp, "Or" );
            // since both are false, doesn't matter which we use
            xArch.set( exp, "Bool", leftResult );
        }
    }
    
    // this function will handle the equals operator.
    private void handleEquals( ObjRef equals, ObjRef exp, SymbolTable symbolTable, ObjRef boolContext )
        throws NoSuchTypeException, MissingElementException, TypeMismatchException
    {
        Object symbolVal = getLeftOperand( equals, symbolTable );
        Object value = getRightOperand( equals, symbolTable );
        
        // if either one of the value or symbolVal is null means that
        // a symbol's value was not in the symbol table, therefore, we cannot
        // continue evaluation.
        if( value == null || symbolVal == null )
            return;
        // if the symbols are strings, we use need to compare ignoring strings
        if( symbolVal instanceof String && value instanceof String )
        {
            if( ( ( String )symbolVal ).equalsIgnoreCase( 
                  ( String ) value ) )
            {
                setBool( exp, "Equals", edu.uci.isr.xarch.boolguard.IBool.ENUM_TRUE, boolContext );
                return;
            }
            else
            {
                setBool( exp, "Equals", edu.uci.isr.xarch.boolguard.IBool.ENUM_FALSE, boolContext );
                return;
            }
        }
        // check to see if its double or date
        else if( ( symbolVal instanceof Date && value instanceof Date ) ||
                 ( symbolVal instanceof Double && value instanceof Double ) )
        {
            if( symbolVal.equals( value ) )
            {
                setBool( exp, "Equals", edu.uci.isr.xarch.boolguard.IBool.ENUM_TRUE, boolContext );
                return;
            }
            else
            {
                setBool( exp, "Equals", edu.uci.isr.xarch.boolguard.IBool.ENUM_FALSE, boolContext );
                return;
            }
        }
        else
            throw new TypeMismatchException( "Cannot convert symbol of type " +
                symbolVal.getClass( ) + " to value of type " + value.getClass( ) +
                " in operator equals " + equals );
    }
    
    // this funtion will evaluate the greater than expressions
    private void handleGreaterThan( ObjRef greaterThan, ObjRef exp, SymbolTable symbolTable, ObjRef boolContext )
        throws NoSuchTypeException, MissingElementException, TypeMismatchException
    {
        Object left = getLeftOperand( greaterThan, symbolTable );
        Object right = getRightOperand( greaterThan, symbolTable );
        
        // check for partial eval
        if( left == null || right == null )
        {
            return;
        }
        // the types are strings
        else if( left instanceof String && right instanceof String )
        {
            // greater than 0 if the argument is LESS
            if( ( ( String )left ).compareToIgnoreCase( ( String ) right ) > 0 )
            {
                setBool( exp, "GreaterThan", 
                    edu.uci.isr.xarch.boolguard.IBool.ENUM_TRUE, boolContext );
                return;
            }
            else
            {
                setBool( exp, "GreaterThan", 
                    edu.uci.isr.xarch.boolguard.IBool.ENUM_FALSE, boolContext );
                return;
            }
        }
        // double and dates are handled the same way. so we can group them together
        else if( ( left instanceof Date && right instanceof Date ) ||
                 ( left instanceof Double && right instanceof Double ) )
        {
			//System.out.println( "Date or double" );
			
            // greater than 0 if the argument is LESS
            if( ( ( Comparable )left ).compareTo( right ) > 0 )
            {
                setBool( exp, "GreaterThan", 
                    edu.uci.isr.xarch.boolguard.IBool.ENUM_TRUE, boolContext );
                return;
            }
            else
            {
                setBool( exp, "GreaterThan", 
                    edu.uci.isr.xarch.boolguard.IBool.ENUM_FALSE, boolContext );
                return;
            }
        }
        else
            throw new TypeMismatchException( "Unable to convert symbol of type " +
                left.getClass( ) + " to type " + right.getClass( ) + 
                " in operator greater than " + greaterThan );
    }
    
    // this function handles the greater than or equals to
    private void handleGreaterThanEq( ObjRef greaterThanEq, ObjRef exp, SymbolTable symbolTable, ObjRef boolContext )
        throws NoSuchTypeException, MissingElementException, TypeMismatchException
    {
        Object left = getLeftOperand( greaterThanEq, symbolTable );
        Object right = getRightOperand( greaterThanEq, symbolTable );
        
        // check for partial eval
        if( left == null || right == null )
        {
            return;
        }
        // the types are strings
        else if( left instanceof String && right instanceof String )
        {
            // greater than 0 if the argument is LESS
            if( ( ( String )left ).compareToIgnoreCase( ( String ) right ) >= 0 )
            {
                setBool( exp, "GreaterThanOrEquals", 
                    edu.uci.isr.xarch.boolguard.IBool.ENUM_TRUE, boolContext );
                return;
            }
            else
            {
                setBool( exp, "GreaterThanOrEquals", 
                    edu.uci.isr.xarch.boolguard.IBool.ENUM_FALSE, boolContext );
                return;
            }
        }
        // double and dates are handled the same way. so we can group them together
        else if( ( left instanceof Date && right instanceof Date ) ||
                 ( left instanceof Double && right instanceof Double ) )
        {
            // greater than 0 if the argument is LESS
            if( ( ( Comparable )left ).compareTo( right ) >= 0 )
            {
                setBool( exp, "GreaterThanOrEquals", 
                    edu.uci.isr.xarch.boolguard.IBool.ENUM_TRUE, boolContext );
                return;
            }
            else
            {
                setBool( exp, "GreaterThanOrEquals", 
                    edu.uci.isr.xarch.boolguard.IBool.ENUM_FALSE, boolContext );
                return;
            }
        }
        else
            throw new TypeMismatchException( "Unable to convert symbol of type " +
                left.getClass( ) + " to type " + right.getClass( ) + 
                " in operator greater than or equals " + greaterThanEq );
    }
    
    // this function handles the less than expression
    private void handleLessThan( ObjRef lessThan, ObjRef exp, SymbolTable symbolTable, ObjRef boolContext )
        throws NoSuchTypeException, MissingElementException, TypeMismatchException
    {
        Object left = getLeftOperand( lessThan, symbolTable );
        Object right = getRightOperand( lessThan, symbolTable );
        
        // check for partial eval
        if( left == null || right == null )
        {
            return;
        }
        // the types are strings
        else if( left instanceof String && right instanceof String )
        {
            // less than 0 if the argument is GREATER
            if( ( ( String )left ).compareToIgnoreCase( ( String ) right ) < 0 )
            {
                setBool( exp, "LessThan", 
                    edu.uci.isr.xarch.boolguard.IBool.ENUM_TRUE, boolContext );
                return;
            }
            else
            {
                setBool( exp, "LessThan", 
                    edu.uci.isr.xarch.boolguard.IBool.ENUM_FALSE, boolContext );
                return;
            }
        }
        // double and dates are handled the same way. so we can group them together
        else if( ( left instanceof Date && right instanceof Date ) ||
                 ( left instanceof Double && right instanceof Double ) )
        {
            // less than 0 if the argument is GREATER
            if( ( ( Comparable )left ).compareTo( right ) < 0 )
            {
                setBool( exp, "LessThan", 
                    edu.uci.isr.xarch.boolguard.IBool.ENUM_TRUE, boolContext );
                return;
            }
            else
            {
                setBool( exp, "LessThan", 
                    edu.uci.isr.xarch.boolguard.IBool.ENUM_FALSE, boolContext );
                return;
            }
        }
        else
            throw new TypeMismatchException( "Unable to convert symbol of type " +
                left.getClass( ) + " to type " + right.getClass( ) + 
                " in operator less than " + lessThan );
    }
    
    // this helper function is responsible for handling the less than or equals to
    // expression
    private void handleLessThanEq( ObjRef lessThanEq, ObjRef exp, SymbolTable symbolTable, ObjRef boolContext )
        throws NoSuchTypeException, MissingElementException, TypeMismatchException
    {
        Object left = getLeftOperand( lessThanEq, symbolTable );
        Object right = getRightOperand( lessThanEq, symbolTable );
        
        // check for partial eval
        if( left == null || right == null )
        {
            return;
        }
        // the types are strings
        else if( left instanceof String && right instanceof String )
        {
            // less than 0 if the argument is GREATER
            if( ( ( String )left ).compareToIgnoreCase( ( String ) right ) <= 0 )
            {
                setBool( exp, "LessThanOrEquals", 
                    edu.uci.isr.xarch.boolguard.IBool.ENUM_TRUE, boolContext );
                return;
            }
            else
            {
                setBool( exp, "LessThanOrEquals", 
                    edu.uci.isr.xarch.boolguard.IBool.ENUM_FALSE, boolContext );
                return;
            }
        }
        // double and dates are handled the same way. so we can group them together
        else if( ( left instanceof Date && right instanceof Date ) ||
                 ( left instanceof Double && right instanceof Double ) )
        {
            // less than 0 if the argument is GREATER
            if( ( ( Comparable )left ).compareTo( right ) <= 0 )
            {
                setBool( exp, "LessThanOrEquals", 
                    edu.uci.isr.xarch.boolguard.IBool.ENUM_TRUE, boolContext );
                return;
            }
            else
            {
                setBool( exp, "LessThanOrEquals", 
                    edu.uci.isr.xarch.boolguard.IBool.ENUM_FALSE, boolContext );
                return;
            }
        }
        else
            throw new TypeMismatchException( "Unable to convert symbol of type " +
                left.getClass( ) + " to type " + right.getClass( ) + 
                " in operator less than or equals " + lessThanEq );
    }
    
    // this helper function is responsible for handling the not-expression
    private void handleNot( ObjRef not, ObjRef exp, SymbolTable symbolTable, ObjRef boolContext )
        throws NoSuchTypeException, MissingElementException, TypeMismatchException
    {
        ObjRef innerExp = ( ObjRef )xArch.get( not, "BooleanExp" );
        // gets the internal expression and evaluate
        if( innerExp != null )
        {
            evalExp( innerExp, symbolTable, boolContext );
			
			ObjRef value = ( ObjRef )xArch.get( innerExp, "Bool" );
			// first check to see if it was evaluated to a simple bool value
			if( value != null )
			{
	            // true so we need to make it false
	            if( boolValue( value ) )
	            {
					setBool( exp, "Not", edu.uci.isr.xarch.boolguard.IBool.ENUM_FALSE, boolContext );
	            }
	            // false becomes true
	            else
	            {
	                setBool( exp, "Not", edu.uci.isr.xarch.boolguard.IBool.ENUM_TRUE, boolContext );
	            }
				return;
			}
            // partial eval
            else
            {
				// we don't need to do anything, the inner exp is already pruned as much as possible
                return;
            }
        }
        else
            throw new MissingElementException( "Not expression " + not + 
                " is missing its internal expression." );
    }
            
    
    // this function handles the not equals operator.
    private void handleNotEquals( ObjRef notEq, ObjRef exp, SymbolTable symbolTable, ObjRef boolContext )
        throws NoSuchTypeException, MissingElementException, TypeMismatchException
    {
		Object symbolVal = getLeftOperand( notEq, symbolTable );
		Object value = getRightOperand( notEq, symbolTable );
		
		// if either one of the value or symbolVal is null means that
		// a symbol's value was not in the symbol table, therefore, we cannot
		// continue evaluation.
		if( value == null || symbolVal == null )
			return;
		// if the symbols are strings, we use need to compare ignoring strings
		if( symbolVal instanceof String && value instanceof String )
		{
			// equals, so we create a new node with false
			if( ( ( String )symbolVal ).equalsIgnoreCase( 
				( String ) value ) )
			{
				setBool( exp, "NotEquals", edu.uci.isr.xarch.boolguard.IBool.ENUM_FALSE, boolContext );
				return;
			}
			// true
			else
			{
				setBool( exp, "NotEquals", edu.uci.isr.xarch.boolguard.IBool.ENUM_TRUE, boolContext );
				return;
			}
		}
		// check to see if its double or date
		else if( ( symbolVal instanceof Date && value instanceof Date ) ||
			( symbolVal instanceof Double && value instanceof Double ) )
		{
			if( symbolVal.equals( value ) )
			{
				setBool( exp, "NotEquals", edu.uci.isr.xarch.boolguard.IBool.ENUM_FALSE, boolContext );
				return;
			}
			else
			{
				setBool( exp, "NotEquals", edu.uci.isr.xarch.boolguard.IBool.ENUM_TRUE, boolContext );
				return;
			}
		}
		else
			throw new TypeMismatchException( "Cannot convert symbol of type " +
			symbolVal.getClass( ) + " to value of type " + value.getClass( ) +
			" in operator not equals " + notEq );
    }
    
    // this function handles the in range operator and returns
    // true if the symbol is within the 2 bounds
    private void handleInRange( ObjRef inRange, ObjRef exp, SymbolTable symbolTable, ObjRef boolContext )
        throws MissingElementException, NoSuchTypeException, TypeMismatchException
    {
        Object varVal = getLeftOperand( inRange, symbolTable );
        // makes sure the variable has some value in the symbol table
        if( varVal == null )
            return;
       
        ObjRef[] bounds = xArch.getAll( inRange, "Value" );
        // makes sure the bounds exists and that there is only an upper and lower
        // bound
        if( bounds == null || bounds.length != 2 )
            throw new MissingElementException( "In range " + inRange + 
                " have invalid number of bounds (must have only 2)." );
        Object bound1 = TypeParser.parse( ( String )xArch.get( bounds[0], "Value" ) );
        Object bound2 = TypeParser.parse( ( String )xArch.get( bounds[1], "Value" ) );
        
        // determine what the bounds are, and makes sure they are the same
        if( ( bound1 instanceof String && bound2 instanceof String ) ||
            ( bound1 instanceof Date && bound2 instanceof Date ) ||
            ( bound1 instanceof Double && bound2 instanceof Double ) )
        {
            // make sure the variable is a string and that the bounds are strings too
            if( varVal instanceof String && bound1 instanceof String )
            {
                // makes sure that bound1 is less than bound2
                if( ( ( String )bound1 ).compareToIgnoreCase( 
                      ( String )bound2 ) < 0 )
                {
                    // checks to see if the variable value is inbetween the range
                    if( ( ( String )bound1 ).compareToIgnoreCase( 
                          ( String )varVal ) <= 0 &&
                        ( ( String )bound2 ).compareToIgnoreCase( 
                          ( String )varVal ) >= 0 )
                    {
                        setBool( exp, "InRange", 
                            edu.uci.isr.xarch.boolguard.IBool.ENUM_TRUE, boolContext );
                        return ;
                    }
                    else
                    {
                        setBool( exp, "InRange", 
                            edu.uci.isr.xarch.boolguard.IBool.ENUM_FALSE, boolContext );
                        return;
                    }
                }
                // bound2 is less than bound1
                else
                {
                    if( ( ( String )bound2 ).compareToIgnoreCase( 
                          ( String )varVal ) <= 0 &&
                        ( ( String )bound1 ).compareToIgnoreCase( 
                          ( String )varVal ) >= 0 )
                    {
                        setBool( exp, "InRange", 
                            edu.uci.isr.xarch.boolguard.IBool.ENUM_TRUE, boolContext );
                        return;
                    }
                    else
                    {
                        setBool( exp, "InRange", 
                            edu.uci.isr.xarch.boolguard.IBool.ENUM_FALSE,
							boolContext);
                        return;
                    }
                }
                
            }
            // the bounds are Dates or Doubles.  there doesn't need to be a
            // difference in handling those 2 types
            else if( ( bound1 instanceof Date  && varVal instanceof Date ) ||
                     ( bound1 instanceof Double && varVal instanceof Double ) )
            {
                // makes sure that bound1 is less than bound2
                if( ( ( Comparable )bound1 ).compareTo(  bound2 ) < 0 )
                {
                    // checks to see if the variable value is inbetween the range
                    if( ( ( Comparable )bound1 ).compareTo( varVal ) <= 0 &&
                        ( ( Comparable )bound2 ).compareTo( varVal ) >= 0 )
                    {
                        setBool( exp, "InRange", 
                            edu.uci.isr.xarch.boolguard.IBool.ENUM_TRUE, boolContext );
                        return;
                    }
                    else
                    {
                        setBool( exp, "InRange", 
                            edu.uci.isr.xarch.boolguard.IBool.ENUM_FALSE, boolContext );
                        return;
                    }
                }
                // bound2 is less than bound1
                else
                {
                    if( ( ( Comparable )bound2 ).compareTo( varVal ) <= 0 &&
                        ( ( Comparable )bound1 ).compareTo( varVal ) >= 0 )
                    {
                        setBool( exp, "InRange", 
                            edu.uci.isr.xarch.boolguard.IBool.ENUM_TRUE, boolContext );
                        return ;
                    }
                    else
                    {
                        setBool( exp, "InRange", 
                            edu.uci.isr.xarch.boolguard.IBool.ENUM_FALSE, boolContext );
                        return;
                    }
                }
            }
            else
                throw new TypeMismatchException( "Variable " + varVal +
                    " cannot convert from " + varVal.getClass( ) +
                    " to bound type " + bound1.getClass( ) + 
                    " in operator in range" + inRange);
        }
        // the bounds' type do not match
        else
        {
            throw new TypeMismatchException( "Bound1's type " + bound1.getClass( ) +
                " does not match bound2's type " + bound2.getClass( ) +
                " in operator in range " + inRange );
        }
    }
    
    // this function handles the in set operator and returns the 
    // the true if it the symbol is contained within the set
    private void handleInSet( ObjRef inSet, ObjRef exp, SymbolTable symbolTable, ObjRef boolContext )
        throws MissingElementException, NoSuchTypeException, TypeMismatchException
    {
        Object varValue = getLeftOperand( inSet, symbolTable );
        // the variable does not have an associated value
        // partial evaluation
        if( varValue == null )
            return;
        
         ObjRef[] set = xArch.getAll( inSet, "Value" );
         // makes sure the set exists
         if( set == null )
            throw new MissingElementException( "In set " + inSet + 
				" is missing the set of values." );
         // goes through the set
         for( int i = 0; i < set.length; i++ )
         {
            String temp = ( String )xArch.get( set[i], "Value" );
            // makes sure the value exists
            if( temp == null )
                throw new MissingElementException( i + "th element " + set[i] + 
                    " in In Set " + inSet + " does not have a value stored." );
            // converts the string value to its appropriate object
            Object setValues = TypeParser.parse( temp );
            
            // makes sure that the type of the symbol is the same as the type of the set elements
            // string comparison ignores case
            if( varValue instanceof String && setValues instanceof String ) 
            {
                if( ( ( String )varValue ).equalsIgnoreCase( ( String )setValues ) )
                {
                    setBool( exp, "InSet", 
                            edu.uci.isr.xarch.boolguard.IBool.ENUM_TRUE, boolContext );
                    return;
                }
            }
            else if( ( varValue instanceof Date && setValues instanceof Date ) || 
                     ( varValue instanceof Double && setValues instanceof Double ) )
            {
                // checks to see if the variable contains some value within the set
                if( varValue.equals( setValues ) )
                {
                    setBool( exp, "InSet", 
                            edu.uci.isr.xarch.boolguard.IBool.ENUM_TRUE, boolContext );
                    return;
                }
            }
            else
                throw new TypeMismatchException( "Cannot convert from symbol of type " + 
                    varValue.getClass( ) + " to set element type of " +  
                    setValues.getClass( ) + " in operator in set" + inSet );
         }
         // finished traversing the set, did not find a match
         setBool( exp, "InSet", edu.uci.isr.xarch.boolguard.IBool.ENUM_FALSE, boolContext );
         return;
    }
        
    // this function takes in a parenthesis reference and the expression
    // it evaluates what is within the parens and modifies the exp 
    private void handleParen( ObjRef paren, ObjRef exp, SymbolTable symbolTable, ObjRef boolContext )
        throws MissingElementException, NoSuchTypeException, TypeMismatchException
    {
        ObjRef innerExp = ( ObjRef ) xArch.get( paren, "BooleanExp" );
        if( innerExp != null )
        {
            evalExp( innerExp, symbolTable, boolContext );  // evaluate the inner expression
            
			ObjRef value = ( ObjRef ) xArch.get( innerExp, "Bool" );
			// make sure we evaluated it properly, so it is only a 
			// boolean value, then we can remove the parens
			if( value != null )
			{
	            // clears the parens
	            xArch.clear( exp, "ParenExp" );
	            xArch.set( exp, "Bool", value );
			}
			// else, partial eval, we leave the parens there
        }
        else
            throw new MissingElementException( "Parenthesis " + paren + 
                " is missing its internal boolean expression" );    
    }
}
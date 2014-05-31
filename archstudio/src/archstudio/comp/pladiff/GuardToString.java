package archstudio.comp.pladiff;

import edu.uci.ics.xarchutils.*;
import java.util.*;
/**
* This class will take in an ObjRef guard, and recursively produce a String version of the
* boolean expression contained in the guard.
*/
public class GuardToString
{
	private XArchFlatInterface xarch;

	public GuardToString(XArchFlatInterface xArch)
	{
		xarch = xArch;
	}

	/**********************Start of Section for retrieving guards*********************/
	// Takes in a BooleanGuard and calls helper function exprToString to get the string version
	// of the expression
	public String guardToString(ObjRef guard)
	{
		ObjRef exp = ( ObjRef ) xarch.get( guard, "BooleanExp" );
		if(exp != null)
			return exprToString(exp);
		return "";
	}

	//helper function which is called by guardToExpr which recursively evaluates a guard to
	//a viewable string
	private String exprToString(ObjRef expression)
	{
		ObjRef paren = (ObjRef)xarch.get(expression, "ParenExp");
		if(paren != null)
		{
			ObjRef internal = (ObjRef)xarch.get( paren, "BooleanExp" );
			String buffer = "(" + exprToString(internal) + ")";
			return buffer;
		}
		ObjRef and = (ObjRef)xarch.get(expression, "And");
		if(and != null)
		{
			ObjRef left = ( ObjRef )xarch.get( and, "BooleanExp1" );
			ObjRef right = ( ObjRef )xarch.get( and, "BooleanExp2" );
			String buffer = "(" + exprToString(left) + " && " + exprToString(right) + ")";
			return buffer;
		}
		ObjRef or = (ObjRef)xarch.get(expression, "Or");
		if(or != null)
		{
			ObjRef left = ( ObjRef )xarch.get( or, "BooleanExp1" );
			ObjRef right = ( ObjRef )xarch.get( or, "BooleanExp2" );
			String buffer = "(" + exprToString(left) + " || " + exprToString(right) + ")";
			return buffer;
		}
		ObjRef not = (ObjRef)xarch.get(expression, "Not");
		if(not != null)
		{
			ObjRef Notexpression = (ObjRef)xarch.get(not, "BooleanExp");
			String buffer = "!(" + exprToString(Notexpression) + ")";
			return buffer;
		}
		ObjRef greaterThan = (ObjRef)xarch.get(expression, "GreaterThan");
		if(greaterThan != null)
		{
			return convertExpression(greaterThan, ">");
		}
		ObjRef lessThan = (ObjRef)xarch.get(expression, "LessThan");
		if(lessThan != null)
		{
			return convertExpression(lessThan, "<");
		}
		ObjRef greaterThanEq = (ObjRef)xarch.get(expression, "GreaterThanOrEquals");
		if(greaterThanEq != null)
		{
			return convertExpression(greaterThanEq, ">=");
		}
		ObjRef lessThanEq = (ObjRef)xarch.get(expression, "LessThanOrEquals");
		if(lessThanEq != null)
		{
			return convertExpression(lessThanEq, "<=");
		}
		ObjRef eq = (ObjRef)xarch.get(expression, "Equals");
		if(eq != null)
		{
			return convertExpression(eq, "==");
		}
		ObjRef Neq = (ObjRef)xarch.get(expression, "NotEquals");
		if(Neq != null)
		{
			return convertExpression(Neq, "!=");
		}
		ObjRef inSet = (ObjRef)xarch.get(expression, "InSet");
		if(inSet != null)
		{
			ObjRef symbol = (ObjRef)xarch.get(inSet, "Symbol");
			String symbolS = (String) xarch.get(symbol, "Value");
			String buffer = symbolS + " @ {";
			ObjRef[] values = xarch.getAll(inSet, "Value");
			int i;
			for (i = 0; i != values.length-1; ++i)
			{
				String v = (String) xarch.get(values[i], "Value");
				buffer = buffer + v + ", ";
			}
			String v = (String) xarch.get(values[i], "Value");
			buffer = buffer + v + "}";
			return buffer;
		}
		ObjRef inRange = (ObjRef)xarch.get(expression, "InRange");
		if(inRange != null)
		{
			ObjRef symbol = (ObjRef)xarch.get(inRange, "symbol");
			String symbolS = (String) xarch.get(symbol, "Value");
			ObjRef[] values = (ObjRef[])xarch.getAll(inRange, "Value");
			String value0 = (String)xarch.get(values[0], "Value");
			String value1 = (String)xarch.get(values[1], "Value");
			String buffer = symbolS + " @ [" + value0 + "," + value1 + "]";
			return buffer;
		}
		return null;
	}

	//This helper method for exprToString() will take in an expression and extract
	//it's correspond symbol and value(s)
	//The String will then be returned in a diplayable context.
	private String convertExpression(ObjRef exp, String symbol)
	{
		ObjRef left;
		ObjRef right;
		String leftValue;
		String rightValue;

		left  = (ObjRef) xarch.get(exp, "Symbol");
		leftValue = (String) xarch.get(left, "Value");
		right = (ObjRef) xarch.get(exp, "Symbol2");
		if (right != null)
		{
			rightValue = (String) xarch.get(right, "Value");
		}
		else
		{
			right = (ObjRef) xarch.get(exp, "Value");
			rightValue = (String) xarch.get(right, "Value");
		}
		String ret = leftValue + " " + symbol + " " + rightValue;
		return ret;
	}
	/**********************End of Section for retrieving guards*********************/
}
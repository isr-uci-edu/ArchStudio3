package archstudio.comp.booleannotation;

import java.io.*;
import edu.uci.ics.xarchutils.*;

public class BooleanGuardConverter{
	
	static{
		new BooleanGuardParser(new StringReader(""));
	}
	
	public static ObjRef parseBooleanGuard(
		XArchFlatInterface xarch,
		String expression, ObjRef xArchRef)
		throws ParseException, TokenMgrError{

		StringReader reader = new StringReader(expression);
		BooleanGuardParser.ReInit(reader);

		SimpleNode n = BooleanGuardParser.Start();

		//ObjRef xArchRef = xarch.getXArch(optionalRef);
		ObjRef booleanContextRef = xarch.createContext(xArchRef, "boolguard");
		ObjRef guardRef = ((BPStart)n).toXArch(booleanContextRef, xarch);
		return guardRef;
	}
	
	public static String booleanGuardToString(XArchFlatInterface xarch,
	ObjRef optionalRef){
		ObjRef guardRef = (ObjRef)xarch.get(optionalRef, "guard");
		if(guardRef != null){
			if(xarch.isInstanceOf(guardRef, "edu.uci.isr.xarch.boolguard.IBooleanGuard")){
				ObjRef booleanExpRef = (ObjRef)xarch.get(guardRef, "BooleanExp");
				if(booleanExpRef != null){
					return convertBooleanExpression(xarch, booleanExpRef);
				}
			}
		}
		return null;
	}
	
	private static String convertBooleanExpression(XArchFlatInterface xarch, ObjRef booleanExp)
	{
		ObjRef expression = null;

		expression = (ObjRef) xarch.get(booleanExp, "And");
		if (expression != null)
		{
		ObjRef left  = (ObjRef) xarch.get(expression, "BooleanExp1");
		ObjRef right = (ObjRef) xarch.get(expression, "BooleanExp2");

		String ret = "(" + convertBooleanExpression(xarch, left) + " && " +
					 convertBooleanExpression(xarch, right) + ")";

		return ret;
		}

		expression = (ObjRef) xarch.get(booleanExp, "Or");
		if (expression != null)
		{
		ObjRef left  = (ObjRef) xarch.get(expression, "BooleanExp1");
		ObjRef right = (ObjRef) xarch.get(expression, "BooleanExp2");

		String ret = "(" + convertBooleanExpression(xarch, left) + " || " +
					 convertBooleanExpression(xarch, right) + ")";

		return ret;
		}

		expression = (ObjRef) xarch.get(booleanExp, "Not");
		if (expression != null)
		{
		ObjRef parameter = (ObjRef) xarch.get(expression, "BooleanExp");

		String ret = "!(" + convertBooleanExpression(xarch, parameter) + ")";

		return ret;
		}

		expression = (ObjRef) xarch.get(booleanExp, "Equals");
		if (expression != null)
		{
		return convertRelationalExpression(xarch, expression, "==");
		}

		expression = (ObjRef) xarch.get(booleanExp, "NotEquals");
		if (expression != null)
		{
		return convertRelationalExpression(xarch, expression, "!=");
		}

		expression = (ObjRef) xarch.get(booleanExp, "GreaterThan");
		if (expression != null)
		{
		return convertRelationalExpression(xarch, expression, ">");
		}

		expression = (ObjRef) xarch.get(booleanExp, "GreaterThanOrEquals");
		if (expression != null)
		{
		return convertRelationalExpression(xarch, expression, ">=");
		}

		expression = (ObjRef) xarch.get(booleanExp, "LessThan");
		if (expression != null)
		{
		return convertRelationalExpression(xarch, expression, "<");
		}

		expression = (ObjRef) xarch.get(booleanExp, "LessThanOrEquals");
		if (expression != null)
		{
		return convertRelationalExpression(xarch, expression, "<=");
		}

		expression = (ObjRef) xarch.get(booleanExp, "InSet");
		if (expression != null)
		{
		ObjRef param = (ObjRef) xarch.get(expression, "Symbol");
		String paramName = (String) xarch.get(param, "Value");

		String ret = paramName + " @ {";
		ObjRef[] values = xarch.getAll(expression, "Value");
		int i;
		for (i = 0; i != values.length-1; ++i)
		{
			String v = (String) xarch.get(values[i], "Value");
			ret = ret + v + ", ";
		}

		String v = (String) xarch.get(values[i], "Value");
		ret = ret + v + "}";

		return ret;
		}

		expression = (ObjRef) xarch.get(booleanExp, "InRange");
		if (expression != null)
		{
		ObjRef param = (ObjRef) xarch.get(expression, "Symbol");
		String paramName = (String) xarch.get(param, "Value");

		String ret = paramName + " @ [";

		ObjRef[] values = xarch.getAll(expression, "Value");
		String value1 = (String) xarch.get(values[0], "Value");
		String value2 = (String) xarch.get(values[1], "Value");

		ret = ret + value1 + ", " + value2 + "]";
		return ret;
		}

		return "";
	}

	private static String convertRelationalExpression(XArchFlatInterface xarch, ObjRef expression,
										   String symbol)
	{
		ObjRef left;
		ObjRef right;
		String leftValue;
		String rightValue;

		left  = (ObjRef) xarch.get(expression, "Symbol");
		leftValue = (String) xarch.get(left, "Value");

		right = (ObjRef) xarch.get(expression, "Symbol2");
		if (right != null)
		{
			rightValue = (String) xarch.get(right, "Value");
		}
		else
		{
			right = (ObjRef) xarch.get(expression, "Value");
			rightValue = (String) xarch.get(right, "Value");
		}

			String ret = leftValue + " " + symbol + " " + rightValue;
		return ret;
	}
	
}

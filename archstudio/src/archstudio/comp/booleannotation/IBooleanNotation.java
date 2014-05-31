package archstudio.comp.booleannotation;

import edu.uci.ics.xarchutils.ObjRef;

public interface IBooleanNotation {

	public ObjRef parseBooleanGuard(String expression, ObjRef xArchRef)
		throws ParseException, TokenMgrError;
	public String booleanGuardToString(ObjRef optionalRef);
	
}

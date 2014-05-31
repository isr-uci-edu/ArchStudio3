package archstudio.comp.graphlayout;

import edu.uci.ics.xarchutils.ObjRef;

public interface IGraphLayout {
	public static final int TOOL_DOT = 100;
	public static final int TOOL_NEATO = 105;

	public GraphLayout doDotLayout(ObjRef archStructureRef, GraphParameters gp) throws
		CantFindGraphLayoutToolException;
	public GraphLayout doNeatoLayout(ObjRef archStructureRef, GraphParameters gp) throws
		CantFindGraphLayoutToolException;
	public GraphLayout doLayout(int tool, ObjRef archStructureRef, GraphParameters gp) throws
		CantFindGraphLayoutToolException;
}

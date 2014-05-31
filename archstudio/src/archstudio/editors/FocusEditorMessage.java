package archstudio.editors;

import c2.fw.*;
import edu.uci.ics.xarchutils.*;

public class FocusEditorMessage extends NamedPropertyMessage{
	
	public static final int FOCUS_EXISTING_DOCS = 100;
	public static final int FOCUS_OPEN_DOCS = 200;
	public static final int FOCUS_OPEN_EDITORS = 300;
	
	public FocusEditorMessage(String[] editorIDs, ObjRef xArchRef, ObjRef ref,
		int focusType){
		super("FocusEditorMessage");
		super.addParameter("editorIDs", editorIDs);
		super.addParameter("xArchRef", xArchRef);
		super.addParameter("refs", new ObjRef[]{ref});
		super.addParameter("focusType", focusType);
	}
	
	public FocusEditorMessage(String[] editorIDs, ObjRef xArchRef, ObjRef[] refs,
		int focusType){
		super("FocusEditorMessage");
		super.addParameter("editorIDs", editorIDs);
		super.addParameter("xArchRef", xArchRef);
		super.addParameter("refs", refs);
		super.addParameter("focusType", focusType);
	}
	
	protected FocusEditorMessage(FocusEditorMessage copyMe){
		super(copyMe);
	}
	
	public Message duplicate(){
		return new FocusEditorMessage(this);
	}
	
	public String[] getEditorIDs(){
		return (String[])getParameter("editorIDs");
	}
	
	public ObjRef getXArchRef(){
		return (ObjRef)getParameter("xArchRef");
	}
	
	public ObjRef[] getRefs(){
		return (ObjRef[])getParameter("refs");
	}
	
	public int getFocusType(){
		return getIntParameter("focusType");
	}
	
}

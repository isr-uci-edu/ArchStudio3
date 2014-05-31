package archstudio.editors;

import c2.fw.*;

public class ChangeEditorStatusMessage extends NamedPropertyMessage{
	
	public static final int STATUS_ACTIVE = 100;
	public static final int STATUS_INACTIVE = 200;
	
	public ChangeEditorStatusMessage(String editorID, int newStatus){
		super("EditorChangeStatusMessage");
		super.addParameter("editorID", editorID);
		super.addParameter("newStatus", newStatus);
	}

	protected ChangeEditorStatusMessage(ChangeEditorStatusMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new ChangeEditorStatusMessage(this);
	}

	public void setEditorID(String editorID){
		addParameter("editorID", editorID);
	}

	public String getEditorID(){
		return (String)getParameter("editorID");
	}

	public void setNewStatus(int newStatus){
		addParameter("newStatus", newStatus);
	}

	public int getNewStatus(){
		return getIntParameter("newStatus");
	}

}


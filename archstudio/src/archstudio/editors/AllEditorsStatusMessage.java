package archstudio.editors;

import c2.fw.*;

public class AllEditorsStatusMessage extends NamedPropertyMessage{
	public AllEditorsStatusMessage(String[] activeEditorIDs){
		super("AllEditorsStatusMessage");
		super.addParameter("activeEditorIDs", activeEditorIDs);
	}

	protected AllEditorsStatusMessage(AllEditorsStatusMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new AllEditorsStatusMessage(this);
	}

	public void setActiveEditorIDs(String[] activeEditorIDs){
		addParameter("activeEditorIDs", activeEditorIDs);
	}

	public String[] getActiveEditorIDs(){
		return (String[])getParameter("activeEditorIDs");
	}

}


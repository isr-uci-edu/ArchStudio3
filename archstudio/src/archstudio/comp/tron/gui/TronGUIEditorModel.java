package archstudio.comp.tron.gui;

import archstudio.editors.*;

public class TronGUIEditorModel{

	public String[] activeEditorIDs = new String[0];
	
	public TronGUIEditorModel(){
	}
	
	public void handleAllEditorsStatus(AllEditorsStatusMessage m){
		this.activeEditorIDs = m.getActiveEditorIDs();
	}
	
	public String[] getActiveEditorIDs(){
		return activeEditorIDs;
	}
	
}

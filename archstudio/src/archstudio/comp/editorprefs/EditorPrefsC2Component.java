package archstudio.comp.editorprefs;

import archstudio.editors.*;
import archstudio.preferences.*;
import archstudio.comp.preferences.*;
import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;

import c2.fw.*;
import c2.legacy.*;
import c2.pcwrap.EBIWrapperUtils;
import c2.util.ArrayUtils;

public class EditorPrefsC2Component extends AbstractC2DelegateBrick{

	protected String[] activeEditors = new String[0];
	
	public EditorPrefsC2Component(Identifier id){
		super(id);
		
		EditorPreferencePanel epp = new EditorPreferencePanel(activeEditors);
		PreferencesUtils.deployPreferencesService(this, bottomIface, "ArchStudio 3/Editors", epp);
		addLifecycleProcessor(new EditorPrefsLifecycleProcessor());
		addMessageProcessor(new EditorPrefsMessageProcessor());
	}
	
	class EditorPrefsLifecycleProcessor extends LifecycleAdapter{
		public void begin(){
			GetAllEditorsStatusMessage gesm = new GetAllEditorsStatusMessage();
			sendToAll(gesm, topIface);
		}
	}
	
	class EditorPrefsMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof AllEditorsStatusMessage){
				AllEditorsStatusMessage aesm = (AllEditorsStatusMessage)m;
				String[] newActiveEditors = aesm.getActiveEditorIDs();
				java.util.Arrays.sort(newActiveEditors);
				boolean same = ArrayUtils.equals(activeEditors, newActiveEditors);
				if(!same){
					activeEditors = newActiveEditors;

					EditorPreferencePanel newepp = new EditorPreferencePanel(activeEditors);
					PreferencesUtils.redeployPreferencesService(
						EditorPrefsC2Component.this, bottomIface, "ArchStudio 3/Editors", newepp);
				}
			}
		}
	}

}

package archstudio.comp.editorprefs;

import archstudio.comp.preferences.IPreferences;

public class EditorPrefsUtils{

	public static String getDefaultEditor(IPreferences preferences){
		if(preferences.keyExists(IPreferences.SYSTEM_SPACE, 
		"/archstudio/comp/editorprefs", "defaulteditor")){
			String defaultEditor = preferences.getStringValue(IPreferences.SYSTEM_SPACE, 
			"/archstudio/comp/editorprefs", "defaulteditor", null);
			return defaultEditor;
		}
		return null;
	}
}

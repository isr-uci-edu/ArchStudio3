package archstudio.comp.editorprefs;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import c2.util.ArrayUtils;

import edu.uci.ics.widgets.JPanelEWL;
import edu.uci.ics.widgets.JPanelUL;
import edu.uci.ics.widgets.JPanelWL;

import archstudio.comp.preferences.IPreferences;
import archstudio.preferences.PreferencePanel;

public class EditorPreferencePanel extends PreferencePanel {

	protected EditorPreferenceJPanel guiPanel;

	public EditorPreferencePanel(String[] availableEditors){
		super();
		guiPanel =  new EditorPreferenceJPanel(availableEditors);
		setComponent(guiPanel);
	}

	public void apply() {
		if(guiPanel != null){
			guiPanel.storeCurrentPreferences();
		}
	}
	
	public void reset(){
		if(guiPanel != null){
			guiPanel.loadCurrentPreferences();
		}
	}

	class EditorPreferenceJPanel extends JPanel{
		
		protected String[] availableEditors;
		protected JComboBox cbDefaultEditor;
		
		public EditorPreferenceJPanel(String[] availableEditors){
			super();
			this.availableEditors = availableEditors;
			
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
			
			JPanel defaultEditorPanel = new JPanel();
			defaultEditorPanel.setLayout(new BoxLayout(defaultEditorPanel, BoxLayout.Y_AXIS));
			
			JLabel lInstructions = new JLabel(
			"<html>" +
			"<div style=\"font-weight: normal\">" +
			"Select a default editor for architecture documents " +
			"here; selections are dependent on what editors are " +
			"available in your local copy of ArchStudio&nbsp;3. " +
			"</div>" +
			"</html>"
			);
			
			JPanel instructionsWLPanel = new JPanelWL(lInstructions, defaultEditorPanel){
				public Insets getInsets(){
					return new Insets(3, 3, 3, 3);
				}
			};
			
			defaultEditorPanel.add(instructionsWLPanel);
			
			String[] editors = new String[availableEditors.length + 1];
			editors[0] = "[None]";
			for(int i = 0; i < availableEditors.length; i++){
				editors[i+1] = availableEditors[i];
			}
			
			cbDefaultEditor = new JComboBox(editors);
			cbDefaultEditor.setEditable(false);
			
			JPanel defaultEditorInputPanel = new JPanel();
			defaultEditorInputPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			
			defaultEditorInputPanel.add(new JLabel("Default Editor:"));
			defaultEditorInputPanel.add(cbDefaultEditor);
			
			defaultEditorPanel.add(defaultEditorInputPanel);
			defaultEditorPanel.setBorder(BorderFactory.createTitledBorder("Default Editor"));
			
			mainPanel.add(/*new JPanelUL(*/defaultEditorPanel/*)*/);
			
			this.setLayout(new BorderLayout());
			this.add("Center", mainPanel);	
		}
		
		public void loadCurrentPreferences(){
			IPreferences preferences = EditorPreferencePanel.this.getPreferences();
			if(preferences == null){
				return;
			}
			
			if(preferences.keyExists(IPreferences.SYSTEM_SPACE, 
			"/archstudio/comp/editorprefs", "defaulteditor")){
				String defaultEditor = preferences.getStringValue(IPreferences.SYSTEM_SPACE, 
				"/archstudio/comp/editorprefs", "defaulteditor", null);
				cbDefaultEditor.setSelectedItem(defaultEditor);
			}
			else{
				cbDefaultEditor.setSelectedIndex(0);
			}
		}
		
		public void storeCurrentPreferences(){
			IPreferences preferences = EditorPreferencePanel.this.getPreferences();
			if(preferences == null){
				return;
			}
			
			String defaultEditor = null;
			//If it's "none" then leave defaultEditor at null, so we'll remove
			//the pref.
			if(cbDefaultEditor.getSelectedIndex() != 0){
				Object defaultEditorObject = cbDefaultEditor.getSelectedItem();
				if(defaultEditorObject != null) defaultEditor = defaultEditorObject.toString();
			}
			
			if(defaultEditor == null){
				preferences.removeKey(IPreferences.SYSTEM_SPACE, "/archstudio/comp/editorprefs", "defaulteditor");
			}
			else{
				preferences.setValue(IPreferences.SYSTEM_SPACE, "/archstudio/comp/editorprefs", "defaulteditor", defaultEditor);
			}
		}
	}
}

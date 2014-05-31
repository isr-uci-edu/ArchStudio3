package archstudio.comp.archipelago.types;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import edu.uci.ics.widgets.ColorPickerDialog;
import edu.uci.ics.widgets.ColorSchemes;
import edu.uci.ics.widgets.JPanelIS;
import edu.uci.ics.widgets.JPanelUL;
import edu.uci.ics.widgets.JPanelWL;
import edu.uci.ics.widgets.WidgetUtils;

import archstudio.comp.preferences.IPreferences;
import archstudio.preferences.PreferencePanel;

public class ArchStructurePreferencePanel extends PreferencePanel {

	protected ArchStructurePreferenceJPanel guiPanel = new ArchStructurePreferenceJPanel();

	public ArchStructurePreferencePanel(){
		super();
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

	class ArchStructurePreferenceJPanel extends JPanel implements ActionListener{
		protected JButton bDefaultComponentColor;
		protected Color defaultComponentColor = BrickMappingLogic.DEFAULT_COMPONENT_COLOR;
		
		protected JButton bDefaultConnectorColor;
		protected Color defaultConnectorColor = BrickMappingLogic.DEFAULT_CONNECTOR_COLOR;
		
		protected JButton bRestoreToDefaultColors;
		
		public ArchStructurePreferenceJPanel(){
			super();
			
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
			
			JPanel allPanel = new JPanel();
			allPanel.setLayout(new BoxLayout(allPanel, BoxLayout.Y_AXIS));
			
			JLabel lInstructions = new JLabel(
			"<html>" +
			"<div style=\"font-weight: normal\">" +
			"Here, you can set defaults dealing with architecture structures. " +
			"</div>" +
			"</html>"
			);
			
			JPanel instructionsWLPanel = new JPanelWL(lInstructions, mainPanel){
				public Insets getInsets(){
					return new Insets(3, 3, 3, 3);
				}
			};
			
			allPanel.add(instructionsWLPanel);
			
			bDefaultComponentColor = new JButton("Set Default Component Color...");
			bDefaultComponentColor.addActionListener(this);
			
			bDefaultConnectorColor = new JButton("Set Default Connector Color...");
			bDefaultConnectorColor.addActionListener(this);
			
			JPanel controlsPanel = new JPanel();
			controlsPanel.setLayout(new GridLayout(2, 1));
			
			controlsPanel.add(bDefaultComponentColor);
			controlsPanel.add(bDefaultConnectorColor);
			
			bRestoreToDefaultColors = new JButton("Restore to Product Defaults");
			bRestoreToDefaultColors.addActionListener(this);
			
			allPanel.add(new JPanelIS(new JPanelUL(controlsPanel), 5));
			allPanel.add(Box.createVerticalStrut(5));
			allPanel.add(new JPanelUL(bRestoreToDefaultColors, "North", "East"));
 			
			allPanel.setBorder(BorderFactory.createTitledBorder("Structure Defaults"));
			
			mainPanel.add(new JPanelUL(allPanel));
			
			this.setLayout(new BorderLayout());
			this.add("Center", mainPanel);	
		}
		
		public void loadCurrentPreferences(){
			IPreferences preferences = ArchStructurePreferencePanel.this.getPreferences();
			if(preferences == null){
				return;
			}
			
			defaultComponentColor = BrickMappingLogic.DEFAULT_COMPONENT_COLOR;
			if(preferences.keyExists(IPreferences.USER_SPACE, 
			"/archstudio/comp/archipelago/types", "defaultComponentColor")){
				int val = preferences.getIntValue(IPreferences.USER_SPACE, 
				"/archstudio/comp/archipelago/types", "defaultComponentColor", -1);
				defaultComponentColor = new Color(val);
			}
			bDefaultComponentColor.setIcon(WidgetUtils.getColorIcon(defaultComponentColor, Color.BLACK, 16, 16));
			
			defaultConnectorColor = BrickMappingLogic.DEFAULT_CONNECTOR_COLOR;
			if(preferences.keyExists(IPreferences.USER_SPACE, 
			"/archstudio/comp/archipelago/types", "defaultConnectorColor")){
				int val = preferences.getIntValue(IPreferences.USER_SPACE, 
				"/archstudio/comp/archipelago/types", "defaultConnectorColor", -1);
				defaultConnectorColor = new Color(val);
			}
			bDefaultConnectorColor.setIcon(WidgetUtils.getColorIcon(defaultConnectorColor, Color.BLACK, 16, 16));
		}
		
		public void storeCurrentPreferences(){
			IPreferences preferences = ArchStructurePreferencePanel.this.getPreferences();
			if(preferences == null){
				return;
			}
			
			int comp = defaultComponentColor.getRGB();
			int conn = defaultConnectorColor.getRGB();
			
			preferences.setValue(IPreferences.USER_SPACE, "/archstudio/comp/archipelago/types", "defaultComponentColor", "" + comp);
			preferences.setValue(IPreferences.USER_SPACE, "/archstudio/comp/archipelago/types", "defaultConnectorColor", "" + conn);
		}
		
		public void doDefaultComponentColor(){
			Color c = ColorPickerDialog.showDialog(
				WidgetUtils.getAncestorDialog(this), "Choose Default Component Color", 
				ColorSchemes.ALL_COLOR_SCHEMES, defaultComponentColor);
			if(c != null){
				defaultComponentColor = c;
				bDefaultComponentColor.setIcon(WidgetUtils.getColorIcon(defaultComponentColor, 
					Color.BLACK, 16, 16));
			}
		}
		
		public void doDefaultConnectorColor(){
			Color c = ColorPickerDialog.showDialog(
				WidgetUtils.getAncestorDialog(this), "Choose Default Connector Color", 
				ColorSchemes.ALL_COLOR_SCHEMES, defaultConnectorColor);
			if(c != null){
				defaultConnectorColor = c;
				bDefaultConnectorColor.setIcon(WidgetUtils.getColorIcon(defaultConnectorColor, 
					Color.BLACK, 16, 16));
			}
		}
		
		public void doRestoreToDefaultColors(){
			defaultComponentColor = BrickMappingLogic.DEFAULT_COMPONENT_COLOR;
			bDefaultComponentColor.setIcon(WidgetUtils.getColorIcon(defaultComponentColor, 
				Color.BLACK, 16, 16));
			
			defaultConnectorColor = BrickMappingLogic.DEFAULT_CONNECTOR_COLOR;
			bDefaultConnectorColor.setIcon(WidgetUtils.getColorIcon(defaultConnectorColor, 
				Color.BLACK, 16, 16));
		}
		
		public void actionPerformed(ActionEvent e){
			if(e.getSource() == bDefaultComponentColor){
				doDefaultComponentColor();
			}
			else if(e.getSource() == bDefaultConnectorColor){
				doDefaultConnectorColor();
			}
			else if(e.getSource() == bRestoreToDefaultColors){
				doRestoreToDefaultColors();
			}
		}		
	}
}

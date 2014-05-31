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

public class ArchTypesPreferencePanel extends PreferencePanel {

	protected ArchTypesPreferenceJPanel guiPanel = new ArchTypesPreferenceJPanel();

	public ArchTypesPreferencePanel(){
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

	class ArchTypesPreferenceJPanel extends JPanel implements ActionListener{
		protected JButton bDefaultComponentTypeColor;
		protected Color defaultComponentTypeColor = TypeMappingLogic.DEFAULT_COMPONENT_TYPE_COLOR;
		
		protected JButton bDefaultConnectorTypeColor;
		protected Color defaultConnectorTypeColor = TypeMappingLogic.DEFAULT_CONNECTOR_TYPE_COLOR;
		
		protected JButton bRestoreToDefaultColors;
		
		public ArchTypesPreferenceJPanel(){
			super();
			
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
			
			JPanel allPanel = new JPanel();
			allPanel.setLayout(new BoxLayout(allPanel, BoxLayout.Y_AXIS));
			
			JLabel lInstructions = new JLabel(
			"<html>" +
			"<div style=\"font-weight: normal\">" +
			"Here, you can set defaults dealing with architecture types. " +
			"</div>" +
			"</html>"
			);
			
			JPanel instructionsWLPanel = new JPanelWL(lInstructions, mainPanel){
				public Insets getInsets(){
					return new Insets(3, 3, 3, 3);
				}
			};
			
			allPanel.add(instructionsWLPanel);
			
			bDefaultComponentTypeColor = new JButton("Set Default Component Type Color...");
			bDefaultComponentTypeColor.addActionListener(this);
			
			bDefaultConnectorTypeColor = new JButton("Set Default Connector Type Color...");
			bDefaultConnectorTypeColor.addActionListener(this);
			
			JPanel controlsPanel = new JPanel();
			controlsPanel.setLayout(new GridLayout(2, 1));
			
			controlsPanel.add(bDefaultComponentTypeColor);
			controlsPanel.add(bDefaultConnectorTypeColor);
			
			bRestoreToDefaultColors = new JButton("Restore to Product Defaults");
			bRestoreToDefaultColors.addActionListener(this);
			
			allPanel.add(new JPanelIS(new JPanelUL(controlsPanel), 5));
			allPanel.add(Box.createVerticalStrut(5));
			allPanel.add(new JPanelUL(bRestoreToDefaultColors, "North", "East"));

			allPanel.setBorder(BorderFactory.createTitledBorder("Types Defaults"));
			
			mainPanel.add(new JPanelUL(allPanel));
			
			this.setLayout(new BorderLayout());
			this.add("Center", mainPanel);	
		}
		
		public void loadCurrentPreferences(){
			IPreferences preferences = ArchTypesPreferencePanel.this.getPreferences();
			if(preferences == null){
				return;
			}
			
			defaultComponentTypeColor = TypeMappingLogic.DEFAULT_COMPONENT_TYPE_COLOR;
			if(preferences.keyExists(IPreferences.USER_SPACE, 
			"/archstudio/comp/archipelago/types", "defaultComponentTypeColor")){
				int val = preferences.getIntValue(IPreferences.USER_SPACE, 
				"/archstudio/comp/archipelago/types", "defaultComponentTypeColor", -1);
				defaultComponentTypeColor = new Color(val);
			}
			bDefaultComponentTypeColor.setIcon(WidgetUtils.getColorIcon(defaultComponentTypeColor, Color.BLACK, 16, 16));
			
			defaultConnectorTypeColor = TypeMappingLogic.DEFAULT_CONNECTOR_TYPE_COLOR;
			if(preferences.keyExists(IPreferences.USER_SPACE, 
			"/archstudio/comp/archipelago/types", "defaultConnectorTypeColor")){
				int val = preferences.getIntValue(IPreferences.USER_SPACE, 
				"/archstudio/comp/archipelago/types", "defaultConnectorTypeColor", -1);
				defaultConnectorTypeColor = new Color(val);
			}
			bDefaultConnectorTypeColor.setIcon(WidgetUtils.getColorIcon(defaultConnectorTypeColor, Color.BLACK, 16, 16));
		}
		
		public void storeCurrentPreferences(){
			IPreferences preferences = ArchTypesPreferencePanel.this.getPreferences();
			if(preferences == null){
				return;
			}
			
			int comp = defaultComponentTypeColor.getRGB();
			int conn = defaultConnectorTypeColor.getRGB();
			
			preferences.setValue(IPreferences.USER_SPACE, "/archstudio/comp/archipelago/types", "defaultComponentTypeColor", "" + comp);
			preferences.setValue(IPreferences.USER_SPACE, "/archstudio/comp/archipelago/types", "defaultConnectorTypeColor", "" + conn);
		}
		
		public void doDefaultComponentTypeColor(){
			Color c = ColorPickerDialog.showDialog(
				WidgetUtils.getAncestorDialog(this), "Choose Default Component Type Color", 
				ColorSchemes.ALL_COLOR_SCHEMES, defaultComponentTypeColor);
			if(c != null){
				defaultComponentTypeColor = c;
				bDefaultComponentTypeColor.setIcon(WidgetUtils.getColorIcon(defaultComponentTypeColor, 
					Color.BLACK, 16, 16));
			}
		}
		
		public void doDefaultConnectorTypeColor(){
			Color c = ColorPickerDialog.showDialog(
				WidgetUtils.getAncestorDialog(this), "Choose Default Connector Type Color", 
				ColorSchemes.ALL_COLOR_SCHEMES, defaultConnectorTypeColor);
			if(c != null){
				defaultConnectorTypeColor = c;
				bDefaultConnectorTypeColor.setIcon(WidgetUtils.getColorIcon(defaultConnectorTypeColor, 
					Color.BLACK, 16, 16));
			}
		}
		
		public void doRestoreToDefaultColors(){
			defaultComponentTypeColor = TypeMappingLogic.DEFAULT_COMPONENT_TYPE_COLOR;
			bDefaultComponentTypeColor.setIcon(WidgetUtils.getColorIcon(defaultComponentTypeColor, 
				Color.BLACK, 16, 16));
			
			defaultConnectorTypeColor = TypeMappingLogic.DEFAULT_CONNECTOR_TYPE_COLOR;
			bDefaultConnectorTypeColor.setIcon(WidgetUtils.getColorIcon(defaultConnectorTypeColor, 
				Color.BLACK, 16, 16));
		}
		
		public void actionPerformed(ActionEvent e){
			if(e.getSource() == bDefaultComponentTypeColor){
				doDefaultComponentTypeColor();
			}
			else if(e.getSource() == bDefaultConnectorTypeColor){
				doDefaultConnectorTypeColor();
			}
			else if(e.getSource() == bRestoreToDefaultColors){
				doRestoreToDefaultColors();
			}
		}		
	}
}

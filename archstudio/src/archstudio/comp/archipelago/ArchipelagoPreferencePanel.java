package archstudio.comp.archipelago;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import edu.uci.ics.bna.BNAUtils;
import edu.uci.ics.widgets.JPanelIS;
import edu.uci.ics.widgets.JPanelUL;
import edu.uci.ics.widgets.JPanelWL;
import edu.uci.ics.widgets.WidgetUtils;

import archstudio.comp.preferences.IPreferences;
import archstudio.preferences.PreferencePanel;

public class ArchipelagoPreferencePanel extends PreferencePanel {

	protected ArchipelagoPreferenceJPanel guiPanel = new ArchipelagoPreferenceJPanel();

	public ArchipelagoPreferencePanel(){
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

	class ArchipelagoPreferenceJPanel extends JPanel implements ActionListener{
		
		protected JCheckBox cbAntialiasText;
		protected JCheckBox cbAntialiasGraphics;
		protected JCheckBox cbGradientGraphics;
		
		protected JLabel lDefaultFont;
		protected Font fDefaultFont;
		protected JButton bSelectDefaultFont;
		
		public ArchipelagoPreferenceJPanel(){
			super();
			
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
			
			JPanel allPanel = new JPanel();
			allPanel.setLayout(new BoxLayout(allPanel, BoxLayout.Y_AXIS));
			
			JLabel lInstructions = new JLabel(
			"<html>" +
			"<div style=\"font-weight: normal\">" +
			"Antialiasing eases the edges of graphical or textual objects, making " +
			"small text more readable and graphics appear smoother.  Gradients give " +
			"a three-dimensional look to various elements.  These options improve " +
			"graphics quality, but also " +
			"use additional processing power and can slow down graphical rendering." +
			"</div>" +
			"</html>"
			);
			
			JPanel instructionsWLPanel = new JPanelWL(lInstructions, mainPanel){
				public Insets getInsets(){
					return new Insets(3, 3, 0, 3);
				}
			};
			
			allPanel.add(instructionsWLPanel);
			
			cbAntialiasText = new JCheckBox("Antialias Text");
			cbAntialiasGraphics = new JCheckBox("Antialias Graphics");
			cbGradientGraphics = new JCheckBox("Use Gradient Fills");
			
			JPanel controlsPanel = new JPanel();
			controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
			
			controlsPanel.add(cbAntialiasText);
			controlsPanel.add(cbAntialiasGraphics);
			controlsPanel.add(cbGradientGraphics);
			
			allPanel.add(new JPanelIS(new JPanelUL(controlsPanel), 5));
			allPanel.setBorder(BorderFactory.createTitledBorder("Quality Settings"));
			
			mainPanel.add(new JPanelUL(allPanel));
			
			JPanel allFontPanel = new JPanel();
			allFontPanel.setLayout(new BoxLayout(allFontPanel, BoxLayout.Y_AXIS));
			
			JLabel lFontInstructions = new JLabel(
				"<html>" +
				"<div style=\"font-weight: normal\">" +
				"You can set a default font that will be used for rendering " +
				"labels in Archipelago." +
				"</div>" +
				"</html>"
				);
			
			JPanel fontInstructionsWLPanel = new JPanelWL(lFontInstructions, mainPanel){
				public Insets getInsets(){
					return new Insets(3, 3, 0, 3);
				}
			};
			
			allFontPanel.add(fontInstructionsWLPanel);

			JPanel fontControlsPanel = new JPanel();
			fontControlsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			
			lDefaultFont = new JLabel("[not set]");
			
			bSelectDefaultFont = new JButton("Select...");
			bSelectDefaultFont.addActionListener(this);
			
			fontControlsPanel.add(lDefaultFont);
			fontControlsPanel.add(bSelectDefaultFont);
			
			allFontPanel.add(new JPanelIS(new JPanelUL(fontControlsPanel), 5));
			allFontPanel.setBorder(BorderFactory.createTitledBorder("Default Font Settings"));

			mainPanel.add(new JPanelUL(allFontPanel));
			
			this.setLayout(new BorderLayout());
			this.add("Center", mainPanel);	
		}
		
		public void actionPerformed(ActionEvent evt){
			if(evt.getSource() == bSelectDefaultFont){
				Font f = WidgetUtils.displayFontChooserDialog(this, "Select Default Font", fDefaultFont);
				if(f != null){
					updateSelectedFont(f);
				}
			}
		}
		
		public void updateSelectedFont(Font f){
			StringBuffer sb = new StringBuffer();
			sb.append(f.getName());
			sb.append(", ");
			sb.append(f.getSize() + "pt");
			boolean bold = (f.getStyle() & Font.BOLD) != 0;
			boolean italic = (f.getStyle() & Font.ITALIC) != 0;
			
			if((!bold) && (!italic)){
				sb.append(", Plain");
			}
			else if(bold && (!italic)){
				sb.append(", Bold");
			}
			else if((!bold) && italic){
				sb.append(", Italic");
			}
			else{
				sb.append(", Bold Italic");
			}
			lDefaultFont.setText(sb.toString());
			this.fDefaultFont = f;
			WidgetUtils.validateAndRepaintInAWTThread(this);
		}
		
		public void loadCurrentPreferences(){
			IPreferences preferences = ArchipelagoPreferencePanel.this.getPreferences();
			if(preferences == null){
				return;
			}
			
			if(preferences.keyExists(IPreferences.USER_SPACE, 
			"/archstudio/comp/archipelago", "antialiasText")){
				String val = preferences.getStringValue(IPreferences.USER_SPACE, 
				"/archstudio/comp/archipelago", "antialiasText", null);
				if((val != null) && (val.equals("true"))){
					cbAntialiasText.setSelected(true);
				}
				else{
					cbAntialiasText.setSelected(false);
				}
			}
			else{
				cbAntialiasText.setSelected(false);
			}
			
			if(preferences.keyExists(IPreferences.USER_SPACE, 
			"/archstudio/comp/archipelago", "antialiasGraphics")){
				String val = preferences.getStringValue(IPreferences.USER_SPACE, 
				"/archstudio/comp/archipelago", "antialiasGraphics", null);
				if((val != null) && (val.equals("true"))){
					cbAntialiasGraphics.setSelected(true);
				}
				else{
					cbAntialiasGraphics.setSelected(false);
				}
			}
			else{
				cbAntialiasGraphics.setSelected(false);
			}

			if(preferences.keyExists(IPreferences.USER_SPACE, 
			"/archstudio/comp/archipelago", "gradientGraphics")){
				String val = preferences.getStringValue(IPreferences.USER_SPACE, 
				"/archstudio/comp/archipelago", "gradientGraphics", null);
				if((val != null) && (val.equals("true"))){
					cbGradientGraphics.setSelected(true);
				}
				else{
					cbGradientGraphics.setSelected(false);
				}
			}
			else{
				cbGradientGraphics.setSelected(false);
			}
			
			String fontName = BNAUtils.DEFAULT_FONT.getName();
			int fontSize = BNAUtils.DEFAULT_FONT.getSize();
			int fontStyle = BNAUtils.DEFAULT_FONT.getStyle();
			
			if(preferences.keyExists(IPreferences.USER_SPACE, 
			"/archstudio/comp/archipelago", "defaultFontName")){
				String val = preferences.getStringValue(IPreferences.USER_SPACE, 
				"/archstudio/comp/archipelago", "defaultFontName", null);
				if(val != null){
					fontName = val;
				}
			}
			
			if(preferences.keyExists(IPreferences.USER_SPACE, 
			"/archstudio/comp/archipelago", "defaultFontSize")){
				String val = preferences.getStringValue(IPreferences.USER_SPACE, 
				"/archstudio/comp/archipelago", "defaultFontSize", null);
				if(val != null){
					try{
						fontSize = Integer.parseInt(val);
					}
					catch(NumberFormatException nfe){}
				}
			}

			if(preferences.keyExists(IPreferences.USER_SPACE, 
			"/archstudio/comp/archipelago", "defaultFontStyle")){
				String val = preferences.getStringValue(IPreferences.USER_SPACE, 
				"/archstudio/comp/archipelago", "defaultFontStyle", null);
				if(val != null){
					try{
						fontStyle = Integer.parseInt(val);
					}
					catch(NumberFormatException nfe){}
				}
			}
			
			Font f = new Font(fontName, fontStyle, fontSize);
			updateSelectedFont(f);
		}
		
		public void storeCurrentPreferences(){
			IPreferences preferences = ArchipelagoPreferencePanel.this.getPreferences();
			if(preferences == null){
				return;
			}
			
			boolean aaText = cbAntialiasText.isSelected();
			boolean aaGraphics = cbAntialiasGraphics.isSelected();
			boolean grGraphics = cbGradientGraphics.isSelected();
			
			String fontName = fDefaultFont.getName();
			int fontSize = fDefaultFont.getSize();
			int fontStyle = fDefaultFont.getStyle();
			
			preferences.setValue(IPreferences.USER_SPACE, "/archstudio/comp/archipelago", "antialiasText", "" + aaText);
			preferences.setValue(IPreferences.USER_SPACE, "/archstudio/comp/archipelago", "antialiasGraphics", "" + aaGraphics);
			preferences.setValue(IPreferences.USER_SPACE, "/archstudio/comp/archipelago", "gradientGraphics", "" + grGraphics);
			
			preferences.setValue(IPreferences.USER_SPACE, "/archstudio/comp/archipelago", "defaultFontName", "" + fontName);
			preferences.setValue(IPreferences.USER_SPACE, "/archstudio/comp/archipelago", "defaultFontSize", "" + fontSize);
			preferences.setValue(IPreferences.USER_SPACE, "/archstudio/comp/archipelago", "defaultFontStyle", "" + fontStyle);
		}
	}
}

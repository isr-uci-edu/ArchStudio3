package archstudio.comp.tron.tools.schematron;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.uci.ics.widgets.JPanelIS;
import edu.uci.ics.widgets.JPanelUL;
import edu.uci.ics.widgets.JPanelWL;

import archstudio.comp.preferences.IPreferences;
import archstudio.preferences.PreferencePanel;

public class SchematronPreferencePanel extends PreferencePanel {

	protected SchematronPreferenceJPanel guiPanel = new SchematronPreferenceJPanel();

	public SchematronPreferencePanel(){
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

	class SchematronPreferenceJPanel extends JPanel implements ActionListener, ListSelectionListener{
		
		protected DefaultListModel listModel;
		protected JList list;
		protected JButton bAddFile;
		protected JButton bAddURL;
		protected JButton bRemove;
		
		public SchematronPreferenceJPanel(){
			super();
			
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
			
			JPanel locationsPanel = new JPanel();
			locationsPanel.setLayout(new BoxLayout(locationsPanel, BoxLayout.Y_AXIS));
			
			JLabel lInstructions = new JLabel(
				"<html>" +
				"<div style=\"font-weight: normal\">" +
				"The Schematron Analysis Tool component is part of the " +
				"ArchStudio 3 Tron Analysis Framework.  This component " +
				"allows ArchStudio 3 to validate architecture descriptions " +
				"against Schematron XML-based constraint specifications. " +
				"This component will load certain default tests from the " +
				"ArchStudio 3 <code>JAR</code> file as resources automatically, " +
				"but you can also specify locations where Schematron will look " +
				"for additional tests here." +
				"</div>" +
				"</html>");
			
			JPanel instructionsWLPanel = new JPanelWL(lInstructions, locationsPanel){
				public Insets getInsets(){
					return new Insets(3, 3, 3, 3);
				}
			};
			
			locationsPanel.add(instructionsWLPanel);
			
			listModel = new DefaultListModel();
			list = new JList(listModel);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.addListSelectionListener(this);
			
			bAddFile = new JButton("Add Directory...");
			bAddFile.addActionListener(this);
			
			bAddURL = new JButton("Add URL...");
			bAddURL.addActionListener(this);
			
			bRemove = new JButton("Remove Selected");
			bRemove.addActionListener(this);
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			
			buttonPanel.add(bAddFile);
			buttonPanel.add(bAddURL);
			buttonPanel.add(bRemove);
			
			//JPanel listPanel = new JPanel();
			//listPanel.setLayout(new BorderLayout());
			//listPanel.add("Center", list);

			JScrollPane listPanel = new JScrollPane(list);
			locationsPanel.add(new JPanelIS(listPanel, 5));
			
			locationsPanel.add(buttonPanel);
			locationsPanel.setBorder(BorderFactory.createTitledBorder("Constraint Locations"));
			
			mainPanel.add(/*new JPanelUL(*/locationsPanel/*)*/);
			
			this.setLayout(new BorderLayout());
			this.add("Center", mainPanel);	
			checkSelections();
		}
		
		public void loadCurrentPreferences(){
			IPreferences preferences = SchematronPreferencePanel.this.getPreferences();
			if(preferences == null){
				return;
			}
			
			listModel.clear();
			
			int i = 0;
			//boolean oneFound = false;
			while(true){
				if(preferences.keyExists(IPreferences.USER_SPACE, 
					"/archstudio/comp/tron/schematron", "testFileURL_" + i)){
					
					String newURI = preferences.getStringValue(IPreferences.USER_SPACE, 
						"/archstudio/comp/tron/schematron", "testFileURL_" + i, null);
					listModel.addElement(newURI);
					//oneFound = true;
				}
				else{
					break;
				}
				i++;
			}
			//if(!oneFound){
			//	listModel.addElement("[No additional locations specified.]");
			//}
			checkSelections();
		}
		
		public void storeCurrentPreferences(){
			IPreferences preferences = SchematronPreferencePanel.this.getPreferences();
			if(preferences == null){
				return;
			}
			String[] urls = new String[listModel.getSize()];
			Object[] listContents = listModel.toArray();
			for(int i = 0; i < listContents.length; i++){
				urls[i] = (String)listContents[i];
			}
			
			//Remove old keys
			int i = 0;
			while(true){
				if(preferences.keyExists(IPreferences.USER_SPACE, 
					"/archstudio/comp/tron/schematron", "testFileURL_" + i)){
					preferences.removeKey(IPreferences.USER_SPACE,
						"/archstudio/comp/tron/schematron", "testFileURL_" + i);
				}
				else{
					break;
				}
				i++;
			}
			
			for(int j = 0; j < urls.length; j++){
				if(urls[j] != null){
					preferences.setValue(IPreferences.USER_SPACE, "/archstudio/comp/tron/schematron", "testFileURL_" + j, urls[j]);
				}
			}
		}
		
		public void doBrowse(){
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setDialogTitle("Locate Directory");
			
			int returnVal = chooser.showOpenDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION){
				File dir = chooser.getSelectedFile();
				try{
					String url = dir.toURL().toString();
					listModel.addElement(url);
				}
				catch(MalformedURLException mue){
					JOptionPane.showMessageDialog(this, 
						"Cannot convert directory to file:/ URL.", "Selection Failed", JOptionPane.ERROR_MESSAGE); 
					return;
				}
			}
		}
		
		public void doGetURL(){
			try{
				String inputValue = JOptionPane.showInputDialog("Enter a URL"); 
				if(inputValue != null){
					inputValue = inputValue.trim();
					if(inputValue.startsWith("http://")){
						URL url = new URL(inputValue);
					}
					else if(inputValue.startsWith("file:")){
						URL url = new URL(inputValue);
					}
					else if(inputValue.startsWith("res:")){
					}
					else{
						JOptionPane.showMessageDialog(this, 
							"Invalid URL", "Invalid URL", JOptionPane.ERROR_MESSAGE); 
						return;
					}
					listModel.addElement(inputValue);
				}
			}
			catch(MalformedURLException mue){
				JOptionPane.showMessageDialog(this, 
					"Invalid URL", "Invalid URL", JOptionPane.ERROR_MESSAGE); 
				return;
			}
		}

		public void actionPerformed(ActionEvent evt){
			if(evt.getSource() == bAddFile){
				doBrowse();
			}
			else if(evt.getSource() == bAddURL){
				doGetURL();
			}
			else if(evt.getSource() == bRemove){
				int selectedIndex = list.getSelectedIndex();
				if(selectedIndex != -1){
					listModel.remove(selectedIndex);
				}
			}
		}

		public void checkSelections(){
			int selectedIndex = list.getSelectedIndex();
			if(selectedIndex == -1){
				bRemove.setEnabled(false);
			}
			else{
				bRemove.setEnabled(true);
			}
		}
		
		public void valueChanged(ListSelectionEvent e){
			checkSelections();
		}
	}
}

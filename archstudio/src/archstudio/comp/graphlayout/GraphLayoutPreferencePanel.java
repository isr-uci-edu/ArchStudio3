package archstudio.comp.graphlayout;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import edu.uci.ics.widgets.JPanelEWL;
import edu.uci.ics.widgets.JPanelUL;
import edu.uci.ics.widgets.JPanelWL;

import archstudio.comp.preferences.IPreferences;
import archstudio.preferences.PreferencePanel;

public class GraphLayoutPreferencePanel extends PreferencePanel {

	protected GraphLayoutPreferenceJPanel guiPanel = new GraphLayoutPreferenceJPanel();

	public GraphLayoutPreferencePanel(){
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

	class GraphLayoutPreferenceJPanel extends JPanel implements ActionListener{
		
		protected JTextField tfDotLocation;
		protected JButton bBrowse;
		protected JButton bCheckDirectory;
		
		public GraphLayoutPreferenceJPanel(){
			super();
			
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
			
			JPanel dotPanel = new JPanel();
			dotPanel.setLayout(new BoxLayout(dotPanel, BoxLayout.Y_AXIS));
			
			JLabel lInstructions = new JLabel(
			"<html>" +
			"<div style=\"font-weight: normal\">" +
			"The Graph Layout component allows ArchStudio 3 to lay out " +
			"architecture graphs in a sensible way.  To do this, it uses an " +
			"external tool called <code>dot</code>, available in the GraphViz " +
			"package from AT&T.  It's available for many platforms, for free at " +			"http://www.research.att.com/sw/tools/graphviz/" +
			"<p>To take advantage of its capabilities, please download and install " +
			"GraphViz.  Then, return here and indicate the installation directory. " +
			"In general, this is the directory called <code>GraphViz/</code> " +
			"that contains directories <code>bin/</code>, <code>doc/</code>, etc." +
			"</div>" +
			"</html>"
			);
			
			JPanel instructionsWLPanel = new JPanelWL(lInstructions, dotPanel){
				public Insets getInsets(){
					return new Insets(3, 3, 3, 3);
				}
			};
			
			dotPanel.add(instructionsWLPanel);
			
			tfDotLocation = new JTextField(20);
			tfDotLocation.setEditable(false);
			
			bBrowse = new JButton("Browse...");
			bBrowse.addActionListener(this);
			
			bCheckDirectory = new JButton("Check Selection");
			bCheckDirectory.addActionListener(this);
			
			JPanel dotInputPanel = new JPanel();
			dotInputPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			
			dotInputPanel.add(new JLabel("Directory:"));
			dotInputPanel.add(tfDotLocation);
			dotInputPanel.add(bBrowse);
			
			JPanel dotCheckPanel = new JPanel();
			dotCheckPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			dotCheckPanel.add(bCheckDirectory);

			dotPanel.add(dotInputPanel);
			dotPanel.add(dotCheckPanel);
			dotPanel.setBorder(BorderFactory.createTitledBorder("GraphViz Location"));
			
			mainPanel.add(new JPanelUL(dotPanel));
			
			this.setLayout(new BorderLayout());
			this.add("Center", mainPanel);	
		}
		
		public void loadCurrentPreferences(){
			IPreferences preferences = GraphLayoutPreferencePanel.this.getPreferences();
			if(preferences == null){
				return;
			}
			
			if(preferences.keyExists(IPreferences.SYSTEM_SPACE, 
			"/archstudio/comp/graphlayout", "graphvizpath")){
				String graphvizPath = preferences.getStringValue(IPreferences.SYSTEM_SPACE, 
				"/archstudio/comp/graphlayout", "graphvizpath", null);
				tfDotLocation.setText(graphvizPath);
			}
			else{
				tfDotLocation.setText("");
			}
		}
		
		public void storeCurrentPreferences(){
			IPreferences preferences = GraphLayoutPreferencePanel.this.getPreferences();
			if(preferences == null){
				return;
			}
			
			String dirName = tfDotLocation.getText();
			
			preferences.setValue(IPreferences.SYSTEM_SPACE, "/archstudio/comp/graphlayout", "graphvizpath", dirName);
		}
		
		public void doBrowse(){
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setDialogTitle("Locate GraphViz Directory");
			
			int returnVal = chooser.showOpenDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION){
				File dir = chooser.getSelectedFile();
				
				String fn = null;
				try{
					fn = dir.getCanonicalPath();
				}
				catch(IOException e){
					fn = dir.getAbsolutePath();
				}
				
				tfDotLocation.setText(fn);
			}
		}
		
		public void doCheckDirectory(){
			String fn = tfDotLocation.getText();
			if((fn == null) || (fn.equals(""))){
				JOptionPane.showMessageDialog(this, 
					"No directory selected.", "Check Failed", JOptionPane.ERROR_MESSAGE); 
				return;
			}
			
			File dir = new File(fn);
			if(!dir.exists()){
				JOptionPane.showMessageDialog(this, 
					"Directory does not exist.", "Check Failed", JOptionPane.ERROR_MESSAGE); 
				return;
			}

			if(!dir.canRead()){
				JOptionPane.showMessageDialog(this, 
					"Directory exists but can't be read.", "Check Failed", JOptionPane.ERROR_MESSAGE); 
				return;
			}
			
			File binDir = new File(dir, "bin");
			if(!binDir.exists()){
				JOptionPane.showMessageDialog(this, 
					"Directory does not contain bin/ directory.", "Check Failed", JOptionPane.WARNING_MESSAGE); 
				return;
			}

			if(!binDir.canRead()){
				JOptionPane.showMessageDialog(this, 
					"Can't read bin/ directory.", "Check Failed", JOptionPane.WARNING_MESSAGE); 
				return;
			}
			
			File dotExe = new File(binDir, "dot.exe");
			File dot = new File(binDir, "dot");
			
			if((!dotExe.exists()) && (!dot.exists())){
				JOptionPane.showMessageDialog(this, 
					"Directory doesn't seem to contain dot executable.", "Check Failed", JOptionPane.WARNING_MESSAGE); 
				return;
			}
			
			JOptionPane.showMessageDialog(this, 
				"Directory looks OK.", "Check Passed", JOptionPane.INFORMATION_MESSAGE); 
		}
		
		public void actionPerformed(ActionEvent evt){
			if(evt.getSource() == bBrowse){
				doBrowse();
			}
			else if(evt.getSource() == bCheckDirectory){
				doCheckDirectory();
			}
		}
	}
}

package archstudio;

import edu.uci.ics.nativeutils.SystemUtils;
import edu.uci.ics.widgets.*;
import edu.uci.isr.registry.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;


public class Configurator extends JFrame{
	
	public static void main(String[] args){
		new Configurator();
	}
	
	public Configurator(){
		ConfiguratorWizard w = new ConfiguratorWizard();
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add("Center", w);
		archstudio.Branding.brandFrame(this);
		
		this.setSize(630, 350);
		this.setVisible(true);
		this.validate();
		this.repaint();
		WidgetUtils.centerInScreen(this);
	}
	
	public String getProductPathFromRegistry(String productName){
		try{
			RegistryNode regRoot = RegistryUtils.loadRegistry();
			if(regRoot == null){
				return null;
			}
			RegistryNode productNode = RegistryUtils.getChild(regRoot, "Install/Software/" + productName);
			if(productNode == null){
				return null;
			}
			if(!productNode.hasEntry("TargetDirectory")){
				return null;
			}
			RegistryEntry targetDirectoryEntry = productNode.getEntry("TargetDirectory");
			if(targetDirectoryEntry == null){
				return null;
			}
			String directory = targetDirectoryEntry.getValue();
			
			return directory;
		}
		catch(Exception e){
			return null;
		}
	}
	
	public String getLibraryPathFromRegistry(String productName){
		String directory = getProductPathFromRegistry("archstudio");
		if(directory != null){
			return directory + SystemUtils.fileSeparator + productName + 
				SystemUtils.fileSeparator + "lib" + SystemUtils.fileSeparator + productName + ".jar";
		}
		else{
			return null;
		}
	}
	
	class ConfiguratorWizard extends Wizard{
		
		public ConfiguratorWizard(){
			super();
			super.setEnclosingWindow(Configurator.this);
			super.setMarquee(new JPanelUL(new JPanelIS(new JLabel(WidgetUtils.getImageIcon("archstudio/res/configurator-logo.jpg")), 5)));
			super.setCards(
				new String[]{
					"ArchStudio 3 Configurator - Welcome", 
					"ArchStudio 3 Configurator - Locate Java Virtual Machine",
					"ArchStudio 3 Configurator - Locate Data Binding Library",
					"ArchStudio 3 Configurator - Locate ArchStudio Library",
					"ArchStudio 3 Configurator - Locate ArchStudio Architecture Description",
					"ArchStudio 3 Configurator - Write Script"
				}, 
				new JComponent[]{
					new WelcomePanel(), 
					new JVMPanel(),
					new XArchLibsPanel(),
					new ArchstudioLibPanel(),
					new ArchstudioDescriptionPanel(),
					new ScriptReviewPanel()
					}
				);
		}
		
		protected boolean shouldNextButtonBeEnabled(JComponent comp){
			if(comp instanceof WelcomePanel){
				return true;
			}
			else if(comp instanceof JVMPanel){
				return ((JVMPanel)comp).shouldNextButtonBeEnabled();
			}
			else if(comp instanceof XArchLibsPanel){
				return ((XArchLibsPanel)comp).shouldNextButtonBeEnabled();
			}
			else if(comp instanceof ArchstudioLibPanel){
				return ((ArchstudioLibPanel)comp).shouldNextButtonBeEnabled();
			}
			else if(comp instanceof ArchstudioDescriptionPanel){
				return ((ArchstudioDescriptionPanel)comp).shouldNextButtonBeEnabled();
			}					
			else{
				return true;
			}
		}
		
		class WelcomePanel extends JPanel{
			static final String welcomeText =
				"<html>" +
				"<font size=+1 color=\"#000088\"><b>Welcome to the ArchStudio 3 Configurator</b></font>" +
				"<p>&nbsp;<p>The ArchStudio 3 Configurator Wizard helps users to locate key system files " +
				"required to run ArchStudio 3 and assists them in generating a platform-specific " +
				"script (a shellscript for UNIX or a batch file for Windows) that can be used to " +
				"launch ArchStudio 3. The steps are:" +
				"<ul>" +
				"<li>Choose the Java Virtual Machine to run ArchStudio 3</li>" +
				"<li>Locate the Data Binding Library</li>" +
				"<li>Locate the ArchStudio 3 Library</li>" +
				"<li>Locate the ArchStudio 3 Architecture Description</li>" +
				"<li>Generate the Script</li>" +
				"</ul>" + 
				"<p>Click 'Next' to continue." +
				"</html>";
			
			public WelcomePanel(){
				JLabel mainLabel = new JLabel(welcomeText);
				this.setLayout(new BorderLayout());
				this.add("North", new JPanelIS(mainLabel, 5));
			}
		}
		
		class JVMPanel extends JPanel implements ActionListener{
			static final String welcomeText =
				"<html>" +
				"<font size=+1>Choose Java Interpreter</font>" +
				"<p>&nbsp;" +
				"<p>ArchStudio 3 requires a Java 2, version 1.4 (or better) virtual machine to run. " +
				"Please choose a Java interpreter to use for executing ArchStudio 3." +
				"</html>";
			
			DefaultComboBoxModel cbModel = new DefaultComboBoxModel();
			JComboBox cb = new JComboBox(cbModel);
			JButton checkJVMButton = new JButton("Check Selected JVM...");
			JTextArea checkTextArea = new JTextArea(5, 30);
			boolean nextButtonEnabled = false;
			
			public JVMPanel(){
				JLabel mainLabel = new JLabel(welcomeText);
				this.setLayout(new BorderLayout());
				this.add("North", new JPanelIS(mainLabel, 5));
				String[] jvms = SystemUtils.guessJVMs();
				
				if(jvms.length == 0){
					cbModel.addElement("[No JVMs Found - Select 'Other' to choose a JVM]");
				}
				else{
					for(int i = 0; i < jvms.length; i++){
						cbModel.addElement(jvms[i]);
					}
				}
				syncData();
				
				cbModel.addElement("Other...");
				
				JPanel p = new JPanel();
				p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
				
				p.add(new JPanelUL(new JLabel("Select a Java Virtual Machine:")));
				
				cb.setEditable(false);
				cb.addActionListener(this);
				p.add(new JPanelIS(new JPanelUL(cb), 5));
				
				checkJVMButton.addActionListener(this);
				p.add(new JPanelIS(checkJVMButton, 5));
				checkTextArea.setEditable(false);
				p.add(new JPanelIS(new JScrollPane(checkTextArea), 5));
				
				this.add("Center", new JPanelIS(new JPanelUL(p), 5));
				checkNextButton();
			}
			
			protected void syncData(){
				String selectedItem = (String)cb.getSelectedItem();
				setData("jvm", selectedItem);
			}
			
			protected void checkNextButton(){
				String s = (String)cb.getSelectedItem();
				if(s.equals("Other...")){
					nextButtonEnabled = false;
				}
				else if(s.equals("[No JVMs Found - Select 'Other' to choose a JVM]")){
					nextButtonEnabled = false;
				}
				else{
					File f = new File(s);
					if(f.exists()){
						nextButtonEnabled = true;
					}
				}
				checkButtons();
			}
			
			public void actionPerformed(ActionEvent e){
				if(e.getSource() == checkJVMButton){
					try{
						File f = new File((String)cb.getSelectedItem());
						if(!f.exists()){
							checkTextArea.setText("Selected file does not exist.");
							return;
						}
						if(!f.canRead()){
							checkTextArea.setText("Cannot read selected file.");
							return;
						}
						Object[] options = { "OK", "Cancel" };
						int result = JOptionPane.showConfirmDialog(Configurator.this, 
							"This will invoke the selected program. OK to continue?", "Warning", 
							JOptionPane.OK_CANCEL_OPTION);
						if(result == JOptionPane.OK_OPTION){
							String cmd = (String)cb.getSelectedItem() + " -version";
							String procOutput = SystemUtils.runAndCaptureProcess(cmd);
							System.out.println("procOutput = " + procOutput);
							String output = "";
							if(procOutput.indexOf("1.4") == -1){
								output += "Warning: JVM version does not contain '1.4'";
								output += System.getProperty("line.separator");
								output += "----------";
								output += System.getProperty("line.separator");
							}
							else{
								output += "Version contains '1.4'; looks OK";
								output += System.getProperty("line.separator");
								output += "----------";
								output += System.getProperty("line.separator");
							}
							output += procOutput;
							checkTextArea.setText(output);
							checkTextArea.setCaretPosition(0);
						}
					}
					catch(Exception ex){
						checkTextArea.setText(ex.toString());
					}
				}
				else if(e.getSource() == cb){
					checkTextArea.setText(" ");
					String s = (String)cb.getSelectedItem();
					if(s.equals("Other...")){
						JFileChooser chooser = new JFileChooser();
						int os = SystemUtils.guessOperatingSystem();
						if(os == SystemUtils.OS_WINDOWS){
							GenericFileFilter filter = new GenericFileFilter();
							filter.addExtension("exe");
							filter.addExtension("com");
							filter.addExtension("bat");
							filter.addExtension("cmd");
							filter.setDescription("Executable Files");
							chooser.setFileFilter(filter);
						}
						
						int returnVal = chooser.showOpenDialog(Configurator.this);
						if(returnVal == JFileChooser.APPROVE_OPTION){
							File f = chooser.getSelectedFile();
							String path = SystemUtils.getCanonicalPath(f);
							for(int i = 0; i < cbModel.getSize(); i++){
								String cbModelEntry = (String)cbModel.getElementAt(i);
								if(cbModelEntry.equals(path)){
									cbModel.setSelectedItem(cbModelEntry);
									return;
								}
							}
							cbModel.insertElementAt(path, 0);
							cbModel.setSelectedItem(path);
						}
					}
					syncData();
					checkNextButton();
				}	
			}
			
			public boolean shouldNextButtonBeEnabled(){
				return nextButtonEnabled;
			}
			
		}
		
		class XArchLibsPanel extends JPanel implements ActionListener{
			static final String welcomeText =
				"<html>" +
				"<font size=+1>Choose Data Binding Library</font>" +
				"<p>&nbsp;" +
				"<p>ArchStudio 3 requires a Data Binding Library to run.  Usually, this " +
				"comes in the form of the xArch/xADL 2.0 Data Binding Library." +
				"</html>";
			
			DefaultComboBoxModel cbModel = new DefaultComboBoxModel();
			JComboBox cb = new JComboBox(cbModel);
			JButton checkLibraryButton = new JButton("Check Selected Library...");
			JTextArea checkTextArea = new JTextArea(5, 30);
			boolean nextButtonEnabled = false;
			
			public XArchLibsPanel(){
				JLabel mainLabel = new JLabel(welcomeText);
				this.setLayout(new BorderLayout());
				this.add("North", new JPanelIS(mainLabel, 5));
				
				String libPath = getLibraryPathFromRegistry("xarchlibs");
				if(libPath != null){
					cbModel.addElement(libPath);
				}
				
				String[] libs = SystemUtils.guessLibLocations("xarchlibs.jar");
				
				if((libPath == null) && (libs.length == 0)){
					cbModel.addElement("[No Libraries Found - Select 'Other' to choose one.]");
				}
				else{
					for(int i = 0; i < libs.length; i++){
						cbModel.addElement(libs[i]);
					}
				}
				
				cbModel.addElement("Other...");
				
				JPanel p = new JPanel();
				p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
				
				p.add(new JPanelUL(new JLabel("Select a Data Binding Library:")));
				
				cb.setEditable(false);
				cb.addActionListener(this);
				p.add(new JPanelIS(new JPanelUL(cb), 5));
				
				checkLibraryButton.addActionListener(this);
				p.add(new JPanelIS(checkLibraryButton, 5));
				checkTextArea.setEditable(false);
				p.add(new JPanelIS(new JScrollPane(checkTextArea), 5));
				
				this.add("Center", new JPanelIS(new JPanelUL(p), 5));
				syncData();
				checkNextButton();
			}
			
			protected void syncData(){
				String selectedItem = (String)cb.getSelectedItem();
				setData("xarchlibs", selectedItem);
			}
			
			protected void checkNextButton(){
				String s = (String)cb.getSelectedItem();
				if(s.equals("Other...")){
					nextButtonEnabled = false;
				}
				else if(s.equals("[No Libraries Found - Select 'Other' to choose one.]")){
					nextButtonEnabled = false;
				}
				else{
					File f = new File(s);
					if(f.exists()){
						nextButtonEnabled = true;
					}
				}
				checkButtons();
			}
			
			public void actionPerformed(ActionEvent e){
				if(e.getSource() == checkLibraryButton){
					try{
						File f = new File((String)cb.getSelectedItem());
						if(!f.exists()){
							checkTextArea.setText("Selected file does not exist.");
							return;
						}
						if(!f.canRead()){
							checkTextArea.setText("Cannot read selected file.");
							return;
						}
						
						boolean found = false;
						try{
							JarFile jf = new JarFile(f);
							for(Enumeration en = jf.entries(); en.hasMoreElements(); ){
								ZipEntry ze = (ZipEntry)en.nextElement();
								String name = ze.getName();
								if(name.equals("edu/uci/isr/xarch/XArchUtils.class")){
									found = true;
									break;
								}
							}
							jf.close();
							if(!found){
								checkTextArea.setText("Warning: Library does not contain XArchUtils.class");
							}
							else{
								checkTextArea.setText("Library looks OK.");
							}
						}
						catch(Exception ex){
							ex.printStackTrace();
							checkTextArea.setText(ex.toString());
						}
						
						checkTextArea.setCaretPosition(0);
					}
					catch(Exception ex){
						checkTextArea.setText(ex.toString());
					}
				}
				else if(e.getSource() == cb){
					checkTextArea.setText(" ");
					String s = (String)cb.getSelectedItem();
					if(s.equals("Other...")){
						JFileChooser chooser = new JFileChooser();
						
						GenericFileFilter filter = new GenericFileFilter();
						filter.addExtension("jar");
						filter.addExtension("zip");
						filter.setDescription("Java Archives");
						chooser.setFileFilter(filter);
						
						int returnVal = chooser.showOpenDialog(Configurator.this);
						if(returnVal == JFileChooser.APPROVE_OPTION){
							File f = chooser.getSelectedFile();
							String path = SystemUtils.getCanonicalPath(f);
							for(int i = 0; i < cbModel.getSize(); i++){
								String cbModelEntry = (String)cbModel.getElementAt(i);
								if(cbModelEntry.equals(path)){
									cbModel.setSelectedItem(cbModelEntry);
									return;
								}
							}
							cbModel.removeElement("[No Libraries Found - Select 'Other' to choose one.]");
							cbModel.insertElementAt(path, 0);
							cbModel.setSelectedItem(path);
						}
					}
					
					syncData();
					checkNextButton();
				}	
			}
			
			public boolean shouldNextButtonBeEnabled(){
				return nextButtonEnabled;
			}
		}
		
		class ArchstudioLibPanel extends JPanel implements ActionListener{
			static final String welcomeText =
				"<html>" +
				"<font size=+1>Choose ArchStudio 3 Library</font>" +
				"<p>&nbsp;" +
				"<p>ArchStudio 3 requires the ArchStudio 3 Library to run." +
				"</html>";
			
			DefaultComboBoxModel cbModel = new DefaultComboBoxModel();
			JComboBox cb = new JComboBox(cbModel);
			JButton checkLibraryButton = new JButton("Check Selected Library...");
			JTextArea checkTextArea = new JTextArea(5, 30);
			boolean nextButtonEnabled = false;
			
			public ArchstudioLibPanel(){
				JLabel mainLabel = new JLabel(welcomeText);
				this.setLayout(new BorderLayout());
				this.add("North", new JPanelIS(mainLabel, 5));
				
				String libPath = getLibraryPathFromRegistry("archstudio");
				if(libPath != null){
					cbModel.addElement(libPath);
				}
				
				String[] libs = SystemUtils.guessLibLocations("archstudio.jar");
				
				if((libPath == null) && (libs.length == 0)){
					cbModel.addElement("[No Libraries Found - Select 'Other' to choose one.]");
				}
				else{
					for(int i = 0; i < libs.length; i++){
						cbModel.addElement(libs[i]);
					}
				}
				
				cbModel.addElement("Other...");
				
				JPanel p = new JPanel();
				p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
				
				p.add(new JPanelUL(new JLabel("Select an ArchStudio 3 Library:")));
				
				cb.setEditable(false);
				cb.addActionListener(this);
				p.add(new JPanelIS(new JPanelUL(cb), 5));
				
				checkLibraryButton.addActionListener(this);
				p.add(new JPanelIS(checkLibraryButton, 5));
				checkTextArea.setEditable(false);
				p.add(new JPanelIS(new JScrollPane(checkTextArea), 5));
				
				this.add("Center", new JPanelIS(new JPanelUL(p), 5));
				syncData();
				checkNextButton();
			}
			
			protected void syncData(){
				String selectedItem = (String)cb.getSelectedItem();
				setData("archstudio", selectedItem);
			}
			
			protected void checkNextButton(){
				String s = (String)cb.getSelectedItem();
				if(s.equals("Other...")){
					nextButtonEnabled = false;
				}
				else if(s.equals("[No Libraries Found - Select 'Other' to choose one.]")){
					nextButtonEnabled = false;
				}
				else{
					File f = new File(s);
					if(f.exists()){
						nextButtonEnabled = true;
					}
				}
				checkButtons();
			}
			
			public void actionPerformed(ActionEvent e){
				if(e.getSource() == checkLibraryButton){
					try{
						File f = new File((String)cb.getSelectedItem());
						if(!f.exists()){
							checkTextArea.setText("Selected file does not exist.");
							return;
						}
						if(!f.canRead()){
							checkTextArea.setText("Cannot read selected file.");
							return;
						}
						
						boolean found = false;
						try{
							JarFile jf = new JarFile(f);
							for(Enumeration en = jf.entries(); en.hasMoreElements(); ){
								ZipEntry ze = (ZipEntry)en.nextElement();
								String name = ze.getName();
								if(name.equals("archstudio/Bootstrap.class")){
									found = true;
									break;
								}
							}
							jf.close();
							if(!found){
								checkTextArea.setText("Warning: Library does not contain\nArchStudio 3 Bootstrap class.");
							}
							else{
								checkTextArea.setText("Library looks OK.");
							}
						}
						catch(Exception ex){
							ex.printStackTrace();
							checkTextArea.setText(ex.toString());
						}
						
						checkTextArea.setCaretPosition(0);
					}
					catch(Exception ex){
						checkTextArea.setText(ex.toString());
					}
				}
				else if(e.getSource() == cb){
					checkTextArea.setText(" ");
					String s = (String)cb.getSelectedItem();
					if(s.equals("Other...")){
						JFileChooser chooser = new JFileChooser();
						
						GenericFileFilter filter = new GenericFileFilter();
						filter.addExtension("jar");
						filter.addExtension("zip");
						filter.setDescription("Java Archives");
						chooser.setFileFilter(filter);
						
						int returnVal = chooser.showOpenDialog(Configurator.this);
						if(returnVal == JFileChooser.APPROVE_OPTION){
							File f = chooser.getSelectedFile();
							String path = SystemUtils.getCanonicalPath(f);
							for(int i = 0; i < cbModel.getSize(); i++){
								String cbModelEntry = (String)cbModel.getElementAt(i);
								if(cbModelEntry.equals(path)){
									cbModel.setSelectedItem(cbModelEntry);
									return;
								}
							}
							cbModel.removeElement("[No Libraries Found - Select 'Other' to choose one.]");
							cbModel.insertElementAt(path, 0);
							cbModel.setSelectedItem(path);
						}
					}
					
					syncData();
					checkNextButton();
				}	
			}
			
			public boolean shouldNextButtonBeEnabled(){
				return nextButtonEnabled;
			}
		}
		
		class ArchstudioDescriptionPanel extends JPanel implements ActionListener{
			static final String welcomeText =
				"<html>" +
				"<font size=+1>Choose ArchStudio 3 Architectural Description</font>" +
				"<p>&nbsp;" +
				"<p>ArchStudio 3 is bootstrapped from an architecture description. " +
				"Usually, this description is called <code>archstudio.xml</code> and " +
				"is located in the ArchStudio 3 <code>bin</code> directory." +
				"</html>";
			
			DefaultComboBoxModel cbModel = new DefaultComboBoxModel();
			JComboBox cb = new JComboBox(cbModel);
			JButton checkLibraryButton = new JButton("Check Selected Description...");
			JTextArea checkTextArea = new JTextArea(5, 30);
			boolean nextButtonEnabled = false;
			
			public ArchstudioDescriptionPanel(){
				JLabel mainLabel = new JLabel(welcomeText);
				this.setLayout(new BorderLayout());
				this.add("North", new JPanelIS(mainLabel, 5));
				
				String prodPath = getProductPathFromRegistry("archstudio");
				if(prodPath != null){
					String xmlPath = prodPath + SystemUtils.fileSeparator +
						"archstudio" + SystemUtils.fileSeparator +
						"bin" + SystemUtils.fileSeparator +
						"archstudio.xml";
					File f = new File(xmlPath);
					if(f.exists()){
						if(!f.isDirectory()){
							cbModel.addElement(xmlPath);
						}
					}
				}
				
				if(cbModel.getSize() == 0){
					cbModel.addElement("[No Descriptions Found - Select 'Other' to choose one.]");
				}
				
				cbModel.addElement("Other...");
				
				JPanel p = new JPanel();
				p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
				
				p.add(new JPanelUL(new JLabel("Select an ArchStudio 3 Description:")));
				
				cb.setEditable(false);
				cb.addActionListener(this);
				p.add(new JPanelIS(new JPanelUL(cb), 5));
				
				checkLibraryButton.addActionListener(this);
				p.add(new JPanelIS(checkLibraryButton, 5));
				checkTextArea.setEditable(false);
				p.add(new JPanelIS(new JScrollPane(checkTextArea), 5));
				
				this.add("Center", new JPanelIS(new JPanelUL(p), 5));
				syncData();
				checkNextButton();
			}
			
			protected void syncData(){
				String selectedItem = (String)cb.getSelectedItem();
				setData("archstudioxml", selectedItem);
			}
			
			protected void checkNextButton(){
				String s = (String)cb.getSelectedItem();
				if(s.equals("Other...")){
					nextButtonEnabled = false;
				}
				else if(s.equals("[No Descriptions Found - Select 'Other' to choose one.]")){
					nextButtonEnabled = false;
				}
				else{
					File f = new File(s);
					if(f.exists()){
						nextButtonEnabled = true;
					}
				}
				checkButtons();
			}
			
			public void actionPerformed(ActionEvent e){
				if(e.getSource() == checkLibraryButton){
					try{
						File f = new File((String)cb.getSelectedItem());
						if(!f.exists()){
							checkTextArea.setText("Selected file does not exist.");
							return;
						}
						if(!f.canRead()){
							checkTextArea.setText("Cannot read selected file.");
							return;
						}
						
						FileInputStream is = null;
						try{
							is = new FileInputStream(f);
							byte[] buf = new byte[512];
							is.read(buf);
							String s = new String(buf);
							if(!s.trim().startsWith("<?xml")){
								checkTextArea.setText("Warning: Description does not start with XML tag.");
								return;
							}
						}
						catch(IOException ioe){
							checkTextArea.setText(ioe.toString());
							return;
						}
						finally{
							try{
								if(is != null) is.close();
							}
							catch(IOException ioe2){}
						}
						
						checkTextArea.setText("Description looks OK.");
						checkTextArea.setCaretPosition(0);
					}
					catch(Exception ex){
						checkTextArea.setText(ex.toString());
					}
				}
				else if(e.getSource() == cb){
					checkTextArea.setText(" ");
					String s = (String)cb.getSelectedItem();
					if(s.equals("Other...")){
						JFileChooser chooser = new JFileChooser();
						
						GenericFileFilter filter = new GenericFileFilter();
						filter.addExtension("xml");
						filter.setDescription("XML Files");
						chooser.setFileFilter(filter);
						
						int returnVal = chooser.showOpenDialog(Configurator.this);
						if(returnVal == JFileChooser.APPROVE_OPTION){
							File f = chooser.getSelectedFile();
							String path = SystemUtils.getCanonicalPath(f);
							for(int i = 0; i < cbModel.getSize(); i++){
								String cbModelEntry = (String)cbModel.getElementAt(i);
								if(cbModelEntry.equals(path)){
									cbModel.setSelectedItem(cbModelEntry);
									return;
								}
							}
							cbModel.removeElement("[No Descriptions Found - Select 'Other' to choose one.]");
							cbModel.insertElementAt(path, 0);
							cbModel.setSelectedItem(path);
						}
					}
					
					syncData();
					checkNextButton();
				}	
			}
			
			public boolean shouldNextButtonBeEnabled(){
				return nextButtonEnabled;
			}
		}
		
		class ScriptReviewPanel extends JPanel implements WizardDataChangeListener{
			static final String welcomeText =
				"<html>" +
				"<font size=+1>Check Final Script</font>" +
				"<p>&nbsp;<p>This panel will generate the script and let you make last-minute " +
				"changes before writing the script to disk.  Click 'Finish' to save the script." +
				"</html>";
			
			JTextArea scriptTextArea = new JTextArea();
			
			public ScriptReviewPanel(){
				JLabel mainLabel = new JLabel(welcomeText);
				this.setLayout(new BorderLayout());
				this.add("North", new JPanelIS(mainLabel, 5));
				this.add("Center", new JPanelIS(new JScrollPane(scriptTextArea), 5));
				
				addWizardDataChangeListener(this);
				generateScript(); 		//do it once initially if possible.
			}
			
			public void wizardDataChanged(Wizard w, String name, java.io.Serializable data){
				generateScript();
			}
			
			public void generateScript(){
				String jvmPath = (String)getData("jvm");
				String xarchlibsPath = (String)getData("xarchlibs");
				String archstudioPath = (String)getData("archstudio");
				String archstudioDescriptionPath = (String)getData("archstudioxml");

				if((jvmPath == null) || (xarchlibsPath == null) || (archstudioPath == null) || 
				(archstudioDescriptionPath == null)){
					return;
				}
				
				StringBuffer sb = new StringBuffer();
				
				String nl = System.getProperty("line.separator");
				
				int os = SystemUtils.guessOperatingSystem();
				if(os == SystemUtils.OS_WINDOWS){
					sb.append("@echo off").append(nl);
					
					if(jvmPath.indexOf(" ") != -1){
						sb.append("\"");
					}
					sb.append(jvmPath);
					if(jvmPath.indexOf(" ") != -1){
						sb.append("\"");
					}
					
					sb.append(" -classpath ");
					
					sb.append("\"");

					/*
					if(archstudioPath.indexOf(" ") != -1){
						sb.append("\"");
					}
					*/
					sb.append(archstudioPath);
					
					/*
					if(archstudioPath.indexOf(" ") != -1){
						sb.append("\"");
					}
					*/
					
					sb.append(System.getProperty("path.separator"));
					
					/*
					if(xarchlibsPath.indexOf(" ") != -1){
						sb.append("\"");
					}
					*/
					sb.append(xarchlibsPath);
					/*
					if(xarchlibsPath.indexOf(" ") != -1){
						sb.append("\"");
					}
					*/
					
					sb.append(System.getProperty("path.separator"));
					sb.append("%CLASSPATH%");

					sb.append("\"");

					sb.append(" ");
					sb.append("archstudio.Bootstrap ");

					if(archstudioDescriptionPath.indexOf(" ") != -1){
						sb.append("\"");
					}
					sb.append(archstudioDescriptionPath);
					if(archstudioDescriptionPath.indexOf(" ") != -1){
						sb.append("\"");
					}
					sb.append(nl);
				}
				else if((os == SystemUtils.OS_UNIX) || (os == SystemUtils.OS_UNKNOWN)){
					sb.append("#!/bin/sh").append(nl);
					sb.append(escape(jvmPath));
					sb.append(" -classpath ");
					sb.append(escape(archstudioPath));
					sb.append(System.getProperty("path.separator"));
					sb.append(escape(xarchlibsPath));
					sb.append(System.getProperty("path.separator"));
					sb.append("$CLASSPATH");
					sb.append(" ");
					sb.append("archstudio.Bootstrap ");
					sb.append(escape(archstudioDescriptionPath));
					sb.append(nl);
				}
				
				scriptTextArea.setText(sb.toString());
			}
			
			public String getScriptText(){
				return scriptTextArea.getText();
			}
		}
		
		public void exit(){
			System.exit(0);
		}
		
		public void finish(){
			JComponent currentPanel = getCurrentPanel();
			if(!(currentPanel instanceof ScriptReviewPanel)){
				System.err.println("Bad Mojo.");
				System.exit(1);
			}
			ScriptReviewPanel srp = (ScriptReviewPanel)currentPanel;
			String scriptText = srp.getScriptText();
			
			int os = SystemUtils.guessOperatingSystem();
			
			String extension;
			switch(os){
			case SystemUtils.OS_WINDOWS:
				extension = "bat";
				break;
			case SystemUtils.OS_UNIX:
			default:
				extension = "sh";
				break;
			}
			
			JFileChooser chooser = new JFileChooser();
			// Note: source for ExampleFileFilter can be found in FileChooserDemo,
			// under the demo/jfc directory in the Java 2 SDK, Standard Edition.
			GenericFileFilter filter = new GenericFileFilter();
			filter.addExtension(extension);
			filter.setDescription("Operating-System Specific Script");
			chooser.setFileFilter(filter);
			int returnVal = chooser.showSaveDialog(Configurator.this);
			if(returnVal == JFileChooser.APPROVE_OPTION){
				File f = chooser.getSelectedFile();
				if(f.exists()){
					int choice = JOptionPane.showConfirmDialog(Configurator.this, 
						"This will overwrite " + f.getName() + ". Continue?", 
						"Confirm File Overwrite", JOptionPane.OK_CANCEL_OPTION);
					if(choice != JOptionPane.OK_OPTION){
						return;
					}
				}
				else{
					if(os == SystemUtils.OS_WINDOWS){
						if((!f.getName().toLowerCase().endsWith(".bat")) && (!f.getName().toLowerCase().endsWith(".cmd"))){
							String path = f.getPath();
							path += ".bat";
							f = new File(path);
						}
					}
				}	
					
				FileWriter fw = null;
				try{
					fw = new FileWriter(f);
					fw.write(scriptText);
					fw.close();
					if(os == SystemUtils.OS_UNIX){
						//Try this...
						try{
							SystemUtils.runAndCaptureProcess("chmod u+x " + f.getPath());
						}
						catch(Exception e){
						}
					}
					JOptionPane.showMessageDialog(Configurator.this, "Script written successfully.",
						"Success!", JOptionPane.INFORMATION_MESSAGE);
					System.exit(0);
				}
				catch(IOException ioe){
					JOptionPane.showMessageDialog(Configurator.this, "Error: " + ioe.toString(),
						"Failure", JOptionPane.ERROR_MESSAGE);
					try{
						if(fw != null) fw.close();
					}
					catch(IOException ioe2){}
				}
			}
		}
	}
	
	public static String escape(String s){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < s.length(); i++){
			char ch = s.charAt(i);
			if(ch == ' '){
				sb.append("\\ ");
			}
			else if(ch == '\\'){
				sb.append("\\\\");
			}
			else{
				sb.append(ch);
			}
		}
		return sb.toString();
	}
}

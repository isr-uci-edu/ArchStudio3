package archstudio.comp.aemdriver;

import archstudio.invoke.*;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

//This is imported to support xArchADT use
import edu.uci.ics.xarchutils.*;

import edu.uci.ics.widgets.*;

//This is imported to support AEM use
import archstudio.comp.aem.*;

//Standard Java imports
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class AEMDriverC2Component extends AbstractC2DelegateBrick implements c2.fw.Component, InvokableBrick{

	public static final String PRODUCT_NAME = "AEM Driver Component";
	public static final String PRODUCT_VERSION = "1.0";
	
	//This XArchFlatInterface is an EPC interface implemented on another component,
	//in this case xArchADT.  Because this is an EBIWrapperComponent, we can call
	//functions in this interface directly and all the communication gets translated
	//from procedure calls (PC) to EPC.
	protected XArchFlatInterface xarch;
	
	//The main application window
	protected AEMDriverFrame aemFrame = null;

	//Maps Managed System URIs to ManagedSystemPanels
	protected HashMap managedSystemPanels = new HashMap();
	
	public AEMDriverC2Component(Identifier id){		
		super(id);
		this.addLifecycleProcessor(new AEMDriverLifecycleProcessor());
		xarch = (XArchFlatInterface)EBIWrapperUtils.addExternalService(this, topIface, XArchFlatInterface.class);

		XArchEventProvider xarchEventProvider = 
			(XArchEventProvider)EBIWrapperUtils.createStateChangeProviderProxy(this, topIface,
			XArchEventProvider.class);
		
		XArchFileListener fileListener = new XArchFileListener(){
			public void handleXArchFileEvent(XArchFileEvent evt) {
				if(aemFrame != null){
					aemFrame.updateOpenURLs();
					return;
				}
			}
		};
		xarchEventProvider.addXArchFileListener(fileListener);
		
		InvokeUtils.deployInvokableService(this, bottomIface, 
			"Runtime Tools/Architecture Evolution Manager GUI", 
			"GUI for the Architecture Evolution Manager");
	}
	
	public void invoke(InvokeMessage im){
		newWindow();
	}
	
	//This is called when we get an invoke message from the invoker.
	public void newWindow(){
		System.out.println("New window.");
		
		//This makes sure we only have one active window open.
		if(aemFrame == null){
			System.out.println("Creating new frame.");
			aemFrame = new AEMDriverFrame();
			aemFrame.syncManagedSystemPanels();
		}
		else{
			aemFrame.requestFocus();
		}
	}

	public void closeWindow(){
		if(aemFrame != null){
			aemFrame.setVisible(false);
			aemFrame.dispose();
			aemFrame = null;
		}
	}
	
	class AEMDriverLifecycleProcessor extends c2.fw.LifecycleAdapter{
		public void end(){
			closeWindow();
		}
	}
	
	class AEMDriverFrame extends JFrame implements ActionListener{
		
		protected JTabbedPane tabbedPane;
		
		protected DefaultComboBoxModel urlComboBoxModel = new DefaultComboBoxModel();
		protected JProgressBar mainProgressBar;
		protected JComboBox instantiateUrlList;
		protected JButton instantiateButton;
		protected JTextField tfManagedSystemName;
		protected JComboBox cbEngineType;
		
		//protected JCheckBox cbCreateInstanceModel;
		//protected JTextField tfInstanceModelURI;
		
		public AEMDriverFrame(){
			super(PRODUCT_NAME + " " + PRODUCT_VERSION);
			archstudio.Branding.brandFrame(this);
			init();
			addMessageProcessor(new AEMInstantiateStatusMessageProcessor());
			addMessageProcessor(new AEMProgressMessageProcessor());
		}
		
		class AEMInstantiateStatusMessageProcessor implements MessageProcessor{
			public void handle(Message m){
				if(m instanceof AEMInstantiateStatusMessage){
					AEMInstantiateStatusMessage aism = (AEMInstantiateStatusMessage)m;
					if(aism.isSuccess()){
						ManagedSystemPanel msp = new ManagedSystemPanel(aism.getManagedSystemURI(),
							aism.getArchitectureURI());
						tabbedPane.addTab(aism.getManagedSystemURI(), msp);
						managedSystemPanels.put(aism.getManagedSystemURI(), msp);
						mainProgressBar.setString("");
						mainProgressBar.setValue(0);
						JOptionPane.showMessageDialog(AEMDriverFrame.this, "System instantiated.", "Done", JOptionPane.PLAIN_MESSAGE);
						return;
					}
					else{
						mainProgressBar.setString("");
						mainProgressBar.setValue(0);
						InvalidArchitectureDescriptionException iade = aism.getError();
						JOptionPane.showMessageDialog(AEMDriverFrame.this, iade.toString(), "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}
		}
		
		public void syncManagedSystemPanels(){
			for(Iterator it = managedSystemPanels.keySet().iterator(); it.hasNext(); ){
				String uri = (String)it.next();
				ManagedSystemPanel panel = (ManagedSystemPanel)managedSystemPanels.get(uri);
				
				boolean found = false;
				for(int i = 0; i < tabbedPane.getTabCount(); i++){
					if(tabbedPane.getTitleAt(i).equals(uri)){
						found = true;
						break;
					}
				}
				if(!found){
					tabbedPane.addTab(uri, panel);
				}
			}
		}
				
		class AEMProgressMessageProcessor implements MessageProcessor{
			public void handle(Message m){
				if(m instanceof AEMProgressMessage){
					AEMProgressMessage apm = (AEMProgressMessage)m;
					String managedSystemURI = apm.getManagedSystemURI();
					if(managedSystemURI != null){
						ManagedSystemPanel msp = (ManagedSystemPanel)managedSystemPanels.get(managedSystemURI);
						msp.updateProgress(apm);
					}
				}
			}
		}
		
		class ManagedSystemPanel extends JPanel{
			String managedSystemURI;
			String architectureURI;
			JProgressBar progressBar;
			JTextField instanceModelURIField;
			
			public ManagedSystemPanel(String managedSystemURI, String architectureURI){
				this.managedSystemURI = managedSystemURI;
				this.architectureURI = architectureURI;
				
				JPanel mainPanel = new JPanel();
				
				JPanel boundLabelPanel = new JPanel();
				boundLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				boundLabelPanel.add(new JLabel("<html><b>Currently bound to:</b><br>" + architectureURI + "</html>"));
		
				JPanel progressPanel = new JPanel();
				progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
				progressPanel.add(new JPanelUL(new JLabel("Activity progress:")));
				progressBar = new JProgressBar();
				progressBar.setStringPainted(true);
				progressPanel.add(progressBar);
				
				JPanel imPanel = new JPanel();
				imPanel.setLayout(new BoxLayout(imPanel, BoxLayout.Y_AXIS));
				imPanel.add(new JPanelUL(new JLabel("Create Instance Model")));
				JPanel imPanel1 = new JPanel();
				imPanel1.setLayout(new FlowLayout(FlowLayout.LEFT));
				imPanel1.add(new JLabel("URI:"));
				
				instanceModelURIField = new JTextField(20);
				imPanel1.add(instanceModelURIField);
								
				JButton createModelButton = new JButton("Create Model");
				createModelButton.addActionListener(
					new ActionListener(){
						public void actionPerformed(ActionEvent evt){
							createInstanceModel();
						}
					}
				);
				
				imPanel1.add(createModelButton);
				
				imPanel.add(imPanel1);
				
				mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
				mainPanel.add(new JPanelUL(boundLabelPanel));
				mainPanel.add(Box.createVerticalStrut(6));
				mainPanel.add(new JSeparator());
				mainPanel.add(progressPanel);
				mainPanel.add(Box.createVerticalStrut(6));
				mainPanel.add(new JSeparator());
				mainPanel.add(Box.createVerticalStrut(6));
				mainPanel.add(imPanel);
				
				this.setLayout(new BorderLayout());
				this.add("Center", new JPanelUL(mainPanel));
			}
			
			public void updateProgress(AEMProgressMessage apm){
				String msg = apm.getAdditionalMessage();
				if(msg != null){
					progressBar.setString(msg);
				}
				progressBar.setMaximum(apm.getUpperBound());
				progressBar.setValue(apm.getCurrentValue());
				//this.validate();
				//tabbedPane.validate();
				this.repaint();//(this.getGraphics());
				//progressBar.paint(progressBar.getGraphics());
				if(apm.getUpperBound() == apm.getCurrentValue()){
					progressBar.setValue(0);
					this.repaint();//paint(this.getGraphics());
				}
			}
			
			public void createInstanceModel(){
				String instanceModelURI = instanceModelURIField.getText().trim();
				if(instanceModelURI.equals("")){
					JOptionPane.showMessageDialog(AEMDriverFrame.this, "Enter an instance model URI", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				else{
					AEMCreateInstanceModelMessage acimm = new AEMCreateInstanceModelMessage(managedSystemURI,
						instanceModelURI);
					sendToAll(acimm, topIface);
				}
			}
		}
		
		//This is pretty standard Swing GUI stuff in Java.
		private void init(){
			Toolkit tk = getToolkit();
			Dimension screenSize = tk.getScreenSize();
			double xSize = (400);
			double ySize = (300);
			double xPos = (screenSize.getWidth() * 0.25);
			double yPos = (screenSize.getHeight() * 0.30);

			JPanel tempPanel;

			tabbedPane = new JTabbedPane();
			mainProgressBar = new JProgressBar();
			mainProgressBar.setStringPainted(true);
			JPanel instantiatePanel = new JPanel();
			instantiateUrlList = new JComboBox(urlComboBoxModel);
			instantiateButton = new JButton("Instantiate");
			//instantiateButton.addActionListener(this);
			instantiateButton.addActionListener(
				(ActionListener)c2.pcwrap.ThreadInterfaceProxyFactory.
				createThreadInterfaceProxy(this, ActionListener.class)
			);
			instantiatePanel.setLayout(new BorderLayout());
			
			tempPanel = new JPanel();
			tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.Y_AXIS));
			
			JPanel tempPanel2 = new JPanel();
			tempPanel2.setLayout(new FlowLayout(FlowLayout.CENTER)); 
			tempPanel2.add(instantiateUrlList);
			tempPanel.add(/*new JPanelUL(*/tempPanel2/*)*/);
			
			JPanel engineTypePanel = new JPanel();
			cbEngineType = new JComboBox(new Object[]{"One Thread Per Brick", "Steppable"});
			engineTypePanel.setLayout(new BoxLayout(engineTypePanel, BoxLayout.Y_AXIS));
			engineTypePanel.add(new JPanelUL(new JLabel("Engine Type")));
			engineTypePanel.add(new JPanelUL(cbEngineType));
			tempPanel.add(engineTypePanel);
			
			tempPanel2 = new JPanel();

			tempPanel2.setLayout(new BoxLayout(tempPanel2, BoxLayout.Y_AXIS));
			JPanel lPanel = new JPanel();
			lPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			lPanel.add(new JLabel("Enter a URI for the managed system:"));
			JPanel tfPanel = new JPanel();
			tfPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			tfManagedSystemName = new JTextField(20);
			tfPanel.add(tfManagedSystemName);
			tempPanel2.add(lPanel);
			tempPanel2.add(tfPanel);
			tempPanel.add(tempPanel2);
			
			tempPanel2 = new JPanel();
			tempPanel2.setLayout(new BoxLayout(tempPanel2, BoxLayout.Y_AXIS));
			lPanel = new JPanel();
			lPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			lPanel.add(new JLabel("Progress:"));
			tempPanel2.add(lPanel);
			JPanel pbarPanel = new JPanel();
			pbarPanel.setLayout(new BoxLayout(pbarPanel, BoxLayout.Y_AXIS));
			pbarPanel.add(mainProgressBar);
			tempPanel2.add(pbarPanel);
			tempPanel.add(tempPanel2);
			
			tempPanel.add(new JSeparator());

			/*
			tempPanel2 = new JPanel();
			tempPanel2.setLayout(new FlowLayout(FlowLayout.LEFT));
			cbCreateInstanceModel = new JCheckBox("Create Instance Model", false);
			tempPanel2.add(cbCreateInstanceModel);
			tempPanel.add(tempPanel2);
			*/
			
			/*
			tempPanel2 = new JPanel();
			tempPanel2.setLayout(new FlowLayout(FlowLayout.LEFT));
			tfInstanceModelURI = new JTextField(20);
			tempPanel2.add(new JLabel("Instance Model URI: "));
			tempPanel2.add(tfInstanceModelURI);
			tempPanel.add(tempPanel2);
			*/
			
			tempPanel.add(Box.createGlue());
			
			instantiatePanel.add("Center", tempPanel);
			
			tempPanel = new JPanel();
			tempPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			tempPanel.add(instantiateButton);
			instantiatePanel.add("South", tempPanel);
			
			tabbedPane.addTab("Instantiate", instantiatePanel);

			JPanel mainPanel = new JPanel();
			
			mainPanel.setLayout(new BorderLayout());
			mainPanel.add("Center", tabbedPane);

			this.getContentPane().add(new JScrollPane(mainPanel));

			setVisible(true);
			setSize((int)xSize, (int)ySize);
			setLocation((int)xPos, (int)yPos);
			setVisible(true);
			paint(getGraphics());

			this.addWindowListener(new AEMDriverWindowAdapter());
			
			//Go get the initial list of URLs.
			updateOpenURLs();
		}
		
		class AEMDriverWindowAdapter extends WindowAdapter{
			public void windowClosing(WindowEvent e){
				closeWindow();
			}
		}				
		
		public void updateOpenURLs(){
			urlComboBoxModel.removeAllElements();
			
			//This innocuous call gets translated by the local proxy
			//(XArchFlatInterface xarch) into an EPC and sent off, via an event
			//to xArchADT.  The result is marshalled into an event and returned
			//here.  All this magic happens under the covers because of the
			//services provided by EBIWrapperComponent.
			String[] urls = xarch.getOpenXArchURIs();
			if(urls.length == 0){
				urlComboBoxModel.addElement("[No Architectures Open]");
			}
			else{
				for(int i = 0; i < urls.length; i++){
					urlComboBoxModel.addElement(urls[i]);
				}
			}
			repaint();
		}
		
		public void actionPerformed(ActionEvent evt){
			if(evt.getSource() instanceof JButton){
				String label = ((JButton)evt.getSource()).getText();
				if(label.equals("Instantiate")){
					//System.out.println("Instantiating!");
					String url = (String)instantiateUrlList.getSelectedItem();
					if(url.equals("[No Architectures Open]")){
						JOptionPane.showMessageDialog(this, "No architecture selected.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					String managedSystemURI = tfManagedSystemName.getText();
					if(managedSystemURI.trim().equals("")){
						managedSystemURI = "urn:ManagedSystem" + System.currentTimeMillis();
					}
					
					String engineTypeString = (String)cbEngineType.getSelectedItem();
					int engineType = AEMInstantiateMessage.ENGINETYPE_ONETHREADPERBRICK;
					if(engineTypeString.equals("One Thread Per Brick")){
						engineType = AEMInstantiateMessage.ENGINETYPE_ONETHREADPERBRICK;
					}
					else if(engineTypeString.equals("Steppable")){
						engineType = AEMInstantiateMessage.ENGINETYPE_ONETHREADSTEPPABLE;
					}
						
					
					AEMInstantiateMessage aim = new AEMInstantiateMessage(managedSystemURI,
						url, engineType);
					sendToAll(aim, topIface);
					
					mainProgressBar.setString("Instantiating...");
					mainProgressBar.setMaximum(100);
					mainProgressBar.setValue(50);
					
					/*
					try{
						if(cbCreateInstanceModel.isSelected()){
							String instanceURI = tfInstanceModelURI.getText();
							if(instanceURI.trim().equals("")){
								instanceURI = "urn:instance";
							}
							aem.instantiate(url, instanceURI);
						}
						else{
							aem.instantiate(url);
						}
					}
					catch(InvalidArchitectureDescriptionException e){
						JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					*/
				}
			}
		}
	}
	
}

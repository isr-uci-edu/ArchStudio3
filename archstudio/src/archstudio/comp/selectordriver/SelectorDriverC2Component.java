package archstudio.comp.selectordriver;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;
import c2.util.UIDGenerator;

//This is imported to support xArchADT use
import edu.uci.ics.xarchutils.*;
import edu.uci.ics.widgets.*;

import archstudio.invoke.*;
//This is imported to the selector
import archstudio.comp.booleaneval.*;
import archstudio.comp.selector.*;
import archstudio.comp.archpruner.*;
import archstudio.comp.pruneversions.*;

//Standard Java imports
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.text.DateFormat;
import java.text.*;
import java.io.*;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;


/**
 * This is the C2 Componenent for the SelectorDriver.
 * It uses an XArchFlatInterface which is an EPC interface implemented on another component,
 * in this case xArchADT.  Because this is an EBIWrapperComponent, we can call
 * functions in this interface directly and all the communication gets translated
 * from procedure calls (PC) to EPC.   
 * @author Matt Critchlow <A HREF="mailto:critchlm@uci.com">(critchlm@uci.edu)</A>
 */
public class SelectorDriverC2Component extends AbstractC2DelegateBrick 
	implements InvokableBrick, c2.fw.Component
{
	public static final String PRODUCT_NAME = "Product Line Selector GUI";
	
	//This XArchFlatInterface is an EPC interface implemented on another component,
	//in this case xArchADT.  Because this is an EBIWrapperComponent, we can call
	//functions in this interface directly and all the communication gets translated
	//from procedure calls (PC) to EPC.
	protected XArchFlatInterface xarch;
	protected boolean stallMessageProcessor;
	protected boolean stallAWTThread;
	
	protected String selectorDriverID;
	protected SelectorDriverFrame selectorFrame = null;
	protected SymbolTable symbolTable = new SymbolTable();
	
	/**
	 * The selector driver listens to asynchronous messages which are passed between
	 * C2 components. Currently the driver has message processor inner classes which can
	 * handle StateChangeMessages, as well as status messages from the Selector and the Pruner.
	 * Since the status messages serve the same purpose in the driver, they can be handled through
	 * one class.
	 * @param id		The unique identifier for the SelectorDriverC2Componenent
	 */
	public SelectorDriverC2Component( Identifier id )
	{
		super( id );
		selectorDriverID = id.toString();

		XArchEventProvider xarchEventProvider = 
			(XArchEventProvider)EBIWrapperUtils.createStateChangeProviderProxy(this, topIface,
			XArchEventProvider.class);
		
		XArchFileListener fileListener = new XArchFileListener(){
			public void handleXArchFileEvent(XArchFileEvent evt) {
				if(selectorFrame != null){
					selectorFrame.updateOpenURLs();
				}
			}
		};
		xarchEventProvider.addXArchFileListener(fileListener);
		
		this.addMessageProcessor( new StatusMessageProcessor());	
		xarch = ( XArchFlatInterface )EBIWrapperUtils.addExternalService(
			this, topIface, XArchFlatInterface.class );
		InvokeUtils.deployInvokableService(this, bottomIface, 
			"Product-Line Support/Product Line Selector GUI", 
			"Selects smaller product lines or individual products from a product line");
	}
	
	public void invoke(InvokeMessage im){
		newWindow();
	}
	private void exitSelectorDriver()
	{
		selectorFrame.setVisible(false);
		selectorFrame = null;
	}
	
	//This is called when we get an invoke message from the invoker.
	public void newWindow(){
		//System.out.println("New window.");
		
		//This makes sure we only have one active window open.
		if(selectorFrame == null){
			//System.out.println("Creating new frame.");
			selectorFrame = new SelectorDriverFrame();
		}
		else
		{
			selectorFrame.requestFocus();
			//selectorFrame.setVisible(true);
		}
	}

	/**
	 * This is a basic helper class for the JTable in the selectorFrame which creates a unique TableModel
	 * that allows JTable restrictions to be created.
	 */
	class myTableModel extends DefaultTableModel
	{
		public boolean isCellEditable(int row, int col)
		{
			return false;
		}
	}
	
	/**
	 * The StatusMessageProcessor listens to messages which may have come from either the Selector or the Pruner.
	 * If this is found to be true, then the driver must update the progress bar of the driver.
	 * This is done by calling updateProgress() which is a method in the selectorFrame class.
	 */
	class StatusMessageProcessor implements MessageProcessor
	{
		public void handle(Message m)
		{
			if(m instanceof ArchSelectorStatusMessage)
			{
				ArchSelectorStatusMessage message = (ArchSelectorStatusMessage)m;
				if(selectorDriverID == message.getComponentID())
				{
					try
					{
						selectorFrame.updateProgress(message);
						return;
					}
					catch(Exception e)
					{
						e.printStackTrace();
						return;
					}
				}
			}
			if(m instanceof ArchPrunerStatusMessage)
			{
				ArchPrunerStatusMessage message = (ArchPrunerStatusMessage)m;			
				if(selectorDriverID == message.getComponentID())
				{
					try
					{
						selectorFrame.updateProgress(message);
						return;
					}
					catch(Exception e)
					{
						e.printStackTrace();
						return;
					}
				}	
			}
			if(m instanceof PruneVersionsStatusMessage)
			{
				PruneVersionsStatusMessage message = (PruneVersionsStatusMessage)m;			
				if(selectorDriverID == message.getComponentID())
				{
					try
					{
						selectorFrame.updateProgress(message);
						return;
					}
					catch(Exception e)
					{
						e.printStackTrace();
						return;
					}
				}	
			}
			else
				return;
		}
	}
	/**
	 * This is the GUI Driver for the Selector and Pruner components in archstudio.
	 * The driver provides different options of performing selection and pruning. A selection or prune
	 * method may use either a version of a type, or a structure as a starting point. Also
	 * the user may choose to either select, prune, or select and prune in one step.
	 * A symbol table is used to input. 
	 */
	class SelectorDriverFrame extends JFrame implements ActionListener
	{
		protected final static String NO_ARCH_OPEN = "[No Architectures Open]";
		
		//GUI stuff for the selector panel
		protected JPanel selectorPanel;
		protected DefaultComboBoxModel urlComboBoxModel = new DefaultComboBoxModel();
		protected JComboBox openUrlList;
		protected JComboBox openUrlList2;
		protected String openUrl = null;
		
		protected JTextField targetUrlField;
		protected String inputFileField;
		protected JButton browseButton;
		protected JButton selectButton;
		private JCheckBox selectBox;
		private JCheckBox pruneBox;
		private JCheckBox versionpruneBox;
		
		//GUI stuff for the create table panel
		protected DefaultComboBoxModel symbolTypes = new DefaultComboBoxModel();
		protected JComboBox openSymbolTypesList;
		protected JTextField nameField;
		protected JTextField valueField;
		protected JButton addToSymbolTableButton;
		protected JButton saveSymbolTableButton;
		protected JButton clearTableButton;
		protected JTextArea textArea;
		
		//GUI stuff for the JTable
		protected myTableModel tableModel;
		private DefaultTableColumnModel columnModel; 
		protected JTable jTable; 
		protected JButton removeSymbolButton;
		protected JScrollPane scrollPane; 
		protected JPanel tablePannel;
		protected JPanel buttonPanel;
		protected JPanel symbolPanel;
		protected JPanel createTable;
		protected JMenuBar menuBar;
		protected JMenu menu;
		protected JMenuItem menuItem;
		private JTabbedPane tabbedPane;
		private JPanel versionPanel;
		private JPanel structPanel;
		private JPanel urlPanel;
		private JPanel urlPanel2;
		
		private JPanel typeSelectorPane = new JPanel( );
		private TypeSelector typeSelector;
		
		private JPanel structSelectorPane = new JPanel( );
		private StructureSelector structureSelector;
		protected JPanel placeholder = new JPanel( );
		
		private boolean isStructure;
		protected JProgressBar mainProgressBar;
		protected JPanel progressPanel;
		protected boolean pruneAllCheck;
		protected boolean pruneCheck;
		protected boolean versionPruneCheck;
		private boolean multipleChoice;
		protected storeMessageParameters messageParameters;
		private String extraTempUrl = null;
		private String opPerformed = "";
		
		public SelectorDriverFrame()
		{
			super(PRODUCT_NAME);
			archstudio.Branding.brandFrame(this);
			init();
		}
		
		//This is pretty standard Swing GUI stuff in Java.
		private void init()
		{
			Toolkit tk = getToolkit();
			Dimension screenSize = tk.getScreenSize();
			double xSize = (550);
			double ySize = (650);
			double xPos = (screenSize.getWidth() * 0.20);
			double yPos = (screenSize.getHeight() * 0.04);
			
			JPanel tempPanel;    // this is the panel that will be used to hold all the 
			// text boxes and such inside the selector panel,
			// its just a temporary panel that will get recreated 
			
			//This is the tabbedPabe for the create table panel and the corresponding panels
			tabbedPane = new JTabbedPane();
			selectorPanel = new JPanel();
			selectorPanel.setLayout(new BoxLayout(selectorPanel, BoxLayout.Y_AXIS));
			
			/***************** Begin of menu bar *******************/
			//Create the menu bar.
			menuBar = new JMenuBar();
			setJMenuBar(menuBar);
			
			//Build the menu.
			menu = new JMenu("File");
			menuBar.add(menu);
			
			//add Load Table to the menu
			menuItem = new JMenuItem("Load Table");
			menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_O, ActionEvent.CTRL_MASK));
			menuItem.addActionListener(this);
			menu.add(menuItem);
			
			//add Save Table to the menu
			menuItem = new JMenuItem("Save Table");
			menuItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_S, ActionEvent.CTRL_MASK));
			menuItem.addActionListener(this);
			menu.add(menuItem);
			menu.addSeparator();
			
			menuItem = new JMenuItem("Exit");
			menuItem.addActionListener(this);
			menu.add(menuItem);
			
			/***************** End of menu bar *********************/
	
			/***************** Begin of structure/version ***********/
			
			urlPanel = new JPanel();
			urlPanel.setLayout(new FlowLayout( FlowLayout.LEFT));
			openUrlList = new JComboBox(urlComboBoxModel);
			openUrlList.addActionListener(this);
			
			urlPanel.add( new JLabel( "Source:" ) );
			urlPanel.add(openUrlList);
			
			urlPanel2 = new JPanel();
			urlPanel2.setLayout(new FlowLayout( FlowLayout.LEFT));
			openUrlList2 = new JComboBox(urlComboBoxModel);
			openUrlList2.addActionListener(this);
			urlPanel2.add( new JLabel( "Source:" ) );
			urlPanel2.add(openUrlList2);
			
			//Go get the initial list of URLs.
			updateOpenURLs();
			
			//typeSelectorPane = new JPanel( );
			//structSelectorPane = new JPanel( );
			
			updateSelectors( );
			
			String selectedArchitecture = ( String )openUrlList.getSelectedItem();
			versionPanel = new JPanel();
			versionPanel.setLayout(new BoxLayout(versionPanel, BoxLayout.Y_AXIS));
			versionPanel.add(urlPanel);
			versionPanel.add( typeSelectorPane );
			
			structPanel = new JPanel();
			structPanel.setLayout(new BoxLayout(structPanel, BoxLayout.Y_AXIS));
			structPanel.add(urlPanel2);
			structPanel.add( structSelectorPane );
			
			// adds the create symbol table panel to the tabbed panel
			tabbedPane.addTab( "Type/Version" , versionPanel);
			// adds the selector panel to the tabbed panel
			tabbedPane.addTab( "Structure", structPanel );
			selectorPanel.add(tabbedPane);
			
			/**************** End of structure/version **********/
			
			
			/*************** Start of Symbol Table Panel *********/
			
			createTable = new JPanel();
			createTable.setLayout(new BoxLayout(createTable, BoxLayout.Y_AXIS));
			
			symbolTypes.addElement("string");
			symbolTypes.addElement("date");
			symbolTypes.addElement("double");
			
			// sets up the combo box for the list of symbol types
			openSymbolTypesList = new JComboBox(symbolTypes);
			
			// sets up the addToSymbolTable button
			addToSymbolTableButton = new JButton( "Add/Modify" );
			addToSymbolTableButton.addActionListener( this );
			addToSymbolTableButton.setActionCommand( "Add/Modify" );
			
			// sets up the removeSymbolButton
			
			removeSymbolButton = new JButton( "Remove" );
			removeSymbolButton.addActionListener( this );
			removeSymbolButton.setActionCommand( "Remove" );
			
			// sets up the clearTableButton
			
			clearTableButton = new JButton( "Clear" );
			clearTableButton.addActionListener( this );
			clearTableButton.setActionCommand( "Clear" );
			
			// sets up the text fields
			nameField = new JTextField( 20 );
			valueField = new JTextField( 20 );
			
			JPanel inputPanel = new JPanel(new GridLayout(2,1));
			// add the list of possible symbol types to symbolPanel
			tempPanel = new JPanel( );
			tempPanel.setLayout(new FlowLayout( FlowLayout.LEFT ) );
			tempPanel.add(new JLabel("Symbol Types"));
			tempPanel.add( openSymbolTypesList);
			inputPanel.add(tempPanel);
			
			// now we add the text fields into a panel
			JPanel textPanel = new JPanel(new GridLayout(1,2));
			tempPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			tempPanel.add( new JLabel( "Name:" ) );
			tempPanel.add( nameField );
			textPanel.add( tempPanel );
			
			tempPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			tempPanel.add( new JLabel( "Value:" ) );
			tempPanel.add( valueField );
			textPanel.add( tempPanel );
			inputPanel.add(textPanel);
			
			createTable.add(new JPanelUL(inputPanel));
			createTable.add(new JSeparator());
			createTable.add(Box.createHorizontalStrut(15));
			
			buttonPanel = new JPanel( new GridLayout(1,3) );
			buttonPanel.add( addToSymbolTableButton);
			buttonPanel.add( removeSymbolButton);
			buttonPanel.add( clearTableButton);
			
			createTable.add(buttonPanel);
			createTable.add(new JSeparator());
			createTable.add(Box.createHorizontalStrut(15));
			
			//**************************************************
			//     Setup info for the JTable				   *
			//**************************************************
			tableModel =  new myTableModel();
			tableModel.addColumn("Symbol Name");
			tableModel.addColumn("Symbol Value");
			tableModel.addColumn("Symbol Type");
			jTable = new JTable(tableModel);
			jTable.setColumnSelectionAllowed(false);
			jTable.setRowSelectionAllowed(true);
			jTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			columnModel = (DefaultTableColumnModel)jTable.getColumnModel();
			columnModel.addColumnModelListener(jTable);
			MouseAdapter listMouseListener = new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 2)
					{
						int row = jTable.getSelectedRow();
						int nameCol = columnModel.getColumnIndex("Symbol Name");
						int valueCol = columnModel.getColumnIndex("Symbol Value");
						int typeCol = columnModel.getColumnIndex("Symbol Type");
						nameField.setText((String)jTable.getValueAt(row,nameCol));
						Object value = jTable.getValueAt(row,valueCol);
						valueField.setText(value.toString());
						String type = (String)jTable.getValueAt(row,typeCol);
						symbolTypes.setSelectedItem(type);
						validate();
						repaint();
					}
				}};
			
			jTable.addMouseListener(listMouseListener); 
			createTable.add(new JScrollPane(jTable));
			createTable.add(new JSeparator());
			createTable.add(Box.createHorizontalStrut(15));
			createTable.add(Box.createGlue());
			selectorPanel.add(createTable);
			
			/********* End of Symbol Table *********************/
			
			/*************** Start of Target Panel ****************/
			JPanel targetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			targetUrlField = new JTextField( 31 );
			// now we add the button and the text fields
			targetPanel.add( new JLabel( "Target URL/URN:" ) );
			targetPanel.add( targetUrlField );
			selectorPanel.add(targetPanel);
			/****************End of Target Panel ******************/
			
			/****************Start of checkBoxes ******************/
			
			JPanel boxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			selectBox = new JCheckBox( "Select" , false );
			pruneBox = new JCheckBox( "Prune" , false );
			versionpruneBox = new JCheckBox ("Version Prune" , false);
			boxPanel.add(new JLabel ("Please Select The Operations(s) To Perform: "));
			boxPanel.add(selectBox);
			boxPanel.add(pruneBox);
			boxPanel.add(versionpruneBox);
			selectorPanel.add(boxPanel);
			selectorPanel.add(new JSeparator());
			selectorPanel.add(Box.createHorizontalGlue());
			
			/************** End of checkBoxes *********************/

			/*************** start of Progress Panel ***************/
			selectButton = new JButton( "Start" );
			selectButton.addActionListener( (ActionListener)
				c2.pcwrap.ThreadInterfaceProxyFactory.createThreadInterfaceProxy( this,
				ActionListener.class ) );
			mainProgressBar = new JProgressBar();
			mainProgressBar.setPreferredSize(new Dimension(300,21));
			mainProgressBar.setMinimumSize(new Dimension(300,21));
			mainProgressBar.setStringPainted(true);
			progressPanel = new JPanel();
			progressPanel.setLayout( new FlowLayout(FlowLayout.CENTER));
			progressPanel.add(selectButton);
			progressPanel.add(mainProgressBar);
			selectorPanel.add(progressPanel);
			
			/*************** End of Progress Panel ***************/
			this.getContentPane( ).add(selectorPanel);
			this.setVisible( true );
			this.setSize( ( int )xSize, ( int )ySize );
			this.setLocation( ( int )xPos, ( int )yPos );
			this.setVisible( true );
			this.paint( getGraphics( ) );
			this.addWindowListener( new SelectorDriverWindowAdapter( ) );
			
			
		}
		/**
		 * This inner class stores the parameters from the currently processing message.
		 * This is used to ensure the correct message and values are being used
		 * during progress, by allowing for reentrant code.
		 */
		class storeMessageParameters
		{
			public String url;
			public String targetUrl;
			public String id;
			public boolean isStructure;
			/**
			 * @param	url			Url for the current process.
			 * @param	targetUrl	The name of url which the finished url will be set to.
			 * @param	id			The starting point for the current process.
			 * @param	isStructure	Boolean value for choosing starting process.
			 */
			public storeMessageParameters(String url, String targetUrl, String id, boolean isStructure)
			{
				this.url = url;
				this.targetUrl = targetUrl;
				this.id = id;
				if(isStructure == true)
					this.isStructure = isStructure;
			}
		}
		class SelectorDriverWindowAdapter extends WindowAdapter
		{
			public void windowClosing(WindowEvent e)
			{
				destroy();
				dispose();
				setVisible(false);
				selectorFrame = null;
			}
		}
		
		// This function creates new (type/version or structure) selectors if necessary and 
		// puts it in the proper panels. 
		protected void updateSelectors( )
		{
			typeSelectorPane.removeAll( );
			structSelectorPane.removeAll( );

			// if the original exists, the new one must exist too (it might be the same)
			if( openUrl != null && !openUrl.equals( NO_ARCH_OPEN ) )
			{
				typeSelector = new TypeSelector( openUrl, "ComponentType", xarch );
				structureSelector = new StructureSelector( openUrl, xarch );

				typeSelectorPane.add( typeSelector );
				structSelectorPane.add( structureSelector );
			}
			else
			{
				// just add a place holder
				typeSelectorPane.add( placeholder );
				structSelectorPane.add( placeholder );
			}
		}
		
		/**
		 * This method is necessary to invoke when a StateChangeMessage has been 
		 * sent from the xArchADT. The list of open urls is removed, and a new list is created by generating 
		 * a new call to getOpenXArchURLs, thus repopulating the list with the current available architectures. 
		 */
		public void updateOpenURLs()
		{
			urlComboBoxModel.removeAllElements( );
			
			//This innocuous call gets translated by the local proxy
			//(XArchFlatInterface xarch) into an EPC and sent off, via an event
			//to xArchADT.  The result is marshalled into an event and returned
			//here.  All this magic happens under the covers because of the
			//services provided by EBIWrapperComponent.
			String[] urls = xarch.getOpenXArchURIs();
			if(urls.length == 0)
			{
				urlComboBoxModel.addElement( NO_ARCH_OPEN );

				// Null the strings
				openUrl = null;
				// null the selectors
				structureSelector = null;
				typeSelector = null;
				
				openUrlList.setSelectedItem( NO_ARCH_OPEN );
				openUrlList2.setSelectedItem( NO_ARCH_OPEN );
				
				// refresh the panels since they don't contain anything now
				// it shouldn't matter what we pass in here
				updateSelectors( );
			}
			else
			{
				boolean origExists = false;
				
				// This flags just prevents us from listening to the 
				// action performed of the combo boxes while we are still adding elements
				
				// we dont' want it to update while we are still adding
				// elements to the list model
				stallAWTThread = true;
				for( int i = 0; i < urls.length; i++ )
				{
					urlComboBoxModel.addElement( urls[i] );
					if( urls[i].equals( openUrl ) )
					{
						origExists = true;
					}
				}
				stallAWTThread = false;
				
				// if the same element is no longer there
				if( !origExists )
				{
					// the element from before was removed
					// selects the first element by default
					openUrl = ( String )urlComboBoxModel.getElementAt( 0 );
				}
				openUrlList.setSelectedItem( openUrl );
				openUrlList2.setSelectedItem( openUrl );

			}
			repaint();
		}
		/**
		 * This method is necessary to invoke when a StatusMessage has been sent from either the Selector or the Pruner.
		 * The message is then used to update the current status of the progress bar.
		 */
		public void updateProgress(Message message) throws Exception
		{
			if(message == null)
				return;
			
			//if the message if a selected message, we need to see how the progress is going
			//if it's done, we have to see if we need to prune and/or versionPrune, if so we have
			//new messages to send.
			if(message instanceof ArchSelectorStatusMessage)
			{
				
				ArchSelectorStatusMessage smsg = (ArchSelectorStatusMessage)message;
				if(!smsg.getIsDone())
				{
					mainProgressBar.setMaximum(smsg.getUpperBound());
					mainProgressBar.setValue(smsg.getCurrentValue());
					double tempValue = mainProgressBar.getPercentComplete();
					tempValue = tempValue*100;
					DecimalFormat format = new DecimalFormat("##");
					String percent = format.format(tempValue);
					percent = "Selecting: " + percent + "%";
					mainProgressBar.setString(percent);
					repaint();
					return;
				}
				else
				{	
					mainProgressBar.setValue(0);
					String url = messageParameters.url;
					UIDGenerator generator = new UIDGenerator();
					String tempTarget = UIDGenerator.generateUID();
					if(smsg.getErrorOccurred())
						//we got an error message, set everything back and throw an exception
					{
						mainProgressBar.setString("Error");
						stallMessageProcessor = false;
						selectButton.setEnabled(true);
						selectBox.setEnabled(true);
						pruneBox.setEnabled(true);
						versionpruneBox.setEnabled(true);
						selectBox.setSelected(false);
						pruneBox.setSelected(false);
						versionpruneBox.setSelected(false);
						isStructure = false;
						pruneAllCheck = false;
						pruneCheck = false;
						versionPruneCheck = false;
						System.out.println("Error while selecting!");
						throw smsg.getError();
					}
					//user selected select->prune->versionPrune
					else if(pruneAllCheck)
					{
						String id = messageParameters.id;
						String targetUrl = messageParameters.targetUrl;
						extraTempUrl = targetUrl;
						boolean isStructural = messageParameters.isStructure;
						pruneAllCheck = false;
						versionPruneCheck = true;
						PerformArchPrunerMessage pruneMessage = new PerformArchPrunerMessage(targetUrl, tempTarget, selectorDriverID, id, isStructural);
						messageParameters = new storeMessageParameters(targetUrl, tempTarget, id, isStructural);
						sendToAll(pruneMessage, topIface);
						System.out.println("Finished Selecting!");
						System.out.println("Started Pruning!");
						return;
					}
					//user selected select->prune
					else if(pruneCheck)
					{
						String id = messageParameters.id;
						String targetUrl = messageParameters.targetUrl;
						boolean isStructural = messageParameters.isStructure;
						pruneCheck = false;
						PerformArchPrunerMessage pruneMessage = new PerformArchPrunerMessage(targetUrl, targetUrlField.getText(), selectorDriverID, id, isStructural);
						messageParameters = new storeMessageParameters(targetUrl, targetUrlField.getText(), id, isStructural);
						sendToAll(pruneMessage, topIface);
						System.out.println("Finished Selecting!");
						System.out.println("Started Pruning!");
						return;
					}
					//user selected select->versionPrune
					else if(versionPruneCheck)
					{
						String id = messageParameters.id;
						String targetUrl = messageParameters.targetUrl;
						boolean isStructural = messageParameters.isStructure;
						versionPruneCheck = false;
						PerformPruneVersionsMessage pruneVersionMessage = new PerformPruneVersionsMessage(targetUrl, targetUrlField.getText(), selectorDriverID);
						messageParameters = new storeMessageParameters(targetUrl, targetUrlField.getText(), id, isStructural);
						sendToAll(pruneVersionMessage, topIface);
						System.out.println("Finished Selecting!");
						System.out.println("Started Version Pruning!");
						return;
					}
					//we are finished with selecting, nothing left to do
					else
					{
						mainProgressBar.setString("Finished");
						stallMessageProcessor = false;
						stallAWTThread = true;
						updateOpenURLs();
						stallAWTThread = false;
						selectBox.setEnabled(true);
						pruneBox.setEnabled(true);
						versionpruneBox.setEnabled(true);
						selectBox.setSelected(false);
						pruneBox.setSelected(false);
						versionpruneBox.setSelected(false);
						selectButton.setEnabled(true);
						System.out.println("Finished Selecting!");
						String op = "Finished " + opPerformed;
						JOptionPane.showMessageDialog(this, op , "Message", JOptionPane.INFORMATION_MESSAGE);
						isStructure = false;
						return;
					}
				}
			}
			
			//if the message comes from the archPruner, we need to check the progress of the task.
			//if we are done with the task, we need to see if we still need to version prune. If so,
			//we have a new message to send.
			if(message instanceof ArchPrunerStatusMessage)
			{
				ArchPrunerStatusMessage pmsg = (ArchPrunerStatusMessage)message;
				if(!pmsg.getIsDone())
				{
					mainProgressBar.setMaximum(pmsg.getUpperBound());
					mainProgressBar.setValue(pmsg.getCurrentValue());
					double tempValue = mainProgressBar.getPercentComplete();
					tempValue = tempValue*100;
					DecimalFormat format = new DecimalFormat("##");
					String percent = format.format(tempValue);
					percent = "Pruning: " + percent + "%";
					mainProgressBar.setString(percent);
					repaint();
					return;
				}
				else
				{
					mainProgressBar.setValue(0);
					String url = messageParameters.url;
					//we got an error message, throw an exception
					if(pmsg.getErrorOccurred())
					{
						selectButton.setEnabled(true);
						selectBox.setEnabled(true);
						pruneBox.setEnabled(true);
						versionpruneBox.setEnabled(true);
						selectBox.setSelected(false);
						pruneBox.setSelected(false);
						versionpruneBox.setSelected(false);
						isStructure = false;
						pruneAllCheck = false;
						pruneCheck = false;
						versionPruneCheck = false;
						mainProgressBar.setString("Error");
						System.out.println("Error while pruning!");
						throw pmsg.getError();
					}
					else if(versionPruneCheck)
					{
						String id = messageParameters.id;
						String targetUrl = messageParameters.targetUrl;
						boolean isStructural = messageParameters.isStructure;
						versionPruneCheck = false;
						PerformPruneVersionsMessage pruneVersionMessage = new PerformPruneVersionsMessage(targetUrl, targetUrlField.getText(), selectorDriverID);
						messageParameters = new storeMessageParameters(targetUrl, targetUrlField.getText(), id, isStructural);
						sendToAll(pruneVersionMessage, topIface);
						System.out.println("Finished Pruning!");
						System.out.println("Started Version Pruning!");
						return;
					}
					else
					{
						if(multipleChoice)
							xarch.close(url);
						multipleChoice = false;
						mainProgressBar.setString("Finished");
						stallMessageProcessor = false;
						stallAWTThread = true;
						updateOpenURLs();
						stallAWTThread = false;
						selectButton.setEnabled(true);
						selectBox.setEnabled(true);
						pruneBox.setEnabled(true);
						versionpruneBox.setEnabled(true);
						selectBox.setSelected(false);
						pruneBox.setSelected(false);
						versionpruneBox.setSelected(false);
						isStructure = false;
						System.out.println("Finished Pruning!");
						String op = "Finished " + opPerformed;
						JOptionPane.showMessageDialog(this, op , "Message", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
				}
			}
			if(message instanceof PruneVersionsStatusMessage)
			{
				PruneVersionsStatusMessage pmsg = (PruneVersionsStatusMessage)message;
				if(!pmsg.getIsDone())
				{
					mainProgressBar.setMaximum(pmsg.getUpperBound());
					mainProgressBar.setValue(pmsg.getCurrentValue());
					double tempValue = mainProgressBar.getPercentComplete();
					tempValue = tempValue*100;
					DecimalFormat format = new DecimalFormat("##");
					String percent = format.format(tempValue);
					percent = "Version Pruning: " + percent + "%";
					mainProgressBar.setString(percent);
					repaint();
					return;
				}
				else
				{
					mainProgressBar.setValue(0);
					String url = messageParameters.url;
					//we got an error message, throw an exception
					if(pmsg.getErrorOccurred())
					{
						selectButton.setEnabled(true);
						selectBox.setEnabled(true);
						pruneBox.setEnabled(true);
						versionpruneBox.setEnabled(true);
						selectBox.setSelected(false);
						pruneBox.setSelected(false);
						versionpruneBox.setSelected(false);
						isStructure = false;
						versionPruneCheck = false;
						pruneCheck = false;
						pruneAllCheck = false;
						mainProgressBar.setString("Error");
						System.out.println("Error while version pruning!");
						throw pmsg.getError();
					}
					else
					{
						if(multipleChoice)
						{
							xarch.close(url);
							if(extraTempUrl != null)
							{
								xarch.close(extraTempUrl);
								extraTempUrl = null;
							}
						}
						multipleChoice = false;
						mainProgressBar.setString("Finished");
						stallMessageProcessor = false;
						stallAWTThread = true;
						updateOpenURLs();
						stallAWTThread = false;
						selectButton.setEnabled(true);
						selectBox.setEnabled(true);
						pruneBox.setEnabled(true);
						versionpruneBox.setEnabled(true);
						selectBox.setSelected(false);
						pruneBox.setSelected(false);
						versionpruneBox.setSelected(false);
						isStructure = false;
						System.out.println("Finished Version Pruning!");
						String op = "Finished " + opPerformed;
						JOptionPane.showMessageDialog(this, op , "Message", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
				}
			}
		}
		public void actionPerformed(ActionEvent evt)
		{
			String label;
			if(evt.getSource() instanceof JComboBox && !stallAWTThread)
			{
				label = (String)( ( JComboBox)evt.getSource() ).getSelectedItem();
				
				if( label == null || label.equals(NO_ARCH_OPEN) )
				{
					return;
				}
				openUrl = label;
				updateSelectors( );
				validate();
				repaint();				
			}	
			if(evt.getSource() instanceof JMenuItem)
			{
				label = ( ( JMenuItem )evt.getSource( ) ).getText();
				if(label.equals( "Load Table" ))
					handleOpenFile();
				if(label.equals( "Save Table" ))
					handleSaveFile();
				if(label.equals( "Exit" ))
				{
					exitSelectorDriver();
				}
			}
			if(evt.getSource() instanceof JButton)
			{
				label = ( ( JButton )evt.getSource( ) ).getText();
				if(tabbedPane.getSelectedIndex() == 1)
					isStructure = true;
				if(label.equals( "Start" ) )
				{
					String url = ( String )urlComboBoxModel.getSelectedItem();
					UIDGenerator generator = new UIDGenerator();
					String tempTarget = UIDGenerator.generateUID();
					if(url.equals( NO_ARCH_OPEN ) )
					{
						JOptionPane.showMessageDialog(this, "No architecture selected.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					else if( targetUrlField.getText( ).equals( "" ) )
					{
						JOptionPane.showMessageDialog(this, "No target URL/URN specified."
							, "Error", JOptionPane.ERROR_MESSAGE );
						return;
					}
					else if ( jTable.getRowCount() == 0 && selectBox.isSelected())
					{
						JOptionPane.showMessageDialog(this, "Symbol Table empty. No selection will be done."
							, "Error", JOptionPane.ERROR_MESSAGE );
						return;
					}
					else if( !isStructure && !typeSelector.isVersionSelected() && (selectBox.isSelected() || pruneBox.isSelected()))
					{
						JOptionPane.showMessageDialog(this, "No Type/Version specified."
							, "Error", JOptionPane.ERROR_MESSAGE );
						return;
					}
					else if( isStructure && structureSelector.getStructureID() == null && (selectBox.isSelected() || pruneBox.isSelected()))
					{
						JOptionPane.showMessageDialog(this, "No Structure specified."
							, "Error", JOptionPane.ERROR_MESSAGE );
						return;
					}
					else if(!selectBox.isSelected() && !pruneBox.isSelected() && !versionpruneBox.isSelected())
					{
						JOptionPane.showMessageDialog(this, "Please choose to select, prune, version prune, or a combination of the three."
							, "Error", JOptionPane.ERROR_MESSAGE );
						return;
					}
					//now decide which selections were made
					String id = null;
					if(isStructure && structureSelector != null)
						id = structureSelector.getStructureID();
					else
					{
						if(typeSelector.isVersionSelected())
							id = typeSelector.getTypeId();
					}
					//if select is chosen
					if(selectBox.isSelected())
					{
						opPerformed = "Select";
						//if select and versionprune is choen
						if(versionpruneBox.isSelected())
						{
							//if select, prune, and versionprune is chosen
							if(pruneBox.isSelected())
							{
								pruneAllCheck = true;
								multipleChoice = true;
								opPerformed = "Select + Prune + Version Prune";
							}
							else
							{
								multipleChoice = true;
								versionPruneCheck = true;
								opPerformed = "Select + Version Prune";
							}
							//if select and prune is chosen
						}
						if(pruneBox.isSelected() && !versionpruneBox.isSelected())
						{
							pruneCheck = true;
							multipleChoice = true;
							opPerformed = "Select + Prune";
						}
						stallMessageProcessor = true;
						selectBox.setEnabled(false);
						pruneBox.setEnabled(false);
						versionpruneBox.setEnabled(false);
						selectButton.setEnabled(false);
						PerformArchSelectorMessage selectMessage = null;
						if(pruneCheck == true || pruneAllCheck == true)
						{
							selectMessage = new PerformArchSelectorMessage(url, tempTarget, 
							selectorDriverID, symbolTable, id, isStructure);
							messageParameters = new storeMessageParameters(url, tempTarget, id, isStructure);
						}
						else
						{
							selectMessage = new PerformArchSelectorMessage(url, targetUrlField.getText(), selectorDriverID,
								symbolTable, id, isStructure);
							messageParameters = new storeMessageParameters(url,  targetUrlField.getText(), id, isStructure);
						}
						System.out.println("Started Selecting!");
						sendToAll(selectMessage, topIface);
						return;
					}
					if(pruneBox.isSelected())
					{
						opPerformed = "Prune";
						if(versionpruneBox.isSelected())
						{
							multipleChoice = true;
							versionPruneCheck = true;
							opPerformed = "Prune + Version Prune";
						}
						stallMessageProcessor = true;
						selectBox.setEnabled(false);
						pruneBox.setEnabled(false);
						versionpruneBox.setEnabled(false);
						selectButton.setEnabled(false);
						PerformArchPrunerMessage pruneMessage = null;
						if(versionPruneCheck == true)
						{
							pruneMessage = new PerformArchPrunerMessage(url, tempTarget, selectorDriverID, id, isStructure);
							messageParameters = new storeMessageParameters(url, tempTarget, id, isStructure);
						}
						else
						{
							pruneMessage = new PerformArchPrunerMessage(url, targetUrlField.getText(), selectorDriverID, id, isStructure);
							messageParameters = new storeMessageParameters(url, targetUrlField.getText(), id, isStructure);
						}
						System.out.println("Started Pruning!");
						sendToAll(pruneMessage, topIface);
						return;
					}
					if(versionpruneBox.isSelected())
					{
						opPerformed = "Version Prune";
						stallMessageProcessor = true;
						selectBox.setEnabled(false);
						pruneBox.setEnabled(false);
						versionpruneBox.setEnabled(false);
						selectButton.setEnabled(false);
						PerformPruneVersionsMessage pruneVersionMessage = new PerformPruneVersionsMessage(url, targetUrlField.getText(), selectorDriverID);	
						messageParameters = new storeMessageParameters(url, targetUrlField.getText(), id, isStructure);
						sendToAll(pruneVersionMessage, topIface);
						System.out.println("Started Version Pruning!");
						return;
					}
				}
				if (label.equals("Add/Modify"))
				{
					String type = ( String )openSymbolTypesList.getSelectedItem();
					String valueType = "";
					String name = nameField.getText();
					String value = valueField.getText();
					String newValue = "";
					if (type.equals("string"))
					{
						StringBuffer buffer = new StringBuffer(50);
						buffer.append("\"" + value + "\""); 
						newValue = buffer.toString();
					}
					
					else if (type.equals("date"))
					{
						StringBuffer buffer = new StringBuffer(50);
						buffer.append("#" + value + "#");
						newValue = buffer.toString();
					}	
					else
						newValue = value;
					// try block tries to add the name-value pair to both the symbolTable and the JTable
					try
					{
						int rowCount = tableModel.getRowCount();
						boolean check = symbolTable.isPresent(name);
						if(name.length() != 0)
						{
							if (check == false || rowCount == 0)
							{
								symbolTable.put(name, newValue);
								if (type.equals("date"))
								{
									value = DateFormat.getDateTimeInstance().format(symbolTable.getDate(name));		
								}
								tableModel.addRow(new Object[]{name, value, type});
							}
							else
							{
								for (int i = 0; i < rowCount; i++)
								{
									String symName = (String)tableModel.getValueAt(i,0);
									if(symName.equals(name))
									{
										symbolTable.put(name, newValue);
										tableModel.removeRow(i);
										if (type.equals("date"))
										{
											value = DateFormat.getDateTimeInstance().format(symbolTable.getDate(name));
										}
										tableModel.addRow(new Object[]{name, value, type});
									}
								}	
							}
						}
					}
					catch(NoSuchTypeException e)
					{
						JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					catch(Exception e)
					{
						JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
						e.printStackTrace( );
						return;
					}
					nameField.setText(null);
					valueField.setText(null);
				}
				if(label.equals("Remove"))
				{
					removeSymbol();
				}
				if(label.equals("Clear"))
				{
					symbolTable.clearTable();
					tableModel.setRowCount(0);
				}
				
			}
		}
		public void removeSymbol()
		{
			int[] selectedRows = jTable.getSelectedRows();
			for(int i = (selectedRows.length - 1); i >= 0; i--)
			{
				String symName = (String)tableModel.getValueAt(selectedRows[i],0);
				tableModel.removeRow(selectedRows[i]);
				symbolTable.remove(symName);
				
			}
		}
		/**
		 * If the user chooses to open a symbol table through the FILE menu, the file is parsed
		 * and loaded into the JTable of the GUI. The JTable represents the Symbol Table visually to the user.
		 */
		public void handleOpenFile()
		{
			try
			{
				JFileChooser chooser = new JFileChooser(".");
				int returnVal = chooser.showOpenDialog(this);
				if(returnVal == JFileChooser.APPROVE_OPTION)
				{
					//clear existing table
					symbolTable.clearTable();
					tableModel.setRowCount(0);
					
					inputFileField = chooser.getSelectedFile().getAbsolutePath();
					symbolTable = FileParser.createTable( inputFileField );
					String[] temp = symbolTable.getVariables();
					String name = "";
					Object value = null;
					String valueType = "";
					int intType;
					for (int i = 0; i < symbolTable.size(); i++)
					{
						name = temp[i];
						intType = symbolTable.getType(name);
						if (intType == SymbolTable.DOUBLE)
						{
							valueType = "double";
							value = symbolTable.getDouble(name);
						}
						if (intType == SymbolTable.STRING)
						{
							valueType = "string";
							value = symbolTable.getString(name);
						}
						if (intType == SymbolTable.DATE)
						{
							valueType = "date";
							value = DateFormat.getDateTimeInstance().format(symbolTable.getDate(name));
						}
						tableModel.addRow(new Object[]{name, value, valueType});
					}
					
				}
			}
			catch( Exception e)
			{
				JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace( );
				return;
			}	
		}
		/**
		 * If the user chooses to open a save table through the FILE menu, the file is parsed
		 * into a String array. The Symbol Table method writeFile is then called and the document is saved.
		 */
		public void handleSaveFile()
		{
			JFileChooser chooser = new JFileChooser(".");
			int returnVal = chooser.showSaveDialog(this);
			if(returnVal == JFileChooser.APPROVE_OPTION)
			{
				String filePath = chooser.getSelectedFile().getAbsolutePath();
				String[]  temp = symbolTable.listTable();
				symbolTable.writeFile(filePath, temp);
			}
		}		
	}
}



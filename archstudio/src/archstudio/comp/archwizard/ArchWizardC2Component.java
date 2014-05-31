// ****************************************************************************
// ** Class Name: ArchWizardC2Component                                      **
// **                                                                        **
// ** Description: This component implements wizard functionality for        **
// **              architectural editing tasks, and is intended to be used   **
// **              in parallel with an editor.  Some conflicts may emerge    **
// **              depending on how much the editor abstracts away the       **
// **              "down'n'dirty" editing details, but a lot of effort has   **
// **              been made to make ArchWizard fully customizable to avoid  **
// **              such conflicts.                                           **
// **                                                                        **
// ** 01/14/2003 - John Georgas[jgeorgas@ics.uci.edu]                        **
// **              Initial development.                                      **
// ** 02/27/2003 - John Georgas[jgeorgas@ics.uci.edu]                        **
// **              Version 0.1 alpha released.                               **
// **                                                                        **
// ** Implemented Feature List:  - Support for saving/loading of settings.   **
// **                            - Support for user options.                 **
// **             			     - Support for user preference persistence,  **
// **                  			   applied to options and GUI settings.      **
// **                            - Start-up time improvement.                **
// **                            - TypeMatching extension available.         **
// **                            - NamingConventions extension available.    **
// **                                                                        **
// ** Copyright 2003, by the University of California, Irvine.               **
// ** ALL RIGHTS RESERVED.                                                   **
// ****************************************************************************

package archstudio.comp.archwizard;

// C2 imports
import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

// Archstudio imports
import archstudio.invoke.*;
import archstudio.comp.archedit.*;
import archstudio.awextensions.*;

// xArch utilities imports
import edu.uci.ics.xarchutils.*;
import edu.uci.ics.widgets.*;
import edu.uci.isr.xarch.*;

// Java imports
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.lang.*;
import java.lang.reflect.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import java.util.prefs.Preferences;

public class ArchWizardC2Component extends AbstractC2DelegateBrick implements c2.fw.Component, InvokableBrick {
	// Class variables
	public static final String PRODUCT_NAME = "ArchWizard";
	public static final String PRODUCT_VERSION = "0.1 alpha";
	public static final String DEVELOPER = "John Georgas";
	public static final String DEVELOPER_EMAIL = "jgeorgas@ics.uci.edu";
	private ArchWizardFrame frame;
	private XArchFlatInterface xarch;
	private DefaultTableModel tableModel;
	private Vector extensions;
    
    // *************************************************
    // ** Class Name: ExtensionStatusMessageProcessor **
    // *************************************************
    class ExtensionStatusMessageProcessor implements MessageProcessor {
    	public void handle(Message m) {
    		if (m instanceof ArchWizardExtensionStatusMessage) {
    			handleStatusEvent((ArchWizardExtensionStatusMessage)m);
    		}
    	}
    }
    		
	
	// Class constructor
	public ArchWizardC2Component(Identifier id) {
		super(id);
		xarch = (XArchFlatInterface)EBIWrapperUtils.addExternalService(this, topIface, XArchFlatInterface.class);
		addMessageProcessors();
		InvokeUtils.deployInvokableService(this, bottomIface, PRODUCT_NAME, "An architectural editing wizard.");
		tableModel = new DefaultTableModel();
		extensions = new Vector();
		tableModel.addColumn("ExtensionActivationButton");
		tableModel.addColumn("ExtensionDescription");
	}
	
	public void invoke(InvokeMessage im) {
		if (im.getServiceName().equals(PRODUCT_NAME)) {
			if (frame == null) {
				frame = new ArchWizardFrame(PRODUCT_NAME, PRODUCT_VERSION);
			} else {
				frame.requestFocus();
				JOptionPane.showMessageDialog(null, "Only one instance of ArchWizard may operate at a time (though enough requests may lead to that changing).", "ArchWizard Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private void addMessageProcessors() {
		XArchEventProvider xarchEventProvider = 
			(XArchEventProvider)EBIWrapperUtils.createStateChangeProviderProxy(this, topIface,
			XArchEventProvider.class);
		
		XArchFlatListener flatListener = new XArchFlatListener(){
			public void handleXArchFlatEvent(XArchFlatEvent evt){
				handleStateChangeEvent(evt);
			}
		};
		xarchEventProvider.addXArchFlatListener(flatListener);

		this.addMessageProcessor(new ExtensionStatusMessageProcessor());
	}
	
	private void handleStateChangeEvent(XArchFlatEvent evt) {
		if (frame != null) {
			if (evt.getIsAttached()) {
				int eventType = evt.getEventType();
				Object target = evt.getTarget();
				if (target instanceof ObjRef) {
					if ((eventType == XArchFlatEvent.ADD_EVENT) || (eventType == XArchFlatEvent.SET_EVENT) || (eventType == XArchFlatEvent.PROMOTE_EVENT)) {
						frame.handleEvent((ObjRef)target);
					}
				}
			}
		}
	}
	
	private void handleStatusEvent(ArchWizardExtensionStatusMessage evt) {
		if (frame != null) {
			Identifier id = evt.getExtensionID();
			String desc = evt.getDescription();
			int status = evt.getStatus();
			if (status == ArchWizardExtensionStatusListing.STATUS_UNAVAILABLE) {
				if (extensions.contains(id)) {
					removeExtComp(id);
				}
			} else {
				if (extensions.contains(id)) {
					replaceExtComp(id, desc, status);
				} else {
					addExtComp(id, desc, status);
				}
			}
		}
	}
	
	private synchronized void removeExtComp(Identifier id) {
		int pos = extensions.indexOf(id);
		if (pos != -1) {
			tableModel.removeRow(pos);
			extensions.remove(id);
		}
	}
	
	private synchronized void replaceExtComp(Identifier id, String desc, int status) {
		int pos = extensions.indexOf(id);
		if (pos != -1) {
			java.awt.Component[] rowComps = buildRowComponents(id, desc, status);
			for (int i = 0; i < rowComps.length; i++) {
				tableModel.setValueAt(rowComps[i], pos, i);
			}
		}
	}
	
	private synchronized void addExtComp(Identifier id, String desc, int status) {
		extensions.add(id);
		tableModel.addRow(buildRowComponents(id, desc, status));
	}
	
	private java.awt.Component[] buildRowComponents(Identifier id, String desc, int status) {
		JButton b = new JButton();
		final Identifier eID = id;
		if (status == ArchWizardExtensionStatusListing.STATUS_ACTIVE) {
			b.setText("<HTML><CENTER>Status: <FONT COLOR=008800>Active</FONT><BR>Click to Deactivate</CENTER></HTML>");
			b.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					ArchWizardExtensionSetStatusMessage m = new ArchWizardExtensionSetStatusMessage(eID, ArchWizardExtensionStatusListing.STATUS_INACTIVE);
					sendRequest(m);
				}
			});
		} else {
			b.setText("<HTML><CENTER>Status: <FONT COLOR=008800>Inactive</FONT><BR>Click to Activate</CENTER></HTML>");
			b.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					ArchWizardExtensionSetStatusMessage m = new ArchWizardExtensionSetStatusMessage(eID, ArchWizardExtensionStatusListing.STATUS_ACTIVE);
					sendRequest(m);
				}
			});
		}
		JPanelUL bPanel = new JPanelUL(b);
		JLabel idLabel = new JLabel("<HTML>" + id.toString() + "</HTML>");
		idLabel.setFont(WidgetUtils.SANSSERIF_BOLD_MEDIUM_FONT);
		JLabel descLabel = new JLabel("<HTML>" + desc + "</HTML>");
		descLabel.setFont(WidgetUtils.SANSSERIF_PLAIN_MEDIUM_FONT);
		JPanelEWL descPanel = new JPanelEWL(descLabel);
		JExpandableDataWidget edw = new JExpandableDataWidget(idLabel, new JPanelUL(descPanel));
		return new java.awt.Component[]{bPanel, edw};
	}
	
	// *********************************
    // ** Class Name: ArchWizardFrame **
    // *********************************
	class ArchWizardFrame extends JFrame implements ActionListener, ChangeListener {
		// Class variables
		public static final double HEIGHT_RATIO = 0.33;
		public static final double WIDTH_RATIO = 0.33;
		public static final double X_POS_RATIO = 0.25;
		public static final double Y_POS_RATIO = 0.25;
		public static final String LOAD_ACTION = "Load";
		public static final String SAVE_ACTION = "Save";
		public static final String ABOUT_ACTION = "About";
		public static final String OPTIONS_ACTION = "Options";
		public static final String CUST_TITLE = "Customization";
		public static final String EXT_TITLE = "Extension Manager";
		public static final String OVERWRITE_KEY = "Overwrite On Promote";
		public static final String HEIGHT_KEY = "Height Key";
		public static final String WIDTH_KEY = "Width Key";
		public static final String X_KEY = "X Key";
		public static final String Y_KEY = "Y Key";
		public static final String VERT_DIVIDER_KEY = "Vertical Divider Key";
		public static final String HORZ_DIVIDER_KEY = "Horizontal Divider Key";
		public static final String DIR_KEY = "Directory Key";
		public static final String XARCH_CNAME = "edu.uci.isr.xarch.XArchImpl";
		public static final String AW_FILE_EXT = "aws";
		public static final int V_TABLE_GAP = 5;
		public static final int H_TABLE_GAP = 5;
		public static final int INSET = 5;
		private AWOptions options;
		private JTabbedPane mainPane;
		private JSplitPane editPane;
		private JSplitPane awCustomizationPane;
		private JTree tree;
		private JPanel treePane;
		private JPanel attribPane;
		private JPanel elemPane;
		private JScrollPane awExtensionManagerPane;
		private JStaticTable staticTable;
		final JFileChooser fc = new JFileChooser();
		protected Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		
		// *********************************
		// ** Class Name: AWWindowAdapter **
		// *********************************
		class AWWindowAdapter extends WindowAdapter {
			public void windowClosing(WindowEvent e) {
				close();
			}
		}
		
		// *****************************************
		// ** Class Name: AWTreeSelectionListener **
		// *****************************************
		class AWTreeSelectionListener implements TreeSelectionListener {
			public void valueChanged(TreeSelectionEvent e) {
				refreshView((AWTreeNode)tree.getLastSelectedPathComponent());
			}
		}
		
		// ******************************
		// ** Class Name: AWFileFilter **
		// ******************************
		class AWFileFilter extends javax.swing.filechooser.FileFilter {
			private String ext;
			
			// Class constructor
			public AWFileFilter(String extension) {
				ext = extension;
			}
			
			public String getDescription() {
				if ((ext != null) && (ext != "")) {
					return "." + ext;
				} else {
					return "No Files.";
				}
			}
			
			public boolean accept(File f) {
				if (f != null) {
					if (f.isDirectory()) {
						return true;
					}
					String fn = f.getName();
					String temp = (fn.substring(fn.lastIndexOf('.') + 1, fn.length())).toLowerCase();
					if (temp.equals(ext)) {
						return true;
					}
				}
				return false;
			}
		}
				
		// ***************************
		// ** Class Name: AWOptions **
		// ***************************
		class AWOptions extends JDialog implements ActionListener {
			public static final double HEIGHT_RATIO = 0.15;
			public static final double WIDTH_RATIO = 0.15;
			public static final String APPLY_ACTION = "Apply";
			public static final String CANCEL_ACTION = "Cancel";
			private JPanel top;
			private Hashtable opTable;
			private Hashtable opToolTip;
			
			// Class constructor
			public AWOptions(JFrame owner) {
				super(owner, "Options", true);
				setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
				getContentPane().setLayout(new FlowLayout());
				opTable = new Hashtable();
				opToolTip = new Hashtable();
				opTable.put((Object)OVERWRITE_KEY, (Object)(new Boolean(prefs.getBoolean(OVERWRITE_KEY, false))));
				opToolTip.put((Object)OVERWRITE_KEY, (Object)"If selected, additions of a promoted element will overwrite those of its parent element.");
				init();
			}
			
			private void init() {
				JPanel main = new JPanel(new GridLayout(2, 1));
				top = new JPanel(new GridLayout(opTable.size(), 2));
				for (Enumeration e = opTable.keys(); e.hasMoreElements();) {
					Object key = e.nextElement();
					if (opTable.get(key) instanceof Boolean) {
						JCheckBox cBox = new JCheckBox((String)key, ((Boolean)(opTable.get(key))).booleanValue());
						cBox.setToolTipText((String)opToolTip.get(key));
						top.add(cBox);
					}
				}
				JPanel bottom = new JPanel();
				JButton ok = new JButton("Apply");
				ok.setActionCommand(APPLY_ACTION);
				ok.addActionListener(this);
				bottom.add(ok);
				JButton cancel = new JButton("Cancel");
				cancel.setActionCommand(CANCEL_ACTION);
				cancel.addActionListener(this);
				bottom.add(cancel);
				main.add(top);
				main.add(bottom);
				getContentPane().add(main);
				Dimension screenSize = getToolkit().getScreenSize();
				setSize((int)(screenSize.getWidth() * WIDTH_RATIO), (int)(screenSize.getHeight() * HEIGHT_RATIO));
			}
			
			private boolean isSelected(JCheckBox cb) {
				Object[] gso = cb.getSelectedObjects();
				if (gso == null) {
					return false;
				}
				return true;
			}
			
			public void actionPerformed(ActionEvent e) {
				String ac = e.getActionCommand();
				if (ac.equals(APPLY_ACTION)) {
					java.awt.Component[] comps = top.getComponents();
					for (int i = 0; i < comps.length; i++) {
						if (comps[i] instanceof JCheckBox) {
							JCheckBox cb = (JCheckBox)comps[i];
							opTable.put((Object)(cb.getText()), (Object)(new Boolean(isSelected(cb))));
						}
					}
				}
				setVisible(false);
			}
			
			public boolean overwriteSelected() {
				return ((Boolean)(opTable.get((Object)OVERWRITE_KEY))).booleanValue();
			}
		}
		
		// ****************************
		// ** Class Name: AWTreeNode **
		// ****************************
		class AWTreeNode extends DefaultMutableTreeNode {
			// Class variables
			private MutableTreeNode parent;
			private Class target;
			private String name;
			private JPanel attribPanel;
			private JPanel elemPanel;
			private Hashtable[] settings = {new Hashtable(), new Hashtable(), new Hashtable()};
			
			// ***********************************
			// ** Class Name: AWSingleElemPanel **
			// ***********************************
			class AWSingleElemPanel extends JPanel implements ChangeListener {
				private String elemName;
				private JCheckBox check;
				
				// Class constructor
				public AWSingleElemPanel(String eName, boolean set) {
					super();
					elemName = eName;
					JPanel namePane = new JPanel(new BorderLayout());
					namePane.add(new JLabel("<HTML><CENTER><I>Element: </I><BR>" + elemName + "</CENTER></HTML>"));
					this.add(namePane);
					check = new JCheckBox("<HTML><I>Check for automatic addition</I></HTML>", set);
					check.addChangeListener(this);
					this.add(check);
				}
				
				public boolean isSelected() {
					Object[] gso = check.getSelectedObjects();
					if (gso == null) {
						return false;
					}
					return true;
				}
				
				public void stateChanged(ChangeEvent e) {
					settings[1].put(elemName, new Boolean(isSelected()));
				}
				
				public String getElemName() {
					return elemName;
				}
			}
				
			// **********************************
			// ** Class Name: AWMultiElemPanel **
			// **********************************
			class AWMultiElemPanel extends AWSingleElemPanel implements ActionListener, ChangeListener {
				public static final String ADD_ACTION = "Add";
				public static final String SUB_ACTION = "Sub";
				public static final int MAX_ELEM = 999;
				public static final int MIN_ELEM = 0;
				public static final int TEXT_FIELD_COLUMNS = 3;
				private JTextField field;
				
				// Class constructor
				public AWMultiElemPanel(String eName, boolean set, int count) {
					super(eName, set);
					field = new JTextField(Integer.toString(count), TEXT_FIELD_COLUMNS);
					field.setEditable(false);
					this.add(field);
					JButton add = new JButton("+");
					add.setActionCommand(ADD_ACTION);
					add.addActionListener(this);
					JButton sub = new JButton("-");
					sub.setActionCommand(SUB_ACTION);
					sub.addActionListener(this);
					this.add(add);
					this.add(sub);
				}
				
				public int getCount() {
					return Integer.parseInt(field.getText());
				}
				
				public void actionPerformed(ActionEvent e) {
					try {
						int val = Integer.parseInt(field.getText());
						String action = e.getActionCommand();
						if (action.equals(ADD_ACTION)) {
							if (val < MAX_ELEM) {
								field.setText(Integer.toString(++val));
							}
						} else if (action.equals(SUB_ACTION)) {
							if (val > MIN_ELEM) {
								field.setText(Integer.toString(--val));
							}
						}
						Object[] arr = {new Boolean(isSelected()), new Integer(Integer.parseInt(field.getText()))};
						settings[2].put(getElemName(), arr);
					} catch (NumberFormatException nfe) {
						JOptionPane.showMessageDialog(null, "Value entered must be an integer.  Value re-set to 0.", "ArchWizard Error", JOptionPane.ERROR_MESSAGE);
						System.err.println(nfe);
						field.setText("0");
					}
				}
				
				public void stateChanged(ChangeEvent e) {
					Object[] arr = {new Boolean(isSelected()), new Integer(Integer.parseInt(field.getText()))};
					settings[2].put(getElemName(), arr);
				}
			}
			
			// *************************************
			// ** Class Name: AWSingleAttribPanel **
			// *************************************
			class AWSingleAttribPanel extends JPanel implements ActionListener {
				public static final int TEXT_FIELD_COLUMNS = 20;
				public static final String UPDATE_ACTION = "Update";
				private String attName;
				private String defaultValue;
				private JLabel defaultValLabel;
				private JTextField textField;
				
				// Class constructor
				public AWSingleAttribPanel(String attribName, String dv) {
					super();
					attName = attribName;
					defaultValue = dv;
					defaultValLabel = new JLabel();
					updateDefValueLabel();
					JPanel namePane = new JPanel(new BorderLayout());
					namePane.add("Center", new JLabel("<HTML><CENTER><I>Attribute: </I><BR>" + attName + "</CENTER></HTML>"));
					JPanel defaultValPane = new JPanel();
					defaultValPane.add(new JLabel("<HTML><B><I>Default Value: </I></B></HTML>"));
					defaultValPane.add(defaultValLabel);
					defaultValPane.add(new JLabel("<HTML><B><I>Enter New Default: </I></B></HTML>"));
					textField = new JTextField(TEXT_FIELD_COLUMNS);
					defaultValPane.add(textField);
					JButton b = new JButton("Update Default");
					b.setActionCommand(UPDATE_ACTION);
					b.addActionListener(this);
					defaultValPane.add(b);
					this.add(namePane);
					this.add(defaultValPane);
				}
				
				public void actionPerformed(ActionEvent e) {
					String ac = e.getActionCommand();
					if (ac.equals(UPDATE_ACTION)) {
						defaultValue = textField.getText();
						settings[0].put(attName, defaultValue);
						textField.setText("");
						updateDefValueLabel();
					}
				}	
				
				private void updateDefValueLabel() {
					if (defaultValue.equals("")) {
						defaultValLabel.setText("<HTML> [No default value specified] </HTML>");
					} else {
						defaultValLabel.setText("<HTML>" + defaultValue + "</HTML>");
					}
					repaint();
				}
			}
			
			// Class constructor
			public AWTreeNode(MutableTreeNode p, String n, Class t) {
				parent = p;
				target = t;
				name = n;
				if (p == null) {
					// This is the root node
					Collection c = ContextInfoFinder.getTopLevelElements();
					for (Iterator it = c.iterator(); it.hasNext();) {
						ElementInfo ei = (ElementInfo)it.next();
						Class pc = getImplClass(ei.getClassName());
						this.add(new AWTreeNode(this, ei.getDisplayName(), pc));
						handlePromotions(this, pc, true);
					}
					attribPanel = new JPanel();
					attribPanel.setLayout(new BoxLayout(attribPanel, BoxLayout.Y_AXIS));
					elemPanel = new JPanel();
					elemPanel.setLayout(new BoxLayout(elemPanel, BoxLayout.Y_AXIS));
					attribPanel.add(new JLabel("No customization possible on top-level, root element."));
					elemPanel.add(new JLabel("No customization possible on top-level, root element."));
				} else {
					if (t != null) {
						Method[] methods = t.getMethods();
						for (int i = 0; i < methods.length; i++) {
							String name = methods[i].getName();
							if (name.startsWith("add")) {
								String className = ((methods[i].getParameterTypes())[0]).getName();
								if (className.startsWith("edu.uci.isr.xarch")) {
									Class pc = getImplClass(((methods[i].getParameterTypes())[0]).getName());
									if (!detectCycle(t)) {
										this.add(new AWTreeNode(this, name.substring(3, name.length()), pc));
										Object[] arr = {new Boolean(false), new Integer(0)};
										settings[2].put(name.substring(3, name.length()), arr);
										handlePromotions(this, pc, true);
									}
								}
							} else if (name.startsWith("set") && !(name.startsWith("setDOMNode")) && !(name.startsWith("setXArch"))) {
								String className = ((methods[i].getParameterTypes())[0]).getName();
								if (className.startsWith("edu.uci.isr.xarch")) {
									if (!detectCycle(t)) {
										Class pc = getImplClass(className);
										this.add(new AWTreeNode(this, name.substring(3, name.length()), pc));
										settings[1].put(name.substring(3, name.length()), new Boolean(false));
										handlePromotions(this, pc, false);
									}
								} else {
									settings[0].put(name.substring(3, name.length()), "");
								}
							}
						}
					}
				}
			}
			
			private boolean detectCycle(Class c) {
				MutableTreeNode node = this.getAWParent();
				while (node != null) {
					if (c.equals(((AWTreeNode)node).getTargetClass())) {
						return true;
					}
					node = ((AWTreeNode)node).getAWParent();
				}
				return false;
			}
			
			private Class getImplClass(String icn) {
				Class ret = null;
				String[] pieces = icn.split("I", 2);
				String className = pieces[0] + pieces[1] + "Impl";
				try {
					ret = Class.forName(className);
				} catch (ClassNotFoundException e) {
					JOptionPane.showMessageDialog(null, "Class name " + className + " was not found!", "ArchWizard Error", JOptionPane.ERROR_MESSAGE);
					System.out.println(e);
				}
				return ret;
			}
			
			private void handlePromotions(AWTreeNode node, Class c, boolean multi) {
				if (!detectCycle(c)) {
					Collection col = ContextInfoFinder.getAvailablePromotions(c);
					for (Iterator it = col.iterator(); it.hasNext();) {
						ElementInfo ei = (ElementInfo)it.next();
						Class pc = getImplClass(ei.getClassName());
						node.add(new AWTreeNode(node, ei.getDisplayName(), pc));
						handlePromotions(node, pc, multi);
						if (multi) {
							node.addMulti(ei.getDisplayName());
						} else {
							node.addSingle(ei.getDisplayName());
						}
					}
				}
			}
			
			public AWTreeNode findInChildren(String s) {
				for (Enumeration e = this.children(); e.hasMoreElements();) {
					AWTreeNode node = (AWTreeNode)(e.nextElement());
					if (s.equals(node.getTargetClass().getName())) {
						return node;
					}
				}
				return null;
			}
			
			private String getContextName(String cName) {
				String temp = cName;
				temp = temp.substring(0, temp.lastIndexOf('.'));
				return temp.substring(temp.lastIndexOf('.') + 1, temp.length());
			}
			
			public void processTasks(ObjRef ref) {
				for (Enumeration e = settings[0].keys(); e.hasMoreElements();) {
					String key = (String)(e.nextElement());
					String value = (String)(settings[0].get(key));
					if (!value.equals("")) {
						xarch.set(ref, key, value);
					}
				}
				for (Enumeration e = settings[1].keys(); e.hasMoreElements();) {
					String key = (String)(e.nextElement());
					Boolean value = (Boolean)(settings[1].get(key));
					if (value.booleanValue()) {
						setElem(ref, key);
					}
				}
				for (Enumeration e = settings[2].keys(); e.hasMoreElements();) {
					String key = (String)(e.nextElement());
					Object[] arr = (Object[])(settings[2].get(key));
					if (((Boolean)(arr[0])).booleanValue()) {
						addElem(ref, key, ((Integer)(arr[1])).intValue());
					}
				}
			}
			
			private void setElem(ObjRef parent, String elemName) {
				for (Enumeration e = this.children(); e.hasMoreElements();) {
					AWTreeNode n = (AWTreeNode)(e.nextElement());
					if (elemName.equals(n.toString())) {
						String temp = n.getTargetClass().getName();
						temp = temp.substring(temp.lastIndexOf('.') + 1, temp.length() - 4);
						// Check on the overwrite option
						if (!options.overwriteSelected()) {
							Object poss = xarch.get(parent, elemName);
							if (poss != null) {
								return;
							}
						}
						xarch.set(parent, elemName, xarch.create(xarch.createContext(xarch.getXArch(parent), getContextName(n.getTargetClass().getName())), temp));	
					}
				}
			}
			
			private void addElem(ObjRef parent, String elemName, int count) {
				for (Enumeration e = this.children(); e.hasMoreElements();) {
					AWTreeNode n = (AWTreeNode)(e.nextElement());
					if (elemName.equals(n.toString())) {
						for (int i = 0; i < count; i++) {
							String temp = n.getTargetClass().getName();
							temp = temp.substring(temp.lastIndexOf('.') + 1, temp.length() - 4);
							xarch.add(parent, elemName, xarch.create(xarch.createContext(xarch.getXArch(parent), getContextName(n.getTargetClass().getName())), temp));
						}
						return;
					}
				}
			}
			
			public String toString() {
				return name;
			}
			
			public Class getTargetClass() {
				return target;
			}
			
			public MutableTreeNode getAWParent() {
				return parent;
			}
			
			public void addMulti(String name) {
				Object[] arr = {new Boolean(false), new Integer(0)};
				settings[2].put(name, arr);
			}
			
			public void addSingle(String name) {
				settings[1].put(name, new Boolean(false));
			}
			
			public JPanel getAttribPanel() {
				if (attribPanel == null) {
					attribPanel = new JPanel();
					attribPanel.setLayout(new BoxLayout(attribPanel, BoxLayout.Y_AXIS));
				}
				attribPanel.removeAll();
				if (settings[0].isEmpty()) {
					attribPanel.add(new JLabel("No attributes on this node."));
				} else {
					for (Enumeration e = settings[0].keys(); e.hasMoreElements();) {
						String key = (String)(e.nextElement());
						attribPanel.add(new AWSingleAttribPanel(key, (String)(settings[0].get(key))));
					}
				}
				return attribPanel;
			}
			
			public JPanel getElemPanel() {
				if (elemPanel == null) {
					elemPanel = new JPanel();
					elemPanel.setLayout(new BoxLayout(elemPanel, BoxLayout.Y_AXIS));
				}
				elemPanel.removeAll();
				for (Enumeration e = settings[1].keys(); e.hasMoreElements();) {
					String key = (String)(e.nextElement());
					elemPanel.add(new AWSingleElemPanel(key, ((Boolean)(settings[1].get(key))).booleanValue()));
				}
				for (Enumeration e = settings[2].keys(); e.hasMoreElements();) {
					String key = (String)(e.nextElement());
					Object[] arr = (Object[])(settings[2].get(key));
					elemPanel.add(new AWMultiElemPanel(key, ((Boolean)arr[0]).booleanValue(), ((Integer)arr[1]).intValue()));
				}
				if (elemPanel.getComponentCount() == 0) {
					elemPanel.add(new JLabel("No elements on this node."));
				}
				return elemPanel;
			}
			
			public Hashtable[] getSettings() {
				return settings;
			}
			
			public void setSettings(Hashtable table, int i) {
				settings[i] = table;
			}
		}
		
		// Class constructor
		public ArchWizardFrame(String name, String version) {
			super(name + " " + version);
			initGUI();
			paint(getGraphics());
		}
		
		private void initGUI() {
			setFileChooserSettings();
			addWindowListener(new AWWindowAdapter());
			createMenuBar();
			options = new AWOptions(this);
			mainPane = new JTabbedPane();
			treePane = createTreePane();
			attribPane = new JPanel();
			attribPane.setBorder(BorderFactory.createTitledBorder("Attributes"));
			JScrollPane topSP = new JScrollPane(attribPane);
			elemPane = new JPanel();
			elemPane.setBorder(BorderFactory.createTitledBorder("Children Elements"));
			JScrollPane bottomSP = new JScrollPane(elemPane);
			editPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSP, bottomSP);
			editPane.setDividerLocation(prefs.getInt(VERT_DIVIDER_KEY, -1));
			editPane.setResizeWeight(0.5);
			awCustomizationPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePane, editPane);
			awCustomizationPane.setDividerLocation(prefs.getInt(HORZ_DIVIDER_KEY, -1));
			awCustomizationPane.setResizeWeight(0.5);
			staticTable = new JStaticTable(tableModel, H_TABLE_GAP, V_TABLE_GAP);			
			staticTable.setDrawRowSplits(true);
			staticTable.setEmptyViewComponent(new JLabel("<HTML><CENTER>No extension components detected.</CENTER></HTML>"));
			awExtensionManagerPane = new JScrollPane(new JPanelIS(new JPanelUL(staticTable), new Insets(INSET, INSET, INSET, INSET)));
			mainPane.add(CUST_TITLE, awCustomizationPane);
			mainPane.add(EXT_TITLE, awExtensionManagerPane);
			mainPane.addChangeListener(this);
			getContentPane().add(mainPane);
			Dimension screenSize = getToolkit().getScreenSize();
			setBounds(prefs.getInt(X_KEY, (int)(screenSize.getWidth() * X_POS_RATIO)), prefs.getInt(Y_KEY, (int)(screenSize.getHeight() * Y_POS_RATIO)),
					  prefs.getInt(WIDTH_KEY, (int)(screenSize.getWidth() * WIDTH_RATIO)), prefs.getInt(HEIGHT_KEY, (int)(screenSize.getHeight() * HEIGHT_RATIO)));		  
			setVisible(true);
		}
		
		private void storePrefs() {
			try {
				prefs.clear();
			} catch (Exception e) {
				System.out.println(e);
			}
			prefs.putBoolean(OVERWRITE_KEY, options.overwriteSelected());
			prefs.putInt(X_KEY, getX());
			prefs.putInt(Y_KEY, getY());
			prefs.putInt(WIDTH_KEY, getWidth());
			prefs.putInt(HEIGHT_KEY, getHeight());
			prefs.putInt(VERT_DIVIDER_KEY, editPane.getDividerLocation());
			prefs.putInt(HORZ_DIVIDER_KEY, awCustomizationPane.getDividerLocation());
			prefs.put(DIR_KEY, (fc.getCurrentDirectory()).getName());
		}
		
		private void close() {
			storePrefs();
			frame = null;
			this.setVisible(false);
			this.dispose();
		}
		
		private JPanel createTreePane() {
			JPanel treePane = new JPanel();
			try {
				Class xarchClass = Class.forName(XARCH_CNAME);
				DefaultTreeModel treeModel = new DefaultTreeModel(new AWTreeNode(null, "xArch", xarchClass));
				tree = new JTree(treeModel);
				tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
				tree.setRootVisible(true);
				tree.setEditable(false);
				tree.addTreeSelectionListener(new AWTreeSelectionListener());
				treePane.setLayout(new BorderLayout());
				treePane.add("Center", new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
				return treePane;
			} catch (ClassNotFoundException e) {
				System.err.println(e);
				JOptionPane.showMessageDialog(this, "Class name " + XARCH_CNAME + " was not found!  Shutting down.", "ArchWizard Error", JOptionPane.ERROR_MESSAGE);
				close();
			}
			return treePane;
		}
		
		private void createMenuBar() {
			JMenuBar bar = new JMenuBar();
			JMenu settings = new JMenu("Settings");
			JMenu config = new JMenu("Configure");
			JMenu about = new JMenu("About");
			createMenuItem(settings, "Load...", 'L', LOAD_ACTION, this, "Best-effort load tree configuration settings from a file.");
			createMenuItem(settings, "Save...", 'S', SAVE_ACTION, this, "Save tree configuration settings to a file.");
			createMenuItem(about, "About...", 'A', ABOUT_ACTION, this, "Display product information.");
			createMenuItem(config, "Options...", 'O', OPTIONS_ACTION, this, "View and modify custom settings.");
			bar.add(settings);
			bar.add(config);
			bar.add(about);
			setJMenuBar(bar);
		}
		
		private void createMenuItem(JMenu in, String name, char mnemonic, String ac, ActionListener al, String toolTip) {
			JMenuItem temp = new JMenuItem(name, (int)mnemonic);
			temp.setActionCommand(ac);
			temp.addActionListener(al);
			temp.setToolTipText(toolTip);
			in.add(temp);
		}
		
		private void refreshView(AWTreeNode node) {
			if (node != null) {
				attribPane.removeAll();
				attribPane.add(node.getAttribPanel());
				elemPane.removeAll();
				elemPane.add(node.getElemPanel());
				frame.repaint();
			}
		}
		
		public void stateChanged(ChangeEvent e) {
			if ((mainPane.getTitleAt(mainPane.getSelectedIndex())).equals(EXT_TITLE)) {
				ArchWizardExtensionGetStatusMessage m = new ArchWizardExtensionGetStatusMessage();
				sendRequest(m);
			}
		}
		
		public void actionPerformed(ActionEvent e) {
			String action = e.getActionCommand();
			if (action.equals(OPTIONS_ACTION)) {
				options.setLocationRelativeTo(options.getOwner());
				options.setVisible(true);
			} else if (action.equals(ABOUT_ACTION)) {
				displayAboutDialog();
			} else if (action.equals(SAVE_ACTION)) {
				int ret = fc.showSaveDialog(this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					String path = fc.getSelectedFile().getPath();
					if ((fc.getSelectedFile().getPath()).endsWith(AW_FILE_EXT)) {
						saveSettings(fc.getSelectedFile());
					} else {
						saveSettings(new File(fc.getSelectedFile().getPath() + "." + AW_FILE_EXT));
					}
				}
			} else if (action.equals(LOAD_ACTION)) {
				int ret = fc.showOpenDialog(this);
				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					loadSettings(file);
				}
			}
		}
		
		private String getNodeKey(AWTreeNode node) {
			TreeNode[] arr = node.getPath();
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < arr.length; i++) {
				buf.append(((AWTreeNode)(arr[i])).getTargetClass().getName());
			}
			return buf.toString();
		}
		
		private void saveSettings(File file) {
			try {
				ObjectOutputStream os = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
				Hashtable temp = new Hashtable();
				AWTreeNode root = (AWTreeNode)(tree.getModel().getRoot());
				for (Enumeration e = root.breadthFirstEnumeration(); e.hasMoreElements();) {
					AWTreeNode node = (AWTreeNode)(e.nextElement());
					temp.put(getNodeKey(node), node.getSettings());
				}
				os.writeObject(temp);
				os.flush();
				os.close();
				JOptionPane.showMessageDialog(this, "Settings saved to " + file.getName(), "Save Successful", JOptionPane.INFORMATION_MESSAGE);
			} catch (FileNotFoundException fnfe) {
				System.err.println(fnfe);
				JOptionPane.showMessageDialog(this, "File " + file.getName() + " not found.  Saving aborted.", "ArchWizard Error", JOptionPane.ERROR_MESSAGE);
			} catch (IOException ioe) {
				System.err.println(ioe);
				JOptionPane.showMessageDialog(this, "Error writing to file.  Saving aborted.", "ArchWizard Error", JOptionPane.ERROR_MESSAGE);
			}	
		}
		
		private void loadSettings(File file) {
			try {
				ObjectInputStream is = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
				Hashtable temp = (Hashtable)(is.readObject());
				is.close();
				insertInTree(temp);
				refreshView((AWTreeNode)tree.getLastSelectedPathComponent());
				JOptionPane.showMessageDialog(this, "Settings loaded.", "Load Successful", JOptionPane.INFORMATION_MESSAGE);
			} catch (ClassNotFoundException cnfe) {
				System.err.println(cnfe);
				JOptionPane.showMessageDialog(this, "Error reading from file.", "ArchWizard Error", JOptionPane.ERROR_MESSAGE);
			} catch (FileNotFoundException fnfe) {
				System.err.println(fnfe);
				JOptionPane.showMessageDialog(this, "File " + file.getName() + " not found.  Loading aborted.", "ArchWizard Error", JOptionPane.ERROR_MESSAGE);
			} catch (IOException ioe) {
				System.err.println(ioe);
				JOptionPane.showMessageDialog(this, "Error reading from file.  Saving aborted.", "ArchWizard Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		private void insertInTree(Hashtable settings) {
			AWTreeNode root = (AWTreeNode)(tree.getModel().getRoot());
			for (Enumeration nodes = root.breadthFirstEnumeration(); nodes.hasMoreElements();) {
				AWTreeNode node = (AWTreeNode)(nodes.nextElement());
				String nodeKey = getNodeKey(node);
				if (settings.containsKey(nodeKey)) {
					Hashtable[] newArr = (Hashtable[])(settings.get(nodeKey));
					Hashtable[] oldArr = node.getSettings();
					for (int i = 0; i < newArr.length; i++) {
						for (Enumeration keys = oldArr[i].keys(); keys.hasMoreElements();) {
							Object key = keys.nextElement();
							if (newArr[i].containsKey(key)) {
								oldArr[i].put(key, newArr[i].get(key));
							}
						}
						node.setSettings(oldArr[i], i);
					}
				}
			}
		}
		
		private void setFileChooserSettings() {
			fc.setFileFilter(new AWFileFilter(AW_FILE_EXT));
			File temp = fc.getCurrentDirectory();
			fc.setCurrentDirectory(new File(prefs.get(DIR_KEY, temp.getName())));
		}
				
		private void displayAboutDialog() {
			JLabel product = new JLabel(PRODUCT_NAME + " " + PRODUCT_VERSION);
			JLabel author = new JLabel("Authored by " + DEVELOPER + " [" + DEVELOPER_EMAIL + "] .");
			JLabel copy = new JLabel("Copyright 2003, University of California, Irvine");
			JPanel aboutPane = new JPanel(new GridLayout(3, 1));
			aboutPane.add(product);
			aboutPane.add(author);
			aboutPane.add(copy);
			JOptionPane.showMessageDialog(this, aboutPane, "About", JOptionPane.INFORMATION_MESSAGE);
		}
		
		private String capFirst(String s) {
			return (s.substring(0, 1)).toUpperCase() + s.substring(1, s.length());
		}
		
		private AWTreeNode findInTree(ObjRef ref) {
			String type = xarch.getType(ref);
			ObjRef[] xarchPath = xarch.getAllAncestors(ref);
			AWTreeNode node = (AWTreeNode)((tree.getModel()).getRoot());
			for (int i = xarchPath.length - 2; i >= 0; i--) {
				node = node.findInChildren(xarch.getType(xarchPath[i]));
				if (node == null) {
					JOptionPane.showMessageDialog(this, "Tree path matching error.  Actions aborted.", "ArchWizard Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
			}
			return node;
		}
		
		private void handleEvent(ObjRef elemAddedRef) {
			AWTreeNode node = findInTree(elemAddedRef);
			if (node != null) {
				node.processTasks(elemAddedRef);
			}
		}
	}
}
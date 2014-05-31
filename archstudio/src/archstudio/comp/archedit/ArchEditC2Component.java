package archstudio.comp.archedit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import xacmleditor.PolicyEditorPanel;
import archstudio.comp.aac.AACC2Component;
import archstudio.editors.EditorUtils;
import archstudio.editors.FocusEditorMessage;
import archstudio.invoke.InvokableBrick;
import archstudio.invoke.InvokeMessage;
import archstudio.invoke.InvokeUtils;
import c2.fw.Identifier;
import c2.fw.LifecycleAdapter;
import c2.fw.Message;
import c2.fw.MessageProcessor;
import c2.legacy.AbstractC2DelegateBrick;
import c2.pcwrap.EBIWrapperUtils;
import edu.uci.ics.widgets.WidgetUtils;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchEventProvider;
import edu.uci.ics.xarchutils.XArchFileEvent;
import edu.uci.ics.xarchutils.XArchFileListener;
import edu.uci.ics.xarchutils.XArchFlatEvent;
import edu.uci.ics.xarchutils.XArchFlatInterface;
import edu.uci.ics.xarchutils.XArchFlatListener;
import edu.uci.isr.xarch.security.IPolicySetType;

public class ArchEditC2Component extends AbstractC2DelegateBrick implements c2.fw.Component, InvokableBrick{
	public static final String PRODUCT_NAME = "ArchEdit";
	public static final String SERVICE_NAME = "Editors/ArchEdit";
	
	protected Vector openWindows;
	protected XArchFlatInterface xarch;
	
	public ArchEditC2Component(Identifier id){
		super(id);
		EditorUtils.registerEditor(this, topIface, PRODUCT_NAME);
		xarch = (XArchFlatInterface)EBIWrapperUtils.addExternalService(this, topIface, XArchFlatInterface.class);
		openWindows = new Vector();
		this.addLifecycleProcessor(new ArchEditLifecycleProcessor());
		this.addMessageProcessor(new FocusEditorMessageProcessor());

		XArchEventProvider xarchEventProvider = 
			(XArchEventProvider)EBIWrapperUtils.createStateChangeProviderProxy(this, topIface,
			XArchEventProvider.class);
		
		XArchFileListener fileListener = new XArchFileListener(){
			public void handleXArchFileEvent(XArchFileEvent evt) {
				handleFileEvent(evt);
			}
		};
		xarchEventProvider.addXArchFileListener(fileListener);
		
		XArchFlatListener flatListener = new XArchFlatListener(){
			public void handleXArchFlatEvent(XArchFlatEvent evt){
				handleStateChangeEvent(evt);
			}
		};
		xarchEventProvider.addXArchFlatListener(flatListener);

		//EBIWrapperUtils.addThreadMessageProcessor(this, new MessageProcessor[]{ new StateChangeMessageProcessor()});
		//addMessageProcessor(new DebugMessageProcessor());
		InvokeUtils.deployInvokableService(this, bottomIface, 
			SERVICE_NAME, 
			"A tree-based graphical xADL 2.0 syntax-directed architecture editor");
	}

	class DebugMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			System.out.println(m);
			System.out.println();
		}
	}
	
	class ArchEditLifecycleProcessor extends LifecycleAdapter{
		public void end(){
			for(int i = 0; i < openWindows.size(); i++){
				((JFrame)openWindows.elementAt(i)).setVisible(false);
				((JFrame)openWindows.elementAt(i)).dispose();
			}
		}
	}
	
	public void invoke(InvokeMessage im){
		if(im.getServiceName().equals(SERVICE_NAME)){
			String url = im.getArchitectureURL();

			if(url == null){
				newWindow();
			}
			else{
				newWindow(url);
			}
		}
	}
	
	class FocusEditorMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof FocusEditorMessage){
				FocusEditorMessage fem = (FocusEditorMessage)m;
				
				if(!EditorUtils.appliesToEditor(PRODUCT_NAME, fem)){
					return;
				}
				
				ObjRef xArchRef = fem.getXArchRef();
				ObjRef[] refs = fem.getRefs();
				ObjRef ref = null;
				if((refs != null) && (refs.length > 0)){
					ref = refs[0];
				}
				int focusType = fem.getFocusType();
				if(focusType == FocusEditorMessage.FOCUS_EXISTING_DOCS){
					ArchEditFrame f = getWindow(xArchRef);
					if(f == null){
						//No open window on that document.
						return;
					}
					if(ref != null){
						f.showRef(ref);
					}
				}
				else if(focusType == FocusEditorMessage.FOCUS_OPEN_DOCS){
					if(openWindows.size() == 0){
						return;
					}
					ArchEditFrame f = getWindow(xArchRef);
					if(f == null){
						f = newWindow(xArchRef);
					}
					if(ref != null){
						f.showRef(ref);
					}
				}
				else if(focusType == FocusEditorMessage.FOCUS_OPEN_EDITORS){
					ArchEditFrame f = getWindow(xArchRef);
					if(f == null){
						f = newWindow(xArchRef);
					}
					if(ref != null){
						f.showRef(ref);
					}
				}
			}
		}
	}

	//-----------------------------------------
	
	public void handleFileEvent(XArchFileEvent evt){
		if(evt.getEventType() == XArchFileEvent.XARCH_CLOSED_EVENT){
			synchronized(openWindows){
				int size = openWindows.size();
				for(int i = (size - 1); i >= 0; i--){
					ArchEditFrame f = (ArchEditFrame)openWindows.elementAt(i);
					ObjRef fXArchRef = f.getDocumentSource();
					if(fXArchRef != null){
						if(fXArchRef.equals(evt.getXArchRef())){
							f.doClose();
						}
					}
				}
			}			
		}
		else if(evt.getEventType() == XArchFileEvent.XARCH_RENAMED_EVENT){
			synchronized(openWindows){
				int size = openWindows.size();
				for(int i = (size - 1); i >= 0; i--){
					ArchEditFrame f = (ArchEditFrame)openWindows.elementAt(i);
					ObjRef fXArchRef = f.getDocumentSource();
					if(fXArchRef != null){
						if(fXArchRef.equals(evt.getXArchRef())){
							f.setWindowTitle(evt.getAsURL());
						}
					}
				}
			}
		}
	}
	
	public void handleStateChangeEvent(XArchFlatEvent evt){
		ArchEditFrame hasFocus = null;
		synchronized(openWindows){
			int size = openWindows.size();
			for(int i = (size - 1); i >= 0; i--){
				ArchEditFrame f = (ArchEditFrame)openWindows.elementAt(i);
				if(f.hasFocus()){
					hasFocus = f;
					break;
				}
				else if(f.getFocusOwner() != null){
					hasFocus = f;
					break;
				}
			}
			for(int i = (size - 1); i >= 0; i--){
				ArchEditFrame f = (ArchEditFrame)openWindows.elementAt(i);
				if(!f.isShowing()){
					openWindows.removeElementAt(i);
				}
				else{
					f.handleXArchFlatEvent(evt);
				}
			}
			if(hasFocus != null){
				hasFocus.requestFocus();
			}
		}
	}
	
	public ArchEditFrame newWindow(){
		synchronized(openWindows){
			ArchEditFrame f = new ArchEditFrame();
			openWindows.addElement(f);
			return f;
		}
	}

	public ArchEditFrame newWindow(String url){
		ArchEditFrame f = newWindow();
		f.openURI(url);
		return f;
	}

	public ArchEditFrame newWindow(ObjRef xArchRef){
		ArchEditFrame f = newWindow();
		f.openXArch(xArchRef);
		return f;
	}
	
	private ArchEditFrame getWindow(ObjRef xArchRef){
		synchronized(openWindows){
			int size = openWindows.size();
			for(int i = (size - 1); i >= 0; i--){
				ArchEditFrame f = (ArchEditFrame)openWindows.elementAt(i);
				ObjRef frameXArchRef = f.getDocumentSource();
				if(frameXArchRef != null){
					if(frameXArchRef.equals(xArchRef)){
						return f;
					}
				}
			}
		}
		return null;
	}
	
	public void showWindow(String uri, String id){
		synchronized(openWindows){
			ObjRef xArchRef = xarch.getOpenXArch(uri);
			if(xArchRef != null){
				int size = openWindows.size();
				for(int i = (size - 1); i >= 0; i--){
					ArchEditFrame f = (ArchEditFrame)openWindows.elementAt(i);
					ObjRef frameXArchRef = f.getDocumentSource();
					if(xArchRef.equals(frameXArchRef)){
						f.showId(id);
						return;
					}
				}
			}
			//We didn't find a window with that URL
			ArchEditFrame newFrame = newWindow(uri);
			newFrame.showId(id);
		}
	}

	private static int initialWindowPositionOffset = 0;
	
	class ArchEditFrame extends JFrame implements ActionListener, TreeSelectionListener, MouseListener, XArchFlatListener{
		
		private JSplitPane splitPanel;
		private JPanel treePanel;
		private JPanel contentPanel;
		private ArchJTree tree;
		
		private ArchTreeNode currentEditPaneNode = null;
		
		//private static final int SRC_NOT_OPEN = 100;
		//private static final int SRC_FILE = 200;
		//private static final int SRC_URL = 300;
		//private int documentSourceType;
		private ObjRef documentSource;
		
		private boolean showInlineIDs = true;
		private boolean showInlineDescriptions = false;
		
		public ArchEditFrame(){
			super();
			archstudio.Branding.brandFrame(this);
			setWindowTitle(null);
			init();
			
			WidgetUtils.validateAndRepaintInAWTThread(this);
			//repaint();
		}
		
		private void closeWindow(){
			openWindows.removeElement(this);
			this.setVisible(false);
			this.dispose();
		}
		
		protected synchronized void setWindowCursor(Cursor cursor){
			this.setCursor(cursor);
		}
		
		protected synchronized Cursor getWindowCursor(){
			return this.getCursor();
		}
		
		private void init(){
			Toolkit tk = getToolkit();
			Dimension screenSize = tk.getScreenSize();
			double xSize = (screenSize.getWidth() * 0.66);
			double ySize = (screenSize.getHeight() * 0.66);
			double xPos = (screenSize.getWidth() * 0.10);
			double yPos = (screenSize.getHeight() * 0.10);

			//Allow subsequent windows to cascade out a bit (up to 5 steps)
			xPos += (initialWindowPositionOffset * 16);
			yPos += (initialWindowPositionOffset * 16);
			initialWindowPositionOffset++;
			if(initialWindowPositionOffset == 5){
				initialWindowPositionOffset = 0;
			}
			
			JMenuBar mb = new JMenuBar();
			JMenu mArchitecture = new JMenu("Architecture");
			WidgetUtils.setMnemonic(mArchitecture, 'A');
			
			JMenu mEdit = new JMenu("Edit");
			WidgetUtils.setMnemonic(mEdit, 'E');
			
			JMenu mView = new JMenu("View");
			WidgetUtils.setMnemonic(mView, 'V');
			
			JMenuItem miNewWindow = new JMenuItem("New Window");
			miNewWindow.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.Event.CTRL_MASK));
			WidgetUtils.setMnemonic(miNewWindow, 'N');
			miNewWindow.addActionListener(this);
			
			JMenuItem miHookOpenArchitecture = new JMenuItem("Open Architecture...");
			miHookOpenArchitecture.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.Event.CTRL_MASK));
			WidgetUtils.setMnemonic(miHookOpenArchitecture, 'O');
			miHookOpenArchitecture.addActionListener(this);

			JMenuItem miClose = new JMenuItem("Close Window");
			WidgetUtils.setMnemonic(miClose, 'C');
			miClose.addActionListener(this);
			
			JMenuItem miDuplicate = new JMenuItem("Duplicate");
			miDuplicate.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.Event.CTRL_MASK));
			WidgetUtils.setMnemonic(miDuplicate, 'D');
			miDuplicate.addActionListener(this);
			
			JMenuItem miFindByID = new JMenuItem("Find by ID...");
			miFindByID.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.Event.CTRL_MASK));
			WidgetUtils.setMnemonic(miFindByID, 'F');
			miFindByID.addActionListener(this);
			
			JCheckBoxMenuItem miViewInlineIDs = new JCheckBoxMenuItem("In-line IDs");
			WidgetUtils.setMnemonic(miViewInlineIDs, 'I');
			miViewInlineIDs.setSelected(showInlineIDs);
			miViewInlineIDs.addActionListener(this);
			
			JCheckBoxMenuItem miViewInlineDescriptions = new JCheckBoxMenuItem("In-line Descriptions");
			WidgetUtils.setMnemonic(miViewInlineDescriptions, 'D');
			miViewInlineDescriptions.setSelected(showInlineDescriptions);
			miViewInlineDescriptions.addActionListener(this);
			
			mArchitecture.add(miNewWindow);
			mArchitecture.add(miHookOpenArchitecture);
			mArchitecture.add(new JSeparator());
			mArchitecture.add(miClose);
			
			mEdit.add(miDuplicate);			
			mEdit.add(new JSeparator());
			mEdit.add(miFindByID);
			
			mView.add(miViewInlineIDs);
			mView.add(miViewInlineDescriptions);
			
			mb.add(mArchitecture);
			mb.add(mEdit);
			mb.add(mView);

			JMenu mAccessControl = new JMenu("Access Control");
			miAccessing = new JMenuItem("Set as Accessing");
			miAccessed = new JMenuItem("Set as Accessed");
			miCheck = new JMenuItem("Check Access Control");
			miClear = new JMenuItem("Clear");
			WidgetUtils.setMnemonic(mAccessControl, 'A');
			WidgetUtils.setMnemonic(miAccessing, 'A');
			WidgetUtils.setMnemonic(miAccessed, 'S');
			WidgetUtils.setMnemonic(miCheck, 'K');
			WidgetUtils.setMnemonic(miClear, 'C');
			miAccessing.addActionListener(this);
			miAccessed.addActionListener(this);
			miClear.addActionListener(this);
			miCheck.addActionListener(this);
			miCheck.setEnabled(false);
			mAccessControl.add(miAccessing);
			mAccessControl.add(miAccessed);
			mAccessControl.add(miCheck);
			mAccessControl.add(miClear);
			mb.add(mAccessControl);
			
			this.setJMenuBar(mb);
			
			treePanel = new JPanel();
			
			contentPanel = new JPanel();
			
			splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePanel, contentPanel);
			splitPanel.setDividerLocation((int)(xSize * 0.33));
			
			//tree = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode("[No Architecture Open]", false)));
			tree = new ArchJTree(new DefaultTreeModel(new ArchTreeNode(null, null, "[No Architecture Open]")));
			DefaultTreeSelectionModel tsm = new DefaultTreeSelectionModel();
			tsm.setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
			//tsm.addTreeSelectionListener(this);
			tree.setSelectionModel(tsm);
			tree.addMouseListener(this);
			
			tree.setRootVisible(true);
			treePanel.setLayout(new BorderLayout());
			treePanel.add("Center", new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
			
			this.getContentPane().add(splitPanel);
			
			documentSource = null;

			this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			this.addWindowListener(
				new WindowAdapter(){
					public void windowClosing(WindowEvent evt){
						closeWindow();
					}
				}
			);
			
			setVisible(true);
			setSize((int)xSize, (int)ySize);
			setLocation((int)xPos, (int)yPos);
			setVisible(true);
			paint(getGraphics());
		}
		
		public void doClose(){
			DefaultTreeModel tm = (DefaultTreeModel)tree.getModel();
			tm.setRoot(new ArchTreeNode(null, null, "[No Architecture Open]"));
			
			setWindowTitle(null);
			showEditPane(null);
			documentSource = null;
		}
		
		public ObjRef getDocumentSource(){
			return documentSource;
		}
		
		private void setWindowTitle(String openFile){
			StringBuffer title = new StringBuffer();
			title.append(PRODUCT_NAME);
			//title.append(' ');
			//title.append(PRODUCT_VERSION);
			if(openFile == null){
				title.append(" - [None]");
			}
			else{
				title.append(" - [").append(openFile).append("]");
			}
			setTitle(title.toString());
		}
		
		public void valueChanged(TreeSelectionEvent evt){
			TreePath path = evt.getPath();
			if(path == null){
			}
			else{
				Object o = path.getLastPathComponent();
				if(o instanceof ArchTreeNode){
					//System.out.println("Showing edit pane.");
					showEditPane((ArchTreeNode)o);
				}
			}
		}
		
		public void mouseClicked(MouseEvent e){
			if(e.getClickCount() == 2){
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				if(selRow != -1) {
					Object o = selPath.getLastPathComponent();
					if((o != null) && (o instanceof ArchTreeNode)){
						ArchTreeNode node = (ArchTreeNode)o;
						if(edu.uci.isr.xarch.instance.IXMLLink.class.isAssignableFrom(node.getTargetClass())){
							ObjRef target = node.getTarget();
							//System.out.println("XArchPath: " + xarch.getXArchPath(target));
							//System.out.println("Target is: " + target);
							String href = (String)xarch.get(target, "href");
							try{
								ObjRef thisXArch = xarch.getXArch(target);
								ObjRef linkTarget = xarch.resolveHref(thisXArch, href);
								//System.out.println("LinkTarget is: " + linkTarget);
								
								if(linkTarget == null){
									JOptionPane.showMessageDialog(this, "Invalid link target.", "Error", JOptionPane.ERROR_MESSAGE);
									return;
								}
								if(!xarch.isAttached(linkTarget)){
									JOptionPane.showMessageDialog(this, "Invalid link target.", "Error", JOptionPane.ERROR_MESSAGE);
									return;
								}
								
								ObjRef linkTargetXArch = xarch.getXArch(linkTarget);
								if(linkTargetXArch.equals(thisXArch)){
									ObjRef[] pathToRoot = xarch.getAllAncestors(linkTarget);
									tree.showNode(pathToRoot);
								}
								else{
									int ps = href.indexOf("#");
									String url = href.substring(0, ps);
									String id = href.substring(ps+1);
									showWindow(url, id);
								}
							}
							catch(Exception ex){
								JOptionPane.showMessageDialog(this, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
								ex.printStackTrace();
								return;
							}
						}
					}
				}
			}
		}
		
		public void mousePressed(MouseEvent e){
			if(checkPopup(e)){
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				tree.setSelectionPath(selPath);
				if(selPath != null){
					Object o = selPath.getLastPathComponent();
					if(o instanceof ArchTreeNode){
						//System.out.println("Showing edit pane.");
						showEditPane((ArchTreeNode)o);
					}
				}
			}
		}
	
		public void mouseReleased(MouseEvent e){
			//System.out.println("Mouse Released.");
			if(checkPopup(e)){
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				tree.setSelectionPath(selPath);
			}
			TreePath selPath = tree.getSelectionPath();
			if((selPath != null) && (currentEditPaneNode != null) && (selPath.getLastPathComponent().equals(currentEditPaneNode))){
				return;
			}
			if(selPath != null){
				Object o = selPath.getLastPathComponent();
				if(o instanceof ArchTreeNode){
					//System.out.println("Showing edit pane.");
					showEditPane((ArchTreeNode)o);
				}
			}
		}
		
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		
		public boolean checkPopup(MouseEvent e){
			if(e.isPopupTrigger()){
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				if(selRow != -1) {
					if(e.getClickCount() == 1){
						Object o = selPath.getLastPathComponent();
						if((o != null) && (o instanceof ArchTreeNode)){
							ArchJPopupMenu popup = new ArchJPopupMenu((ArchTreeNode)o);
							popup.show(e.getComponent(), e.getX(), e.getY());
							return true;
						}
					}
				}
			}
			return false;
		}
		
		private String[] getAttributeNames(Class oc){
			Hashtable setMethods = new Hashtable();
			Hashtable getMethods = new Hashtable();
			
			Method[] methods = oc.getMethods();
			for(int i = 0; i < methods.length; i++){
				Method m = methods[i];
				String methodName = m.getName();
				if(methodName.startsWith("get")){
					if(!methodName.startsWith("getAll")){
						Class c = m.getDeclaringClass();
						if(c.getName().startsWith("edu.uci.isr.xarch.")){
							int mods = m.getModifiers();
							if(Modifier.isPublic(mods)){
								if(!Modifier.isStatic(mods)){
									if(!methodName.equals("getXArch")){
										if(!methodName.equals("getDOMNode")){
											if(!methodName.equals("getTypeMetadata")){
												if(!methodName.equals("getInstanceMetadata")){
													if(m.getReturnType().equals(java.lang.String.class)){
														//System.out.println("Got a GET method: " + methodName);
														getMethods.put(methodName, m);
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				else if(methodName.startsWith("set")){
					Class c = m.getDeclaringClass();
					if(c.getName().startsWith("edu.uci.isr.xarch.")){
						int mods = m.getModifiers();
						if(Modifier.isPublic(mods)){
							if(!Modifier.isStatic(mods)){
								if(m.getParameterTypes().length == 1){
									if(m.getParameterTypes()[0].equals(java.lang.String.class)){
										//System.out.println("Got a SET method: " + methodName);
										setMethods.put(methodName, m);
									}
								}
							}
						}
					}
				}
			}
			
			Vector retVals = new Vector();
			for(Enumeration en = getMethods.keys(); en.hasMoreElements(); ){
				String getMethodName = (String)en.nextElement();
				String typeOfThing = getMethodName.substring(3);
				if(setMethods.get("set" + typeOfThing) != null){
					retVals.addElement(typeOfThing);
				}
			}
			
			String[] retArr = new String[retVals.size()];
			retVals.copyInto(retArr);
			return retArr;
		}
		
		public void showEditPane(){
			//Shows the edit pane for the currently selected node.
			TreePath path = tree.getSelectionPath();
			if(path == null){
				showEditPane(null);
				return;
			}
			Object o = path.getLastPathComponent();
			if(o == null){
				return;
			}
			else if(!(o instanceof ArchTreeNode)){
				return;
			}
			showEditPane((ArchTreeNode)o);
		}
		
		public void showEditPane(ArchTreeNode node){
			currentEditPaneNode = node;
			
			if((node == null) || (node.getTarget() == null)){
				contentPanel.removeAll();
				this.setVisible(true);
				this.paint(getGraphics());
				return;
			}
			
			int divLoc = ((int)splitPanel.getLeftComponent().getSize().getWidth()) + 1;
			
			ObjRef target = node.getTarget();
			Class targetClass = node.getTargetClass();
			String[] attributeNames = getAttributeNames(targetClass);
			
			contentPanel.removeAll();
			Box editPane = Box.createVerticalBox();
			
			final JLabel headerLabel = new JLabel("<HTML><FONT SIZE=+1><B>Element: " + c2.util.TextHtmlifier.htmlify(node.toString()) + "</B></FONT></HTML>");
			JPanel headerPanel = new JPanel(){
				public Dimension getMaximumSize(){
					return new Dimension((int)super.getMaximumSize().getWidth(), (int)headerLabel.getPreferredSize().getHeight());
				}
			};
			headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			headerPanel.add(headerLabel);
			editPane.add(headerPanel);
			
			if(attributeNames.length == 0){
				editPane.add(new JLabel("[No attributes on this item.]"));
				editPane.add(Box.createGlue());
			}
			else{
				editPane.add(new HorizontalLine());
				
				if(edu.uci.isr.xarch.instance.IXMLLink.class.isAssignableFrom(targetClass)){
					editPane.add(new LinkDropPanel(node));
					editPane.add(new HorizontalLine());
				}
				
				for(int i = 0; i < attributeNames.length; i++){
					editPane.add(new AttributeValueEditPanel(node, attributeNames[i]));
					editPane.add(new HorizontalLine());
					//editPane.add(new JSeparator());
				}
				editPane.add(Box.createGlue());
			}
			
			JPanel insetEditPanel = new JPanel(){
				public Insets getInsets(){
					return new Insets(10, 10, 10, 10);
				}
			};
			insetEditPanel.setLayout(new BorderLayout());
			insetEditPanel.add("Center", editPane);
			
			contentPanel.setLayout(new BorderLayout());
			contentPanel.add("Center", new JScrollPane(insetEditPanel));
			contentPanel.setVisible(true);
			splitPanel.setDividerLocation(divLoc);
			this.setVisible(true);
			this.paint(getGraphics());
		}
		
		public void refreshEditPane(){
			if(currentEditPaneNode != null){
				showEditPane(currentEditPaneNode);
			}
		}
		
		public void handleNewWindow(){
			newWindow();
		}
		
		public void openURI(String uri){
			try{
				//System.out.println("Entering parse call.");
				ObjRef xArchRef = xarch.getOpenXArch(uri);
				openXArch(xArchRef);
			}
			catch(Exception e){
				JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return;
			}	
		}
		
		public void openXArch(ObjRef xArchRef){
			try{
				String uri = xarch.getXArchURI(xArchRef);
				ArchTreeNode rootNode = new ArchTreeNode(null, xArchRef, "xArch");
				((DefaultTreeModel)tree.getModel()).setRoot(rootNode);
				((DefaultTreeModel)tree.getModel()).reload();
				tree.setRootVisible(true);
				setWindowTitle(uri);
				repaint();
				documentSource = xArchRef;
			}
			catch(Exception e){
				JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return;
			}	
		}
		
		public void handleHookOpenArchitecture(){
			String[] urls = xarch.getOpenXArchURIs();
			if(urls.length == 0){
				JOptionPane.showMessageDialog(this, "No architectures open.", "No Architectures", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String result = (String)JOptionPane.showInputDialog(this, "Choose an Architecture", "Choose an Architecture", 
				JOptionPane.QUESTION_MESSAGE, null, urls, urls[0]);

			if(result == null){
				return;
			}
			openURI(result);
			/*
			try{
				ObjRef xarchRef = xarch.getOpenXArch(result);

				ArchTreeNode rootNode = new ArchTreeNode(null, xarchRef, "xArch");
				((DefaultTreeModel)tree.getModel()).setRoot(rootNode);
				((DefaultTreeModel)tree.getModel()).reload();
				tree.setRootVisible(true);
				setWindowTitle(result);
				repaint();
				documentSourceType = SRC_URL;
				documentSource = result;
			}
			catch(Exception e){
				e.printStackTrace();
				return;
			}	
			*/		
		}
		
		public void handleFindById(){
			String inputValue = JOptionPane.showInputDialog(this, "Enter ID:", "Find by ID", JOptionPane.QUESTION_MESSAGE);
			
			if(inputValue == null){
				return;
			}
			showId(inputValue);
		}
		
		public void handleDuplicate(){
			TreePath selPath = tree.getSelectionPath();
			Object o = selPath.getLastPathComponent();
			if((o != null) && (o instanceof ArchTreeNode)){
				ArchTreeNode node = (ArchTreeNode)o;
				if(edu.uci.isr.xarch.IXArch.class.isAssignableFrom(node.getTargetClass())){
					JOptionPane.showMessageDialog(this, "Can't duplicate the xArch node.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				ArchTreeNode parentNode = (ArchTreeNode)node.getParent();
				if(edu.uci.isr.xarch.IXArch.class.isAssignableFrom(parentNode.getTargetClass())){
					JOptionPane.showMessageDialog(this, "Can't duplicate first-level nodes.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				ObjRef target = node.getTarget();
				ObjRef parentTarget = parentNode.getTarget();
				ObjRef targetClone = xarch.cloneElement(target, edu.uci.isr.xarch.IXArchElement.DEPTH_INFINITY);

				String typeOfThing = node.getDisplayName();
				
				try{
					xarch.add(parentTarget, typeOfThing, targetClone);
				}
				catch(Exception e){
					//System.out.println("Tried " + typeOfThing);
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, "Can't make duplicates of that here.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				return;
			}
			JOptionPane.showMessageDialog(this, "Nothing selected.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		public void handleViewInlinesChanged(){
			//((DefaultTreeModel)tree.getModel()).reload();
			ArchTreeNode targetNode = (ArchTreeNode)tree.getModel().getRoot();
			tree.refreshNode(targetNode);
			//tree.invalidate();
			//tree.validate();
			//tree.repaint();
		}
		
		public void showRef(ObjRef ref){
			ArchTreeNode rootNode = (ArchTreeNode)tree.getModel().getRoot();
			ObjRef target = rootNode.getTarget();
			if(target == null){
				JOptionPane.showMessageDialog(this, "No architecture open.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			if(ref == null){
				JOptionPane.showMessageDialog(this, "No such element.", "Not Found", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			else{
				ObjRef[] pathToRoot = xarch.getAllAncestors(ref);
				//System.out.println("Ancestors are: " + c2.util.ArrayPrinter.arrayToString(pathToRoot));
				tree.showNode(pathToRoot);
			}
		}

		public void showId(String id){
			ArchTreeNode rootNode = (ArchTreeNode)tree.getModel().getRoot();
			ObjRef target = rootNode.getTarget();
			if(target == null){
				JOptionPane.showMessageDialog(this, "No architecture open.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			ObjRef ref = xarch.getByID(target, id);
			if(ref == null){
				JOptionPane.showMessageDialog(this, "No such element.", "Not Found", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			else{
				ObjRef[] pathToRoot = xarch.getAllAncestors(ref);
				//System.out.println("Ancestors are: " + c2.util.ArrayPrinter.arrayToString(pathToRoot));
				tree.showNode(pathToRoot);
			}
		}

		JMenuItem miAccessing;
		JMenuItem miAccessed;
		JMenuItem miCheck;
		JMenuItem miClear;
		
		String		accessingInterface = null;
		
		public void handleSetAsAccessing() {
			ObjRef t = currentEditPaneNode.getTarget();
			if (xarch.isInstanceOf(t, "edu.uci.isr.xarch.types.IInterface")) {
				accessingInterface = (String)xarch.get(t, "Id");
				miAccessing.setText("Accessing: " + currentEditPaneNode.toString());
				if (accessedInterface != null)
					miCheck.setEnabled(true);			}
		}
			
		String		accessedInterface = null;
		
		public void handleSetAsAccessed() {
			ObjRef t = currentEditPaneNode.getTarget();
			if (xarch.isInstanceOf(t, "edu.uci.isr.xarch.types.IInterface")) {
				accessedInterface = (String)xarch.get(t, "Id");
				miAccessed.setText("Accessed: " + currentEditPaneNode.toString());
				if (accessingInterface != null)
					miCheck.setEnabled(true);
			}
		}

		public void handleCheck() {
			boolean		result = AACC2Component.checkAccessControl(xarch, 
					documentSource, accessingInterface, accessedInterface);
			if (result) 
				JOptionPane.showMessageDialog(null, "Access is allowed");
			else
				JOptionPane.showMessageDialog(null, "Access is denied");
		}
		
		public void handleClearAccess() {
			accessingInterface = null;
			accessedInterface = null;
			miAccessing.setText("Set as Accessing");
			miAccessed.setText("Set as Accessed");
			miCheck.setEnabled(false);
		}
		
		public void actionPerformed(ActionEvent evt){
			Object src = evt.getSource();
			if(src instanceof JMenuItem){
				if(((JMenuItem)src).getText().equals("New Window")){
					handleNewWindow();
				}
				else if(((JMenuItem)src).getText().equals("Open Architecture...")){
					handleHookOpenArchitecture();
				}
				else if(((JMenuItem)src).getText().equals("Close Window")){
					closeWindow();
				}
				else if(((JMenuItem)src).getText().equals("Find by ID...")){
					handleFindById();
				}
				else if(((JMenuItem)src).getText().equals("Duplicate")){
					handleDuplicate();
				}
				else if(((JMenuItem)src).getText().equals("In-line IDs")){
					showInlineIDs = ((JCheckBoxMenuItem)src).getState();
					handleViewInlinesChanged();
				}
				else if(((JMenuItem)src).getText().equals("In-line Descriptions")){
					showInlineDescriptions = ((JCheckBoxMenuItem)src).getState();
					handleViewInlinesChanged();
				}
				else if(((JMenuItem)src).getText().equals("Set as Accessing")){
					handleSetAsAccessing();
				}
				else if(((JMenuItem)src).getText().equals("Set as Accessed")){
					handleSetAsAccessed();
				}
				else if(((JMenuItem)src).getText().equals("Clear")){
					handleClearAccess();
				}
				else if(((JMenuItem)src).getText().equals("Check Access Control")){
					handleCheck();
				}
			}
		}
		
		public void handleXArchFlatEvent(XArchFlatEvent evt){
			if(!evt.getIsAttached()){
				//If it's not attached, we're not representing it.  Ignore.
				return;
			}
			ArchTreeNode root = (ArchTreeNode)tree.getModel().getRoot();
			ArchTreeNode targetNode = root.resolveRef(evt.getSource());

			if(targetNode == null){
				return;
			}
			int srcType = evt.getSourceType();
			if((srcType == XArchFlatEvent.ATTRIBUTE_CHANGED) || (srcType == XArchFlatEvent.SIMPLE_TYPE_VALUE_CHANGED)){
				tree.refreshNode(targetNode);
				TreePath selectionPath = tree.getSelectionPath();
				if(selectionPath != null){
					ArchTreeNode selectedNode = (ArchTreeNode)selectionPath.getLastPathComponent();
					if(targetNode.equals(selectedNode)){
						refreshEditPane();
					}
				}
				//This fixes a buglet in Swing
				this.repaint();
			}
			else if(srcType == XArchFlatEvent.ELEMENT_CHANGED){
			    if (evt.getEventType() == XArchFlatEvent.PROMOTE_EVENT)
			        targetNode.updateTargetClass();
				targetNode.syncChildren();
				tree.refreshNode(targetNode);

				TreePath selectionPath = null;
				int[] selRows = tree.getSelectionRows();
				if((selRows != null) && (selRows.length > 0)){
					selectionPath = tree.getSelectionPath();
				}
				if(selectionPath != null){
					ArchTreeNode selectedNode = (ArchTreeNode)selectionPath.getLastPathComponent();
					if(targetNode.equals(selectedNode)){
						refreshEditPane();
					}
				}
				else{
					showEditPane(null);
					this.repaint();
				}
			}
		}
		
		class AttributeValueEditPanel extends JPanel implements ActionListener{
			private JPanel leftPanel;
			private JPanel topPanel;
			private Box bottomPanel;
			private boolean multiLine = false;
			private JLabel attrLabel;
			
			private ArchTreeNode node;
			private String attributeName;
			
			private JTextComponent textField;
			private JButton applyButton;
			private JButton clearButton;
			
			public AttributeValueEditPanel(ArchTreeNode node, String attributeName){
				super();
				
				this.node = node;
				this.attributeName = attributeName;
				
				String currentValue = (String)xarch.get(node.getTarget(), attributeName);
				if(currentValue == null){
					currentValue = "[No Value Assigned]";
				}
				
				leftPanel = new JPanel(){
					public Dimension getPreferredSize(){
						return new Dimension((int)attrLabel.getPreferredSize().getWidth() + 10, (int)super.getPreferredSize().getHeight());
					}
				};
					
				topPanel = new JPanel();
				textField = new JTextField(0){
					public Dimension getMaximumSize(){
						return new Dimension((int)super.getMaximumSize().getWidth(), (int)super.getPreferredSize().getHeight());
					}
					public Dimension getPreferredSize(){
						return new Dimension(30, (int)super.getPreferredSize().getHeight());
					}
						
					public Dimension getMinimumSize(){
						return new Dimension(30, (int)super.getMinimumSize().getHeight());
					}						
				};
				attrLabel = new JLabel("<HTML><CENTER><B><I>Attribute:</I></B><BR><B>" + c2.util.TextHtmlifier.htmlify(attributeName) + "</B></CENTER></HTML>");
				if (currentValue.indexOf('\n') != -1) {
					multiLine = true;
					JTextArea textArea = new JTextArea(){
						public Dimension getMaximumSize(){
							return new Dimension((int)super.getMaximumSize().getWidth(), (int)super.getPreferredSize().getHeight());
						}
						public Dimension getPreferredSize(){
							return new Dimension(30, (int)super.getPreferredSize().getHeight());
						}
							
						public Dimension getMinimumSize(){
							return new Dimension(30, (int)super.getMinimumSize().getHeight());
						}						
					};
					textArea.setFont(attrLabel.getFont());
					textArea.setText(currentValue);
					textField = textArea;
				}
				
				applyButton = new JButton("Apply");
				applyButton.addActionListener(this);
				clearButton = new JButton("Clear");
				clearButton.addActionListener(this);
				
				if (textField instanceof JTextField) {
					((JTextField)textField).addActionListener(
						new ActionListener(){
							public void actionPerformed(ActionEvent evt){
								applyButton.doClick();
							}
						}
					);
				}
				
				//leftPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
				//leftPanel.add(new JLabel("<HTML><B>" + attributeName + "</B></HTML>"));
				leftPanel.setLayout(new BorderLayout());
				leftPanel.add("Center", attrLabel);
				
				topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				if (multiLine) 
					topPanel.add(new JLabel("<HTML><B>Current Value:</B> <hr>" + c2.util.TextHtmlifier.htmlify(currentValue) + "</HTML>"));
				else
					topPanel.add(new JLabel("<HTML><B>Current Value:</B> " + c2.util.TextHtmlifier.htmlify(currentValue) + "</HTML>"));
				
				final JButton copyButton = new JButton(edu.uci.ics.widgets.WidgetUtils.getImageIcon("archstudio/comp/archedit/copyicon.gif"));
				final String fCurrentValue = currentValue;
				copyButton.addActionListener(
					new ActionListener(){
						public void actionPerformed(ActionEvent evt){
							if(!fCurrentValue.equals("[No Value Assigned]")){
								textField.setText(fCurrentValue);
							}
						}
					}
				);
				copyButton.setToolTipText("Copy current value into field.");
				topPanel.add(copyButton); 
				
				if (multiLine)
					bottomPanel = Box.createVerticalBox();
				else
					bottomPanel = Box.createHorizontalBox();
				
				String strNewValue = "<HTML><B>New Value:</B></HTML>";
				if (multiLine) {
					strNewValue = "<HTML><B>New Value:</B> <hr> </HTML>";
				}
				final JLabel newValueLabel = new JLabel(strNewValue);
				JPanel tempPanel = new JPanel(){
					public Dimension getPreferredSize(){
						return new Dimension((int)newValueLabel.getPreferredSize().getWidth() + 10, (int)super.getPreferredSize().getHeight());
					}
					public Dimension getMaximumSize(){
						return getPreferredSize();
					}
					public Dimension getMinimumSize(){
						return getPreferredSize();
					}
				};
				tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				tempPanel.add(newValueLabel);
				bottomPanel.add(tempPanel);
				bottomPanel.add(textField);
				bottomPanel.add(applyButton);
				if (!multiLine) {
					// If the target is the security policy, it should not be cleared as an attribute
					// The policy should only be "removed" from its parent
					bottomPanel.add(clearButton);
				}
				
				this.setLayout(new BorderLayout());
				this.add("West", leftPanel);
				JPanel rightHalfPanel = new JPanel();
				rightHalfPanel.setLayout(new GridLayout(2,1));
				this.add("Center", rightHalfPanel);
				rightHalfPanel.add(topPanel);
				rightHalfPanel.add(bottomPanel);
			}
			
			public void actionPerformed(ActionEvent evt){
				if(evt.getSource().equals(applyButton)){
					String val = textField.getText();
					try{
						xarch.set(node.getTarget(), attributeName, val);
					}
					catch(edu.uci.isr.xarch.FixedValueException fve){
						JOptionPane.showMessageDialog(this, "Field " + fve.getFieldName() + " must have fixed value " + fve.getFieldValue(), "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				else if(evt.getSource().equals(clearButton)){
					xarch.clear(node.getTarget(), attributeName);
				}
			}
			
			public Dimension getMaximumSize(){
				return new Dimension((int)super.getMaximumSize().getWidth(), (int)getPreferredSize().getHeight());
			}
		}
		
		class PolicyAttributeValueEditPanel extends JPanel implements ActionListener{
			private JPanel leftPanel;
			private JPanel topPanel;
			private JPanel bottomPanel;
			//private Box bottomPanel;
			private JLabel attrLabel;
			
			private ArchTreeNode node;
			private String attributeName;
			
			private PolicyEditorPanel policyPanel = null;
			private JButton applyButton;
			private JButton clearButton;
			
			
			public PolicyAttributeValueEditPanel(ArchTreeNode node, String attributeName){
				super();
				
				this.node = node;
				this.attributeName = attributeName;
				
				String currentValue = (String)xarch.get(node.getTarget(), attributeName);
				if(currentValue == null){
					currentValue = "[No Value Assigned]";
				}
				
				leftPanel = new JPanel(){
					public Dimension getPreferredSize(){
						return new Dimension((int)attrLabel.getPreferredSize().getWidth() + 10, (int)super.getPreferredSize().getHeight());
					}
				};
					
				topPanel = new JPanel();
				attrLabel = new JLabel("<HTML><H2><I>Attribute:</I>   " + c2.util.TextHtmlifier.htmlify(attributeName) + "</H2></HTML>");
				policyPanel = new PolicyEditorPanel();
				policyPanel.setPolicy(currentValue);
				
				applyButton = new JButton("Apply");
				applyButton.addActionListener(this);
				clearButton = new JButton("Clear");
				clearButton.addActionListener(this);
				
				//leftPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
				//leftPanel.add(new JLabel("<HTML><B>" + attributeName + "</B></HTML>"));
				leftPanel.setLayout(new BorderLayout());
				leftPanel.add("Center", attrLabel);
				
				topPanel.setLayout(new BorderLayout());
				
				//topPanel.add(new JLabel("<HTML><H2>Current Value:</H2> " + c2.util.TextHtmlifier.htmlify(currentValue) + "</HTML>"), BorderLayout.CENTER);
				topPanel.add(new JLabel("<HTML><H2>Current Value:</H2></HTML>"), BorderLayout.NORTH);
				JTextArea	policyValue = new JTextArea();
				policyValue.setColumns(60);
				policyValue.setRows(40);
				policyValue.setEditable(false);
				policyValue.setText(currentValue);
				JScrollPane policyValuePane = new JScrollPane(policyValue);
				policyValuePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				policyValuePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				topPanel.add(policyValuePane, BorderLayout.CENTER);
				
				final JButton copyButton = new JButton(edu.uci.ics.widgets.WidgetUtils.getImageIcon("archstudio/comp/archedit/copyicon.gif"));
				final String fCurrentValue = currentValue;
				copyButton.addActionListener(
					new ActionListener(){
						public void actionPerformed(ActionEvent evt){
							if(!fCurrentValue.equals("[No Value Assigned]")){
								policyPanel.setPolicy(fCurrentValue);
							}
						}
					}
				);
				copyButton.setToolTipText("Copy current value into field.");
				topPanel.add(copyButton, BorderLayout.SOUTH); 
				
				bottomPanel = new JPanel();
				bottomPanel.setLayout(new BorderLayout());
				//bottomPanel = Box.createVerticalBox();
				
				final JLabel newValueLabel = new JLabel("<HTML><H2>New Value:</H2></HTML>");
				JPanel tempPanel = new JPanel(){
					public Dimension getPreferredSize(){
						return new Dimension((int)newValueLabel.getPreferredSize().getWidth() + 10, (int)super.getPreferredSize().getHeight());
					}
					public Dimension getMaximumSize(){
						return getPreferredSize();
					}
					public Dimension getMinimumSize(){
						return getPreferredSize();
					}
				};
				bottomPanel.add(newValueLabel, BorderLayout.NORTH);
				//tempPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				//tempPanel.add(newValueLabel);
				//bottomPanel.add(tempPanel);
				bottomPanel.add(policyPanel, BorderLayout.CENTER);
				bottomPanel.add(applyButton, BorderLayout.SOUTH);
				// If the target is the security policy, it should not be cleared as an attribute
				// The policy should only be "removed" from its parent
				// bottomPanel.add(clearButton);
				
				this.setLayout(new BorderLayout());
				this.add("North", leftPanel);
				JPanel rightHalfPanel = new JPanel();
				rightHalfPanel.setLayout(new GridLayout(2,1));
				this.add("Center", rightHalfPanel);
				rightHalfPanel.add(topPanel);
				rightHalfPanel.add(bottomPanel);
			}
			
			public void actionPerformed(ActionEvent evt){
				if(evt.getSource().equals(applyButton)){
					String val = policyPanel.getPolicy();
					
					try{
						xarch.set(node.getTarget(), attributeName, val);
					}
					catch(edu.uci.isr.xarch.FixedValueException fve){
						JOptionPane.showMessageDialog(this, "Field " + fve.getFieldName() + " must have fixed value " + fve.getFieldValue(), "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				else if(evt.getSource().equals(clearButton)){
					xarch.clear(node.getTarget(), attributeName);
				}
			}
			
			public Dimension getMaximumSize(){
				return new Dimension((int)super.getMaximumSize().getWidth(), (int)getPreferredSize().getHeight());
			}
		}
		
		class HorizontalLine extends JComponent{
			public Dimension getPreferredSize(){
				return new Dimension((int)super.getPreferredSize().getWidth(), 3);
			}
			
			public Dimension getMinimumSize(){
				return new Dimension (0, 3);
			}
			
			public Dimension getMaximumSize(){
				return new Dimension((int)super.getMaximumSize().getWidth(), 3);
			}
			
			public void paint(Graphics g){
				//super.paint(g);
				g.setColor(Color.black);
				g.drawLine(1, 1, getWidth() - 2, 1);
				g.setColor(Color.white);
				g.drawLine(1, 2, getWidth() - 2, 2);
			}
		}
		
		public class ArchJTree extends JTree implements DragGestureListener{
		  /** Variables needed for DnD */
		  private DragSource dragSource = null;
		  private DragSourceContext dragSourceContext = null;
		  private Point cursorLocation = null;
		
		  public ArchJTree(TreeModel tm) {
		    super(tm);
		    //addTreeSelectionListener(this);
		
		    /*  Custom dragsource object: needed to handle DnD in a JTree.
				 *  This is pretty ugly. I had to overide the updateCurrentCursor
				 *  method to get the cursor to update properly. 
				 */
		    dragSource = new DragSource() {
		      protected DragSourceContext createDragSourceContext(
		         DragSourceContextPeer dscp, DragGestureEvent dgl, Cursor dragCursor, 
		         Image dragImage, Point imageOffset, Transferable t, 
		         DragSourceListener dsl) {
		           return new DragSourceContext(dscp, dgl, dragCursor, dragImage, 
		                                        imageOffset, t, dsl) {
		                      protected void updateCurrentCursor(int dropOp, 
		                                        int targetAct, int status) {}
		        };
		      }
		    };
		 
		    DragGestureRecognizer dgr = 
		      dragSource.createDefaultDragGestureRecognizer(
		        this,                             //DragSource
		        DnDConstants.ACTION_COPY_OR_MOVE, //specifies valid actions
		        this                              //DragGestureListener
		      );
		
		
		    /* Eliminates right mouse clicks as valid actions - useful especially
				 * if you implement a JPopupMenu for the JTree
				 */
		    dgr.setSourceActions(dgr.getSourceActions() & ~InputEvent.BUTTON3_MASK);
		
		    //unnecessary, but gives FileManager look
		    putClientProperty("JTree.lineStyle", "Angled");
		    //MetalTreeUI ui = (MetalTreeUI) getUI();
		  }
		
		  /** Returns The selected node */
		  public synchronized TreeNode getSelectedNode() {
		    if(getSelectionPath() == null){
					return null;
				}
				else{
					return (TreeNode)getSelectionPath().getLastPathComponent();
				}
		  }
		
		  /** DragGestureListener interface method */
		  public void dragGestureRecognized(DragGestureEvent e) {
		    //Get the selected node
		    TreeNode dragNode = getSelectedNode();
		    if (dragNode != null) {
		
					if(!(dragNode instanceof ArchTreeNode)){
						return;
					}
					
		      //Get the Transferable Object
					String id = ((ArchTreeNode)dragNode).getId();
					if(id == null){
						return;
					}
					
		      Transferable transferable = new DocID(getDocumentSource(), id);
		
		      //Select the appropriate cursor;
		      Cursor cursor = DragSource.DefaultLinkDrop;
		      //int action = e.getDragAction();
		      //if(action == DnDConstants.ACTION_MOVE) 
		      //cursor = DragSource.DefaultMoveDrop;
		   
		      //begin the drag
		      dragSource.startDrag(e, cursor, transferable, new DSListener());
		    }
		  }
			
			class DSListener implements DragSourceListener{
				public void dragDropEnd(DragSourceDropEvent dsde){
					if(dsde.getDropSuccess() == false){
						showEditPane();
					}
				} 
				public void dragEnter(DragSourceDragEvent dsde){
				}
				public void dragExit(DragSourceEvent dse){
				}
				public void dragOver(DragSourceDragEvent dsde){}
				public void dropActionChanged(DragSourceDragEvent dsde){}
			}
			
			private synchronized TreePath getNodePath(ArchTreeNode node){
				Vector v = new Vector();
				ArchTreeNode curNode = node;
				while(true){
					v.addElement(curNode);
					curNode = (ArchTreeNode)curNode.getParent();
					if(curNode == null){
						break;
					}
				}
				Object[] arr = new Object[v.size()];
				for(int i = 0; i < arr.length; i++){
					arr[i] = v.elementAt(arr.length - i - 1);
				}
				return new TreePath(arr);
			}
			
			public synchronized void refreshNode(ArchTreeNode node){
				//System.out.println("Entering refreshNode");
				Vector toggledPaths = new Vector();
				//System.out.println("Enumerating paths");
				//Fix for JDK1.4.0
				//for(Enumeration en = getDescendantToggledPaths(getNodePath(node)); en.hasMoreElements(); ){
				Enumeration expen = getExpandedDescendants(getNodePath(node));
				if(expen != null){
					while(expen.hasMoreElements()){
						toggledPaths.addElement(expen.nextElement());
					}
				}
				//System.out.println("Done enumerating paths");
				TreePath selectedPath = getSelectionPath();
				DefaultTreeModel tm = (DefaultTreeModel)getModel();
				//System.out.println("Node structure changed.");
				tm.nodeStructureChanged(node);
				//System.out.println("Reloading.");
				//tm.reload(node);
				//System.out.println("Reloaded.");
				for(Enumeration en = toggledPaths.elements(); en.hasMoreElements(); ){
					TreePath path = (TreePath)en.nextElement();
					try{
						expandPath(path);
					}
					catch(Exception e){}
				}
				//System.out.println("Expanded paths.");
				try{
					setSelectionPath(selectedPath);
				}
				catch(Exception e){}
				//System.out.println("Exiting refreshNode");
			}
			
			public void showNode(ObjRef[] pathToRoot){
				//Start at the root and walk downward
				ArchTreeNode currentNode = (ArchTreeNode)getModel().getRoot();
				for(int i = (pathToRoot.length - 1); i >= 0; i--){
					if(!currentNode.getTarget().equals(pathToRoot[i])){
						throw new IllegalArgumentException("Path not found?");
					}
					//System.out.println("Okay, node: " + currentNode + " was the right target of : " + pathToRoot[i]);
					
					if(i == 0){
						//We found it!
						//System.out.println("Found it!");
						TreePath tp = getNodePath(currentNode);
						setSelectionPath(tp);
						scrollPathToVisible(tp);
						showEditPane();
						return;
					}
					else{
						//System.out.println("syncing children.");
						currentNode.syncChildren();
						
						ObjRef findMe = pathToRoot[i-1];
						//System.out.println("looking for: " + findMe);
						int cnt = currentNode.getChildCount();
						boolean foundIt = false;
						for(int j = 0; j < cnt; j++){
							ArchTreeNode childNode = (ArchTreeNode)currentNode.getChildAt(j);
							//System.out.println("Checking : " + childNode);
							if(childNode.getTarget().equals(findMe)){
								//System.out.println("Hey, i found it!");
								currentNode = childNode;
								foundIt = true;
								break;
							}
						}
						if(!foundIt){
							throw new IllegalArgumentException("Can't find child in the path.");
						}
					}
				}
			}	
		}
		
		class LinkDropPanel extends JPanel implements DropTargetListener{
			protected ArchTreeNode node;
			protected DropTarget myTarget;
			protected ImageIcon icon;
			protected JLabel iconLabel;
			protected ObjRef target;
			private TreePath nodePath = null;
			
			public LinkDropPanel(ArchTreeNode node){
				init();
				this.node = node;
				this.target = node.getTarget();
			}
			
			private TreePath getNodePath(){
				if(nodePath != null){
					return nodePath;
				}
				
				Vector v = new Vector();
				ArchTreeNode curNode = node;
				while(true){
					v.addElement(curNode);
					curNode = (ArchTreeNode)curNode.getParent();
					if(curNode == null){
						break;
					}
				}
				Object[] arr = new Object[v.size()];
				for(int i = 0; i < arr.length; i++){
					arr[i] = v.elementAt(arr.length - i - 1);
				}
				return new TreePath(arr);
			}
			
			protected void init(){
				try{
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					InputStream is = getClass().getClassLoader().getResourceAsStream("archstudio/comp/archedit/link.gif");
					
					byte[] buf = new byte[256];
					int len;
					while((len = is.read(buf)) != -1){
						baos.write(buf, 0, len);
					}
					is.close();
					baos.close();
					this.icon = new ImageIcon(baos.toByteArray());
				}
				catch(IOException e){
					throw new RuntimeException(e.toString());
				}
				
				this.setLayout(new FlowLayout(FlowLayout.LEFT));
				
				iconLabel = new JLabel(icon);
				this.add(iconLabel);
				this.add(new JLabel("<HTML><B>This is a link.<BR>To link, drop the tree node on the icon to the left.</B></HTML>"));
				myTarget = new DropTarget(iconLabel, DnDConstants.ACTION_COPY, this, true);
				this.setVisible(true);
			}
			
			public DropTarget getDropTarget(){
				return myTarget;
			}
			
			public void setDropTarget(DropTarget t){
				myTarget = t;
			}
			
			public Dimension getMaximumSize(){
				//return new Dimension((int)super.getMaximumSize().getWidth(), (int)super.getPreferredSize().getHeight());
				return new Dimension((int)super.getMaximumSize().getWidth(), (int)getPreferredSize().getHeight());
			}
			
			public void dragEnter(DropTargetDragEvent dtde){}
			public void dragExit(DropTargetEvent dte){}
			public void dragOver(DropTargetDragEvent dtde){}
			public void drop(DropTargetDropEvent dtde){
				//System.out.println("Got a drop event!: " + dtde);
				Transferable t = dtde.getTransferable();
				if(t.isDataFlavorSupported(DocID.DOCID_DATA_FLAVOR)){
					DocID src = null;
					try{
						src = (DocID)t.getTransferData(DocID.DOCID_DATA_FLAVOR);
					}
					catch(Exception willNotHappen){
						willNotHappen.printStackTrace();
						return;
					}
					
					ObjRef xArchRef = src.getXArchRef();
					String id = src.getId();
					if(id == null){
						JOptionPane.showMessageDialog(null, "Dropped Items must have an ID", "Error", JOptionPane.ERROR_MESSAGE);
						dtde.rejectDrop();
						dtde.dropComplete(false);
						TreePath path = new TreePath(node);
						tree.setSelectionPath(getNodePath());
						tree.setLeadSelectionPath(getNodePath());
						showEditPane();
						return;
					}
					else{
						xarch.set(target, "type", "simple");
						String hrefURI = xarch.getXArchURI(xArchRef);
						if(xArchRef.equals(getDocumentSource())){
							hrefURI = "";
						}
						xarch.set(target, "href", hrefURI + "#" + id);
						dtde.acceptDrop(DnDConstants.ACTION_COPY);
						dtde.dropComplete(true);
						//DefaultTreeModel tm = (DefaultTreeModel)tree.getModel();
						//tm.nodeChanged(node);
						//tm.reload(node);
						tree.refreshNode(node);
						tree.setSelectionPath(getNodePath());
						tree.setLeadSelectionPath(getNodePath());
						refreshEditPane();
					}
				}
				else{
					dtde.rejectDrop();
					dtde.dropComplete(false);
					tree.setSelectionPath(getNodePath());
					tree.setLeadSelectionPath(getNodePath());
					showEditPane();
				}
			}
			public void dropActionChanged(DropTargetDragEvent dtde){}

		}

		class ArchTreeNode extends DefaultMutableTreeNode implements MutableTreeNode{
			private MutableTreeNode parent;
			private ObjRef target;
			private Class targetClass;
			private String displayName;
			private Vector children = null;
			private boolean allowsChildren;
			
			//These only apply if this node is the root node:
			private WeakHashMap rootRefMappings = null;
			
			public ArchTreeNode(MutableTreeNode parent, ObjRef objRef, String displayName){
				this.parent = parent;
				target = objRef;
				
				//If we are (or could be) the root, add a ref-mapping table
				if(parent == null){
					rootRefMappings = new WeakHashMap();
					if(target != null){
						rootRefMappings.put(target, this);
					}
				}
				else{
					if(target != null){
						ArchTreeNode p = (ArchTreeNode)parent;
						while(p.getParent() != null){
							p = (ArchTreeNode)p.getParent();
						}
						//Now P is the root node, which will have the rootRefMappings
						p.rootRefMappings.put(target, this);
					}
				}
				
				this.displayName = displayName;
				if(target != null){
					updateTargetClass();
				}
			}

			public void updateTargetClass(){
				try{
					targetClass = Class.forName(xarch.getType(target));
				}
				catch(ClassNotFoundException e){
					throw new IllegalArgumentException("Referenced class not found.  xArch Libraries not linked, perhaps?");
				}
			}	
			
			//Efficiency tweak--saves an EPC
			public ArchTreeNode(MutableTreeNode parent, ObjRef objRef, String displayName, String targetClassName){
				this.parent = parent;
				target = objRef;
				this.displayName = displayName;
				if(target != null){
					try{
						targetClass = Class.forName(targetClassName);
					}
					catch(ClassNotFoundException e){
						throw new IllegalArgumentException("Referenced class not found.  xArch Libraries not linked, perhaps?");
					}
				}
			}
			
			public ObjRef getTarget(){
				return target;
			}
			
			public Class getTargetClass(){
				return targetClass;
			}
			
			private Method[] getXArchMethods(Class targetClass){
				Method[] origMethods = targetClass.getMethods();
				Vector methods = new Vector(origMethods.length);
				for(int i = 0; i < origMethods.length; i++){
					Method m = origMethods[i];
					String methodName = m.getName();
					if(methodName.startsWith("get")){
						Class c = m.getDeclaringClass();
						if(c.getName().startsWith("edu.uci.isr.xarch.")){
							int mods = m.getModifiers();
							if(Modifier.isPublic(mods)){
								if(!Modifier.isStatic(mods)){
									if(!methodName.equals("getXArch")){
										if(!methodName.equals("getDOMNode")){
											if(!methodName.equals("getTypeMetadata")){
												if(!methodName.equals("getInstanceMetadata")){
													methods.addElement(m);
												}
											}
										}	
									}
								}
							}
						}
					}
				}
				
				Method[] retArr = new Method[methods.size()];
				methods.copyInto(retArr);
				return retArr;
			}
			
			protected void determineAllowsChildren(){
				if(children != null){
					return;
				}
					
				allowsChildren = false;
				
				//Method[] methods = targetClass.getMethods();
				Method[] methods = getXArchMethods(targetClass);
				
				for(int i = 0; i < methods.length; i++){
					String methodName = methods[i].getName();
					if(methodName.startsWith("get")){
						//See what kind of 'get' method it is...
						if(methodName.equals("getClass")){
							//Nope!  Whoops!
						}
						else if(methodName.equals("getXArch")){
							//Nope!  Whoops!
						}
						else if(methodName.equals("getTypeMetadata")){
							//Nope!  Whoops!
						}
						else if(methodName.equals("getInstanceMetadata")){
							//Nope!  Whoops!
						}
						else if(methods[i].getReturnType().equals(java.lang.String.class)){
							//This means it's an attribute "get" method.
							//No children for this method.
						}
						else if(methodName.startsWith("getAll")){
							//It's a collection method.
							allowsChildren = true;
							break;
						}
						else if(methods[i].getParameterTypes().length == 0){
							//It's a single-item "get" method
							allowsChildren = true;
							break;
						}
					}
				}
			}
			
			public synchronized void syncChildren(){
				synchronized(ArchEditFrame.this){
					//System.out.println("Entering syncChildren");
					if(target == null){
						//System.out.println("Exiting syncChildren");
						return;
					}
	
					Cursor origCursor = getWindowCursor();
					setWindowCursor(new Cursor(Cursor.WAIT_CURSOR));
					
					Vector newChildren = new Vector();
	
					if(children == null){
						children = new Vector();
					}
					
					Hashtable oldChildren = new Hashtable();
					int oldSize = children.size();
					for(int i = 0; i < oldSize; i++){
						ArchTreeNode oldChild = (ArchTreeNode)children.elementAt(i);
						ObjRef oldTarget = oldChild.getTarget();
						oldChildren.put(oldTarget, oldChild);
					}
					
					//Method[] methods = targetClass.getMethods();
					Method[] methods = getXArchMethods(targetClass);
					
					for(int i = 0; i < methods.length; i++){
						String methodName = methods[i].getName();
						if(methodName.startsWith("get")){
							//See what kind of 'get' method it is...
							if(methodName.equals("getClass")){
								//Nope!  Whoops!
							}
							else if(methodName.equals("getXArch")){
								//Nope!  Whoops!
							}
							else if(methodName.equals("getTypeMetadata")){
								//Nope!  Whoops!
							}
							else if(methodName.equals("getInstanceMetadata")){
								//Nope!  Whoops!
							}
							else if(methods[i].getReturnType().equals(java.lang.String.class)){
								//This means it's an attribute "get" method.
								//No children for this method.
							}
							else if(methodName.startsWith("getAll")){
								//It's a collection method.
								//System.out.println("Got a get-all method: " + methodName);
								String typeOfThing = methodName.substring(6, methodName.length() - 1);
								//System.out.println("its type of thing is : " + typeOfThing);
								
								ObjRef[] childObjRefs = xarch.getAll(target, typeOfThing);
								boolean thisIsAnXArch = xarch.isInstanceOf(target, "edu.uci.isr.xarch.IXArch");
	
								for(int j = 0; j < childObjRefs.length; j++){
									if(!thisIsAnXArch){
										ArchTreeNode oldChild = (ArchTreeNode)oldChildren.get(childObjRefs[j]);
										if(oldChild != null){
											newChildren.addElement(oldChild);
										}
										else{
											newChildren.addElement(new ArchTreeNode(this, childObjRefs[j], typeOfThing));
										}
									}
									else{
										//System.out.println("*** It's an XARCH!  Number of children is: " + childObjRefs.length);
										String dn = xarch.getType(childObjRefs[j]);
										if(dn.lastIndexOf(".") != -1){
											dn = dn.substring(dn.lastIndexOf(".") + 1);
										}
										if(dn.endsWith("Impl")){
											dn = dn.substring(0, dn.length() - 4);
										}
										
										ArchTreeNode oldChild = (ArchTreeNode)oldChildren.get(childObjRefs[j]);
										if(oldChild != null){
											newChildren.addElement(oldChild);
										}
										else{
											newChildren.addElement(new ArchTreeNode(this, childObjRefs[j], dn));
										}
										
									}
								}
								allowsChildren = true;
							}
							else if(methods[i].getParameterTypes().length == 0){
								//It's a single-item "get" method
								//System.out.println("Got a get method: " + methodName);
								String typeOfThing = methodName.substring(3);
								//System.out.println("its type of thing is : " + typeOfThing);
								Object o = xarch.get(target, typeOfThing);
								//if(typeOfThing.equals("Implementation")){
								//	xarch.dump((ObjRef)o);
								//}
								
								if(o != null){
									ObjRef childObjRef = (ObjRef)o;
									ArchTreeNode oldChild = (ArchTreeNode)oldChildren.get(childObjRef);
									if(oldChild != null){
										newChildren.addElement(oldChild);
									}
									else{
										newChildren.addElement(new ArchTreeNode(this, childObjRef, typeOfThing));
									}
								}
								allowsChildren = true;
							}
						}
					}
	
					children = newChildren;
					//System.out.println("Exiting syncChildren");
					setWindowCursor(origCursor);
				} //synchronized(this)
			}
			
			public TreeNode getParent(){
				return parent;
			}
			
			public TreeNode getChildAt(int index){
				if(target == null){
					return null;
				}
				if(children == null){
					syncChildren();
				}
				
				return (TreeNode)children.elementAt(index);
			}
			
			public int getChildCount(){
				if(target == null){
					return 0;
				}
				if(children == null){
					syncChildren();
				}
				
				return children.size();
			}
			
			public int getIndex(TreeNode node){
				if(target == null){
					return -1;
				}
				if(children == null){
					syncChildren();
				}
				
				return children.indexOf(node);
			}
			
			public boolean getAllowsChildren(){
				if(target == null){
					return false;
				}
				if(children == null){
					determineAllowsChildren();
				}
				
				return allowsChildren;
			}
			
			public boolean isLeaf(){
				if(target == null){
					return true;
				}
				if(children == null){
					determineAllowsChildren();
				}
				
				return !allowsChildren;
			}
			
			public Enumeration children(){
				if(target == null){
					return new Vector().elements();
				}
				if(children == null){
					syncChildren();
				}
				
				return children.elements();
			}
			
			public void insert(MutableTreeNode child, int index){
				if(children == null){
					syncChildren();
				}
				children.insertElementAt(child, index);
			}
			
			public void remove(int index){
				if(children == null){
					syncChildren();
				}
				children.removeElementAt(index);
			}
			
			public void remove(MutableTreeNode node){
				if(children == null){
					syncChildren();
				}
				children.removeElement(node);
			}
			
			public void setUserObject(Object object){
				if(!(object instanceof ObjRef)){
					throw new IllegalArgumentException("Invalid user object type.");
				}
				target = (ObjRef)object;
				syncChildren();
			}
			
			public void removeFromParent(){
				parent = null;
			}
			
			public void setParent(MutableTreeNode newParent){
				this.parent = newParent;
			}
			
			private String getId(){
				if(target == null){
					return null;
				}
				
				try{
					Method m = targetClass.getMethod("getId", new Class[]{});
					String s = (String)xarch.get(target, "id");
					return s;
				}
				catch(NoSuchMethodException e){
					return null;
				}
			}
			
			private String getDescription(){
				if(target == null){
					return null;
				}
				try{
					ObjRef desc = (ObjRef)xarch.get(target, "Description");
					if(desc != null){
						String value = (String)xarch.get(desc, "Value");
						return value;
					}
					return null;
				}
				catch(Exception e){
					return null;
				}
			}
			
			private String getLinkedId(){
				if(target == null){
					return null;
				}
				if(!edu.uci.isr.xarch.instance.IXMLLink.class.isAssignableFrom(targetClass)){
					return null;
				}
				String href = (String)xarch.get(target, "href");
				if(href == null){
					return null;
				}
				href = href.trim();
				int index = href.indexOf("#");
				if(index == -1){
					return null;
				}
				if(index == 0){
					return ("link-local: " + href.substring(1));
				}
				else{
					return("link-remote: " + href.substring(index + 1));
				}
			}
				
			public String getDisplayName(){
				return displayName;
			}
			
			public String toString(){
				String id = null;
				if(showInlineIDs){
					id = getId();
				}
				String description = null;
				if(showInlineDescriptions){
					description = getDescription();
				}
			
				if(id == null){
					String linkedId = getLinkedId();
					if(linkedId != null){
						return displayName + " [" + linkedId + "]";
					}
				}
				if((showInlineIDs == false) && (showInlineDescriptions == false)){
					return displayName;
				}
				else if((showInlineIDs == true) && (showInlineDescriptions == false)){
					if(id == null){
						return displayName;
					}
					return displayName + " (" + id + ")";
				}
				else if((showInlineIDs == false) && (showInlineDescriptions == true)){
					if(description == null){
						return displayName;
					}
					return displayName + " (" + description + ")";
				}
				else if((showInlineIDs == true) && (showInlineDescriptions == true)){
					if((id == null) && (description == null)){
						return displayName;
					}
					if(id == null){
						id = "[N/A]";
					}
					if(description == null){
						description = "[N/A]";
					}
					return displayName + " (" + id + ", " + description + ")";
				}
				//this shouldn't happen
				return displayName;
			}
			
			public ArchTreeNode resolveRef(ObjRef ref){
				if((target != null) && (target.equals(ref))){
					return this;
				}
				if(rootRefMappings == null){
					if(parent == null){
						throw new RuntimeException("Detached tree node?");
					}
					return ((ArchTreeNode)parent).resolveRef(ref);
				}
				else{
					return (ArchTreeNode)rootRefMappings.get(ref);
				}
			}
			
		}
		
		class ArchJPopupMenu extends JPopupMenu implements ActionListener{
			
			private ArchTreeNode node;
			private JMenuItem removeMenuItem;
			private Hashtable adderMethodsTable;
			private Hashtable promoteMethodsTable;
			private Hashtable topLevelElementsTable;
			private boolean isXArch;
			private boolean parentIsXArch;
			
			public ArchJPopupMenu(ArchTreeNode node){
				super();
				this.node = node;
				init();
			}
			
			private void init(){
				isXArch = false;
				parentIsXArch = false;
				if((node.getTargetClass() == null) || (node.getTarget() == null)){
					JMenuItem item = new JMenuItem("[No Actions Available]");
					item.setEnabled(false);
					this.add(item);
					return;
				}
				
				Class tc = node.getTargetClass();
				String tcName = tc.getName();
				String tcNameLastPart = tcName.substring(tcName.lastIndexOf(".") + 1);
				if(tcNameLastPart.endsWith("Impl")){
					JLabel label = new JLabel("<HTML>&nbsp;<I><FONT COLOR=\"#0000AA\">" + 
						tcNameLastPart.substring(0, tcNameLastPart.length() - 4) +
						"</FONT></I></HTML>");
					this.add(label);
					this.add(new JSeparator());
				}
				
				if(edu.uci.isr.xarch.IXArch.class.isAssignableFrom(node.getTargetClass())){
					//This is the xArch tree root.
					isXArch = true;
					topLevelElementsTable = new Hashtable();
					
					Collection c = ContextInfoFinder.getTopLevelElements();
					for(Iterator it = c.iterator(); it.hasNext(); ){
						ElementInfo ei = (ElementInfo)it.next();
						JMenuItem addTleItem = new JMenuItem("Add " + ei.getDisplayName());
						addTleItem.addActionListener(this);
						this.add(addTleItem);
						int cnt = node.getChildCount();
						/* Commented out because we now support multiple identical top level elements. */
						/*
						for(int i = 0; i < cnt; i++){
							ArchTreeNode child = (ArchTreeNode)node.getChildAt(i);
							if(ei.getDisplayName().equals(child.toString())){
								addTleItem.setEnabled(false);
							}
						}
						*/
						topLevelElementsTable.put(addTleItem, ei);
					}
					return;
				}
				
				if(node.getParent() != null){
					if(edu.uci.isr.xarch.IXArch.class.isAssignableFrom(((ArchTreeNode)node.getParent()).getTargetClass())){
						parentIsXArch = true;
					}
				}
				
				
				removeMenuItem = new JMenuItem("Remove");
				removeMenuItem.addActionListener(this);
				this.add(removeMenuItem);
				
				this.add(new JSeparator());
				//System.out.println("Making a menu.  Target class = " + node.getTargetClass());
				Collection promotionsAvailable = ContextInfoFinder.getAvailablePromotions(node.getTargetClass());
				if(promotionsAvailable.size() == 0){
					JMenuItem item2 = new JMenuItem("[No Promotions Available]");
					item2.setEnabled(false);
					this.add(item2);
				}
				else{
					promoteMethodsTable = new Hashtable();
					for(Iterator it = promotionsAvailable.iterator(); it.hasNext(); ){
						ElementInfo p = (ElementInfo)it.next();
						JMenuItem promoteItem = new JMenuItem("Promote to " + p.getDisplayName());
						promoteMethodsTable.put(promoteItem, p);
						promoteItem.addActionListener(this);
						this.add(promoteItem);
					}
				}
				
				Method[] adderMethods = getAdderMethods(node.getTargetClass());
				adderMethodsTable = new Hashtable();
				if(adderMethods.length > 0){
					this.add(new JSeparator());
				}
				for(int i = 0; i < adderMethods.length; i++){
					JMenuItem mi = new JMenuItem("Add " + adderMethods[i].getName().substring(3));
					if(adderMethods[i].getName().startsWith("set")){
						int cnt = node.getChildCount();
						String dnToCheck = adderMethods[i].getName().substring(3);
						for(int j = 0; j < cnt; j++){
							ArchTreeNode childNode = (ArchTreeNode)node.getChildAt(j);
							if(childNode.getDisplayName().equals(dnToCheck)){
								mi.setEnabled(false);
							}
						}
					}
					mi.addActionListener(this);
					adderMethodsTable.put(mi, adderMethods[i]);
					this.add(mi);
				}
			}

			private boolean containsMethod(Class c, String methodName){
				Method[] ms = c.getMethods();
				for(int i = 0; i < ms.length; i++){
					if(ms[i].getName().equals(methodName)){
						return true;
					}
				}
				return false;
			}
			
			public void actionPerformed(ActionEvent evt){
				Object src = evt.getSource();
				if(src == removeMenuItem){
					if(parentIsXArch){
						ArchTreeNode parent = (ArchTreeNode)node.getParent();
						ObjRef parentTarget = parent.getTarget();
						xarch.remove(parentTarget, "Object", node.getTarget());
						//parent.syncChildren();
						//tree.refreshNode(parent);
					}	
					else{
						//System.out.println("Attempting remove.");
						String typeOfThing = node.getDisplayName();
						ArchTreeNode parent = (ArchTreeNode)node.getParent();
						//System.out.println("parent = " + parent);
						ObjRef parentTarget = parent.getTarget();
						Class parentTargetClass = parent.getTargetClass();
						//System.out.println("TypeOfThing = " + typeOfThing);
						if(containsMethod(parentTargetClass, "remove" + capFirstLetter(typeOfThing))){
							xarch.remove(parentTarget, typeOfThing, node.getTarget());
						}
						else{
							xarch.clear(parentTarget, typeOfThing);
						}
						//parent.syncChildren();
						//DefaultTreeModel tm = (DefaultTreeModel)tree.getModel();
						//tm.nodeStructureChanged(parent);
						//tm.reload(parent);
						//tree.refreshNode(parent);
					}
				}
				else if(src instanceof JMenuItem){
					String label = ((JMenuItem)src).getText();
					if(label.startsWith("Add")){
						if(!isXArch){
							Method m = (Method)adderMethodsTable.get(src);
							Class paramType = m.getParameterTypes()[0];
							//System.out.println("***Param type is: " + paramType.getName());
							//The thing to add is going to be in the form start with edu.uci.isr.xarch.[packagename].Ixxxx
							String paramTypeName = paramType.getName();
							//System.out.println("***Param type name is: " + paramTypeName);
							String inPackageClassName = paramTypeName.substring(paramTypeName.lastIndexOf(".") + 1);
							//System.out.println("***In Package Class name is: " + inPackageClassName);
							paramTypeName = paramTypeName.substring(0, paramTypeName.lastIndexOf("."));
							//System.out.println("***Param type name is: " + paramTypeName);
	
							//Class targetClass = node.getTargetClass();
							//System.out.println("***Target class is : " + targetClass.toString());
							//String classname = targetClass.getName();
							
							//Fix for Vijay --EMD
							Class targetClass = paramType;
							//System.out.println("***targetClass = " + targetClass);
							String classname = paramType.getName();
							while(true){
								//Should always create objects in their parent's (package) context if possible.
								//If not, climb the inheritance tree until you get down to a context that works.
								//String packageName = paramTypeName.substring(paramTypeName.lastIndexOf(".") + 1);
								
								String tmp = classname.substring(0, classname.lastIndexOf("."));
								//System.out.println("***Tmp is : " + tmp);
								String packageName = tmp.substring(tmp.lastIndexOf(".") + 1);
								//System.out.println("***packageName is : " + packageName);
								
								ObjRef context = xarch.createContext(xarch.getXArch(node.getTarget()), packageName);
								String typeOfThingName = m.getName().substring(3);
								//System.out.println("***typeOfThingName is : " + typeOfThingName);
								try{
									//System.out.println("***inPackageClassName is : " + inPackageClassName);
									ObjRef createdThing = xarch.create(context, inPackageClassName.substring(1));

									// This is the post processing after creating a PolicySetType
									// Setting a well formed empty XML policy allows better view in the editor
									//
									// A more general solution to the processing is to modify apiGen so
									// the context creation routine calls the post processing routines
									// of each element. That is a bigger change, though.
									//
									// This cannot be done at the constructor, since the constructor might be
									// called with an existing policy. This initial creation depends on the context
									if (typeOfThingName.equals("PolicySet")) {
										xarch.set(createdThing, "Policy", IPolicySetType.EMPTY_POLICY);
									}
									
									if(m.getName().startsWith("set")){
										xarch.set(node.getTarget(), typeOfThingName, createdThing);
									}
									else if(m.getName().startsWith("add")){
										xarch.add(node.getTarget(), typeOfThingName, createdThing);
									}
									break;
								}
								catch(RuntimeException e){
									e.printStackTrace();
									//Okay, we have to climb the inheritance tree here
									//Fix for Vijay --EMD
									//System.out.println("Climbing to superclass.  Current targetClass is: " + targetClass);
									targetClass = targetClass.getSuperclass();
									//System.out.println("Climbed to superclass.  Now targetClass is: " + targetClass);
									
									classname = targetClass.getName();
									if(!classname.startsWith("edu.uci.isr.xarch")){
										throw e;
									}
									continue;
								}
							}
						}
						else{
							ElementInfo ei = (ElementInfo)topLevelElementsTable.get(src);
							
							String shortPackageName = ei.getPackageName().substring(ei.getPackageName().lastIndexOf(".") + 1);
							ObjRef context = xarch.createContext(xarch.getXArch(node.getTarget()), shortPackageName);
							String typeOfElement = ei.getDisplayName();
							ObjRef createdElement = xarch.createElement(context, typeOfElement);
							xarch.add(node.getTarget(), "Object", createdElement);
							//node.syncChildren();
							//tree.refreshNode(node);
							showEditPane();
						}
					}
					else if(label.startsWith("Promote")){
						ElementInfo p = (ElementInfo)promoteMethodsTable.get(src);
						
						//System.out.println("Promoting");
						
						String shortPackageName = p.getPackageName().substring(p.getPackageName().lastIndexOf(".") + 1);
						ObjRef context = xarch.createContext(xarch.getXArch(node.getTarget()), shortPackageName);
						String typeOfPromotion = p.getDisplayName();
						ArchTreeNode parentNode = (ArchTreeNode)node.getParent();
						//System.out.println("Promoting.");
						xarch.promoteTo(context, typeOfPromotion, node.getTarget());
						//System.out.println("returned.");
						node.updateTargetClass();
						parentNode.syncChildren();
						//DefaultTreeModel tm = (DefaultTreeModel)tree.getModel();
						//tm.nodeStructureChanged(parentNode);
						//tm.reload(parentNode);
						tree.refreshNode(parentNode);
						showEditPane();
					}
				}
			}
		
			private String capFirstLetter(String s){
				if(s == null) return null;
				if(s.equals("")) return "";
				StringBuffer sb = new StringBuffer();
				sb.append(Character.toUpperCase(s.charAt(0)));
				if(s.length() > 0){
					sb.append(s.substring(1));
				}
				return sb.toString();
			}
				
			private Method[] getAdderMethods(Class targetClass){
				Method[] origMethods = targetClass.getMethods();
				Vector methods = new Vector(origMethods.length);
				for(int i = 0; i < origMethods.length; i++){
					Method m = origMethods[i];
					String methodName = m.getName();
					if(methodName.startsWith("set")){
						Class c = m.getDeclaringClass();
						if(c.getName().startsWith("edu.uci.isr.xarch.")){
							int mods = m.getModifiers();
							if(Modifier.isPublic(mods)){
								if(!Modifier.isStatic(mods)){
									Class[] paramClasses = m.getParameterTypes();
									if(paramClasses.length == 1){
										if(!paramClasses[0].equals(java.lang.String.class)){
											if(paramClasses[0].getName().startsWith("edu.uci.isr.xarch.")){
												if(paramClasses[0].getName().indexOf(".", 18) != -1){
													methods.addElement(m);
												}
											}
										}
									}
								}
							}
						}
					}
					else if(methodName.startsWith("add")){
						Class c = m.getDeclaringClass();
						if(c.getName().startsWith("edu.uci.isr.xarch.")){
							int mods = m.getModifiers();
							if(Modifier.isPublic(mods)){
								if(!Modifier.isStatic(mods)){
									Class[] paramClasses = m.getParameterTypes();
									if(paramClasses.length == 1){
										if(!paramClasses[0].equals(java.util.Collection.class)){
											methods.addElement(m);
										}
									}
								}
							}
						}
					}
				}
				
				Method[] retArr = new Method[methods.size()];
				methods.copyInto(retArr);
				return retArr;
			}
		}
			
	}
}

class DocID implements Transferable, java.io.Serializable{
	ObjRef xArchRef;
	String id;
	
	public static final DataFlavor DOCID_DATA_FLAVOR = 
		new DataFlavor(DocID.class, "DOCID_DATA_FLAVOR");
	
	public DocID(ObjRef xArchRef, String id){
		this.xArchRef = xArchRef;
		this.id = id;
	}
	
	public String getId(){
		return id;
	}
	
	public ObjRef getXArchRef(){
		return xArchRef;
	}
	
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, java.io.IOException{
		if(flavor.equals(DOCID_DATA_FLAVOR)){
			return this;
		}
		else if(flavor.equals(DataFlavor.stringFlavor)){
			return this.toString();
		}
		else{
			throw new UnsupportedFlavorException(flavor);
		}
	}
	
	public DataFlavor[] getTransferDataFlavors(){
		return new DataFlavor[]{
			DataFlavor.stringFlavor,
			DOCID_DATA_FLAVOR
		};
	}
		
	public boolean isDataFlavorSupported(DataFlavor flavor){
		DataFlavor[] flavs = getTransferDataFlavors();
		for(int i = 0; i < flavs.length; i++){
			if(flavor.equals(flavs[i])){
				return true;
			}
		}
		return false;
	}
	
}


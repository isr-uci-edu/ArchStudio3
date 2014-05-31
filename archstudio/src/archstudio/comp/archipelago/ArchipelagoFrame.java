package archstudio.comp.archipelago;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import archstudio.comp.archipelago.hints.HintDecodingException;
import archstudio.comp.archipelago.hints.HintEncodingException;
import archstudio.comp.archipelago.options.ArchOptionsTreePlugin;
import archstudio.comp.archipelago.security.ArchSecurityTreePlugin;
import archstudio.comp.archipelago.types.ArchStructurePreferencePanel;
import archstudio.comp.archipelago.types.ArchStructureTreePlugin;
import archstudio.comp.archipelago.types.ArchTypesPreferencePanel;
import archstudio.comp.archipelago.types.ArchTypesTreePlugin;
import archstudio.comp.archipelago.variants.ArchVariantsTreePlugin;
import archstudio.comp.booleannotation.IBooleanNotation;
import archstudio.comp.graphlayout.IGraphLayout;
import archstudio.comp.preferences.IPreferences;
import archstudio.comp.preferencesgui.ShowPreferencesDialogMessage;
import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;
import c2.fw.Message;
import c2.fw.MessageListener;
import c2.util.MessageSendProxy;
import edu.uci.ics.bna.BNAModel;
import edu.uci.ics.widgets.IconableTreeCellRenderer;
import edu.uci.ics.widgets.WidgetUtils;
import edu.uci.ics.widgets.navpanel.NavigationItem;
import edu.uci.ics.widgets.navpanel.NavigationPanel;
import edu.uci.ics.widgets.navpanel.NavigationPanelListener;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchFileEvent;
import edu.uci.ics.xarchutils.XArchFlatEvent;
import edu.uci.ics.xarchutils.XArchFlatListener;
import edu.uci.ics.xarchutils.XArchPath;

public class ArchipelagoFrame extends JFrame implements ActionListener, XArchFlatListener, MessageListener, NavigationPanelListener{
	private static int initialWindowPositionOffset = 1;
	
	protected ArchipelagoC2Component c2Component;
	protected MessageSendProxy topIfaceSender;
	protected MessageSendProxy bottomIfaceSender;
	
	private ObjRef documentSource;
	private XArchFlatTransactionsInterface xarch;
	private IPreferences preferences;
	private IGraphLayout gli;
	private IBooleanNotation bni;
	
	protected java.util.List archipelagoTreePlugins = new ArrayList();
	
	protected JSplitPane splitPane;
	protected ArchipelagoTree tree;
	protected ArchipelagoStatusBar statusBar;
	protected RenderingHints renderingHints = null;
	
	protected JMenuItem miNewWindow;
	protected JMenuItem miHookOpenArchitecture;
	protected JMenuItem miWriteHints;
	protected JMenuItem miPreferences;
	protected JMenuItem miClose;
	
	protected NavigationPanel navigationPanel;
	
	protected String windowTitleFile;
	protected String windowTitleElement;
	
	public ArchipelagoFrame(ArchipelagoC2Component c2Component,
	XArchFlatTransactionsInterface xarch, IPreferences preferences,
	IGraphLayout gli, IBooleanNotation bni){
		super();
		archstudio.Branding.brandFrame(this);
		this.c2Component = c2Component;
		this.xarch = xarch;
		this.preferences = preferences;
		this.gli = gli;
		this.bni = bni;
		
		topIfaceSender = new MessageSendProxy(c2Component, 
			c2Component.getInterface(ArchipelagoC2Component.TOP_INTERFACE_ID));
		bottomIfaceSender = new MessageSendProxy(c2Component, 
			c2Component.getInterface(ArchipelagoC2Component.BOTTOM_INTERFACE_ID));
		
		addWindowListener(new ArchipelagoWindowListener());
		
		updateWindowTitle();
		init();
		initPlugins();
		repaint();
	}
	
	class ArchipelagoWindowListener extends WindowAdapter{
		public void windowDeactivated(WindowEvent e){
			//writeHints();
		}
	}
	
	public void messageSent(Message m){
		c2Component.sendRequest(m);
	}
	
	private void closeWindow(){
		closeDocument();
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
		
		//Set up Menu Bar
		JMenuBar mb = new JMenuBar();
		JMenu mArchitecture = new JMenu("Architecture");
		//JMenu mEdit = new JMenu("Edit");
		
		miNewWindow = new JMenuItem("New Window");
		WidgetUtils.setMnemonic(miNewWindow, 'N');
		miNewWindow.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.Event.CTRL_MASK));
		miNewWindow.addActionListener(this);
		
		miHookOpenArchitecture = new JMenuItem("Open Architecture...");
		WidgetUtils.setMnemonic(miHookOpenArchitecture, 'O');
		miHookOpenArchitecture.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.Event.CTRL_MASK));
		miHookOpenArchitecture.addActionListener(this);
		
		miWriteHints = new JMenuItem("Store Rendering Hints");
		WidgetUtils.setMnemonic(miWriteHints, 'H');
		miWriteHints.addActionListener(this);
		
		miPreferences = new JMenuItem("Archipelago Preferences...");
		WidgetUtils.setMnemonic(miPreferences, 'P');
		miPreferences.addActionListener(this);
		
		miClose = new JMenuItem("Close Window");
		WidgetUtils.setMnemonic(miClose, 'C');
		miClose.addActionListener(this);
		
		mArchitecture.add(miNewWindow);
		mArchitecture.add(miHookOpenArchitecture);
		mArchitecture.add(new JSeparator());
		mArchitecture.add(miPreferences);
		mArchitecture.add(new JSeparator());
		mArchitecture.add(miWriteHints);
		mArchitecture.add(miClose);
		
		mb.add(mArchitecture);
		
		this.setJMenuBar(mb);
		
		tree = new ArchipelagoTree(new ArchipelagoTreeDragGestureListener());
		IconableTreeCellRenderer cellRenderer = new IconableTreeCellRenderer();
		cellRenderer.setCustomIconsOnlyForLeafs(false);
		tree.setCellRenderer(cellRenderer);
		tree.setRowHeight(18);
		tree.addMouseListener(new ArchipelagoTreeMouseAdapter());
		
		JPanel treePanel = new JPanel();
		treePanel.setLayout(new BorderLayout());
		treePanel.add("Center", new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePanel, getNothingComponent());
		splitPane.setDividerLocation((int)(xSize * 0.33));
		splitPane.setOneTouchExpandable(true);

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add("Center", splitPane);
		
		statusBar = new ArchipelagoStatusBar();
		this.getContentPane().add("South", statusBar);
		
		documentSource = null;
		
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(
			new WindowAdapter(){
				public void windowClosing(WindowEvent evt){
					closeWindow();
				}
			}
		);
		
		JToolBar toolBar = new JToolBar("Nav Toolbar");
		navigationPanel = new NavigationPanel();
		navigationPanel.addNavigationPanelListener(this);
		toolBar.add(navigationPanel);
		treePanel.add("South", toolBar);
		
		tree.requestFocus();
		//this.getFocusTraversalPolicy().getDefaultComponent(this).requestFocus();
		
		setVisible(true);
		setSize((int)xSize, (int)ySize);
		setLocation((int)xPos, (int)yPos);
		splitPane.setDividerLocation((int)(xSize / 3));
		setVisible(true);
		invalidate();
		WidgetUtils.validateAndRepaintInAWTThread(this);
		//validate();
		//paint(getGraphics());
	}
	
	public static Map getPreferencePanels(){
		Map m = new HashMap();
		m.put("ArchStudio 3/Archipelago/Structure", new ArchStructurePreferencePanel());
		m.put("ArchStudio 3/Archipelago/Types", new ArchTypesPreferencePanel());
		return m;
	}
	
	public void initPlugins(){
		ArchTypesTreePlugin archTypesTreePlugin = 
			new ArchTypesTreePlugin(topIfaceSender, bottomIfaceSender, this, tree, xarch, preferences, gli);
		addArchipelagoTreePlugin(archTypesTreePlugin);
		
		ArchStructureTreePlugin archStructureTreePlugin = 
			new ArchStructureTreePlugin(topIfaceSender, bottomIfaceSender, this, tree, xarch, preferences, gli, archTypesTreePlugin);
		addArchipelagoTreePlugin(archStructureTreePlugin);
		
		ArchOptionsTreePlugin archOptionsTreePlugin = 
			new ArchOptionsTreePlugin(topIfaceSender, bottomIfaceSender, this, tree, xarch, preferences, bni, archStructureTreePlugin);
		addArchipelagoTreePlugin(archOptionsTreePlugin);
		
		ArchOptionsTreePlugin archOptionsTreePlugin2 = 
			new ArchOptionsTreePlugin(topIfaceSender, bottomIfaceSender, this, tree, xarch, preferences, bni, archTypesTreePlugin);
		addArchipelagoTreePlugin(archOptionsTreePlugin2);
		
		ArchVariantsTreePlugin archVariantsTreePlugin = 
			new ArchVariantsTreePlugin(topIfaceSender, bottomIfaceSender, this, tree, xarch, preferences, bni, archStructureTreePlugin, archTypesTreePlugin);
		addArchipelagoTreePlugin(archVariantsTreePlugin);
		
		ArchSecurityTreePlugin archSecurityTreePlugin = 
			new ArchSecurityTreePlugin(topIfaceSender, bottomIfaceSender, this, tree, xarch, preferences, bni, archStructureTreePlugin, archTypesTreePlugin);
		addArchipelagoTreePlugin(archSecurityTreePlugin);
		
		//TODO: Come up with some more reasonable way to deal with this
		try{
			Class c = Class.forName("archstudio.comp.archipelago.rasds.RASDSTreePlugin");
			Object[] parms = new Object[]{topIfaceSender, bottomIfaceSender, this, tree, xarch, preferences, archStructureTreePlugin};
			java.lang.reflect.Constructor cons = c.getDeclaredConstructors()[0];
			ArchipelagoTreePlugin rasdsPlugin = (ArchipelagoTreePlugin)cons.newInstance(parms);
			addArchipelagoTreePlugin(rasdsPlugin);
		}
		catch(Exception e){
		}
	}
	
	public void addArchipelagoTreePlugin(ArchipelagoTreePlugin atp){
		tree.getModel().addTreeModelListener(atp);
		tree.addTreeSelectionListener(atp);
		atp.addMessageListener(this);
		//xadl events are sent to everybody in this list automagically
		archipelagoTreePlugins.add(atp);
	}
	
	public void removeArchipelagoTreePlugin(ArchipelagoTreePlugin atp){
		archipelagoTreePlugins.remove(atp);
		atp.removeMessageListener(this);
		tree.getModel().removeTreeModelListener(atp);
		tree.removeTreeSelectionListener(atp);
	}
	
	public void addNavigationItem(NavigationItem ni){
		navigationPanel.addNavigationItem(ni);
	}
	
	public void navigateTo(NavigationPanel navigationPanel,	NavigationItem navigationItem){
		for(Iterator it = archipelagoTreePlugins.iterator(); it.hasNext(); ){
			ArchipelagoTreePlugin atp = (ArchipelagoTreePlugin)it.next();
			boolean handled = atp.navigateTo(navigationItem);
			if(handled) return;
		}
	}

	class ArchipelagoTreeMouseAdapter extends MouseAdapter{
		
		public void mousePressed(MouseEvent e){
			if(checkPopup(e)){
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				tree.setSelectionPath(selPath);
			}
		}
		
		public void mouseReleased(MouseEvent e){
			if(checkPopup(e)){
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				tree.setSelectionPath(selPath);
			}
		}
		
		public boolean checkPopup(MouseEvent e){
			if(e.isPopupTrigger()){
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				if(selRow != -1) {
					if(e.getClickCount() == 1){
						Object o = selPath.getLastPathComponent();
						if((o != null) && (o instanceof TreeNode)){
							TreeNode n = (TreeNode)o;
							java.util.List allMenuItems = new ArrayList();
							for(Iterator it = archipelagoTreePlugins.iterator(); it.hasNext(); ){
								ArchipelagoTreePlugin atp = (ArchipelagoTreePlugin)it.next();
								JMenuItem[] menuItems = atp.getPopupMenuItems(n);
								if(menuItems != null){
									allMenuItems.addAll(Arrays.asList(menuItems));
								}
							}
							if(allMenuItems.size() > 0){
								JPopupMenu popupMenu = new JPopupMenu();
								for(Iterator it = allMenuItems.iterator(); it.hasNext(); ){
									JMenuItem mi = (JMenuItem)it.next();
									popupMenu.add(mi);
								}
								popupMenu.show(e.getComponent(), e.getX(), e.getY());
								return true;
							}
						}
					}
				}
			}
			return false;
		}
		
	}
	
	public static JComponent getNothingComponent(){
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.CENTER));
		p.add(new JLabel("Double-click an item to the left to view it."));
		return p;
	}
	
	public java.awt.Component getRightComponent(){
		return splitPane.getRightComponent();
	}
	
	public void setRightComponent(java.awt.Component c){
		int divLoc = ((int)splitPane.getLeftComponent().getSize().getWidth()) + 1;
		if(c == null){
			splitPane.setRightComponent(getNothingComponent());
		}
		else{
			splitPane.setRightComponent(c);
		}
		splitPane.setDividerLocation(divLoc);
		
		this.requestFocus();
		WidgetUtils.validateAndRepaintInAWTThread(this);
	}
	
	public void writeHints(){
		if(documentSource == null){
			//No doc open
			return;
		}
		java.util.List hintsRefList = new ArrayList();
		java.util.List hintsBNAModelList = new ArrayList();
		
		for(Iterator it = archipelagoTreePlugins.iterator(); it.hasNext(); ){
			ArchipelagoTreePlugin atp = (ArchipelagoTreePlugin)it.next();
			ArchipelagoHintsInfo[] hintsInfo = atp.getHintsInfo();
			if(hintsInfo != null){
				for(int i = 0; i < hintsInfo.length; i++){
					hintsRefList.add(hintsInfo[i].getRef());
					hintsBNAModelList.add(hintsInfo[i].getBNAModel());
				}
			}
		}

		ObjRef[] thingsToHint = (ObjRef[])hintsRefList.toArray(new ObjRef[0]);
		BNAModel[] modelsToHint = (BNAModel[])hintsBNAModelList.toArray(new BNAModel[0]);
		
		//ProgressDialog pd = new ProgressDialog(this, "Writing Rendering Hints", "Loading Hints");
		//pd.doPopup();
		synchronized(statusBar){
			statusBar.reset();
			try{
				archstudio.comp.archipelago.RenderingHints.writeHints(xarch, getDocumentSource(), thingsToHint, modelsToHint, statusBar);
			}
			catch(HintEncodingException hee){
				JOptionPane.showMessageDialog(this, hee.toString(), "Problem Writing Hints", JOptionPane.WARNING_MESSAGE);
				hee.printStackTrace();
			}
			statusBar.reset();
		}
		//pd.doDone();
	}
	
	public void closeDocument(boolean writeHints){
		navigationPanel.clearAll();
		setRightComponent(getNothingComponent());
		if(writeHints) writeHints();
		for(Iterator it = archipelagoTreePlugins.iterator(); it.hasNext(); ){
			ArchipelagoTreePlugin atp = (ArchipelagoTreePlugin)it.next();
			atp.documentClosed();
		}
		
		setWindowTitleElement(null);
		setWindowTitleFile(null);
		documentSource = null;
	}

	public void closeDocument(){
		closeDocument(true);
	}
	
	public ObjRef getDocumentSource(){
		return documentSource;
	}
	
	public void setWindowTitleFile(String openFile){
		this.windowTitleFile = openFile;
		updateWindowTitle();
	}
	
	public void setWindowTitleElement(String element){
		this.windowTitleElement = element;
		updateWindowTitle();
	}
	
	private void updateWindowTitle(){
		StringBuffer title = new StringBuffer();
		
		//I'm trying it the "Backwards way" at the moment;
		//item name first, then document, then product.
		//This is the way IE and many apps do it so the
		//current item name is most prominent in the UI
		if(windowTitleElement != null){
			title.append(windowTitleElement);
			title.append(" - ");
		}
		if(windowTitleFile != null){
			title.append("[").append(windowTitleFile).append("]");
			title.append(" - ");
		}
		title.append(ArchipelagoC2Component.PRODUCT_NAME);
		
		/*
		title.append(ArchipelagoC2Component.PRODUCT_NAME);
		if(windowTitleFile == null){
			title.append(" - [None]");
		}
		else{
			title.append(" - [").append(windowTitleFile).append("]");
			if(windowTitleElement != null){
				title.append(" - " + windowTitleElement);
			}
		}
		*/
		
		setTitle(title.toString());
		
	}
	
	public void handleXArchFlatEvent(XArchFlatEvent evt){
		for(Iterator it = archipelagoTreePlugins.iterator(); it.hasNext(); ){
			ArchipelagoTreePlugin atp = (ArchipelagoTreePlugin)it.next();
			atp.handleXArchFlatEvent(evt);
		}
	}
	
	public void handleXArchFileEvent(XArchFileEvent evt){
		if(evt.getEventType() == XArchFileEvent.XARCH_RENAMED_EVENT){
			setWindowTitleFile(evt.getAsURL());
		}
		for(Iterator it = archipelagoTreePlugins.iterator(); it.hasNext(); ){
			ArchipelagoTreePlugin atp = (ArchipelagoTreePlugin)it.next();
			atp.handleXArchFileEvent(evt);
		}
	}
	
	public void showRef(ObjRef ref){
		XArchPath path = null;
		if(ref != null){
			path = xarch.getXArchPath(ref);
		}
		for(Iterator it = archipelagoTreePlugins.iterator(); it.hasNext(); ){
			ArchipelagoTreePlugin atp = (ArchipelagoTreePlugin)it.next();
			if(atp.showRef(ref, path)){
				return;
			}
		}
	}

	public void openXArch(ObjRef xArchRef){
		setDocumentSource(xArchRef);
		navigationPanel.clearAll();
		readRenderingHints(xArchRef);
		//ObjRef documentSourceXArchRef = xarch.getOpenXArch(url);
		String uri = xarch.getXArchURI(xArchRef);
		setWindowTitleFile(uri);
		for(Iterator it = archipelagoTreePlugins.iterator(); it.hasNext(); ){
			ArchipelagoTreePlugin atp = (ArchipelagoTreePlugin)it.next();
			atp.documentOpened(xArchRef, null);
		}
	}
	
	//protected HashMap renderingHintsCache = new HashMap();
	
	public void readRenderingHints(ObjRef xArchRef){
		//ProgressDialog pd = new ProgressDialog(this, "Loading Rendering Hints", "Loading Hints");
		//pd.doPopup();
		statusBar.reset();
		synchronized(statusBar){
			try{
				renderingHints = RenderingHints.readHints(xarch, xArchRef, statusBar);
			}
			catch(HintDecodingException hee){
				JOptionPane.showMessageDialog(this, hee.toString(), "Problem Loading Hints", JOptionPane.WARNING_MESSAGE);
				hee.printStackTrace();
			}
		}
		statusBar.reset();
		//pd.doDone();
	}
	
	public RenderingHints getRenderingHints(){
		return renderingHints;
	}
	
	public void openXArch(ObjRef xArchRef, ObjRef elementRef){
		openXArch(xArchRef);
	}
	
	public void showID(String id){
	}
	
	public void actionPerformed(ActionEvent evt){
		if(evt.getSource() == miClose){
			closeWindow();
		}
		else if(evt.getSource() == miHookOpenArchitecture){
			handleHookOpenArchitecture();
		}
		else if(evt.getSource() == miNewWindow){
			c2Component.newWindow();
		}
		else if(evt.getSource() == miWriteHints){
			writeHints();
		}
		else if(evt.getSource() == miPreferences){
			handleEditPreferences();
		}
	}
	
	public void handleEditPreferences(){
		ShowPreferencesDialogMessage spdm = new ShowPreferencesDialogMessage(ArchipelagoC2Component.PREFERENCE_NAME);
		c2Component.sendToAll(spdm, c2Component.getInterface(ArchipelagoC2Component.BOTTOM_INTERFACE_ID));
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
		ObjRef xArchRef = xarch.getOpenXArch(result);
		if(xArchRef.equals(getDocumentSource())){
			//Document is already open in this window
			return;
		}
		ArchipelagoFrame otherFrame = c2Component.getWindow(xArchRef);
		if(otherFrame != null){
			//It's already open in another window
			otherFrame.requestFocus();
			return;
		}

		try{
			closeDocument();
			ObjRef resultXArchRef = xarch.getOpenXArch(result);
			if(xArchRef != null){
				openXArch(resultXArchRef);
				WidgetUtils.validateAndRepaintInAWTThread(this);
				//validate();
				//repaint();
			}
		}
		catch(Exception e){
			e.printStackTrace();
			return;
		}			
	}
	
	public void setDocumentSource(ObjRef xArchRef){
		this.documentSource = xArchRef;
	}
	
	//Drag & Drop stuff for the tree
	class ArchipelagoTreeDragGestureListener implements DragGestureListener {
		public void dragGestureRecognized(DragGestureEvent e){
			//Get the selected node
			if(tree == null){
				return;
			}
			DragSource dragSource = tree.getDragSource();
			if(dragSource == null){
				return;
			}

			TreeNode dragNode = tree.getSelectedNode();
			if(dragNode != null){
				//See if anybody cares
				for(Iterator it = archipelagoTreePlugins.iterator(); it.hasNext(); ){
					ArchipelagoTreePlugin atp = (ArchipelagoTreePlugin)it.next();
					if(atp.shouldAllowDrag(dragNode)){
						DragInfo dragInfo = atp.getDragInfo(dragNode);
						
						if(dragInfo == null){
							throw new RuntimeException("Archipelago Tree Plugin allowed drag but returned no drag info.");
						}
						Cursor c = dragInfo.getCursor();
						if(c == null){
							c = DragSource.DefaultLinkDrop;
						}
						Transferable t = dragInfo.getTransferable();
						DragSourceListener dsl = dragInfo.getDragSourceListener();
						dragSource.startDrag(e, c, t, dsl);
					}
				}
			}
		}
	}
	
	public void handle(Message m){
		for(Iterator it = archipelagoTreePlugins.iterator(); it.hasNext(); ){
			ArchipelagoTreePlugin atp = (ArchipelagoTreePlugin)it.next();
			atp.handle(m);
		}
	}
	
}	
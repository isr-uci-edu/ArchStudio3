
package archstudio.comp.tron.gui;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.*;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;
import archstudio.tron.*;

import c2.fw.*;
import c2.util.MessageSendProxy;
import c2.util.UIDGenerator;

import edu.uci.ics.widgets.JPanelGR;
import edu.uci.ics.widgets.WidgetUtils;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchFileEvent;

class TronGUIFrame extends JFrame implements ClipboardOwner{
	
	protected TronGUIC2Component c2Component;
	protected MessageSendProxy requestProxy;
	protected MessageSendProxy notificationProxy;
	protected XArchFlatTransactionsInterface xarch;

	protected TronGUIEditorModel tronEditorModel;
	
	protected TronGUITree tronTree;
	protected TronGUITreeModel tronTreeModel;
	
	protected TronGUITableModel tronTableModel;

	protected TronGUITabbedPane tronTabbedPane;
	
	protected TronGUIDescriptionPanel tronDescriptionPanel;
	protected TronGUISelectionHandler tronSelectionHandler;
	protected TronGUIToolbar tronToolbar;
	
	protected TronGUITestErrorBar tronTestErrorBar;
	
	protected TronGUIConsoleModel tronConsoleModel;
	
	protected TronGUIToolStatusModel tronToolStatusModel;
	
	protected JMenuBar mb;
	protected JMenu mDevelopment;
	protected JMenuItem miToolsRefresh;
	protected JMenuItem miGenerateUID;
	
	public TronGUIFrame(TronGUIC2Component c2Component, MessageSendProxy requestProxy,
	MessageSendProxy notificationProxy, XArchFlatTransactionsInterface xarch,
	TronGUIEditorModel tronEditorModel, TronGUITreeModel tronTreeModel, TronGUITableModel tronTableModel,
	TronGUIConsoleModel tronConsoleModel, TronGUIToolStatusModel tronToolStatusModel){
		super("Tron - ArchStudio 3 Analysis Framework");
		this.c2Component = c2Component;
		this.requestProxy = requestProxy;
		this.notificationProxy = notificationProxy;
		this.xarch = xarch;
		this.tronEditorModel = tronEditorModel;
		this.tronTreeModel = tronTreeModel;
		this.tronTableModel = tronTableModel;
		this.tronConsoleModel = tronConsoleModel;
		this.tronToolStatusModel = tronToolStatusModel;
		archstudio.Branding.brandFrame(this);
		
		this.getContentPane().setLayout(new BorderLayout());

		tronTree = new TronGUITree(tronTreeModel);
		tronTree.setShowsRootHandles(true);
		TronGUITreeClickHandler clickHandler = new TronGUITreeClickHandler(tronTree, xarch);
		//tronTree.setOpaque(false);
		//((DefaultTreeCellRenderer)tronTree.getCellRenderer()).setOpaque(false);
		//((DefaultTreeCellRenderer)tronTree.getCellRenderer()).setBackgroundNonSelectionColor(null);
		ToolTipManager.sharedInstance().registerComponent(tronTree); 
		JScrollPane tronTreePane = new JScrollPane(tronTree);
		
		JPanel tronLeftPane = new JPanel();
		tronLeftPane.setLayout(new BorderLayout());
		tronLeftPane.add("Center", tronTreePane);
		
		TronGUIToolStatusPanel tronToolStatusPanel = new TronGUIToolStatusPanel(tronToolStatusModel);
		tronLeftPane.add("South", tronToolStatusPanel);
		
		tronTabbedPane = new TronGUITabbedPane(tronTableModel, tronConsoleModel);
		
		tronDescriptionPanel = new TronGUIDescriptionPanel();
		JScrollPane tronDescriptionPane = new JScrollPane(tronDescriptionPanel);
		WidgetUtils.adjustScrollPaneMovement(tronDescriptionPane);

		tronSelectionHandler = new TronGUISelectionHandler(xarch, notificationProxy, 
			tronEditorModel, tronTree, tronTabbedPane, tronDescriptionPanel);
		
		tronTestErrorBar = new TronGUITestErrorBar(getViewConsoleActionListener(TronGUIConsoleModel.TEST_ERROR_TOOLNAME));

		tronToolbar = new TronGUIToolbar(requestProxy, tronTreeModel, tronTestErrorBar);
		
		mb = new JMenuBar();
		mDevelopment = new JMenu("Development");
		WidgetUtils.setMnemonic(mDevelopment, 'D');
		mb.add(mDevelopment);
		
		miToolsRefresh = new JMenuItem("Refresh Tests");
		miToolsRefresh.addActionListener(getRefreshTestsActionListener());
		WidgetUtils.setMnemonic(miToolsRefresh, 'R');
		mDevelopment.add(miToolsRefresh);
		
		miGenerateUID = new JMenuItem("Generate UID on Clipboard");
		miGenerateUID.addActionListener(getGenerateUIDActionListener());
		WidgetUtils.setMnemonic(miGenerateUID, 'U');
		mDevelopment.add(miGenerateUID);
		
		JPanel tablePanel = new JPanel();
		tablePanel.setLayout(new BorderLayout());
		tablePanel.add("North", tronTestErrorBar);
		tablePanel.add("Center", tronTabbedPane);
		JSplitPane topOnBottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablePanel, tronDescriptionPane);
		JSplitPane sideBySideSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tronLeftPane, topOnBottomSplit);
		this.getContentPane().add("Center", sideBySideSplit);
		this.getContentPane().add("North", tronToolbar);
		
		this.setJMenuBar(mb);
		
		Toolkit tk = getToolkit();
		Dimension screenSize = tk.getScreenSize();
		double xSize = (screenSize.getWidth() * 0.80);
		double ySize = (screenSize.getHeight() * 0.75);
		double xPos = (screenSize.getWidth() * 0.05);
		double yPos = (screenSize.getHeight() * 0.05);
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		setSize((int)xSize, (int)ySize);
		setLocation((int)xPos, (int)yPos);
		setVisible(true);
		validate();
		
		topOnBottomSplit.setDividerLocation(0.60d);
		sideBySideSplit.setDividerLocation(0.38d);

		tronTreeModel.refreshTreeModel();
		tronTree.setSelectionRow(0);
		
		WidgetUtils.validateAndRepaintInAWTThread(this);
	}
	
	protected void doRefreshTests(){
		TronRefreshTestsMessage trtm = new TronRefreshTestsMessage();
		requestProxy.send(trtm);
	}
	
	public void handleTestErrors(TronTestErrorsMessage m){
		if(m.getTestErrors() != null){
			if(m.getTestErrors().length > 0){
				tronTestErrorBar.popup();
			}
		}
	}
	
	private ActionListener getViewConsoleActionListener(final String tabToSelect){
		return new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				if(tabToSelect != null){
					tronTabbedPane.selectTab(tabToSelect);
				}
			}
		};
	}
	
	private ActionListener getRefreshTestsActionListener(){
		return new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				doRefreshTests();
			}
		};
	}

	private ActionListener getGenerateUIDActionListener(){
		return new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				String uid = UIDGenerator.generateUID("test");
				StringSelection stringSelection = new StringSelection(uid);
		    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		    clipboard.setContents(stringSelection, TronGUIFrame.this);
			}
		};
	}

	public void lostOwnership(Clipboard aClipboard, Transferable aContents){
    //do nothing
  }
} /* Frame */

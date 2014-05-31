package archstudio.comp.preferencesgui;

//import archstudio.invoke.*;

import archstudio.comp.preferences.IPreferences;
import archstudio.preferences.*;
import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

import edu.uci.ics.widgets.*;
//import edu.uci.ics.xarchutils.*;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;

public class PreferencesGUIC2Component extends AbstractC2DelegateBrick implements MessageListener{

	protected Vector artistMessages = new Vector();
	protected DefaultTreeModel treeModel;

	private JFrame fakeDialogParent = new JFrame();
	{
		archstudio.Branding.brandFrame(fakeDialogParent);
	}

	protected PreferencesGUIDialog guiDialog = null;
	protected IPreferences preferences = null;
	
	//Maps path strings ("ArchStudio 3/SomePreferenceHeading/SomeSubHeading") to PreferencePanels
	protected Map preferencePanels;
	
	public PreferencesGUIC2Component(Identifier id){
		super(id);
		
		preferencePanels = new HashMap();

		preferences = (IPreferences)EBIWrapperUtils.addExternalService(this,
			topIface, IPreferences.class);
		
		addMessageProcessor(new PreferencesGUIMessageProcessor());
		addLifecycleProcessor(new PreferencesGUILifecycleProcessor());

		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("ArchStudio 3");
		treeModel = new DefaultTreeModel(rootNode);
	}

	public void showDialog(ShowPreferencesDialogMessage m){
		//Let's open a new window.
		newWindow(m.getNodeToShow());
	}

	public void removePath(String path){
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)treeModel.getRoot();
		
		StringTokenizer st = new StringTokenizer(path, "/");
		String tok = st.nextToken();
		if(!tok.equals("ArchStudio 3")){
			throw new IllegalArgumentException("Paths for preference panels must start with \"ArchStudio 3\"/");
		}
		
		while(st.hasMoreTokens()){
			tok = st.nextToken();
			boolean found = false;
			for(int i = 0; i < treeNode.getChildCount(); i++){
				DefaultMutableTreeNode child = (DefaultMutableTreeNode)treeNode.getChildAt(i);
				String childString = child.getUserObject().toString();
				if(childString.equals(tok)){
					found = true;
					treeNode = child;
					break;
				}
			}
			if(!found){
				return;
			}
		}
		
		treeModel.removeNodeFromParent(treeNode);
	}
	
	public void putPreferencePanel(String path, PreferencePanel p){
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)treeModel.getRoot();
		
		StringTokenizer st = new StringTokenizer(path, "/");
		String tok = st.nextToken();
		if(!tok.equals("ArchStudio 3")){
			throw new IllegalArgumentException("Paths for preference panels must start with \"ArchStudio 3\"/");
		}
		
		//Find the node given by the path (or create it if necessary);
		//at the end of this loop treeNode will be the leaf node we're inserting at.
		while(st.hasMoreTokens()){
			tok = st.nextToken();
			boolean found = false;
			for(int i = 0; i < treeNode.getChildCount(); i++){
				DefaultMutableTreeNode child = (DefaultMutableTreeNode)treeNode.getChildAt(i);
				String childString = child.getUserObject().toString();
				if(childString.equals(tok)){
					found = true;
					treeNode = child;
					break;
				}
			}
			if(!found){
				DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(tok);
				insertChildAlphabetically(treeModel, treeNode, newNode);
				treeNode = newNode;
			}
		}
		
		p.setPreferences(preferences);
		p.reset();
		
		preferencePanels.put(path, p);
	}

	protected void insertChildAlphabetically(DefaultTreeModel tm, DefaultMutableTreeNode parent, DefaultMutableTreeNode newChild){
		String newChildString = getTreeNodeString(newChild);
		
		for(int i = 0; i < parent.getChildCount(); i++){
			TreeNode child = parent.getChildAt(i);
			String childString = getTreeNodeString(child);
			if(newChildString.compareTo(childString) < 0){
				tm.insertNodeInto(newChild, parent, i);
				return;
			}
		}
		tm.insertNodeInto(newChild, parent, parent.getChildCount());
	}
	
	protected static String getTreeNodeString(TreeNode tn){
		if(tn instanceof DefaultMutableTreeNode){
			return (((DefaultMutableTreeNode)tn).getUserObject().toString());
		}
		else{
			return tn.toString();
		}
	}
	
	public void closeWindow(){
		if(guiDialog != null){
			guiDialog.setVisible(false);
			guiDialog.dispose();
			guiDialog = null;
		}
	}
	
	public void newWindow(String nodeToShow){
		//This makes sure we only have one active window open.
		if(guiDialog == null){
			guiDialog = new PreferencesGUIDialog();
		}
		else{
			guiDialog.requestFocus();
		}
		if(nodeToShow != null){
			guiDialog.showNode(nodeToShow);
		}
	}
	
	public void queryPreferencePanels(){
		QueryPreferencePanelMessage qppm = new QueryPreferencePanelMessage();
		sendToAll(qppm, topIface);
	}
	
	public void messageSent(Message m){
		//System.out.println("Critic gui got message: " + m);
		sendToAll(m, bottomIface);
	}
	
	class PreferencesGUILifecycleProcessor extends c2.fw.LifecycleAdapter{
		public void begin(){
			queryPreferencePanels();
		}
		
		public void end(){
			closeWindow();
		}
	}
	
	class PreferencesGUIMessageProcessor implements MessageProcessor{
		public synchronized void handle(Message m){
			if(m instanceof ShowPreferencesDialogMessage){
				showDialog((ShowPreferencesDialogMessage)m);
			}
			else if(m instanceof PreferencesPanelMessage){
				PreferencesPanelMessage ppm = (PreferencesPanelMessage)m;
				int state = ppm.getState();
				if(state == PreferencesPanelMessage.SERVICE_ADVERTISED){
					String path = ppm.getPreferencesTreePath();
					PreferencePanel pp = ppm.getPreferencePanel();
					putPreferencePanel(path, pp);
				}
				else if(state == PreferencesPanelMessage.SERVICE_UNADVERTISED){
					String path = ppm.getPreferencesTreePath();
					removePath(path);
				}
			}
		}
	}

	static class DefaultPreferencePanel extends PreferencePanel{
			
		JLabel l = new JLabel(
		"<html>" +
		"Select a node on the left to edit preferences." +
		"<br><br>" +
		"ArchStudio 3 uses a unified preferences model that allows " +
		"all components that need to store persistent preferences " +
		"to share this user interface. " +
		"The nodes in the tree to the left will vary depending on what components " +
		"are included in the current ArchStudio 3 configuration." +
		"</html>"
		);
		
		public DefaultPreferencePanel(){
			super();
			
			JPanel p = new JPanel();
			p.setLayout(new BorderLayout());
			p.add("North", new JPanelWL(l, p));
			this.setComponent(p);	
		}
		
		public void apply(){
		}
		
		public void reset(){
		}
	}

	class PreferencesGUIDialog extends JDialog implements ActionListener, TreeSelectionListener, TreeModelListener{
		private JTree tree;
		private JLabel bigContentLabel;
		
		private JButton bResetAll;
		private JButton bApply;
		private JButton bOK;
		private JButton bCancel;
			
		private JPanel preferencesContentPanel;
		
		public PreferencesGUIDialog(){
			super(fakeDialogParent);
			setTitle("ArchStudio 3 Preferences");
			setModal(false);
			
			treeModel.addTreeModelListener(this);
			
			tree = new JTree(treeModel){
				public Dimension getMinimumSize(){
					return new Dimension(150, 450);
				}
				
				public Dimension getPreferredSize(){
					return getMinimumSize();
				}
				
				public Insets getInsets(){
					return new Insets(5, 5, 5, 5);
				}
			};
			
			DefaultTreeCellRenderer cellRenderer = new DefaultTreeCellRenderer();
			cellRenderer.setClosedIcon(null);
			cellRenderer.setOpenIcon(null);
			cellRenderer.setLeafIcon(null);
			cellRenderer.setFont(WidgetUtils.SANSSERIF_PLAIN_MEDIUM_FONT);
			
			tree.setCellRenderer(cellRenderer);
			tree.setEditable(false);
			tree.setRootVisible(true);
			tree.setBorder(BorderFactory.createLoweredBevelBorder());
			tree.addTreeSelectionListener(this);
			tree.setToggleClickCount(-1);
			
			JPanel treePanel = new JPanel();
			treePanel.setSize(new Dimension(200, 430));
			treePanel.setForeground(Color.WHITE);
			treePanel.setBackground(Color.WHITE);
			treePanel.setLayout(new BorderLayout());
			//treePanel.add("Center", new JLabel("Fook Yoo!"));
			treePanel.add("Center", tree);
			
			preferencesContentPanel = new JPanel();
			preferencesContentPanel.setLayout(new BorderLayout());
			
			JPanel bigLabelPanel = new JPanel();
			bigLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			bigContentLabel = new JLabel("ArchStudio 3");
			bigContentLabel.setFont(WidgetUtils.SANSSERIF_BOLD_BIG_FONT);
			bigLabelPanel.add(bigContentLabel);
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			
			bResetAll = new JButton("Reset All");
			bResetAll.addActionListener(this);
			
			bApply = new JButton("Apply");
			bApply.addActionListener(this);
			
			bOK = new JButton("OK");
			bOK.addActionListener(this);

			bCancel = new JButton("Cancel");
			bCancel.addActionListener(this);
			
			buttonPanel.add(bResetAll);
			buttonPanel.add(Box.createHorizontalStrut(10));
			buttonPanel.add(bApply);
			buttonPanel.add(bOK);
			buttonPanel.add(bCancel);
			
			JPanel contentPanel = new JPanel();
			contentPanel.setLayout(new BorderLayout());
			contentPanel.add("North", new JPanelUL(new JPanelIS(bigLabelPanel, 5)));
			contentPanel.add("Center", preferencesContentPanel);
			contentPanel.add("South", buttonPanel);
			
			this.getContentPane().setLayout(new BorderLayout());
			this.getContentPane().add("West", new JPanelIS(treePanel, 7));
			this.getContentPane().add("Center", new JPanelIS(contentPanel, 3));
			
			JPanel rootContentPanel = new JPanel();
			rootContentPanel.setLayout(new BoxLayout(rootContentPanel, BoxLayout.Y_AXIS));
			rootContentPanel.add(new JLabel("Select a node on the left to edit preferences."));
			rootContentPanel.add(Box.createGlue());
			
			putPreferencePanel("ArchStudio 3", new DefaultPreferencePanel());
			/*
			putPreferencePanel("ArchStudio 3/Graph Layout/DOT", null);
			putPreferencePanel("ArchStudio 3/Graph Layout/ABC", null);
			putPreferencePanel("ArchStudio 3/Graph Layout/SUX", null);
			putPreferencePanel("ArchStudio 3/Graph Layout/GEF", null);
			putPreferencePanel("ArchStudio 3/Fark Layout/GEF", null);
			*/
			
			tree.setSelectionRow(0);
			expandTree();
			
			//Toolkit tk = getToolkit();
			//Dimension screenSize = tk.getScreenSize();
			double xSize = 600;
			double ySize = 450;
			
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			addWindowListener(new PreferencesGUIWindowAdapter());
			
			setVisible(true);
			setSize((int)xSize, (int)ySize);
			WidgetUtils.centerInScreen(this);
			WidgetUtils.validateAndRepaintInAWTThread(this);
			//validate();
			//paint(getGraphics());
		}
		
		public void showNode(String nodeToShow){
			String[] nodeNames = nodeToShow.split("/");
			
			TreeNode node = (TreeNode)tree.getModel().getRoot();
			for(int i = 1; i < nodeNames.length; i++){
				boolean found = false;
				for(int j = 0; j < node.getChildCount(); j++){
					TreeNode child = node.getChildAt(j);
					if(child.toString().equals(nodeNames[i])){
						found = true;
						node = child;
					}
				}
				if(!found){
					break;
				}
			}
			tree.setSelectionPath(getNodePath(node));
		}
		
		private synchronized TreePath getNodePath(TreeNode node){
			Vector v = new Vector();
			TreeNode curNode = node;
			while(true){
				v.addElement(curNode);
				curNode = (TreeNode)curNode.getParent();
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
		
		class PreferencesGUIWindowAdapter extends WindowAdapter implements ActionListener{
			public void windowClosing(WindowEvent evt) {
				doReset();
				doClose();
			}
			
			public void actionPerformed(ActionEvent evt){
				doReset();
				doClose();
			}
		}
		
		public void doClose(){
			closeWindow();
		}
		
		protected void expandTree(){
			//Note: this works because the row count actually increases
			//as nodes expand.
			for(int i = 0; i < tree.getRowCount(); i++){
				tree.expandRow(i);
			}
		}

		protected void setNewContentPanel(PreferencePanel p){
			preferencesContentPanel.removeAll();
			JPanel cp = new JPanel(){
				public Insets getInsets(){
					return new Insets(5,5,5,5);
				}
			};
			cp.setLayout(new BorderLayout());
			cp.setBorder(BorderFactory.createEtchedBorder());
			cp.add("Center", p.getComponent());
			
			preferencesContentPanel.add("Center", cp);
			this.validate();
			this.repaint();
		}
		
		protected void setBigLabel(String text){
			bigContentLabel.setText(text);
		}
		
		protected void doApply(){
			for(Iterator it = preferencePanels.values().iterator(); it.hasNext(); ){
				PreferencePanel pp = (PreferencePanel)it.next();
				pp.apply();
			}
			JOptionPane.showMessageDialog(this, 
				"Preferences saved.", "Preferences Saved", JOptionPane.INFORMATION_MESSAGE); 
		}

		protected void doReset(){
			for(Iterator it = preferencePanels.values().iterator(); it.hasNext(); ){
				PreferencePanel pp = (PreferencePanel)it.next();
				pp.reset();
			}					
		}
		
		public void actionPerformed(ActionEvent evt){
			Object src = evt.getSource();
			if(src == bResetAll){
				doReset();
			}
			else if(src == bApply){
				doApply();
			}
			else if(src == bCancel){
				doReset();
				doClose();
			}
			else if(src == bOK){
				doApply();
				doClose();
			}
		}
		
		public void treeNodesChanged(TreeModelEvent arg0){
			expandTree();
		}

		public void treeNodesInserted(TreeModelEvent arg0) {
			expandTree();
		}

		public void treeNodesRemoved(TreeModelEvent arg0) {
			expandTree();
		}

		public void treeStructureChanged(TreeModelEvent arg0) {
			expandTree();
		}

		public void valueChanged(TreeSelectionEvent e){
			setBigLabel(getSelectedPath());
			PreferencePanel p = (PreferencePanel)preferencePanels.get(getSelectedPath());
			if(p == null){
				setNewContentPanel(new DefaultPreferencePanel());
			}
			else{
				setNewContentPanel(p);
			}
		}

		public String getSelectedPath(){
			TreePath tp = tree.getSelectionPath();
			if(tp == null){
				return "";
			}
			int numNodes = tp.getPathCount();
			StringBuffer sb = new StringBuffer();
			for(int i = 0; i < numNodes; i++){
				TreeNode tn = (TreeNode)tp.getPathComponent(i);
				sb.append((((DefaultMutableTreeNode)tn).getUserObject()).toString());
				if(i < (numNodes - 1)){
					sb.append("/");
				}
			}
			return sb.toString();
		}
		
	}
	
}

package archstudio.comp.tron.gui;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import c2.util.MessageSendProxy;

import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;
import archstudio.tron.*;

import edu.uci.ics.widgets.JPanelIS;
import edu.uci.ics.widgets.JPanelTL;
import edu.uci.ics.widgets.JPanelUL;
import edu.uci.ics.widgets.JPanelWL;
import edu.uci.ics.widgets.WidgetUtils;
import edu.uci.ics.xadlutils.Resources;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchFlatInterface;

public class TronGUISelectionHandler{

	protected static final int NEITHER = 0;
	protected static final int TREE = 100;
	protected static final int TABBED = 200;
	
	protected int selectedElement = NEITHER;
	
	protected XArchFlatTransactionsInterface xarch;
	protected MessageSendProxy notificationProxy;
	protected TronGUIEditorModel editorModel;
	protected TronGUITree tree;
	protected TronGUITabbedPane tabbedPane;
	protected TronGUIDescriptionPanel descriptionPanel;
	protected MyTronGUITreeModelListener tronGUITreeModelListener = new MyTronGUITreeModelListener();
	
	public TronGUISelectionHandler(XArchFlatTransactionsInterface xarch, MessageSendProxy notificationProxy, 
	TronGUIEditorModel editorModel, TronGUITree tree, TronGUITabbedPane tabbedPane, TronGUIDescriptionPanel descriptionPanel){
		this.xarch = xarch;
		this.notificationProxy = notificationProxy;
		this.editorModel = editorModel;
		this.tree = tree;
		TronGUITreeModel treeModel = (TronGUITreeModel)tree.getModel();
		treeModel.addTronGUITreeModelListener(tronGUITreeModelListener);
		this.tabbedPane = tabbedPane;
		this.descriptionPanel = descriptionPanel;
		tree.addTreeSelectionListener(new TronTreeSelectionListener());
		tabbedPane.addTronGUITabbedPaneListener(new MyTronGUITabbedPaneListener());
		//table.getSelectionModel().addListSelectionListener(new TronTableSelectionListener());
		displayNoSelection();
	}
	
	class MyTronGUITreeModelListener implements TronGUITreeModelListener{
		public void nodeShouldRefresh(TreeNode n){
			checkSelections();
		}		
	}
	
	class TronTreeSelectionListener implements TreeSelectionListener{
		public void valueChanged(TreeSelectionEvent e){
			if(selectedElement != TREE){
				tabbedPane.clearSelection();
				selectedElement = TREE;
			}
			checkSelections();
		}
	}
	
	class MyTronGUITabbedPaneListener implements TronGUITabbedPaneListener{
		public void stateChanged(TronGUITabbedPane src){
			if(selectedElement != TABBED){
				tree.clearSelection();
				selectedElement = TABBED;
			}
			checkSelections();
		}
	}
	
	protected void checkSelections(){
		TreePath[] selectedPaths = tree.getSelectionPaths();
		Object selectedTabObject = tabbedPane.getSelectedItem();
		if((selectedPaths != null) && (selectedPaths.length > 0)){
			boolean oneVisible = false;
			for(int i = 0; i < selectedPaths.length; i++){
				if(tree.isVisible(selectedPaths[i])){
					oneVisible = true;
					break;
				}
			}
			if(oneVisible){
				handleTreeSelection(selectedPaths);
			}
		}
		else if(selectedTabObject != null){
			handleTabbedSelection(selectedTabObject);
		}
		else{
			//No selection
			displayNoSelection();
		}
	}
	
	protected void handleTreeSelection(TreePath[] selectedPaths){
		TreeNode[] selectedNodes = new TreeNode[selectedPaths.length];
		for(int i = 0; i < selectedPaths.length; i++){
			selectedNodes[i] = (TreeNode)selectedPaths[i].getLastPathComponent();
		}
		
		if(selectedNodes.length == 1){
			if(selectedNodes[0] == tree.getModel().getRoot()){
				displayWelcome();
				return;
			}
			if(selectedNodes[0] instanceof TronGUITreeTestNode){
				TronGUITreeTestNode testNode = (TronGUITreeTestNode)selectedNodes[0];
				if(testNode.isLeaf()){
					//It's a leaf node
					if(testNode.getTest() != null){
						displayManageOneTest(testNode);
						return;
					}
				}
				else{
					//It's an intermediate folder.
					displayManageOneFolder(selectedNodes[0]);
					return;
				}
			}
			else if(selectedNodes[0] instanceof TronGUITreeDocumentNode){
				displayManageOneFolder(selectedNodes[0]);
				return;
			}
		}
		
		displayNoOptions();
	}
	
	protected void handleTabbedSelection(Object selectedObject){
		if(selectedObject instanceof TronIssue){
			displayOneIssue((TronIssue)selectedObject);
			return;
		}
		else if(selectedObject instanceof TronToolNotice){
			displayOneToolNotice((TronToolNotice)selectedObject);
			return;
		}
		displayNoOptions();
	}
	
	protected void displayComponent(JComponent comp){
		descriptionPanel.removeAll();
		descriptionPanel.setBackground(Color.WHITE);
		descriptionPanel.setLayout(new BorderLayout());
		
		java.awt.Component[] allComponents = WidgetUtils.getHierarchyRecursive(comp);
		for(int i = 0; i < allComponents.length; i++){
			if(allComponents[i] instanceof JPanel){
				((JPanel)allComponents[i]).setOpaque(false);
			}
			else if(allComponents[i] instanceof JButton){
				((JButton)allComponents[i]).setOpaque(false);
			}
		}
		
		descriptionPanel.add("Center", comp);
		descriptionPanel.validate();
		descriptionPanel.repaint();
		
		Container ancestor = descriptionPanel.getParent();
		while(true){
			if(ancestor == null) return;
			if(ancestor instanceof JScrollPane){
				ancestor.validate();
				((JScrollPane)ancestor).getVerticalScrollBar().setValue(0);
				((JScrollPane)ancestor).getHorizontalScrollBar().setValue(0);
				return;
			}
			ancestor = ancestor.getParent();
		}
	}
	
	protected void displayWelcome(){
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		Icon icon = TronGUIUtils.TRON_LOGO_32;
		
		JLabel headlineLabel = new JLabel(
			"<html>" +
			"<div style=\"font-size: 20pt; font-weight: bold; margin-bottom: 5px;\">" +
			"Welcome to Tron" + 
			"</div>" +
			"</html>"
		);
		
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BorderLayout());
		labelPanel.add("Center", new JPanelWL(headlineLabel, labelPanel));
		
		JPanel headlinePanel = new JPanel();
		headlinePanel.setLayout(new BorderLayout());
		headlinePanel.add("Center", labelPanel);
		
		if(icon != null){
			JLabel iconLabel = new JLabel(icon);
			headlinePanel.add("East", iconLabel);
		}
		
		JLabel textLabel = new JLabel(
			"<html><div style=\"font-weight: normal\">" +
			"Tron is ArchStudio 3's architecture analysis framework.  It " +
			"is a set of components that " +
			"unify architectural analysis capabilities provided by a " +
			"variety of analysis tools under a single user interface, which " +
			"you are currently using." +
			"<br><br>The tree to the left displays open documents " +
			"and the tests that are applied to those documents (i.e. that those " +
			"documents are expected to pass)." +
			"<br><br>The pane above displays all issues " +
			"detected during analysis.  By using the tabs below the pane, you can " +
			"navigate to the tool consoles for the various analysis tools that are part " +
			"of the Tron framework to see status messages from those tools." +
			"<br><br>This pane " +
			"allows you to see the status of any selected test group, test, issue, or " +
			"tool notice." +
			"</div></html>"
		);
		
		mainPanel.add(headlinePanel);
		mainPanel.add(new JPanelWL(textLabel, mainPanel));
		JPanel tPanel = new JPanel();
		tPanel.setLayout(new BorderLayout());
		tPanel.add("North", mainPanel);
		displayComponent(tPanel);
	}
	
	protected void displayNoOptions(){
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JLabel label = new JLabel(
			"<html>" +
			"<div style=\"font-size: 20pt; font-weight: bold; margin-bottom: 5px;\">No Options Available</div>" +
			"<p>The selected item(s) do not have any options available. " +
			"<p>Please make a different selection." +
			"</html>"
		);
		mainPanel.add(new JPanelUL(label));
		displayComponent(mainPanel);
	}

	protected void displayNoSelection(){
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JLabel label = new JLabel(
			"<html>" +
			"<div style=\"font-size: 20pt; font-weight: bold; margin-bottom: 5px;\">No Selection</div>" +
			"<p>Select an item or items to display options. " +
			"</html>"
		);
		mainPanel.add(new JPanelUL(label));
		displayComponent(mainPanel);
	}
	
	private JComponent getTestOptionsComponent(TronGUITreeTestNode node){
		if(node.getTest() == null){
			return null;
		}

		String testCategory = node.getTest().getCategory();
		String testDescription = node.getTest().getLongDescription();
		
		JPanel testOptionsPanel = new JPanel();
		testOptionsPanel.setLayout(new BoxLayout(testOptionsPanel, BoxLayout.Y_AXIS));

		JLabel descLabel = new JLabel("<html><div style=\"font-weight: normal; margin-bottom: 2px\">" +
			testDescription + "</div></html>");
		testOptionsPanel.add(new JPanelWL(descLabel, testOptionsPanel));
		
		if(node.isApplied()){
			if(node.isEnabled()){
				JLabel stateLabel = 
					new JLabel("<html><div style=\"font-weight: normal; margin-bottom: 2px\">" +
							"<b>Applied/Enabled</b></div></html>");
				testOptionsPanel.add(new JPanelWL(stateLabel, descriptionPanel));
			}
			else{
				JLabel stateLabel = 
					new JLabel("<html><div style=\"font-weight: normal; margin-bottom: 2px\">" +
							"<b>Applied/Disabled</b></div></html>");
				testOptionsPanel.add(new JPanelWL(stateLabel, descriptionPanel));
			}
		}
		else{
			JLabel stateLabel = 
				new JLabel("<html><div style=\"font-weight: normal; margin-bottom: 2px\">" +
						"<b>Not Applied</b></div></html>");
			testOptionsPanel.add(new JPanelWL(stateLabel, descriptionPanel));
		}
		if(node.isUnknown()){
			JLabel unknownLabel = 
				new JLabel("<html><div style=\"font-weight: normal; margin-bottom: 2px\">" +
					"This test is unknown.  This means that it is applied in the document, but " +
					"no tool reports that it can perform the test.  This can result from a " +
					"tool misconfiguration, an applied test that no longer exists, or a tool error. " +
					"If you un-apply this test, it will disappear and no longer be available " +
					"since no record of the test will remain.</div></html>");
			testOptionsPanel.add(new JPanelWL(unknownLabel, descriptionPanel));
		}

		JMenuItem[] menuItems = TronGUIUtils.getChangeTestStateMenuItems(xarch, node);
		JButton popupButton = WidgetUtils.getPopupMenuButton(
			"Change Test State...", node.getIcon(), menuItems);
		testOptionsPanel.add(Box.createVerticalStrut(5));
		testOptionsPanel.add(new JPanelIS(new JPanelUL(popupButton), 5));
		return new JPanelTL(testOptionsPanel, "Test Data/Options", 3);
	}
	
	private JComponent getTestGroupOptionsComponent(TreeNode node){
		if(node.getChildCount() == 0){
			return null;
		}

		JPanel testGroupOptionsPanel = new JPanel();
		testGroupOptionsPanel.setLayout(new BoxLayout(testGroupOptionsPanel, BoxLayout.Y_AXIS));

		JLabel descLabel = new JLabel("<html><div style=\"font-weight: normal\">This folder groups related tests.</div></html>");
		testGroupOptionsPanel.add(new JPanelWL(descLabel, testGroupOptionsPanel));
		//testOptionsPanel.add(Box.createVerticalStrut(4));

		JMenuItem[] menuItems = TronGUIUtils.getChangeTestGroupStateMenuItems(xarch, node);
		Icon icon = edu.uci.ics.xadlutils.Resources.FOLDER_ICON;
		
		JButton popupButton = WidgetUtils.getPopupMenuButton(
			"Change State of All Tests...", icon, menuItems);
		testGroupOptionsPanel.add(Box.createVerticalStrut(5));
		testGroupOptionsPanel.add(new JPanelIS(new JPanelUL(popupButton), 5));
		return new JPanelTL(testGroupOptionsPanel, "Test Group Options", 3);
	}
	
	protected void displayManageOneTest(TronGUITreeTestNode node){
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		TronGUITreeDocumentNode docNode = TronGUIUtils.getDocumentNode(node);
		ObjRef xArchRef = null;
		if(docNode != null){
			xArchRef = docNode.getDocumentRef();
		}
		
		JLabel headlineLabel = new JLabel(
			"<html>" +
			"<div style=\"font-size: 20pt; font-weight: bold; margin-bottom: 5px;\">Manage Test</div>" +
			"</html>"
		);
		mainPanel.add(new JPanelUL(headlineLabel));
		JComponent testOptionsPanel = getTestOptionsComponent(node);
		if(testOptionsPanel != null){
			mainPanel.add(testOptionsPanel);
		}
		
		JPanel tPanel = new JPanel();
		tPanel.setLayout(new BorderLayout());
		tPanel.add("North", mainPanel);
		displayComponent(tPanel);
	}

	protected void displayOneIssue(TronIssue issue){
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		String issueIconHref = issue.getIconHref();
		int issueSeverity = issue.getSeverity();
		Icon icon = null;
		if(issueIconHref != null){
			icon = TronGUIUtils.getIconByURL(issueIconHref);
		}
		//If that failed or none was provided...
		if(icon == null){
			switch(issueSeverity){
			case TronIssue.SEVERITY_ERROR:
				icon = edu.uci.ics.xadlutils.Resources.ERROR_ICON_32;
				break;
			case TronIssue.SEVERITY_WARNING:
				icon = edu.uci.ics.xadlutils.Resources.WARNING_ICON_32;
				break;
			default:
				icon = edu.uci.ics.xadlutils.Resources.INFO_ICON_32;
				break;
			}
		}
		
		String issueHeadline = issue.getHeadline();
		JLabel headlineLabel = new JLabel(
			"<html>" +
			"<div style=\"font-size: 20pt; font-weight: bold; margin-bottom: 5px;\">" +
			issueHeadline + 
			"</div>" +
			"</html>"
		);
		
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BorderLayout());
		labelPanel.add("Center", new JPanelWL(headlineLabel, labelPanel));
		
		JPanel headlinePanel = new JPanel();
		headlinePanel.setLayout(new BorderLayout());
		headlinePanel.add("Center", labelPanel);
		
		if(icon != null){
			JLabel iconLabel = new JLabel(icon);
			headlinePanel.add("East", iconLabel);
		}
		
		mainPanel.add(headlinePanel);
		mainPanel.add(getIssueOptionsComponent(issue));
		JPanel tPanel = new JPanel();
		tPanel.setLayout(new BorderLayout());
		tPanel.add("North", mainPanel);
		displayComponent(tPanel);
	}

	private JComponent getIssueOptionsComponent(TronIssue issue){
		JPanel issueOptionsPanel = new JPanel();
		issueOptionsPanel.setLayout(new BoxLayout(issueOptionsPanel, BoxLayout.Y_AXIS));

		ObjRef issueDocumentRef = issue.getDocumentRef();
		TronGUITreeModel treeModel = (TronGUITreeModel)tree.getModel();
		String issueTestUID = issue.getTestUID();
		TronGUITreeDocumentNode docNode = treeModel.getDocumentNode(issueDocumentRef);
		TronGUITreeTestNode testNode = null;
		if(docNode != null){
			testNode = treeModel.getTestNode(docNode, issueTestUID);
		}

		String issueDetail = issue.getDetailedDescription();
		if(issueDetail != null){
			JLabel descLabel = new JLabel("<html><div style=\"font-weight: normal; margin-bottom: 2px\">" + 
				issueDetail + "</div></html>");
			issueOptionsPanel.add(new JPanelWL(descLabel, issueOptionsPanel));
		}
		
		if(testNode != null){
			TronTest failedTest = testNode.getTest();
			if(failedTest != null){
				String testName = failedTest.getCategory();
				JLabel testLabel = new JLabel("<html><div style=\"font-weight: normal; margin-bottom: 2px\"><b>Test:</b> " + 
					testName + "</div></html>");
				issueOptionsPanel.add(new JPanelWL(testLabel, issueOptionsPanel));
			}
		}
		
		String docName = "[unknown]";
		if(docNode != null){
			docName = docNode.toString();
		}
		JLabel docLabel = new JLabel("<html><div style=\"font-weight: normal; margin-bottom: 2px\"><b>Document:</b> " + 
			docName + "</div></html>");
		issueOptionsPanel.add(new JPanelWL(docLabel, issueOptionsPanel));
		
		String issueToolID = issue.getToolID();
		JLabel toolLabel = new JLabel("<html><div style=\"font-weight: normal; margin-bottom: 2px\"><b>Tool:</b> " + 
			issueToolID + "</div></html>");
		issueOptionsPanel.add(new JPanelWL(toolLabel, issueOptionsPanel));
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		if(docNode != null){
			ObjRef documentRef = docNode.getDocumentRef();
			TronElementIdentifier[] issueElementIdentifiers = issue.getElementIdentifiers();
			
			JPanel focusButtonsPanel = buttonsPanel;
			if(issueElementIdentifiers.length > 1){
				focusButtonsPanel = new JPanel();
				focusButtonsPanel.setLayout(new GridLayout(issueElementIdentifiers.length, 1));
				buttonsPanel.add(focusButtonsPanel);
			}
			
			for(int i = 0; i < issueElementIdentifiers.length; i++){
				TronElementIdentifier ei = issueElementIdentifiers[i];
				ObjRef ref = ei.getElementRef();
				if(ref == null){
					String id = ei.getElementID();
					ref = xarch.getByID(docNode.getDocumentRef(), id);
				}
				if(ref != null){
					String desc = ei.getElementDescription();
					if(desc == null){
						desc = "Element";
					}
					Icon icon = Resources.EDIT_ICON;
					JMenuItem[] focusEditorMenuItems = TronGUIUtils.getFocusEditorMenuItems(
						notificationProxy, documentRef, ref, editorModel);
					JButton focusButton = WidgetUtils.getPopupMenuButton("Focus Editor: " + desc,
						icon, focusEditorMenuItems);
					focusButtonsPanel.add(focusButton);
				}
			}
		}
		
		if(testNode != null){
			JButton selectTestButton = new JButton("Go to Test"){
				public Dimension getPreferredSize(){
					Dimension d = super.getPreferredSize();
					d.height += 2;
					return d;
				}
			};
			selectTestButton.setIcon(testNode.getIcon());
			selectTestButton.addActionListener(getSelectTestActionListener(testNode));
			buttonsPanel.add(selectTestButton);
		}
		
		issueOptionsPanel.add(Box.createVerticalStrut(4));
		issueOptionsPanel.add(new JPanelUL(buttonsPanel));

		
		/*
		JMenuItem[] menuItems = TronGUIUtils.getChangeTestGroupStateMenuItems(xarch, node);
		Icon icon = edu.uci.ics.xadlutils.Resources.FOLDER_ICON;
		
		JButton popupButton = WidgetUtils.getPopupMenuButton(
			"Change State of All Tests...", icon, menuItems);
		testGroupOptionsPanel.add(Box.createVerticalStrut(5));
		testGroupOptionsPanel.add(new JPanelIS(new JPanelUL(popupButton), 5));
		*/
		return new JPanelTL(issueOptionsPanel, "Issue Info", 3);
	}

	protected void displayManageOneFolder(TreeNode node){
		TronGUITreeDocumentNode docNode = TronGUIUtils.getDocumentNode(node);
		ObjRef xArchRef = null;
		if(docNode != null){
			xArchRef = docNode.getDocumentRef();
		}

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JLabel headlineLabel = new JLabel(
			"<html>" +
			"<div style=\"font-size: 20pt; font-weight: bold; margin-bottom: 5px;\">Manage Test Group</div>" +
			"</html>"
		);
		mainPanel.add(new JPanelUL(headlineLabel));
		
		TronTest tronTest = null;
		if(node instanceof TronGUITreeTestNode){
			tronTest = ((TronGUITreeTestNode)node).getTest();
		}
		if(tronTest != null){
			//This folder has a test.
			JComponent testOptionsPanel = getTestOptionsComponent((TronGUITreeTestNode)node);
			if(testOptionsPanel != null){
				mainPanel.add(testOptionsPanel);
				mainPanel.add(Box.createVerticalStrut(5));
			}
		}
		
		JComponent testGroupOptionsPanel = getTestGroupOptionsComponent(node);
		if(testGroupOptionsPanel != null){
			mainPanel.add(testGroupOptionsPanel);
			mainPanel.add(Box.createVerticalStrut(5));
		}
		
		JPanel tPanel = new JPanel();
		tPanel.setLayout(new BorderLayout());
		tPanel.add("North", mainPanel);
		displayComponent(tPanel);
	}

	protected void displayOneToolNotice(TronToolNotice toolNotice){
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		String headline = toolNotice.getMessage();
		JLabel headlineLabel = new JLabel(
			"<html>" +
			"<div style=\"font-size: 16pt; font-weight: bold; margin-bottom: 5px;\">" +
			headline +
			"</div>" +
			"</html>"
		);
		mainPanel.add(new JPanelWL(headlineLabel, mainPanel));
		
		String detail = toolNotice.getAdditionalDetail();
		if(detail == null){
			detail = "[No additional detail]";
		}
		JLabel detailLabel = new JLabel(
			"<html><div style=\"font-weight: normal; margin-bottom: 2px\">" + 
			detail + 
			"</div></html>");
		mainPanel.add(new JPanelWL(detailLabel, mainPanel));
		
		Throwable error = toolNotice.getError();
		if(error != null){
			JTextArea ta = new JTextArea();
			ta.setEditable(false);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			error.printStackTrace(pw);
			String output = sw.toString();
			ta.setText(output);
			JScrollPane taPane = new JScrollPane(ta);
			WidgetUtils.adjustScrollPaneMovement(taPane);
			mainPanel.add(Box.createVerticalStrut(5));
			mainPanel.add(new JPanelWL(taPane, mainPanel));
		}

		JPanel tPanel = new JPanel();
		tPanel.setLayout(new BorderLayout());
		tPanel.add("North", mainPanel);
		displayComponent(tPanel);
	}
	
	private ActionListener getSelectTestActionListener(final TronGUITreeTestNode testNode){
		return new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				tree.setSelectedNode(testNode);
			}
		};
	}

}

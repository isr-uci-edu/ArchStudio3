package archstudio.comp.tron.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.TreeNode;

import c2.util.MessageSendProxy;
import edu.uci.ics.widgets.WidgetUtils;

public class TronGUIToolbar extends JToolBar{

	protected MessageSendProxy requestProxy;
	protected TronGUITreeModel treeModel;
	
	protected MyTronGUITreeModelListener tronGuiTreeModelListener = 
		new	MyTronGUITreeModelListener();
	protected JButton bTestAll = null;
	protected JButton bTestDoc = null;
	protected TronGUITestErrorBar errorBar;
	
	public TronGUIToolbar(MessageSendProxy requestProxy, TronGUITreeModel treeModel, TronGUITestErrorBar errorBar){
		super("Tron Toolbar", JToolBar.HORIZONTAL);
		this.requestProxy = requestProxy;
		this.treeModel = treeModel;
		this.errorBar = errorBar;
		treeModel.addTronGUITreeModelListener(tronGuiTreeModelListener);
		refreshButtons();
	}
	
	class MyTronGUITreeModelListener implements TronGUITreeModelListener{
		public void nodeShouldRefresh(TreeNode n){
			refreshButtons();
		}
	}
	
	protected void refreshButtons(){
		refreshTestAllButton();
		refreshTestDocButton();
	}
	
	protected void refreshTestAllButton(){
		int testAllIndex = -1;
		if(bTestAll != null){
			testAllIndex = getComponentIndex(bTestAll);
			remove(bTestAll);
		}
		bTestAll = new JButton("Test All", edu.uci.ics.xadlutils.Resources.GO_ICON);
		TronGUITreeDocumentNode[] docNodes = treeModel.getDocumentNodes();
		bTestAll.addActionListener(new TronGUIUtils.TestAllActionListener(requestProxy, docNodes, errorBar));
		add(bTestAll, testAllIndex);
		validate();
	}
	
	protected void refreshTestDocButton(){
		int testDocIndex = -1;
		if(bTestDoc != null){
			testDocIndex = getComponentIndex(bTestDoc);
			remove(bTestDoc);
		}
		
		TronGUITreeDocumentNode[] docNodes = treeModel.getDocumentNodes();
		JMenuItem[] runDocTestMenuItems = TronGUIUtils.getRunDocTestMenuItems(requestProxy, docNodes, errorBar);
		
		bTestDoc = WidgetUtils.getPopupMenuButton("Test Document...", 
			edu.uci.ics.xadlutils.Resources.GO_ICON, runDocTestMenuItems);
		
		add(bTestDoc, testDocIndex);
		validate();
	}
	
}

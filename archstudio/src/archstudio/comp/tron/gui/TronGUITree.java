package archstudio.comp.tron.gui;

import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.event.*;
import javax.swing.tree.*;

import edu.uci.ics.widgets.IconableTreeCellRenderer;
import edu.uci.ics.widgets.WidgetUtils;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchFlatInterface;

class TronGUITree extends JTree{
	
	protected MyTreeModelListener myTreeModelListener = new MyTreeModelListener();
	
	public TronGUITree(TronGUITreeModel model){
		super(model);
		IconableTreeCellRenderer cellRenderer = new IconableTreeCellRenderer()/*{
			public java.awt.Color getBackground(){
				return null;
			}
		}*/;
		cellRenderer.setCustomIconsOnlyForLeafs(false);
		setRowHeight(18);
		setCellRenderer(cellRenderer);
		setRootVisible(true);
		model.addTronGUITreeModelListener(myTreeModelListener);
	}
	
	class MyTreeModelListener implements TronGUITreeModelListener{
		public void nodeShouldRefresh(TreeNode n){
			refreshNode(n);
		}
		/*
		public void treeNodesChanged(TreeModelEvent e){
			System.out.println("got nodes changed event: " + e);
			Object src = e.getSource();
			if(src instanceof TreeNode){
				refreshNode((TreeNode)src);
			}
		}
		public void treeNodesInserted(TreeModelEvent e){
		}
		public void treeNodesRemoved(TreeModelEvent e){
		}
		public void treeStructureChanged(TreeModelEvent e){
			System.out.println("got structure changed event: " + e);
			Object src = e.getSource();
			if(src instanceof TreeNode){
				refreshNode((TreeNode)src);
			}
		}
		*/
	}
	
	public TronGUITreeModel getTronGUITreeModel(){
		return (TronGUITreeModel)getModel();
	}
	
	
	public String getToolTipText(MouseEvent evt){
		try{
			int selRow = getRowForLocation(evt.getX(), evt.getY());
      TreePath selPath = getPathForLocation(evt.getX(), evt.getY());
      if(selRow != -1){
      	Object lastPathComponent = selPath.getPathComponent(selPath.getPathCount() - 1);
      	if(lastPathComponent instanceof TronGUITreeDocumentNode){
      		return ((TronGUITreeDocumentNode)lastPathComponent).getDocumentURI();
      	}
      	else if(lastPathComponent instanceof TreeNode){
      		return lastPathComponent.toString();
      	}
      }
		}
		catch(Exception e){
		}
		return null;
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
	
	public synchronized void refreshNode(TreeNode node){
		Vector toggledPaths = new Vector();
		Enumeration expen = getExpandedDescendants(getNodePath(node));
		if(expen != null){
			while(expen.hasMoreElements()){
				toggledPaths.addElement(expen.nextElement());
			}
		}
		TreePath selectedPath = getSelectionPath();
		DefaultTreeModel tm = (DefaultTreeModel)getModel();
		tm.nodeStructureChanged(node);
		for(Enumeration en = toggledPaths.elements(); en.hasMoreElements(); ){
			TreePath path = (TreePath)en.nextElement();
			try{
				expandPath(path);
			}
			catch(Exception e){}
		}
		try{
			setSelectionPath(selectedPath);
		}
		catch(Exception e){}
	}
	
	public Insets getInsets(){
		return new Insets(3, 3, 3, 3);
	}
	
	public void setSelectedNode(TreeNode n){
		java.util.List nodeList = new ArrayList();
		while(n != null){
			nodeList.add(n);
			n = n.getParent();
		}
		java.util.Collections.reverse(nodeList);
		Object[] pathElements = nodeList.toArray();
		TreePath pathToSelect = new TreePath(pathElements);
		setSelectionPath(pathToSelect);
	}

} /* Tree */



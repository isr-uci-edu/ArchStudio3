
package archstudio.comp.archipelago;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.dnd.peer.*;
import java.awt.event.*;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.tree.*;

import edu.uci.ics.widgets.IconableTreeCellRenderer;

public class ArchipelagoTree extends JTree {

	protected DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Archipelago");
	protected DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);

	private DragSource dragSource = null;

	public ArchipelagoTree(DragGestureListener dgl){
		super();
		super.setModel(treeModel);
		super.setCellRenderer(new IconableTreeCellRenderer());
		if(dgl != null){
			setupDND(dgl);
		}
		ToolTipManager.sharedInstance().registerComponent(this);
	}
	
	public DragSource getDragSource(){
		return dragSource;
	}
	
	private void setupDND(DragGestureListener dgl){
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
			dgl                              //DragGestureListener
		  );
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

	public DefaultTreeModel getTreeModel(){
		return treeModel;
	}
	
	public DefaultMutableTreeNode getRootNode(){
		return rootNode;
	}
	
	public String getToolTipText(MouseEvent evt){
		TreePath tp = getPathForLocation(evt.getX(), evt.getY());
		if(tp != null){
			Object lpc = tp.getLastPathComponent();
			if(lpc != null){
				String ttt = lpc.toString();
				return ttt;
			}
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
		//Fix for JDK1.4.0
		//for(Enumeration en = getDescendantToggledPaths(getNodePath(node)); en.hasMoreElements(); ){
		Enumeration expen = getExpandedDescendants(getNodePath(node));
		if(expen != null){
			while(expen.hasMoreElements()){
				toggledPaths.addElement(expen.nextElement());
			}
		}
		TreePath selectedPath = getSelectionPath();
		DefaultTreeModel tm = (DefaultTreeModel)getModel();
		//tm.nodeStructureChanged(node);
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
		
		tm.nodeStructureChanged(node);
	}
	

}

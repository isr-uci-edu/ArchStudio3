package archstudio.comp.tron.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;

import archstudio.tron.TronTest;

import edu.uci.ics.xarchutils.XArchFlatInterface;

public class TronGUITreeClickHandler extends MouseAdapter{

	protected TronGUITree tree;
	protected XArchFlatInterface xarch;
	
	public TronGUITreeClickHandler(TronGUITree tree, XArchFlatInterface xarch){
		this.tree = tree;
		this.xarch = xarch;
		tree.addMouseListener(this);
	}
	
	public void mousePressed(MouseEvent e){
		handlePopup(e);
	}

	public void mouseReleased(MouseEvent e){
		handlePopup(e);
	}
	
	protected void handlePopup(MouseEvent e){
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
					if(o == null){
						return false;
					}
					if(o instanceof TronGUITreeDocumentNode){
						TronGUITreeDocumentNode docNode = (TronGUITreeDocumentNode)o;
						JPopupMenu popup = new JPopupMenu();
						JMenuItem[] folderMenuItems = TronGUIUtils.getChangeTestGroupStateMenuItems(xarch, docNode);
						for(int i = 0; i < folderMenuItems.length; i++){
							popup.add(folderMenuItems[i]);
						}
						popup.show(e.getComponent(), e.getX(), e.getY());
						return true;
					}
					else if(o instanceof TronGUITreeTestNode){
						TronGUITreeTestNode testNode = (TronGUITreeTestNode)o;
						JPopupMenu popup = new JPopupMenu();
						if(testNode.getTest() != null){
							JMenuItem[] testMenuItems = TronGUIUtils.getChangeTestStateMenuItems(xarch, testNode);
							for(int i = 0; i < testMenuItems.length; i++){
								popup.add(testMenuItems[i]);
							}
						}
						if(testNode.getChildCount() != 0){
							if(testNode.getTest() != null){
								popup.add(new JSeparator());
							}
							JMenuItem[] folderMenuItems = TronGUIUtils.getChangeTestGroupStateMenuItems(xarch, testNode);
							for(int i = 0; i < folderMenuItems.length; i++){
								popup.add(folderMenuItems[i]);
							}
						}
						popup.show(e.getComponent(), e.getX(), e.getY());
						return true;
					}
				}
			}
		}
		return false;
	}	
}

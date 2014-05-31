
package edu.uci.ics.bna.contextmenu;

import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.*;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.logic.*;

public class SelectionBasedContextMenuLogic extends AbstractContextMenuLogic{

	protected SelectionTrackingLogic stl;
	protected Vector plugins;
	
	public SelectionBasedContextMenuLogic(SelectionTrackingLogic stl){
		super();
		plugins = new Vector();
		this.stl = stl;
	}

	public void addPlugin(SelectionBasedContextMenuPlugin plugin){
		synchronized(plugins){
			plugins.addElement(plugin);
		}
	}
	
	public void removePlugin(SelectionBasedContextMenuPlugin plugin){
		synchronized(plugins){
			plugins.removeElement(plugin);
		}
	}

	static int sc = 0;
	
	protected void checkAndDoPopup(Thing t, MouseEvent evt, int worldX, int worldY){
		if(evt.isPopupTrigger()){
			if(t == null){
				//System.err.println("nothing was selected " + sc++);
				//Select nothing; do popup
				SelectionUtils.removeAllSelections(getBNAComponent());
			}
			else if(t instanceof ISelectable){
				ISelectable st = (ISelectable)t;
				if(st.isSelected()){
					//Do popup for all selected things
				}
				else{
					//Select only this thing, do popup
					SelectionUtils.removeAllSelections(getBNAComponent());
					st.setSelected(true);
				}
			}
			else{
				//It's not selectable; select nothing; do popup
				SelectionUtils.removeAllSelections(getBNAComponent());
			}
			super.checkAndDoPopup(t, evt, worldX, worldY);
			/*
			JPopupMenu contextMenu = getContextMenu(t, worldX, worldY);
			if(contextMenu != null){
				contextMenu.show(evt.getComponent(), evt.getX(), evt.getY());
			}
			*/
		}
	}

	public JPopupMenu getContextMenu(Thing t, int worldX, int worldY) {
		//Wait for the selections to settle down here before we go about dealing with them.
		BNAComponent c = getBNAComponent();
		if(c != null){
			BNAModel m = c.getModel();
			if(m instanceof DefaultBNAModel){
				((DefaultBNAModel)m).waitForProcessing();
			}
		}
		Thing[] currentSelectedThingSet = stl.getSelectedThings();
		JPopupMenu popupMenu = new JPopupMenu();
		synchronized(plugins){
			for(Iterator it = plugins.iterator(); it.hasNext(); ){
				SelectionBasedContextMenuPlugin plugin = (SelectionBasedContextMenuPlugin)it.next();
				popupMenu = plugin.addToContextMenu(popupMenu, currentSelectedThingSet, t);
			}
		}
		if(popupMenu.getComponentCount() == 0){
			//It's empty, none of the plugins added anything.
			JMenuItem miNone = new JMenuItem("[No operations for selected set]");
			miNone.setEnabled(false);
			popupMenu.add(miNone);
		}
		return popupMenu;
	}
	
	public static SelectionBasedContextMenuLogic getSelectionBasedContextMenuLogic(BNAComponent c){
		ThingLogic[] logics = c.getThingLogics();
		for(int i = 0; i < logics.length; i++){
			if(logics[i] instanceof SelectionBasedContextMenuLogic){
				return (SelectionBasedContextMenuLogic)logics[i];
			}
		}
		return null;
	}

}


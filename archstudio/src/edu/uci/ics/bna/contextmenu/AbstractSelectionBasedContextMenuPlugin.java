package edu.uci.ics.bna.contextmenu;

import javax.swing.JPopupMenu;

import edu.uci.ics.bna.*;

public abstract class AbstractSelectionBasedContextMenuPlugin
implements SelectionBasedContextMenuPlugin {

	protected BNAComponent c;
	
	public AbstractSelectionBasedContextMenuPlugin(BNAComponent bna){
		super();
		this.c = bna;
	}

	public BNAComponent getBNAComponent(){
		return c;
	}
	
	public abstract JPopupMenu addToContextMenu(JPopupMenu currentContextMenu, Thing[] selectedThingSet, Thing thingUnderCursor);
}

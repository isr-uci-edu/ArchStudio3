
package edu.uci.ics.bna.contextmenu;

import javax.swing.*;
import edu.uci.ics.bna.*;

interface SelectionBasedContextMenuPlugin{
	public JPopupMenu addToContextMenu(JPopupMenu currentContextMenu, Thing[] selectedThingSet, Thing thingUnderCursor);
}
package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.contextmenu.*;
import edu.uci.ics.bna.swingthing.*;
import edu.uci.ics.widgets.windowheader.*;

import edu.uci.ics.xarchutils.*;

public class RemoveStructureElementContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{
	
	protected XArchFlatInterface xarch;
	protected ThingIDMap thingIDMap;
	
	public RemoveStructureElementContextMenuPlugin(BNAComponent c, ThingIDMap thingIDMap, XArchFlatInterface xarch){
		super(c);
		this.thingIDMap = thingIDMap;
		this.xarch = xarch;
	}

	public JPopupMenu addToContextMenu(JPopupMenu currentContextMenu, Thing[] selectedThingSet, Thing thingUnderCursor){
		Thing t = null;
		if(selectedThingSet.length == 0){
			if(thingUnderCursor != null){
				t = thingUnderCursor;
			}
			else{
				return currentContextMenu;
			}
		}
		else if(selectedThingSet.length > 1){
			return currentContextMenu;
		}
		else{
			t = selectedThingSet[0];
		}
		
		if(
		(t instanceof ComponentThing) ||
		(t instanceof ConnectorThing) ||
		(t instanceof LinkThing) ||
		(t instanceof InterfaceThing)
		){
			RemoveElementMenuItemSet edmis = new RemoveElementMenuItemSet(t);
			JMenuItem[] miArray = edmis.getMenuItemSet();
			if(miArray.length > 0){
				currentContextMenu.addSeparator();
			}
			for(int i = 0; i < miArray.length; i++){
				currentContextMenu.add(miArray[i]);
			}
		}
		
		return currentContextMenu;
	}
	
	class RemoveElementMenuItemSet implements ActionListener{
		protected Thing thingToRemove;
		protected JMenuItem miRemove;
		
		public RemoveElementMenuItemSet(Thing thingToRemove){
			this.thingToRemove = thingToRemove;
			miRemove = new JMenuItem("Remove");
			miRemove.addActionListener(this);
		}
		
		public JMenuItem[] getMenuItemSet(){
			return new JMenuItem[]{miRemove};
		}
		
		public void actionPerformed(ActionEvent evt){
			if(thingToRemove instanceof ComponentThing){
				String thingToRemoveId = thingToRemove.getID();
				ObjRef thingToRemoveRef = thingIDMap.getXArchRef(thingToRemoveId);
				
				ObjRef archStructureRef = xarch.getParent(thingToRemoveRef);
				
				xarch.remove(archStructureRef, "component", thingToRemoveRef);
			}
			else if(thingToRemove instanceof ConnectorThing){
				String thingToRemoveId = thingToRemove.getID();
				ObjRef thingToRemoveRef = thingIDMap.getXArchRef(thingToRemoveId);
				
				ObjRef archStructureRef = xarch.getParent(thingToRemoveRef);
				
				xarch.remove(archStructureRef, "connector", thingToRemoveRef);
			}
			else if(thingToRemove instanceof LinkThing){
				String thingToRemoveId = thingToRemove.getID();
				ObjRef thingToRemoveRef = thingIDMap.getXArchRef(thingToRemoveId);
				
				ObjRef archStructureRef = xarch.getParent(thingToRemoveRef);
				
				xarch.remove(archStructureRef, "link", thingToRemoveRef);
			}
			else if(thingToRemove instanceof InterfaceThing){
				String thingToRemoveId = thingToRemove.getID();
				ObjRef thingToRemoveRef = thingIDMap.getXArchRef(thingToRemoveId);
				
				ObjRef brickRef = xarch.getParent(thingToRemoveRef);
				
				xarch.remove(brickRef, "interface", thingToRemoveRef);
			}
		}
	}
}

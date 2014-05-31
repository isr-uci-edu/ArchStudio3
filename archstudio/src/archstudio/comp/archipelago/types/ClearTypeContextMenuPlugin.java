package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import c2.util.UIDGenerator;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.contextmenu.*;
import edu.uci.ics.bna.swingthing.*;
import edu.uci.ics.widgets.windowheader.*;

import edu.uci.ics.xarchutils.*;

public class ClearTypeContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{
	
	protected XArchFlatInterface xarch;
	protected ThingIDMap thingIDMap;
	
	public ClearTypeContextMenuPlugin(BNAComponent c, ThingIDMap thingIDMap, XArchFlatInterface xarch){
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
		
		if(t instanceof IHasXadlType){
			ClearTypeMenuItemSet nsmis = new ClearTypeMenuItemSet(t);
			JMenuItem[] miArray = nsmis.getMenuItemSet();
			for(int i = 0; i < miArray.length; i++){
				currentContextMenu.add(miArray[i]);
			}
		}
		
		return currentContextMenu;
	}
	
	class ClearTypeMenuItemSet implements ActionListener{
		protected JMenuItem miClearType;
		protected Thing t;
				
		public ClearTypeMenuItemSet(Thing t){
			this.t = t;
			miClearType = new JMenuItem("Clear Type");
			miClearType.addActionListener(this);
			
			ObjRef typedThingRef = thingIDMap.getXArchRef(t.getID());
			if(typedThingRef == null){
				miClearType.setEnabled(false);
			}
			
			ObjRef typeLinkRef = (ObjRef)xarch.get(typedThingRef, "type");
			if(typeLinkRef == null){
				miClearType.setEnabled(false);
			}
		}
		
		public JMenuItem[] getMenuItemSet(){
			return new JMenuItem[]{miClearType};
		}
		
		public void actionPerformed(ActionEvent evt){
			if(evt.getSource() == miClearType){
				ObjRef typedThingRef = thingIDMap.getXArchRef(t.getID());
				if(typedThingRef == null){
					JOptionPane.showMessageDialog(c, "Thing not mapped to xArch element.", "Can't add interface.", JOptionPane.ERROR_MESSAGE);
					return;
				}
				xarch.clear(typedThingRef, "type");
			}
		}
	}
}

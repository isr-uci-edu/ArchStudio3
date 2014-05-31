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

public class ClearSubarchitectureContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{
	
	protected ObjRef archTypesRef;
	protected XArchFlatInterface xarch;
	protected ThingIDMap thingIDMap;
	
	public ClearSubarchitectureContextMenuPlugin(BNAComponent c, ObjRef archTypesRef,
	ThingIDMap thingIDMap, XArchFlatInterface xarch){
		super(c);
		this.archTypesRef = archTypesRef;
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
		
		if((t instanceof ComponentTypeThing) || (t instanceof ConnectorTypeThing)){
			ClearSubarchitectureMenuItemSet nsmis = new ClearSubarchitectureMenuItemSet(t);
			JMenuItem[] miArray = nsmis.getMenuItemSet();
			for(int i = 0; i < miArray.length; i++){
				currentContextMenu.add(miArray[i]);
			}
		}
		
		return currentContextMenu;
	}
	
	class ClearSubarchitectureMenuItemSet implements ActionListener{
		protected JMenuItem miClearSubarchitecture;
		protected Thing t;
				
		public ClearSubarchitectureMenuItemSet(Thing t){
			this.t = t;
			miClearSubarchitecture = new JMenuItem("Clear Sub-architecture");
			miClearSubarchitecture.addActionListener(this);
			
			ObjRef typeRef = thingIDMap.getXArchRef(t.getID());
			if(typeRef == null){
				miClearSubarchitecture.setEnabled(false);
			}
			
			ObjRef subArchitectureRef = (ObjRef)xarch.get(typeRef, "subArchitecture");
			if(subArchitectureRef == null){
				miClearSubarchitecture.setEnabled(false);
			}
		}
		
		public JMenuItem[] getMenuItemSet(){
			return new JMenuItem[]{miClearSubarchitecture};
		}
		
		public void actionPerformed(ActionEvent evt){
			if(evt.getSource() == miClearSubarchitecture){
				int result = JOptionPane.showConfirmDialog(c, 
					"Are you sure?", "Confirm Clear Sub-architecture", JOptionPane.YES_NO_OPTION);
				if(result != JOptionPane.YES_OPTION){
					return;
				}
				ObjRef typeRef = thingIDMap.getXArchRef(t.getID());
				if(typeRef == null){
					JOptionPane.showMessageDialog(c, "Thing not mapped to xArch element.", "Can't add interface.", JOptionPane.ERROR_MESSAGE);
					return;
				}
				xarch.clear(typeRef, "subArchitecture");
			}
		}
	}
}

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

public class NewInterfaceContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{
	
	protected ObjRef archStructureRef;
	protected XArchFlatInterface xarch;
	protected ThingIDMap thingIDMap;
	
	public NewInterfaceContextMenuPlugin(BNAComponent c, ObjRef archStructureRef,
	ThingIDMap thingIDMap, XArchFlatInterface xarch){
		super(c);
		this.archStructureRef = archStructureRef;
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
		
		if((t instanceof ComponentThing) || (t instanceof ConnectorThing)){
			NewInterfaceMenuItemSet nimis = new NewInterfaceMenuItemSet(t);
			JMenuItem[] miArray = nimis.getMenuItemSet();
			for(int i = 0; i < miArray.length; i++){
				currentContextMenu.add(miArray[i]);
			}
		}
		
		return currentContextMenu;
	}
	
	class NewInterfaceMenuItemSet implements ActionListener{
		protected JMenuItem miNewInterface;
		protected Thing t;
				
		public NewInterfaceMenuItemSet(Thing t){
			this.t = t;
			miNewInterface = new JMenuItem("New Interface");
			miNewInterface.addActionListener(this);
		}
		
		public JMenuItem[] getMenuItemSet(){
			return new JMenuItem[]{miNewInterface};
		}
		
		public void actionPerformed(ActionEvent evt){
			if(evt.getSource() == miNewInterface){
				ObjRef xArchRef = xarch.getXArch(archStructureRef);
				ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
				
				ObjRef brickRef = thingIDMap.getXArchRef(t.getID());
				if(brickRef == null){
					JOptionPane.showMessageDialog(c, "Thing not mapped to xArch element.", "Can't add interface.", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				ObjRef newInterfaceRef = xarch.create(typesContextRef, "interface");
				String uid = UIDGenerator.generateUID("interface");
				xarch.set(newInterfaceRef, "id", uid);
				ObjRef newDescriptionRef = xarch.create(typesContextRef, "description");
				xarch.set(newDescriptionRef, "Value", "(New Interface)");
				xarch.set(newInterfaceRef, "Description", newDescriptionRef);
				xarch.add(brickRef, "interface", newInterfaceRef);
			}
		}
	}
}

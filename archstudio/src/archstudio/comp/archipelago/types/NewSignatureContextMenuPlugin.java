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

public class NewSignatureContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{
	
	protected ObjRef archTypesRef;
	protected XArchFlatInterface xarch;
	protected ThingIDMap thingIDMap;
	
	public NewSignatureContextMenuPlugin(BNAComponent c, ObjRef archTypesRef,
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
			NewSignatureMenuItemSet nsmis = new NewSignatureMenuItemSet(t);
			JMenuItem[] miArray = nsmis.getMenuItemSet();
			if(miArray.length > 0){
				currentContextMenu.addSeparator();
			}
			for(int i = 0; i < miArray.length; i++){
				currentContextMenu.add(miArray[i]);
			}
		}
		
		return currentContextMenu;
	}
	
	class NewSignatureMenuItemSet implements ActionListener{
		protected JMenuItem miNewInterface;
		protected Thing t;
				
		public NewSignatureMenuItemSet(Thing t){
			this.t = t;
			miNewInterface = new JMenuItem("New Signature");
			miNewInterface.addActionListener(this);
		}
		
		public JMenuItem[] getMenuItemSet(){
			return new JMenuItem[]{miNewInterface};
		}
		
		public void actionPerformed(ActionEvent evt){
			if(evt.getSource() == miNewInterface){
				ObjRef xArchRef = xarch.getXArch(archTypesRef);
				ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
				
				ObjRef typeRef = thingIDMap.getXArchRef(t.getID());
				if(typeRef == null){
					JOptionPane.showMessageDialog(c, "Thing not mapped to xArch element.", "Can't add signature.", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				ObjRef newSignatureRef = xarch.create(typesContextRef, "signature");
				String uid = UIDGenerator.generateUID("signature");
				xarch.set(newSignatureRef, "id", uid);
				ObjRef newDescriptionRef = xarch.create(typesContextRef, "description");
				xarch.set(newDescriptionRef, "Value", "(New Signature)");
				xarch.set(newSignatureRef, "Description", newDescriptionRef);
				xarch.add(typeRef, "signature", newSignatureRef);
			}
		}
	}
}

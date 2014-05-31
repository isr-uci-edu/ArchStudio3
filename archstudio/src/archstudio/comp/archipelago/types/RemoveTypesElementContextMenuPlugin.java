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

public class RemoveTypesElementContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{
	
	protected XArchFlatInterface xarch;
	protected ThingIDMap thingIDMap;
	
	public RemoveTypesElementContextMenuPlugin(BNAComponent c, ThingIDMap thingIDMap, XArchFlatInterface xarch){
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
		(t instanceof SignatureInterfaceMappingThing) ||
		(t instanceof SignatureThing)
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
			if(thingToRemove instanceof SignatureThing){
				String thingToRemoveId = thingToRemove.getID();
				String thingToRemoveXArchID = ((SignatureThing)thingToRemove).getXArchID();
				ObjRef thingToRemoveRef = thingIDMap.getXArchRef(thingToRemoveId);
				
				ObjRef typeRef = xarch.getParent(thingToRemoveRef);
				
				ObjRef subArchitectureRef = (ObjRef)xarch.get(typeRef, "subArchitecture");
				if(subArchitectureRef != null){
					ObjRef[] simRefs = xarch.getAll(subArchitectureRef, "signatureInterfaceMapping");
					for(int i = 0; i < simRefs.length; i++){
						ObjRef simRef = simRefs[i];
						ObjRef sigLinkRef = (ObjRef)xarch.get(simRef, "outerSignature");
						if(sigLinkRef != null){
							String href = edu.uci.ics.xadlutils.XadlUtils.getHref(xarch, sigLinkRef);
							System.out.println("href " + href);
							if(href != null){
								System.out.println("ttrxi" + thingToRemoveXArchID);
								if(href.equals("#" + thingToRemoveXArchID)){
									System.out.println("equals w000");
									xarch.remove(subArchitectureRef, "signatureInterfaceMapping", simRef);
								}
							}
						}
					}
				}
				
				xarch.remove(typeRef, "signature", thingToRemoveRef);
			}
			else if(thingToRemove instanceof SignatureInterfaceMappingThing){
				String thingToRemoveId = thingToRemove.getID();
				ObjRef thingToRemoveRef = thingIDMap.getXArchRef(thingToRemoveId);
				
				ObjRef archStructureRef = xarch.getParent(thingToRemoveRef);
				
				xarch.remove(archStructureRef, "signatureInterfaceMapping", thingToRemoveRef);
			}
		}
	}
}

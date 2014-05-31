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

public class NewLinkContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{
	
	protected ObjRef archStructureRef;
	protected XArchFlatInterface xarch;
	protected ThingIDMap thingIDMap;
	
	public NewLinkContextMenuPlugin(BNAComponent c, ObjRef archStructureRef,
	ThingIDMap thingIDMap, XArchFlatInterface xarch){
		super(c);
		this.archStructureRef = archStructureRef;
		this.thingIDMap = thingIDMap;
		this.xarch = xarch;
	}

	public JPopupMenu addToContextMenu(JPopupMenu currentContextMenu, Thing[] selectedThingSet, Thing thingUnderCursor){
		Thing t = null;
		if(thingUnderCursor != null){
			return currentContextMenu;
		}
		
		NewLinkMenuItemSet nlmis = new NewLinkMenuItemSet();
		JMenuItem[] miArray = nlmis.getMenuItemSet();
		for(int i = 0; i < miArray.length; i++){
			currentContextMenu.add(miArray[i]);
		}
		
		return currentContextMenu;
	}
	
	class NewLinkMenuItemSet implements ActionListener{
		protected JMenuItem miNewLink;
				
		public NewLinkMenuItemSet(){
			miNewLink= new JMenuItem("New Link");
			miNewLink.addActionListener(this);
		}
		
		public JMenuItem[] getMenuItemSet(){
			return new JMenuItem[]{miNewLink};
		}
		
		public void actionPerformed(ActionEvent evt){
			if(evt.getSource() == miNewLink){
				ObjRef xArchRef = xarch.getXArch(archStructureRef);
				ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
				ObjRef newLinkRef = xarch.create(typesContextRef, "link");
				String uid = UIDGenerator.generateUID("link");
				xarch.set(newLinkRef, "id", uid);
				ObjRef newDescriptionRef = xarch.create(typesContextRef, "description");
				xarch.set(newDescriptionRef, "Value", "(New Link)");
				xarch.set(newLinkRef, "Description", newDescriptionRef);
				
				ObjRef newPoint1Ref = xarch.create(typesContextRef, "point");
				ObjRef newAnchor1Ref = xarch.create(typesContextRef, "XMLLink");
				xarch.set(newPoint1Ref, "anchorOnInterface", newAnchor1Ref);
				
				ObjRef newPoint2Ref = xarch.create(typesContextRef, "point");
				ObjRef newAnchor2Ref = xarch.create(typesContextRef, "XMLLink");
				xarch.set(newPoint2Ref, "anchorOnInterface", newAnchor2Ref);
				
				xarch.add(newLinkRef, "point", newPoint1Ref);
				xarch.add(newLinkRef, "point", newPoint2Ref);
				
				xarch.add(archStructureRef, "link", newLinkRef);
			}
		}
	}
}

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

public class NewBrickContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{
	
	protected ObjRef archStructureRef;
	protected XArchFlatInterface xarch;
	protected ThingIDMap thingIDMap;
	
	public NewBrickContextMenuPlugin(BNAComponent c, ObjRef archStructureRef,
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
		//Only allow 'new component', 'new connector' on clicking NoThing
		
		NewBrickMenuItemSet nbmis = new NewBrickMenuItemSet();
		JMenuItem[] miArray = nbmis.getMenuItemSet();
		for(int i = 0; i < miArray.length; i++){
			currentContextMenu.add(miArray[i]);
		}
		
		return currentContextMenu;
	}
	
	class NewBrickMenuItemSet implements ActionListener{
		protected JMenuItem miNewComponent;
		protected JMenuItem miNewConnector;
				
		public NewBrickMenuItemSet(){
			miNewComponent = new JMenuItem("New Component");
			miNewComponent.addActionListener(this);
			miNewConnector = new JMenuItem("New Connector");
			miNewConnector.addActionListener(this);
		}
		
		public JMenuItem[] getMenuItemSet(){
			return new JMenuItem[]{miNewComponent, miNewConnector};
		}
		
		public void actionPerformed(ActionEvent evt){
			if(evt.getSource() == miNewComponent){
				ObjRef xArchRef = xarch.getXArch(archStructureRef);
				ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
				ObjRef newComponentRef = xarch.create(typesContextRef, "component");
				String uid = UIDGenerator.generateUID("component");
				xarch.set(newComponentRef, "id", uid);
				ObjRef newDescriptionRef = xarch.create(typesContextRef, "description");
				xarch.set(newDescriptionRef, "Value", "(New Component)");
				xarch.set(newComponentRef, "Description", newDescriptionRef);
				xarch.add(archStructureRef, "component", newComponentRef);
			}
			else if(evt.getSource() == miNewConnector){
				ObjRef xArchRef = xarch.getXArch(archStructureRef);
				ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
				ObjRef newConnectorRef = xarch.create(typesContextRef, "connector");
				String uid = UIDGenerator.generateUID("connector");
				xarch.set(newConnectorRef, "id", uid);
				ObjRef newDescriptionRef = xarch.create(typesContextRef, "description");
				xarch.set(newDescriptionRef, "Value", "(New Connector)");
				xarch.set(newConnectorRef, "Description", newDescriptionRef);
				xarch.add(archStructureRef, "connector", newConnectorRef);
			}
		}
	}
}

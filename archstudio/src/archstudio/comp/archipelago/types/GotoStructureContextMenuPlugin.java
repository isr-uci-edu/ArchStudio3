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

public class GotoStructureContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{
	
	protected XArchFlatInterface xarch;
	protected ThingIDMap thingIDMap;
	protected ArchStructureTreePlugin archStructureTreePlugin;
	
	public GotoStructureContextMenuPlugin(BNAComponent c, ThingIDMap thingIDMap, 
	XArchFlatInterface xarch, ArchStructureTreePlugin astp){
		super(c);
		this.thingIDMap = thingIDMap;
		this.xarch = xarch;
		this.archStructureTreePlugin = astp;
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
		
		if(t instanceof BrickTypeThing){
			GotoStructureMenuItemSet gsmis = new GotoStructureMenuItemSet(t);
			JMenuItem[] miArray = gsmis.getMenuItemSet();

			boolean hasSubArchitecture = false;
			ObjRef thingWithStructureRef = thingIDMap.getXArchRef(t.getID());
			
			if(thingWithStructureRef != null){
				ObjRef subArchitectureRef = (ObjRef)xarch.get(thingWithStructureRef, "subArchitecture");
				if(subArchitectureRef != null){
					hasSubArchitecture = true;
				}
			}

			if(miArray.length > 0){
				currentContextMenu.addSeparator();
			}
			for(int i = 0; i < miArray.length; i++){
				miArray[i].setEnabled(hasSubArchitecture);
				currentContextMenu.add(miArray[i]);
			}
		}
		
		return currentContextMenu;
	}
	
	protected void centerInComponent(IResizableLocalBoxBounded lbbt){
		Rectangle lbbtBoundingBox = lbbt.getLocalBoundingBox();
		
		int lbbtWidth = lbbtBoundingBox.width;
		int lbbtHeight = lbbtBoundingBox.height;
		
		Dimension bnaSize = getBNAComponent().getSize();
		
		int bnacx = bnaSize.width / 2;
		int bnacy = bnaSize.height / 2;
		
		int ulx = bnacx - (lbbtWidth / 2);
		int uly = bnacy - (lbbtHeight / 2);
		
		lbbtBoundingBox.setLocation(ulx, uly);
		lbbt.setLocalBoundingBox(lbbtBoundingBox);
	}
	
	class GotoStructureMenuItemSet implements ActionListener{
		protected Thing thingWithStructure;
		protected JMenuItem miHighlightStructure;
		protected JMenuItem miGotoStructure;
				
		public GotoStructureMenuItemSet(Thing thingWithStructure){
			this.thingWithStructure = thingWithStructure;
			miHighlightStructure = new JMenuItem("Highlight Structure");
			miHighlightStructure.addActionListener(this);
			miGotoStructure = new JMenuItem("Go to Structure");
			miGotoStructure.addActionListener(this);
		}
		
		public JMenuItem[] getMenuItemSet(){
			return new JMenuItem[]{miHighlightStructure, miGotoStructure};
		}
		
		public void actionPerformed(ActionEvent evt){
			ObjRef thingWithStructureRef = thingIDMap.getXArchRef(thingWithStructure.getID());
			
			if(thingWithStructureRef != null){
				ObjRef subArchitectureRef = (ObjRef)xarch.get(thingWithStructureRef, "subArchitecture");
				if(subArchitectureRef != null){
					ObjRef archStructureLinkRef = (ObjRef)xarch.get(subArchitectureRef, "archStructure");
					if(archStructureLinkRef != null){
						String href = (String)xarch.get(archStructureLinkRef, "href");
						if(href != null){
							ObjRef xArchRef = xarch.getXArch(thingWithStructureRef);
							ObjRef structureRef = xarch.resolveHref(xArchRef, href);
							if(structureRef != null){
								ArchStructureTreeNode astn = archStructureTreePlugin.getArchStructureTreeNode(structureRef);
								if(astn != null){
									archStructureTreePlugin.showAndSelect(astn);
									if(evt.getSource() == miGotoStructure){
										archStructureTreePlugin.doOpen(astn);
									}
									return;
								}
							}
						}
					}
				}
			}
			JOptionPane.showMessageDialog(c, "Can't resolve subarchitecture link.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}

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

public class GotoTypeContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{
	
	protected XArchFlatInterface xarch;
	protected ThingIDMap thingIDMap;
	protected ArchTypesTreePlugin archTypesTreePlugin;
	
	public GotoTypeContextMenuPlugin(BNAComponent c, ThingIDMap thingIDMap, 
	XArchFlatInterface xarch, ArchTypesTreePlugin atp){
		super(c);
		this.thingIDMap = thingIDMap;
		this.xarch = xarch;
		this.archTypesTreePlugin = atp;
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
			GotoTypeMenuItemSet edmis = new GotoTypeMenuItemSet(t);
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
	
	class GotoTypeMenuItemSet implements ActionListener{
		protected Thing thingWithType;
		protected JMenuItem miHighlightType;
		protected JMenuItem miGotoType;
				
		public GotoTypeMenuItemSet(Thing thingWithType){
			this.thingWithType = thingWithType;
			miHighlightType = new JMenuItem("Highlight Type");
			miHighlightType.addActionListener(this);
			miGotoType = new JMenuItem("Go to Type");
			miGotoType.addActionListener(this);
		}
		
		public JMenuItem[] getMenuItemSet(){
			return new JMenuItem[]{miHighlightType, miGotoType};
		}
		
		public void actionPerformed(ActionEvent evt){
			ObjRef thingWithTypeRef = thingIDMap.getXArchRef(thingWithType.getID());
			
			if(thingWithTypeRef != null){
				ObjRef typeLinkRef = (ObjRef)xarch.get(thingWithTypeRef, "type");
				if(typeLinkRef != null){
					String href = (String)xarch.get(typeLinkRef, "href");
					if(href != null){
						ObjRef xArchRef = xarch.getXArch(thingWithTypeRef);
						ObjRef typeRef = xarch.resolveHref(xArchRef, href);
						if(typeRef != null){
							ArchTypesTreeNode attn = archTypesTreePlugin.getArchTypesTreeNode(typeRef);
							if(attn != null){
								archTypesTreePlugin.showAndSelect(attn);
								if(evt.getSource() == miGotoType){
									archTypesTreePlugin.doOpen(attn);
								}
								return;
							}
						}
					}
				}
			}
			JOptionPane.showMessageDialog(c, "Can't resolve type link.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}

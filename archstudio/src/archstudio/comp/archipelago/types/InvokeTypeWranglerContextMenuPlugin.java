package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;
import archstudio.comp.typewrangler.InvokeTypeWranglerMessage;
import archstudio.invoke.InvokeMessage;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import c2.fw.MessageProvider;
import c2.util.UIDGenerator;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.contextmenu.*;
import edu.uci.ics.bna.swingthing.*;
import edu.uci.ics.widgets.windowheader.*;

import edu.uci.ics.xarchutils.*;

public class InvokeTypeWranglerContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{
	
	protected ObjRef archTypesRef;
	protected XArchFlatInterface xarch;
	protected ThingIDMap thingIDMap;
	protected ArchStructureTreePlugin astp;
	
	public InvokeTypeWranglerContextMenuPlugin(BNAComponent c, ObjRef archTypesRef,
	ThingIDMap thingIDMap, XArchFlatInterface xarch, ArchStructureTreePlugin astp){
		super(c);
		this.archTypesRef = archTypesRef;
		this.thingIDMap = thingIDMap;
		this.xarch = xarch;
		this.astp = astp;
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
		
		if(t instanceof BrickThing){
			InvokeTypeWranglerMenuItemSet nsmis = new InvokeTypeWranglerMenuItemSet(t);
			JMenuItem[] miArray = nsmis.getMenuItemSet();
			for(int i = 0; i < miArray.length; i++){
				currentContextMenu.add(miArray[i]);
			}
		}
		
		return currentContextMenu;
	}
	
	class InvokeTypeWranglerMenuItemSet implements ActionListener{
		protected JMenuItem miInvokeTypeWrangler;
		protected Thing t;
				
		public InvokeTypeWranglerMenuItemSet(Thing t){
			this.t = t;
			miInvokeTypeWrangler = new JMenuItem("Invoke Type Wrangler");
			miInvokeTypeWrangler.addActionListener(this);
		}
		
		public JMenuItem[] getMenuItemSet(){
			return new JMenuItem[]{miInvokeTypeWrangler};
		}
		
		public void actionPerformed(ActionEvent evt){
			if(evt.getSource() == miInvokeTypeWrangler){
				
				ObjRef typeRef = thingIDMap.getXArchRef(t.getID());
				if(typeRef == null){
					JOptionPane.showMessageDialog(c, "Thing not mapped to xArch element.", "Can't add interface.", JOptionPane.ERROR_MESSAGE);
					return;
				}
				ObjRef xArchRef = xarch.getXArch(typeRef);
				String url = xarch.getXArchURI(xArchRef);
				InvokeTypeWranglerMessage im = new InvokeTypeWranglerMessage(url, typeRef);
				astp.fireMessageSent(im);
			}
		}
	}
}

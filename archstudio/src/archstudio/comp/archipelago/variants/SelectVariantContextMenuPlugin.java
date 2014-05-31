package archstudio.comp.archipelago.variants;

import archstudio.comp.archipelago.*;
import archstudio.comp.archipelago.types.*;
import archstudio.comp.archipelago.variants.VariantsContextMenuPlugin.VariantMenuItem;
import archstudio.comp.archipelago.variants.VariantsContextMenuPlugin.VariantMenuItemSet;
import archstudio.comp.archipelago.variants.VariantsContextMenuPlugin.VariantMenuItemSet.EditVariantPanel;
import archstudio.comp.booleannotation.IBooleanNotation;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.contextmenu.*;
import edu.uci.ics.bna.swingthing.*;

import edu.uci.ics.xadlutils.*;
import edu.uci.ics.xarchutils.*;

public class SelectVariantContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{
	protected ArchVariantsTreePlugin archVariantsTreePlugin;
	protected XArchFlatInterface xarch;
	protected ThingIDMap thingIDMap;

	public SelectVariantContextMenuPlugin(BNAComponent c, ArchVariantsTreePlugin avtp,
	ThingIDMap thingIDMap, XArchFlatInterface xarch){
		super(c);
		this.archVariantsTreePlugin = avtp;
		this.thingIDMap = thingIDMap;
		this.xarch = xarch;
	}

	public JPopupMenu addToContextMenu(JPopupMenu currentContextMenu, Thing[] selectedThingSet, Thing thingUnderCursor){
		if(selectedThingSet.length == 1){
			Thing t = selectedThingSet[0];
			if(t instanceof BrickThing){
				SelectVariantMenuItemSet nsmis = new SelectVariantMenuItemSet(t);
				JMenuItem[] miArray = nsmis.getMenuItemSet();
				if(miArray.length > 0){
					currentContextMenu.add(new JSeparator());
				}
				for(int i = 0; i < miArray.length; i++){
					currentContextMenu.add(miArray[i]);
				}
			}
		}
		return currentContextMenu;
	}

	class SelectVariantMenuItemSet implements ActionListener{
		protected JMenu mSelectVariant;
		
		protected Thing t;
				
		public SelectVariantMenuItemSet(Thing t){
			this.t = t;
			mSelectVariant = new JMenu("Select Variant Now");
			
			try{
				if(t instanceof BrickThing){
					ObjRef brickRef = thingIDMap.getXArchRef(t.getID());
					if(brickRef != null){
						ObjRef brickTypeRef = XadlUtils.resolveXLink(xarch, brickRef, "type");
						if(brickTypeRef != null){
							ObjRef[] variantRefs = xarch.getAll(brickTypeRef, "variant");
							if((variantRefs != null) && (variantRefs.length > 0)){
								for(int i = 0; i < variantRefs.length; i++){
									ObjRef variantTypeLinkRef = (ObjRef)xarch.get(variantRefs[i], "variantType");
									if(variantTypeLinkRef != null){
										String variantTypeLinkHref = (String)xarch.get(variantTypeLinkRef, "href");
										if(variantTypeLinkHref != null){
											ObjRef variantTypeRef = XadlUtils.resolveXLink(xarch, variantTypeLinkRef);
											if(variantTypeRef != null){
												String description = XadlUtils.getDescription(xarch, variantTypeRef);
												if(description == null) description = "[Unknown Type]";
												VariantMenuItem vmi = new VariantMenuItem(description, brickRef, variantTypeLinkHref);
												vmi.addActionListener(this);
												mSelectVariant.add(vmi);
											}
										}
									}
								}
							}
						}
					}
				}
			}
			catch(Exception e){
				return;
			}
		}
		
		class VariantMenuItem extends JMenuItem{
			protected ObjRef brickRef;
			protected String variantTypeHref;
			
			public VariantMenuItem(String name, ObjRef brickRef, String variantTypeHref){
				super(name);
				this.brickRef = brickRef;
				this.variantTypeHref = variantTypeHref;
			}
			
			public String getVariantTypeHref(){
				return variantTypeHref;
			}
			
			public ObjRef getBrickRef(){
				return brickRef;
			}
		}
		
		public JMenuItem[] getMenuItemSet(){
			if(mSelectVariant.getItemCount() > 0){
				return new JMenuItem[]{mSelectVariant};
			}
			else{
				return new JMenuItem[0];
			}
		}
		
		public void actionPerformed(ActionEvent evt){
			if(evt.getSource() instanceof VariantMenuItem){
				VariantMenuItem vmi = (VariantMenuItem)evt.getSource();
				ObjRef brickRef = vmi.getBrickRef();
				String newTypeHref = vmi.getVariantTypeHref();
				XadlUtils.setXLinkByHref(xarch, brickRef, "type", newTypeHref);
			}
		}
	}
}

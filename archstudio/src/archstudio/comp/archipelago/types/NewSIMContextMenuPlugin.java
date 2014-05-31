package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.contextmenu.*;
import edu.uci.ics.bna.swingthing.*;
import edu.uci.ics.bna.thumbnail.Thumbnail;
import edu.uci.ics.widgets.IconableTreeCellRenderer;
import edu.uci.ics.widgets.windowheader.*;

import edu.uci.ics.xadlutils.StructureSelectorDialog;
import edu.uci.ics.xadlutils.Resources;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.*;

public class NewSIMContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{
	
	protected XArchFlatInterface xarch;
	protected ThingIDMap thingIDMap;
	
	public NewSIMContextMenuPlugin(BNAComponent c, ThingIDMap thingIDMap, XArchFlatInterface xarch){
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
		else{
			return currentContextMenu;
		}
		
		if(t instanceof SignatureThing){
			NewSIMMenuItemSet edmis = new NewSIMMenuItemSet((SignatureThing)t);
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
	
	class NewSIMMenuItemSet implements ActionListener{
		protected SignatureThing signatureThing;
		protected JMenuItem miNewSIM;
		
		public NewSIMMenuItemSet(SignatureThing signatureThing){
			this.signatureThing = signatureThing;
			miNewSIM = new JMenuItem("New Signature-Interface Mapping...");
			miNewSIM.addActionListener(this);
			miNewSIM.setEnabled(false);
			
			ObjRef sigRef = thingIDMap.getXArchRef(signatureThing.getID());
			if(sigRef != null){
				ObjRef typeRef = xarch.getParent(sigRef);
				if(typeRef != null){
					ObjRef subArchitectureRef = (ObjRef)xarch.get(typeRef, "subArchitecture");
					if(subArchitectureRef != null){
						miNewSIM.setEnabled(true);
					}
				}
			}
		}
		
		public JMenuItem[] getMenuItemSet(){
			return new JMenuItem[]{miNewSIM};
		}
		
		public void actionPerformed(ActionEvent evt){
			SwingPanelThing newSIMPanel = new SwingPanelThing();
			NewSIMPanel edp = new NewSIMPanel(newSIMPanel, signatureThing);
			
			getBNAComponent().getModel().addThing(newSIMPanel);
			newSIMPanel.setLocalBoundingBox(new Rectangle(0, 0, 300, 340));
			centerInComponent(newSIMPanel);
		}

		class NewSIMPanel extends JPanel implements WindowHeaderPanelListener, ActionListener{
			protected SwingPanelThing swingPanelThing;
			protected SignatureThing signatureThing;
			
			protected StructureSelectorDialog.StructureSelectorComponent brickSelectorComponent;
		
			protected JTree tTree;
			protected JButton bOK;
			protected JButton bCancel;
		
			public NewSIMPanel(SwingPanelThing swingPanelThing, SignatureThing signatureThing){
				super();
				this.swingPanelThing = swingPanelThing;
				this.signatureThing = signatureThing;
				init();
			}
			
			private	StructureSelectorDialog.StructureSelectorComponent getBrickSelectorComponent(){
				String bttID = signatureThing.getTargetThingID();
				Thing t = getBNAComponent().getModel().getThing(bttID);
				if(t == null) return null;
				
				if(!(t instanceof BrickTypeThing)){
					return null;
				}
				
				BrickTypeThing btt = (BrickTypeThing)t;
				//ObjRef signatureRef = thingIDMap.getXArchRef(signatureThing.getID());
				ObjRef brickTypeRef = thingIDMap.getXArchRef(bttID);
				if(brickTypeRef != null){
					ObjRef subArchitectureRef = (ObjRef)xarch.get(brickTypeRef, "subArchitecture");
					if(subArchitectureRef != null){
						ObjRef subarchitectureStructureRef = XadlUtils.resolveXLink(xarch, subArchitectureRef, "archStructure");
						if(subarchitectureStructureRef != null){
							ObjRef xArchRef = xarch.getXArch(subarchitectureStructureRef);
							StructureSelectorDialog.StructureSelectorComponent bsc = new StructureSelectorDialog.StructureSelectorComponent(xarch, xArchRef,
								new ObjRef[]{subarchitectureStructureRef}, 
								StructureSelectorDialog.SHOW_COMPONENTS |
								StructureSelectorDialog.SHOW_CONNECTORS |
								StructureSelectorDialog.SHOW_INTERFACES |	
								StructureSelectorDialog.SELECTABLE_INTERFACES);
							return bsc;
						}
					}
				}
				return null;
			}
			
			public void init(){			
				this.setLayout(new BorderLayout());
			
				WindowHeaderPanel whp = new WindowHeaderPanel("Choose Inner Interface");
				whp.addWindowHeaderPanelListener(this);
			
				this.add("North", whp);

				brickSelectorComponent = getBrickSelectorComponent();
				brickSelectorComponent.addActionListener(this);
				this.add("Center", brickSelectorComponent);
				
				swingPanelThing.setPanel(this);
				swingPanelThing.setIndicatorThingId(signatureThing.getID());
			}
		
			public void closeButtonPressed(WindowHeaderPanel src){
				remove();
			}

			protected void createNewSIM(ObjRef innerInterfaceRef){
				String bttID = signatureThing.getTargetThingID();
				Thing t = getBNAComponent().getModel().getThing(bttID);
				if(t == null){
					//This shouldn't happen
					JOptionPane.showMessageDialog(getBNAComponent(), 
						"Brick does not exist for signature", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if(!(t instanceof BrickTypeThing)){
					//This shouldn't happen
					JOptionPane.showMessageDialog(getBNAComponent(), 
						"Signature's parent not a brick.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				BrickTypeThing btt = (BrickTypeThing)t;
				ObjRef signatureRef = thingIDMap.getXArchRef(signatureThing.getID());
				ObjRef typeRef = thingIDMap.getXArchRef(btt.getID());
				
				ObjRef subArchitectureRef = (ObjRef)xarch.get(typeRef, "subArchitecture");
				if(subArchitectureRef == null){
					//This shouldn't happen
					JOptionPane.showMessageDialog(getBNAComponent(), 
						"Brick has no sub-architecture.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				ObjRef xArchRef = xarch.getXArch(signatureRef);
				ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
				
				String outerSignatureXArchID = signatureThing.getXArchID();
				
				String innerInterfaceXArchID = XadlUtils.getID(xarch, innerInterfaceRef);
				
				ObjRef simRef = xarch.create(typesContextRef, "signatureInterfaceMapping");
				ObjRef sigLinkRef = xarch.create(typesContextRef, "XMLLink");
				xarch.set(sigLinkRef, "type", "simple");
				xarch.set(sigLinkRef, "href", "#" + outerSignatureXArchID);
				
				ObjRef intLinkRef = xarch.create(typesContextRef, "XMLLink");
				xarch.set(intLinkRef, "type", "simple");
				xarch.set(intLinkRef, "href", "#" + innerInterfaceXArchID);
				
				xarch.set(simRef, "id", c2.util.UIDGenerator.generateUID("sigIntMap"));
				ObjRef descriptionRef = xarch.create(typesContextRef, "Description");
				xarch.set(descriptionRef, "Value", "(New Signature-Interface Mapping)");
				xarch.set(simRef, "Description", descriptionRef);
				
				xarch.set(simRef, "outerSignature", sigLinkRef);
				xarch.set(simRef, "innerInterface", intLinkRef);
				xarch.add(subArchitectureRef, "signatureInterfaceMapping", simRef);
			}
			
			public void actionPerformed(ActionEvent evt){
				if(evt.getSource() == brickSelectorComponent){
					ObjRef result = brickSelectorComponent.getResult();
					remove();
					if(result != null){
						createNewSIM(result);
					}
				}
			}
			
			public void remove(){
				getBNAComponent().getModel().removeThing(swingPanelThing);
			}
		}
	}
}


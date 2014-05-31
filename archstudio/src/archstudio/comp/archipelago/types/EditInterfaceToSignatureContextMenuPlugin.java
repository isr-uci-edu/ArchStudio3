package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.contextmenu.*;
import edu.uci.ics.bna.swingthing.*;
import edu.uci.ics.widgets.windowheader.*;

import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.*;

public class EditInterfaceToSignatureContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{
	
	protected XArchFlatInterface xarch;
	protected ThingIDMap thingIDMap;
	
	public EditInterfaceToSignatureContextMenuPlugin(BNAComponent c, ThingIDMap thingIDMap, XArchFlatInterface xarch){
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
		
		if(t instanceof InterfaceThing){
			EditInterfaceToSignatureMenuItemSet edmis = new EditInterfaceToSignatureMenuItemSet((InterfaceThing)t);
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
	
	private SignatureItem[] getAllSignatureItems(InterfaceThing thingToEdit){
		ArrayList signatureItems = new ArrayList();
		ObjRef interfaceRef = thingIDMap.getXArchRef(thingToEdit.getID());
		if(interfaceRef != null){
			ObjRef xArchRef = xarch.getXArch(interfaceRef);
			//System.out.println("got interfaceRef");
			ObjRef brickRef = xarch.getParent(interfaceRef);
			if(brickRef != null){
				ObjRef typeLinkRef = (ObjRef)xarch.get(brickRef, "type");
				if(typeLinkRef != null){
					//System.out.println("got typeLinkRef");
					String typeLinkHref = XadlUtils.getHref(xarch, typeLinkRef);
					if(typeLinkHref != null){
						//System.out.println("got typeLinkHref");
						ObjRef typeRef = xarch.resolveHref(xArchRef, typeLinkHref);
						if(typeRef != null){
							//System.out.println("got typeREf");
							ObjRef[] signatureRefs = xarch.getAll(typeRef, "signature");
							for(int i = 0; i < signatureRefs.length; i++){
								//System.out.println("got sig " + i);
								String id = XadlUtils.getID(xarch, signatureRefs[i]);
								if(id != null){
									//System.out.println("got id");
									String description = XadlUtils.getDescription(xarch, signatureRefs[i]);
									if(description == null){
										description = "(Signature Lacking Description)";
									}
									SignatureItem si = new SignatureItem();
									si.signatureID = id;
									si.signatureDescription = description;
									signatureItems.add(si);
								}
							}
						}
					}
				}
			}
		}
		return (SignatureItem[])signatureItems.toArray(new SignatureItem[0]);
	}
			
	private SignatureItem getCurrentSignatureItem(InterfaceThing thingToEdit, SignatureItem[] signatureItems){
		ObjRef interfaceRef = thingIDMap.getXArchRef(thingToEdit.getID());
		if(interfaceRef != null){
			ObjRef xArchRef = xarch.getXArch(interfaceRef);
			ObjRef signatureLinkRef = (ObjRef)xarch.get(interfaceRef, "signature");
			if(signatureLinkRef != null){
				String signatureLinkHref = XadlUtils.getHref(xarch, signatureLinkRef);
				if(signatureLinkHref != null){
					ObjRef sigRef = xarch.resolveHref(xArchRef, signatureLinkHref);
					if(sigRef != null){
						String id = XadlUtils.getID(xarch, sigRef);
						if(id != null){
							for(int i = 0; i < signatureItems.length; i++){
								if(signatureItems[i].signatureID.equals(id)){
									return signatureItems[i];
								}
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	static class SignatureItem{
		public String signatureID;
		public String signatureDescription;
		public String toString(){
			return signatureDescription;
		}
	}
		
	class EditInterfaceToSignatureMenuItemSet implements ActionListener{
		protected InterfaceThing thingToEdit;
		protected JMenuItem miEditInterfaceToSignature;
		protected JMenuItem miClearInterfaceToSignature;
		protected SignatureItem[] signatureItems;
			
		public EditInterfaceToSignatureMenuItemSet(InterfaceThing thingToEdit){
			this.thingToEdit = thingToEdit;
			miEditInterfaceToSignature = new JMenuItem("Edit Interface-to-Signature Link");
			miEditInterfaceToSignature.addActionListener(this);
			miClearInterfaceToSignature = new JMenuItem("Clear Interface-to-Signature Link");
			miClearInterfaceToSignature.addActionListener(this);
			
			signatureItems = getAllSignatureItems(thingToEdit);
			if((signatureItems == null) || (signatureItems.length == 0)){
				miEditInterfaceToSignature.setEnabled(false);
			}
		}
		
		public JMenuItem[] getMenuItemSet(){
			return new JMenuItem[]{miEditInterfaceToSignature, miClearInterfaceToSignature};
		}
		
		public void clearInterfaceToSignatureMapping(){
			String thingToEditId = thingToEdit.getID();
			ObjRef thingToEditRef = thingIDMap.getXArchRef(thingToEditId);
			if(thingToEditRef != null){
				xarch.clear(thingToEditRef, "signature");
			}
		}
		
		public void actionPerformed(ActionEvent evt){
			if(evt.getSource() == miEditInterfaceToSignature){
				SwingPanelThing interfaceToSignatureEditPanel = new SwingPanelThing();
				EditInterfaceToSignaturePanel edp = new EditInterfaceToSignaturePanel(interfaceToSignatureEditPanel, thingToEdit);
			
				getBNAComponent().getModel().addThing(interfaceToSignatureEditPanel);
				interfaceToSignatureEditPanel.setLocalBoundingBox(new Rectangle(0, 0, 300, 90));
				centerInComponent(interfaceToSignatureEditPanel);
			}
			else if(evt.getSource() == miClearInterfaceToSignature){
				clearInterfaceToSignatureMapping();
			}
		}

		class EditInterfaceToSignaturePanel extends JPanel implements WindowHeaderPanelListener, ActionListener{
			protected Thing swingPanelThing;
			protected InterfaceThing thingToEdit;
		
			protected JComboBox cbMapping;
			protected JButton bOK;
			protected JButton bCancel;
		
			public EditInterfaceToSignaturePanel(SwingPanelThing swingPanelThing, InterfaceThing thingToEdit){
				super();
				this.swingPanelThing = swingPanelThing;
				this.thingToEdit = thingToEdit;
		
				this.setLayout(new BorderLayout());
			
				WindowHeaderPanel whp = new WindowHeaderPanel("Edit Interface-to-Signature Link");
				whp.addWindowHeaderPanelListener(this);
			
				this.add("North", whp);
			
				//Let's get all the signatures
				DefaultComboBoxModel cbm = new DefaultComboBoxModel();
				for(int i = 0; i < signatureItems.length; i++){
					cbm.addElement(signatureItems[i]);
				}
				
				cbMapping = new JComboBox(cbm);
				cbMapping.setEditable(false);
				cbMapping.addActionListener(this);
			
				bOK = new JButton("OK");
				bOK.addActionListener(this);
			
				bCancel = new JButton("Cancel");
				bCancel.addActionListener(this);
				
				JPanel centerPanel = new JPanel();
				centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				centerPanel.add(cbMapping);
				centerPanel.add(bOK);
			
				this.add("Center", centerPanel);
				swingPanelThing.setPanel(this);
				swingPanelThing.setIndicatorThingId(thingToEdit.getID());
				updateTextField();
			}
			
			protected void updateTextField(){
				SignatureItem currentSignatureItem = getCurrentSignatureItem(thingToEdit, signatureItems);
				if(currentSignatureItem != null){
					cbMapping.setSelectedItem(currentSignatureItem);
				}
			}
			
			protected void setNewValue(SignatureItem newValue){
				String thingToEditId = thingToEdit.getID();
				ObjRef thingToEditRef = thingIDMap.getXArchRef(thingToEditId);
				if(thingToEditRef != null){
					boolean hadLink = true;
					ObjRef signatureLinkRef = (ObjRef)xarch.get(thingToEditRef, "signature");
					if(signatureLinkRef == null){
						hadLink = false;
						ObjRef xArchRef = xarch.getXArch(thingToEditRef);
						ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
						signatureLinkRef = xarch.create(typesContextRef, "XMLLink");
					}
					xarch.set(signatureLinkRef, "type", "simple");
					xarch.set(signatureLinkRef, "href", "#" + newValue.signatureID);
					if(!hadLink){
						xarch.set(thingToEditRef, "signature", signatureLinkRef);
					}
				}
			}

			public void closeButtonPressed(WindowHeaderPanel src){
				remove();
			}
			
			public void actionPerformed(ActionEvent evt){
				if(evt.getSource() == bOK){
					SignatureItem newSignatureItem = (SignatureItem)cbMapping.getSelectedItem();
					remove();
					setNewValue(newSignatureItem);
				}
				if(evt.getSource() == cbMapping){
					bOK.doClick();
				}
				if(evt.getSource() == bCancel){
					remove();
				}
			}
			
			public void remove(){
				getBNAComponent().getModel().removeThing(swingPanelThing);
			}
		}
	}
}


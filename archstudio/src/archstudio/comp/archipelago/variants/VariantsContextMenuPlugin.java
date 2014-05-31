package archstudio.comp.archipelago.variants;

import archstudio.comp.archipelago.*;
import archstudio.comp.archipelago.types.*;
import archstudio.comp.booleannotation.IBooleanNotation;
import archstudio.comp.booleannotation.TokenMgrError;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import c2.fw.MessageProvider;
import c2.util.UIDGenerator;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.contextmenu.*;
import edu.uci.ics.bna.swingthing.*;
import edu.uci.ics.widgets.JPanelUL;
import edu.uci.ics.widgets.TooltipComboBoxRenderer;
import edu.uci.ics.widgets.windowheader.*;

import edu.uci.ics.xadlutils.*;
import edu.uci.ics.xarchutils.*;

public class VariantsContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{
	protected ArchVariantsTreePlugin archVariantsTreePlugin;
	protected XArchFlatInterface xarch;
	protected ThingIDMap thingIDMap;
	protected IBooleanNotation bni;
	
	public VariantsContextMenuPlugin(BNAComponent c, ArchVariantsTreePlugin avtp,
	ThingIDMap thingIDMap, XArchFlatInterface xarch, IBooleanNotation bni){
		super(c);
		this.archVariantsTreePlugin = avtp;
		this.thingIDMap = thingIDMap;
		this.xarch = xarch;
		this.bni = bni;
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
			if(currentContextMenu.getSubElements().length > 0){
				currentContextMenu.addSeparator();
			}
			VariantMenuItemSet nsmis = new VariantMenuItemSet(t);
			JMenuItem[] miArray = nsmis.getMenuItemSet();
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
		
	class VariantMenuItemSet implements ActionListener{
		protected JMenuItem miPromoteToVariant;
		protected JMenuItem miAddNewVariant;
		protected JMenu mEditVariant;
		protected JMenu mRemoveVariant;
		
		protected Thing t;
				
		public VariantMenuItemSet(Thing t){
			this.t = t;
			miPromoteToVariant = new JMenuItem("Promote to Variant");
			miPromoteToVariant.addActionListener(this);

			miAddNewVariant = new JMenuItem("Add New Variant...");
			miAddNewVariant.addActionListener(this);

			mEditVariant = new JMenu("Edit Variant");
			mEditVariant.addActionListener(this);

			mRemoveVariant = new JMenu("Remove Variant");
			mRemoveVariant.addActionListener(this);

			ObjRef eltRef = thingIDMap.getXArchRef(t.getID());
			if(eltRef == null){
				return;
			}

			boolean isVariant = false;
			if(t instanceof ComponentTypeThing){
				isVariant = xarch.isInstanceOf(eltRef, "edu.uci.isr.xarch.variants.IVariantComponentType");
			}
			else if(t instanceof ConnectorTypeThing){
				isVariant = xarch.isInstanceOf(eltRef, "edu.uci.isr.xarch.variants.IVariantConnectorType");
			}
			
			if(!isVariant){
				miAddNewVariant.setEnabled(false);
				mEditVariant.setEnabled(false);
				mRemoveVariant.setEnabled(false);
			}
			else{
				ObjRef subArchitectureRef = (ObjRef)xarch.get(eltRef, "subArchitecture");
				if(subArchitectureRef != null){
					//It has a subarchitecture, so we can't add variants.
					miAddNewVariant.setEnabled(false);
					mEditVariant.setEnabled(false);
					mRemoveVariant.setEnabled(false);
				}
				else{
					miPromoteToVariant.setEnabled(false);
				
					miAddNewVariant.setEnabled(true);
					ObjRef[] variantRefs = xarch.getAll(eltRef, "variant");
					if((variantRefs == null) || (variantRefs.length == 0)){
						//Has no variants
						mEditVariant.setEnabled(false);
						mRemoveVariant.setEnabled(false);
					}
					else{
						//Has at least one variant.
						mEditVariant.setEnabled(true);
						mRemoveVariant.setEnabled(true);
					
						String[] variantTypeDescriptions = new String[variantRefs.length];
						for(int i = 0; i < variantRefs.length; i++){
							ObjRef variantTypeRef = XadlUtils.resolveXLink(xarch, variantRefs[i], "variantType");
							if(variantTypeRef != null){
								variantTypeDescriptions[i] = XadlUtils.getDescription(xarch, variantTypeRef);
								if(variantTypeDescriptions[i] == null){
									variantTypeDescriptions[i] = "(Type Lacking Description)";
								}
							}
							else{
								variantTypeDescriptions[i] = "[Unknown/Invalid Variant]";
							}
						
							VariantMenuItem vmi = new VariantMenuItem(VariantMenuItem.EDIT_VARIANT,
								variantRefs[i], variantTypeDescriptions[i]);
							vmi.addActionListener(this);
							mEditVariant.add(vmi);
						
							VariantMenuItem vmi2 = new VariantMenuItem(VariantMenuItem.REMOVE_VARIANT,
								variantRefs[i], variantTypeDescriptions[i]);
							vmi2.addActionListener(this);
							mRemoveVariant.add(vmi2);
						}
					}
				}
			}
		}
		
		public JMenuItem[] getMenuItemSet(){
			return new JMenuItem[]{miPromoteToVariant, miAddNewVariant, mEditVariant, mRemoveVariant};
		}
		
		public void actionPerformed(ActionEvent evt){
			ObjRef eltRef = thingIDMap.getXArchRef(t.getID());
			if(eltRef == null){
				JOptionPane.showMessageDialog(c, "Thing not mapped to xArch element.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if(evt.getSource() == miPromoteToVariant){
				ObjRef xArchRef = xarch.getXArch(eltRef);
				ObjRef variantsContextRef = xarch.createContext(xArchRef, "variants");

				if(t instanceof ComponentTypeThing){
					xarch.promoteTo(variantsContextRef, "variantComponentType", eltRef);
				}
				else if(t instanceof ConnectorTypeThing){
					xarch.promoteTo(variantsContextRef, "variantConnectorType", eltRef);
				}
			}
			else if(evt.getSource() == miAddNewVariant){
				ObjRef xArchRef = xarch.getXArch(eltRef);
				ObjRef variantsContextRef = xarch.createContext(xArchRef, "variants");
				ObjRef newVariantRef = xarch.create(variantsContextRef, "variant");
				xarch.add(eltRef, "variant", newVariantRef);
				
				SwingPanelThing editPanel = new SwingPanelThing();
				EditVariantPanel ep = new EditVariantPanel(editPanel, t, newVariantRef);
			
				getBNAComponent().getModel().addThing(editPanel);
				editPanel.setLocalBoundingBox(new Rectangle(0, 0, 300, 135));
				centerInComponent(editPanel);
			}
			else if(evt.getSource() instanceof VariantMenuItem){
				VariantMenuItem vmi = (VariantMenuItem)evt.getSource();
				int vmiType = vmi.getItemType();
				
				if(vmiType == VariantMenuItem.EDIT_VARIANT){
					SwingPanelThing editPanel = new SwingPanelThing();
					EditVariantPanel ep = new EditVariantPanel(editPanel, t, vmi.getVariantRef());
			
					getBNAComponent().getModel().addThing(editPanel);
					editPanel.setLocalBoundingBox(new Rectangle(0, 0, 300, 135));
					centerInComponent(editPanel);
				}
				else if(vmiType == VariantMenuItem.REMOVE_VARIANT){
					ObjRef variantRef = vmi.getVariantRef();
					ObjRef parentRef = xarch.getParent(variantRef);
					xarch.remove(parentRef, "Variant", variantRef);
				}
			}
		}


		class EditVariantPanel extends JPanel implements WindowHeaderPanelListener, ActionListener{
			protected Thing swingPanelThing;
			protected Thing thingToEdit;
			protected ObjRef variantRef;
		
			protected JButton bSelectedType;
			protected JTextField tfGuard;
			protected JComboBox cbGuard;
			protected JButton bOK;
		
			protected ObjRef selectedTypeRef = null;
		
			public EditVariantPanel(SwingPanelThing swingPanelThing, Thing thingToEdit, ObjRef variantRef){
				super();
				this.swingPanelThing = swingPanelThing;
				this.thingToEdit = thingToEdit;
				this.variantRef = variantRef;
		
				this.setLayout(new BorderLayout());
			
				WindowHeaderPanel whp = new WindowHeaderPanel("Edit Variant");
				whp.addWindowHeaderPanelListener(this);
			
				this.add("North", whp);
				
				bSelectedType = new JButton("[No Selected Type]");
				bSelectedType.addActionListener(this);
			
				cbGuard = new JComboBox(archVariantsTreePlugin.getDocumentGuardStrings());
				cbGuard.setEditable(true);
				cbGuard.setPrototypeDisplayValue("aaaaaaaaaaaaaaaaaaaaaaaaa");
				cbGuard.setRenderer(new TooltipComboBoxRenderer());
				
				tfGuard = (JTextField)cbGuard.getEditor().getEditorComponent();
				tfGuard.addActionListener(this);
				SwingThingUtils.requestFocusOnDisplay(tfGuard);
			
				bOK = new JButton("OK");
				bOK.addActionListener(this);
			
				JPanel centerPanel1 = new JPanel();
				centerPanel1.setLayout(new FlowLayout(FlowLayout.LEFT));
				centerPanel1.add(new JLabel("Type:"));
				centerPanel1.add(bSelectedType);
			
				JPanel centerPanel2 = new JPanel();
				centerPanel2.setLayout(new FlowLayout(FlowLayout.LEFT));
				centerPanel2.add(new JLabel("Guard:"));
				centerPanel2.add(cbGuard);
				
				JPanel centerPanel3 = new JPanel();
				centerPanel3.setLayout(new FlowLayout(FlowLayout.LEFT));
				centerPanel3.add(bOK);
			
				JPanel centerPanel = new JPanel();
				centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
				centerPanel.add(centerPanel1);
				centerPanel.add(centerPanel2);
				centerPanel.add(centerPanel3);
			
				this.add("Center", new JPanelUL(centerPanel));
				swingPanelThing.setPanel(this);
				swingPanelThing.setIndicatorThingId(thingToEdit.getID());
				
				selectedTypeRef = XadlUtils.resolveXLink(xarch, variantRef, "variantType");
				updateButton();
				updateTextField();
			}
			
			protected void updateButton(){
				if(selectedTypeRef != null){
					if(xarch.isInstanceOf(selectedTypeRef, "edu.uci.isr.xarch.types.IComponentType")){
						bSelectedType.setIcon(edu.uci.ics.xadlutils.Resources.COMPONENT_TYPE_ICON);
					}
					else if(xarch.isInstanceOf(selectedTypeRef, "edu.uci.isr.xarch.types.IConnectorType")){
						bSelectedType.setIcon(edu.uci.ics.xadlutils.Resources.CONNECTOR_TYPE_ICON);
					}
					else{
						bSelectedType.setIcon(null);
					}
					
					String variantTypeDescription = XadlUtils.getDescription(xarch, selectedTypeRef);
					if(variantTypeDescription == null){
						variantTypeDescription = "(Type Lacking Description)";
					}
					bSelectedType.setText(variantTypeDescription);
				}
			}
		
			protected void updateTextField(){
				String guardString = bni.booleanGuardToString(variantRef);
				if(guardString == null) guardString = "";
				tfGuard.setText(guardString);
			}
			
			protected boolean setNewGuardValue(String newValue){
				try{
					ObjRef xArchRef = xarch.getXArch(variantRef);
					if(xArchRef == null){
						//This shouldn't happen
						JOptionPane.showMessageDialog(c, "Thing has no xArch document associated.", "Error", JOptionPane.ERROR_MESSAGE);
						return true;
					}
					ObjRef newGuardRef = bni.parseBooleanGuard(newValue, xArchRef);
					if(newGuardRef == null){
						//This shouldn't happen
						JOptionPane.showMessageDialog(c, "Unknown error parsing expression.", "Error", JOptionPane.ERROR_MESSAGE);
						return false;
					}
					xarch.set(variantRef, "guard", newGuardRef);
					return true;
				}
				catch(TokenMgrError tme){
					int errorColumn = tme.errorColumn;
					if(errorColumn != -1){
						try{
							tfGuard.requestFocus();
							tfGuard.setSelectionStart(errorColumn - 1);
							tfGuard.setSelectionEnd(errorColumn);
							//tfGuard.setCaretPosition(errorColumn);
						}
						catch(Throwable t){
						}
					}
					JOptionPane.showMessageDialog(c, tme.getMessage(), "Error Parsing Expression", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				catch(Throwable t){
					JOptionPane.showMessageDialog(c, t.toString(), "Error Parsing Expression", JOptionPane.ERROR_MESSAGE);
					//t.printStackTrace();
					return false;
				}
			}
			
			public void typeButtonPressed(){
				ObjRef xArchRef = xarch.getXArch(variantRef);
				java.awt.Frame frameParent = (java.awt.Frame)SwingUtilities.getAncestorOfClass(java.awt.Frame.class, c);
				TypeSelectorDialog tsd = new TypeSelectorDialog(frameParent, xarch, xArchRef, TypeSelectorDialog.COMPONENT_TYPES | TypeSelectorDialog.CONNECTOR_TYPES);
				tsd.doPopup();
				ObjRef selectedTypeRef = tsd.getSelectedTypeRef();
				if(selectedTypeRef == null){
					return;
				}
				this.selectedTypeRef = selectedTypeRef;
				updateButton();
			}
			

			public void closeButtonPressed(WindowHeaderPanel src){
				remove();
			}
			
			public void actionPerformed(ActionEvent evt){
				if(evt.getSource() == bSelectedType){
					typeButtonPressed();
				}
				else if(evt.getSource() == bOK){
					if(selectedTypeRef == null){
						JOptionPane.showMessageDialog(c, "No type selected.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					String selectedTypeID = XadlUtils.getID(xarch, selectedTypeRef);
					if(selectedTypeID == null){
						JOptionPane.showMessageDialog(c, "Selected type has no ID.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					//We should be OK to set the type XLink for the variant, let's check
					//(and set) the guard value.
					String newGuard = tfGuard.getText();
					if(setNewGuardValue(newGuard)){
						XadlUtils.setXLink(xarch, variantRef, "variantType", selectedTypeID);
						remove();
					}
				}
				else if(evt.getSource() == tfGuard){
					bOK.doClick();
				}
			}
			
			public void remove(){
				getBNAComponent().getModel().removeThing(swingPanelThing);
			}
		}
	}
	
	static class VariantMenuItem extends JMenuItem{
		public static final int EDIT_VARIANT = 100;
		public static final int REMOVE_VARIANT = 200;
			
		protected int itemType;
		protected ObjRef variantRef;
		protected String typeDescription;
			
		public VariantMenuItem(int itemType, ObjRef variantRef, String typeDescription){
			super(typeDescription);
			this.itemType = itemType;
			this.variantRef = variantRef;
			this.typeDescription = typeDescription;
		}
			
		public int getItemType(){
			return itemType;
		}
			
		public ObjRef getVariantRef(){
			return variantRef;
		}
			
		public String getTypeDescription(){
			return typeDescription;
		}
	}
	
}

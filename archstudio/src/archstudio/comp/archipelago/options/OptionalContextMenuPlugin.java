package archstudio.comp.archipelago.options;

import archstudio.comp.archipelago.*;
import archstudio.comp.archipelago.types.*;
import archstudio.comp.booleannotation.IBooleanNotation;
import archstudio.comp.booleannotation.TokenMgrError;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import c2.fw.MessageProvider;
import c2.util.UIDGenerator;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.contextmenu.*;
import edu.uci.ics.bna.swingthing.*;
import edu.uci.ics.widgets.TooltipComboBoxRenderer;
import edu.uci.ics.widgets.windowheader.*;

import edu.uci.ics.xarchutils.*;

public class OptionalContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{
	protected ArchOptionsTreePlugin archOptionsTreePlugin;
	protected XArchFlatInterface xarch;
	protected ThingIDMap thingIDMap;
	protected IBooleanNotation bni;
	
	public OptionalContextMenuPlugin(BNAComponent c, ArchOptionsTreePlugin aotp,
	ThingIDMap thingIDMap, XArchFlatInterface xarch, IBooleanNotation bni){
		super(c);
		this.archOptionsTreePlugin = aotp;
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
		
		if((t instanceof BrickThing) || 
		(t instanceof LinkThing) || 
		(t instanceof InterfaceThing) || 
		(t instanceof SignatureThing)){
			if(currentContextMenu.getSubElements().length > 0){
				currentContextMenu.addSeparator();
			}
			OptionalMenuItemSet nsmis = new OptionalMenuItemSet(t);
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
		
	class OptionalMenuItemSet implements ActionListener{
		protected JMenuItem miPromoteToOptional;
		protected JMenuItem miMakeMandatory;
		protected JMenuItem miEditGuard;
		
		protected Thing t;
				
		public OptionalMenuItemSet(Thing t){
			this.t = t;
			miPromoteToOptional = new JMenuItem("Promote to Optional");
			miPromoteToOptional.addActionListener(this);

			miMakeMandatory = new JMenuItem("Make Mandatory");
			miMakeMandatory.addActionListener(this);

			miEditGuard = new JMenuItem("Make Optional/Edit Guard...");
			miEditGuard.addActionListener(this);

			ObjRef eltRef = thingIDMap.getXArchRef(t.getID());
			if(eltRef == null){
				return;
			}

			boolean isOptional = false;
			if(t instanceof ComponentThing){
				isOptional = xarch.isInstanceOf(eltRef, "edu.uci.isr.xarch.options.IOptionalComponent");
			}
			else if(t instanceof ConnectorThing){
				isOptional = xarch.isInstanceOf(eltRef, "edu.uci.isr.xarch.options.IOptionalConnector");
			}
			else if(t instanceof InterfaceThing){
				isOptional = xarch.isInstanceOf(eltRef, "edu.uci.isr.xarch.options.IOptionalInterface");
			}
			else if(t instanceof SignatureThing){
				isOptional = xarch.isInstanceOf(eltRef, "edu.uci.isr.xarch.options.IOptionalSignature");
			}
			else if(t instanceof LinkThing){
				isOptional = xarch.isInstanceOf(eltRef, "edu.uci.isr.xarch.options.IOptionalLink");
			}
			
			boolean isMandatory = true;
			if(!isOptional){
				miPromoteToOptional.setEnabled(true);
				miMakeMandatory.setEnabled(false);
				miEditGuard.setEnabled(false);
			}
			else{
				miPromoteToOptional.setEnabled(false);
				
				ObjRef optionalRef = (ObjRef)xarch.get(eltRef, "optional");
				if(optionalRef != null){
					ObjRef guardRef = (ObjRef)xarch.get(optionalRef, "guard");
					if(guardRef != null){
						isMandatory = false;
					}
				}
				
				if(isMandatory){
					miMakeMandatory.setEnabled(false);
					miEditGuard.setEnabled(true);
				}
				else{
					miMakeMandatory.setEnabled(true);
					miEditGuard.setEnabled(true);
				}
			}
		}
		
		public JMenuItem[] getMenuItemSet(){
			return new JMenuItem[]{miPromoteToOptional, miMakeMandatory, miEditGuard};
		}
		
		public void actionPerformed(ActionEvent evt){
			ObjRef eltRef = thingIDMap.getXArchRef(t.getID());
			if(eltRef == null){
				JOptionPane.showMessageDialog(c, "Thing not mapped to xArch element.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if(evt.getSource() == miPromoteToOptional){
				ObjRef xArchRef = xarch.getXArch(eltRef);
				ObjRef optionsContextRef = xarch.createContext(xArchRef, "options");

				if(t instanceof ComponentThing){
					xarch.promoteTo(optionsContextRef, "optionalComponent", eltRef);
				}
				else if(t instanceof ConnectorThing){
					xarch.promoteTo(optionsContextRef, "optionalConnector", eltRef);
				}
				else if(t instanceof InterfaceThing){
					xarch.promoteTo(optionsContextRef, "optionalInterface", eltRef);
				}
				else if(t instanceof SignatureThing){
					xarch.promoteTo(optionsContextRef, "optionalSignature", eltRef);
				}
				else if(t instanceof LinkThing){
					xarch.promoteTo(optionsContextRef, "optionalLink", eltRef);
				}
			}
			else if(evt.getSource() == miMakeMandatory){
				//Clear the element's optional
				xarch.clear(eltRef, "optional");
			}
			else if(evt.getSource() == miEditGuard){
				SwingPanelThing editPanel = new SwingPanelThing();
				EditGuardPanel ep = new EditGuardPanel(editPanel, t);
			
				getBNAComponent().getModel().addThing(editPanel);
				editPanel.setLocalBoundingBox(new Rectangle(0, 0, 300, 100));
				centerInComponent(editPanel);
			}
		}


		class EditGuardPanel extends JPanel implements WindowHeaderPanelListener, ActionListener{
			protected Thing swingPanelThing;
			protected Thing thingToEdit;
		
			protected JComboBox cbGuard;
			protected JTextField tfGuard;
			protected JButton bOK;
		
			public EditGuardPanel(SwingPanelThing swingPanelThing, Thing thingToEdit){
				super();
				this.swingPanelThing = swingPanelThing;
				this.thingToEdit = thingToEdit;
		
				this.setLayout(new BorderLayout());
			
				WindowHeaderPanel whp = new WindowHeaderPanel("Edit/Set Guard");
				whp.addWindowHeaderPanelListener(this);
			
				this.add("North", whp);
			
				cbGuard = new JComboBox(archOptionsTreePlugin.getDocumentGuardStrings());
				cbGuard.setPrototypeDisplayValue("aaaaaaaaaaaaaaaaaaaaaaaaa");
				cbGuard.setRenderer(new TooltipComboBoxRenderer());
				
				cbGuard.setEditable(true);
				tfGuard = (JTextField)cbGuard.getEditor().getEditorComponent();
				tfGuard.addActionListener(this);
				SwingThingUtils.requestFocusOnDisplay(tfGuard);
			
				bOK = new JButton("OK");
				bOK.addActionListener(this);
			
				JPanel centerPanel = new JPanel();
				centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				centerPanel.add(cbGuard);
				centerPanel.add(bOK);
			
				this.add("Center", centerPanel);
				swingPanelThing.setPanel(this);
				swingPanelThing.setIndicatorThingId(thingToEdit.getID());
				updateTextField();
			}
		
			protected void updateTextField(){
				String thingToEditId = thingToEdit.getID();
				ObjRef thingToEditRef = thingIDMap.getXArchRef(thingToEditId);
				if(thingToEditRef != null){
					String guardString = null;
					ObjRef optionalRef = (ObjRef)xarch.get(thingToEditRef, "optional");
					if(optionalRef != null){
						guardString = bni.booleanGuardToString(optionalRef);
					}
					if(guardString == null) guardString = "";
					tfGuard.setText(guardString);
				}
			}
			
			protected boolean setNewValue(String newValue){
				String thingToEditId = thingToEdit.getID();
				ObjRef thingToEditRef = thingIDMap.getXArchRef(thingToEditId);
				if(thingToEditRef != null){
					try{
						ObjRef xArchRef = xarch.getXArch(thingToEditRef);
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
						boolean foundOptionalRef = true;
						ObjRef optionalRef = (ObjRef)xarch.get(thingToEditRef, "optional");
						if(optionalRef == null){
							foundOptionalRef = false;
							ObjRef optionsContextRef = xarch.createContext(xArchRef, "options");
							optionalRef = xarch.create(optionsContextRef, "optional");
						}
						xarch.set(optionalRef, "guard", newGuardRef);
						if(!foundOptionalRef){
							xarch.set(thingToEditRef, "optional", optionalRef);
						}
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
				else{
					JOptionPane.showMessageDialog(c, "Thing has no xArch element associated.", "Error", JOptionPane.ERROR_MESSAGE);
					return true;
				}
			}

			public void closeButtonPressed(WindowHeaderPanel src){
				remove();
			}
			
			public void actionPerformed(ActionEvent evt){
				if(evt.getSource() == bOK){
					String newDescription = tfGuard.getText();
					if(setNewValue(newDescription)){
						remove();
					}
				}
				if(evt.getSource() == tfGuard){
					bOK.doClick();
				}
			}
			
			public void remove(){
				getBNAComponent().getModel().removeThing(swingPanelThing);
			}
		}
	}
	
	
}

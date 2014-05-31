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

public class EditDirectionContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{
	
	protected XArchFlatInterface xarch;
	protected ThingIDMap thingIDMap;
	
	public EditDirectionContextMenuPlugin(BNAComponent c, ThingIDMap thingIDMap, XArchFlatInterface xarch){
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
		
		if(t instanceof IEditableDirection){
			EditDirectionMenuItemSet edmis = new EditDirectionMenuItemSet(t);
			JMenuItem[] miArray = edmis.getMenuItemSet();
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
	
	class EditDirectionMenuItemSet implements ActionListener{
		protected Thing thingToEdit;
		protected JMenuItem miEditDirection;
		
		public EditDirectionMenuItemSet(Thing thingToEdit){
			this.thingToEdit = thingToEdit;
			miEditDirection = new JMenuItem("Edit Direction...");
			miEditDirection.addActionListener(this);
		}
		
		public JMenuItem[] getMenuItemSet(){
			return new JMenuItem[]{miEditDirection};
		}
		
		public void actionPerformed(ActionEvent evt){
			SwingPanelThing descEditPanel = new SwingPanelThing();
			EditDirectionPanel edp = new EditDirectionPanel(descEditPanel, thingToEdit);
			
			getBNAComponent().getModel().addThing(descEditPanel);
			descEditPanel.setLocalBoundingBox(new Rectangle(0, 0, 300, 90));
			centerInComponent(descEditPanel);
		}

		class EditDirectionPanel extends JPanel implements WindowHeaderPanelListener, ActionListener{
			protected Thing swingPanelThing;
			protected Thing thingToEdit;
		
			protected JComboBox cbDirection;
			protected JButton bOK;
		
			public EditDirectionPanel(SwingPanelThing swingPanelThing, Thing thingToEdit){
				super();
				this.swingPanelThing = swingPanelThing;
				this.thingToEdit = thingToEdit;
		
				this.setLayout(new BorderLayout());
			
				WindowHeaderPanel whp = new WindowHeaderPanel("Edit Direction");
				whp.addWindowHeaderPanelListener(this);
			
				this.add("North", whp);
			
				DefaultComboBoxModel cbm = new DefaultComboBoxModel();
				cbm.addElement("none");
				cbm.addElement("in");
				cbm.addElement("out");
				cbm.addElement("inout");
				
				cbDirection = new JComboBox(cbm);
				cbDirection.setEditable(true);
				cbDirection.addActionListener(this);
			
				bOK = new JButton("OK");
				bOK.addActionListener(this);
			
				JPanel centerPanel = new JPanel();
				centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				centerPanel.add(cbDirection);
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
					String direction = edu.uci.ics.xadlutils.XadlUtils.getDirection(xarch, thingToEditRef);
					cbDirection.getEditor().setItem(direction);
				}
			}
			
			protected void setNewValue(String newValue){
				String thingToEditId = thingToEdit.getID();
				ObjRef thingToEditRef = thingIDMap.getXArchRef(thingToEditId);
				if(thingToEditRef != null){
					edu.uci.ics.xadlutils.XadlUtils.setDirection(xarch, thingToEditRef, newValue);
				}
			}

			public void closeButtonPressed(WindowHeaderPanel src){
				remove();
			}
			
			public void actionPerformed(ActionEvent evt){
				if(evt.getSource() == bOK){
					String newDirection = (String)cbDirection.getEditor().getItem();
					remove();
					setNewValue(newDirection);
				}
				if(evt.getSource() == cbDirection){
					bOK.doClick();
				}
			}
			
			public void remove(){
				getBNAComponent().getModel().removeThing(swingPanelThing);
			}
		}
	}
}


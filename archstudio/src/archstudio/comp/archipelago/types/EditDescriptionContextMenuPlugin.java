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

public class EditDescriptionContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{
	
	protected XArchFlatInterface xarch;
	protected ThingIDMap thingIDMap;
	
	public EditDescriptionContextMenuPlugin(BNAComponent c, ThingIDMap thingIDMap, XArchFlatInterface xarch){
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
		else if(selectedThingSet.length > 1){
			return currentContextMenu;
		}
		else{
			t = selectedThingSet[0];
		}
		
		if(t instanceof IEditableDescription){
			EditDescriptionMenuItemSet edmis = new EditDescriptionMenuItemSet(t);
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
	
	class EditDescriptionMenuItemSet implements ActionListener{
		protected Thing thingToEdit;
		protected JMenuItem miEditDescription;
				
		public EditDescriptionMenuItemSet(Thing thingToEdit){
			this.thingToEdit = thingToEdit;
			miEditDescription = new JMenuItem("Edit Description...");
			miEditDescription.addActionListener(this);
		}
		
		public JMenuItem[] getMenuItemSet(){
			return new JMenuItem[]{miEditDescription};
		}
		
		public void actionPerformed(ActionEvent evt){
			SwingPanelThing descEditPanel = new SwingPanelThing();
			EditDescriptionPanel edp = new EditDescriptionPanel(descEditPanel, thingToEdit);
			
			getBNAComponent().getModel().addThing(descEditPanel);
			descEditPanel.setLocalBoundingBox(new Rectangle(0, 0, 300, 90));
			centerInComponent(descEditPanel);
		}

		class EditDescriptionPanel extends JPanel implements WindowHeaderPanelListener, ActionListener{
			protected Thing swingPanelThing;
			protected Thing thingToEdit;
		
			protected JTextField tfDescription;
			protected JButton bOK;
		
			public EditDescriptionPanel(SwingPanelThing swingPanelThing, Thing thingToEdit){
				super();
				this.swingPanelThing = swingPanelThing;
				this.thingToEdit = thingToEdit;
		
				this.setLayout(new BorderLayout());
			
				WindowHeaderPanel whp = new WindowHeaderPanel("Edit Description");
				whp.addWindowHeaderPanelListener(this);
			
				this.add("North", whp);
			
				tfDescription = new JTextField(15);
				tfDescription.addActionListener(this);
				SwingThingUtils.requestFocusOnDisplay(tfDescription);
			
				bOK = new JButton("OK");
				bOK.addActionListener(this);
			
				JPanel centerPanel = new JPanel();
				centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
				centerPanel.add(tfDescription);
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
					String description = edu.uci.ics.xadlutils.XadlUtils.getDescription(xarch, thingToEditRef);
					tfDescription.setText(description);
				}
			}
			
			protected void setNewValue(String newValue){
				String thingToEditId = thingToEdit.getID();
				ObjRef thingToEditRef = thingIDMap.getXArchRef(thingToEditId);
				if(thingToEditRef != null){
					edu.uci.ics.xadlutils.XadlUtils.setDescription(xarch, thingToEditRef, newValue);
				}
			}

			public void closeButtonPressed(WindowHeaderPanel src){
				remove();
			}
			
			public void actionPerformed(ActionEvent evt){
				if(evt.getSource() == bOK){
					String newDescription = tfDescription.getText();
					remove();
					setNewValue(newDescription);
				}
				if(evt.getSource() == tfDescription){
					bOK.doClick();
				}
			}
			
			public void remove(){
				getBNAComponent().getModel().removeThing(swingPanelThing);
			}
		}
	}
}

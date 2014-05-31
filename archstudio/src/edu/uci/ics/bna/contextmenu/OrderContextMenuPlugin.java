package edu.uci.ics.bna.contextmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.Rectangle;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.uci.ics.bna.*;
import edu.uci.ics.widgets.WidgetUtils;

public class OrderContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{

	public OrderContextMenuPlugin(BNAComponent c){
		super(c);
	}

	public JPopupMenu addToContextMenu(JPopupMenu currentContextMenu, Thing[] selectedThingSet, Thing thingUnderCursor){
		//Alignment only works if more than one thing is selected.
		if(selectedThingSet.length == 0){
			return currentContextMenu;
		}
		
		JMenu alignSubMenu = new JMenu("Order");
		MenuItemSet mis = new MenuItemSet(selectedThingSet);
		JMenuItem[] miArray = mis.getMenuItems();
		for(int i = 0; i < miArray.length; i++){
			alignSubMenu.add(miArray[i]);
		}
		currentContextMenu.add(alignSubMenu);
		return currentContextMenu;
	}
	
	protected void bringToFront(Thing[] thingSet){
		for(int i = thingSet.length - 1; i >= 0; i--){
			getBNAComponent().getModel().bringToFront(thingSet[i]);
		}
	}

	protected void sendToBack(Thing[] thingSet){
		for(int i = thingSet.length - 1; i >= 0; i--){
			getBNAComponent().getModel().sendToBack(thingSet[i]);
		}
	}
	
	class MenuItemSet implements ActionListener{
		protected JMenuItem miBringToFront;
		protected JMenuItem miSendToBack;
		
		protected Thing[] thingSet;
		
		public MenuItemSet(Thing[] alignableThings){
			this.thingSet = alignableThings;
			miBringToFront = new JMenuItem("Bring to Front");
			miBringToFront.setIcon(WidgetUtils.getImageIcon("edu/uci/ics/bna/res/bring-to-front.gif"));
			miBringToFront.addActionListener(this);
			
			miSendToBack = new JMenuItem("Send to Back");
			miSendToBack.setIcon(WidgetUtils.getImageIcon("edu/uci/ics/bna/res/send-to-back.gif"));
			miSendToBack.addActionListener(this);
		}
		
		public void actionPerformed(ActionEvent evt){
			Object src = evt.getSource();
			if(src == miSendToBack){
				sendToBack(thingSet);
			}
			else if(src == miBringToFront){
				bringToFront(thingSet);
			}
		}
		
		public JMenuItem[] getMenuItems(){
			return new JMenuItem[]{
				miSendToBack,
				miBringToFront
			};
		}
	}
	

}

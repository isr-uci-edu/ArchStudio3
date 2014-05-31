package edu.uci.ics.bna.contextmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.Rectangle;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.uci.ics.bna.*;
import edu.uci.ics.widgets.WidgetUtils;

public class DistributeContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{

	public DistributeContextMenuPlugin(BNAComponent c){
		super(c);
	}

	public JPopupMenu addToContextMenu(JPopupMenu currentContextMenu, Thing[] selectedThingSet, Thing thingUnderCursor){
		//Alignment only works if more than one thing is selected.
		if(selectedThingSet.length < 2){
			return currentContextMenu;
		}
		
		for(int i = 0; i < selectedThingSet.length; i++){
			if(!(selectedThingSet[i] instanceof BoxThing)){
				return currentContextMenu; 
			}
		}
		
		//OK, everything that's selected is a box.
		JMenu alignSubMenu = new JMenu("Distribute");
		MenuItemSet mis = new MenuItemSet(selectedThingSet);
		JMenuItem[] miArray = mis.getMenuItems();
		for(int i = 0; i < miArray.length; i++){
			alignSubMenu.add(miArray[i]);
		}
		currentContextMenu.add(alignSubMenu);
		return currentContextMenu;
	}
	

	static class MenuItemSet implements ActionListener{
		protected JMenuItem miDistributeHorizontallyTight;
		protected JMenuItem miDistributeHorizontallyLoose;
		protected JMenuItem miDistributeVerticallyTight;
		protected JMenuItem miDistributeVerticallyLoose;
		
		protected Thing[] distributableThings;
		
		public MenuItemSet(Thing[] distributableThings){
			this.distributableThings = distributableThings;
			miDistributeHorizontallyTight  = new JMenuItem("Distribute Horizontally (Tight)");
			miDistributeHorizontallyTight.setIcon(WidgetUtils.getImageIcon("edu/uci/ics/bna/res/distribute-horiz-tight.gif"));
			miDistributeHorizontallyTight.addActionListener(this);
			
			miDistributeVerticallyTight = new JMenuItem("Distribute Vertically (Tight)");
			miDistributeVerticallyTight.setIcon(WidgetUtils.getImageIcon("edu/uci/ics/bna/res/distribute-vert-tight.gif"));
			miDistributeVerticallyTight.addActionListener(this);

			miDistributeHorizontallyLoose = new JMenuItem("Distribute Horizontally (Bounds)");
			miDistributeHorizontallyLoose.setIcon(WidgetUtils.getImageIcon("edu/uci/ics/bna/res/distribute-horiz-loose.gif"));
			miDistributeHorizontallyLoose.addActionListener(this);

			miDistributeVerticallyLoose = new JMenuItem("Distribute Vertically (Bounds)");
			miDistributeVerticallyLoose.setIcon(WidgetUtils.getImageIcon("edu/uci/ics/bna/res/distribute-vert-loose.gif"));
			miDistributeVerticallyLoose.addActionListener(this);
		}
		
		public void actionPerformed(ActionEvent evt){
			Object src = evt.getSource();
			
			if(src == miDistributeHorizontallyTight){
				DistributeUtils.distributeHorizontallyTight(distributableThings);
			}
			else if(src == miDistributeVerticallyTight){
				DistributeUtils.distributeVerticallyTight(distributableThings);
			}
			else if(src == miDistributeHorizontallyLoose){
				DistributeUtils.distributeHorizontallyLoose(distributableThings);
			}
			else if(src == miDistributeVerticallyLoose){
				DistributeUtils.distributeVerticallyLoose(distributableThings);
			}
		}
		
		public JMenuItem[] getMenuItems(){
			return new JMenuItem[]{
				miDistributeHorizontallyTight,
				miDistributeHorizontallyLoose,
				miDistributeVerticallyTight,
				miDistributeVerticallyLoose
			};
		}
	}
	

}

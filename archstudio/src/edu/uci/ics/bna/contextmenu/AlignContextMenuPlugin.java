package edu.uci.ics.bna.contextmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.Rectangle;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.uci.ics.bna.*;
import edu.uci.ics.widgets.WidgetUtils;

public class AlignContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{

	public AlignContextMenuPlugin(BNAComponent c){
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
		JMenu alignSubMenu = new JMenu("Align");
		MenuItemSet mis = new MenuItemSet(selectedThingSet);
		JMenuItem[] miArray = mis.getMenuItems();
		for(int i = 0; i < miArray.length; i++){
			alignSubMenu.add(miArray[i]);
		}
		currentContextMenu.add(alignSubMenu);
		return currentContextMenu;
	}
	

	static class MenuItemSet implements ActionListener{
		protected JMenuItem miAlignTops;
		protected JMenuItem miAlignBottoms;
		protected JMenuItem miAlignVerticalCenters;
		protected JMenuItem miAlignLefts;
		protected JMenuItem miAlignRights;
		protected JMenuItem miAlignHorizontalCenters;
		
		protected Thing[] alignableThings;
		
		public MenuItemSet(Thing[] alignableThings){
			this.alignableThings = alignableThings;
			miAlignTops = new JMenuItem("Align Tops");
			miAlignTops.setIcon(WidgetUtils.getImageIcon("edu/uci/ics/bna/res/align-tops.gif"));
			miAlignTops.addActionListener(this);
			
			miAlignBottoms = new JMenuItem("Align Bottoms");
			miAlignBottoms.setIcon(WidgetUtils.getImageIcon("edu/uci/ics/bna/res/align-bottoms.gif"));
			miAlignBottoms.addActionListener(this);

			miAlignHorizontalCenters = new JMenuItem("Align Horizontal Centers");
			miAlignHorizontalCenters.setIcon(WidgetUtils.getImageIcon("edu/uci/ics/bna/res/align-horizontal-centers.gif"));
			miAlignHorizontalCenters.addActionListener(this);

			miAlignLefts = new JMenuItem("Align Lefts");
			miAlignLefts.setIcon(WidgetUtils.getImageIcon("edu/uci/ics/bna/res/align-lefts.gif"));
			miAlignLefts.addActionListener(this);

			miAlignRights = new JMenuItem("Align Rights");
			miAlignRights.setIcon(WidgetUtils.getImageIcon("edu/uci/ics/bna/res/align-rights.gif"));
			miAlignRights.addActionListener(this);

			miAlignVerticalCenters = new JMenuItem("Align Vertical Centers");
			miAlignVerticalCenters.setIcon(WidgetUtils.getImageIcon("edu/uci/ics/bna/res/align-vertical-centers.gif"));
			miAlignVerticalCenters.addActionListener(this);
		}
		
		public void actionPerformed(ActionEvent evt){
			Object src = evt.getSource();
			if(src == miAlignTops){
				AlignUtils.alignTops(alignableThings);
			}
			else if(src == miAlignHorizontalCenters){
				AlignUtils.alignHorizontalCenters(alignableThings);
			}
			else if(src == miAlignBottoms){
				AlignUtils.alignBottoms(alignableThings);
			}
			if(src == miAlignLefts){
				AlignUtils.alignLefts(alignableThings);
			}
			else if(src == miAlignVerticalCenters){
				AlignUtils.alignVerticalCenters(alignableThings);
			}
			else if(src == miAlignRights){
				AlignUtils.alignRights(alignableThings);
			}
		}
		
		public JMenuItem[] getMenuItems(){
			return new JMenuItem[]{
				miAlignTops,
				miAlignHorizontalCenters,
				miAlignBottoms,
				miAlignLefts,
				miAlignVerticalCenters,
				miAlignRights
			};
		}
	}
	

}

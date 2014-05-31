package edu.uci.ics.bna.contextmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.uci.ics.bna.*;
import edu.uci.ics.widgets.WidgetUtils;

public class RotateContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{

	public RotateContextMenuPlugin(BNAComponent c){
		super(c);
	}

	public JPopupMenu addToContextMenu(JPopupMenu currentContextMenu, Thing[] selectedThingSet, Thing thingUnderCursor){
		//Alignment only works if more than one thing is selected.
		
		if(selectedThingSet.length == 0){
			if(thingUnderCursor instanceof IRotatable){
				MenuItemSet mis = new MenuItemSet((IRotatable)thingUnderCursor, c.getModel());
				JMenuItem[] miArray = mis.getMenuItems();
				for(int i = 0; i < miArray.length; i++){
					currentContextMenu.add(miArray[i]);
				}
			}
		}
		return currentContextMenu;
	}
	

	static class MenuItemSet implements ActionListener{
		protected JMenuItem miRotate;
		protected JMenuItem miClearRotate;
		
		protected IRotatable rotatableThing;
		protected BNAModel m;
		
		public MenuItemSet(IRotatable rotatableThing, BNAModel m){
			this.rotatableThing = rotatableThing;
			this.m = m;
			
			miRotate = new JMenuItem("Rotate");
			miRotate.setIcon(WidgetUtils.getImageIcon("edu/uci/ics/bna/res/rotate.gif"));
			miRotate.addActionListener(this);
			
			miClearRotate = new JMenuItem("Clear Rotation");
			miClearRotate.addActionListener(this);
		}
		
		public void actionPerformed(ActionEvent evt){
			Object src = evt.getSource();
			if(src == miRotate){
				RotaterThing rt = new RotaterThing();

				Point anchorPoint = new Point(DefaultCoordinateMapper.DEFAULT_WORLD_WIDTH / 2, DefaultCoordinateMapper.DEFAULT_WORLD_HEIGHT / 2);
				if(rotatableThing instanceof IAnchored){
					Point p = ((IAnchored)rotatableThing).getAnchorPoint();
					anchorPoint.x = p.x;
					anchorPoint.y = p.y;
					rt.setMoveTogetherMode(IMoveTogether.MOVE_TOGETHER_TRACK_ANCHOR_POINT_ONLY);
				}
				else if(rotatableThing instanceof IBoxBounded){
					Rectangle boundingBox = ((IBoxBounded)rotatableThing).getBoundingBox();
					if(boundingBox != null){
						anchorPoint.x = boundingBox.x + (boundingBox.width / 2);
						anchorPoint.y = boundingBox.y + (boundingBox.height / 2);
					}
					rt.setMoveTogetherMode(IMoveTogether.MOVE_TOGETHER_TRACK_BOUNDING_BOX_ONLY);
				}
				rt.setMoveTogetherThingId(rotatableThing.getID());
				rt.addRotatedThingId(rotatableThing.getID());
				rt.setAnchorPoint(anchorPoint);
				rt.setRotationAngle(rotatableThing.getRotationAngle());
				m.addThing(rt);
			}
			else if(src == miClearRotate){
				rotatableThing.setRotationAngle(0);
			}
		}
		
		public JMenuItem[] getMenuItems(){
			return new JMenuItem[]{
				miRotate, miClearRotate
			};
		}
	}
	

}

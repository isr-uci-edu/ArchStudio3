package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import edu.uci.ics.bna.*;

public class NoThingLogic extends ThingLogicAdapter{

	protected DotMarqueeThing marqueeSelection = null;
	
	protected int initDownX = -1;
	protected int initDownY = -1;

	public void destroy() {
		BNAComponent c = bnaComponent;
		if (marqueeSelection != null) {
			c.getModel().removeThing(marqueeSelection);
			marqueeSelection = null;
		}
		super.destroy();
	}
	
	public void mousePressed(Thing t, MouseEvent evt, int worldX, int worldY){
		BNAComponent c = bnaComponent;
		if(evt.getButton() == MouseEvent.BUTTON1){
			if(t == null){
				initDownX = worldX;
				initDownY = worldY;
			
				marqueeSelection = new DotMarqueeThing();
				marqueeSelection.setID("MarqueeSelection");
				BNAUtils.setStackingPriority(marqueeSelection, BNAUtils.STACKING_PRIORITY_ALWAYS_ON_TOP);
				marqueeSelection.setX1(worldX);
				marqueeSelection.setY1(worldY);
				marqueeSelection.setX2(worldX);
				marqueeSelection.setY2(worldY);
				c.getModel().addThing(marqueeSelection);
			}
		}
	}
	
	public void mouseReleased(Thing t, MouseEvent evt, int worldX, int worldY){
		BNAComponent c = bnaComponent;
		if(evt.getButton() == MouseEvent.BUTTON1){
			if(marqueeSelection != null){
				//System.out.println("Handle selection");
				int wx1 = marqueeSelection.getX1();
				int wx2 = marqueeSelection.getX2();
				int wy1 = marqueeSelection.getY1();
				int wy2 = marqueeSelection.getY2();
				
				if(wx1 > wx2){
					int tmp = wx2;
					wx2 = wx1;
					wx1 = tmp;
				}
				if(wy1 > wy2){
					int tmp = wy2;
					wy2 = wy1;
					wy1 = tmp;
				}
				c.getModel().removeThing(marqueeSelection);
				
				Rectangle selectRect = new Rectangle();
				selectRect.x = wx1;
				selectRect.y = wy1;
				selectRect.width = wx2 - wx1;
				selectRect.height = wy2 - wy1;
				
				if(!BNAUtils.wasControlPressed(evt)){
					SelectionUtils.removeAllSelections(c);
				}
				
				try{
					c.getModel().beginBulkChange();
					for(Iterator it = c.getModel().getThingIterator(); it.hasNext(); ){
						Thing t2 = (Thing)it.next();
						if(t2 instanceof IMarqueeSelectable){
							Rectangle boundingBox = ((IBoxBounded)t2).getBoundingBox();
							if(BNAUtils.isWithin(selectRect, boundingBox)){
								if (t2 instanceof IVisible) {
									if (!((IVisible) t2).isVisible())
										continue;
								}
								if (t2 instanceof IUserEditable) {
									if (!((IUserEditable) t2).isUserEditable())
										continue;
								}
								if(!BNAUtils.wasControlPressed(evt)){
									((ISelectable)t2).setSelected(true);
								}
								else{
									((ISelectable)t2).setSelected(!((ISelectable)t2).isSelected());
								}								
							}
						}
					}
				}finally{
					c.getModel().endBulkChange();
				}
				marqueeSelection = null;
			}
		}
	}
	
	public void mouseDragged(Thing t, MouseEvent evt, int worldX, int worldY){
		BNAComponent c = bnaComponent;
		if(marqueeSelection != null){
			marqueeSelection.setX2(worldX);
			marqueeSelection.setY2(worldY);
		}
	}
	

}
package edu.uci.ics.bna.logic;

import java.awt.Point;
import java.awt.event.MouseEvent;

import edu.uci.ics.bna.*;

public class RotaterThingLogic extends ThingLogicAdapter {

	RotaterThing rt;
	boolean pressed = false;

	public void mousePressed(Thing t, MouseEvent evt, int worldX, int worldY){
		if(t instanceof RotaterThing){
			pressed = true;
			rt = (RotaterThing)t;
		}
	}
	
	public void mouseDragged(Thing t, MouseEvent evt, int worldX, int worldY){
		handleMouseEvent(t, evt, worldX, worldY);
	}
	
	public void mouseReleased(Thing t, MouseEvent evt, int worldX, int worldY){
		pressed = false;
	}
	
	private void handleMouseEvent(Thing t, MouseEvent evt, int worldX, int worldY){
		if(pressed){
			Point anchorPointWorld = rt.getAnchorPoint();
			int rwx = anchorPointWorld.x;
			int rwy = anchorPointWorld.y;
			
			int dx = worldX - rwx;
			int dy = worldY - rwy;
			
			double angleInRadians = Math.atan((double)dy / (double)dx);
			double angleInDegrees = (angleInRadians * 180) / Math.PI;
			if(dx < 0) angleInDegrees = (angleInDegrees + 180) % 360;
			int intAngle = BNAUtils.round(angleInDegrees);
			int increment = rt.getAdjustmentIncrement();
			if(increment > 1){
				while((intAngle % increment) != 0){
					intAngle = (intAngle + 1) % 360;
				}
			}
			rt.setRotationAngle(intAngle);
			
			String[] rotatedThingIds = rt.getRotatedThingIds();
			if(rotatedThingIds != null){
				BNAComponent c = getBNAComponent();
				if(c != null){
					BNAModel m = c.getModel();
					if(m != null){
						for(int i = 0; i < rotatedThingIds.length; i++){
							Thing rotatedThing = m.getThing(rotatedThingIds[i]);
							if(rotatedThing != null){
								if(rotatedThing instanceof IRotatable){
									((IRotatable)rotatedThing).setRotationAngle(intAngle);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void mouseClicked(Thing t, MouseEvent evt, int worldX, int worldY){
		if(evt.getClickCount() == 2){
			if(evt.getButton() == MouseEvent.BUTTON1){
				if(t instanceof RotaterThing){
					BNAComponent c = getBNAComponent();
					if(c != null){
						BNAModel m = c.getModel();
						if(m != null){
							m.removeThing(t);
						}
					}
				}
			}
		}
	}
}

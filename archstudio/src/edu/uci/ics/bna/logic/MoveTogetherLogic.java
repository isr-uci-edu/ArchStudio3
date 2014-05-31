package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import c2.util.ArrayUtils;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.logic.*;

public class MoveTogetherLogic extends ThingLogicAdapter implements BoundingBoxTrackingListener,
AnchorPointTrackingListener{

	protected BoundingBoxTrackingLogic boundingBoxTrackingLogic;
	protected AnchorPointTrackingLogic anchorPointTrackingLogic;
	
	//Maps ids of moved-together things to the id of the IMoveTogether pointing at them.
	protected Map moveTogetherThingMap = new HashMap();
	
	public MoveTogetherLogic(BoundingBoxTrackingLogic boundingBoxTrackingLogic, 
	AnchorPointTrackingLogic anchorPointTrackingLogic){
		this.boundingBoxTrackingLogic = boundingBoxTrackingLogic;
		boundingBoxTrackingLogic.addBoundingBoxTrackingListener(this);
		this.anchorPointTrackingLogic = anchorPointTrackingLogic;
		anchorPointTrackingLogic.addAnchorPointTrackingListener(this);
	}
	
	public void init(){
		BNAModel m = getBNAComponent().getModel();
		if(m != null){
			Thing[] allThings = m.getAllThings();
			for(int i = 0; i < allThings.length; i++){
				if(allThings[i] instanceof IMoveTogether){
					String movedThingId = ((IMoveTogether)allThings[i]).getMoveTogetherThingId();
					if(movedThingId != null){
						addThingMoved((IMoveTogether)allThings[i], movedThingId);
					}
				}
			}
		}
	}
	
	private void addThingMoved(IMoveTogether mtt, String movedThingId){
		String[] moveTogetherThingIdArray = (String[])moveTogetherThingMap.get(movedThingId);
		if(moveTogetherThingIdArray == null){
			moveTogetherThingMap.put(movedThingId, new String[]{mtt.getID()});
			return;
		}
		HashSet s = new HashSet(Arrays.asList(moveTogetherThingIdArray));
		s.add(mtt.getID());
		moveTogetherThingIdArray = (String[])s.toArray(moveTogetherThingIdArray);
		moveTogetherThingMap.put(movedThingId, moveTogetherThingIdArray);
	}
	
	private void removeThingMoved(IMoveTogether mtt, String movedThingId){
		String[] moveTogetherThingIdArray = (String[])moveTogetherThingMap.get(movedThingId);
		if(moveTogetherThingIdArray == null){
			return;
		}
		HashSet s = new HashSet(Arrays.asList(moveTogetherThingIdArray));
		s.remove(mtt.getID());
		if(s.isEmpty()){
			moveTogetherThingMap.remove(movedThingId);
		}
		else{
			moveTogetherThingIdArray = (String[])s.toArray(moveTogetherThingIdArray);
			moveTogetherThingMap.put(movedThingId, moveTogetherThingIdArray);
		}
	}

	final String[] emptyStringArray = new String[0];
		
	private String[] getMoveTogetherThingIds(String movedThingId){
		String[] arr = (String[])moveTogetherThingMap.get(movedThingId);
		return (arr == null) ? emptyStringArray : arr;  
	}
	
	public void bnaModelChanged(BNAModelEvent evt){
		int evtType = evt.getEventType();
		if(evtType == BNAModelEvent.THING_ADDED){
			Thing targetThing = evt.getTargetThing();
			if(targetThing instanceof IMoveTogether){
				IMoveTogether mtt = (IMoveTogether)targetThing;
				String movedThingId = mtt.getMoveTogetherThingId();
				if(movedThingId != null){
					addThingMoved(mtt, movedThingId);
				}
				else{
					removeThingMoved(mtt, movedThingId);
				}
				//updateIndicator(it);
			}
		}
		else if(evtType == BNAModelEvent.THING_CHANGED){
			Thing targetThing = evt.getTargetThing();
			if(targetThing instanceof IMoveTogether){
				ThingEvent tevt = evt.getThingEvent();
				String propertyName = tevt.getPropertyName();
				if(propertyName.equals(IMoveTogether.MOVE_TOGETHER_THING_ID_PROPERTY_NAME)){
					IMoveTogether mtt = (IMoveTogether)targetThing;
					String movedThingId = mtt.getMoveTogetherThingId();
					if(movedThingId != null){
						String oldmovedThingId = (String)evt.getThingEvent().getOldPropertyValue();
						if(oldmovedThingId != null){
							removeThingMoved(mtt, oldmovedThingId);
						}
						addThingMoved(mtt, movedThingId);
					}
					else{
						String oldmovedThingId = (String)evt.getThingEvent().getOldPropertyValue();
						removeThingMoved(mtt, oldmovedThingId);
					}
					//updateIndicator(it);
				}
			}
		}
		else if(evtType == BNAModelEvent.THING_REMOVING){
			Thing targetThing = evt.getTargetThing();
			if(targetThing == null) return;
			if(targetThing instanceof IMoveTogether){
				IMoveTogether mtt = (IMoveTogether)targetThing;
				String movedThingId = mtt.getMoveTogetherThingId();
				if(movedThingId != null){
					removeThingMoved(mtt, movedThingId);
				}
			}
			if(targetThing instanceof IBoxBounded){
				String targetThingId = targetThing.getID();
				String[] movedThingIds = getMoveTogetherThingIds(targetThingId);
				for(int i = 0; i < movedThingIds.length; i++){
					BNAComponent c = getBNAComponent();
					if(c != null){
						BNAModel m = c.getModel();
						if(m != null){
							IMoveTogether moveTogetherThing = (IMoveTogether)m.getThing(movedThingIds[i]);
							moveTogetherThing.setMoveTogetherThingId(null);
						}
					}
				}
			}
		}
	}	
	
	public void boundingBoxChanged(BoundingBoxChangedEvent evt){
		String targetThingId = evt.getTargetThing().getID();
		if(targetThingId != null){
			String[] moveTogetherThingIds = getMoveTogetherThingIds(targetThingId);
			for(int i = 0; i < moveTogetherThingIds.length; i++){
				BNAComponent c = getBNAComponent();
				if(c != null){
					BNAModel m = c.getModel();
					if(m != null){
						IMoveTogether moveTogetherThing = (IMoveTogether)m.getThing(moveTogetherThingIds[i]);
						if(moveTogetherThing != null){
							int moveTogetherMode = moveTogetherThing.getMoveTogetherMode();
							if(moveTogetherMode == IMoveTogether.MOVE_TOGETHER_TRACK_ANCHOR_POINT_ONLY){
								//Don't track bounding box.
								return;
							}
							else if(moveTogetherMode == IMoveTogether.MOVE_TOGETHER_TRACK_ANCHOR_POINT_FIRST){
								//If the moving thing has an anchor point, ignore the bounding box change.
								Thing movingThing = m.getThing(targetThingId);
								if(movingThing instanceof IAnchored){
									return;
								}
							}
							
							Rectangle newBoundingBox = evt.getNewBoundingBox();
							Rectangle oldBoundingBox = evt.getOldBoundingBox();
							if((oldBoundingBox != null) && (newBoundingBox != null)){
								int dx = newBoundingBox.x - oldBoundingBox.x;
								int dy = newBoundingBox.y - oldBoundingBox.y;
								//System.out.println("moveRelative: " + moveTogetherThing.getID() + "->" + dx +"," + dy);
								moveTogetherThing.moveRelative(dx, dy);
							}
						}
					}
				}
			}
		}
	}
		
	public void anchorPointChanged(AnchorPointChangedEvent evt){
		String targetThingId = evt.getTargetThing().getID();
		if(targetThingId != null){
			String[] moveTogetherThingIds = getMoveTogetherThingIds(targetThingId);
			for(int i = 0; i < moveTogetherThingIds.length; i++){
				BNAComponent c = getBNAComponent();
				if(c != null){
					BNAModel m = c.getModel();
					if(m != null){
						IMoveTogether moveTogetherThing = (IMoveTogether)m.getThing(moveTogetherThingIds[i]);
						if(moveTogetherThing != null){
							int moveTogetherMode = moveTogetherThing.getMoveTogetherMode();
							if(moveTogetherMode == IMoveTogether.MOVE_TOGETHER_TRACK_BOUNDING_BOX_ONLY){
								//Don't track anchor point.
								return;
							}
							else if(moveTogetherMode == IMoveTogether.MOVE_TOGETHER_TRACK_BOUNDING_BOX_FIRST){
								//If the moving thing has an bounding box, ignore the anchor point change.
								Thing movingThing = m.getThing(targetThingId);
								if(movingThing instanceof IBoxBounded){
									return;
								}
							}
							
							Point newAnchorPoint = evt.getNewAnchorPoint();
							Point oldAnchorPoint = evt.getOldAnchorPoint();
							if(oldAnchorPoint != null){
								int dx = newAnchorPoint.x - oldAnchorPoint.x;
								int dy = newAnchorPoint.y - oldAnchorPoint.y;
								//System.out.println("moveRelative: " + moveTogetherThing.getID() + "->" + dx +"," + dy);
								moveTogetherThing.moveRelative(dx, dy);
							}
						}
					}
				}
			}
		}
	}

}
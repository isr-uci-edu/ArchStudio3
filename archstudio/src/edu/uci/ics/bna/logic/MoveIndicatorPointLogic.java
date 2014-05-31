package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.BNAModel;
import edu.uci.ics.bna.BNAModelEvent;
import edu.uci.ics.bna.IBoxBounded;
import edu.uci.ics.bna.IIndicator;
import edu.uci.ics.bna.IReshapableSpline;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.ThingEvent;
import edu.uci.ics.bna.ThingLogicAdapter;
import edu.uci.ics.bna.logic.*;

public class MoveIndicatorPointLogic extends ThingLogicAdapter implements BoundingBoxTrackingListener{

	protected BoundingBoxTrackingLogic boundingBoxTrackingLogic;
	
	//Maps ids of indicated things to the id of the IIndicator pointing at them.
	protected Map indicatedThingMap = new HashMap();
	
	public MoveIndicatorPointLogic(BoundingBoxTrackingLogic boundingBoxTrackingLogic){
		this.boundingBoxTrackingLogic = boundingBoxTrackingLogic;
		boundingBoxTrackingLogic.addBoundingBoxTrackingListener(this);
	}

	public void init(){
		BNAModel m = getBNAComponent().getModel();
		if(m != null){
			Thing[] allThings = m.getAllThings();
			for(int i = 0; i < allThings.length; i++){
				if(allThings[i] instanceof IIndicator){
					String movedThingId = ((IIndicator)allThings[i]).getIndicatorThingId();
					if(movedThingId != null){
						addThingIndicated((IIndicator)allThings[i], movedThingId);
					}
				}
			}
		}
	}
	
	private void addThingIndicated(IIndicator it, String indicatedThingId){
		String[] indicatorThingIdArray = (String[])indicatedThingMap.get(indicatedThingId);
		if(indicatorThingIdArray == null){
			indicatedThingMap.put(indicatedThingId, new String[]{it.getID()});
			return;
		}
		HashSet s = new HashSet(Arrays.asList(indicatorThingIdArray));
		s.add(it.getID());
		indicatorThingIdArray = (String[])s.toArray(indicatorThingIdArray);
		indicatedThingMap.put(indicatedThingId, indicatorThingIdArray);
	}
	
	private void removeThingIndicated(IIndicator it, String indicatedThingId){
		String[] indicatorThingIdArray = (String[])indicatedThingMap.get(indicatedThingId);
		if(indicatorThingIdArray == null){
			return;
		}
		HashSet s = new HashSet(Arrays.asList(indicatorThingIdArray));
		s.remove(it.getID());
		if(s.isEmpty()){
			indicatedThingMap.remove(indicatedThingId);
		}
		else{
			indicatorThingIdArray = (String[])s.toArray(indicatorThingIdArray);
			indicatedThingMap.put(indicatedThingId, indicatorThingIdArray);
		}
	}

	final String[] emptyStringArray = new String[0];
		
	private String[] getIndicatorThingIds(String indicatedThingId){
		String[] arr = (String[])indicatedThingMap.get(indicatedThingId);
		return (arr == null) ? emptyStringArray : arr;  
	}
	
	public void bnaModelChanged(BNAModelEvent evt){
		int evtType = evt.getEventType();
		if(evtType == BNAModelEvent.THING_ADDED){
			Thing targetThing = evt.getTargetThing();
			if(targetThing instanceof IIndicator){
				IIndicator it = (IIndicator)targetThing;
				String indicatedThingId = it.getIndicatorThingId();
				if(indicatedThingId != null){
					addThingIndicated(it, indicatedThingId);
				}
				else{
					removeThingIndicated(it, indicatedThingId);
				}
				updateIndicator(it);
			}
		}
		else if(evtType == BNAModelEvent.THING_CHANGED){
			Thing targetThing = evt.getTargetThing();
			if(targetThing instanceof IIndicator){
				ThingEvent tevt = evt.getThingEvent();
				String propertyName = tevt.getPropertyName();
				if(propertyName.equals(IIndicator.INDICATOR_THING_ID_PROPERTY_NAME)){
					IIndicator it = (IIndicator)targetThing;
					String indicatedThingId = it.getIndicatorThingId();
					if(indicatedThingId != null){
						String oldIndicatedThingId = (String)evt.getThingEvent().getOldPropertyValue();
						if(oldIndicatedThingId != null){
							removeThingIndicated(it, oldIndicatedThingId);
						}
						addThingIndicated(it, indicatedThingId);
					}
					else{
						String oldIndicatedThingId = (String)evt.getThingEvent().getOldPropertyValue();
						removeThingIndicated(it, oldIndicatedThingId);
					}
					updateIndicator(it);
				}
			}
			if(targetThing instanceof IReshapableSpline){
				String[] indicatorThingIds = getIndicatorThingIds(targetThing.getID());
				for(int i = 0; i < indicatorThingIds.length; i++){
					Thing it = evt.getSource().getThing(indicatorThingIds[i]);
					if(it != null){
						ThingEvent tevt = evt.getThingEvent();
						String propertyName = tevt.getPropertyName();
						if(propertyName.startsWith("point")){
							updateIndicator((IIndicator)it);
						}
					}
				}
			}
		}
		else if(evtType == BNAModelEvent.THING_REMOVING){
			Thing targetThing = evt.getTargetThing();
			if(targetThing == null) return;
			if(targetThing instanceof IIndicator){
				IIndicator it = (IIndicator)targetThing;
				String indicatedThingId = it.getIndicatorThingId();
				if(indicatedThingId != null){
					removeThingIndicated(it, indicatedThingId);
				}
			}
			if(targetThing instanceof IBoxBounded){
				String targetThingId = targetThing.getID();
				String[] indicatorThingIds = getIndicatorThingIds(targetThingId);
				for(int i = 0; i < indicatorThingIds.length; i++){
					BNAComponent c = getBNAComponent();
					if(c != null){
						BNAModel m = c.getModel();
						if(m != null){
							IIndicator indicatorThing = (IIndicator)m.getThing(indicatorThingIds[i]);
							indicatorThing.setIndicatorThingId(null);
							indicatorThing.setIndicatorPoint(null);
						}
					}
				}
			}
		}
	}	
	
	public void updateIndicator(IIndicator t){
		String indicatedThingId = t.getIndicatorThingId();
		if(indicatedThingId != null){
			BNAComponent c = getBNAComponent();
			if(c == null) return;
			BNAModel m = c.getModel();
			if(m == null) return;
			
			Thing indicatedThing = m.getThing(indicatedThingId);
			if(indicatedThing != null){
				if(indicatedThing instanceof IReshapableSpline){
					IReshapableSpline rst = (IReshapableSpline)indicatedThing;
					Point[] points = rst.getAllPoints();
					if(points.length == 0){
						t.setIndicatorPoint(null);
					}
					else if(points.length == 1){
						t.setIndicatorPoint(points[0]);
					}
					else if(points.length == 2){
						Point p1 = points[0];
						Point p2 = points[1];
						
						int dx = p2.x - p1.x;
						if(dx < 0) dx = -dx;
						dx /= 2;
						
						int dy = p2.y - p1.y;
						if(dy < 0) dy = -dy;
						dy /= 2;
						
						int x, y;
						if(p1.x < p2.x){
							x = p1.x + dx;
						}
						else{
							x = p2.x + dx;
						}
						
						if(p1.y < p2.y){
							y = p1.y + dy;
						}
						else{
							y = p2.y + dy;
						}
						
						t.setIndicatorPoint(new Point(x,y));
					}
					else{ //points.length > 2
						t.setIndicatorPoint(points[points.length / 2]);
					}
				}
				else if(indicatedThing instanceof IBoxBounded){
					IBoxBounded bbt = (IBoxBounded)indicatedThing;
					//System.err.println("indicatedThing=" + bbt);
					Rectangle r = bbt.getBoundingBox();
					int mx = r.x + (r.width / 2);
					int my = r.y + (r.height / 2);
					//System.err.println("Setting indicator point to " + r.x + "," + r.y);
					t.setIndicatorPoint(new Point(mx, my));
				}
			}
		}
	}
	
	public void boundingBoxChanged(BoundingBoxChangedEvent evt){
		String targetThingId = evt.getTargetThing().getID();
		if(targetThingId != null){
			String[] indicatorThingIds = getIndicatorThingIds(targetThingId);
			for(int i = 0; i < indicatorThingIds.length; i++){
				BNAComponent c = getBNAComponent();
				if(c != null){
					BNAModel m = c.getModel();
					if(m != null){
						IIndicator indicatorThing = (IIndicator)m.getThing(indicatorThingIds[i]);
						if(indicatorThing != null){
							updateIndicator(indicatorThing);
						}
					}
				}
			}
		}
	}
		

}
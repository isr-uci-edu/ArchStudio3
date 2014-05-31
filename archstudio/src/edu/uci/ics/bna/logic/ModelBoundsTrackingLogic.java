package edu.uci.ics.bna.logic;

import edu.uci.ics.bna.*;

import java.awt.Rectangle;
import java.util.*;

import c2.util.DelayedExecuteOnceThread;

public class ModelBoundsTrackingLogic extends ThingLogicAdapter implements BoundingBoxTrackingListener{
	public static final String DONT_USE_IN_MODEL_BOUNDS_COMPUTATION_PROPERTY_NAME = "#dontuseinmodelboundscomputation";

	protected BoundingBoxTrackingLogic bbtl;
	protected Rectangle modelBounds = null;
	protected ShrinkUpdateThread updateThread = null;
	
	public ModelBoundsTrackingLogic(BoundingBoxTrackingLogic bbtl){
		super();
		this.bbtl = bbtl;
	}
	
	public void init(){
		bbtl.addBoundingBoxTrackingListener(this);
		updateThread = new ShrinkUpdateThread();
		updateThread.start();
		recompute();
	}
	
	public void destroy(){
		if(updateThread != null){
			updateThread.terminate();
		}
	}
	
	protected synchronized void recompute(){
		int westernBoundary = Integer.MAX_VALUE;
		int easternBoundary = Integer.MIN_VALUE;
		int northernBoundary = Integer.MAX_VALUE;
		int southernBoundary = Integer.MIN_VALUE;
		
		Rectangle oldModelBounds = modelBounds;
		Rectangle newModelBounds = new Rectangle();
		
		BNAComponent c = getBNAComponent();
		if(c != null){
			BNAModel m = c.getModel();
			if(m != null){
				for(Iterator it = m.getThingIterator(); it.hasNext(); ){
					Thing t = (Thing)it.next();
					if(t instanceof IBoxBounded){
						Rectangle r = ((IBoxBounded)t).getBoundingBox();
						if(r.equals(BNAUtils.NONEXISTENT_RECTANGLE)) continue;
						if(t.getProperty(DONT_USE_IN_MODEL_BOUNDS_COMPUTATION_PROPERTY_NAME) != null) continue;
						System.out.println("x = " + r.x + "; t = " + t);
						int x1 = r.x;
						int y1 = r.y;
						int x2 = r.x + r.width;
						int y2 = r.y + r.height;
						
						if(x1 <= westernBoundary){
							westernBoundary = x1;
						}
						
						if(x2 >= easternBoundary){
							easternBoundary = x2;
						}
						
						if(y1 <= northernBoundary){
							northernBoundary = y1;
						}

						if(y2 >= southernBoundary){
							southernBoundary = y2;
						}
					}
				}
				newModelBounds.x = westernBoundary;
				newModelBounds.y = northernBoundary;
				newModelBounds.width = easternBoundary - westernBoundary;
				newModelBounds.height = southernBoundary - northernBoundary;
				modelBounds = newModelBounds;
				fireModelBoundsChangedEvent(m, oldModelBounds, newModelBounds);
			}
		}
	}
	
	protected Vector listeners = new Vector();
	
	public void addModelBoundsChangedListener(ModelBoundsChangedListener l){
		listeners.add(l);
	}
	
	public void removeModelBoundsChangedListener(ModelBoundsChangedListener l){
		listeners.remove(l);
	}
	
	public Rectangle getModelBounds(){
		return new Rectangle(modelBounds);
	}
	
	protected void fireModelBoundsChangedEvent(BNAModel src, Rectangle oldModelBounds, Rectangle newModelBounds){
		if((oldModelBounds == null) && (newModelBounds == null)){
			return;
		}
		if((oldModelBounds != null) && (newModelBounds != null)){
			if(oldModelBounds.equals(newModelBounds)){
				return;
			}
		}
		ModelBoundsChangedListener[] listenerArray = 
			(ModelBoundsChangedListener[])listeners.toArray(new ModelBoundsChangedListener[listeners.size()]);
		for(int i = 0; i < listenerArray.length; i++){
			listenerArray[i].modelBoundsChanged(src, oldModelBounds, newModelBounds);
		}
	}

	class ShrinkUpdateThread extends DelayedExecuteOnceThread{
		public ShrinkUpdateThread(){
			super(2000);
		}
		
		public void doExecute(){
			recompute();
		}
	}
	
	public synchronized void boundingBoxChanged(BoundingBoxChangedEvent evt){
		Thing t = evt.getTargetThing();
		if(t == null) return;
		
		Rectangle newBoundingBox = evt.getNewBoundingBox();
		if(newBoundingBox != null){
			if(newBoundingBox.equals(BNAUtils.NONEXISTENT_RECTANGLE)) return;
			if(t.getProperty(DONT_USE_IN_MODEL_BOUNDS_COMPUTATION_PROPERTY_NAME) != null) return;

			Rectangle oldModelBounds = modelBounds;
			int ox1 = modelBounds.x;
			int oy1 = modelBounds.y;
			int ox2 = modelBounds.x + modelBounds.width;
			int oy2 = modelBounds.y + modelBounds.height;
			
			boolean modelBoundsExpanded = false;
			int nx1 = ox1;
			int ny1 = oy1;
			int nx2 = ox2;
			int ny2 = oy2;
			
			if(newBoundingBox.x < ox1){
				modelBoundsExpanded = true;
				nx1 = newBoundingBox.x;
			}
			if(newBoundingBox.y < oy1){
				modelBoundsExpanded = true;
				ny1 = newBoundingBox.y;
			}
			if((newBoundingBox.x + newBoundingBox.width) > ox2){
				modelBoundsExpanded = true;
				nx2 = newBoundingBox.x + newBoundingBox.width;
			}
			if((newBoundingBox.y + newBoundingBox.height) > oy2){
				modelBoundsExpanded = true;
				ny2 = newBoundingBox.y + newBoundingBox.height;
			}
			
			if(modelBoundsExpanded){
				Rectangle newModelBounds = new Rectangle(nx1, ny1, nx2 - nx1, ny2 - ny1);
				modelBounds = newModelBounds;
				fireModelBoundsChangedEvent(getBNAComponent().getModel(), oldModelBounds, newModelBounds);
			}
		}
		updateThread.execute();
	}
	
}

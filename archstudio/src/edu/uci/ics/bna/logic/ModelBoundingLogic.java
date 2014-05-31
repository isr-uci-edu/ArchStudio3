package edu.uci.ics.bna.logic;

import java.awt.Rectangle;

import edu.uci.ics.bna.*;

public class ModelBoundingLogic extends ThingLogicAdapter implements ModelBoundsChangedListener{

	protected IResizableBoxBounded modelBoundingThing = null;
	protected ModelBoundsTrackingLogic mbtl;
	protected int margin = 0;
	
	public ModelBoundingLogic(ModelBoundsTrackingLogic mbtl){
		super();
		this.mbtl = mbtl;
	}
	
	public void init(){
		mbtl.addModelBoundsChangedListener(this);
		update();
	}
	
	public void destroy(){
		super.destroy();
		mbtl.removeModelBoundsChangedListener(this);
	}
	
	public void setMargin(int margin){
		this.margin = margin;
		update();
	}
	
	public void setModelBoundingThing(IResizableBoxBounded modelBoundingThing){
		if(this.modelBoundingThing != null){
			this.modelBoundingThing.removeProperty(ModelBoundsTrackingLogic.DONT_USE_IN_MODEL_BOUNDS_COMPUTATION_PROPERTY_NAME);
		}
		this.modelBoundingThing = modelBoundingThing;
		this.modelBoundingThing.setProperty(ModelBoundsTrackingLogic.DONT_USE_IN_MODEL_BOUNDS_COMPUTATION_PROPERTY_NAME, true);
		update();
	}
	
	public IResizableBoxBounded getModelBoundingThing(){
		return modelBoundingThing;
	}
	
	public void update(){
		if(getBNAComponent() == null) return;
		if(modelBoundingThing != null){
			Rectangle r = mbtl.getModelBounds();
			Rectangle b = new Rectangle(r);
			b.x -= margin;
			b.y -= margin;
			b.width += margin + margin;
			b.height += margin + margin;
			modelBoundingThing.setBoundingBox(b);
		}
	}
	
	public void modelBoundsChanged(BNAModel src, Rectangle oldBounds, Rectangle newBounds){
		update();
	}
	
	public void bnaModelChanged(BNAModelEvent evt){
		if(evt.getEventType() == BNAModelEvent.THING_REMOVING){
			if((evt.getTargetThing() != null) && (evt.getTargetThing() == modelBoundingThing)){
				setModelBoundingThing(null);
			}
		}
	}

}

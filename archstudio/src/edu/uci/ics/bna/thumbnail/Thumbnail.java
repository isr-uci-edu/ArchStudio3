package edu.uci.ics.bna.thumbnail;

import edu.uci.ics.bna.*;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

public class Thumbnail implements BNAModelListener{

	protected BNAModel model;
	protected PeerCache peerCache;
	protected DefaultCoordinateMapper cm;

	protected Rectangle modelBounds;
	protected boolean boundsNeedUpdate = true;

	public Thumbnail(BNAModel model){
		this(null, model);
	}
	
	public Thumbnail(BNAComponent referenceComponent, BNAModel model){
		this.model = model;
		model.addBNAModelListener(this);
		
		BNAComponent c = new BNAComponent("thumbnail", model);
		if(referenceComponent != null){
			c.setAntialiasGraphics(referenceComponent.shouldAntialiasGraphics());
			c.setAntialiasText(referenceComponent.shouldAntialiasText());
			c.setGradientGraphics(referenceComponent.shouldGradientGraphics());
			Map properties = referenceComponent.getProperties();
			for(Iterator it = properties.keySet().iterator(); it.hasNext(); ){
				String propName = (String) it.next();
				Object propValue = properties.get(propName);
				c.setProperty(propName, propValue);
			}
		}
		
		this.peerCache = new PeerCache(c);
		this.cm = new DefaultCoordinateMapper();
	}
	
	public void bnaModelChanged(BNAModelEvent evt){
		boundsNeedUpdate = true;
	}

	public CoordinateMapper getCoordinateMapper(){
		return cm;
	}
	
	public BNAModel getModel(){
		return model;
	}
	
	public Rectangle getModelBounds(){
		checkAndRecalcBounds();
		return new Rectangle(modelBounds);
	}
	
	public void setPeerCache(PeerCache peerCache){
		this.peerCache = peerCache;
	}
	
	public PeerCache getPeerCache(){
		return peerCache;
	}

	protected synchronized void checkAndRecalcBounds(){
		if(!boundsNeedUpdate) return;
		Rectangle newBounds = null;
		Thing[] things = model.getAllThings();
		for(int i = 0; i < things.length; i++){
			if(things[i] instanceof IBoxBounded){
				IBoxBounded bbt = (IBoxBounded)things[i];
				Rectangle boundingBox = bbt.getBoundingBox();
				if(boundingBox != null){
					if(newBounds == null){
						newBounds = boundingBox;
					}
					else{
						newBounds = newBounds.union(boundingBox);
					}
				}
			}
		}
		modelBounds = newBounds;
		modelBounds.width++;
		modelBounds.height++;
		boundsNeedUpdate = false;
	}

	public Rectangle getThumbnailWorldBounds(){
		checkAndRecalcBounds();
		return new Rectangle(modelBounds);
	}

	public void drawThumbnail(Graphics2D g2d, Rectangle bounds){
		drawThumbnail(g2d, bounds.x, bounds.y, bounds.width, bounds.height);
	}
	
	public Dimension getThumbnailDimensions(int gx, int gy, int gWidth, int gHeight){
		checkAndRecalcBounds();
		
		double xScale = (double)gWidth / (double)modelBounds.width;
		double yScale = (double)gHeight / (double)modelBounds.height;
		
		//Now, we need to set the scale proportionally so the thumb fits in the
		//gWidth x gHeight rectangle, so we pick the minimum of the xScale and
		//the yScale
		
		double scale = Math.min(xScale, yScale);
		
		Dimension d = new Dimension();
		d.width = BNAUtils.round((double)modelBounds.width * scale);
		d.height = BNAUtils.round((double)modelBounds.height * scale);
		return d; 
	}
	
	public void setupCoordinateMapper(int gX, int gY, int gWidth, int gHeight){
		checkAndRecalcBounds();
		
		//modelBounds now has the min/max world coordinates for the thumbed model
		double xScale = (double)gWidth / (double)modelBounds.width;
		double yScale = (double)gHeight / (double)modelBounds.height;
		
		//Now, we need to set the scale proportionally so the thumb fits in the
		//gWidth x gHeight rectangle, so we pick the minimum of the xScale and
		//the yScale
		
		double scale = Math.min(xScale, yScale);
		
		//Now that we've got the scale right, presumably we can just set the
		//originWorldX and originWorldY of the internal coordinate mapper to
		//whatever the min world x and world y of the model are, and the fact
		//that the scale is set correctly should cause the drawing to make sure
		//we don't blow past the bounds on the right.
		
		cm.rescaleAbsolute(scale);
		cm.repositionAbsolute(modelBounds.x, modelBounds.y);
	}
	
	public void drawThumbnail(Graphics2D g2d, int gX, int gY, int gWidth, int gHeight){
		if((gWidth < 5) && (gHeight < 5)){
			//This prevents us from drawing thumbnails that are too damn
			//small and also prevents infinite thumbnail recursion
			return;
		}

		Graphics2D g = (Graphics2D)g2d.create(gX, gY, gWidth, gHeight);
		setupCoordinateMapper(gX, gY, gWidth, gHeight);
		
		//Now the coordinate mapper is configured, we just have to ask
		//the peers to draw.
		synchronized(model.getLock()){
			for(Iterator it = model.getThingIterator(); it.hasNext(); ){
				Thing th = (Thing)it.next();
				ThingPeer peer = peerCache.getPeer(th);
				peer.draw(g, cm);
			}
		}
	}
	
}

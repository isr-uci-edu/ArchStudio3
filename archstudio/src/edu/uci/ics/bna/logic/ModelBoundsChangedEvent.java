package edu.uci.ics.bna.logic;

import edu.uci.ics.bna.*;

import java.awt.Rectangle;

public class ModelBoundsChangedEvent{
	protected BNAModel src;
	protected Rectangle oldBounds;
	protected Rectangle newBounds;
	
	public ModelBoundsChangedEvent(BNAModel src, Rectangle oldBounds, Rectangle newBounds){
		super();
		this.src = src;
		this.oldBounds = oldBounds;
		this.newBounds = newBounds;
	}

	public Rectangle getNewBounds(){
		return newBounds;
	}
	
	public void setNewBounds(Rectangle newBounds){
		this.newBounds = newBounds;
	}
	
	public Rectangle getOldBounds(){
		return oldBounds;
	}
	
	public void setOldBounds(Rectangle oldBounds){
		this.oldBounds = oldBounds;
	}
	
	public BNAModel getSource(){
		return src;
	}
	
	public void setSource(BNAModel src){
		this.src = src;
	}

}

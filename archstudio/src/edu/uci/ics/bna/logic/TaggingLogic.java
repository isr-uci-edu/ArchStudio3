package edu.uci.ics.bna.logic;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.BNAModel;
import edu.uci.ics.bna.BNAModelEvent;
import edu.uci.ics.bna.BNAUtils;
import edu.uci.ics.bna.DefaultCoordinateMapper;
import edu.uci.ics.bna.IAnchored;
import edu.uci.ics.bna.IBoxBounded;
import edu.uci.ics.bna.IMoveTogether;
//import edu.uci.ics.bna.ITaggable;
import edu.uci.ics.bna.TagThing;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.ThingLogicAdapter;

public class TaggingLogic extends ThingLogicAdapter{

	protected TagThingTrackingLogic tagTracker = null;
	
	public TaggingLogic(TagThingTrackingLogic tagTracker){
		super();
		this.tagTracker = tagTracker;
	}
	
	protected boolean initialized = false;

	public TagThing findTagForThing(Thing taggedThing){
		String taggedThingId = taggedThing.getID();
		Thing[] tagThings = tagTracker.getTrackedThings();
		for(int i = 0; i < tagThings.length; i++){
			TagThing tagThing = (TagThing)tagThings[i];
			String indicatedThingId = tagThing.getTaggedThingId();
			if((indicatedThingId != null) && (indicatedThingId.equals(taggedThingId))){
				return tagThing;
			}
		}
		return null;
	}
	
	public static TagThing createTag(Thing t, String tagText){
		TagThing tag = new TagThing();
		
		Point tagAnchorPoint = new Point();
		if(t instanceof IAnchored){
			Point tAnchorPoint = ((IAnchored)t).getAnchorPoint();
			tagAnchorPoint.x = tAnchorPoint.x + 10;
			tagAnchorPoint.y = tAnchorPoint.y - 10;
			tag.setMoveTogetherMode(IMoveTogether.MOVE_TOGETHER_TRACK_ANCHOR_POINT_ONLY);
		}
		else if(t instanceof IBoxBounded){
			Rectangle tBoundingBox = ((IBoxBounded)t).getBoundingBox();
			tagAnchorPoint.x = tBoundingBox.x + (tBoundingBox.width / 2) + 10;
			tagAnchorPoint.y = tBoundingBox.y - 10;
			tag.setMoveTogetherMode(IMoveTogether.MOVE_TOGETHER_TRACK_BOUNDING_BOX_ONLY);
		}
		else{
			//God knows where to put it...
			tagAnchorPoint.x = DefaultCoordinateMapper.DEFAULT_WORLD_WIDTH / 2;
			tagAnchorPoint.y = DefaultCoordinateMapper.DEFAULT_WORLD_HEIGHT / 2;
		}
		tag.setTaggedThingId(t.getID());
		tag.setText(tagText);
		
		tag.setAnchorPoint(tagAnchorPoint);
		return tag;
	}
	
	public synchronized void bnaModelChanged(BNAModelEvent evt){
		if(evt.getEventType() == BNAModelEvent.THING_REMOVING){
			Thing targetThing = evt.getTargetThing();
			TagThing tag = findTagForThing(targetThing);
			getBNAComponent().getModel().removeThing(tag);
		}
	}
}
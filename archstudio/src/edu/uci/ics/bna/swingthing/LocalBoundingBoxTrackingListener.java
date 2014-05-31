package edu.uci.ics.bna.swingthing;

import edu.uci.ics.bna.logic.BoundingBoxChangedEvent;

public interface LocalBoundingBoxTrackingListener{
	public void localBoundingBoxChanged(LocalBoundingBoxChangedEvent evt);
}

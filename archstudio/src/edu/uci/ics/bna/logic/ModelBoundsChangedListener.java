package edu.uci.ics.bna.logic;

import java.awt.Rectangle;
import edu.uci.ics.bna.BNAModel;

public interface ModelBoundsChangedListener{
	public void modelBoundsChanged(BNAModel src, Rectangle oldBounds, Rectangle newBounds);
}

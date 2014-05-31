package edu.uci.ics.bna.swingthing;

import edu.uci.ics.bna.Thing;

public interface ILocalDragMovable extends Thing{

	public void localMoveRelative(int dx, int dy);
	
}

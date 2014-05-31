package edu.uci.ics.bna.logic;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.Thing;

public interface DropHandler{

	public void handleDrop(String sourceBNAComponentID, BNAComponent dropTarget, Thing t, int worldX, int worldY);
	

}

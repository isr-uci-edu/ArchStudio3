package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.IToolTip;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.ThingLogicAdapter;

public class ToolTipLogic extends ThingLogicAdapter{

	public void mouseMoved(Thing t, MouseEvent evt, int worldX, int worldY){
		BNAComponent c = bnaComponent;
		String ttt = null;
		if((t != null) && (t instanceof IToolTip)){
			ttt = ((IToolTip)t).getToolTipText();
		}
		c.setToolTipText(ttt);
	}
	
}

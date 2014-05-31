package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.ICustomCursor;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.ThingLogicAdapter;

public class CustomCursorLogic extends ThingLogicAdapter{

	public static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
	protected Cursor lastCursor = DEFAULT_CURSOR;

	public void mouseMoved(Thing t, MouseEvent evt, int worldX, int worldY){
		BNAComponent c = bnaComponent;
		Cursor newCursor = DEFAULT_CURSOR;
		if((t != null) && (t instanceof ICustomCursor)){
			int cursorType = ((ICustomCursor)t).getCursorType();
			newCursor = Cursor.getPredefinedCursor(cursorType);
		}
		Cursor currentCursor = c.getCursor();
		if(currentCursor.getType() != newCursor.getType()){
			c.setCursor(newCursor);
		}
	}
	
}

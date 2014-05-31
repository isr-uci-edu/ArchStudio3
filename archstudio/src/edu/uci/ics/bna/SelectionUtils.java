package edu.uci.ics.bna;

import java.util.*;

public class SelectionUtils{
	public static synchronized void removeAllSelections(BNAComponent c){
		removeAllSelections(c.getModel());
	}	

	public static synchronized void removeAllSelections(BNAModel m){
		for(Iterator it = m.getThingIterator(); it.hasNext(); ){
			Thing t2 = (Thing)it.next();
			if(t2 instanceof ISelectable){
				synchronized(t2){
					((ISelectable)t2).setSelected(false);
				}
			}
		}
	}	
}

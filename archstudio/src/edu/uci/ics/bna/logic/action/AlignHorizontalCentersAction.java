package edu.uci.ics.bna.logic.action;

import java.util.ArrayList;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.logic.SelectionTrackingLogic;

public class AlignHorizontalCentersAction extends AbstractThingLogicAction {

	protected SelectionTrackingLogic stl = null;

	public AlignHorizontalCentersAction(SelectionTrackingLogic stl){
		super();
		this.stl = stl;
	}

	public void invoke(){
		ArrayList list = new ArrayList();
		Thing[] selectedThings = stl.getSelectedThings();
		for(int i = 0; i < selectedThings.length; i++){
			if(selectedThings[i] instanceof BoxThing){
				list.add(selectedThings[i]);
			}
		}
		Thing[] alignableThings = (Thing[])list.toArray(new Thing[0]);
		if(alignableThings.length > 0){
			AlignUtils.alignHorizontalCenters(alignableThings);
		}
	}

}

package edu.uci.ics.bna.logic.action;

import java.util.ArrayList;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.logic.SelectionTrackingLogic;

public class DistributeHorizontallyTightAction extends AbstractThingLogicAction {

	protected SelectionTrackingLogic stl = null;

	public DistributeHorizontallyTightAction(SelectionTrackingLogic stl){
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
		Thing[] distributableThings = (Thing[])list.toArray(new Thing[0]);
		if(distributableThings.length > 0){
			DistributeUtils.distributeHorizontallyTight(distributableThings);
		}
	}

}

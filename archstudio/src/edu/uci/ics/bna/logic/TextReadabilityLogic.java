package edu.uci.ics.bna.logic;

import java.awt.Color;

import edu.uci.ics.bna.*;
import edu.uci.ics.widgets.WidgetUtils;

public class TextReadabilityLogic extends ThingLogicAdapter{

	public void init(){
		BNAModel m = getBNAComponent().getModel();
		if(m != null){
			Thing[] allThings = m.getAllThings();
			for(int i = 0; i < allThings.length; i++){
				checkTextColor(allThings[i]);
			}
		}
	}

	public void bnaModelChanged(BNAModelEvent evt){
		Thing src = evt.getTargetThing();
		if(src != null){
			checkTextColor(src);
		}
	}
	
	public void checkTextColor(Thing t){
		if((t instanceof IColored) && (t instanceof ITextColored)){
			Color c = ((IColored)t).getColor();
			if(WidgetUtils.isDark(c)){
				((ITextColored)t).setTextColor(Color.WHITE);
			}
			else{
				((ITextColored)t).setTextColor(Color.BLACK);
			}
		}
	}
}

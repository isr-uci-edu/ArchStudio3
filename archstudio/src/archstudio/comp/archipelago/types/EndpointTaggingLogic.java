package archstudio.comp.archipelago.types;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.logic.*;

public class EndpointTaggingLogic extends TaggingLogic{

	public EndpointTaggingLogic(TagThingTrackingLogic tagTracker){
		super(tagTracker);
	}
	
	public static TagThing createTag(Thing t, String tagText){
		TagThing originalTag = TaggingLogic.createTag(t, tagText);
		EndpointTagThing endpointTag = new EndpointTagThing(originalTag);
		return endpointTag;
	}
	
	public synchronized void bnaModelChanged(BNAModelEvent evt){
		super.bnaModelChanged(evt);
		if(evt.getEventType() == BNAModelEvent.THING_CHANGED){
			Thing targetThing = evt.getTargetThing();
			if((targetThing != null) && (targetThing instanceof EndpointThing)){
				TagThing tt = findTagForThing(targetThing);
				if(tt != null){
					String newText = ((EndpointThing)targetThing).getToolTipText();
					String oldText = tt.getText();
					if(!BNAUtils.objNullEq(oldText, newText)){
						tt.setText(((EndpointThing)targetThing).getToolTipText());
						BNAComponent c = getBNAComponent();
						if(c != null) c.repaint();
					}
				}
			}
		}
	}

}

package archstudio.comp.archipelago.types;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

import archstudio.comp.archipelago.*;
import archstudio.editors.*;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.logic.*;
import edu.uci.ics.xarchutils.*;

public class ElementIndicatorLogic extends ThingLogicAdapter implements c2.fw.MessageProcessor{

	protected ThingIDMap thingIDMap;
	
	public ElementIndicatorLogic(ThingIDMap thingIDMap){
		this.thingIDMap = thingIDMap;
	}

	public void init(){
		init(getBNAComponent().getModel());
	}
	
	public void init(BNAModel m){
	}
	
	/*
	public void destroy(){
		getBNAComponent().getCoordinateMapper().removeCoordinateMapperListener(this);
	}
	*/
	
	protected void removeIndications(Thing t){
		if(t instanceof IColored){
			Color oldColor = (Color)t.getProperty("$$oldColor");
			if(oldColor != null){
				t.removeProperty("$$oldColor");
				((IColored)t).setColor(oldColor);
			}
		}
		if(t instanceof IStroked){
			Stroke oldStroke = (Stroke)t.getProperty("$$oldStroke");
			if(oldStroke != null){
				t.removeProperty("$$oldStroke");
				((IStroked)t).setStroke(oldStroke);
			}
		}
		if(t instanceof IDirectional){
			try{
				int oldDirection = t.getIntProperty("$$oldDirection");
				t.removeProperty("$$oldDirection");
				((IDirectional)t).setDirection(oldDirection);
			}
			catch(Exception e){}
		}
	}
	
	public void handle(c2.fw.Message m){
		BNAComponent bnaComponent = super.getBNAComponent();
		if(bnaComponent == null){
			return;
		}
		BNAModel bnaModel = bnaComponent.getModel();
		if(m instanceof IndicateElementsMessage){
			IndicateElementsMessage iem = (IndicateElementsMessage)m;
			ObjRef[] elementRefs = iem.getElementRefs();
			for(int i = 0; i < elementRefs.length; i++){
				String thingID = thingIDMap.getThingID(elementRefs[i]);
				if(thingID != null){
					Thing t = bnaModel.getThing(thingID);
					if(t != null){
						Indication[] indications = iem.getIndications();
						for(int j = 0; j < indications.length; j++){
							Indication indication = indications[j];
							if(indication instanceof StrokeIndication){
								StrokeIndication si = (StrokeIndication)indication;
								Stroke s = si.getStroke();
								if(t instanceof IStroked){
									Stroke oldStroke = ((IStroked)t).getStroke();
									t.setProperty("$$oldStroke", oldStroke);
									((IStroked)t).setStroke(s);
								}
							}
							if(indication instanceof ColorIndication){
								ColorIndication ci = (ColorIndication)indication;
								Color c = ci.getColor();
								if(t instanceof IColored){
									Color oldColor = ((IColored)t).getColor();
									t.setProperty("$$oldColor", oldColor);
									((IColored)t).setColor(c);
								}
							}
							if(indication instanceof DirectionalIndication){
								DirectionalIndication di = (DirectionalIndication)indication;
								int d = di.getDirection();
								if(t instanceof IDirectional){
									int oldDirection = ((IDirectional)t).getDirection();
									t.setProperty("$$oldDirection", oldDirection);
									((IDirectional)t).setDirection(d);
								}
							}
						}
					}
				}
			}
		}
		else if(m instanceof UnindicateElementsMessage){
			UnindicateElementsMessage uem = (UnindicateElementsMessage)m;
			ObjRef[] refs = uem.getElementRefs();
			for(int i = 0; i < refs.length; i++){
				String thingID = thingIDMap.getThingID(refs[i]);
				if(thingID != null){
					Thing t = bnaModel.getThing(thingID);
					if(t != null){
						removeIndications(t);
					}
				}
			}
		}
	}

}
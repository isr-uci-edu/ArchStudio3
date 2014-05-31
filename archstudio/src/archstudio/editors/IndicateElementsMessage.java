package archstudio.editors;

import c2.fw.*;
import edu.uci.ics.xarchutils.ObjRef;

public class IndicateElementsMessage extends NamedPropertyMessage{

	public IndicateElementsMessage(ObjRef[] elementRefs, Indication indication){
		this(elementRefs, new Indication[]{indication});
	}

	public IndicateElementsMessage(ObjRef elementRef, Indication indication){
		this(new ObjRef[]{elementRef}, new Indication[]{indication});
	}

	public IndicateElementsMessage(ObjRef elementRef, Indication[] indications){
		this(new ObjRef[]{elementRef}, indications);
	}

	public IndicateElementsMessage(ObjRef[] elementRefs, Indication[] indications){
		super("IndicateElementsMessage");
		super.addParameter("elementRefs", elementRefs);
		super.addParameter("indications", indications);
	}

	protected IndicateElementsMessage(IndicateElementsMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new IndicateElementsMessage(this);
	}

	public void setElementRefs(ObjRef[] elementRefs){
		addParameter("elementRefs", elementRefs);
	}

	public ObjRef[] getElementRefs(){
		return (ObjRef[])getParameter("elementRefs");
	}

	public void setIndications(Indication[] indications){
		addParameter("indications", indications);
	}

	public Indication[] getIndications(){
		return (Indication[])getParameter("indications");
	}

}


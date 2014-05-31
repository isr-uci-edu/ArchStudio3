package archstudio.editors;

import c2.fw.*;
import edu.uci.ics.xarchutils.ObjRef;

public class UnindicateElementsMessage extends NamedPropertyMessage{

	public UnindicateElementsMessage(ObjRef[] elementRefs){
		super("UnndicateElementsMessage");
		super.addParameter("elementRefs", elementRefs);
	}

	public UnindicateElementsMessage(ObjRef elementRef){
		this(new ObjRef[]{elementRef});
	}

	protected UnindicateElementsMessage(UnindicateElementsMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new UnindicateElementsMessage(this);
	}

	public void setElementRefs(ObjRef[] elementRefs){
		addParameter("elementRefs", elementRefs);
	}

	public ObjRef[] getElementRefs(){
		return (ObjRef[])getParameter("elementRefs");
	}

}


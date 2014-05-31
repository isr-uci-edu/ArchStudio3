package archstudio.comp.xarchtrans;

import c2.fw.*;
import edu.uci.ics.xarchutils.*;

public class XArchTransactionEvent extends NamedPropertyMessage{
	
	public XArchTransactionEvent(XArchFlatEvent[] events){
		super("XArchTransactionEvent");
		super.addParameter("events", events);
	}
	
	protected XArchTransactionEvent(XArchTransactionEvent copyMe){
		super(copyMe);
	}
	
	public Message duplicate(){
		return new XArchTransactionEvent(this);
	}

	public XArchFlatEvent[] getEvents(){
		return (XArchFlatEvent[])getParameter("events");
	}
	
}
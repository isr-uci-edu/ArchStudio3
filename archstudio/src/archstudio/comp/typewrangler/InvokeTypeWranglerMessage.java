package archstudio.comp.typewrangler;

import c2.fw.*;
import edu.uci.ics.xarchutils.ObjRef;

public class InvokeTypeWranglerMessage extends NamedPropertyMessage{
	public InvokeTypeWranglerMessage(String url, ObjRef elementRef){
		super("InvokeTypeWranglerMessage");
		super.addParameter("url", url);
		super.addParameter("elementRef", elementRef);
	}

	protected InvokeTypeWranglerMessage(InvokeTypeWranglerMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new InvokeTypeWranglerMessage(this);
	}

	public void setUrl(String url){
		addParameter("url", url);
	}

	public String getUrl(){
		return (String)getParameter("url");
	}

	public void setElementRef(ObjRef elementRef){
		addParameter("elementRef", elementRef);
	}

	public ObjRef getElementRef(){
		return (ObjRef)getParameter("elementRef");
	}

}


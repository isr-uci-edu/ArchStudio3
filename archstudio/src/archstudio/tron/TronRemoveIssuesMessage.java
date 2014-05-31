package archstudio.tron;

import c2.fw.*;
import edu.uci.ics.xarchutils.ObjRef;

public class TronRemoveIssuesMessage extends NamedPropertyMessage{
	public TronRemoveIssuesMessage(ObjRef documentRef){
		super("TronRemoveIssuesMessage");
		super.addParameter("documentRef", documentRef);
	}

	protected TronRemoveIssuesMessage(TronRemoveIssuesMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new TronRemoveIssuesMessage(this);
	}

	public void setDocumentRef(ObjRef documentRef){
		addParameter("documentRef", documentRef);
	}

	public ObjRef getDocumentRef(){
		return (ObjRef)getParameter("documentRef");
	}

}


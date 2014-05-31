package archstudio.tron;

import c2.fw.*;
import edu.uci.ics.xarchutils.ObjRef;

public class TronRunTestsMessage extends NamedPropertyMessage{
	public TronRunTestsMessage(ObjRef documentRef, String[] testUIDs){
		super("TronRunTestsMessage");
		super.addParameter("documentRef", documentRef);
		super.addParameter("testUIDs", testUIDs);
	}

	protected TronRunTestsMessage(TronRunTestsMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new TronRunTestsMessage(this);
	}

	public void setDocumentRef(ObjRef documentRef){
		addParameter("documentRef", documentRef);
	}

	public ObjRef getDocumentRef(){
		return (ObjRef)getParameter("documentRef");
	}

	public void setTestUIDs(String[] testUIDs){
		addParameter("testUIDs", testUIDs);
	}

	public String[] getTestUIDs(){
		return (String[])getParameter("testUIDs");
	}

}


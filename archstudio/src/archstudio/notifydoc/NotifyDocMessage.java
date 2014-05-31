package archstudio.notifydoc;

import c2.fw.*;
import c2.util.UIDGenerator;
import edu.uci.ics.xarchutils.ObjRef;

public class NotifyDocMessage extends NamedPropertyMessage{
	
	public static final int OPERATION_SAVING = 300;
	public static final int OPERATION_CLOSING = 400;
	
	public NotifyDocMessage(int operation, ObjRef documentRef){
		super("NotifyDocMessage");
		super.addParameter("uid", UIDGenerator.generateUID());
		super.addParameter("operation", operation);
		super.addParameter("documentRef", documentRef);
	}

	protected NotifyDocMessage(NotifyDocMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new NotifyDocMessage(this);
	}
	
	public String getUID(){
		return (String)getParameter("uid");
	}

	public void setOperation(int operation){
		addParameter("operation", operation);
	}

	public int getOperation(){
		return getIntParameter("operation");
	}

	public void setDocumentRef(ObjRef documentRef){
		addParameter("documentRef", documentRef);
	}

	public ObjRef getDocumentRef(){
		return (ObjRef)getParameter("documentRef");
	}

}


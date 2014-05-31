package archstudio.notifydoc;

import c2.fw.*;

public class NotifyDocDoneMessage extends NamedPropertyMessage{
	public NotifyDocDoneMessage(Identifier componentId, String uid){
		super("NotifyDocDoneMessage");
		super.addParameter("componentId", componentId);
		super.addParameter("uid", uid);
	}

	protected NotifyDocDoneMessage(NotifyDocDoneMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new NotifyDocDoneMessage(this);
	}

	public void setComponentId(Identifier componentId){
		addParameter("componentId", componentId);
	}

	public Identifier getComponentId(){
		return (Identifier)getParameter("componentId");
	}

	public void setUid(String uid){
		addParameter("uid", uid);
	}

	public String getUid(){
		return (String)getParameter("uid");
	}

}


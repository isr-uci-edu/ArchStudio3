package archstudio.notifydoc;

import c2.fw.*;

public class NotifyDocStateMessage extends NamedPropertyMessage implements java.io.Serializable{

	public static final int START_NOTIFY = 200;
	public static final int STOP_NOTIFY = 250;
	
	public NotifyDocStateMessage(int messageType, Identifier componentId){
		super("InvokableStateMessage");
		super.addParameter("messageType", messageType);
		super.addParameter("componentId", componentId);
	}
	
	protected NotifyDocStateMessage(NotifyDocStateMessage copyMe){
		super(copyMe);
	}
	
	public Message duplicate(){
		return new NotifyDocStateMessage(this);
	}
	
	public int getMessageType(){
		return super.getIntParameter("messageType");
	}
	
	public Identifier getComponentId(){
		return (Identifier)super.getParameter("componentId");
	}
	
}


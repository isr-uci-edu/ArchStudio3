package archstudio.invoke;

import c2.fw.*;

public class InvokableStateMessage extends NamedPropertyMessage implements java.io.Serializable{

	public static final int SERVICE_ADVERTISED = 200;
	public static final int SERVICE_UNADVERTISED = 250;
	
	public InvokableStateMessage(int messageType, Identifier componentId, String serviceName, String serviceDescription){
		super("InvokableStateMessage");
		super.addParameter("messageType", messageType);
		super.addParameter("componentId", componentId);
		super.addParameter("serviceName", serviceName);
		super.addParameter("serviceDescription", serviceDescription);
	}
	
	protected InvokableStateMessage(InvokableStateMessage copyMe){
		super(copyMe);
	}
	
	public Message duplicate(){
		return new InvokableStateMessage(this);
	}
	
	public int getMessageType(){
		return super.getIntParameter("messageType");
	}
	
	public Identifier getComponentId(){
		return (Identifier)super.getParameter("componentId");
	}
	
	public String getServiceName(){
		return (String)super.getParameter("serviceName");
	}
	
	public String getServiceDescription(){
		return (String)super.getParameter("serviceDescription");
	}

}


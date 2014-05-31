package archstudio.invoke;

import c2.fw.*;

public class InvokeMessageProcessor implements MessageProcessor{

	protected InvokableBrick target;
	protected Interface invokeInterface;
	protected String serviceName;
	protected String serviceDescription;
	
	public InvokeMessageProcessor(InvokableBrick target, Interface invokeInterface, String serviceName, String serviceDescription){
		this.target = target;
		this.invokeInterface = invokeInterface;
		this.serviceName = serviceName;
		this.serviceDescription = serviceDescription;
	}
	
	public InvokableBrick getBrick(){
		return target;
	}
	
	public Interface getInterface(){
		return invokeInterface;
	}
	
	public String getServiceName(){
		return serviceName;
	}
	
	//These two functions are so this component's interface can be started by the Invoker.
	private void sendAdvertisement(){
		InvokableStateMessage ism = new InvokableStateMessage(InvokableStateMessage.SERVICE_ADVERTISED, 
			target.getIdentifier(), serviceName, serviceDescription);
		target.sendToAll(ism, invokeInterface);
	}
	
	private void sendUnadvertisement(){
		InvokableStateMessage ism = new InvokableStateMessage(InvokableStateMessage.SERVICE_UNADVERTISED, 
			target.getIdentifier(), serviceName, serviceDescription);
		target.sendToAll(ism, invokeInterface);
	}
	
	//We have to handle a request from the invoker that is asking us,
	//asynchronously, whether we can be invoked.
	public void handle(Message m){
		//If the incoming message is a QueryInvokableMessage, we have
		//to send our advertisement of availability.
		if(m instanceof QueryInvokableMessage){
			sendAdvertisement();
			//We return false because no further processing needs to take place
			//on this message.
			return;
		}
		//If the incoming message from the invoker is asking us to be
		//invoked, we have to do so.
		else if(m instanceof InvokeMessage){
			//First, we make sure that this message is for our component.
			InvokeMessage im = (InvokeMessage)m;
			Identifier compId = im.getComponentId();
			if(!compId.equals(target.getIdentifier())){
				return;
			}
			String sn = im.getServiceName();
			if(sn.equals(serviceName)){
				target.invoke(im);
				return;
			}
		}
		return;
	}

}

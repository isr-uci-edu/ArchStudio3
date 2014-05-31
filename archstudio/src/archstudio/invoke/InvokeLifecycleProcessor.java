package archstudio.invoke;

import c2.fw.*;

public class InvokeLifecycleProcessor extends LifecycleAdapter{
	
	protected InvokableBrick target;
	protected Interface invokeInterface;
	protected String serviceName;
	protected String serviceDescription;
	
	public InvokeLifecycleProcessor(InvokableBrick target, Interface invokeInterface, String serviceName, String serviceDescription){
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

	public void begin(){
		sendAdvertisement();
	}
	
	public void end(){
		sendUnadvertisement();
	}

}

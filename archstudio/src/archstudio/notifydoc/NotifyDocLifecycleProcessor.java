package archstudio.notifydoc;

import c2.fw.*;

public class NotifyDocLifecycleProcessor extends LifecycleAdapter{
	
	protected NotifyDocBrick target;
	protected Interface notifyDocInterface;
	
	public NotifyDocLifecycleProcessor(NotifyDocBrick target, Interface notifyDocInterface){
		this.target = target;
		this.notifyDocInterface = notifyDocInterface;
	}
	
	public NotifyDocBrick getBrick(){
		return target;
	}
	
	public Interface getInterface(){
		return notifyDocInterface;
	}
	
	//These two functions are so this component's interface can be started by the Invoker.
	private void sendStartNotifying(){
		NotifyDocStateMessage sm = new NotifyDocStateMessage(NotifyDocStateMessage.START_NOTIFY, 
			target.getIdentifier());
		target.sendToAll(sm, notifyDocInterface);
	}
	
	private void sendStopNotifying(){
		NotifyDocStateMessage sm = new NotifyDocStateMessage(NotifyDocStateMessage.STOP_NOTIFY, 
			target.getIdentifier());
		target.sendToAll(sm, notifyDocInterface);
	}

	public void begin(){
		sendStartNotifying();
	}
	
	public void end(){
		sendStopNotifying();
	}

}

package archstudio.notifydoc;

import c2.fw.*;

public class NotifyDocUtils{

	public static void deployNotifyDocService(DelegateBrick brick, Interface notifyDocInterface){
	
		if(!(brick instanceof NotifyDocBrick)){
			throw new IllegalArgumentException("Brick must be notifiable.");
		}
		
		NotifyDocMessageProcessor mp = new NotifyDocMessageProcessor((NotifyDocBrick)brick, notifyDocInterface);
		NotifyDocLifecycleProcessor lp = new NotifyDocLifecycleProcessor((NotifyDocBrick)brick, notifyDocInterface);
		
		brick.addMessageProcessor(mp);
		brick.addLifecycleProcessor(lp);
	}

	public static void undeployNotifyDocService(DelegateBrick brick, Interface notifyDocInterface){
		if(!(brick instanceof NotifyDocBrick)){
			throw new IllegalArgumentException("Brick must be notifiable.");
		}
		
		MessageProcessor[] mps = brick.getMessageProcessors();
		LifecycleProcessor[] lps = brick.getLifecycleProcessors();
		
		for(int i = 0; i < mps.length; i++){
			if(mps[i] instanceof NotifyDocMessageProcessor){
				NotifyDocMessageProcessor imp = (NotifyDocMessageProcessor)mps[i];
				if(imp.getInterface().equals(notifyDocInterface)){
					brick.removeMessageProcessor(imp);
					break;
				}
			}
		}
		
		for(int i = 0; i < lps.length; i++){
			if(lps[i] instanceof NotifyDocLifecycleProcessor){
				NotifyDocLifecycleProcessor ilp = (NotifyDocLifecycleProcessor)lps[i];
				if(ilp.getInterface().equals(notifyDocInterface)){
					brick.removeLifecycleProcessor(ilp);
					break;
				}
			}
		}
	}
}

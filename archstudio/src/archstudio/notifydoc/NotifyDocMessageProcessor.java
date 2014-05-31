package archstudio.notifydoc;

import c2.fw.*;
import edu.uci.ics.xarchutils.ObjRef;

public class NotifyDocMessageProcessor implements MessageProcessor{

	protected NotifyDocBrick target;
	protected Interface notifyDocInterface;

	public NotifyDocMessageProcessor(NotifyDocBrick target, Interface notifyDocInterface){
		this.target = target;
		this.notifyDocInterface = notifyDocInterface;
	}
	
	public NotifyDocBrick getBrick(){
		return target;
	}
	
	public Interface getInterface(){
		return notifyDocInterface;
	}
	
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
	
	public void handle(Message m){
		if(m instanceof QueryNotifyDocMessage){
			sendStartNotifying();
			return;
		}
		else if(m instanceof NotifyDocMessage){
			//This says "OK, I'm going to save/close a document."
			//It's now our job to synchronously tell our target
			//component, wait for it to get done with whatever
			//it's going to do, and then send out an 
			//"OK, you can save/close now" message.
			NotifyDocMessage ndm = (NotifyDocMessage)m;
			
			int op = ndm.getOperation();
			ObjRef documentRef = ndm.getDocumentRef();
			
			switch(op){
			case NotifyDocMessage.OPERATION_SAVING:
				target.docSaving(documentRef);
				break;
			case NotifyDocMessage.OPERATION_CLOSING:
				target.docClosing(documentRef);
				break;
			}
			NotifyDocDoneMessage nddm = new NotifyDocDoneMessage(target.getIdentifier(), ndm.getUID());
			target.sendToAll(nddm, notifyDocInterface);
		}
		return;
	}

}

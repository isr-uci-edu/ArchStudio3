package archstudio.comp.editoradt;

import java.util.*;

import c2.fw.*;

//Support for "legacy" C2 components
import c2.legacy.*;

import archstudio.editors.*;

public class EditorADTC2Component extends AbstractC2DelegateBrick{

	protected Set activeEditors = Collections.synchronizedSet(new HashSet());
	
	public EditorADTC2Component(Identifier id){
		super(id);
		addLifecycleProcessor(new EditorADTLifecycleProcessor());
		addMessageProcessor(new EditorADTMessageProcessor());
	}
	
	class EditorADTLifecycleProcessor extends LifecycleAdapter{
		public void begin(){
			sendAllEditorsStatusMessage();
		}
		
		public void end(){
			AllEditorsStatusMessage aesm = new AllEditorsStatusMessage(new String[0]);
			sendToAll(aesm, bottomIface);
		}
	}
	
	class EditorADTMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof ChangeEditorStatusMessage){
				handleChangeEditorStatus((ChangeEditorStatusMessage)m);
			}
			else if(m instanceof GetAllEditorsStatusMessage){
				handleGetAllEditorsStatus((GetAllEditorsStatusMessage)m);
			}
		}
	}
	
	protected void handleChangeEditorStatus(ChangeEditorStatusMessage m){
		synchronized(activeEditors){
			String editorID = m.getEditorID();
			int newStatus = m.getNewStatus();
			if(newStatus == ChangeEditorStatusMessage.STATUS_ACTIVE){
				activeEditors.add(editorID);
			}
			sendAllEditorsStatusMessage();
		}
	}
	
	protected void handleGetAllEditorsStatus(GetAllEditorsStatusMessage m){
		sendAllEditorsStatusMessage();
	}
	
	public void sendAllEditorsStatusMessage(){
		String[] activeEditorIDs = (String[])activeEditors.toArray(new String[0]);
		AllEditorsStatusMessage aesm = new AllEditorsStatusMessage(activeEditorIDs);
		sendToAll(aesm, bottomIface);
	}
	
	
}
	
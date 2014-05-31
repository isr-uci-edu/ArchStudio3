package archstudio.comp.archon;

import archstudio.archon.*;
import c2.fw.*;
import c2.legacy.*;
import c2.pcwrap.EBIWrapperUtils;

import java.util.*;

import edu.uci.ics.xarchutils.XArchFlatInterface;

public class ArchonC2Component extends AbstractC2DelegateBrick{

	protected Map interpreters = Collections.synchronizedMap(new HashMap());
	protected ArchonInputReadyHandler inputReadyHandler = new ArchonInputReadyHandler();
	protected ArchonOutputHandler outputHandler = new ArchonOutputHandler();
	protected XArchFlatInterface xarch;
	
	protected Map availableServices = new HashMap();
	
	public ArchonC2Component(Identifier id){
		super(id);
		
		xarch = (XArchFlatInterface)EBIWrapperUtils.addExternalService(this, topIface, XArchFlatInterface.class);
		availableServices.put("xarch", xarch);
		
		addLifecycleProcessor(new ArchonLifecycleProcessor());
		addMessageProcessor(new ArchonMessageProcessor());
	}
	
	protected ArchonInterpreter createInterpreter(String id){
		ArchonInterpreter interp = new ArchonInterpreter(id, availableServices);
		interp.addArchonInputReadyListener(inputReadyHandler);
		interp.addArchonOutputListener(outputHandler);
		interpreters.put(id, interp);
		return interp;
	}
	
	protected ArchonInterpreter getInterpreter(String id){
		return (ArchonInterpreter)interpreters.get(id);
	}

	protected void destroyInterpreter(String id){
		interpreters.remove(id);
	}
	
	class ArchonLifecycleProcessor extends LifecycleAdapter{
		public void begin(){
		}
	}
	
	class ArchonInputReadyHandler implements ArchonInputReadyListener{
		public synchronized void archonInputReady(String interpreterID, boolean continuing){
			ArchonInputReadyMessage airm = new ArchonInputReadyMessage(interpreterID, continuing);
			sendToAll(airm, bottomIface);
		}
	}
	
	class ArchonOutputHandler implements ArchonOutputListener{
		public void archonLine(String interpreterID, String line){}
		
		public synchronized void archonEchoLine(String interpreterID, String line){
			ArchonOutputMessage aom = new ArchonOutputMessage(interpreterID, ArchonOutputMessage.STREAM_ECHO, line);
			sendToAll(aom, bottomIface);
		}

		public synchronized void archonStdoutLine(String interpreterID, String line){
			ArchonOutputMessage aom = new ArchonOutputMessage(interpreterID, ArchonOutputMessage.STREAM_OUT, line);
			sendToAll(aom, bottomIface);
		}
		
		public synchronized void archonStderrLine(String interpreterID, String line){
			ArchonOutputMessage aom = new ArchonOutputMessage(interpreterID, ArchonOutputMessage.STREAM_ERR, line);
			sendToAll(aom, bottomIface);
		}
	}
	
	class ArchonMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof ArchonCreateInterpreterMessage){
				ArchonCreateInterpreterMessage acim = (ArchonCreateInterpreterMessage)m;
				createInterpreter(acim.getInterpreterID());
			}
			else if(m instanceof ArchonDestroyInterpreterMessage){
				ArchonDestroyInterpreterMessage adim = (ArchonDestroyInterpreterMessage)m;
				destroyInterpreter(adim.getInterpreterID());
			}
			else if(m instanceof ArchonExecMessage){
				ArchonExecMessage aem = (ArchonExecMessage)m;
				String id = aem.getInterpreterID();
				ArchonInterpreter interpreter = getInterpreter(id);
				if(interpreter != null){
					String[] commands = aem.getCommands();
					for(int i = 0; i < commands.length; i++){
						interpreter.exec(commands[i]);
					}
				}
			}
		}
	}

}

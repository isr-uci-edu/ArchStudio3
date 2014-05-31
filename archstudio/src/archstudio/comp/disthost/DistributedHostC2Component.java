package archstudio.comp.disthost;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

import java.net.*;
import java.security.*;
import java.util.*;

public class DistributedHostC2Component extends AbstractC2DelegateBrick{
	
	public static final int PING_INTERVAL = 2000;
	public static final Message pingMessage = getPingMessage();
	
	private Pinger pinger;
	
	public DistributedHostC2Component(Identifier id){
		super(id);
		this.addLifecycleProcessor(new DistributedHostC2ComponentLifecycleProcessor());
		this.addMessageProcessor(new DistributedHostC2ComponentMessageProcessor());
	}
	
	public static final Message getPingMessage(){
		String hostName = "[UnknownHost]";
		try{
			InetAddress localHostAddress = InetAddress.getLocalHost();
			hostName = localHostAddress.getCanonicalHostName();
		}
		catch(Exception e){
		}
		
		String processID = System.getProperty("process.id");
		if(processID == null){
			Random rnd = new Random();
			processID = Long.toHexString(System.currentTimeMillis()) + "." + Long.toHexString(rnd.nextLong());
		}
		NamedPropertyMessage m = new NamedPropertyMessage("DistributedHostPing");
		m.addParameter("hostName", hostName);
		m.addParameter("processID", processID);
		return m;
	}
		
	class DistributedHostC2ComponentLifecycleProcessor extends LifecycleAdapter{
		public void begin(){
			if(pinger == null){
				pinger = new Pinger();
				pinger.start();
			}
		}
		
		public void end(){
			if(pinger != null){
				pinger.terminate();
				pinger = null;
			}
		}			
	}
	
	class DistributedHostC2ComponentMessageProcessor implements MessageProcessor{
		public void handle(Message m){
		}
	}
	
	class Pinger extends Thread{
		private boolean shouldTerminate = false;
			
		public void terminate(){
			shouldTerminate = true;
		}
		
		public synchronized void run(){
			while(!shouldTerminate){
				sendToAll(pingMessage, bottomIface);
				try{
					Thread.sleep(PING_INTERVAL);
				}
				catch(InterruptedException e){
				}
			}
		}
	}
}

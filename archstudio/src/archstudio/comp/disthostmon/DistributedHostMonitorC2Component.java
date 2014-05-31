package archstudio.comp.disthostmon;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class DistributedHostMonitorC2Component extends AbstractC2DelegateBrick{
	//protected Set hostSet;
	/** Maps host messages to Longs with the times they last pinged */
	protected Map hostPingMap;
	protected DistributedHostMonitorFrame frame;
	
	public DistributedHostMonitorC2Component(Identifier id){
		super(id);
		//this.hostSet = new HashSet();
		this.hostPingMap = new HashMap();
		this.addLifecycleProcessor(new DistributedHostMonitorC2ComponentLifecycleProcessor());
		this.addMessageProcessor(new DistributedHostMonitorC2ComponentMessageProcessor());
	}
	
	class DistributedHostMonitorC2ComponentLifecycleProcessor extends LifecycleAdapter{
		HostListThread hlt;
		
		public void begin(){
			frame = new DistributedHostMonitorFrame();
			frame.setVisible(true);
			hlt = new HostListThread();
		}
		
		public void end(){
			hlt.terminate();
			if(frame != null){
				frame.setVisible(false);
				frame.dispose();
				frame = null;
			}
		}
	}
	
	class DistributedHostMonitorC2ComponentMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof NamedPropertyMessage){
				NamedPropertyMessage npm = (NamedPropertyMessage)m;
				if(npm.getName().equals("DistributedHostPing")){
					handlePing(npm);
				}
			}
		}
	}

	protected void handlePing(NamedPropertyMessage npm){
		//System.out.println("Got message: " + npm);
		synchronized(hostPingMap){
			hostPingMap.put(npm, new Long(System.currentTimeMillis()));
		}
		//updateHostList();
	}
	
	protected void updateHostList(){
		if(frame != null){
			frame.updateHostList();
		}
	}
	
	class HostListThread extends Thread{
		protected boolean shouldTerminate = false;
		
		public HostListThread(){
			this.setPriority(Thread.MIN_PRIORITY);
			this.setDaemon(true);
			this.start();
		}
		
		public void terminate(){
			this.shouldTerminate = true;
		}
		
		public void run(){
			while(!shouldTerminate){
				updateHostList();
				try{
					Thread.sleep(2000);
				}
				catch(InterruptedException e){
				}
			}
		}
	}
	
	class DistributedHostMonitorFrame extends JFrame{
		protected DefaultListModel listModel = new DefaultListModel();
		protected Vector listContents = new Vector();
		
		public DistributedHostMonitorFrame(){
			super("Distributed Host Monitor");
			
			JList list = new JList(listModel);
			this.getContentPane().setLayout(new BorderLayout());
			
			this.getContentPane().add("Center", list);
			
			this.setSize(400, 400);
			this.setVisible(true);
			this.validate();
			this.repaint();
			updateHostList();
		}
	
		public void updateHostList(){
			System.out.println("Updating host list.");
			synchronized(hostPingMap){
				listModel.removeAllElements();
				listContents.removeAllElements();
				long curTime = System.currentTimeMillis();
				for(Iterator it = hostPingMap.keySet().iterator(); it.hasNext(); ){
					NamedPropertyMessage npm = (NamedPropertyMessage)it.next();
					String hostName = (String)npm.getParameter("hostName");
					String processID = (String)npm.getParameter("processID");
					
					long pingTime = ((Long)hostPingMap.get(npm)).longValue();
					long idleTime = curTime - pingTime;
					long idleTimeSeconds = idleTime / 1000L;
					
					String descString = null;
					if(idleTime < 5000){
						descString = "<html><font color=#000000>" + hostName + " / " + processID + "</font></html>";
					}
					else if((idleTime >= 5000) && (idleTime < 10000)){
						descString = "<html><font color=#000000>" + hostName + " / " + processID + 
							" (" + (idleTimeSeconds) + "s)</font></html>";
					}
					else{
						descString = "<html><font color=#888888>" + hostName + " / " + processID + 
							" (" + (idleTimeSeconds) + "s)</font></html>";
					}
					
					listModel.addElement(descString);
					listContents.addElement(npm);
				}
				validate();
				repaint();
			}
		}
	}
	
}

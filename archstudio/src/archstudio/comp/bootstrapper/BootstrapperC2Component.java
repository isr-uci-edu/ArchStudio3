package archstudio.comp.bootstrapper;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

import java.util.*;

import archstudio.comp.xarchtrans.*;
import archstudio.comp.aem.AEMInstantiateMessage;
import archstudio.comp.aem.AEMInstantiateStatusMessage;
import edu.uci.ics.xarchutils.*;

public class BootstrapperC2Component extends AbstractC2DelegateBrick{
	protected XArchFlatTransactionsInterface xarch;
	
	protected InitializationParameter ip = null;
	protected InitializationParameter engineIp = null;
	
	public BootstrapperC2Component(Identifier id){
		super(id);
		xarch = (XArchFlatTransactionsInterface)EBIWrapperUtils.addExternalService(this, topIface, XArchFlatTransactionsInterface.class);
		this.addLifecycleProcessor(new BootstrapperLifecycleProcessor());
		this.addMessageProcessor(new BootstrapperMessageProcessor());
	}
	
	public BootstrapperC2Component(Identifier id, InitializationParameter[] params){
		this(id);
		for(int i = 0; i < params.length; i++){
			String name = params[i].getName();
			String value = params[i].getValue();
			
			if(name.equals("startURL")){
				ip = params[i];
			}
			else if(name.equals("startFile")){
				ip = params[i];
			}
			else if(name.equals("engineType")){
				engineIp = params[i];
			}
		}
	}
	
	class BootstrapperLifecycleProcessor extends c2.fw.LifecycleAdapter{
		public void begin(){
			super.begin();
			if(ip == null){
				return;
			}
			String name = ip.getName();
			String value = ip.getValue();
			
			int engineType = AEMInstantiateMessage.ENGINETYPE_ONETHREADPERBRICK;
			if(engineIp != null){
				if(engineIp.getValue().equals("regular")){
					engineType = AEMInstantiateMessage.ENGINETYPE_ONETHREADPERBRICK;
				}
				else if(engineIp.getValue().equals("steppable")){
					engineType = AEMInstantiateMessage.ENGINETYPE_ONETHREADSTEPPABLE;
				}
			}
			
			if(name.equals("startURL")){
				ObjRef xArchRef = null;
				try{
					xArchRef = xarch.parseFromURL(value);
				}
				catch(org.xml.sax.SAXException saxe){
					saxe.printStackTrace();
					System.exit(7);
				}
				catch(java.net.MalformedURLException mue){
					mue.printStackTrace();
					System.exit(5);
				}
				catch(java.io.IOException ioe){
					ioe.printStackTrace();
					System.exit(5);
				}
				//aemImpl.instantiate(xArchRef);
				AEMInstantiateMessage aim = new AEMInstantiateMessage("urn:System", xArchRef, engineType);
				sendToAll(aim, topIface);
				return;
			}
			else if(name.equals("startFile")){
				ObjRef xArchRef = null;
				
				try{
					xArchRef = xarch.parseFromFile(value);
				}
				catch(org.xml.sax.SAXException saxe){
					saxe.printStackTrace();
					System.exit(7);
				}
				catch(java.net.MalformedURLException mue){
					mue.printStackTrace();
					System.exit(5);
				}
				catch(java.io.IOException ioe){
					ioe.printStackTrace();
					System.exit(5);
				}
				
				AEMInstantiateMessage aim = new AEMInstantiateMessage("urn:System", xArchRef, engineType);
				sendToAll(aim, topIface);
				return;
			}
		}
	}
	
	class BootstrapperMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof AEMInstantiateStatusMessage){
				AEMInstantiateStatusMessage aism = (AEMInstantiateStatusMessage)m;
				if(!aism.isSuccess()){
					System.err.println("System could not be instantiated.");
					System.err.println();
					System.err.println("Exception was:");
					aism.getError().printStackTrace();
					System.exit(10);
				}
			}
		}
	}				

	
}

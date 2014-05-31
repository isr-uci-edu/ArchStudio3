package archstudio.comp.aem;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

import java.util.*;

import archstudio.comp.xarchtrans.*;
import edu.uci.ics.xarchutils.*;

public class AEMC2Component extends AbstractC2DelegateBrick{
	protected XArchFlatTransactionsInterface xarch;
	
	protected InitializationParameter ip = null;
	protected InitializationParameter engineIp = null;
	
	protected HashMap managedSystems = new HashMap();
	
	private AEMMessageProcessor aemMP;
	
	public AEMC2Component(Identifier id){
		super(id);
		xarch = (XArchFlatTransactionsInterface)EBIWrapperUtils.addExternalService(this, topIface, XArchFlatTransactionsInterface.class);
		//aemImpl = new AEMImpl(xarch);
		//EBIWrapperUtils.deployService(this, bottomIface, bottomIface, aemImpl, new Class[]{AEMInterface.class}, new Class[]{});
		//EBIWrapperUtils.deployService(this, bottomIface, bottomIface, this, new Class[]{AEMInterface.class}, new Class[]{});
		this.addLifecycleProcessor(new AEMLifecycleProcessor());
		aemMP = new AEMMessageProcessor();
		this.addMessageProcessor(aemMP);
	}
	
	public AEMC2Component(Identifier id, InitializationParameter[] params){
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
	
	class AEMLifecycleProcessor extends c2.fw.LifecycleAdapter{
		public void begin(){
			super.begin();
			try{
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
					System.err.println("***NOTE: Bootstrapping from AEM is now deprecated and ");
					System.err.println("   will be removed in a future release.");
					ObjRef xArchRef = xarch.parseFromURL(value);
					//aemImpl.instantiate(xArchRef);
					AEMInstantiateMessage aim = new AEMInstantiateMessage("urn:System", xArchRef, engineType);
					aemMP.handle(aim);
					/*
					ManagedSystem managedSystem = new ManagedSystem("urn:System", xarch, engineType);
					managedSystem.addMessageListener(
						new MessageListener(){
							public void messageSent(Message m){
								sendToAll(m, bottomIface);
							}
						}
					);
					managedSystem.bind(xArchRef);
					managedSystem.startSystem();
					*/
					return;
				}
				else if(name.equals("startFile")){
					System.err.println("***NOTE: Bootstrapping from AEM is now deprecated and ");
					System.err.println("   will be removed in a future release.");
					ObjRef xArchRef = xarch.parseFromFile(value);
					AEMInstantiateMessage aim = new AEMInstantiateMessage("urn:System", xArchRef, engineType);
					aemMP.handle(aim);
					//aemImpl.instantiate(xArchRef);
					/*
					ManagedSystem managedSystem = new ManagedSystem("urn:System", xarch, engineType);
					managedSystem.addMessageListener(
						new MessageListener(){
							public void messageSent(Message m){
								sendToAll(m, bottomIface);
							}
						}
					);
					managedSystem.bind(xArchRef);
					managedSystem.startSystem();
					*/
					return;
				}
			}
			catch(Exception e){
				e.printStackTrace();
				System.exit(10);
			}
		}
	}

	class AEMMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof XArchTransactionEvent){
				for(Iterator it = managedSystems.values().iterator(); it.hasNext(); ){
					ManagedSystem ms = (ManagedSystem)it.next();
					ms.handleTransactionEvent((XArchTransactionEvent)m);
				}
			}
			if(m instanceof AEMCreateInstanceModelMessage){
				String managedSystemURI = ((AEMCreateInstanceModelMessage)m).getManagedSystemURI();
				ManagedSystem ms = (ManagedSystem)managedSystems.get(managedSystemURI);
				if(ms != null){
					String instanceModelURI = ((AEMCreateInstanceModelMessage)m).getInstanceModelURI();
					ms.createInstanceModel(xarch, instanceModelURI);
				}
			}
			else if(m instanceof AEMInstantiateMessage){
				String managedSystemURI = ((AEMInstantiateMessage)m).getManagedSystemURI();
				ObjRef xArchRef = ((AEMInstantiateMessage)m).getXArchRef();
				int engineType = ((AEMInstantiateMessage)m).getEngineType();
				if(xArchRef != null){
					String architectureURI = xarch.getXArchURI(xArchRef);
					try{
						instantiate(managedSystemURI, xArchRef, engineType);
						AEMInstantiateStatusMessage aism = new AEMInstantiateStatusMessage(
							managedSystemURI, architectureURI);
						sendToAll(aism, bottomIface);
					}
					catch(InvalidArchitectureDescriptionException iade){
						AEMInstantiateStatusMessage aism = new AEMInstantiateStatusMessage(
							architectureURI, iade);
						sendToAll(aism, bottomIface);
					}
					return;
				}
				String uri = ((AEMInstantiateMessage)m).getURI();
				if(uri != null){
					try{
						instantiate(managedSystemURI, uri, engineType);
						AEMInstantiateStatusMessage aism = new AEMInstantiateStatusMessage(
							managedSystemURI, uri);
						sendToAll(aism, bottomIface);
					}
					catch(InvalidArchitectureDescriptionException iade){
						AEMInstantiateStatusMessage aism = new AEMInstantiateStatusMessage(
							uri, iade);
						sendToAll(aism, bottomIface);
					}
					return;
				}
			}
		}
	}				
	
	public void instantiate(String managedSystemURI, ObjRef xArchRef, int engineType) throws InvalidArchitectureDescriptionException{
		ManagedSystem managedSystem = new ManagedSystem(managedSystemURI, xarch, engineType);
		managedSystem.addMessageListener(
			new MessageListener(){
				public void messageSent(Message m){
					sendToAll(m, bottomIface);
				}
			}
		);
		managedSystem.bind(xArchRef);
		managedSystem.startSystem();
		managedSystems.put(managedSystemURI, managedSystem);
		//managedSystem.createInstanceModel("Instance00");
	}

	public void instantiate(String managedSystemURI, String url, int engineType) throws InvalidArchitectureDescriptionException{
		ObjRef xArchRef = xarch.getOpenXArch(url);
		if(xArchRef == null){
			throw new IllegalArgumentException("Invalid URL");
		}
		instantiate(managedSystemURI, xArchRef, engineType);
	}
	
}


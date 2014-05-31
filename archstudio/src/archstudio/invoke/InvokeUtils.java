package archstudio.invoke;

import c2.fw.*;

/**
 * Provides static functions to easily make any DelegateBrick invokable by the ArchStudio
 * File Manager/Invoker component.
 * @author Eric M. Dashofy <A HREF="mailto:edashofy@ics.uci.edu">edashofy@ics.uci.edu</A>
 */
public class InvokeUtils{

	/**
	 * Configure a DelegateBrick to provide an invokable service.
	 * @param brick The brick that provides the invokable service. 
	 * <FONT COLOR=#FF0000>NOTE!</CODE> This brick must also implement the
	 * <CODE>InvokableBrick</CODE> interface, or this method will throw an 
	 * <CODE>IllegalArgumentException</CODE>.
	 * @param invokeInterface The interface on <CODE>brick</CODE> where incoming
	 * invoke and query-invokable messages will arrive.
	 * @param serviceName The name of the invokable service to deploy.
	 * @param serviceDescription Description of the invokable service to deploy.
	 */
	public static void deployInvokableService(DelegateBrick brick, Interface invokeInterface,
	String serviceName, String serviceDescription){
	
		if(!(brick instanceof InvokableBrick)){
			throw new IllegalArgumentException("Brick must be invokable.");
		}
		
		InvokeMessageProcessor imp = new InvokeMessageProcessor((InvokableBrick)brick, invokeInterface,
			serviceName, serviceDescription);
		InvokeLifecycleProcessor ilp = new InvokeLifecycleProcessor((InvokableBrick)brick, invokeInterface,
			serviceName, serviceDescription);
		
		brick.addMessageProcessor(imp);
		brick.addLifecycleProcessor(ilp);
	}

	/**
	 * Undeploy a previously deployed invokable service.
	 * @param brick The brick that will no longer provide the invokable service. 
	 * @param invokeInterface The interface on <CODE>brick</CODE> where the service
	 * was previously deployed.
	 * @param serviceName The name of the invokable service to undeploy.
	 */
	public static void undeployInvokableService(DelegateBrick brick, Interface invokeInterface,
	String serviceName){
		if(!(brick instanceof InvokableBrick)){
			throw new IllegalArgumentException("Brick must be invokable.");
		}
		
		MessageProcessor[] mps = brick.getMessageProcessors();
		LifecycleProcessor[] lps = brick.getLifecycleProcessors();
		
		for(int i = 0; i < mps.length; i++){
			if(mps[i] instanceof InvokeMessageProcessor){
				InvokeMessageProcessor imp = (InvokeMessageProcessor)mps[i];
				if(imp.getInterface().equals(invokeInterface)){
					if(imp.getServiceName().equals(serviceName)){
						brick.removeMessageProcessor(imp);
						break;
					}
				}
			}
		}
		
		for(int i = 0; i < lps.length; i++){
			if(lps[i] instanceof InvokeLifecycleProcessor){
				InvokeLifecycleProcessor ilp = (InvokeLifecycleProcessor)lps[i];
				if(ilp.getInterface().equals(invokeInterface)){
					if(ilp.getServiceName().equals(serviceName)){
						brick.removeLifecycleProcessor(ilp);
						break;
					}
				}
			}
		}
	}
}

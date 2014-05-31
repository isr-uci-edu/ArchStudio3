/*
 * Created on Jul 4, 2005
 *
 */
package archstudio.comp.aem;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import archstudio.tron.TronElementIdentifier;
import archstudio.tron.TronIssue;
import archstudio.tron.TronTestResultMessage;
import c2.fw.Identifier;
import c2.fw.InitializationParameter;
import c2.fw.Message;
import c2.fw.MessageListener;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.ObjRef;

public class SecureAEMC2Component extends AEMC2Component {
	public SecureAEMC2Component(Identifier id){
		super(id);
	}
	
	public SecureAEMC2Component(Identifier id, InitializationParameter[] params){
		super(id, params);
	}

	public void instantiate(String managedSystemURI, ObjRef xArchRef, int engineType) throws InvalidArchitectureDescriptionException{
		// To create our SecureManagedSystem, instead of a regular ManagedSystem
		// The variable declaration should not be of type ManagedSystem, 
		// since this involves loader linkage issue
		SecureManagedSystem managedSystem = new SecureManagedSystem(managedSystemURI, xarch, engineType);
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

		Set bindIssues = new HashSet();
		Set	insecureBrickRefs = managedSystem.getInsecureBrickRefs();
		Set insecureLinkRefs = managedSystem.getInsecureLinkRefs();
		Set rejectedLinkRefs = managedSystem.getRejectedLinkRefs();
		for (Iterator i = insecureBrickRefs.iterator(); i.hasNext(); ) {
			ObjRef	b = (ObjRef)i.next();
			TronElementIdentifier	tei = new TronElementIdentifier(b, null);
			TronIssue ti = new TronIssue("Security", xArchRef, "SecureManagedSystem", TronIssue.SEVERITY_ERROR,
					"Insecure brick: " + XadlUtils.getID(xarch, b), "Cannot instantiate bricks", null, new TronElementIdentifier[]{tei});
			bindIssues.add(ti);
		}
		for (Iterator i = insecureLinkRefs.iterator(); i.hasNext(); ) {
			ObjRef	l = (ObjRef)i.next();
			TronElementIdentifier	tei = new TronElementIdentifier(l, null);
			TronIssue ti = new TronIssue("Security", xArchRef, "SecureManagedSystem", TronIssue.SEVERITY_ERROR,
					"Link to insecure brick: " + XadlUtils.getID(xarch, l), "Cannot weld insecure bricks", null, new TronElementIdentifier[]{tei});
			bindIssues.add(ti);
		}
		for (Iterator i = rejectedLinkRefs.iterator(); i.hasNext(); ) {
			ObjRef	l = (ObjRef)i.next();
			TronElementIdentifier	tei = new TronElementIdentifier(l, null);
			TronIssue ti = new TronIssue("Security", xArchRef, "SecureManagedSystem", TronIssue.SEVERITY_ERROR,
					"Rejected link: " + XadlUtils.getID(xarch, l), "Cannot add insecure weld", null, new TronElementIdentifier[]{tei});
			bindIssues.add(ti);
		}
		TronTestResultMessage ttrm = new TronTestResultMessage(
			null, xArchRef, (TronIssue[])bindIssues.toArray(new TronIssue[0]));
		sendToAll(ttrm, topIface);
	}
}

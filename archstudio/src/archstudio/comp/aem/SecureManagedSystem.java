/*
 * Created on Jul 4, 2005
 *
 */
package archstudio.comp.aem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import archstudio.comp.aac.AACC2Component;
import archstudio.comp.aac.ArchitecturalTopology;
import archstudio.comp.aac.AACC2Component.SecurityProperties;
import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;
import c2.fw.Brick;
import c2.fw.BrickDescription;
import c2.fw.Identifier;
import c2.fw.InitializationParameter;
import c2.fw.LocalArchitectureManager;
import c2.fw.MessageHandler;
import c2.fw.Weld;
import c2.fw.secure.BrickSecurityException;
import c2.fw.secure.IPEP;
import c2.fw.secure.IPolicy;
import c2.fw.secure.SecureArchitectureManager;
import c2.fw.secure.SecureOneQueuePerInterfaceMessageHandler;
import c2.fw.secure.WeldSecurityException;
import c2.fw.secure.xacml.DynamicPDP;
import c2.fw.secure.xacml.XACMLUtils;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchBulkQuery;
import edu.uci.ics.xarchutils.XArchFlatProxyUtils;
import edu.uci.ics.xarchutils.XArchFlatQueryInterface;
import edu.uci.isr.xarch.security.IInitializationParameterStructure;
import edu.uci.isr.xarch.security.IParameterPriority;
import edu.uci.isr.xarch.security.IPolicies;
import edu.uci.isr.xarch.security.IPolicySetType;
import edu.uci.isr.xarch.security.IPrincipal;
import edu.uci.isr.xarch.security.IPrincipals;
import edu.uci.isr.xarch.security.IPrivilege;
import edu.uci.isr.xarch.security.IPrivileges;
import edu.uci.isr.xarch.security.ISafeguards;
import edu.uci.isr.xarch.security.ISecureComponent;
import edu.uci.isr.xarch.security.ISecureComponentType;
import edu.uci.isr.xarch.security.ISecureConnector;
import edu.uci.isr.xarch.security.ISecureConnectorType;
import edu.uci.isr.xarch.security.ISecureInterface;
import edu.uci.isr.xarch.security.ISecureSignature;
import edu.uci.isr.xarch.security.ISecurityPropertyType;
import edu.uci.isr.xarch.security.ISubject;

public class SecureManagedSystem extends ManagedSystem {
    protected	SecureOneQueuePerInterfaceMessageHandler	handler;
    protected 	SecureArchitectureManager					manager;
    protected	ArchitecturalTopology						topology;
    
	public SecureManagedSystem(String managedSystemURI, XArchFlatTransactionsInterface realxarch, int engineType){
		super(managedSystemURI, realxarch, engineType);
	}

	// The security is concerned with the architectural mananger and the messagehandler
	protected MessageHandler createHandler() {
		handler = new SecureOneQueuePerInterfaceMessageHandler();
		return handler;
	}

	protected LocalArchitectureManager createManager() {
		manager = new SecureArchitectureManager();
		return manager;
	}

	// Here, we add more elements that the security is interested
	public XArchBulkQuery getBulkQuery(ObjRef xArchRef){
		XArchBulkQuery result = super.getBulkQuery(xArchRef);
		ArchitecturalTopology.addSecurityQuery(result);
		
		topology = new ArchitecturalTopology(realxarch, xArchRef, 
				ArchitecturalTopology.BOTTOM_UP | ArchitecturalTopology.TOP_DOWN);
		
		// Get policy for the whole architecture
	    ISecurityPropertyType iSecurity = topology.getArchitecturalProperty();
		if (iSecurity != null) {
			IPolicies policies = iSecurity.getPolicies();
			if (policies != null) {
				for (Iterator pi = policies.getAllPolicySets().iterator(); pi.hasNext(); ) {
					IPolicySetType	iPolicy = (IPolicySetType)pi.next();
				    if (iPolicy != null) {
				        String	policy = iPolicy.getPolicy();
				        if (policy != null) {
				            Set	p = new HashSet();
				            p.add(policy);
				            architecturePDP = new DynamicPDP(p, new HashSet());
				            manager.setPDP(architecturePDP);
				            handler.setPDP(architecturePDP);
				        }
				    }
				}
			}
		}
		return result;
	}
	protected	DynamicPDP	architecturePDP;
	
	// The set of bricks that have not been created because of security reasons
	protected Set		insecureBricks = new HashSet();
	protected Set		insecureBrickRefs = new HashSet();
	protected XArchFlatQueryInterface xarchBulk;
	protected Identifier bindBrick(XArchFlatQueryInterface xarchBulk, ObjRef elementRef) throws InvalidArchitectureDescriptionException{
		// Need to record xarchBulk
		this.xarchBulk = xarchBulk;
		
		Identifier	id = null;
		try {
			id = super.bindBrick(xarchBulk, elementRef);
			// If this used to be a insecure brick, now it is successfully created
			insecureBricks.remove(id);
			insecureBrickRefs.remove(elementRef);
		}
		catch (BrickSecurityException bcse) {
			// First, record the brick id so later related links would not trigger errors
			Identifier insecureId = bcse.getBrickIdentifier();
			insecureBricks.add(insecureId);
			insecureBrickRefs.add(elementRef);
			// Second, remove the brick from xArchADT to maintain "consistency"
			// realxarch.remove(xarchBulk.getParent(elementRef), 
			//		xarchBulk.getElementName(elementRef), elementRef);
		}
		return id;
	}
	
	public Set getInsecureBrickRefs() {
		return insecureBrickRefs;
	}
	
	// To record a parameter for a structure with its priority over the type counterpart
	private static class ParameterWithPriority {
		String	name;
		List	values;
		String	priority;
		ParameterWithPriority(String name, List values, String priority) {
			this.name = name;
			this.values = values;
			this.priority = priority;
		}
	}
	
	protected void addBrick(Identifier brickId, BrickDescription brickDescription, 
			InitializationParameter[] initParamsForClass, ObjRef brickRef, ObjRef[] interfaceRefs,
			String[] interfaceIds) throws InvalidArchitectureDescriptionException {
		// Copy the init parameters of the class, this is from the type specification
		// A map of a list, the key is the parameter name, 
		// the list is the parameters having the same name
		Map		parameters = new HashMap();
		for (int i =0; initParamsForClass != null && i<initParamsForClass.length; i++) {
			String	parameterName = initParamsForClass[i].getName();
			List	parameterForName;
			if (parameters.containsKey(parameterName)) {
				parameterForName = (List)parameters.get(parameterName);
			}
			else {
				parameterForName = new ArrayList();
				parameters.put(parameterName, parameterForName);
			}
			parameterForName.add(initParamsForClass[i]);
		}

		// Get the security policy from the type
		ObjRef 		typeRef = resolveTypeLink(realxarch, brickRef);
		ISecureConnector		iConnector = null;
		ISecureComponent		iComponent = null;
		ISecureConnectorType	iConnectorType = null;
		ISecureComponentType	iComponentType = null;
		ISecurityPropertyType	iSecurityType = null;
		if(xarchBulk.isInstanceOf(brickRef, "edu.uci.isr.xarch.security.ISecureConnector")) {
			iConnector = (ISecureConnector)XArchFlatProxyUtils.
								proxy(realxarch, brickRef);
			iConnectorType	= (ISecureConnectorType)XArchFlatProxyUtils.
								proxy(realxarch, typeRef);
			iSecurityType = iConnectorType.getSecurity();
		}
		if(xarchBulk.isInstanceOf(brickRef, "edu.uci.isr.xarch.security.ISecureComponent")) { 
			iComponent = (ISecureComponent)XArchFlatProxyUtils.
								proxy(realxarch, brickRef);
			iComponentType = (ISecureComponentType)XArchFlatProxyUtils.
								proxy(realxarch, typeRef);
			iSecurityType = iComponentType.getSecurity();
		}
		
		if (iSecurityType != null) {
			getSecurityProperty(iSecurityType, parameters, IPolicy.TYPE_POLICY, true);
		}
		
		Map	interfaceSafeguards = new HashMap();
		Map	safeguardsPriorities = new HashMap();
		ObjRef[]	signatureRefs = new ObjRef[interfaceRefs.length];
		for (int i = 0; i<interfaceRefs.length; i++) {
			signatureRefs[i] = (ObjRef)xarchBulk.get(interfaceRefs[i], "signature");
			if (signatureRefs[i] != null) {
				ObjRef	signatureOnTypeRef = resolveXLink(realxarch, signatureRefs[i]);
				if (xarchBulk.isInstanceOf(signatureOnTypeRef, 
						"edu.uci.isr.xarch.security.ISecureSignature")) {
					ISecureSignature iSignature	= (ISecureSignature)XArchFlatProxyUtils.
							proxy(realxarch, signatureOnTypeRef);
					ISafeguards		iSafeguards = iSignature.getSafeguards();
					if (iSafeguards != null) {
						Collection	safeguards = iSafeguards.getAllSafeguards();
						interfaceSafeguards.put(interfaceIds[i], safeguards);
						safeguardsPriorities.put(interfaceIds[i], iSafeguards.getPriority());
					}
				}
			}
		}
		
		Map		parametersForStructure = new HashMap();
		if(iConnector != null || iComponent != null) {
			// Get the regular parameters
			Collection params = null;
			if (iConnector != null) {
				params = iConnector.getAllInitializationParameters();
			}
			else {
				params = iComponent.getAllInitializationParameters();
			}
			for(Iterator ii = params.iterator(); ii.hasNext(); ){
				IInitializationParameterStructure iips = (IInitializationParameterStructure)ii.next();
				String	priority = iips.getPriority();
				String	name = iips.getName();
				String  value = iips.getValue();
				List	l = new ArrayList();
				l.add(new InitializationParameter(name, value));
				parametersForStructure.put(name, new ParameterWithPriority(name, l, priority));
			}

			// This is to pack security related information, including subject,
			//  principals, privileges, policy, and safeguards
			//	into the initialization parameter framework, thus the creation
			//  process of the architecture controller does not have to be modified
			ISecurityPropertyType	iSecurity = null;
			if (iConnector != null) {
				iSecurity = iConnector.getSecurity();
			}
			else {
				iSecurity = iComponent.getSecurity();
			}
			if (iSecurity != null) {
				getSecurityProperty(iSecurity, parametersForStructure, 
						IPolicy.STRUCTURE_POLICY, false);
			}
			// get safeguards
			for (int k = 0; k<interfaceRefs.length; k++) {
				if (xarchBulk.isInstanceOf(interfaceRefs[k], 
						"edu.uci.isr.xarch.security.ISecureInterface")) {
					ISecureInterface iInterface = (ISecureInterface)
						XArchFlatProxyUtils.proxy(realxarch, interfaceRefs[k]);
					ISafeguards		iSafeguards = iInterface.getSafeguards();
					if (iSafeguards != null) {
						Collection	safeguards = iSafeguards.getAllSafeguards();
						interfaceSafeguards.put(interfaceIds[k], safeguards);
						safeguardsPriorities.put(interfaceIds[k], iSafeguards.getPriority());
					}
				}
			}
			for (Iterator ii = interfaceSafeguards.keySet().iterator(); ii.hasNext(); ) {
				String	interfaceId = (String)ii.next();
				String	interfacePriority = (String)safeguardsPriorities.get(interfaceId);
				List	lSafeguardsForInterface = new ArrayList();
				Collection	safeguards = (Collection)interfaceSafeguards.get(interfaceId);
				for (Iterator is = safeguards.iterator(); is.hasNext();) {
					IPrivilege safeguard = (IPrivilege)is.next(); 
					lSafeguardsForInterface.add(new InitializationParameter(
						c2.fw.secure.IPrivilege.SAFEGUARD + interfaceId, 
						safeguard.getValue()));
				}
				parametersForStructure.put(c2.fw.secure.IPrivilege.SAFEGUARD + interfaceId, 
						new ParameterWithPriority(c2.fw.secure.IPrivilege.SAFEGUARD 
						+ interfaceId, lSafeguardsForInterface, interfacePriority));
			}
			// TODO: So far it's about structure of xADL, what about type and instance?
		}

		// Merge structure parameters with priorities into type parameters 
		for (Iterator ii = parametersForStructure.keySet().iterator(); ii.hasNext(); ) {
			List		parameterForName;
			boolean		add = true;
			String		name = (String)ii.next();
			ParameterWithPriority	pwp = (ParameterWithPriority)parametersForStructure.get(name);
			String		priority = pwp.priority;
			if (parameters.containsKey(name)) {
				parameterForName = (List)parameters.get(name);
				if (IParameterPriority.ENUM_REPLACE.equals(priority)) {
					// if replace, then use a new parameterForName
					parameters.remove(name);
					parameterForName = new ArrayList();
					parameters.put(name, parameterForName);
				}
				else if (IParameterPriority.ENUM_IGNORE.equals(priority)) {
					// if ignore, then not to add
					add = false;
				}
				// otherwise (specified as "append", 
				// or specified unrecognizable), we just append 
			}
			else {
				parameterForName = new ArrayList();
				parameters.put(name, parameterForName);
			}
			if (add)
				parameterForName.addAll(pwp.values);
		}
		
		// Pack all parameters into one array
		int		paramsLength = 0;
		for (Iterator i = parameters.keySet().iterator(); i.hasNext(); ) {
			paramsLength += ((List)parameters.get(i.next())).size();
		}
		InitializationParameter[]	initParams = new InitializationParameter[paramsLength];
		int		j = 0;
		for (Iterator i = parameters.keySet().iterator(); i.hasNext(); ) {
			List	l = (List)parameters.get(i.next());
			for (Iterator li = l.iterator(); li.hasNext(); ) {
				initParams[j++] = (InitializationParameter)li.next();
			}
		}

		String					addBrickRequest = "";
		if(xarchBulk.isInstanceOf(brickRef, "edu.uci.isr.xarch.security.ISecureConnector")) {
			addBrickRequest = XACMLUtils.createRequestForConnector(
			        XACMLUtils.SUBJECT_ID_SMS, iConnectorType.getId(), 
			        iConnector.getId(), XACMLUtils.ACTION_ADD_BRICK, initParams);
		}
		if(xarchBulk.isInstanceOf(brickRef, "edu.uci.isr.xarch.security.ISecureComponent")) { 
			addBrickRequest = XACMLUtils.createRequestForComponent(
			        XACMLUtils.SUBJECT_ID_SMS, iComponentType.getId(), 
			        iComponent.getId(), XACMLUtils.ACTION_ADD_BRICK, initParams);
		}
		
		SecurityProperties sp = topology.getSecurityProperties(brickRef);
		DynamicPDP brickPDP = AACC2Component.getPDP(sp);
		// It is possible to tie xarch as an attribute finder module for the PDP
		// But such flexibility is probably unnecessary. And even if the 
		// flexibility is needed, it is better to integrate it with BulkQuery
		// and Tron/XPath, which essentially gives the capability of "programming"
		// xArchADT. Here, we can just use what we have collected in the parameters
		// array. 
		
		// Check whether this brick is allowed to be added
		if (brickPDP != null && !addBrickRequest.equals("") && !brickPDP.evaluate(addBrickRequest)) {
		    throw new BrickSecurityException(brickId, XACMLUtils.ACTION_ADD_BRICK);
		}
		
		super.addBrick(brickId, brickDescription, initParams, brickRef, interfaceRefs, interfaceIds);
		
		// Get the newly created brick
		Brick b = manager.getBrick(brickId);
		if (b instanceof IPEP && brickPDP != null) {
		    // use the created PDP
		    ((IPEP)b).setPDP(brickPDP);
		}
	}
	
	private void getSecurityProperty(ISecurityPropertyType iSecurity,
			Map	parametersToPut, String policyType, boolean addRegularList) {
		// get subject
		ISubject subject = iSecurity.getSubject();
		if (subject != null) {
			List	lSubject = new ArrayList();
			lSubject.add(new InitializationParameter(
					c2.fw.secure.ISubject.SUBJECT, subject.getValue()));
			// ApiGen cannot generate a string with an attibute, 
			// so we cannot get call getPriority on iSubject
			if (addRegularList) {
				parametersToPut.put(c2.fw.secure.ISubject.SUBJECT, lSubject);
			}
			else {
				parametersToPut.put(c2.fw.secure.ISubject.SUBJECT,
						new ParameterWithPriority(c2.fw.secure.ISubject.SUBJECT, 
						lSubject, IParameterPriority.ENUM_REPLACE));
			}
		}
		// get all principals
		IPrincipals	iPrincipals = iSecurity.getPrincipals();
		if (iPrincipals != null) {
			Collection principals = iPrincipals.getAllPrincipals();
			List	lPrincipals = new ArrayList();
			for(Iterator ip = principals.iterator(); ip.hasNext(); ){
				lPrincipals.add(new InitializationParameter(
					c2.fw.secure.IPrincipal.PRINCIPAL, 
					((IPrincipal)ip.next()).getValue()));
			}
			if (addRegularList) {
				parametersToPut.put(c2.fw.secure.IPrincipal.PRINCIPAL, lPrincipals);
			}
			else {
				parametersToPut.put(c2.fw.secure.IPrincipal.PRINCIPAL, 
					new ParameterWithPriority(c2.fw.secure.IPrincipal.PRINCIPAL, 
							lPrincipals, iPrincipals.getPriority()));
			}
		}
		// get all privileges
		IPrivileges iPrivileges = iSecurity.getPrivileges();
		if (iPrivileges != null) {
			Collection privileges = iPrivileges.getAllPrivileges();
			List	lPrivileges = new ArrayList();
			for(Iterator ip = privileges.iterator(); ip.hasNext(); ){
				lPrivileges.add(new InitializationParameter(
						c2.fw.secure.IPrivilege.PRIVILEGE, 
						((IPrivilege)ip.next()).getValue()));
			}
			if (addRegularList) {
				parametersToPut.put(c2.fw.secure.IPrivilege.PRIVILEGE, lPrivileges);
			}
			else {
				parametersToPut.put(c2.fw.secure.IPrivilege.PRIVILEGE, 
					new ParameterWithPriority(c2.fw.secure.IPrivilege.PRIVILEGE, 
							lPrivileges, iPrivileges.getPriority()));
			}
		}
		// get policy
		IPolicies policies = iSecurity.getPolicies();
		if (policies != null) {
			for (Iterator pi = policies.getAllPolicySets().iterator(); pi.hasNext(); ) {
				IPolicySetType policyElement = (IPolicySetType)pi.next();
				if (policyElement != null) {
					String policy = policyElement.getPolicy();
					if (policy != null) {
						List	lPolicy = new ArrayList();
						lPolicy.add(new InitializationParameter(
								policyType, policy));
						// Just use a "replac" priority. The policy itself 
						//	specifies the combination
						if (addRegularList) {
							parametersToPut.put(policyType, lPolicy);
						}
						else {
							parametersToPut.put(policyType, 
								new ParameterWithPriority(policyType, 
										lPolicy, IParameterPriority.ENUM_REPLACE));
						}
					}
				}
			}
		}
	}
	
	protected	Set	insecureLinkRefs = new HashSet();
	protected   Set rejectedLinkRefs = new HashSet();
	
	protected Weld bindLink(XArchFlatQueryInterface xarchBulk, ObjRef linkRef) throws InvalidArchitectureDescriptionException{
		Weld w = null;
		try {
			w = super.bindLink(xarchBulk, linkRef);
		}
		catch (IllegalArgumentException iae) {
			boolean		rethrow = false;
			// Is this related to a previously insecure brick?
			boolean	becauseOfInsecureBrick = false;
			String	reason = iae.getMessage();
			if (reason.indexOf("EndpointThing brick does not exist: ") != -1) {
				for (Iterator ibi = insecureBricks.iterator(); ibi.hasNext();) {
					String	insecureId = ((Identifier)ibi.next()).toString();
					if (reason.indexOf(insecureId) != -1) {
						becauseOfInsecureBrick = true;
					}
				}
			}
			if (!becauseOfInsecureBrick) {
			    rethrow = true;
			}
			else {
				// remove the link from xArchADT
				//realxarch.remove(xarchBulk.getParent(linkRef), 
				//	xarchBulk.getElementName(linkRef), linkRef);
				insecureLinkRefs.add(linkRef);
			}
			if (rethrow) {
			    throw iae;
			}
		}
		catch (WeldSecurityException wse) {
		    rejectedLinkRefs.add(linkRef);
		}
		return w;
	}
	
	public Set getInsecureLinkRefs() {
		return insecureLinkRefs;
	}
	
	public Set getRejectedLinkRefs() {
	    return rejectedLinkRefs;
	}
}

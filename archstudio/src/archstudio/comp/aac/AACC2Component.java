package archstudio.comp.aac;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.xacml.AbstractPolicy;

import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;
import archstudio.invoke.InvokableBrick;
import archstudio.invoke.InvokeMessage;
import archstudio.invoke.InvokeUtils;
import c2.fw.Identifier;
import c2.fw.Message;
import c2.fw.MessageProcessor;
import c2.fw.secure.xacml.DynamicPDP;
import c2.fw.secure.xacml.RBACHierarchicalWithXACML;
import c2.fw.secure.xacml.XACMLUtils;
import c2.legacy.AbstractC2DelegateBrick;
import c2.pcwrap.EBIWrapperUtils;
import c2.util.MessageSendProxy;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchEventProvider;
import edu.uci.ics.xarchutils.XArchFileEvent;
import edu.uci.ics.xarchutils.XArchFileListener;
import edu.uci.ics.xarchutils.XArchFlatEvent;
import edu.uci.ics.xarchutils.XArchFlatInterface;
import edu.uci.ics.xarchutils.XArchFlatListener;
import edu.uci.ics.xarchutils.XArchFlatProxyUtils;
import edu.uci.ics.xarchutils.XArchFlatQueryInterface;
import edu.uci.isr.xarch.security.IPolicies;
import edu.uci.isr.xarch.security.IPolicySetType;
import edu.uci.isr.xarch.security.IPrivilege;
import edu.uci.isr.xarch.security.ISafeguards;
import edu.uci.isr.xarch.security.ISecureInterface;
import edu.uci.isr.xarch.security.ISecureSignature;
import edu.uci.isr.xarch.security.ISecurityPropertyType;

public class AACC2Component extends AbstractC2DelegateBrick implements
		InvokableBrick {
	public static final String SERVICE_NAME = "Analysis Tools/Architectural Access Control";

	protected XArchFlatTransactionsInterface realxarch = null;
	protected XArchFlatQueryInterface xarch = null;
	
	protected MessageSendProxy requestProxy = new MessageSendProxy(this,
			topIface);

	protected MessageSendProxy notificationProxy = new MessageSendProxy(this,
			bottomIface);

	public AACC2Component(Identifier id) {
		super(id);
		realxarch = (XArchFlatTransactionsInterface) EBIWrapperUtils
				.addExternalService(this, topIface,
						XArchFlatTransactionsInterface.class);

		addLifecycleProcessor(new AACLifecycleProcessor());
		addMessageProcessor(new AACMessageProcessor());

		XArchEventProvider xarchEventProvider = (XArchEventProvider) EBIWrapperUtils
				.createStateChangeProviderProxy(this, topIface,
						XArchEventProvider.class);

		XArchFileListener fileListener = new XArchFileListener() {
			public void handleXArchFileEvent(XArchFileEvent evt) {
				handleFileEvent(evt);
			}
		};
		xarchEventProvider.addXArchFileListener(fileListener);

		XArchFlatListener flatListener = new XArchFlatListener() {
			public void handleXArchFlatEvent(XArchFlatEvent evt) {
				handleFlatEvent(evt);
			}
		};
		xarchEventProvider.addXArchFlatListener(flatListener);

		InvokeUtils
				.deployInvokableService(this, bottomIface,
						SERVICE_NAME,
						"An Architectural Access Control Analysis Tool for ArchStudio 3");
	}

	public void handleFileEvent(XArchFileEvent evt) {
	}

	public void handleFlatEvent(XArchFlatEvent evt) {
	}

	class AACLifecycleProcessor extends c2.fw.LifecycleAdapter {
		public void begin() {
		}

		public void end() {
			closeWindow();
		}
	}

	public void newWindow() {
		// This makes sure we only have one active window open.
	}

	public void closeWindow() {
	}

	public void invoke(InvokeMessage m) {
		// Let's open a new window.
		//newWindow();
		if(m.getServiceName().equals(SERVICE_NAME)){
			String	url = m.getArchitectureURL();
			if (url != null) {
				ObjRef xArchRef = realxarch.getOpenXArch(url);
				new ArchitecturalTopology(realxarch, xArchRef, 
						ArchitecturalTopology.TOP_DOWN | ArchitecturalTopology.BOTTOM_UP);
			}
		}
	}

	/**
	 * Check whether one interface can access another interface
	 * 
	 * @param xarch				the query interface
	 * @param xArchRef			the architectural reference
	 * @param fromInterface		the accessing interface
	 * @param toInterface		the accessed interface
	 * @return true if the brick containing the accessing interface has sufficient privileges
	 * to access the accessed interface, false otherwise
	 */
	public static boolean checkAccessControl(XArchFlatInterface xarch, 
			ObjRef xArchRef, ObjRef fromInterface, 
			ObjRef toInterface) {
		String		from = XadlUtils.getID(xarch, fromInterface);
		if (from == null)
			return false;
		
		String		to = XadlUtils.getID(xarch, toInterface);
		if (to == null)
			return false;
		
		return checkAccessControl(xarch, xArchRef, from, to);
	}
	
	/**
	 * Check whether one interface can access another interface
	 * 
	 * @param xarch				the query interface
	 * @param xArchRef			the architectural reference
	 * @param fromInterface		the accessing interface
	 * @param toInterface		the accessed interface
	 * @return true if the brick containing the accessing interface has sufficient privileges
	 * to access the accessed interface, false otherwise
	 */
	public static boolean checkAccessControl(XArchFlatInterface xarch, 
			ObjRef xArchRef, String fromInterface, 
			String toInterface) {
		// Get safeguards of the accessed interface 
		Set	safeguards = getSafeguards(xarch, xArchRef, toInterface);
		if (safeguards == null || safeguards.isEmpty())
			// if not protected, then everybody can access
			return true;
		
	    // Get the graph
	    ArchitecturalTopology at = new ArchitecturalTopology(xarch, xArchRef, 
	    		ArchitecturalTopology.TOP_DOWN | ArchitecturalTopology.BOTTOM_UP);
		Set	privileges = at.getPrivileges(fromInterface);
		if (privileges == null || privileges.isEmpty())
			// if no privileges, then cannot access
			return false;
		
		// Get privileges of the brick of the accessing interface
	    boolean result = false;

	    // Get the path that might change the privilegs
		List	path = at.getPath(fromInterface, toInterface);
		if (path == null)
			// if not reachable, then deny the access
			return false;
		
		String	previousInterface = null;
        for (Iterator k = path.iterator(); k.hasNext(); ) {
            String		currentInterface = (String)k.next();
            propagatePrivileges(privileges, previousInterface, currentInterface, 
            		xarch, xArchRef, at, path);
            previousInterface = currentInterface;
        }
        // if the privileges contains the safeguards, access would be granted
        if (privileges != null && safeguards != null && privileges.containsAll(safeguards))
        	result = true;
	    return result;
	}
	
	/**
	 * Get the safeguards for an interface.
	 * 
	 * @param xarch				the query interface
	 * @param xArchRef			the architectural reference
	 * @param interfaceName		the name of the interface
	 * @return the safeguards that protect the interface. Null if there is no safeguards
	 */
	public static Set getSafeguards(XArchFlatInterface xarch, ObjRef xArchRef, String interfaceId) {
	    ObjRef	interfaceRef = xarch.getByID(xArchRef, interfaceId);
	    if (interfaceRef == null)
	        return null;
	    return getSafeGuards(xarch, xArchRef, interfaceRef);
	}
	
	/**
	 * Get the safeguards for an interface. If no safeguards for the specific interface, 
	 * try to return the signature of the interface. 
	 * 
	 * @param xarch				the query interface
	 * @param xArchRef			the architectural reference
	 * @param interfaceRef		the reference of the interface
	 * @return the safeguards that protect the interface. Null if there is no safeguards
	 */
	public static Set getSafeGuards(XArchFlatInterface xarch, ObjRef xArchRef, ObjRef interfaceRef) {
	    // check interface first, then check the signature of the interface
	    Collection result = null;
        if (xarch.isInstanceOf(interfaceRef,
                "edu.uci.isr.xarch.security.ISecureInterface")) {
            // assume a secure interface. Possible to support mix things, 
            // but easier to code for one type of interfaces
            ISecureInterface iInterface = (ISecureInterface) 
            			XArchFlatProxyUtils.proxy(xarch, interfaceRef);
            ISafeguards iSafeguards = iInterface.getSafeguards();
            if (iSafeguards != null) {
                result = iSafeguards.getAllSafeguards();
            }
            else {
                // found no safeguards for this interface, try the signature of this interface
    			ObjRef signatureRef = (ObjRef)xarch.get(interfaceRef, "signature");
    			if (signatureRef != null) {
    			    try {
        				ObjRef	signatureOnTypeRef = XadlUtils.resolveXLink(xarch, xArchRef, signatureRef);
        				if (xarch.isInstanceOf(signatureOnTypeRef, 
        						"edu.uci.isr.xarch.security.ISecureSignature")) {
        					ISecureSignature iSignature	= (ISecureSignature)XArchFlatProxyUtils.
        							proxy(xarch, signatureOnTypeRef);
        					iSafeguards = iSignature.getSafeguards();
        					if (iSafeguards != null) {
        					    result = iSafeguards.getAllSafeguards(); 
        					}
        				}
    			    }
    			    catch (Exception e) {
    			        e.printStackTrace();
    			    }
        		}
            }
        }
        // Get the string for privileges and return them
        Set	s = null;
        if (result != null) {
            s = new HashSet();
			for (Iterator i = result.iterator(); i.hasNext(); ) {
			    IPrivilege safeguard = (IPrivilege)i.next();
			    s.add(safeguard.getValue());
			}
        }
        return s;
	}
	
	/**
	 * Propagate a set of privileges from one interface to another. These two interfaces are either
	 * conntected by a link, in which case no change would be made to the privileges, or are 
	 * interfaces of one brick, in which case the brick decides what privileges can be removed,
	 * or the privileges can be completely replaced.
	 * 
	 * @param privilges			the set of privileges to propagate
	 * @param fromInterface		the starting interface
	 * @param toInterface		the ending interface
	 * @param xarch				the query interface
	 * @param xArchRef			the architectural reference
	 * @param at				the architectural topology
	 * @param path				the larger context of the propagation. 
	 */
	public static void propagatePrivileges(Set privileges, String fromInterface, 
			String toInterface, XArchFlatInterface xarch, ObjRef xArchRef, 
			ArchitecturalTopology at, List path) {
		// no changes
		if (fromInterface == null || toInterface == null)
			return;

        ObjRef fromBrick = XadlUtils.getBrickForInterface(xarch, fromInterface);
        ObjRef toBrick = XadlUtils.getBrickForInterface(xarch, toInterface);
        if (fromBrick.equals(toBrick)) {
            // do interface propagation based on brick characters.
    		// Here a brick can modify the privileges in any way it wants/implements
    		// Default it does not change anything
        	SecurityProperties	sp = at.getSecurityProperties(toBrick);
        	Set		brickPrivileges = ArchitecturalTopology.getPrivileges(sp);
        	if (brickPrivileges != null && !brickPrivileges.isEmpty()) {
        		// The brick specifies privileges, then we use this set to replace the old set
        		privileges.clear();
        		privileges.addAll(brickPrivileges);
        	}
        	else {
        		// Iterator through the privileges to see whether any of them 
        		// 	has been removed by this brick
        		// This is a question that is easier answered by using XACML,
        		//  since it asks whether an item should get an yes or no. 
        		// Its purpose is for denial, so separating it from the privileges
        		// (which is to grant) is also appropriate.
        		DynamicPDP	pdp = getPDP(sp);
        		if (sp != null && pdp != null) {
        			// package the fromInterface/privilege/toInterface as subject/action/resource
        			// TODO: document it in the language 
        			// to see whether the policy would deny its propagation
					Set toRemove = new HashSet();
					for (Iterator i = privileges.iterator(); i.hasNext();) {
						String	privilege = (String)i.next();
						String	request = XACMLUtils.createRequest(fromInterface, toInterface, privilege);
						if (!pdp.evaluate(request)) {
							// This privilege is denied by the policy, 
							//	which means it should not propagate to the next point
							toRemove.add(privilege);
						}
					}
					privileges.removeAll(toRemove);
        		}
        	}
        }
    	// No change on the privileges along a normal semantics-less link between bricks
	}
	
	public static DynamicPDP getPDP(SecurityProperties sp) {
	    Logger logger1 = Logger.getLogger("com.sun.xacml.finder.AttributeFinder");	    
	    Logger logger2 = Logger.getLogger("com.sun.xacml.finder.PolicyFinder");
	    logger1.setLevel(Level.OFF);
	    logger2.setLevel(Level.OFF);
	    
		SecurityPolicies	policy = getPolicy(sp);
		Set currentPolicy = new HashSet();
		Set potentialPolicy = new HashSet();
		Set rbacPolicy = new HashSet();
		getCurrentAndPotentialPolicies(policy, currentPolicy, potentialPolicy, rbacPolicy);
		DynamicPDP pdp = null;
		if (!currentPolicy.isEmpty()) {
		    pdp = new DynamicPDP(currentPolicy, potentialPolicy);
			if (!rbacPolicy.isEmpty()) {
				RBACHierarchicalWithXACML	rbac = new RBACHierarchicalWithXACML(rbacPolicy);
			    pdp.setSecondaryRBAC(rbac);
			}
		}
		return pdp;
	}
	
	/**
	 * Get the set of security policies
	 * @param sp the security properties (including subject/principal/privilege/policy)
	 * @return the set of policies
	 */
	public static SecurityPolicies	getPolicy(SecurityProperties sp) {
		SecurityPolicies	result = new SecurityPolicies();
		if (sp != null) {
			if (sp.brick != null)
				getPolicy(sp.brick.getPolicies(), result.brickPolicy);
			if (sp.brickType != null)
				getPolicy(sp.brickType.getPolicies(), result.typePolicy);
			if (sp.containers != null) {
				for (Iterator i = sp.containers.iterator(); i.hasNext(); ) {
					ISecurityPropertyType iSecurity = (ISecurityPropertyType)i.next();
					Set		policy = new HashSet();
					getPolicy(iSecurity.getPolicies(), policy);
					result.containerPolicies.add(policy);
				}
			}
		}
		return result;
	}

	/**
	 * Collect all policies into a set of strings
	 * 
	 * @param policies			the policies
	 * @param policyStrings		the set of strings for the policies
	 */
	public static void getPolicy(IPolicies policies, Set policyStrings) {
		if (policies == null)
			return;
		
		for (Iterator i= policies.getAllPolicySets().iterator(); i.hasNext();) {
    		IPolicySetType	iPolicy = (IPolicySetType)i.next();
    		String 			policy = iPolicy.getPolicy();
    		if (policy != null)
    			policyStrings.add(policy);
		}
	}
	
	/**
	 * Collect the set of policies into a current set and a potential set for DynamicPDP.
	 * In the order of brick, brick type, brick container, and global policies, the first
	 * non empty set becomes the current set, the remaining sets become the potential set.
	 * 
	 * @param sp			the security properties
	 * @param current		the current policy, probably structure policy
	 * @param potential		the potential policy, probably type/container
	 */
	public static void getCurrentAndPotentialPolicies(SecurityPolicies sp, Set current, 
	        Set potential, Set rbac) {
		if (!sp.brickPolicy.isEmpty()) {
			current.addAll(sp.brickPolicy);
			potential.addAll(sp.typePolicy);
			for (Iterator i = sp.containerPolicies.iterator(); i.hasNext();) {
			    Set s = (Set)i.next();
				potential.addAll(s);
			}
		}
		else if (!sp.typePolicy.isEmpty()) {
			current.addAll(sp.typePolicy);
			for (Iterator i = sp.containerPolicies.iterator(); i.hasNext();) {
			    Set s = (Set)i.next();
				potential.addAll(s);
			}
		}
		else if (!sp.containerPolicies.isEmpty()) {
			for (Iterator i = sp.containerPolicies.iterator(); i.hasNext();) {
			    Set s = (Set)i.next();
				current.addAll(s);
			}
		}
		// Get all RBAC related policies, and move PPS from current to potential 
		Set		all = new HashSet(current);
		all.addAll(potential);
		for (Iterator i = all.iterator(); i.hasNext(); ) {
		    String	p = (String)i.next();
		    AbstractPolicy ap = XACMLUtils.getPolicy(p);
		    if (RBACHierarchicalWithXACML.isRBAC(ap)) {
		        rbac.add(p);
		    }
		    if (RBACHierarchicalWithXACML.isPPS(ap) && current.contains(p)) {
		        current.remove(p);
		        potential.add(p);
		    }
		}
		// Move all rbac policies to rbac
		// This is not ideal, but the real issue is how to do PolicySetCombine,
		// because RBAC uses policySet, and it needs to be combined with others 
		current.removeAll(rbac);
		potential.removeAll(rbac);
		// This also assumes RBAC would not be the only policy, otherwise
		// current might be empty
	}
	
	class AACWindowAdapter extends WindowAdapter {
		public void windowClosing(WindowEvent we) {
			closeWindow();
		}
	}

	class AACMessageProcessor implements MessageProcessor {
		public synchronized void handle(Message m) {
		}
	}

	public static class SecurityProperties {
		public ISecurityPropertyType		brick;
		public ISecurityPropertyType		brickType;
		public List							containers;
		
		public boolean	hasSecurity() {
			return brick != null || brickType != null || 
			(containers != null && !containers.isEmpty());
		}
	}
	
	public static class SecurityPolicies {
		public Set		brickPolicy;
		public Set		typePolicy;
		public List		containerPolicies;
		
		public SecurityPolicies() {
			brickPolicy = new HashSet();
			typePolicy = new HashSet();
			containerPolicies = new ArrayList();
		}
		
		public boolean hasPolicy() {
			for (Iterator i = containerPolicies.iterator(); i.hasNext(); ) {
				Set		s = (Set)i.next();
				if (!s.isEmpty())
					return true;
			}
			return !brickPolicy.isEmpty() || typePolicy.isEmpty();
		}
	}
	
}

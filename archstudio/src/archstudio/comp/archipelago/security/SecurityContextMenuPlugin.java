package archstudio.comp.archipelago.security;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import xacmleditor.PolicyEditorPanel;
import archstudio.comp.aac.AACC2Component;
import archstudio.comp.archipelago.ArchipelagoFrame;
import archstudio.comp.archipelago.ThingIDMap;
import archstudio.comp.archipelago.types.BrickTypeThing;
import archstudio.comp.archipelago.types.ComponentThing;
import archstudio.comp.archipelago.types.ComponentTypeThing;
import archstudio.comp.archipelago.types.ConnectorThing;
import archstudio.comp.archipelago.types.ConnectorTypeThing;
import archstudio.comp.archipelago.types.InterfaceThing;
import archstudio.comp.archipelago.types.InterfaceTypeThing;
import archstudio.comp.archipelago.types.SignatureThing;
import archstudio.comp.booleannotation.IBooleanNotation;
import c2.fw.secure.IPolicy;
import c2.fw.secure.IPrincipal;
import c2.fw.secure.IPrivilege;
import c2.fw.secure.ISubject;
import c2.fw.secure.xacml.XACMLUtils;

import com.sun.xacml.AbstractPolicy;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.BoxThing;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.contextmenu.AbstractSelectionBasedContextMenuPlugin;
import edu.uci.ics.bna.swingthing.IResizableLocalBoxBounded;
import edu.uci.ics.bna.swingthing.SwingPanelThing;
import edu.uci.ics.bna.swingthing.SwingThingUtils;
import edu.uci.ics.widgets.WidgetUtils;
import edu.uci.ics.widgets.windowheader.WindowHeaderPanel;
import edu.uci.ics.widgets.windowheader.WindowHeaderPanelListener;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchFlatInterface;
import edu.uci.isr.xarch.security.IPolicySetType;

public class SecurityContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{
	protected ArchSecurityTreePlugin 		archSecurityTreePlugin;
	protected XArchFlatInterface 			xarch;
	protected ThingIDMap 					thingIDMap;
	protected IBooleanNotation 				bni;
	protected AccessControlMainMenuLogic	acmml;
	
	// We used thingIDMap to store the accessing and accessed interface.
	// It was a hack, and it worked, until we needed no cross subArchitectures
	// The problems were 1)thingIDMap cannot go across panes; 2) there was a 
	// deadlock, when right clicking in one pane, after setting just one interface
	// in another pane. 
	// So we now resort back to the Archipelago frame, just like in ArchEdit frame
	public SecurityContextMenuPlugin(BNAComponent c, ArchSecurityTreePlugin avtp,
	ThingIDMap thingIDMap, XArchFlatInterface xarch, IBooleanNotation bni, AccessControlMainMenuLogic acmml){
		super(c);
		this.archSecurityTreePlugin = avtp;
		this.thingIDMap = thingIDMap;
		this.xarch = xarch;
		this.bni = bni;
		this.acmml = acmml; 

		// Update the access control menu in the menu bar. This is necessary when
		// one interface is set, and we swtich to another archStructure
		if (acmml != null) {
			ObjRef accessingInterface = archSecurityTreePlugin.getAccessingInterface();
			if (accessingInterface != null)
				acmml.setAccessingInterface(XadlUtils.getDescription(xarch, accessingInterface));
			ObjRef accessedInterface = archSecurityTreePlugin.getAccessedInterface();
			if (accessedInterface != null)
				acmml.setAccessedInterface(XadlUtils.getDescription(xarch, accessedInterface));
		}
	}

	public JPopupMenu addToContextMenu(JPopupMenu currentContextMenu, Thing[] selectedThingSet, Thing thingUnderCursor){
		Thing t = null;
		if(selectedThingSet.length == 0){
			if(thingUnderCursor != null){
				t = thingUnderCursor;
			}
			else{
				return currentContextMenu;
			}
		}
		else if(selectedThingSet.length > 1){
			return currentContextMenu;
		}
		else{
			t = selectedThingSet[0];
		}
		
		if(t instanceof BrickTypeThing || t instanceof BoxThing ){
			if(currentContextMenu.getSubElements().length > 0){
				currentContextMenu.addSeparator();
			}
			BrickSecurityMenuItemSet nsmis = new BrickSecurityMenuItemSet(t);
			JMenuItem[] miArray = nsmis.getMenuItemSet();
			for(int i = 0; i < miArray.length; i++){
				currentContextMenu.add(miArray[i]);
			}
		}
		
		if(t instanceof InterfaceTypeThing || t instanceof SignatureThing || t instanceof InterfaceThing ){
			if(currentContextMenu.getSubElements().length > 0){
				currentContextMenu.addSeparator();
			}
			InterfaceSecurityMenuItemSet nsmis = new InterfaceSecurityMenuItemSet(t);
			JMenuItem[] miArray = nsmis.getMenuItemSet();
			for(int i = 0; i < miArray.length; i++){
				currentContextMenu.add(miArray[i]);
			}
		}
		
		return currentContextMenu;
	}

	protected void centerInComponent(IResizableLocalBoxBounded lbbt){
		Rectangle lbbtBoundingBox = lbbt.getLocalBoundingBox();
		
		int lbbtWidth = lbbtBoundingBox.width;
		int lbbtHeight = lbbtBoundingBox.height;
		
		Dimension bnaSize = getBNAComponent().getSize();
		
		int bnacx = bnaSize.width / 2;
		int bnacy = bnaSize.height / 2;
		
		int ulx = bnacx - (lbbtWidth / 2);
		int uly = bnacy - (lbbtHeight / 2);
		
		lbbtBoundingBox.setLocation(ulx, uly);
		lbbt.setLocalBoundingBox(lbbtBoundingBox);
	}

	class InterfaceSecurityMenuItemSet implements ActionListener{
		protected JMenuItem miToggleSecurity;
		protected JMenu mSafeguards;
		JMenuItem miAccessing;
		JMenuItem miAccessed;
		JMenuItem miCheck;
		JMenuItem miClear;
		ObjRef	  dummy = new ObjRef("dummy");
		
		public void handleSetAsAccessing() {
			// TODO: the most general case should consider the level of the 
			//	interface (and use prefix of containers, probably)
			ObjRef accessingInterface = thingIDMap.getXArchRef(t.getID());
			archSecurityTreePlugin.setAccessingInterface(accessingInterface);
			acmml.setAccessingInterface(XadlUtils.getDescription(xarch, accessingInterface));
			// This class is created for each menu, so it cannot retain status 
			//  across pop up menus. A static memeber is not ideal,
			//  so we hack the thingIDMap to store the two interfaces
			//thingIDMap.mapRefToID(accessingInterface, "accessingInterface");
			//if (exists("accessedInterface"))
			//	miCheck.setEnabled(true);
		}
			
		public void handleSetAsAccessed() {
			ObjRef accessedInterface = thingIDMap.getXArchRef(t.getID());
			archSecurityTreePlugin.setAccessedInterface(accessedInterface);
			acmml.setAccessedInterface(XadlUtils.getDescription(xarch, accessedInterface));
		}

		public void handleCheck() {
			ObjRef 		accessingInterface = archSecurityTreePlugin.getAccessingInterface();
			ObjRef 		accessedInterface = archSecurityTreePlugin.getAccessedInterface();
			boolean		result = AACC2Component.checkAccessControl(xarch, 
					xarch.getXArch(thingIDMap.getXArchRef(t.getID())), accessingInterface, accessedInterface);
			if (result) 
				JOptionPane.showMessageDialog(null, "Access is allowed");
			else
				JOptionPane.showMessageDialog(null, "Access is denied");
		}
		
		public void handleClearAccess() {
			miAccessing.setText("Set as Accessing");
			miAccessed.setText("Set as Accessed");
			miCheck.setEnabled(false);
			archSecurityTreePlugin.setAccessingInterface(null);
			archSecurityTreePlugin.setAccessedInterface(null);
		}
		
		protected boolean exists(String key) {
			boolean result = true;
			ObjRef r = thingIDMap.getXArchRef(key);
			if (r == null || r.equals(dummy))
				result = false;
			return result;
		}
		
		protected Thing t;
				
		public InterfaceSecurityMenuItemSet(Thing t) {
			this.t = t;
			miToggleSecurity = new JMenuItem("Promote to Security");
			miToggleSecurity.addActionListener(this);

			mSafeguards = new JMenu("Safeguards");
			mSafeguards.addActionListener(this);

			miAccessing = new JMenuItem("Set as Accessing");
			miAccessed = new JMenuItem("Set as Accessed");
			miCheck = new JMenuItem("Check Access Control");
			miClear = new JMenuItem("Clear");
			WidgetUtils.setMnemonic(miAccessing, 'A');
			WidgetUtils.setMnemonic(miAccessed, 'S');
			WidgetUtils.setMnemonic(miCheck, 'K');
			WidgetUtils.setMnemonic(miClear, 'C');
			miAccessing.addActionListener(this);
			miAccessed.addActionListener(this);
			miClear.addActionListener(this);
			miCheck.addActionListener(this);
			// This is once again a per interface structure, so we need to check 
			// before we disable the item
			ObjRef 		accessingInterface = archSecurityTreePlugin.getAccessingInterface();
			ObjRef 		accessedInterface = archSecurityTreePlugin.getAccessedInterface();
			if (accessingInterface==null || accessedInterface==null)
				miCheck.setEnabled(false);
			if (accessingInterface != null) {
				ObjRef d = (ObjRef)xarch.get(accessingInterface, "Description");
				String	at = XadlUtils.getID(xarch, accessingInterface);
				if (d != null) {
					String aa = (String)xarch.get(d, "Value");
					if (aa.length() != 0)
						at = aa;
				}
				miAccessing.setText("Accessing: " + at);
			}
			if (accessedInterface != null) {
				ObjRef d = (ObjRef)xarch.get(accessedInterface, "Description");
				String	at = XadlUtils.getID(xarch, accessedInterface);
				if (d != null) {
					String aa = (String)xarch.get(d, "Value");
					if (aa.length() != 0)
						at = aa;
				}
				miAccessed.setText("Accessed: " + at);
			}

			ObjRef eltRef = thingIDMap.getXArchRef(t.getID());
			if(eltRef == null){
				return;
			}

		    boolean isSecure = false;
			if(t instanceof InterfaceTypeThing){
				isSecure = xarch.isInstanceOf(eltRef, "edu.uci.isr.xarch.security.ISecureInterfaceType");
			}
			else if(t instanceof SignatureThing){
				isSecure = xarch.isInstanceOf(eltRef, "edu.uci.isr.xarch.security.ISecureSignature");
			}
			else if(t instanceof InterfaceThing){
				isSecure = xarch.isInstanceOf(eltRef, "edu.uci.isr.xarch.security.ISecureInterface");
			}
			
			if(!isSecure){
				mSafeguards.setEnabled(false);
			}
			else{
			    // do not allow removal
				// miToggleSecurity.setText("Remove Security");
			    miToggleSecurity.setEnabled(false);

				SecurityMenuItem smiSafeguard = new SecurityMenuItem("Add Safeguard...", 
						IPrivilege.SAFEGUARD, null);
				smiSafeguard.addActionListener(this);
				mSafeguards.add(smiSafeguard);

				ObjRef		safeguardsRef = (ObjRef)xarch.get(eltRef, "Safeguards");
		    	if (safeguardsRef != null) {
		    		ObjRef[]	safeguardRefs = xarch.getAll(safeguardsRef, "Safeguard");
		    		for (int i = 0; i<safeguardRefs.length; i++) {
		    			SecurityMenuItem	smi = new SecurityMenuItem(
		    				"Remove Safeguard " + (String)xarch.get(safeguardRefs[i], "value"),
		    				IPrivilege.SAFEGUARD, safeguardRefs[i]);
		    			smi.addActionListener(this);
		    			mSafeguards.add(smi);
		    		}
		    	}
			}
		}
		
		public JMenuItem[] getMenuItemSet(){
			return new JMenuItem[]{miToggleSecurity, mSafeguards, miAccessing, miAccessed, miCheck, miClear};
		}
		
		public void actionPerformed(ActionEvent evt){
			ObjRef eltRef = thingIDMap.getXArchRef(t.getID());
			Object src = evt.getSource();
			
			if(eltRef == null){
				JOptionPane.showMessageDialog(c, "Thing not mapped to xArch element.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if(evt.getSource() == miToggleSecurity){
				if (miToggleSecurity.getText().indexOf("Promote") != -1) {
					ObjRef xArchRef = xarch.getXArch(eltRef);
					ObjRef securityContextRef = xarch.createContext(xArchRef, "security");

					if(t instanceof InterfaceTypeThing){
						xarch.promoteTo(securityContextRef, "secureInterfaceType", eltRef);
					}
					else if(t instanceof SignatureThing){
						xarch.promoteTo(securityContextRef, "secureSignature", eltRef);
					}
					else if(t instanceof InterfaceThing){
						xarch.promoteTo(securityContextRef, "secureInterface", eltRef);
					}
				}
			}
			else if(((JMenuItem)src).getText().equals("Set as Accessing")){
				handleSetAsAccessing();
			}
			else if(((JMenuItem)src).getText().equals("Set as Accessed")){
				handleSetAsAccessed();
			}
			else if(((JMenuItem)src).getText().equals("Clear")){
				handleClearAccess();
			}
			else if(((JMenuItem)src).getText().equals("Check Access Control")){
				handleCheck();
			}
			else if(evt.getSource() instanceof SecurityMenuItem){
				SecurityMenuItem 	smi = (SecurityMenuItem)evt.getSource();
				//String				command = smi.getText();
				String 				smiType = smi.getItemType();
				ObjRef				thingRef = smi.getThingRef();
				ObjRef 				xArchRef = xarch.getXArch(eltRef);
				ObjRef 				securityContextRef = xarch.createContext(xArchRef, "security");

				ObjRef				refToAddValue = null;
				
				if(smiType.equals(IPrivilege.SAFEGUARD)){
					ObjRef	safeguardsRef = (ObjRef)xarch.get(eltRef, "Safeguards");
					if (safeguardsRef == null) {
						safeguardsRef = xarch.create(securityContextRef, 
											"Safeguards");
						xarch.set(eltRef, "Safeguards", safeguardsRef);
					}

					if (thingRef != null) {
						// to remove
						xarch.remove(safeguardsRef, IPrivilege.SAFEGUARD, thingRef);
					}
					else {
						refToAddValue = xarch.create(securityContextRef, 
								IPrivilege.PRIVILEGE);

						xarch.add(safeguardsRef, IPrivilege.SAFEGUARD, refToAddValue);
					}
				}
				if (refToAddValue != null) {
					SwingPanelThing descEditPanel = new SwingPanelThing();
					new SetValuePanel(descEditPanel, refToAddValue);
					
					getBNAComponent().getModel().addThing(descEditPanel);
					descEditPanel.setLocalBoundingBox(new Rectangle(0, 0, 300, 90));
						centerInComponent(descEditPanel);
				}
			}
		}
	}
	
	class BrickSecurityMenuItemSet implements ActionListener{
		protected JMenuItem miToggleSecurity;
		protected JMenu mSubject;
		protected JMenu mPrincipals;
		protected JMenu mPrivileges;
		protected JMenu mPolicies;
		
		protected Thing t;
				
		public BrickSecurityMenuItemSet(Thing t){
			this.t = t;
			miToggleSecurity = new JMenuItem("Promote to Security");
			miToggleSecurity.addActionListener(this);

			mSubject = new JMenu("Subject");
			mSubject.addActionListener(this);

			mPrincipals = new JMenu("Principals");
			mPrincipals.addActionListener(this);

			mPrivileges = new JMenu("Privileges");
			mPrivileges.addActionListener(this);
			
			mPolicies = new JMenu("Policies");
			mPolicies.addActionListener(this);

			ObjRef eltRef = thingIDMap.getXArchRef(t.getID());
			if(eltRef == null){
				return;
			}

		    boolean isSecure = false;
			if(t instanceof ComponentTypeThing){
				isSecure = xarch.isInstanceOf(eltRef, "edu.uci.isr.xarch.security.ISecureComponentType");
			}
			else if(t instanceof ConnectorTypeThing){
				isSecure = xarch.isInstanceOf(eltRef, "edu.uci.isr.xarch.security.ISecureConnectorType");
			}
			else if(t instanceof ComponentThing){
				isSecure = xarch.isInstanceOf(eltRef, "edu.uci.isr.xarch.security.ISecureComponent");
			}
			else if(t instanceof ConnectorThing){
				isSecure = xarch.isInstanceOf(eltRef, "edu.uci.isr.xarch.security.ISecureConnector");
			}
			
			if(!isSecure){
				mSubject.setEnabled(false);
				mPrincipals.setEnabled(false);
				mPrivileges.setEnabled(false);
				mPolicies.setEnabled(false);
			}
			else{
			    // Do not allow removal of security after addition
				// miToggleSecurity.setText("Remove Security");
			    miToggleSecurity.setEnabled(false);

				SecurityMenuItem smiSubject = new SecurityMenuItem("Set Subject...", 
							ISubject.SUBJECT, null);
				smiSubject.addActionListener(this);
				mSubject.add(smiSubject);
				SecurityMenuItem smiPrincipal = new SecurityMenuItem("Add Principal...", 
								IPrincipal.PRINCIPAL, null);
				smiPrincipal.addActionListener(this);
				mPrincipals.add(smiPrincipal);
				SecurityMenuItem smiPrivilege = new SecurityMenuItem("Add Privilege...", 
						IPrivilege.PRIVILEGE, null);
				smiPrivilege.addActionListener(this);
				mPrivileges.add(smiPrivilege);
				SecurityMenuItem smiPolicy = new SecurityMenuItem("Add Policy...", 
						IPolicy.POLICY, null);
				smiPolicy.addActionListener(this);
				mPolicies.add(smiPolicy);

				ObjRef	securityRef = (ObjRef)xarch.get(eltRef, "Security");
				if (securityRef != null) {
			    	ObjRef		subjectRef = (ObjRef)xarch.get(securityRef, ISubject.SUBJECT);
			    	String		subjectName = "[No Subject assigned]";
			    	if (subjectRef != null) {
			    		subjectName = (String)xarch.get(subjectRef, "value");
			    		smiSubject.setThingRef(subjectRef);
			    		smiSubject.setText("Clear Subject " + subjectName);
			    	}
			    	
			    	ObjRef		principalsRef = (ObjRef)xarch.get(securityRef, "Principals");
			    	if (principalsRef != null) {
			    		ObjRef[]	principalRefs = xarch.getAll(principalsRef, "Principal");
			    		for (int i = 0; i<principalRefs.length; i++) {
			    			SecurityMenuItem	smi = new SecurityMenuItem(
			    				"Remove Principal " + (String)xarch.get(principalRefs[i], "value"),
			    				IPrincipal.PRINCIPAL, principalRefs[i]);
			    			smi.addActionListener(this);
			    			mPrincipals.add(smi);
			    		}
			    	}

			    	ObjRef		privilegesRef = (ObjRef)xarch.get(securityRef, "Privileges");
			    	if (privilegesRef != null) {
			    		ObjRef[]	privilegeRefs = xarch.getAll(privilegesRef, "Privilege");
			    		for (int i = 0; i<privilegeRefs.length; i++) {
			    			SecurityMenuItem	smi = new SecurityMenuItem(
			    				"Remove Privilege " + (String)xarch.get(privilegeRefs[i], "value"),
			    				IPrivilege.PRIVILEGE, privilegeRefs[i]);
			    			smi.addActionListener(this);
			    			mPrivileges.add(smi);
			    		}
			    	}

			    	ObjRef		policiesRef = (ObjRef)xarch.get(securityRef, IPolicy.POLICIES);
			    	if (policiesRef != null) {
			    		ObjRef[]	policyRefs = xarch.getAll(policiesRef, IPolicy.POLICY_SET);
			    		for (int i = 0; i<policyRefs.length; i++) {
			    			String policy = (String)xarch.get(policyRefs[i], IPolicy.POLICY);
					    	if (!policy.equals("")) {
					    		AbstractPolicy ap = XACMLUtils.getPolicy(policy);
					    		if (ap != null) {
					    			String policyId = ap.getId().toString();
					    			// add remove policy entry
					    			SecurityMenuItem	smi = new SecurityMenuItem(
					    				"Remove Policy " + policyId, IPolicy.POLICY, policyRefs[i]);
					    			smi.addActionListener(this);
					    			mPolicies.add(smi);
					    			// add edit policy entry
					    			smi = new SecurityMenuItem(
					    				"Edit Policy " + policyId + " ...", IPolicy.POLICY, policyRefs[i]);
					    			smi.addActionListener(this);
					    			mPolicies.add(smi);
					    		}
					    	}
			    		}
			    	}
				}
				else {
					
				}
			}
		}
		
		public JMenuItem[] getMenuItemSet(){
			return new JMenuItem[]{miToggleSecurity, mSubject, mPrincipals, mPrivileges, mPolicies};
		}
		
		public void actionPerformed(ActionEvent evt){
			ObjRef eltRef = thingIDMap.getXArchRef(t.getID());
			if(eltRef == null){
				JOptionPane.showMessageDialog(c, "Thing not mapped to xArch element.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if(evt.getSource() == miToggleSecurity){
				if (miToggleSecurity.getText().indexOf("Promote") != -1) {
					ObjRef xArchRef = xarch.getXArch(eltRef);
					ObjRef securityContextRef = xarch.createContext(xArchRef, "security");

					if(t instanceof ComponentTypeThing){
						xarch.promoteTo(securityContextRef, "secureComponentType", eltRef);
					}
					else if(t instanceof ConnectorTypeThing){
						xarch.promoteTo(securityContextRef, "secureConnectorType", eltRef);
					}
					else if(t instanceof ComponentThing){
						if (!xarch.isInstanceOf(eltRef, "edu.uci.isr.xarch.security.IComponentParams")) {
							xarch.promoteTo(securityContextRef, "componentParams", eltRef);
						}
						xarch.promoteTo(securityContextRef, "secureComponent", eltRef);
					}
					else if(t instanceof ConnectorThing){
						if (!xarch.isInstanceOf(eltRef, "edu.uci.isr.xarch.security.IConnectorParams")) {
							xarch.promoteTo(securityContextRef, "connectorParams", eltRef);
						}
						xarch.promoteTo(securityContextRef, "secureConnector", eltRef);
					}
				}
				else if (miToggleSecurity.getText().indexOf("Remove") != -1) {
				    // We do not allow a removal after adding security
					// xarch.clear(eltRef, "security");
				}
			}
			else if(evt.getSource() instanceof SecurityMenuItem){
				SecurityMenuItem 	smi = (SecurityMenuItem)evt.getSource();
				String				command = smi.getText();
				String 				smiType = smi.getItemType();
				ObjRef				thingRef = smi.getThingRef();
				ObjRef				securityRef = (ObjRef)xarch.get(eltRef, "security");
				ObjRef 				xArchRef = xarch.getXArch(eltRef);
				ObjRef 				securityContextRef = xarch.createContext(xArchRef, "security");

				// whether we are editing a security policy
				boolean				editingPolicy = false;
				// if we are, what the policy was
				String				oldPolicy = ""; 

				if (securityRef == null) {
					securityRef = xarch.create(securityContextRef, 
										"SecurityPropertyType");
					xarch.set(eltRef, "security", securityRef);
				}
				ObjRef				refToAddValue = null;
				
				if(smiType.equals(ISubject.SUBJECT)){
					if (thingRef != null) {
						// to remove
						xarch.clear(securityRef, ISubject.SUBJECT);
					}
					else {
						refToAddValue = xarch.create(securityContextRef, 
								ISubject.SUBJECT);
						xarch.set(securityRef, ISubject.SUBJECT, refToAddValue);
					}
				}
				else if(smiType.equals(IPrincipal.PRINCIPAL)){
					ObjRef	principalsRef = (ObjRef)xarch.get(securityRef, "Principals");
					if (principalsRef == null) {
						principalsRef = xarch.create(securityContextRef, 
											"Principals");
						xarch.set(securityRef, "Principals", principalsRef);
					}

					if (thingRef != null) {
						// to remove
						xarch.remove(principalsRef, IPrincipal.PRINCIPAL, thingRef);
					}
					else {
						refToAddValue = xarch.create(securityContextRef, 
								IPrincipal.PRINCIPAL);

						xarch.add(principalsRef, IPrincipal.PRINCIPAL, refToAddValue);
					}
				}
				else if(smiType.equals(IPrivilege.PRIVILEGE)){
					ObjRef	privilegesRef = (ObjRef)xarch.get(securityRef, "Privileges");
					if (privilegesRef == null) {
						privilegesRef = xarch.create(securityContextRef, 
											"Privileges");
						xarch.set(securityRef, "Privileges", privilegesRef);
					}

					if (thingRef != null) {
						// to remove
						xarch.remove(privilegesRef, IPrivilege.PRIVILEGE, thingRef);
					}
					else {
						refToAddValue = xarch.create(securityContextRef, 
								IPrivilege.PRIVILEGE);

						xarch.add(privilegesRef, IPrivilege.PRIVILEGE, refToAddValue);
					}
				}
				else if(smiType.equals(IPolicy.POLICY)){
					ObjRef	policiesRef = (ObjRef)xarch.get(securityRef, "Policies");
					if (policiesRef == null) {
						policiesRef = xarch.create(securityContextRef, 
											"Policies");
						xarch.set(securityRef, "Policies", policiesRef);
					}

					if (thingRef != null) {
					    if (command.startsWith("Remove")) {
							// to remove
							xarch.remove(policiesRef, IPolicy.POLICY_SET, thingRef);
					    }
					    else {
					        // to edit an existing policy
					        refToAddValue = thingRef;
					        oldPolicy = (String)xarch.get(thingRef, IPolicy.POLICY);
					        editingPolicy = true;
					    }
					}
					else {
						refToAddValue = xarch.create(securityContextRef, 
								IPolicy.POLICY_XADL_NAME);
						xarch.set(refToAddValue, "Policy", IPolicySetType.EMPTY_POLICY);
						xarch.add(policiesRef, IPolicy.POLICY_SET, refToAddValue);
						editingPolicy = true;
					}
				}
				
				if (refToAddValue != null) {
					if (!editingPolicy) {
						SwingPanelThing descEditPanel = new SwingPanelThing();
						new SetValuePanel(descEditPanel, refToAddValue);
						
						getBNAComponent().getModel().addThing(descEditPanel);
						descEditPanel.setLocalBoundingBox(new Rectangle(0, 0, 300, 90));
						centerInComponent(descEditPanel);
					}
					else {
						SwingPanelThing descEditPanel = new SwingPanelThing();
						new BNAPolicyEditorPanel(descEditPanel, refToAddValue, oldPolicy);
						getBNAComponent().getModel().addThing(descEditPanel);
						descEditPanel.setLocalBoundingBox(new Rectangle(0, 0, 600, 400));
						centerInComponent(descEditPanel);
					}
				}
			}
		}

		class BNAPolicyEditorPanel extends JPanel implements WindowHeaderPanelListener, ActionListener{
			/**
			 * Comment for <code>serialVersionUID</code>
			 */
			private static final long serialVersionUID = 1L;
			
			protected Thing swingPanelThing;
		
			protected JButton bOK;
			protected PolicyEditorPanel policyPanel;

			protected ObjRef	theRef;
			
			protected String	oldPolicy;
		
			public BNAPolicyEditorPanel(SwingPanelThing swingPanelThing, ObjRef theRef, String oldPolicy) {
				super();
				this.swingPanelThing = swingPanelThing;
				this.theRef = theRef;
				this.oldPolicy = oldPolicy;
		
				this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
				
				WindowHeaderPanel whp = new WindowHeaderPanel("Edit Security Policy");
				whp.addWindowHeaderPanelListener(this);
				this.add(whp);
			
				policyPanel = new PolicyEditorPanel();
				this.add(policyPanel);
			
				bOK = new JButton("OK");
				bOK.addActionListener(this);
				this.add(bOK);

				swingPanelThing.setPanel(this);
				if (oldPolicy.equals(""))
				    updateTextField();
				else
				    policyPanel.setPolicy(oldPolicy);
			}
		
			protected void updateTextField(){
				policyPanel.setPolicy(IPolicySetType.EMPTY_POLICY);
			}
			
			protected void setNewValue(String newValue){
				xarch.set(theRef, IPolicy.POLICY, newValue);
			}

			public void closeButtonPressed(WindowHeaderPanel src){
				remove();
			}
			
			public void actionPerformed(ActionEvent evt){
				if(evt.getSource() == bOK){
					String newPolicy = policyPanel.getPolicy();
					remove();
					setNewValue(newPolicy);
				}
			}
			
			public void remove(){
				getBNAComponent().getModel().removeThing(swingPanelThing);
			}
		}
	}
	
	class SetValuePanel extends JPanel implements WindowHeaderPanelListener, ActionListener{
		/**
		 * Comment for <code>serialVersionUID</code>
		 */
		private static final long serialVersionUID = 1L;
		
		protected Thing swingPanelThing;
	
		protected JTextField tfDescription;
		protected JButton bOK;

		protected ObjRef	theRef;
	
		public SetValuePanel(SwingPanelThing swingPanelThing, ObjRef theRef){
			super();
			this.swingPanelThing = swingPanelThing;
			this.theRef = theRef;
	
			this.setLayout(new BorderLayout());
		
			WindowHeaderPanel whp = new WindowHeaderPanel("Input a value");
			whp.addWindowHeaderPanelListener(this);
		
			this.add("North", whp);
		
			tfDescription = new JTextField(15);
			tfDescription.addActionListener(this);
			SwingThingUtils.requestFocusOnDisplay(tfDescription);
		
			bOK = new JButton("OK");
			bOK.addActionListener(this);
		
			JPanel centerPanel = new JPanel();
			centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			centerPanel.add(tfDescription);
			centerPanel.add(bOK);
		
			this.add("Center", centerPanel);
			swingPanelThing.setPanel(this);
			updateTextField();
		}
	
		protected void updateTextField(){
			tfDescription.setText("");
		}
		
		protected void setNewValue(String newValue){
			xarch.set(theRef, "Value", newValue);
		}

		public void closeButtonPressed(WindowHeaderPanel src){
			remove();
		}
		
		public void actionPerformed(ActionEvent evt){
			if(evt.getSource() == bOK){
				String newDescription = tfDescription.getText();
				remove();
				setNewValue(newDescription);
			}
			if(evt.getSource() == tfDescription){
				bOK.doClick();
			}
		}
		
		public void remove(){
			getBNAComponent().getModel().removeThing(swingPanelThing);
		}
	}
	
	static class SecurityMenuItem extends JMenuItem{
		/**
		 * Comment for <code>serialVersionUID</code>
		 */
		private static final long serialVersionUID = 1L;

		protected String		itemType;
		protected ObjRef 		thingRef;
			
		public SecurityMenuItem(String name, String itemType, ObjRef variantRef){
			super(name);
			this.itemType = itemType;
			this.thingRef = variantRef;
		}
			
		public String getItemType(){
			return itemType;
		}
			
		public ObjRef getThingRef(){
			return thingRef;
		}
		
		public void setThingRef(ObjRef thingRef) {
			this.thingRef = thingRef;
		}
	}
}

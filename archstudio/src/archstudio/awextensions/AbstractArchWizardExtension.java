package archstudio.awextensions;

// C2 imports
import c2.fw.*;
import c2.legacy.*;
import c2.pcwrap.*;

// xArch utilities imports
import edu.uci.isr.xarch.*;
import edu.uci.ics.xarchutils.*;

// Java imports
// import java.util.*;

/**
 * An abstract base class that implements base functionality for ArchWizard
 * extension components.  These components monitor the architecture for
 * specific events that take place during editing, and perform the programmed
 * responce.  Most often used to automate certain repetitive tasks associated
 * with the editing of architectural descriptions.
 *
 * @author John C. Georgas. <A HREF="mailto:jgeorgas@ics.uci.edu>jgeorgas@ics.uci.edu</A>
 *
 * Copyright 2003, by the University of California, Irvine.
 * ALL RIGHTS RESERVED.
 */
public abstract class AbstractArchWizardExtension extends AbstractC2DelegateBrick {
	// Class variables
	protected XArchFlatInterface xarch;
	private boolean active = false;
	
	/**
	 * Emits a message notifying of it's existence on its initialization.
	 */
	class ArchWizardExtensionLifecycleProcessor extends LifecycleAdapter {
		public void begin() {
			sendStatus();
		}
		
		public void end() {
			ArchWizardExtensionStatusMessage m = new ArchWizardExtensionStatusMessage(getIdentifier(), getDescription(), ArchWizardExtensionStatusListing.STATUS_UNAVAILABLE);
			sendNotification(m);
		}
	}
	
	/**
	 * Handles messages from XArchADT having to do with the state of the 
	 * architectural description having been modified.
	 */
	class StateChangeMessageProcessor implements MessageProcessor {
		public void handle(Message m) {
			if (m instanceof NamedPropertyMessage) {
				NamedPropertyMessage npm = (NamedPropertyMessage)m;
				if (npm.hasParameter("stateChangeMessage")) {
					if (npm.getBooleanParameter("stateChangeMessage")) {
						Object eo = npm.getParameter("paramValue0");
						if (eo instanceof XArchFlatEvent) {
							XArchFlatEvent e = (XArchFlatEvent)eo;
							handleStateChangeEvent(e);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Handles messages having to do with the status of this extension component.
	 */
	class ArchWizardExtensionStatusMessageProcessor implements MessageProcessor {
		public void handle(Message m) {
			if (m instanceof ArchWizardExtensionGetStatusMessage) {
				ArchWizardExtensionGetStatusMessage gsm = (ArchWizardExtensionGetStatusMessage)m;
				Identifier[] ids = gsm.getExtensionIDs();
				if ((ids == null) || (ids.length == 0)) {
					sendStatus();
				} else {
					for (int i = 0; i < ids.length; i++) {
						if (ids[i].equals(getIdentifier())) {
							sendStatus();
						}
					}
				}	
			} else if (m instanceof ArchWizardExtensionSetStatusMessage) {
				ArchWizardExtensionSetStatusMessage ssm = (ArchWizardExtensionSetStatusMessage)m;
				Identifier[] ids = ssm.getExtensionIDs();
				for (int i = 0; i < ids.length; i++) {
					if (ids[i].equals(getIdentifier())) {
						int status = ssm.getNewStatus();
						if (status == ArchWizardExtensionStatusListing.STATUS_ACTIVE) {
							setActive(true);
						} else if (status == ArchWizardExtensionStatusListing.STATUS_INACTIVE) {
							setActive(false);
						}
					}
				}
				sendStatus();
			}
		}
	}
	
	/**
	 * Creates a new extension component with the given id.
	 * @param id The <CODE>Identifier</CODE> of this brick.
	 */
	 public AbstractArchWizardExtension(Identifier id) {
		super(id);
		xarch = (XArchFlatInterface)EBIWrapperUtils.addExternalService(this, topIface, XArchFlatInterface.class);
		addMessageProcessors();
		addLifecycleProcessors();
	}
	
	/**
	 * Adds <CODE>MessageProcessor</CODE>s to this brick.
	 */
	protected void addMessageProcessors() {
		this.addMessageProcessor(new StateChangeMessageProcessor());
		this.addMessageProcessor(new ArchWizardExtensionStatusMessageProcessor());
	}
	
	/**
	 * Adds <CODE>LifecycleProcessor</CODE>s to this brick.
	 */
	protected void addLifecycleProcessors() {
		this.addLifecycleProcessor(new ArchWizardExtensionLifecycleProcessor());
	}
	
	/**
	 * Sets the activity of this extension component.
	 * @param a The activity; <CODE>true</CODE> if active, <CODE>false</CODE> otherwise.
	 */
	protected void setActive(boolean a) {
		active = a;
	}
	
	/**
	 * Indicates whether this component is active or not.
	 * @return <CODE>true</CODE> if active, <CODE>false</CODE> otherwise.
	 */
	protected boolean isActive() {
		return active;
	}
	
	/**
	 * Sends out an <CODE>ArchWizardExtensionStatusMessage</CODE> with this
	 * extension components status.
	 */
	protected void sendStatus() {
		int status = ArchWizardExtensionStatusListing.STATUS_INACTIVE;
		if (active) {
			status = ArchWizardExtensionStatusListing.STATUS_ACTIVE;
		}
		ArchWizardExtensionStatusMessage m = new ArchWizardExtensionStatusMessage(getIdentifier(), getDescription(), status);
		sendNotification(m);
	}
	
	/**
	 * Called with a XArchFlatEvent is detected.
	 * @param evt The <CODE>XArchFlatEvent</CODE> to handle.
	 */
	protected abstract void handleStateChangeEvent(XArchFlatEvent evt);
	
	/**
	 * Get the description of this extension component.
	 * @return The <CODE>String</CODE> description of this extension component.
	 */
	protected abstract String getDescription();
}
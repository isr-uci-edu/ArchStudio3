package archstudio.critics;

import c2.fw.*;
import c2.legacy.*;
import c2.pcwrap.*;

//Imported to support xArchADT use
import edu.uci.ics.xarchutils.*;

import java.util.*;

/**
 * This is an abstract base class for bricks that are ArchStudio 3 critics.
 * ArchStudio 3 critics watch the architecture for changes and make suggestions
 * about how to improve it or correct errors in the architecture description.
 * Critic classes should generally extend this class unless they have a really
 * good reason not to.
 * @author Eric M. Dashofy, <A HREF="mailto:edashofy@ics.uci.edu">edashofy@ics.uci.edu</A>
 */
public abstract class AbstractCritic extends AbstractC2DelegateBrick{

	/** The local proxy for making calls to xArchADT */
	protected XArchFlatInterface xarch;
	private boolean active = false;
	private int busyCount = 0;
	
	/**
	 * Create a new critic with the given identifier.
	 * @param id Identifier of this brick.
	 */
	public AbstractCritic(Identifier id){
		super(id);
		xarch = (XArchFlatInterface)EBIWrapperUtils.addExternalService(this, topIface, XArchFlatInterface.class);
		this.addMessageProcessor(new CriticStatusMessageProcessor());
		this.addLifecycleProcessor(new BasicCriticLifecycleProcessor());
		
		XArchEventProvider xarchEventProvider = 
			(XArchEventProvider)EBIWrapperUtils.createStateChangeProviderProxy(this, topIface,
			XArchEventProvider.class);
		
		XArchFileListener fileListener = new XArchFileListener(){
			public void handleXArchFileEvent(XArchFileEvent evt) {
				handleFileEvent(evt);
			}
		};
		xarchEventProvider.addXArchFileListener(fileListener);
		
		XArchFlatListener flatListener = new XArchFlatListener(){
			public void handleXArchFlatEvent(XArchFlatEvent evt){
				handleXArchEvent(evt);
			}
		};
		xarchEventProvider.addXArchFlatListener(flatListener);
	}
	
	/**
	 * Report an issue with an open architecture.  This issue will remain open
	 * until the architecture document is closed or the issue is removed.
	 * @param ci Issue to report.
	 */
	protected void addIssue(CriticIssue ci){
		sendToAll(new CriticIssueMessage(ci, CriticIssueMessage.ISSUE_OPEN), topIface);
	}
	
	/**
	 * Un-report an issue with an open architecture.  The issue may match by
	 * content rather than passing the same object.  Removing an issue
	 * that does not exist is simply a no-op.
	 * @param ci Issue to un-report.
	 */
	protected void removeIssue(CriticIssue ci){
		sendToAll(new CriticIssueMessage(ci, CriticIssueMessage.ISSUE_CLOSED), topIface);
	}

	/**
	 * Reports a full set of open issues with all the open architectures, replacing
	 * any issues not present in the given set.  This issue set is critic-wide,
	 * meaning that the set passed in here should represent the full set of issues
	 * detected by this critic.
	 * @param cis Issue set to report.
	 */
	protected void replaceIssues(CriticIssue[] cis){
		sendToAll(new CriticReplaceIssuesMessage(getIdentifier(), cis), topIface);
	}
	
	/**
	 * Reports a set of open issues with a given open architecture, replacing
	 * any issues <I>for that architecture</I> not present in the current set
	 * of open issues <I>for that architecture</I>. 
	 * @param cis Issue set to report.
	 */
	protected void replaceIssues(ObjRef xArchRef, CriticIssue[] cis){
		//System.out.println("Replacing issues: " + new CriticReplaceIssuesMessage(getIdentifier(), architectureURI, cis));
		sendToAll(new CriticReplaceIssuesMessage(getIdentifier(), xArchRef, cis), topIface);
	}
	
	/**
	 * Determine whether this critic is active or not.  Active critics are
	 * currently looking for problems with open architectures.
	 * @return <CODE>true</CODE> if this critic is active, <CODE>false</CODE>
	 * otherwise.
	 */
	protected boolean isActive(){
		return active;
	}
	
	/**
	 * Activates this critic.  Rechecks all open documents, sets this critic's state
	 * to active, and sends a CriticStatusMessage saying that it's active down to the critic
	 * manager (and whomever else might be listening).  Note that this method should
	 * most likely not be called directly, but can be overridden by critic classes.
	 */
	protected void doActivate(){
		this.active = true;
		recheckAll();
		sendStatusMessage(CriticStatuses.STAT_AVAILABLE_ACTIVE);
	}
	
	/**
	 * Deactivates this critic.  Sets this critic's state
	 * to inactive, and sends a CriticStatusMessage saying that it's 
	 * inactive down to the critic
	 * manager (and whomever else might be listening).  Note that this method should
	 * most likely not be called directly, but can be overridden by critic classes.
	 */
	protected void doDeactivate(){
		this.active = false;
		sendStatusMessage(CriticStatuses.STAT_AVAILABLE_INACTIVE);
	}

	/** 
	 * Makes this critic unavailable, as when it's being unwelded or will
	 * no longer be avaialble at all.  Sets the critic to inactive and
	 * then sends a CriticStatusMessage with <CODE>STAT_UNAVAILABLE</CODE>
	 * down. Note that this method should
	 * most likely not be called directly, but can be overridden by critic classes.
	 */
	protected void doMakeUnavailable(){
		this.active = false;
		sendStatusMessage(CriticStatuses.STAT_UNAVAILABLE);
	}
	
	/**
	 * Sets this critic to active or inactive.
	 * @param newActive Call with <CODE>true</CODE> to make this critic active,
	 * <CODE>false</CODE> to make it inactive.
	 */
	protected void setActive(boolean newActive){
		if(newActive){
			doActivate();
		}
		else{
			doDeactivate();
		}
	}
	
	/**
	 * Set whether this critic is busy or not.  A busy critic is currently
	 * doing some sort of analysis.  This is not the same as waiting, which
	 * is when this critic is waiting for the results of a dependent critic
	 * to make a final determination.  The waiting state is handled by
	 * the critic manager component.
	 * <P>Note that this function works not as a simple flag set/clear
	 * function, but as a counter.  This allows a function that, say, rechecks
	 * all documents, to set 'busy' and then clear it at the end.  Likewise,
	 * a function that checks one document (called by the check-all-documents
	 * function) can do the same thing.  So, <CODE>setBusy(true)</CODE> <B>MUST</B>
	 * be followed by a <CODE>setBusy(false)</CODE> or else the critic could
	 * remain busy forever.
	 * @param newBusy <CODE>true</CODE> if the component is beginning some processing,
	 * <CODE>false</CODE> when it is done.
	 */
	protected void setBusy(boolean newBusy){
		if(newBusy){
			this.busyCount++;
		}
		else{
			this.busyCount--;
			if(busyCount < 0) busyCount = 0;
		}
		sendCurrentStatus();
	}

	/**
	 * Determines whether this critic is currently busy or not.
	 * @return <CODE>true</CODE> if this critic is busy, <CODE>false</CODE>
	 * otherwise.
	 */
	protected boolean isBusy(){
		return busyCount > 0;
	}
	
	/**
	 * Get a description of this critic, where "Description" is a detailed
	 * description that describes, in prose, what this critic does.
	 * @return Description of this critic.
	 */
	protected abstract String getDescription();
	
	/**
	 * Get the dependencies of this critic; i.e. what critics need to be
	 * active and working to make sure that the results of this critic
	 * are correct.  Given as a list of identifiers of other critic
	 * components.
	 * @return array of critic identifiers on which this critic is dependent.
	 */
	protected abstract Identifier[] getDependencies();

	/**
	 * Sends a status message indicating the current status of this critic,
	 * as given in <CODE>CriticStatuses</CODE>.
	 * Should not be called by critics, but may be overridden.
	 * @param status The current status of this critic to send out.
	 * @see CriticStatuses
	 */
	protected void sendStatusMessage(int status){
		CriticStatusMessage csm = new CriticStatusMessage(this.getIdentifier(), getDescription(),
			getDependencies(), status, false);
		//System.out.println(getIdentifier() + ": sending status: " + status);
		this.sendToAll(csm, bottomIface);
		this.sendToAll(csm, topIface);
	}

	/**
	 * Send a <CODE>CriticStatusMessage</CODE> notification down to indicate the current status of this
	 * critic.  The current status is determined automatically by the state of the
	 * critic.
	 */
	protected void sendCurrentStatus(){
		if(active){
			if(isBusy()){
				sendStatusMessage(CriticStatuses.STAT_AVAILABLE_ACTIVE_BUSY);
			}
			else{
				sendStatusMessage(CriticStatuses.STAT_AVAILABLE_ACTIVE);
			}
		}
		if(!active){
			sendStatusMessage(CriticStatuses.STAT_AVAILABLE_INACTIVE);
		}
	}
	
	/**
	 * Sends out initial and final status messages, sets the critic to inactive initially;
	 * should not be a concern for subclasses.
	 */
	class BasicCriticLifecycleProcessor extends LifecycleAdapter implements LifecycleProcessor{
		public void init(){
			setActive(false);
		}
		
		public void begin(){
			sendCurrentStatus();
		}
		
		public void end(){
			doMakeUnavailable();
		}
	}

	/**
	 * Handles <CODE>CriticSetStatusMessage</CODE>s coming from
	 * the critic manager.  If the critic status messages
	 * are not "approved" by the critic manager (i.e. checked
	 * first), they are ignored (it is possible, depending
	 * on the architectural topology, that <CODE>CriticSetStatusMessage</CODE>s
	 * might arrive from some random component, and we don't want
	 * to process those.
	 */
	class CriticStatusMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof CriticSetStatusMessage){
				CriticSetStatusMessage cssm = (CriticSetStatusMessage)m;
				if(!cssm.getApproved()){
					return;
				}
				
				Identifier[] ids = cssm.getCriticIDs();
				boolean appliesToThis = false;
				if(ids == null){
					return;
				}
				else{
					for(int i = 0; i < ids.length; i++){
						if(ids[i].equals(getIdentifier())){
							appliesToThis = true;
						}
					}
				}
				if(!appliesToThis){
					return;
				}
				int status = cssm.getNewStatus();
				if((status == CriticStatuses.STAT_AVAILABLE_ACTIVE) ||
					(status == CriticStatuses.STAT_AVAILABLE_ACTIVE_BUSY)){
					setActive(true);
				}
				else if(status == CriticStatuses.STAT_AVAILABLE_INACTIVE){
					setActive(false);
				}
			}
			else if(m instanceof CriticGetStatusMessage){
				CriticGetStatusMessage cgsm = (CriticGetStatusMessage)m;
				Identifier[] ids = cgsm.getCriticIDs();
				if((ids == null) || (ids.length == 0)){
					sendCurrentStatus();
					return;
				}
				else{
					for(int i = 0; i < ids.length; i++){
						if(ids[i].equals(getIdentifier())){
							sendCurrentStatus();
							return;
						}
					}
				}
			}
		}
	}
	
	/**
	 * If this critic is active, calls <CODE>checkDocument</CODE> on all
	 * open documents.  Called automatically when this critic is activated.
	 */
	public void recheckAll(){
		if(!isActive()) return;
		setBusy(true);
		ObjRef[] openXArches = xarch.getOpenXArches();
		ArrayList openIssues = new ArrayList();
		for(int i = 0; i < openXArches.length; i++){
			checkDocument(openXArches[i]);
		}
		setBusy(false);
	}

	/**
	 * Called when an <CODE>XArchFileEvent</CODE> arrives for this critic.
	 * @param evt <CODE>XArchFileEvent</CODE> to handle.
	 */
	protected abstract void handleFileEvent(XArchFileEvent evt);

	/**
	 * Called when an <CODE>XArchFlatEvent</CODE> arrives for this critic.
	 * @param evt <CODE>XArchFlatEvent</CODE> to handle.
	 */
	protected abstract void handleXArchEvent(XArchFlatEvent evt);

	/**
	 * Called when this critic should check a document for issues.
	 * @param xArchRef the top-level element of the architecture to check
	 * for issues.
	 */
	protected abstract void checkDocument(ObjRef xArchRef);
	

	

}

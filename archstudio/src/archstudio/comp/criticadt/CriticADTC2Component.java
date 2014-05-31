package archstudio.comp.criticadt;

import java.util.*;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;
import c2.util.AdaptingQueue;

import archstudio.critics.*;

import edu.uci.ics.xarchutils.*;

public class CriticADTC2Component extends AbstractC2DelegateBrick{

	//Contains all open CriticIssueMessages
	private HashSet openIssues;
	
	public CriticADTC2Component(Identifier id){
		super(id);
		openIssues = new HashSet();
		addMessageProcessor(new IssueMessageProcessor());
		addMessageProcessor(new CriticStatusMessageProcessor());

		XArchEventProvider xarchEventProvider = 
			(XArchEventProvider)EBIWrapperUtils.createStateChangeProviderProxy(this, topIface,
			XArchEventProvider.class);
		
		XArchFileListener fileListener = new XArchFileListener(){
			public void handleXArchFileEvent(XArchFileEvent evt) {
				handleFileEvent(evt);
			}
		};
		xarchEventProvider.addXArchFileListener(fileListener);
	}
	
	private synchronized void addIssue(CriticIssue issue){
		//System.out.println("Issue added to CriticADT: " + issue);
		openIssues.add(issue);
		sendToAll(new CriticIssueMessage(issue, CriticIssueMessage.ISSUE_OPEN), bottomIface);
	}
	
	private synchronized void removeIssue(CriticIssue issue){
		//System.out.println("Issue removed from CriticADT: " + issue);
		openIssues.remove(issue);
		//System.out.println("Sending remove message: " + issue);
		sendToAll(new CriticIssueMessage(issue, CriticIssueMessage.ISSUE_CLOSED), bottomIface);
	}
	
	private synchronized void removeAllCriticIssues(Identifier criticID){
		HashSet issuesToRemove = new HashSet();
		for(Iterator it = openIssues.iterator(); it.hasNext(); ){
			CriticIssue issue = (CriticIssue)it.next();
			if(issue.getCriticID().equals(criticID)){
				issuesToRemove.add(issue);
			}
		}
		for(Iterator it = issuesToRemove.iterator(); it.hasNext(); ){
			removeIssue((CriticIssue)it.next());
		}
	}
	
	private synchronized void removeAllCriticIssues(Identifier criticID, ObjRef xArchRef){
		HashSet issuesToRemove = new HashSet();
		for(Iterator it = openIssues.iterator(); it.hasNext(); ){
			CriticIssue issue = (CriticIssue)it.next();
			if(issue.getCriticID().equals(criticID)){
				if(issue.getXArchRef().equals(xArchRef)){
					issuesToRemove.add(issue);
				}
			}
		}
		for(Iterator it = issuesToRemove.iterator(); it.hasNext(); ){
			removeIssue((CriticIssue)it.next());
		}
	}
	
	class CriticStatusMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof CriticStatusMessage){
				CriticStatusMessage csm = (CriticStatusMessage)m;
				if((csm.getStatus() == CriticStatuses.STAT_AVAILABLE_INACTIVE) ||
					(csm.getStatus() == CriticStatuses.STAT_UNAVAILABLE)){
					removeAllCriticIssues(csm.getCriticID());
				}
			}
		}
	}
		
	protected void handleFileEvent(XArchFileEvent evt){
		int type = evt.getEventType();
		if((type == XArchFileEvent.XARCH_CLOSED_EVENT)){
			//String url = evt.getURL();
			ObjRef closedXArchRef = evt.getXArchRef();
			
			ArrayList markedForDeath = new ArrayList();
			for(Iterator it = openIssues.iterator(); it.hasNext(); ){
				CriticIssue issue = (CriticIssue)it.next();
				if(issue.getXArchRef().equals(closedXArchRef)){
					markedForDeath.add(issue);
				}
			}
			
			for(Iterator it = markedForDeath.iterator(); it.hasNext(); ){
				removeIssue((CriticIssue)it.next());
			}
		}
	}
	
	class IssueMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof CriticGetIssuesMessage){
				CriticGetIssuesMessage cgim = (CriticGetIssuesMessage)m;
				Identifier[] criticIDs = cgim.getCriticIDs();
				ObjRef[] xArchRefs = cgim.getXArchRefs();
				if((criticIDs == null) && (xArchRefs == null)){
					synchronized(this){
						for(Iterator it = openIssues.iterator(); it.hasNext(); ){
							CriticIssue issue = (CriticIssue)it.next();
							sendToAll(new CriticIssueMessage(issue, CriticIssueMessage.ISSUE_OPEN), bottomIface);
						}
					}							
				}
				else if(criticIDs != null){
					synchronized(this){
						for(Iterator it = openIssues.iterator(); it.hasNext(); ){
							CriticIssue issue = (CriticIssue)it.next();
							for(int i = 0; i < criticIDs.length; i++){
								if(issue.getCriticID().equals(criticIDs[i])){
									sendToAll(new CriticIssueMessage(issue, CriticIssueMessage.ISSUE_OPEN), bottomIface);
								}
							}
						}
					}							
				}
				else if(xArchRefs != null){
					synchronized(this){
						for(Iterator it = openIssues.iterator(); it.hasNext(); ){
							CriticIssue issue = (CriticIssue)it.next();
							for(int i = 0; i < xArchRefs.length; i++){
								if(issue.getXArchRef().equals(xArchRefs[i])){
									sendToAll(new CriticIssueMessage(issue, CriticIssueMessage.ISSUE_OPEN), bottomIface);
								}
							}
						}
					}
				}				
			}
			else if(m instanceof CriticIssueMessage){
				CriticIssueMessage cim = (CriticIssueMessage)m;
				int status = cim.getStatus();
				if(status == CriticIssueMessage.ISSUE_OPEN){
					CriticIssue ci = cim.getIssue();
					addIssue(ci);
				}
				else if(status == CriticIssueMessage.ISSUE_CLOSED){
					CriticIssue ci = cim.getIssue();
					removeIssue(ci);
				}
			}
			else if(m instanceof CriticReplaceIssuesMessage){
				CriticReplaceIssuesMessage crim = (CriticReplaceIssuesMessage)m;
				Identifier criticID = crim.getCriticID();
				CriticIssue[] issues = crim.getIssues();
				ObjRef xArchRef = crim.getXArchRef();
				HashSet issuesToRemove = new HashSet();
				for(Iterator it = openIssues.iterator(); it.hasNext(); ){
					CriticIssue ci = (CriticIssue)it.next();
					//System.out.println("Considering issue: " + ci);
					if(ci.getCriticID().equals(criticID)){
						if(xArchRef != null){
							if(xArchRef.equals(ci.getXArchRef())){
								issuesToRemove.add(ci);
							}
						}
						else{
							issuesToRemove.add(ci);
						}
					}
				}
				
				for(int i = 0; i < issues.length; i++){
					if(!openIssues.contains(issues[i])){
						//It's not there
						addIssue(issues[i]);
					}
					else{
						//It is there.  Don't remove it.
						issuesToRemove.remove(issues[i]);
					}
				}
				//Okay, all the new issues have been added and the ones
				//that are no longer relevant are on the list of ones
				//to remove.  Let's remove those, shall we?
				for(Iterator it = issuesToRemove.iterator(); it.hasNext(); ){
					removeIssue((CriticIssue)it.next());
				}
			}
		}
	}
	
}

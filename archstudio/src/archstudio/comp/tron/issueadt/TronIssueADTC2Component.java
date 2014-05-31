package archstudio.comp.tron.issueadt;

import archstudio.tron.*;

import java.util.*;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchEventProvider;
import edu.uci.ics.xarchutils.XArchFileEvent;
import edu.uci.ics.xarchutils.XArchFileListener;

public class TronIssueADTC2Component extends AbstractC2DelegateBrick{

	protected TronIssueStore issueStore;
	
	public TronIssueADTC2Component(Identifier id){
		super(id);
		issueStore = new TronIssueStore();
		
		addLifecycleProcessor(new TronIssueADTLifecycleProcessor());
		addMessageProcessor(new TronIssueADTMessageProcessor());

		XArchEventProvider xarchEventProvider = 
			(XArchEventProvider)EBIWrapperUtils.createStateChangeProviderProxy(this, topIface,
			XArchEventProvider.class);
		
		XArchFileListener fileListener = new XArchFileListener(){
			public void handleXArchFileEvent(XArchFileEvent evt){
				handleFileEvent(evt);
			}
		};
		xarchEventProvider.addXArchFileListener(fileListener);
	}
	
	public void handleFileEvent(XArchFileEvent evt){
		if(evt.getEventType() == XArchFileEvent.XARCH_CLOSED_EVENT){
			synchronized(issueStore){
				ObjRef documentRef = evt.getXArchRef();
				if(documentRef != null){
					TronIssue[] issuesForDocument = issueStore.getAllIssues(documentRef);
					issueStore.removeIssues(issuesForDocument);
					TronIssueListDiff listDiff = new TronIssueListDiff(issuesForDocument, new TronIssue[0]);
					TronIssuesChangedMessage icm = new TronIssuesChangedMessage(listDiff);
					sendToAll(icm, bottomIface);
				}
			}
		}
	}
	
	class TronIssueADTLifecycleProcessor extends LifecycleAdapter{
		public void begin(){
			TronIssueADTStatusMessage sm = 
				new TronIssueADTStatusMessage(TronIssueADTStatusMessage.STATUS_ACTIVE);
			sendToAll(sm, bottomIface);
			
			TronIssue[] allIssues = issueStore.getAllIssues();
			TronAllIssuesMessage taim = new TronAllIssuesMessage(allIssues);
			sendToAll(taim, bottomIface);
		}
		
		public void end(){
			TronIssueADTStatusMessage sm = 
				new TronIssueADTStatusMessage(TronIssueADTStatusMessage.STATUS_INACTIVE);
			sendToAll(sm, bottomIface);
		}
	}

	class TronIssueADTMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m == null){
				return;
			}
			else if(m instanceof TronGetAllIssuesMessage){
				handleGetAllIssues((TronGetAllIssuesMessage)m);
			}
			else if(m instanceof TronTestResultMessage){
				handleTestResult((TronTestResultMessage)m);
			}
			else if(m instanceof TronTestResultSetMessage){
				handleTestResultSet((TronTestResultSetMessage)m);
			}
			else if(m instanceof TronRemoveIssuesMessage){
				handleRemoveIssues((TronRemoveIssuesMessage)m);
			}
		}
	}
	
	public void handleGetAllIssues(TronGetAllIssuesMessage m){
		TronIssue[] allIssues = issueStore.getAllIssues();
		TronAllIssuesMessage taim = new TronAllIssuesMessage(allIssues);
		sendToAll(taim, bottomIface);
	}
	
	private void handleTestResult(TronTestResultMessage m){
		synchronized(issueStore){
			TronTest test = m.getTest();
			ObjRef documentRef = m.getDocumentRef();
			TronIssue[] oldIssues = new TronIssue[0];
			if (test != null) {
				oldIssues = issueStore.getAllIssues(documentRef, test.getToolID(), test.getUID());
			}
			TronIssue[] newIssues = m.getIssues();
			
			TronIssueListDiff issueListDiff = TronIssueListDiff.diffLists(oldIssues, newIssues);
			issueStore.removeIssues(issueListDiff.getIssuesToRemove());
			issueStore.addIssues(issueListDiff.getIssuesToAdd());
			TronIssuesChangedMessage ticm = new TronIssuesChangedMessage(issueListDiff);
			sendToAll(ticm, bottomIface);
 		}
	}

	public void handleTestResultSet(TronTestResultSetMessage m){
		synchronized(issueStore){
			TronTestResultMessage[] testResultMessages = m.getTestResults();
			for(int i = 0; i < testResultMessages.length; i++){
				handleTestResult(testResultMessages[i]);
			}
		}
	}
	
	public void handleRemoveIssues(TronRemoveIssuesMessage m){
		synchronized(issueStore){
			ObjRef documentRef = m.getDocumentRef();
			TronIssue[] issuesToRemove = issueStore.getAllIssues(documentRef);
			issueStore.removeIssues(issuesToRemove);

			TronIssueListDiff issueListDiff = TronIssueListDiff.diffLists(issuesToRemove, new TronIssue[0]);
			TronIssuesChangedMessage ticm = new TronIssuesChangedMessage(issueListDiff);
			sendToAll(ticm, bottomIface);
		}
		
	}
}

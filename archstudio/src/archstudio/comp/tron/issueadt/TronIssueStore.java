package archstudio.comp.tron.issueadt;

import java.util.*;

import archstudio.tron.*;
import edu.uci.ics.xarchutils.ObjRef;
	
public class TronIssueStore{

	private static final TronIssue[] emptyIssueArray = new TronIssue[0];
	
	protected List issueList;
	
	public TronIssueStore(){
		issueList = new ArrayList();
	}
	
	public synchronized TronIssue[] getAllIssues(){
		return (TronIssue[])issueList.toArray(emptyIssueArray);
	}
	
	public synchronized TronIssue[] getAllIssues(ObjRef documentRef){
		List matchingList = new ArrayList();
		for(Iterator it = issueList.iterator(); it.hasNext(); ){
			TronIssue issue = (TronIssue)it.next();
			if((issue.getDocumentRef() != null) && (issue.getDocumentRef().equals(documentRef))){
				matchingList.add(issue);
			}
		}
		return (TronIssue[])matchingList.toArray(emptyIssueArray);
	}
	
	public synchronized TronIssue[] getAllIssues(String toolID){
		List matchingList = new ArrayList();
		for(Iterator it = issueList.iterator(); it.hasNext(); ){
			TronIssue issue = (TronIssue)it.next();
			if((issue.getToolID() != null) && (issue.getToolID().equals(toolID))){
				matchingList.add(issue);
			}
		}
		return (TronIssue[])matchingList.toArray(emptyIssueArray);
	}
	
	public synchronized TronIssue[] getAllIssues(ObjRef documentRef, String toolID){
		List matchingList = new ArrayList();
		for(Iterator it = issueList.iterator(); it.hasNext(); ){
			TronIssue issue = (TronIssue)it.next();
			if((issue.getDocumentRef() != null) && (issue.getDocumentRef().equals(documentRef))){
				if((issue.getToolID() != null) && (issue.getToolID().equals(toolID))){
					matchingList.add(issue);
				}
			}
		}
		return (TronIssue[])matchingList.toArray(emptyIssueArray);
	}
	
	public synchronized TronIssue[] getAllIssuesByTestUID(ObjRef documentRef, String testUID){
		List matchingList = new ArrayList();
		for(Iterator it = issueList.iterator(); it.hasNext(); ){
			TronIssue issue = (TronIssue)it.next();
			if((issue.getDocumentRef() != null) && (issue.getDocumentRef().equals(documentRef))){
				if((issue.getTestUID() != null) && (issue.getToolID().equals(testUID))){
					matchingList.add(issue);
				}
			}
		}
		return (TronIssue[])matchingList.toArray(emptyIssueArray);
	}
	
	public synchronized TronIssue[] getAllIssues(ObjRef documentRef, String toolID, String testUID){
		List matchingList = new ArrayList();
		for(Iterator it = issueList.iterator(); it.hasNext(); ){
			TronIssue issue = (TronIssue)it.next();
			if((issue.getDocumentRef() != null) && (issue.getDocumentRef().equals(documentRef))){
				if((issue.getToolID() != null) && (issue.getToolID().equals(toolID))){
					if((issue.getTestUID() != null) && (issue.getTestUID().equals(testUID))){
						matchingList.add(issue);
					}
				}
			}
		}
		return (TronIssue[])matchingList.toArray(emptyIssueArray);
	}
	
	public void addIssues(TronIssue[] issues){
		for(int i = 0; i < issues.length; i++){
			issueList.add(issues[i]);
		}
	}
	
	public void removeIssues(TronIssue[] issues){
		for(int i = 0; i < issues.length; i++){
			issueList.remove(issues[i]);
		}
	}
	
}

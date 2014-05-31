package archstudio.tron;

import edu.uci.ics.xarchutils.ObjRef;

public class TronTestResult{

	protected ObjRef documentRef;
	protected String testUID;
	protected TronIssue[] issues;
	
	public TronTestResult(ObjRef documentRef, String testUID, TronIssue[] issues){
		super();
		this.documentRef = documentRef;
		this.testUID = testUID;
		this.issues = issues;
	}
	
	public ObjRef getDocumentRef(){
		return documentRef;
	}
	
	public void setDocumentRef(ObjRef documentRef){
		this.documentRef = documentRef;
	}
	
	public TronIssue[] getIssues(){
		return issues;
	}
	
	public void setIssues(TronIssue[] issues){
		this.issues = issues;
	}
	
	public String getTestUID(){
		return testUID;
	}
	
	public void setTestUID(String testUID){
		this.testUID = testUID;
	}
	
	public String toString(){
		StringBuffer buf = new StringBuffer("TronTestResult[");
		buf.append("testUID=").append(testUID).append(";");
		if(issues == null){
			buf.append("issues=null;");
		}
		else{
			for(int i = 0; i < issues.length; i++){
				buf.append("issues[").append(i).append("]=").append(issues[i]).append(";");
			}
		}
		buf.append("];");
		return buf.toString();
	}
}

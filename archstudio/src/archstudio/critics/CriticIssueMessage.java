package archstudio.critics;

import c2.fw.*;

public class CriticIssueMessage extends NamedPropertyMessage implements java.io.Serializable{
	
	public static final int ISSUE_OPEN = 100;
	public static final int ISSUE_CLOSED = 200;
	
	public CriticIssueMessage(CriticIssue issue, int status){
		super("CriticIssueMessage");
		super.addParameter("issue", issue);
		super.addParameter("status", status);
	}
	
	protected CriticIssueMessage(CriticIssueMessage copyMe){
		super(copyMe);
	}
	
	
	public boolean equals(Object o){
		if(!(o instanceof CriticIssueMessage)){
			return false;
		}
		CriticIssueMessage otherMessage = (CriticIssueMessage)o;
		return
			getIssue().equals(otherMessage.getIssue()) &&
			(getStatus() == otherMessage.getStatus());
	}
	
	public int hashCode(){
		return 
			getIssue().hashCode() ^
			getStatus();
	}
	
	public Message duplicate(){
		return new CriticIssueMessage(this);
	}
	
	public CriticIssue getIssue(){
		return (CriticIssue)getParameter("issue");
	}
	
	public int getStatus(){
		return getIntParameter("status");
	}
	
}

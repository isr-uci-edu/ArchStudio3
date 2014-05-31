package archstudio.tron;

import c2.fw.*;

public class TronIssuesChangedMessage extends NamedPropertyMessage{
	public TronIssuesChangedMessage(TronIssueListDiff issueListDiff){
		super("TronIssuesChangedMessage");
		super.addParameter("issueListDiff", issueListDiff);
	}

	protected TronIssuesChangedMessage(TronIssuesChangedMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new TronIssuesChangedMessage(this);
	}

	public void setIssueListDiff(TronIssueListDiff issueListDiff){
		addParameter("issueListDiff", issueListDiff);
	}

	public TronIssueListDiff getIssueListDiff(){
		return (TronIssueListDiff)getParameter("issueListDiff");
	}

}


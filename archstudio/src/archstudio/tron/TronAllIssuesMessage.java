package archstudio.tron;

import c2.fw.*;

public class TronAllIssuesMessage extends NamedPropertyMessage{
	public TronAllIssuesMessage(TronIssue[] allIssues){
		super("TronAllIssuesMessage");
		super.addParameter("allIssues", allIssues);
	}

	protected TronAllIssuesMessage(TronAllIssuesMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new TronAllIssuesMessage(this);
	}

	public void setAllIssues(TronIssue[] allIssues){
		addParameter("allIssues", allIssues);
	}

	public TronIssue[] getAllIssues(){
		return (TronIssue[])getParameter("allIssues");
	}

}


package archstudio.tron;

import c2.fw.*;

public class TronIssueADTStatusMessage extends NamedPropertyMessage{
	
	public static final int STATUS_ACTIVE = 100;
	public static final int STATUS_INACTIVE = 200;
	
	public TronIssueADTStatusMessage(int newStatus){
		super("TronIssueADTStatusMessage");
		super.addParameter("newStatus", newStatus);
	}

	protected TronIssueADTStatusMessage(TronIssueADTStatusMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new TronIssueADTStatusMessage(this);
	}

	public void setNewStatus(int newStatus){
		addParameter("newStatus", newStatus);
	}

	public int getNewStatus(){
		return getIntParameter("newStatus");
	}

}


package archstudio.tron;

import c2.fw.*;

public class TronTestADTStatusMessage extends NamedPropertyMessage{
	
	public static final int STATUS_ACTIVE = 100;
	public static final int STATUS_INACTIVE = 200;
	
	public TronTestADTStatusMessage(int newStatus){
		super("TronTestADTStatusMessage");
		super.addParameter("newStatus", newStatus);
	}

	protected TronTestADTStatusMessage(TronTestADTStatusMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new TronTestADTStatusMessage(this);
	}

	public void setNewStatus(int newStatus){
		addParameter("newStatus", newStatus);
	}

	public int getNewStatus(){
		return getIntParameter("newStatus");
	}

}


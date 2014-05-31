package archstudio.critics;

import c2.fw.*;

public class CriticSetStatusMessage extends NamedPropertyMessage implements java.io.Serializable{
	
	//Approved means that this status change has been approved by the critic manager;
	//otherwise, the critic should not change its status.
	public CriticSetStatusMessage(Identifier criticID, int newStatus, boolean approved){
		super("CriticSetStatusMessage");
		super.addParameter("criticIDs", new Identifier[]{criticID});
		super.addParameter("newStatus", newStatus);
		super.addParameter("approved", approved);
	}
	
	public CriticSetStatusMessage(Identifier[] criticIDs, int newStatus, boolean approved){
		super("CriticSetStatusMessage");
		super.addParameter("criticIDs", criticIDs);
		super.addParameter("newStatus", newStatus);
		super.addParameter("approved", approved);
	}
	
	protected CriticSetStatusMessage(CriticSetStatusMessage copyMe){
		super(copyMe);
	}
	
	public Message duplicate(){
		return new CriticSetStatusMessage(this);
	}
	
	public Identifier[] getCriticIDs(){
		return (Identifier[])getParameter("criticIDs");
	}
	
	public int getNewStatus(){
		return getIntParameter("newStatus");
	}

	public boolean getApproved(){
		return getBooleanParameter("approved");
	}
}

package archstudio.tron;

import c2.fw.*;

public class TronToolStatusMessage extends NamedPropertyMessage{
	
	public static final String ENDING_STATUS = "$$ending$$";
	
	public TronToolStatusMessage(String toolID, String status, int progressPercent){
		super("TronToolStatusMessage");
		super.addParameter("toolID", toolID);
		super.addParameter("status", status);
		super.addParameter("progressPercent", progressPercent);
	}

	protected TronToolStatusMessage(TronToolStatusMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new TronToolStatusMessage(this);
	}

	public void setToolID(String toolID){
		addParameter("toolID", toolID);
	}

	public String getToolID(){
		return (String)getParameter("toolID");
	}

	public void setStatus(String status){
		addParameter("status", status);
	}

	public String getStatus(){
		return (String)getParameter("status");
	}

	public void setProgressPercent(int progressPercent){
		addParameter("progressPercent", progressPercent);
	}

	public int getProgressPercent(){
		return getIntParameter("progressPercent");
	}

}


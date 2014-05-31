package archstudio.tron;

import c2.fw.*;

public class TronAllToolNoticesMessage extends NamedPropertyMessage{
	public TronAllToolNoticesMessage(String toolID, TronToolNotice[] toolNotices){
		super("TronAllToolNoticesMessage");
		super.addParameter("toolID", toolID);
		super.addParameter("toolNotices", toolNotices);
	}

	protected TronAllToolNoticesMessage(TronAllToolNoticesMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new TronAllToolNoticesMessage(this);
	}

	public void setToolID(String toolID){
		addParameter("toolID", toolID);
	}

	public String getToolID(){
		return (String)getParameter("toolID");
	}

	public void setToolNotices(TronToolNotice[] toolNotices){
		addParameter("toolNotices", toolNotices);
	}

	public TronToolNotice[] getToolNotices(){
		return (TronToolNotice[])getParameter("toolNotices");
	}

}


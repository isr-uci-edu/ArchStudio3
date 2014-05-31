package archstudio.tron;

import c2.fw.*;

public class TronReplaceTestsMessage extends NamedPropertyMessage{
	public TronReplaceTestsMessage(String toolID, TronTest[] newTests){
		super("TronReplaceTestsMessage");
		super.addParameter("toolID", toolID);
		super.addParameter("newTests", newTests);
	}

	protected TronReplaceTestsMessage(TronReplaceTestsMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new TronReplaceTestsMessage(this);
	}

	public void setToolID(String toolID){
		addParameter("toolID", toolID);
	}

	public String getToolID(){
		return (String)getParameter("toolID");
	}

	public void setNewTests(TronTest[] newTests){
		addParameter("newTests", newTests);
	}

	public TronTest[] getNewTests(){
		return (TronTest[])getParameter("newTests");
	}

}


package archstudio.tron;

import c2.fw.*;

public class TronTestErrorsMessage extends NamedPropertyMessage{
	public TronTestErrorsMessage(String toolID, TronTestError[] testErrors){
		super("TronTestErrorsMessage");
		super.addParameter("toolID", toolID);
		super.addParameter("testErrors", testErrors);
	}

	protected TronTestErrorsMessage(TronTestErrorsMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new TronTestErrorsMessage(this);
	}

	public void setToolID(String toolID){
		addParameter("toolID", toolID);
	}

	public String getToolID(){
		return (String)getParameter("toolID");
	}

	public void setTestErrors(TronTestError[] testErrors){
		addParameter("testErrors", testErrors);
	}

	public TronTestError[] getTestErrors(){
		return (TronTestError[])getParameter("testErrors");
	}

}
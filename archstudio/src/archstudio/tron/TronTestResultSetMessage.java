package archstudio.tron;

import c2.fw.*;
import edu.uci.ics.xarchutils.ObjRef;

public class TronTestResultSetMessage extends NamedPropertyMessage{
	public TronTestResultSetMessage(TronTestResultMessage[] testResults){
		super("TronTestResultSetMessage");
		super.addParameter("testResults", testResults);
	}

	protected TronTestResultSetMessage(TronTestResultSetMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new TronTestResultSetMessage(this);
	}

	public void setTestResults(TronTestResultMessage[] testResults){
		addParameter("testResults", testResults);
	}

	public TronTestResultMessage[] getTestResults(){
		return (TronTestResultMessage[])getParameter("testResults");
	}

}


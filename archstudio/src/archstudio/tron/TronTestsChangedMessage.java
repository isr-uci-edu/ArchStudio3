package archstudio.tron;

import c2.fw.*;

public class TronTestsChangedMessage extends NamedPropertyMessage{
	public TronTestsChangedMessage(TronTestListDiff testListDiff){
		super("TronTestsChangedMessage");
		super.addParameter("testListDiff", testListDiff);
	}

	protected TronTestsChangedMessage(TronTestsChangedMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new TronTestsChangedMessage(this);
	}

	public void setTestListDiff(TronTestListDiff testListDiff){
		addParameter("testListDiff", testListDiff);
	}

	public TronTestListDiff getTestListDiff(){
		return (TronTestListDiff)getParameter("testListDiff");
	}

}


package archstudio.tron;

import c2.fw.*;

public class TronAllTestsMessage extends NamedPropertyMessage{
	public TronAllTestsMessage(TronTest[] allTests){
		super("TronAllTestsMessage");
		super.addParameter("allTests", allTests);
	}

	protected TronAllTestsMessage(TronAllTestsMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new TronAllTestsMessage(this);
	}

	public void setAllTests(TronTest[] allTests){
		addParameter("allTests", allTests);
	}

	public TronTest[] getAllTests(){
		return (TronTest[])getParameter("allTests");
	}

}


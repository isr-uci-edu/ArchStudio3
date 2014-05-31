package archstudio.tron;

import c2.fw.*;

public class TronGetAllTestsMessage extends NamedPropertyMessage{
	public TronGetAllTestsMessage(){
		super("TronGetAllIssuesMessage");
	}

	protected TronGetAllTestsMessage(TronGetAllTestsMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new TronGetAllTestsMessage(this);
	}

}


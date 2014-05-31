package archstudio.tron;

import c2.fw.*;

public class TronGetAllIssuesMessage extends NamedPropertyMessage{
	public TronGetAllIssuesMessage(){
		super("TronGetAllIssuesMessage");
	}

	protected TronGetAllIssuesMessage(TronGetAllIssuesMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new TronGetAllIssuesMessage(this);
	}

}


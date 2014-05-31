package archstudio.tron;

import c2.fw.*;

public class TronRefreshTestsMessage extends NamedPropertyMessage{
	public TronRefreshTestsMessage(){
		super("TronRefreshTestsMessage");
	}

	protected TronRefreshTestsMessage(TronRefreshTestsMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new TronRefreshTestsMessage(this);
	}

}

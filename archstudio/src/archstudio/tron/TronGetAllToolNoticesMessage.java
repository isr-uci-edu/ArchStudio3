package archstudio.tron;

import c2.fw.*;

public class TronGetAllToolNoticesMessage extends NamedPropertyMessage{
	public TronGetAllToolNoticesMessage(){
		super("TronGetAllToolNoticesMessage");
	}

	protected TronGetAllToolNoticesMessage(TronGetAllToolNoticesMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new TronGetAllToolNoticesMessage(this);
	}

}


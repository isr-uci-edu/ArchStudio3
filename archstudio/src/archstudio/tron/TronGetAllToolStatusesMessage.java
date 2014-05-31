package archstudio.tron;

import c2.fw.*;

public class TronGetAllToolStatusesMessage extends NamedPropertyMessage{
	public TronGetAllToolStatusesMessage(){
		super("TronGetAllToolStatusesMessage");
	}

	protected TronGetAllToolStatusesMessage(TronGetAllToolStatusesMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new TronGetAllToolStatusesMessage(this);
	}

}

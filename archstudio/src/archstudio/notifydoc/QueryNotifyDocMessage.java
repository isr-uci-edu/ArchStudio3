package archstudio.notifydoc;

import c2.fw.*;

public class QueryNotifyDocMessage extends NamedPropertyMessage implements java.io.Serializable{

	public QueryNotifyDocMessage(){
		super("QueryNotifyDocMessage");
	}
	
	protected QueryNotifyDocMessage(QueryNotifyDocMessage copyMe){
		super(copyMe);
	}
	
	public Message duplicate(){
		return new QueryNotifyDocMessage(this);
	}

}


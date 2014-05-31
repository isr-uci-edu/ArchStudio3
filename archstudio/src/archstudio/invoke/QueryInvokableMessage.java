package archstudio.invoke;

import c2.fw.*;

public class QueryInvokableMessage extends NamedPropertyMessage implements java.io.Serializable{

	public QueryInvokableMessage(){
		super("QueryInvokableMessage");
	}
	
	protected QueryInvokableMessage(QueryInvokableMessage copyMe){
		super(copyMe);
	}
	
	public Message duplicate(){
		return new QueryInvokableMessage(this);
	}

}


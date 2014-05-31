package archstudio.preferences;

import c2.fw.*;

public class QueryPreferencePanelMessage extends NamedPropertyMessage{
	public QueryPreferencePanelMessage(){
		super("QueryPreferencePanelMessage");
	}

	protected QueryPreferencePanelMessage(QueryPreferencePanelMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new QueryPreferencePanelMessage(this);
	}

}


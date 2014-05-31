package archstudio.comp.guardtracker;

import c2.fw.*;

public class GetAllGuardsMessage extends NamedPropertyMessage{
	public GetAllGuardsMessage(){
		super("GetAllGuardsMessage");
	}

	protected GetAllGuardsMessage(GetAllGuardsMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new GetAllGuardsMessage(this);
	}

}


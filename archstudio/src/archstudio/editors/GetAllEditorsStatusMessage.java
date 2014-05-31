package archstudio.editors;

import c2.fw.*;

public class GetAllEditorsStatusMessage extends NamedPropertyMessage{
	public GetAllEditorsStatusMessage(){
		super("GetAllEditorsStatusMessage");
	}

	protected GetAllEditorsStatusMessage(GetAllEditorsStatusMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new GetAllEditorsStatusMessage(this);
	}

}

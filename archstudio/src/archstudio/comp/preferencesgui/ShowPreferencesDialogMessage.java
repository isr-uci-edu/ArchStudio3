package archstudio.comp.preferencesgui;

import c2.fw.*;

public class ShowPreferencesDialogMessage extends NamedPropertyMessage{
	public ShowPreferencesDialogMessage(){
		super("ShowPreferencesDialogMessage");
	}

	public ShowPreferencesDialogMessage(String nodeToShow){
		super("ShowPreferencesDialogMessage");
		this.addParameter("nodeToShow", nodeToShow);
	}
	
	protected ShowPreferencesDialogMessage(ShowPreferencesDialogMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new ShowPreferencesDialogMessage(this);
	}
	
	public String getNodeToShow(){
		return (String)super.getParameter("nodeToShow");
	}

}


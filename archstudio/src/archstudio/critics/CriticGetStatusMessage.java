package archstudio.critics;

import c2.fw.*;

public class CriticGetStatusMessage extends NamedPropertyMessage{
	
	public CriticGetStatusMessage(Identifier criticID){
		super("CriticGetStatusMessage");
		super.addParameter("criticIDs", new Identifier[]{criticID});
	}
	
	public CriticGetStatusMessage(Identifier[] criticIDs){
		super("CriticGetStatusMessage");
		super.addParameter("criticIDs", criticIDs);
	}
	
	public CriticGetStatusMessage(){
		super("CriticGetStatusMessage");
	}		
	
	protected CriticGetStatusMessage(CriticGetStatusMessage copyMe){
		super(copyMe);
	}
	
	public Message duplicate(){
		return new CriticGetStatusMessage(this);
	}

	public Identifier[] getCriticIDs(){
		return (Identifier[])super.getParameter("criticIDs");
	}
	
}

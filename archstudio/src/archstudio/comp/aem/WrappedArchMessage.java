package archstudio.comp.aem;

import c2.fw.*;

public class WrappedArchMessage extends NamedPropertyMessage{
	public WrappedArchMessage(String managedSystemURI, Message originalMessage){
		super("WrappedArchMessage");
		super.addParameter("managedSystemURI", managedSystemURI);
		super.addParameter("originalMessage", originalMessage);
	}

	protected WrappedArchMessage(WrappedArchMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new WrappedArchMessage(this);
	}

	public void setManagedSystemURI(String managedSystemURI){
		addParameter("managedSystemURI", managedSystemURI);
	}

	public String getManagedSystemURI(){
		return (String)getParameter("managedSystemURI");
	}

	public void setOriginalMessage(Message originalMessage){
		addParameter("originalMessage", originalMessage);
	}

	public Message getOriginalMessage(){
		return (Message)getParameter("originalMessage");
	}

}


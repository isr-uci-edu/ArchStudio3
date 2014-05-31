package archstudio.comp.aem;

import c2.fw.*;

import edu.uci.ics.xarchutils.*;

public class AEMProgressMessage extends NamedPropertyMessage{
	
	public AEMProgressMessage(String managedSystemURI, String activity, 
	int currentValue, int upperBound, String addlMessage){
		super("AEMInstantiateMessage");
		super.addParameter("managedSystemURI", managedSystemURI);
		super.addParameter("activity", activity);
		super.addParameter("currentValue", currentValue);
		super.addParameter("upperBound", upperBound);
		super.addParameter("additionalMessage", addlMessage);
	}
	
	protected AEMProgressMessage(AEMProgressMessage copyMe){
		super(copyMe);
	}
	
	public Message duplicate(){
		return new AEMProgressMessage(this);
	}
	
	public String getManagedSystemURI(){
		return (String)getParameter("managedSystemURI");
	}
	
	public String getActivity(){
		return (String)getParameter("activity");
	}
	
	public int getCurrentValue(){
		return getIntParameter("currentValue");
	}
	
	public int getUpperBound(){
		return getIntParameter("upperBound");
	}
	
	public String getAdditionalMessage(){
		return (String)getParameter("additionalMessage");
	}
}
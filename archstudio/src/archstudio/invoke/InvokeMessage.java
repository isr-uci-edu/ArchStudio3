package archstudio.invoke;

import c2.fw.*;
import edu.uci.ics.xarchutils.ObjRef;

public class InvokeMessage extends NamedPropertyMessage implements java.io.Serializable{

	public InvokeMessage(Identifier componentId, String serviceName){
		super("InvokeMessage");
		super.addParameter("componentId", componentId);
		super.addParameter("serviceName", serviceName);
	}
	
	public InvokeMessage(Identifier componentId, String serviceName, String architectureURL){
		this(componentId, serviceName);
		if(architectureURL == null){
			architectureURL = "$NULL$";
		}
		super.addParameter("architectureURL", architectureURL);
	}
	
	public InvokeMessage(Identifier componentId, String serviceName, String architectureURL, ObjRef elementRef){
		this(componentId, serviceName, architectureURL);
		super.addParameter("elementRef", elementRef);
	}
	
	protected InvokeMessage(InvokeMessage copyMe){
		super(copyMe);
	}
	
	public Message duplicate(){
		return new InvokeMessage(this);
	}
	
	public Identifier getComponentId(){
		return (Identifier)super.getParameter("componentId");
	}
	
	public String getServiceName(){
		return (String)super.getParameter("serviceName");
	}
	
	public String getArchitectureURL(){
		String s = (String)super.getParameter("architectureURL");
		if(s.equals("$NULL$")){
			return null;
		}
		else{
			return s;
		}
	}
	
	public ObjRef getElementRef(){
		return (ObjRef)super.getParameter("elementRef");
	}

}


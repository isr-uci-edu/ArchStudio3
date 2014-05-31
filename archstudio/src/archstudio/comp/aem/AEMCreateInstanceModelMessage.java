package archstudio.comp.aem;

import c2.fw.*;

public class AEMCreateInstanceModelMessage extends NamedPropertyMessage{
	public AEMCreateInstanceModelMessage(String managedSystemURI, String instanceModelURI){
		super("AEMCreateInstanceModelMessage");
		super.addParameter("managedSystemURI", managedSystemURI);
		super.addParameter("instanceModelURI", instanceModelURI);
	}

	protected AEMCreateInstanceModelMessage(AEMCreateInstanceModelMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new AEMCreateInstanceModelMessage(this);
	}

	public void setManagedSystemURI(String managedSystemURI){
		addParameter("managedSystemURI", managedSystemURI);
	}

	public String getManagedSystemURI(){
		return (String)getParameter("managedSystemURI");
	}

	public void setInstanceModelURI(String instanceModelURI){
		addParameter("instanceModelURI", instanceModelURI);
	}

	public String getInstanceModelURI(){
		return (String)getParameter("instanceModelURI");
	}

}



package archstudio.comp.aem;

import c2.fw.*;

import edu.uci.ics.xarchutils.*;

public class AEMInstantiateStatusMessage extends NamedPropertyMessage{
	public AEMInstantiateStatusMessage(String managedSystemURI, String architectureURI){
		super("AEMInstantiateStatusMessage");
		super.addParameter("managedSystemURI", managedSystemURI);
		super.addParameter("architectureURI", architectureURI);
	}
	
	public AEMInstantiateStatusMessage(String architectureURI, InvalidArchitectureDescriptionException e){
		super("AEMInstantiateStatusMessage");
		super.addParameter("architectureURI", architectureURI);
		super.addParameter("error", e);
	}
	
	protected AEMInstantiateStatusMessage(AEMInstantiateStatusMessage copyMe){
		super(copyMe);
	}
	
	public Message duplicate(){
		return new AEMInstantiateStatusMessage(this);
	}
	
	public InvalidArchitectureDescriptionException getError(){
		return (InvalidArchitectureDescriptionException)getParameter("error");
	}
	
	public String getManagedSystemURI(){
		return (String)getParameter("managedSystemURI");
	}
	
	public String getArchitectureURI(){
		return (String)getParameter("architectureURI");
	}
	
	public boolean isSuccess(){
		return getError() == null;
	}
	
}

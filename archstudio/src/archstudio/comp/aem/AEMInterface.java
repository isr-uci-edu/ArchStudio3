package archstudio.comp.aem;

import edu.uci.ics.xarchutils.*;

public interface AEMInterface{
	
	public void instantiate(String url) throws InvalidArchitectureDescriptionException;
	//public void instantiate(String url, String instanceURI) throws InvalidArchitectureDescriptionException;
	public void instantiate(ObjRef xArchRef) throws InvalidArchitectureDescriptionException;
	//public void instantiate(ObjRef xArchRef, String instanceURI) throws InvalidArchitectureDescriptionException;

}


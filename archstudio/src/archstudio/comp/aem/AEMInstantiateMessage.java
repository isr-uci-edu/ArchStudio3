package archstudio.comp.aem;

import c2.fw.*;

import edu.uci.ics.xarchutils.*;

public class AEMInstantiateMessage extends NamedPropertyMessage{
	
	public static final int ENGINETYPE_ONETHREADPERBRICK = 100;
	public static final int ENGINETYPE_ONETHREADSTEPPABLE = 200;
	
	public AEMInstantiateMessage(String managedSystemURI, ObjRef xArchRef){
		this(managedSystemURI, xArchRef, ENGINETYPE_ONETHREADPERBRICK);
	}
	
	public AEMInstantiateMessage(String managedSystemURI, String uri){
		this(managedSystemURI, uri, ENGINETYPE_ONETHREADPERBRICK);
	}
	
	public AEMInstantiateMessage(String managedSystemURI, ObjRef xArchRef, int engineType){
		super("AEMInstantiateMessage");
		super.addParameter("xArchRef", xArchRef);
		super.addParameter("engineType", engineType);
	}
	
	public AEMInstantiateMessage(String managedSystemURI, String uri, int engineType){
		super("AEMInstantiateMessage");
		super.addParameter("managedSystemURI", managedSystemURI);
		super.addParameter("uri", uri);
		super.addParameter("engineType", engineType);
	}
	
	protected AEMInstantiateMessage(AEMInstantiateMessage copyMe){
		super(copyMe);
	}
	
	public Message duplicate(){
		return new AEMInstantiateMessage(this);
	}

	public int getEngineType(){
		return getIntParameter("engineType");
	}
	
	public ObjRef getXArchRef(){
		return (ObjRef)getParameter("xArchRef");
	}
	
	public String getURI(){
		return (String)getParameter("uri");
	}
	
	public String getManagedSystemURI(){
		return (String)getParameter("managedSystemURI");
	}
}
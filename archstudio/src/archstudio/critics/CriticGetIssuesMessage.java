package archstudio.critics;

import c2.fw.*;
import edu.uci.ics.xarchutils.ObjRef;

public class CriticGetIssuesMessage extends NamedPropertyMessage{
	
	public CriticGetIssuesMessage(Identifier criticID){
		super("CriticGetIssuesMessage");
		super.addParameter("criticIDs", new Identifier[]{criticID});
	}
	
	public CriticGetIssuesMessage(Identifier[] criticIDs){
		super("CriticGetIssuesMessage");
		super.addParameter("criticIDs", criticIDs);
	}
	
	public CriticGetIssuesMessage(){
		super("CriticGetIssuesMessage");
	}
	
	public CriticGetIssuesMessage(ObjRef xArchRef){
		super("CriticGetIssuesMessage");
		super.addParameter("xArchRefs", new ObjRef[]{xArchRef});
	}
	
	public CriticGetIssuesMessage(ObjRef[] xArchRefs){
		super("CriticGetIssuesMessage");
		super.addParameter("xArchRefs", xArchRefs);
	}
	
	
	protected CriticGetIssuesMessage(CriticGetIssuesMessage copyMe){
		super(copyMe);
	}
	
	public Message duplicate(){
		return new CriticGetIssuesMessage(this);
	}

	public Identifier[] getCriticIDs(){
		return (Identifier[])super.getParameter("criticIDs");
	}
	
	public ObjRef[] getXArchRefs(){
		return (ObjRef[])super.getParameter("xArchRefs");
	}
	
}
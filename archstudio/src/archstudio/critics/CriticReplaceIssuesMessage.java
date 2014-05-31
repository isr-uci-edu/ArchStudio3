package archstudio.critics;

import c2.fw.*;
import edu.uci.ics.xarchutils.ObjRef;

//Tells the Critic ADT to replace all open issues from a given
//critic with this set.  Optionally includes a document to scope
//down the request.
public class CriticReplaceIssuesMessage extends NamedPropertyMessage implements java.io.Serializable{
		
	public CriticReplaceIssuesMessage(Identifier criticID, CriticIssue[] issues){
		super("CriticReplaceIssuesMessage");
		super.addParameter("criticID", criticID);
		super.addParameter("issues", issues);
	}

	public CriticReplaceIssuesMessage(Identifier criticID, ObjRef xArchRef, CriticIssue[] issues){
		super("CriticReplaceIssuesMessage");
		super.addParameter("criticID", criticID);
		super.addParameter("xArchRef", xArchRef);
		super.addParameter("issues", issues);
	}
	
	protected CriticReplaceIssuesMessage(CriticReplaceIssuesMessage copyMe){
		super(copyMe);
	}
	
	public Message duplicate(){
		return new CriticReplaceIssuesMessage(this);
	}
	
	public CriticIssue[] getIssues(){
		return (CriticIssue[])super.getParameter("issues");
	}
	
	public ObjRef getXArchRef(){
		return (ObjRef)super.getParameter("xArchRef");
	}
	
	public Identifier getCriticID(){
		return (Identifier)super.getParameter("criticID");
	}

}

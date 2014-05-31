package archstudio.critics;

import c2.fw.*;

import edu.uci.ics.xarchutils.*;

public class CriticIssue extends NamedPropertyMessage implements java.io.Serializable{

	//CriticID: The identifier of the critic issuing this message.
	//categoryID: Identifier identifying broad-brushed category for different 
	//  sorts of issues (used to select the icon in one of the artists)
	//categoryDescriprtion: Description of the category, usually 1-2 words
	//issueTypeID: Identifies the type of issue this is.  This allows selection
	//  of various issue types from the ADT programmatically, so critics
	//  can interact with one another programmatically.
	//headline: A one-line summary of the problem
	//issueDescription: A more detailed description of the problem
	//specificInfo: Details about this particular problem (i.e. as opposed to others
	// with the same description)
	//architectureURI:  The architecture URI where this problem was found
	//affectedElements:  Elements within that architecture where the problem was found
	
	public CriticIssue(Identifier criticId, Identifier categoryID, String categoryDescription,
	Identifier issueTypeID, String headline, String issueDescription, 
	String specificInfo, ObjRef xArchRef, ObjRef[] affectedElements){
		super("CriticIssue");
		super.addParameter("criticID", criticId);
		super.addParameter("categoryID", categoryID);
		super.addParameter("categoryDescription", categoryDescription);
		super.addParameter("issueTypeID", issueTypeID);
		super.addParameter("headline", headline);
		super.addParameter("issueDescription", issueDescription);
		super.addParameter("specificInfo", specificInfo);
		super.addParameter("xArchRef", xArchRef);
		super.addParameter("affectedElements", affectedElements);
	}
	
	protected CriticIssue(CriticIssue copyMe){
		super(copyMe);
	}
	
	public boolean equals(Object o){
		if(!(o instanceof CriticIssue)){
			return false;
		}
		CriticIssue otherMessage = (CriticIssue)o;
		return
			getCategoryID().equals(otherMessage.getCategoryID()) &&
			getCategoryDescription().equals(otherMessage.getCategoryDescription()) &&
			getCriticID().equals(otherMessage.getCriticID()) &&
			getIssueTypeID().equals(otherMessage.getIssueTypeID()) &&
			getHeadline().equals(otherMessage.getHeadline()) &&
			getIssueDescription().equals(otherMessage.getIssueDescription()) &&
			getSpecificInfo().equals(otherMessage.getSpecificInfo()) &&
			getXArchRef().equals(otherMessage.getXArchRef()) &&
			c2.util.ArrayUtils.equals(getAffectedElements(), otherMessage.getAffectedElements());
	}
	
	public int hashCode(){
		return 
			getCategoryID().hashCode() ^
			getCategoryDescription().hashCode() ^
			getCriticID().hashCode() ^
			getIssueTypeID().hashCode() ^
			getHeadline().hashCode() ^
			getIssueDescription().hashCode() ^
			getSpecificInfo().hashCode() ^
			getXArchRef().hashCode() ^
			c2.util.ArrayUtils.arrayHashCode(getAffectedElements());
	}
	
	public Message duplicate(){
		return new CriticIssue(this);
	}
	
	public Identifier getCategoryID(){
		return (Identifier)getParameter("categoryID");
	}
	
	public Identifier getIssueTypeID(){
		return (Identifier)getParameter("issueTypeID");
	}
	
	public String getCategoryDescription(){
		return (String)getParameter("categoryDescription");
	}
	
	public ObjRef getXArchRef(){
		return (ObjRef)getParameter("xArchRef");
	}

	public String getHeadline(){
		return (String)getParameter("headline");
	}
		
	public String getIssueDescription(){
		return (String)getParameter("issueDescription");
	}

	public Identifier getCriticID(){
		return (Identifier)getParameter("criticID");
	}

	public String getSpecificInfo(){
		return (String)getParameter("specificInfo");
	}

	public ObjRef[] getAffectedElements(){
		return (ObjRef[])getParameter("affectedElements");
	}
	
}

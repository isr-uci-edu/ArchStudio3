package archstudio.tron;

import edu.uci.ics.xarchutils.ObjRef;

public class TronIssue implements java.io.Serializable{

	public final static int SEVERITY_INFO = 100;
	public final static int SEVERITY_WARNING = 200;
	public final static int SEVERITY_ERROR = 300;
	
	protected String testUID;
	protected ObjRef documentRef;
	protected String toolID;
	protected int severity;
	protected String headline;
	protected String detailedDescription;
	protected String iconHref;
	
	protected TronElementIdentifier[] elementIdentifiers;
	
	public TronIssue(String testUID, ObjRef documentRef, String toolID,
		int severity, String headline, String detailedDescription, String iconHref,
		TronElementIdentifier[] elementIdentifiers){
		super();
		this.testUID = testUID;
		this.documentRef = documentRef;
		this.toolID = toolID;
		this.severity = severity;
		this.headline = headline;
		this.detailedDescription = detailedDescription;
		this.iconHref = iconHref;
		this.elementIdentifiers = elementIdentifiers;
	}

	public String getDetailedDescription(){
		return detailedDescription;
	}
	
	public void setDetailedDescription(String detailedDescription){
		this.detailedDescription = detailedDescription;
	}
	
	public ObjRef getDocumentRef(){
		return documentRef;
	}
	
	public void setDocumentRef(ObjRef documentRef){
		this.documentRef = documentRef;
	}
	
	public TronElementIdentifier[] getElementIdentifiers(){
		return elementIdentifiers;
	}
	
	public void setElementIdentifiers(TronElementIdentifier[] elementIdentifiers){
		this.elementIdentifiers = elementIdentifiers;
	}
	
	public String getHeadline(){
		return headline;
	}
	
	public void setHeadline(String headline){
		this.headline = headline;
	}
	
	public String getIconHref(){
		return iconHref;
	}
	
	public void setIconHref(String iconHref){
		this.iconHref = iconHref;
	}
	
	public int getSeverity(){
		return severity;
	}
	
	public void setSeverity(int severity){
		this.severity = severity;
	}
	
	public String getToolID(){
		return toolID;
	}
	
	public void setToolID(String toolID){
		this.toolID = toolID;
	}
	
	public String getTestUID(){
		return testUID;
	}
	
	public void setTestUID(String testUID){
		this.testUID = testUID;
	}
	
	public String toString(){
		StringBuffer buf = new StringBuffer("TronIssue[");
		buf.append("headline=").append(headline).append("; ");
		buf.append("detailedDescription=").append(detailedDescription).append("; ");
		buf.append("testUID=").append(testUID).append("; ");
		buf.append("documentRef=").append(documentRef).append("; ");
		buf.append("toolID=").append(toolID).append("; ");
		String severityString = "" + severity;
		switch(severity){
		case SEVERITY_ERROR:
			severityString = "error";
			break;
		case SEVERITY_WARNING:
			severityString = "warning";
			break;
		case SEVERITY_INFO:
			severityString = "info";
			break;
		}
		buf.append("severity=").append(severityString).append("; ");
		if(elementIdentifiers == null){
			buf.append("elementIdentifiers=null; ");
		}
		else{
			for(int i = 0; i < elementIdentifiers.length; i++){
				buf.append("elementIdentifier[").append(i).append("]=").append(elementIdentifiers[i]).append("; ");
			}
		}
		buf.append("iconHref=").append(iconHref).append("];");
		return buf.toString();
	}
	
	public boolean equals(Object o){
		if(!(o instanceof TronIssue)){
			return false;
		}
		TronIssue otherIssue = (TronIssue)o; 
		java.util.Collection c1 = java.util.Arrays.asList(elementIdentifiers);
		java.util.Collection c2 = java.util.Arrays.asList(otherIssue.elementIdentifiers);
		
		return nulleq(testUID, otherIssue.testUID) &&
			nulleq(documentRef, otherIssue.documentRef) &&
			nulleq(toolID, otherIssue.toolID) &&
			(severity == otherIssue.severity) &&
			nulleq(headline, otherIssue.headline) &&
			nulleq(detailedDescription, otherIssue.detailedDescription) &&
			nulleq(iconHref, otherIssue.iconHref) &&
			c1.containsAll(c2) && c2.containsAll(c1);
	}
	
	public int hashCode(){
		return hc(testUID) ^ hc(toolID) ^ hc(headline); 
	}
	
	private static int hc(Object o){
		if(o == null) return Object.class.hashCode();
		return o.hashCode();
	}
	
	private static boolean nulleq(Object o1, Object o2){
		if((o1 == null) && (o2 == null)) return true;
		if((o1 == null) && (o2 != null)) return false;
		if((o1 != null) && (o2 == null)) return false;
		return o1.equals(o2);
	}
}

package archstudio.comp.plamerge;

import c2.fw.*;

public class PerformPLAMergeMessage extends NamedPropertyMessage
{
	public PerformPLAMergeMessage(String diffURL, String archURL, String targetArchURL, String startingID, boolean isStructural, String componentID)
	{
		super("PerformPLAMergeMessage");
		super.addParameter("diffURL", diffURL);
		super.addParameter("archURL", archURL);
		super.addParameter("targetArchURL", targetArchURL);
		super.addParameter("startingID", startingID);
		super.addParameter("isStructural", isStructural);
		super.addParameter("componentID", componentID);
	}
	
	protected PerformPLAMergeMessage(PerformPLAMergeMessage copy)
	{
		super(copy);
	}
	
	public Message duplicate()
	{
		return new PerformPLAMergeMessage(this);
	}
	
	public void setDiffURL(String diffURL)
	{
		addParameter("diffURL", diffURL);
	}
	
	public String getDiffURL()
	{
		return (String)getParameter("diffURL");
	}
	
	public void setArchURL(String archURL)
	{
		addParameter("archURL", archURL);
	}
	
	public String getArchURL()
	{
		return (String)getParameter("archURL");
	}
	
	public void setTargetArchURL(String targetArchURL)
	{
		addParameter("targetArchURL", targetArchURL);
	}
	
	public String getTargetArchURL()
	{
		return (String)getParameter("targetArchURL");
	}
	
	public void setStartingID(String startingID)
	{
		addParameter("startingID", startingID);
	}
	
	public String getStartingID()
	{
		return (String)getParameter("startingID");
	}
	
	public void setIsStructural(boolean isStructural)
	{
		addParameter("isStructural", isStructural);
	}
	
	public boolean getIsStructural()
	{
		return (boolean)getBooleanParameter("isStructural");
	}
	
	public void setComponentID(String componentID)
	{
		addParameter("componentID", componentID);
	}
	
	public String getComponentID()
	{
		return (String)getParameter("componentID");
	}
}
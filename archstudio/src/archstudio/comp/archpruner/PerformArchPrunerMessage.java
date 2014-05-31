package archstudio.comp.archpruner;

import c2.fw.*;

/**
 * This PerformArchPrunerMessage class is a specific message that holds fields used
 * in pruning.  This message is intended for the ArchPruner component and it is that
 * component that will make use of the parameters stored in this message.
 */
public class PerformArchPrunerMessage extends NamedPropertyMessage
{
	/**
     * Constructor for the PerformArchPrunerMessage
     *
     * @param archURL This is the URL of the xADL document that needs to be pruned
     * @param targetArchURL This is the URL of the new xADL document that will be created and store the pruned architecture
     * @param componentID ID of the component creating the message
	 * @param startingID This is the ID of the element that the pruning algorithm should start from
     * @param isStructural If this is true then the startingID is that of an archStruct, otherwise it is an ID to a type
     */
	public PerformArchPrunerMessage(String archURL, String targetArchURL, String componentID, String startingID, boolean isStructural)
	{
		super("PerformArchPrunerMessage");
		super.addParameter("archURL", archURL);
		super.addParameter("targetArchURL", targetArchURL);
		super.addParameter("componentID", componentID);
		super.addParameter("startingID", startingID);
		super.addParameter("isStructural", isStructural);
	}
	
	protected PerformArchPrunerMessage(PerformArchPrunerMessage copy)
	{
		super(copy);
	}
	
	public Message duplicate()
	{
		return new PerformArchPrunerMessage(this);
	}
	public String getComponentID()
	{
		return (String)getParameter("componentID");
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

	public  boolean getIsStructural()
	{
		return getBooleanParameter("isStructural");
	}
}
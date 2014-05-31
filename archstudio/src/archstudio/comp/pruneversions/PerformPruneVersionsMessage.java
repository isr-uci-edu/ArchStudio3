package archstudio.comp.pruneversions;

import c2.fw.*;

/**
 * This PerformPruneVersionsMessage class is a specific message that holds fields used
 * in pruning.  This message is intended for the PruneVersions component and it is that
 * component that will make use of the parameters stored in this message.
 * 
 * @author Christopher Van der Westhuizen <A HREF="mailto:vanderwe@uci.edu">(vanderwe@uci.edu)</A> 
 */
public class PerformPruneVersionsMessage extends NamedPropertyMessage
{
	/**
	 * Constructor for the PerformPruneVersionsMessage
	 *
	 * @param archURL This is the URL of the xADL document that needs to be pruned
	 * @param targetArchURL This is the URL of the new xADL document that will be created and store the pruned architecture
	 * @param componentID ID of the component creating the message
	 */
	public PerformPruneVersionsMessage(String archURL, String targetArchURL, String componentID)
	{
		super("PerformPruneVersionsMessage");
		super.addParameter("archURL", archURL);
		super.addParameter("targetArchURL", targetArchURL);
		super.addParameter("componentID", componentID);
	}
	
	/**
     * Constructor that acts as a copy constructor for the PerformPruneVersionsMessage.
     *
     * @param copy The instance of the PerformPruneVersionsMessage to copy
     */
	protected PerformPruneVersionsMessage(PerformPruneVersionsMessage copy)
	{
		super(copy);
	}
	
	/**
     * This method creates and returns a copy of this PerformPruneVersionsMessage instance
     *
     * @return A copy of this message
     */
	public Message duplicate()
	{
		return new PerformPruneVersionsMessage(this);
	}
	
	/**
     * This method returns the ID of the component that created and sent this message
     *
     * @return The ID of the component that created this message
     */
	public String getComponentID()
	{
		return (String)getParameter("componentID");
	}
	
	/**
     * This method stores the URL of the document to be pruned
     *
     * @param archURL The URL of the document to be pruned
     */
	public void setArchURL(String archURL)
	{
		addParameter("archURL", archURL);
	}
	
	/**
     * This method retrieves the URL of the document to be pruned
     * 
     * @return The URL of the document to be pruned
     */
	public String getArchURL()
	{
		return (String)getParameter("archURL");
	}
	
	/**
     * This method stores the URL of the document that will be the resultant document after
     * applying the prune.
     *
     * @targetArchURL The URL of the resultant pruned document
     */
	public void setTargetArchURL(String targetArchURL)
	{
		addParameter("targetArchURL", targetArchURL);
	}
	
	/**
     * This method retrieves the URL of the resultant pruned document 
     * 
     * @return The URL of the resultant pruned document
     */ 
	public String getTargetArchURL()
	{
		return (String)getParameter("targetArchURL");
	}
}
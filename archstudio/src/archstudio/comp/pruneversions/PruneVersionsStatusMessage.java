package archstudio.comp.pruneversions;


import c2.fw.*;

/**
 * This PruneVersionsStatusMessage class is a specific message that holds fields used
 * to represent the current status of a pruning operation.  This message is created and 
 * sent out by the PruneVersions component and is intended for any component (for example,
 * a graphical component) that needs knowledge of the progress of a particular "version 
 * pruning" operation. 
 * 
 * @author Christopher Van der Westhuizen <A HREF="mailto:vanderwe@uci.edu">(vanderwe@uci.edu)</A> 
 */
public class PruneVersionsStatusMessage extends NamedPropertyMessage
{
	/**
	 * Constructor for the PruneVersionsStatusMessage
	 *
	 * @param prunedArchURL 	The URL of the document that contains (will contain) the final pruned architecture
	 * @param componentID		ID of the component that requested the version pruning service
	 * @param currentValue		Value representing the current progress of the pruning operation
	 * @param upperBound		The upper bound on the progress value.  When the current progress value reaches the upper bound the pruning is done.
	 * @param isDone			Indicates whether or not the version pruning process is done
	 * @param errorOccurred		Indicates whether or not an error occured during the version pruning process
	 * @param error				This is the error that occurred; or null if no error occurred
	 */
	public PruneVersionsStatusMessage(String prunedArchURL, String componentID, int currentValue, int upperBound, boolean isDone, boolean errorOccurred, Exception error)
	{
		super("PruneVersionsStatusMessage");
		super.addParameter("prunedArchURL", prunedArchURL);
		super.addParameter("componentID", componentID);
		super.addParameter("currentValue", currentValue);
		super.addParameter("upperBound", upperBound);
		super.addParameter("isDone", isDone);
		super.addParameter("errorOccurred", errorOccurred);
		super.addParameter("error", error);
	}
	
	/**
	 * Constructor that acts as a copy constructor for the PruneVersionsStatusMessage.
	 *
	 * @param copy The instance of the PruneVersionsStatusMessage to copy
	 */
	protected PruneVersionsStatusMessage(PruneVersionsStatusMessage copy)
	{
		super(copy);
	}
	
	/**
	 * This method creates and returns a copy of this PruneVersionsStatusMessage instance
	 *
	 * @return A copy of this message
	 */
	public Message duplicate()
	{
		return new PruneVersionsStatusMessage(this);
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
	 * This method returns whether or not the current version prune service is done or not
	 *
	 * @return A boolean value indicating whether or not the operation is done
	 */
	public boolean getIsDone()
	{
		return getBooleanParameter("isDone");
	}
	
	/**
	 * This method returns current value of progress
	 *
	 * @return Current value of progress
	 */
	public int getCurrentValue()
	{
		return getIntParameter("currentValue");
	}
	
	/**
	 * This method retrieves the upper bound on the range of progress values
	 *
	 * @return The upper bound of the progress
	 */
	public int getUpperBound()
	{
		return getIntParameter("upperBound");
	}
	
	/**
	 * This method stores the URL of the document that will be the resultant document after
	 * applying the prune.
	 *
	 * @prunedArchURL The URL of the resultant pruned document
	 */
	public void setPrunedArchURL(String prunedArchURL)
	{
		addParameter("prunedArchURL", prunedArchURL);
	}
	
	/**
	 * This method retrieves the URL of the resultant pruned document 
	 * 
	 * @return The URL of the resultant pruned document
	 */
	public String getPrunedArchURL()
	{
		return (String)getParameter("prunedArchURL");
	}
	
	/**
	 * This method stores a boolean value indicating whether an error occurred or not
	 * 
	 * @param errorOccurred Value indicating whether or not an error occurred
	 */
	public void setErrorOccurred(boolean errorOccurred)
	{
		addParameter("errorOccurred", errorOccurred);
	}
	
	/**
	 * This method returns whether or not an error occurred
	 * 
	 * @return Value indicating whether or not an error occurred
	 */
	public boolean getErrorOccurred()
	{
		return getBooleanParameter("errorOccurred");
	}
	
	/**
     * This method stores the error (if any) that occurred 
     * 
     * @param error The exception indicative of the error that took place
     */
	public void setError(Exception error)
	{
		addParameter("error", error);
	}
	
	/**
     * This method returns the error that took place
     *
     * @return The exception describing the error that took place or null if no error is stored
     */
	public Exception getError()
	{
		return (Exception)getParameter("error");
	}
}
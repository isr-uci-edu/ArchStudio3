package archstudio.comp.pladiff;

import c2.fw.*;

public class PLADiffStatusMessage extends NamedPropertyMessage
{
	public PLADiffStatusMessage(String diffArchURL, String componentID, int currentValue, int upperBound ,boolean isDone, boolean errorOccurred, Exception error)
	{
		super("PLADiffStatusMessage");
		super.addParameter("diffArchURL", diffArchURL);
		super.addParameter("componentID", componentID);
		super.addParameter("currentValue", currentValue);
		super.addParameter("upperBound", upperBound);
		super.addParameter("isDone", isDone);
		super.addParameter("errorOccurred", errorOccurred);
		super.addParameter("error", error);
	}
	
	protected PLADiffStatusMessage(PLADiffStatusMessage copy)
	{
		super(copy);
	}
	public String getComponentID()
	{
		return (String)getParameter("componentID");
	}
	public boolean getIsDone()
	{
		return getBooleanParameter("isDone");
	}
	//current value you set in contructor
	public int getCurrentValue()
	{
		return getIntParameter("currentValue");
	}
	//max value for progress bar, when we hit this we're done
	public int getUpperBound()
	{
		return getIntParameter("upperBound");
	}
	public Message duplicate()
	{
		return new PLADiffStatusMessage(this);
	}
	
	public void setDiffArchURL(String diffArchURL)
	{
		addParameter("diffArchURL", diffArchURL);
	}
	
	public String getDiffArchURL()
	{
		return (String)getParameter("diffArchURL");
	}
	
	public void setErrorOccurred(boolean errorOccurred)
	{
		addParameter("errorOccurred", errorOccurred);
	}
	
	public boolean getErrorOccurred()
	{
		return getBooleanParameter("errorOccurred");
	}
	
	public void setError(Exception error)
	{
		addParameter("error", error);
	}
	
	public Exception getError()
	{
		return (Exception)getParameter("error");
	}
}
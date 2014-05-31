package archstudio.comp.pladiff;

import c2.fw.*;

public class PerformPLADiffMessage extends NamedPropertyMessage
{
	public PerformPLADiffMessage(String origArchURL, String newArchURL, String diffURL,
		String origStartingID, String newStartingID, boolean isStructural, String componentID)
	{
		super("PerformPLADiffMessage");
		super.addParameter( "origArchURL", origArchURL );
		super.addParameter( "newArchURL", newArchURL );
		super.addParameter( "diffURL", diffURL);
		super.addParameter( "origStartingID", origStartingID );
		super.addParameter( "newStartingID", newStartingID );
		super.addParameter( "isStructural", isStructural );
		super.addParameter( "componentID", componentID );
	}
	
	protected PerformPLADiffMessage( PerformPLADiffMessage copy )
	{
		super( copy );
	}
	
	public Message duplicate()
	{
		return new PerformPLADiffMessage( this );
	}
	
	// gets and sets the original architecture URL
	public void setOrigArchURL(String origArchURL)
	{
		addParameter( "origArchURL", origArchURL );
	}
	public String getOrigArchURL()
	{
		return (String)getParameter( "origArchURL" );
	}
	
	// gets and sets the new architecture URL
	public void setNewArchURL(String newArchURL)
	{
		addParameter("newArchURL", newArchURL);
	}
	
	public String getNewArchURL()
	{
		return (String)getParameter("newArchURL");
	}
	
	// gets and sets the diff URL
	public void setDiffURL(String diffURL)
	{
		addParameter("diffURL", diffURL);
	}
	
	public String getDiffURL()
	{
		return (String)getParameter("diffURL");
	}
	
	// gets and sets the original starting ID
	public String getOrigStartingID()
	{
		return (String)getParameter("origStartingID");
	}
	public void setOrigStartingID( String origStartingID )
	{
		addParameter("origStartingID", origStartingID);
	}
	
	// sets and gets the new starting ID
	public void setNewStartingID(String newStartingID)
	{
		addParameter("newStartingID", newStartingID);
	}
	
	public String getNewStartingID()
	{
		return (String)getParameter("newStartingID");
	}
	
	
	// gets and sets the is structural
	public void setIsStructural(boolean isStructural)
	{
		addParameter( "isStructural", isStructural );
	}
	
	public boolean getIsStructural()
	{
		return getBooleanParameter( "isStructural" );
	}
	
	// gets and sets the component ID
	public void setComponentID(String componentID)
	{
		addParameter( "componentID", componentID );
	}
	public String getComponentID()
	{
		return (String)getParameter("componentID");
	}
}
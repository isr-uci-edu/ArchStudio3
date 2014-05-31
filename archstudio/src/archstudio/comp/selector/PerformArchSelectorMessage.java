package archstudio.comp.selector;

import c2.fw.*;
import archstudio.comp.booleaneval.*; 

public class PerformArchSelectorMessage extends NamedPropertyMessage
{
	public PerformArchSelectorMessage(String archURL, String targetArchURL, String componentID, SymbolTable table, String startingID, boolean isStructural)
	{
		super("PerformArchSelectorMessage");
		super.addParameter("archURL", archURL);
		super.addParameter("targetArchURL", targetArchURL);
		super.addParameter("componentID", componentID);
		super.addParameter("table", table);
		super.addParameter("startingID", startingID);
		super.addParameter("isStructural", isStructural);
	}
	
	protected PerformArchSelectorMessage(PerformArchSelectorMessage copy)
	{
		super(copy);
	}
	
	public Message duplicate()
	{
		return new PerformArchSelectorMessage(this);
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
	public SymbolTable getSymbolTable()
	{
		return (SymbolTable)getParameter("table");
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
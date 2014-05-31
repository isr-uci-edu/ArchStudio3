package archstudio.comp.guardtracker;

import c2.fw.*;

public class AllGuardsMessage extends NamedPropertyMessage{
	public AllGuardsMessage(String[] guardStrings){
		super("AllGuardsMessage");
		super.addParameter("guardStrings", guardStrings);
	}

	protected AllGuardsMessage(AllGuardsMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new AllGuardsMessage(this);
	}

	public void setGuardStrings(String[] guardStrings){
		addParameter("guardStrings", guardStrings);
	}

	public String[] getGuardStrings(){
		return (String[])getParameter("guardStrings");
	}

}


package archstudio.comp.guardtracker;

import c2.fw.*;
import edu.uci.ics.xarchutils.ObjRef;

public class GuardsMessage extends NamedPropertyMessage{
	public GuardsMessage(ObjRef xArchRef, String[] guardStrings){
		super("GuardsMessage");
		super.addParameter("xArchRef", xArchRef);
		super.addParameter("guardStrings", guardStrings);
	}

	protected GuardsMessage(GuardsMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new GuardsMessage(this);
	}

	public void setXArchRef(ObjRef xArchRef){
		addParameter("xArchRef", xArchRef);
	}

	public ObjRef getXArchRef(){
		return (ObjRef)getParameter("xArchRef");
	}

	public void setGuardStrings(String[] guardStrings){
		addParameter("guardStrings", guardStrings);
	}

	public String[] getGuardStrings(){
		return (String[])getParameter("guardStrings");
	}

}


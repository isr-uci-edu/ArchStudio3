package archstudio.archon;

import c2.fw.*;

public class ArchonDestroyInterpreterMessage extends NamedPropertyMessage{
	public ArchonDestroyInterpreterMessage(String interpreterID){
		super("ArchonDestroyInterpreterMessage");
		super.addParameter("interpreterID", interpreterID);
	}

	protected ArchonDestroyInterpreterMessage(ArchonDestroyInterpreterMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new ArchonDestroyInterpreterMessage(this);
	}

	public void setInterpreterID(String interpreterID){
		addParameter("interpreterID", interpreterID);
	}

	public String getInterpreterID(){
		return (String)getParameter("interpreterID");
	}

}


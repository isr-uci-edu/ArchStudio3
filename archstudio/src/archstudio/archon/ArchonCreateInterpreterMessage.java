package archstudio.archon;

import c2.fw.*;

public class ArchonCreateInterpreterMessage extends NamedPropertyMessage{
	public ArchonCreateInterpreterMessage(String interpreterID){
		super("ArchonCreateInterpreterMessage");
		super.addParameter("interpreterID", interpreterID);
	}

	protected ArchonCreateInterpreterMessage(ArchonCreateInterpreterMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new ArchonCreateInterpreterMessage(this);
	}

	public void setInterpreterID(String interpreterID){
		addParameter("interpreterID", interpreterID);
	}

	public String getInterpreterID(){
		return (String)getParameter("interpreterID");
	}

}


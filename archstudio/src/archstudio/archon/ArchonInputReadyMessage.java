package archstudio.archon;

import c2.fw.*;

public class ArchonInputReadyMessage extends NamedPropertyMessage{
	public ArchonInputReadyMessage(String interpreterID, boolean continuing){
		super("ArchonInputReadyMessage");
		super.addParameter("interpreterID", interpreterID);
		super.addParameter("continuing", continuing);
	}

	protected ArchonInputReadyMessage(ArchonInputReadyMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new ArchonInputReadyMessage(this);
	}

	public void setInterpreterID(String interpreterID){
		addParameter("interpreterID", interpreterID);
	}

	public String getInterpreterID(){
		return (String)getParameter("interpreterID");
	}

	public void setContinuing(boolean continuing){
		addParameter("continuing", continuing);
	}

	public boolean getContinuing(){
		return getBooleanParameter("continuing");
	}

}


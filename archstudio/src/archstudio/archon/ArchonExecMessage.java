package archstudio.archon;

import c2.fw.*;

public class ArchonExecMessage extends NamedPropertyMessage{
	
	public ArchonExecMessage(String interpreterID, String command){
		this(interpreterID, new String[]{command});
	}
	
	public ArchonExecMessage(String interpreterID, String[] commands){
		super("ArchonExecMessage");
		super.addParameter("interpreterID", interpreterID);
		super.addParameter("commands", commands);
	}

	protected ArchonExecMessage(ArchonExecMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new ArchonExecMessage(this);
	}

	public void setInterpreterID(String interpreterID){
		addParameter("interpreterID", interpreterID);
	}

	public String getInterpreterID(){
		return (String)getParameter("interpreterID");
	}

	public void setCommands(String[] commands){
		addParameter("commands", commands);
	}

	public String[] getCommands(){
		return (String[])getParameter("commands");
	}

}


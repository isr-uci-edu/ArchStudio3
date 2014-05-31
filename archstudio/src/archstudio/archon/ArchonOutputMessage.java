package archstudio.archon;

import c2.fw.*;

public class ArchonOutputMessage extends NamedPropertyMessage{
	
	public static final int STREAM_OUT = 100;
	public static final int STREAM_ERR = 200;
	public static final int STREAM_ECHO = 300;
	
	public ArchonOutputMessage(String interpreterID, int stream, String output){
		super("ArchonOutputMessage");
		super.addParameter("interpreterID", interpreterID);
		super.addParameter("stream", stream);
		super.addParameter("output", output);
	}

	protected ArchonOutputMessage(ArchonOutputMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new ArchonOutputMessage(this);
	}

	public void setInterpreterID(String interpreterID){
		addParameter("interpreterID", interpreterID);
	}

	public String getInterpreterID(){
		return (String)getParameter("interpreterID");
	}

	public void setStream(int stream){
		addParameter("stream", stream);
	}

	public int getStream(){
		return getIntParameter("stream");
	}

	public void setOutput(String output){
		addParameter("output", output);
	}

	public String getOutput(){
		return (String)getParameter("output");
	}

}


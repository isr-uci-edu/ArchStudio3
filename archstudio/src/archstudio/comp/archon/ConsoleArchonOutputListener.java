package archstudio.comp.archon;

public class ConsoleArchonOutputListener implements ArchonOutputListener{
	
	protected boolean echo = false;
	
	public ConsoleArchonOutputListener(boolean echoOn){
		this.echo = echoOn;
	}
	
	public void archonLine(String interpreterID, String line){
	}
	
	public void archonEchoLine(String interpreterID, String line){
		if(echo) System.out.println(line);
	}
	
	public void archonStdoutLine(String interpreterID, String line){
		System.out.println(line);
	}

	public void archonStderrLine(String interpreterID, String line){
		System.err.println(line);
	}
}

package archstudio.comp.archon;

public interface ArchonOutputListener{
	public void archonLine(String interpreterID, String line);
	public void archonStdoutLine(String interpreterID, String line);
	public void archonStderrLine(String interpreterID, String line);
	public void archonEchoLine(String interpreterID, String line);
}

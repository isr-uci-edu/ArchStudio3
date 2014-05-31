package archstudio.comp.archmerge;

import c2.fw.*;

public class ArchMergeStatusMessage extends NamedPropertyMessage{
	public ArchMergeStatusMessage(String architectureURI, String diffURI, boolean errorOccurred, Exception error){
		super("ArchMergeStatusMessage");
		super.addParameter("architectureURI", architectureURI);
		super.addParameter("diffURI", diffURI);
		super.addParameter("errorOccurred", errorOccurred);
		super.addParameter("error", error);
	}

	protected ArchMergeStatusMessage(ArchMergeStatusMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new ArchMergeStatusMessage(this);
	}

	public void setArchitectureURI(String architectureURI){
		addParameter("architectureURI", architectureURI);
	}

	public String getArchitectureURI(){
		return (String)getParameter("architectureURI");
	}

	public void setDiffURI(String diffURI){
		addParameter("diffURI", diffURI);
	}

	public String getDiffURI(){
		return (String)getParameter("diffURI");
	}

	public void setErrorOccurred(boolean errorOccurred){
		addParameter("errorOccurred", errorOccurred);
	}

	public boolean getErrorOccurred(){
		return getBooleanParameter("errorOccurred");
	}

	public void setError(Exception error){
		addParameter("error", error);
	}

	public Exception getError(){
		return (Exception)getParameter("error");
	}

}


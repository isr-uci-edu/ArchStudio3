package archstudio.comp.archdiff;

import c2.fw.*;

public class ArchDiffStatusMessage extends NamedPropertyMessage{
	public ArchDiffStatusMessage(String diffArchURI, boolean errorOccurred, Exception error){
		super("ArchDiffStatusMessage");
		super.addParameter("diffArchURI", diffArchURI);
		super.addParameter("errorOccurred", errorOccurred);
		super.addParameter("error", error);
	}

	protected ArchDiffStatusMessage(ArchDiffStatusMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new ArchDiffStatusMessage(this);
	}

	public void setDiffArchURI(String diffArchURI){
		addParameter("diffArchURI", diffArchURI);
	}

	public String getDiffArchURI(){
		return (String)getParameter("diffArchURI");
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


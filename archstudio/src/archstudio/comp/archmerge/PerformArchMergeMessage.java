package archstudio.comp.archmerge;

import c2.fw.*;

public class PerformArchMergeMessage extends NamedPropertyMessage{
	public PerformArchMergeMessage(String architectureURI, String diffURI){
		super("PerformArchMergeMessage");
		super.addParameter("architectureURI", architectureURI);
		super.addParameter("diffURI", diffURI);
	}

	protected PerformArchMergeMessage(PerformArchMergeMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new PerformArchMergeMessage(this);
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

}


package archstudio.comp.archdiff;

import c2.fw.*;

public class PerformArchDiffMessage extends NamedPropertyMessage{
	public PerformArchDiffMessage(String origArchURI, String newArchURI, String diffArchURI){
		super("PerformArchDiffMessage");
		super.addParameter("origArchURI", origArchURI);
		super.addParameter("newArchURI", newArchURI);
		super.addParameter("diffArchURI", diffArchURI);
	}

	protected PerformArchDiffMessage(PerformArchDiffMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new PerformArchDiffMessage(this);
	}

	public void setOrigArchURI(String origArchURI){
		addParameter("origArchURI", origArchURI);
	}

	public String getOrigArchURI(){
		return (String)getParameter("origArchURI");
	}

	public void setNewArchURI(String newArchURI){
		addParameter("newArchURI", newArchURI);
	}

	public String getNewArchURI(){
		return (String)getParameter("newArchURI");
	}

	public void setDiffArchURI(String diffArchURI){
		addParameter("diffArchURI", diffArchURI);
	}

	public String getDiffArchURI(){
		return (String)getParameter("diffArchURI");
	}

}


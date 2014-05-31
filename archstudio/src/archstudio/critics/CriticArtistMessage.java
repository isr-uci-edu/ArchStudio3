package archstudio.critics;

import c2.fw.*;

public class CriticArtistMessage extends NamedPropertyMessage{
	
	public static final int FIDELITY_UNRENDERED = -1000;
	public static final int FIDELITY_LOW = 1;
	public static final int FIDELITY_MEDIUM = 5;
	public static final int FIDELITY_HIGH = 10;
	
	public CriticArtistMessage(CriticIssue criticIssue,
		java.awt.Component labelComponent, java.awt.Component contentComponent,
		int fidelity){
		super("CriticArtistMessage");
		super.addParameter("criticIssue", criticIssue);
		super.addParameter("labelComponent", labelComponent);
		super.addParameter("contentComponent", contentComponent);
		super.addParameter("fidelity", fidelity);
	}

	protected CriticArtistMessage(CriticArtistMessage copyMe){
		super(copyMe);
	}
	
	public Message duplicate(){
		return new CriticArtistMessage(this);
	}
	
	public CriticIssue getIssue(){
		return (CriticIssue)getParameter("criticIssue");
	}
	
	public java.awt.Component getLabelComponent(){
		return (java.awt.Component)getParameter("labelComponent");
	}
	
	public java.awt.Component getContentComponent(){
		return (java.awt.Component)getParameter("contentComponent");
	}

	public int getFidelity(){
		return getIntParameter("fidelity");
	}
}

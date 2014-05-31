package archstudio.critics;

import c2.fw.*;

public class CriticStatusMessage extends NamedPropertyMessage{
	
	public CriticStatusMessage(Identifier criticID, String description,
	Identifier[] dependencies, int status, boolean isApproved){
		super("CriticStatusMessage");
		super.addParameter("criticID", criticID);
		if(dependencies == null){
			dependencies = new Identifier[]{};
		}
		super.addParameter("description", description);
		super.addParameter("dependencies", dependencies);
		super.addParameter("status", status);
		super.addParameter("isApproved", isApproved);
	}
	
	protected CriticStatusMessage(CriticStatusMessage copyMe){
		super(copyMe);
	}
	
	public Message duplicate(){
		return new CriticStatusMessage(this);
	}

	public boolean getIsApproved(){
		return super.getBooleanParameter("isApproved");
	}
	
	public int getStatus(){
		return super.getIntParameter("status");
	}
	
	public String getDescription(){
		return (String)super.getParameter("description");
	}
	
	public Identifier[] getDependencies(){
		return (Identifier[])super.getParameter("dependencies");
	}
	
	public Identifier getCriticID(){
		return (Identifier)super.getParameter("criticID");
	}

}

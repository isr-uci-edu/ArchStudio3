package archstudio.critics;

import c2.fw.*;

public class CriticDependencyMissingMessage extends NamedPropertyMessage implements java.io.Serializable{
	
	public CriticDependencyMissingMessage(Identifier criticID, Identifier missingDependencyID){
		super("CriticDependencyMissingMessage");
		super.addParameter("criticID", criticID);
		super.addParameter("missingDependencyID", missingDependencyID);
	}
	
	protected CriticDependencyMissingMessage(CriticDependencyMissingMessage copyMe){
		super(copyMe);
	}
	
	public Message duplicate(){
		return new CriticDependencyMissingMessage(this);
	}

	public Identifier getCriticID(){
		return (Identifier)super.getParameter("criticID");
	}
	
	public Identifier getMissingDependencyID(){
		return (Identifier)super.getParameter("missingDependencyID");
	}
}
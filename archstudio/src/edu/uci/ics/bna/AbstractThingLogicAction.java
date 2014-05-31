package edu.uci.ics.bna;

public abstract class AbstractThingLogicAction implements ThingLogicAction{

	protected BNAComponent c = null;

	public AbstractThingLogicAction(){
		super();
	}
	
	public void setComponent(BNAComponent c){
		this.c = c;
	}
	
	public BNAComponent getBNAComponent(){
		return c;
	}

	public abstract void invoke();

}

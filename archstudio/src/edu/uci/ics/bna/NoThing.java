package edu.uci.ics.bna;

//This class is to be used as a placeholder in the Thing Trees for grouping
//Things for stacking purposes
public class NoThing extends AbstractThing implements Thing{
	
	public NoThing(String id){
		super(id);
	}
	
	public Class getPeerClass(){
		return NoThingPeer.class;
	}
}

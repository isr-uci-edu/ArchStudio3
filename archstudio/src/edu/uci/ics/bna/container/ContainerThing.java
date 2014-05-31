package edu.uci.ics.bna.container;

import javax.swing.JComponent;

import c2.util.UIDGenerator;
import edu.uci.ics.bna.BoxThing;
import edu.uci.ics.bna.Thing;

public class ContainerThing extends BoxThing implements Thing{

	public ContainerThing(){
		super();
		setID(UIDGenerator.generateUID("Container"));
	}

	public static final String CONTAINED_THING_IDS_PROPERTY_NAME = "containedThingIds";
	
	public String[] getContainedThingIDs(){
		return (String[])getProperty(CONTAINED_THING_IDS_PROPERTY_NAME);
	}
	
	public void setContainedThingIDs(String[] containedThingIds){
		setProperty(CONTAINED_THING_IDS_PROPERTY_NAME, containedThingIds);
	}

}

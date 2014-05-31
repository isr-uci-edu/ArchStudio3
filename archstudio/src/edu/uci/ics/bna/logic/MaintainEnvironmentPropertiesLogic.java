package edu.uci.ics.bna.logic;

import edu.uci.ics.bna.*;

public class MaintainEnvironmentPropertiesLogic extends ThingLogicAdapter implements CoordinateMapperListener{

	private EnvironmentPropertiesThing ept;

	protected void init(BNAModel m){
		ept = BNAUtils.getEnvironmentPropertiesThing(m);
		if(ept == null){
			ept = new EnvironmentPropertiesThing();
			m.addThing(ept);
		}
	}
	
	public void init(){
		BNAComponent c = getBNAComponent();
		c.getCoordinateMapper().addCoordinateMapperListener(this);
		this.init(c.getModel());
		ept.setScale(c.getCoordinateMapper().getScale());
		ept.setWorldOriginX(c.getCoordinateMapper().localXtoWorldX(0));
		ept.setWorldOriginY(c.getCoordinateMapper().localYtoWorldY(0));
	}

	public void coordinateMappingsChanged(CoordinateMapperEvent evt){
		if(ept != null){
			ept.setWorldOriginX(evt.getNewWorldOriginX());
			ept.setWorldOriginY(evt.getNewWorldOriginY());
			ept.setScale(evt.getNewScale());
		}
	}

}

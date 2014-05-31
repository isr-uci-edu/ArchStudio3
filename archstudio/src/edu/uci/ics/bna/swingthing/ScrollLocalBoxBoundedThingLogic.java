package edu.uci.ics.bna.swingthing;

import java.util.*;

import edu.uci.ics.bna.*;

public class ScrollLocalBoxBoundedThingLogic extends ThingLogicAdapter implements CoordinateMapperListener{

	protected int lastWorldOriginX = 0;
	protected int lastWorldOriginY = 0;

	protected double lastScale = 0;
	protected boolean lastWasScale = false;

	public ScrollLocalBoxBoundedThingLogic(){
		super();
	}
	
	public void coordinateMappingsChanged(CoordinateMapperEvent cme){
		BNAComponent c = getBNAComponent();
		if(c == null) return;
		BNAModel m = c.getModel();
		if(m == null) return;
		
		CoordinateMapper cm = c.getCoordinateMapper();
		
		boolean dontMoveThings = false;
		
		//This code prevents local objects from jumping
		//around wildly when we scale by suppressing
		//the move from catching the moveRelative that
		//inevitably comes right after a scale.
		double newScale = cm.getScale();
		if(newScale != lastScale){
			lastWasScale = true;
			lastScale = newScale;
		}
		else{
			if(lastWasScale){
				dontMoveThings = true;
				lastWasScale = false;
			}
		}
		
		int newWorldOriginX = cme.getNewWorldOriginX();
		int newWorldOriginY = cme.getNewWorldOriginY();
		
		int dxWorld = newWorldOriginX - lastWorldOriginX;
		int dyWorld = newWorldOriginY - lastWorldOriginY;
		
		int dxLocal = cm.worldXtoLocalX(dxWorld) - cm.worldXtoLocalX(0);
		int dyLocal = cm.worldYtoLocalY(dyWorld) - cm.worldYtoLocalY(0);
		
		//int dxLocal = c.getCoordinateMapper().worldXtoLocalX(dxWorld);
		//int dyLocal = c.getCoordinateMapper().worldYtoLocalY(dyWorld);
		
		/*
		System.out.println("dxWorld = " + dxWorld);
		System.out.println("dyWorld = " + dyWorld);

		System.out.println("dxLocal = " + dxLocal);
		System.out.println("dyLocal = " + dyLocal);
		*/
		
		if(!dontMoveThings){
			for(Iterator it = m.getThingIterator(); it.hasNext(); ){
				Thing t = (Thing)it.next();
				if(t instanceof ILocalDragMovable){
					ILocalDragMovable ldmt = (ILocalDragMovable)t;
					ldmt.localMoveRelative(-dxLocal, -dyLocal);
				}
			}
		}
		
		lastWorldOriginX = newWorldOriginX;
		lastWorldOriginY = newWorldOriginY;
		
	}
	
	public void init(){
		CoordinateMapper cm = getBNAComponent().getCoordinateMapper();
		lastWorldOriginX = cm.localXtoWorldX(0);
		lastWorldOriginY = cm.localYtoWorldY(0);
		lastScale = cm.getScale();
		//System.out.println("init:lastWorldOriginX = " + lastWorldOriginX); 
		//System.out.println("init:lastWorldOriginY = " + lastWorldOriginY); 
		cm.addCoordinateMapperListener(this);
	}
	

}

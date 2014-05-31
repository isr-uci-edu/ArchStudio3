package edu.uci.ics.bna.logic;

import java.util.Iterator;
import java.util.Set;

import edu.uci.ics.bna.*;

public class EphemeralLogic extends ThingLogicAdapter{
	
	protected Sapper sapper = null;
	
	public EphemeralLogic(){
		super();
	}
	
	public void init(){
		sapper = new Sapper();
		for(Iterator it = getBNAComponent().getModel().getThingIterator(); it.hasNext(); ){
			Thing t = (Thing)it.next();
			if(t instanceof IEphemeral){
				if(sapper != null){
					sapper.addThing((IEphemeral)t);
				}
			}
		}
	}
	
	public void destroy(){
		if(sapper != null){
			sapper.terminate();
		}
	}

	public void bnaModelChanged(BNAModelEvent evt){
		if(evt.getEventType() == BNAModelEvent.THING_ADDED){
			Thing t = evt.getTargetThing();
			if(t != null){
				if(t instanceof IEphemeral){
					if(sapper != null){
						sapper.addThing((IEphemeral)t);
					}
				}
			}
		}
		else if(evt.getEventType() == BNAModelEvent.THING_REMOVING){
			Thing t = evt.getTargetThing();
			if(t != null){
				if(t instanceof IEphemeral){
					if(sapper != null){
						sapper.removeThing((IEphemeral)t);
					}
				}
			}
		}
	}

	public class Sapper extends Thread{
		protected Set things = new java.util.HashSet();
		protected boolean shouldTerminate = false;
			
		protected Sapper(){
			setDaemon(true);
			start();
		}
		
		public void addThing(IEphemeral dm){
			synchronized(things){
				things.add(dm);
				things.notifyAll();
			}
		}
		
		public void removeThing(IEphemeral dm){
			synchronized(things){
				things.remove(dm);
				things.notifyAll();
			}
		}
		
		public void terminate(){
			synchronized(things){
				shouldTerminate = true;
				things.notifyAll();
			}
		}
		
		public void run(){
			while(true){
				if(shouldTerminate) return;
				
				try{
					Thread.sleep(100);
				}
				catch(InterruptedException e){
				}

				synchronized(things){
					if(things.size() == 0){
						try{
							things.wait();
						}
						catch(InterruptedException e){
						}
					}
					BNAComponent c = getBNAComponent();
					if(c != null){
						BNAModel m = c.getModel();
						if(m != null){
							try{
								m.beginBulkChange();
								for(Iterator it = things.iterator(); it.hasNext(); ){
									IEphemeral dm = (IEphemeral)it.next();
									float ephemeralTransparency = dm.getEphemeralTransparency();
									ephemeralTransparency = ephemeralTransparency - 0.05f;
									if(ephemeralTransparency <= 0){
										getBNAComponent().getModel().removeThing(dm);
										continue;
									}
									dm.setEphemeralTransparency(ephemeralTransparency);
								}
							}finally{
								m.endBulkChange();
							}
						}
					}
				}
			}
		}
	}
}

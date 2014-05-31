package edu.uci.ics.bna.logic;

import java.util.Iterator;
import java.util.Set;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.BNAModel;
import edu.uci.ics.bna.IOffset;
import edu.uci.ics.bna.ThingLogicAdapter;

public class OffsetRotatorLogic extends ThingLogicAdapter{
	
	protected OffsetRotatorTimer timer = null;
	
	public OffsetRotatorLogic(){
		super();
	}
	
	public void init(){
		timer = new OffsetRotatorTimer();
	}
	
	public void destroy(){
		if(timer != null){
			timer.terminate();
		}
	}

	public void addThingWithOffset(IOffset dm){
		if(timer != null){
			timer.addThingWithOffset(dm);
		}
	}
		
	public void removeThingWithOffset(IOffset dm){
		if(timer != null){
			timer.removeThingWithOffset(dm);
		}
	}


	public class OffsetRotatorTimer extends Thread{
		protected Set things = new java.util.HashSet();
		protected boolean shouldTerminate = false;
			
		protected OffsetRotatorTimer(){
			setDaemon(true);
			start();
		}
		
		public void addThingWithOffset(IOffset dm){
			synchronized(things){
				things.add(dm);
				things.notifyAll();
			}
		}
		
		public void removeThingWithOffset(IOffset dm){
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
					Thread.sleep(333);
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
									IOffset dm = (IOffset)it.next();
									int offset = dm.getOffset();
									offset++;
									offset %= 6;
									try{
										dm.setOffset(offset);
									}
									catch(Exception e){
										e.printStackTrace();
									}
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

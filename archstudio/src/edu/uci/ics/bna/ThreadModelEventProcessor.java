package edu.uci.ics.bna;

import java.util.*;

import c2.util.*;

class ThreadModelEventProcessor extends Thread{

	private Object flushLock = new Object();	
	private Object lock = new Object();
	private IQueue eventQueue = new SynchronizedGrowableWraparoundQueue();
	private boolean terminate = false;
	
	private Vector modelListeners = new Vector();
	private boolean listenerListChanged = true;
	
	private static int counter = 0;
	
	public ThreadModelEventProcessor(){
		setDaemon(true);
		setName("BNAThreadModelEventProcessor-" + (counter++));
		setPriority(Thread.MAX_PRIORITY);
		//start();
	}
	
	public void addModelListener(BNAModelListener ml){
		synchronized(modelListeners){
			modelListeners.addElement(ml);
			listenerListChanged = true;
		}
	}
	
	static final BNAModelListener[] emptyModelListenerArray = new BNAModelListener[0];
	public BNAModelListener[] getModelListeners(){
		return ((BNAModelListener[])modelListeners.toArray(emptyModelListenerArray));
	}
	
	public void removeModelListener(BNAModelListener ml){
		synchronized(modelListeners){
			modelListeners.removeElement(ml);
			listenerListChanged = true;
		}
	}
	
	public void waitForProcessing(){
		//if(true) return;
		synchronized(flushLock){
			while(!eventQueue.isEmpty()){
				try{
					flushLock.wait(100);
				}
				catch(InterruptedException ie){}
			}
		}
	}
	
	public void fireBNAModelEvent(BNAModelEvent evt){
		//System.err.println("ThreadedMessageProcessor got a message: " + evt.getThingEvent());

		synchronized(lock){
			eventQueue.enqueue(evt);
			lock.notify();
		}
		Thread.yield();
	}
	
	public void terminate(boolean t){
		this.terminate = t;
		synchronized(lock){
			lock.notifyAll();
		}
	}
	
	BNAModelListener[] modelListenerArray = null;
	
	public void run(){
		while(true){
			if(terminate){
				synchronized(flushLock){
					flushLock.notifyAll();
				}
				return;
			}
			
			if(listenerListChanged){
				modelListenerArray = getModelListeners();
				listenerListChanged = false;
			}
		
			//System.out.println("Checking for messages.");
			synchronized(flushLock){
				while(!eventQueue.isEmpty()){
					BNAModelEvent evt = null;
					evt = (BNAModelEvent)eventQueue.dequeue();
					//System.err.println("evt = " + evt);
					//System.err.println("thingevt = " + evt.getThingEvent());
					
					for(int i = 0; i < modelListenerArray.length; i++){
						try{
							modelListenerArray[i].bnaModelChanged(evt);
						}catch(Throwable t){
							t.printStackTrace();
						}
					}
					Thread.yield();
				}
				flushLock.notifyAll();
			}
			synchronized(lock){
				if(eventQueue.isEmpty()){
					try{
						lock.wait();
					}
					catch(InterruptedException e){}
				}
			}
		}
	}
			
}



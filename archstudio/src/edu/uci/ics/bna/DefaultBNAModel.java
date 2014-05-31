package edu.uci.ics.bna;

import java.util.*;

public class DefaultBNAModel implements BNAModel, ThingListener{
	
	protected Object thingTreeLock = new Object();
	protected ThingTree topThingTree;
	protected ThingTree middleThingTree;
	protected ThingTree bottomThingTree;

	protected ThreadModelEventProcessor threadModelEventProcessor;
	
	public DefaultBNAModel(){
		topThingTree = new ThingTree();
		middleThingTree = new ThingTree();
		bottomThingTree = new ThingTree();
		threadModelEventProcessor = new ThreadModelEventProcessor();
		threadModelEventProcessor.start();
	}
	
	public Object getLock(){
		return thingTreeLock;
	}
	
	public void addBNAModelListener(BNAModelListener l){
		threadModelEventProcessor.addModelListener(l);
	}
	
	public void removeBNAModelListener(BNAModelListener l){
		threadModelEventProcessor.removeModelListener(l);
	}
	
	protected void fireBNAModelEvent(int eventType, Thing targetThing){
		BNAModelEvent evt = new BNAModelEvent(this, eventType, targetThing);
		threadModelEventProcessor.fireBNAModelEvent(evt);
	}
	
	protected void fireBNAModelEvent(int eventType, Thing targetThing, ThingEvent thingEvent){
		BNAModelEvent evt = new BNAModelEvent(this, eventType, targetThing, thingEvent);
		threadModelEventProcessor.fireBNAModelEvent(evt);
	}
	
	public void fireStreamNotificationEvent(String streamNotificationEvent){
		BNAModelEvent evt = new BNAModelEvent(this, streamNotificationEvent);
		threadModelEventProcessor.fireBNAModelEvent(evt);
	}
	
	public void beginBulkChange(){
		fireBNAModelEvent(BNAModelEvent.BULK_CHANGE_BEGIN, null);
	}
	
	public void endBulkChange(){
		fireBNAModelEvent(BNAModelEvent.BULK_CHANGE_END, null);
	}
	
	/**
	 * Blocks the calling thread until all pending ThingEvents
	 * have been processed.  This is rarely needed, but useful 
	 * if a caller needs to wait for the various logics to have 
	 * processed a ThingEvent before making further changes.
	 */
	public void waitForProcessing(){
		threadModelEventProcessor.waitForProcessing();
	}
	
	public void addThing(Thing t){
		synchronized(thingTreeLock){
			t.addThingListener(this);
			int stackingPriority = BNAUtils.getStackingPriority(t);
			if(stackingPriority == BNAUtils.STACKING_PRIORITY_ALWAYS_ON_TOP){
				topThingTree.add(t);
			}
			else if(stackingPriority == BNAUtils.STACKING_PRIORITY_ALWAYS_ON_BOTTOM){
				bottomThingTree.add(t);
			}
			else if(stackingPriority == BNAUtils.STACKING_PRIORITY_MIDDLE){
				middleThingTree.add(t);
			}
			updateListCache();
			t.adding();
			fireBNAModelEvent(BNAModelEvent.THING_ADDED, t);
		}
	}
	
	public void addThing(Thing t, Thing parentThing){
		synchronized(thingTreeLock){
			t.addThingListener(this);
			int stackingPriority = BNAUtils.getStackingPriority(t);
			if(stackingPriority == BNAUtils.STACKING_PRIORITY_ALWAYS_ON_TOP){
				topThingTree.add(t, parentThing);
			}
			else if(stackingPriority == BNAUtils.STACKING_PRIORITY_ALWAYS_ON_BOTTOM){
				bottomThingTree.add(t, parentThing);
			}
			else if(stackingPriority == BNAUtils.STACKING_PRIORITY_MIDDLE){
				middleThingTree.add(t, parentThing);
			}
			updateListCache();
			t.adding();
			fireBNAModelEvent(BNAModelEvent.THING_ADDED, t);
		}
	}
	
	public void removeThing(Thing t){
		synchronized(thingTreeLock){
			boolean removed = false;
			
			if(topThingTree.contains(t)){
				fireBNAModelEvent(BNAModelEvent.THING_REMOVING, t);
				t.removeThingListener(this);
				topThingTree.remove(t);
				fireBNAModelEvent(BNAModelEvent.THING_REMOVED, t);
				updateListCache();
				return;
			}
			if(middleThingTree.contains(t)){
				fireBNAModelEvent(BNAModelEvent.THING_REMOVING, t);
				t.removeThingListener(this);
				middleThingTree.remove(t);
				fireBNAModelEvent(BNAModelEvent.THING_REMOVED, t);
				updateListCache();
				return;
			}
			if(bottomThingTree.contains(t)){
				fireBNAModelEvent(BNAModelEvent.THING_REMOVING, t);
				t.removeThingListener(this);
				bottomThingTree.remove(t);
				fireBNAModelEvent(BNAModelEvent.THING_REMOVED, t);
				updateListCache();
				return;
			}
		}
	}
	
	public void removeThingAndChildren(Thing t){
		synchronized(thingTreeLock){
			boolean removed = false;
			
			if(topThingTree.contains(t)){
				fireBNAModelEvent(BNAModelEvent.THING_REMOVING, t);
				t.removeThingListener(this);
				topThingTree.removeWithChildren(t);
				fireBNAModelEvent(BNAModelEvent.THING_REMOVED, t);
				updateListCache();
				return;
			}
			if(middleThingTree.contains(t)){
				fireBNAModelEvent(BNAModelEvent.THING_REMOVING, t);
				t.removeThingListener(this);
				middleThingTree.removeWithChildren(t);
				fireBNAModelEvent(BNAModelEvent.THING_REMOVED, t);
				updateListCache();
				return;
			}
			if(bottomThingTree.contains(t)){
				fireBNAModelEvent(BNAModelEvent.THING_REMOVING, t);
				t.removeThingListener(this);
				bottomThingTree.removeWithChildren(t);
				fireBNAModelEvent(BNAModelEvent.THING_REMOVED, t);
				updateListCache();
				return;
			}
		}
	}
	
	public Thing getThing(String id){
		synchronized(thingTreeLock){
			for(Iterator it = middleThingTree.iterator(); it.hasNext(); ){
				Thing t = (Thing)it.next();
				if(t.getID().equals(id)){
					return t;
				}
			}
			for(Iterator it = topThingTree.iterator(); it.hasNext(); ){
				Thing t = (Thing)it.next();
				if(t.getID().equals(id)){
					return t;
				}
			}
			for(Iterator it = bottomThingTree.iterator(); it.hasNext(); ){
				Thing t = (Thing)it.next();
				if(t.getID().equals(id)){
					return t;
				}
			}
			return null;
		}
	}
	
	public void removeThing(String id){
		synchronized(thingTreeLock){
			for(Iterator it = middleThingTree.iterator(); it.hasNext(); ){
				Thing t = (Thing)it.next();
				if(t.getID().equals(id)){
					fireBNAModelEvent(BNAModelEvent.THING_REMOVING, t);
					t.removeThingListener(this);
					middleThingTree.remove(t);
					fireBNAModelEvent(BNAModelEvent.THING_REMOVED, t);
					updateListCache();
					return;
				}
			}
			for(Iterator it = topThingTree.iterator(); it.hasNext(); ){
				Thing t = (Thing)it.next();
				if(t.getID().equals(id)){
					fireBNAModelEvent(BNAModelEvent.THING_REMOVING, t);
					t.removeThingListener(this);
					topThingTree.remove(t);
					fireBNAModelEvent(BNAModelEvent.THING_REMOVED, t);
					updateListCache();
					return;
				}
			}
			for(Iterator it = bottomThingTree.iterator(); it.hasNext(); ){
				Thing t = (Thing)it.next();
				if(t.getID().equals(id)){
					fireBNAModelEvent(BNAModelEvent.THING_REMOVING, t);
					t.removeThingListener(this);
					bottomThingTree.remove(t);
					fireBNAModelEvent(BNAModelEvent.THING_REMOVED, t);
					updateListCache();
					return;
				}
			}
		}
	}
	
	ArrayList cachedThingList = new ArrayList();
	
	public void updateListCache(){
		synchronized(thingTreeLock){
			cachedThingList = new ArrayList();
			cachedThingList.addAll(bottomThingTree.asList());
			cachedThingList.addAll(middleThingTree.asList());
			cachedThingList.addAll(topThingTree.asList());
		}
	}
	
	public Iterator getThingIterator(){
		synchronized(thingTreeLock){
			//ArrayList list = new ArrayList();
			//list.addAll(bottomThingTree.asList());
			//list.addAll(middleThingTree.asList());
			//list.addAll(topThingTree.asList());
			//return list.iterator();
			return cachedThingList.iterator();
			
		}
	}
	
	public ListIterator getThingListIterator(int index){
		synchronized(thingTreeLock){
			//ArrayList list = new ArrayList();
			//list.addAll(bottomThingTree.asList());
			//list.addAll(middleThingTree.asList());
			//list.addAll(topThingTree.asList());
			return cachedThingList.listIterator(index);
		}
	}
	
	public int getNumThings(){
		synchronized(thingTreeLock){
			return topThingTree.size() + middleThingTree.size() + bottomThingTree.size();
		}
	}
	
	public Thing[] getAllThings(){
		synchronized(thingTreeLock){
			return (Thing[])cachedThingList.toArray(new Thing[0]);
		}
	}
	
	public void thingChanged(ThingEvent evt){
		fireBNAModelEvent(BNAModelEvent.THING_CHANGED, evt.getTargetThing(), evt);
	}

	public void stackAbove(Thing upperThing, Thing lowerThing){
		synchronized(thingTreeLock){
			if(!bottomThingTree.moveAfter(lowerThing, upperThing)){
				if(!middleThingTree.moveAfter(lowerThing, upperThing)){
					topThingTree.moveAfter(lowerThing, upperThing);
				}
			}
			updateListCache();
		}
	}
	
	public void bringToFront(Thing thing){
		synchronized(thingTreeLock){
			if(!bottomThingTree.bringToFront(thing)){
				if(!middleThingTree.bringToFront(thing)){
					topThingTree.bringToFront(thing);
				}
			}
			updateListCache();
		}
	}

	public void sendToBack(Thing thing){
		synchronized(thingTreeLock){
			if(!bottomThingTree.sendToBack(thing)){
				if(!middleThingTree.sendToBack(thing)){
					topThingTree.sendToBack(thing);
				}
			}
			updateListCache();
		}
	}
	
	public void dumpThingTree(Thing thing){
		synchronized(thingTreeLock){
			middleThingTree.dumpThingTree(thing);
			bottomThingTree.dumpThingTree(thing);
			topThingTree.dumpThingTree(thing);
		}
	}

}

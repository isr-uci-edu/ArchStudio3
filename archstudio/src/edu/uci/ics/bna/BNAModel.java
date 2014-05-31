package edu.uci.ics.bna;

public interface BNAModel{
	
	public Thing[] getAllThings();
	public void removeThing(String id);
	public Thing getThing(String id);
	public void removeThing(Thing t);
	public void removeThingAndChildren(Thing t);
	public void addThing(Thing t);
	public void addThing(Thing t, Thing parentThing);
	public int getNumThings();
	public void removeBNAModelListener(BNAModelListener l);
	public void addBNAModelListener(BNAModelListener l);
	public java.util.Iterator getThingIterator();
	public java.util.ListIterator getThingListIterator(int index);
	public void stackAbove(Thing upperThing, Thing lowerThing);
	public void bringToFront(Thing thing);
	public void sendToBack(Thing thing);

	public void fireStreamNotificationEvent(String streamNotification);
	
	/**
	 * Callers should invoke this when they are about to start a bulk change
	 * to the model, that is, many changes to the model in quick
	 * succession.  This generally results in a <code>BULK_CHANGE_BEGIN</code>
	 * event being emitted from the model and can provide listeners
	 * with a hint to hold normally per-event processing (like repaints)
	 * until the end of the bulk change. This method is optional;
	 * it need not be implemented by models.
	 * 
	 * Note that each call to beginBulkChange MUST be accompanied by
	 * a paired <code>endBulkChange()</code> call.
	 */
	public void beginBulkChange();

	/**
	 * Callers should invoke this when they have completed a bulk change
	 * to the model, that is, many changes to the model in quick
	 * succession.  This generally results in a <code>BULK_CHANGE_END</code>
	 * event being emitted from the model and can provide listeners
	 * with a hint to hold normally per-event processing (like repaints)
	 * until the end of the bulk change. This method is optional;
	 * it need not be implemented by models.
	 */
	public void endBulkChange();

	public Object getLock();

}

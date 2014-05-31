package edu.uci.ics.bna;

public class BNAModelEvent implements java.io.Serializable{
	
	public static final int THING_ADDED = 100;
	public static final int THING_REMOVING = 200;
	public static final int THING_REMOVED = 250;
	public static final int THING_CHANGED = 500;
	
	public static final int BULK_CHANGE_BEGIN = 1000;
	public static final int BULK_CHANGE_END = 1050;
	
	public static final int STREAM_NOTIFICATION_EVENT = 2000;
	
	protected BNAModel source;
	protected int eventType;
	protected Thing targetThing;
	protected ThingEvent thingEvent;
	protected String streamNotification;

	public BNAModelEvent(BNAModel source, String streamNotification){
		this.source = source;
		this.eventType = STREAM_NOTIFICATION_EVENT;
		this.streamNotification = streamNotification;
	}
	
	public BNAModelEvent(BNAModel source, int eventType, Thing targetThing){
		this.source = source;
		this.eventType = eventType;
		this.targetThing = targetThing;
		this.thingEvent = null;
	}
	
	public BNAModelEvent(BNAModel source, int eventType, Thing targetThing, ThingEvent thingEvent){
		this.source = source;
		this.eventType = eventType;
		this.targetThing = targetThing;
		this.thingEvent = thingEvent;
	}
	
	public String getStreamNotification(){
		return streamNotification;
	}
	
	public void setSource(BNAModel source){
		this.source = source;
	}
	
	public BNAModel getSource(){
		return source;
	}
	
	public void setEventType(int eventType){
		this.eventType = eventType;
	}
	
	public void setTargetThing(Thing targetThing){
		this.targetThing = targetThing;
	}
	
	public void setThingEvent(ThingEvent thingEvent){
		this.thingEvent = thingEvent;
	}
	
	public int getEventType(){
		return eventType;
	}
	
	public Thing getTargetThing(){
		return targetThing;
	}
	
	public ThingEvent getThingEvent(){
		return thingEvent;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer("BNAModelEvent[");
		sb.append("eventType=");
		switch(eventType){
		case THING_ADDED:
			sb.append("THING_ADDED");
			break;
		case THING_CHANGED:
			sb.append("THING_CHANGED");
			break;
		case THING_REMOVED:
			sb.append("THING_REMOVED");
			break;
		case BULK_CHANGE_BEGIN:
			sb.append("BULK_CHANGE_BEGIN");
			break;
		case BULK_CHANGE_END:
			sb.append("BULK_CHANGE_END");
			break;
		case STREAM_NOTIFICATION_EVENT:
			sb.append("STREAM_NOTIFICATION_EVENT");
			break;
		case THING_REMOVING:
			sb.append("THING_REMOVING");
			break;
		default:
			sb.append(eventType);
		}
		sb.append(";");
		sb.append("source=");
		sb.append(source);
		sb.append(";");
		sb.append("targetThing=");
		sb.append(targetThing);
		sb.append(";");
		sb.append("thingEvent=");
		sb.append(thingEvent);
		sb.append(";");
		sb.append("streamNotification=");
		sb.append(streamNotification);
		sb.append("]");
		return sb.toString();
	}
}

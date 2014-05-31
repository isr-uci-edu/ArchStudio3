package edu.uci.ics.bna;

public class ThingEvent implements java.io.Serializable{
	
	public static final int PROPERTY_SET = 100;
	public static final int PROPERTY_REMOVED = 105;
	
	protected int eventType;
	protected Thing targetThing;
	protected String propertyName;
	protected Object oldPropertyValue;
	protected Object newPropertyValue;
	
	public ThingEvent(int eventType, Thing targetThing, String propertyName,
		Object oldPropertyValue, Object newPropertyValue){
		this.eventType = eventType;
		this.targetThing = targetThing;
		this.propertyName = propertyName;
		this.oldPropertyValue = oldPropertyValue;
		this.newPropertyValue = newPropertyValue;
	}
	
	public int getEventType(){
		return eventType;
	}
	
	public Thing getTargetThing(){
		return targetThing;
	}
	
	public String getPropertyName(){
		return propertyName;
	}
	
	public Object getOldPropertyValue(){
		return oldPropertyValue;
	}
	
	public Object getNewPropertyValue(){
		return newPropertyValue;
	}
	
	public String toString(){
		return "ThingEvent{eventType=" + eventType + 
			"; targetThing=" + targetThing +
			"; propertyName=" + propertyName +
			"; oldPropertyValue=" + oldPropertyValue +
			"; newPropertyValue=" + newPropertyValue +
			"};";
	}

}

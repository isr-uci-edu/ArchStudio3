package edu.uci.ics.xarchutils;

/**
 * An event object that is broadcast to <code>XArchFlatListener</code>s
 * when an xArch element changes.
 * 
 * @author Eric M. Dashofy [edashofy@ics.uci.edu]
 */
public class XArchFlatEvent implements java.io.Serializable{

	/** Event type used when an element is set */
	public static final int SET_EVENT = 100;
	/** Event type used when an element or set of elements is cleared. */
	public static final int CLEAR_EVENT = 200;
	/** Event type used when an element is added. */
	public static final int ADD_EVENT = 300;
	/** Event type used when an element is removed. */
	public static final int REMOVE_EVENT = 400;
	/** Event type used when an element is promoted */
	public static final int PROMOTE_EVENT = 500;
	
	/** Source type used when an attribute changes. */
	public static final int ATTRIBUTE_CHANGED = 1100;
	/** Source type used when an element changes. */
	public static final int ELEMENT_CHANGED = 1200;
	/** Source type used when a simple type's value changes. */
	public static final int SIMPLE_TYPE_VALUE_CHANGED = 1300;
	
	protected ObjRef src;
	protected XArchPath srcPath;
	protected int eventType;
	protected int srcType;
	protected String targetName;
	
	/**
	 * This is a string with the value if srcType = ATTRIBUTE_CHANGED,
	 * a string with the value srcType = SIMPLE_TYPE_VALUE_CHANGED,
	 * otherwise it's an ObjRef if srcType = ELEMENT_CHANGED.
	 */
	protected Object target;
	
	/**
	 * This is the <CODE>XArchPath</CODE> to the target if the target
	 * is an ObjRef, or <CODE>null</CODE> if it's not.
	 */
	protected XArchPath targetPath;
	
	/**
	 * <code>true</code> if the source of the event is attached to the
	 * xArch element in the context, <code>false</code> otherwise.
	 */
	protected boolean isAttached;
	
	/**
	 * Create a new xArch event.
	 *
	 * @param src xArch element that is the source of this event (i.e. that
	 * changed.)
	 * @param eventType One of the event types (above) that indicates what
	 * happened.
	 * @param srcType One of the event types (above) that indicates what
	 * changed.
	 * @param targetName Name of the element or attribute that was 
	 * set/added/cleared/removed.
	 * @param target The attribute/element/value that was 
	 * set/added/cleared/removed.
	 * @param isAttached <code>true</code> if the element that was changed is
	 * actually connected to the xArch element emitting this event or not.
	 */
	public XArchFlatEvent(ObjRef src, XArchPath srcPath, 
	int eventType, int srcType, String targetName,
	Object target, XArchPath targetPath, boolean isAttached){
		this.src = src;
		this.srcPath = srcPath;
		this.eventType = eventType;
		this.srcType = srcType;
		this.targetName = targetName;
		this.target = target;
		this.targetPath = targetPath;
		this.isAttached = isAttached;
	}

	/**
	 * Set the source of this event.
	 * @param src Source of this event.
	 */
	public void setSource(ObjRef src){
		this.src = src;
	}
	
	/**
	 * Get the source xArch element of this event.
	 * @return source of this event.
	 */
	public ObjRef getSource(){
		return src;
	}
	
	/**
	 * Set the <CODE>XArchPath</CODE> to the source element
	 * of this event.
	 * @param sourcePath <CODE>XArchPath</CODE> to source.
	 */
	public void setSourcePath(XArchPath srcPath){
		this.srcPath = srcPath;
	}
	
	/**
	 * Get the <CODE>XArchPath</CODE> to the source element
	 * of this event.
	 * @return <CODE>XArchPath</CODE> to source.
	 */
	public XArchPath getSourcePath(){
		return srcPath;
	}
	
	/**
	 * Set the event type of this event.
	 * @param eventType Event type.
	 */
	public void setEventType(int eventType){
		this.eventType = eventType;
	}
	
	/**
	 * Get the event type of this event.
	 * @return Event type.
	 */
	public int getEventType(){
		return eventType;
	}
	
	/**
	 * Set the source type of this event.
	 * @param srcType Source type.
	 */
	public void setSourceType(int srcType){
		this.srcType = srcType;
	}
	
	/**
	 * Get the source type of this event.
	 * @return Source type.
	 */
	public int getSourceType(){
		return srcType;
	}
	
	/**
	 * Set the target name of this event.
	 * @param targetName Target name.
	 */
	public void setTargetName(String targetName){
		this.targetName = targetName;
	}
	
	/**
	 * Get the target name of this event.
	 * Returns the tag name if the target is an element,
	 * the attribute name if the target is an attribute,
	 * or an undefined value if the target is a simple type.
	 * @return Target name.
	 */
	public String getTargetName(){
		return this.targetName;
	}
	
	/**
	 * Set the target of this event.
	 * @param target Event target.
	 */
	public void setTarget(Object target){
		this.target = target;
	}
	
	/**
	 * Get the target of this event.
	 * @return Event target.
	 */
	public Object getTarget(){
		return target;
	}
	
	/**
	 * Set the <CODE>XArchPath</CODE> to the target element
	 * of this event.
	 * @param targetPath <CODE>XArchPath</CODE> to target.
	 */
	public void setTargetPath(XArchPath targetPath){
		this.targetPath = targetPath;
	}
	
	/**
	 * Get the <CODE>XArchPath</CODE> to the target element
	 * of this event, or <CODE>null</CODE> if the target
	 * is not an ObjRef.
	 * @return <CODE>XArchPath</CODE> to target, or <CODE>null</CODE>
	 * if the target is not an ObjRef.
	 */
	public XArchPath getTargetPath(){
		return targetPath;
	}
	
	/**
	 * Set whether the changed xArch element is currently
	 * attached to (has as an ancestor) the xArch element
	 * that emitted this event.
	 * @param isAttached Whether the changed element is attached or not.
	 */
	public void setIsAttached(boolean isAttached){
		this.isAttached = isAttached;
	}
	
	/**
	 * Determine whether the changed xArch element is currently
	 * attached to (has as an ancestor) the xArch element
	 * that emits this event.
	 * @return <code>true</code> if it is attached, <code>false</code>
	 * otherwise.
	 */
	public boolean getIsAttached(){
		return isAttached;
	}
	
	public String toString(){
		return "XArchFlatEvent{" +
			"src=" + src + ", " +
			"srcPath=" + srcPath + ", " +
			"eventType=" + eventType + ", " +
			"srcType=" + srcType + ", " + 
			"targetName=" + targetName + ", " +
			"target=" + target + ", " +
			"targetPath=" + targetPath + ", " +
			"isAttached=" + isAttached + "};";
	}
}

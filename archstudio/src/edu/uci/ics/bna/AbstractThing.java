package edu.uci.ics.bna;

import java.lang.reflect.*;
import java.util.*;

/**
 * Describes a C2 Thing that has one name (a String) and zero or more properties (String to Object
 * mappings).
 */
public class AbstractThing implements Thing{
	
	/** ID of the Thing (Unique, not its label!) */
	protected String id;
	
	/** Thing Properties */
	protected Map props;
	
	public Object propertyLockObject;
	
	public AbstractThing(String id){
		this.id = id;
		this.props = Collections.synchronizedMap(new HashMap());
		this.propertyLockObject = props;
	}
	
	public Object getPropertyLockObject(){
		return propertyLockObject;
	}
	
	public void adding(){}
	
	public void removing(){}
	
	protected AbstractThing(Thing copyMe){
		this.id = copyMe.getID();
		this.props = Collections.synchronizedMap(new HashMap(copyMe.getPropertyMap()));
		this.propertyLockObject = props;
		//this.props = (HashMap)(copyMe.props).clone();
		/*
		this.props = new Hashtable();
		
		for(Iterator it = copyMe.props.keySet().iterator(); it.hasNext(); ){
			Object key = it.next();
			Object value = copyMe.props.get(key);
			Object keyClone = cloneObject(key);
			Object valueClone = cloneObject(value);
			this.props.put(keyClone, valueClone);
		}
		*/
	}
	
	final public String getPeerClassname(){
		return getPeerClass().getName();
	}
	
	public Class getPeerClass(){
		throw new RuntimeException("Method not implemented.");
	}
	
	private static final Class[] emptyClassArray = new Class[]{};
	private static final Object[] emptyObjectArray = new Object[]{};
	
	protected static Object cloneObject(Object o){
		if(o == null) return null;
		Method cloneMethod = null;
		try{
			cloneMethod = o.getClass().getMethod("clone", emptyClassArray);
		}
		catch(NoSuchMethodException e){
			return o;
		}
		
		if(cloneMethod != null){
			try{
				Object clonedObject = cloneMethod.invoke(o, emptyObjectArray);
				return clonedObject;
			}
			catch(Exception e){
				return o;
			}
		}
		else{
			return o;
		}
	}
	
	
	public Object clone() throws CloneNotSupportedException{
		Thing m = new AbstractThing(this);
		return m;
	}
	
	//!!!
	public Thing duplicate(){
		try{
			return (Thing)clone();
		}
		catch(CloneNotSupportedException cnse){
			//won't happen;
			return null;
		}
		
		//NamedPropertyThing m2 = new NamedPropertyThing(name);
		//m2.props = (Hashtable)props.clone();
		//return m2;
	}
	
	private int getHashCode(Object o){
		if(o == null){
			return 0;
		}
		Class c = o.getClass();
		
		if(c.isArray()){
			return Array.getLength(o);
		}
		else{
			return o.hashCode();
		}
	}	
	
	private boolean realEquals(Object o1, Object o2){
		if((o1 == null) && (o2 == null)){
			return true;
		}
		if((o1 == null) || (o2 == null)){
			return false;
		}
		
		Class o1Class = o1.getClass();
		Class o2Class = o2.getClass();
		
		if((o1Class.isArray()) && (o2Class.isArray())){
			Class o1ct = o1Class.getComponentType();
			Class o2ct = o2Class.getComponentType();
			
			if(o1ct.isPrimitive() && o2ct.isPrimitive()){
				if(!o1ct.equals(o2ct)){
					return false;
				}
				if(o1ct.equals(boolean.class)){
					return Arrays.equals((boolean[])o1, (boolean[])o2);
				}
				if(o1ct.equals(byte.class)){
					return Arrays.equals((byte[])o1, (byte[])o2);
				}
				if(o1ct.equals(short.class)){
					return Arrays.equals((short[])o1, (short[])o2);
				}					
				if(o1ct.equals(int.class)){
					return Arrays.equals((int[])o1, (int[])o2);
				}					
				if(o1ct.equals(char.class)){
					return Arrays.equals((char[])o1, (char[])o2);
				}					
				if(o1ct.equals(long.class)){
					return Arrays.equals((long[])o1, (long[])o2);
				}					
				if(o1ct.equals(float.class)){
					return Arrays.equals((float[])o1, (float[])o2);
				}					
				if(o1ct.equals(double.class)){
					return Arrays.equals((double[])o1, (double[])o2);
				}					
				throw new RuntimeException("Bad primitive type mojo!");
			}
			if(o1ct.isPrimitive() || o2ct.isPrimitive()){
				return false;
			}
			//They're both not primitive
			//Check to see if it's an array-of-arrays
			if(o1ct.isArray() && o2ct.isArray()){
				Object[] arr1 = (Object[])o1;
				Object[] arr2 = (Object[])o2;
				if(arr1.length != arr2.length){
					return false;
				}
				for(int i = 0; i < arr1.length; i++){
					if(!realEquals(arr1[i], arr2[i])){
						return false;
					}
				}
				//The contents are equal
				return true;
			}
			else if(o1ct.isArray() || (o2ct.isArray())){
				//Only one component type is an array
				return false;
			}
			else{
				//Neither component type is an array
				return Arrays.equals((Object[])o1, (Object[])o2);
			}
		}
		else if(o1Class.isArray() || o2Class.isArray()){
			//Only one of the two objects is an array
			return false;
		}
		else{
			//Neither one is an array.
			return o1.equals(o2);
		}
	}			
	
	public boolean equals(Object o){
		if(o == this) return true;
		
		if(!(o instanceof Thing)){
			return false;
		}
		Thing om = (Thing)o;
		if((id == null) && (om.getID() != null)){
			return false;
		}
		if((id != null) && (om.getID() == null)){
			return false;
		}
		if((id != null) && (om.getID() != null) && (!id.equals(om.getID()))){
			return false;
		}
		
		Map ht1 = props;
		Map ht2 = om.getPropertyMap();
		
		synchronized(props){
			if(ht1.size() != ht2.size()){
				return false;
			}
			for(Iterator it = ht1.keySet().iterator(); it.hasNext(); ){
				Object key = it.next();
				Object ht1Value = ht1.get(key);
				Object ht2Value = ht2.get(key);
				if(ht2Value == null){
					return false;
				}
				
				if(!realEquals(ht1Value, ht2Value)){
					return false;
				}
			}
			return true;
		}
	}
	
	public int hashCode(){
		int hc = getHashCode(id);
		return hc;
		/*
		for(Iterator it = props.keySet().iterator(); it.hasNext(); ){
			Object key = it.next();
			Object propValue = props.get(key);
			hc ^= getHashCode(key);
			hc ^= getHashCode(propValue);
		}
		return hc;
		*/
	}
	
	public void setID(String id){
		this.id = id;
	}
	
	public String getID(){
		return id;
	}
	
	protected java.util.Vector thingListeners = new java.util.Vector();
	
	protected void fireThingEvent(int eventType, String propertyName, Object oldValue, Object newValue){
		if(propertyName.startsWith("#")){
			return;
		}
		if((oldValue == null) && (newValue == null)){
			return;
		}
		if((oldValue != null) && (newValue != null)){
			if(oldValue.equals(newValue)){
				return;
			}
		}
		ThingEvent te = new ThingEvent(eventType, this, propertyName, oldValue, newValue);
		Vector v = null;
		synchronized(thingListeners){
			v = new Vector(thingListeners);
		}
		for(int i = 0; i < v.size(); i++){
			((ThingListener)v.elementAt(i)).thingChanged(te);
		}
	}
	
	public void addThingListener(ThingListener tl){
		synchronized(thingListeners){
			thingListeners.addElement(tl);
		}
	}
	
	public void removeThingListener(ThingListener tl){
		synchronized(thingListeners){
			thingListeners.removeElement(tl);
		}
	}		
	
	/**
	 * Adds a String-Byte mapping to the property list of this thing.
	 * @param name Name of the property.
	 * @param value Value of the property.
	 */
	public void setProperty(String name, byte value){
		Object oldPropValue = getProperty(name);
		props.put(name, new Byte(value));
		fireThingEvent(ThingEvent.PROPERTY_SET, name, oldPropValue, new Byte(value));
	}
	
	/**
	 * Adds a String-Short mapping to the property list of this thing.
	 * @param name Name of the property.
	 * @param value Value of the property.
	 */
	public void setProperty(String name, short value){
		Object oldPropValue = getProperty(name);
		props.put(name, new Short(value));
		fireThingEvent(ThingEvent.PROPERTY_SET, name, oldPropValue, new Short(value));
	}
	
	/**
	 * Adds a String-Character mapping to the property list of this thing.
	 * @param name Name of the property.
	 * @param value Value of the property.
	 */
	public void setProperty(String name, char value){
		Object oldPropValue = getProperty(name);
		props.put(name, new Character(value));
		fireThingEvent(ThingEvent.PROPERTY_SET, name, oldPropValue, new Character(value));
	}
	
	/**
	 * Adds a String-Integer mapping to the property list of this thing.
	 * @param name Name of the property.
	 * @param value Value of the property.
	 */
	public void setProperty(String name, int value){
		Object oldPropValue = getProperty(name);
		props.put(name, new Integer(value));
		fireThingEvent(ThingEvent.PROPERTY_SET, name, oldPropValue, new Integer(value));
	}
	
	/**
	 * Adds a String-Long mapping to the property list of this thing.
	 * @param name Name of the property.
	 * @param value Value of the property.
	 */
	public void setProperty(String name, long value){
		Object oldPropValue = getProperty(name);
		props.put(name, new Long(value));
		fireThingEvent(ThingEvent.PROPERTY_SET, name, oldPropValue, new Long(value));
	}
	
	/**
	 * Adds a String-Float mapping to the property list of this thing.
	 * @param name Name of the property.
	 * @param value Value of the property.
	 */
	public void setProperty(String name, float value){
		Object oldPropValue = getProperty(name);
		props.put(name, new Float(value));
		fireThingEvent(ThingEvent.PROPERTY_SET, name, oldPropValue, new Float(value));
	}
	
	/**
	 * Adds a String-Double mapping to the property list of this thing.
	 * @param name Name of the property.
	 * @param value Value of the property.
	 */
	public void setProperty(String name, double value){
		Object oldPropValue = getProperty(name);
		props.put(name, new Double(value));
		fireThingEvent(ThingEvent.PROPERTY_SET, name, oldPropValue, new Double(value));
	}
	
	/**
	 * Adds a String-Boolean mapping to the property list of this thing.
	 * @param name Name of the property.
	 * @param value Value of the property.
	 */
	public void setProperty(String name, boolean value){
		Object oldPropValue = getProperty(name);
		props.put(name, Boolean.valueOf(value));
		fireThingEvent(ThingEvent.PROPERTY_SET, name, oldPropValue, new Boolean(value));
	}
	
	/**
	 * Adds a String-Object mapping to the property list of this thing.
	 * @param name Name of the property.
	 * @param value Value of the property.
	 */
	public void setProperty(String name, Object value){
		Object oldPropValue = getProperty(name);
		if(value == null){
			props.put(name, new NullPropertyValue());
		}
		else{
			props.put(name, value);
		}
		fireThingEvent(ThingEvent.PROPERTY_SET, name, oldPropValue, value);
	}
	
	/**
	 * Gets a property from this thing.
	 * @param name Name of the property.
	 * @return Value of the property, or <code>null</code> if there is no such property.
	 */
	public Object getProperty(String name){
		Object o = props.get(name);
		if(o instanceof NullPropertyValue){
			return null;
		}
		return o; //props.get(name);
	}
	
	public boolean hasProperty(String name){
		Object o = props.get(name);
		return (o != null);
	}
	
	/**
	 * Gets a property from this thing as a byte value.
	 * @param name Name of the property.
	 * @return Value of the property as a byte.
	 * @exception IllegalArgumentException if the property does not exist or is
	 * not of the correct type.
	 */
	public byte getByteProperty(String name){
		try{
			return ((Byte)getProperty(name)).byteValue();
		}
		catch(NullPointerException npe){
			throw new IllegalArgumentException("No such property.");
		}
		catch(ClassCastException cce){
			throw new IllegalArgumentException("Property name given did not represent a byte.");
		}
	}
	
	/**
	 * Gets a property from this thing as a short value.
	 * @param name Name of the property.
	 * @return Value of the property as a short.
	 * @exception IllegalArgumentException if the property does not exist or is
	 * not of the correct type.
	 */
	public short getShortProperty(String name){
		try{
			return ((Short)getProperty(name)).shortValue();
		}
		catch(NullPointerException npe){
			throw new IllegalArgumentException("No such property.");
		}
		catch(ClassCastException cce){
			throw new IllegalArgumentException("Property name given did not represent a short.");
		}
	}
	
	/**
	 * Gets a property from this thing as a char value.
	 * @param name Name of the property.
	 * @return Value of the property as a char.
	 * @exception IllegalArgumentException if the property does not exist or is
	 * not of the correct type.
	 */
	public char getCharProperty(String name){
		try{
			return ((Character)getProperty(name)).charValue();
		}
		catch(NullPointerException npe){
			throw new IllegalArgumentException("No such property.");
		}
		catch(ClassCastException cce){
			throw new IllegalArgumentException("Property name given did not represent a char.");
		}
	}
	
	/**
	 * Gets a property from this thing as an integer value.
	 * @param name Name of the property.
	 * @return Value of the property as an int.
	 * @exception IllegalArgumentException if the property does not exist or is
	 * not of the correct type.
	 */
	public int getIntProperty(String name){
		try{
			return ((Integer)getProperty(name)).intValue();
		}
		catch(NullPointerException npe){
			throw new IllegalArgumentException("No such property.");
		}
		catch(ClassCastException cce){
			throw new IllegalArgumentException("Property name given did not represent an integer.");
		}
	}
	
	/**
	 * Gets a property from this thing as a long value.
	 * @param name Name of the property.
	 * @return Value of the property as a long.
	 * @exception IllegalArgumentException if the property does not exist or is
	 * not of the correct type.
	 */
	public long getLongProperty(String name){
		try{
			return ((Long)getProperty(name)).longValue();
		}
		catch(NullPointerException npe){
			throw new IllegalArgumentException("No such property.");
		}
		catch(ClassCastException cce){
			throw new IllegalArgumentException("Property name given did not represent a long.");
		}
	}
	
	/**
	 * Gets a property from this thing as a short value.
	 * @param name Name of the property.
	 * @return Value of the property as a double.
	 * @exception IllegalArgumentException if the property does not exist or is
	 * not of the correct type.
	 */
	public double getDoubleProperty(String name){
		try{
			return ((Double)getProperty(name)).doubleValue();
		}
		catch(NullPointerException npe){
			throw new IllegalArgumentException("No such property.");
		}
		catch(ClassCastException cce){
			throw new IllegalArgumentException("Property name given did not represent a double.");
		}
	}
	
	/**
	 * Gets a property from this thing as a float value.
	 * @param name Name of the property.
	 * @return Value of the property as a float.
	 * @exception IllegalArgumentException if the property does not exist or is
	 * not of the correct type.
	 */
	public float getFloatProperty(String name){
		try{
			return ((Float)getProperty(name)).floatValue();
		}
		catch(NullPointerException npe){
			throw new IllegalArgumentException("No such property.");
		}
		catch(ClassCastException cce){
			throw new IllegalArgumentException("Property name given did not represent a float.");
		}
	}
	
	/**
	 * Gets a property from this thing as a boolean value.
	 * @param name Name of the property.
	 * @return Value of the property as a boolean.
	 * @exception IllegalArgumentException if the property does not exist or is
	 * not of the correct type.
	 */
	public boolean getBooleanProperty(String name){
		try{
			return ((Boolean)getProperty(name)).booleanValue();
		}
		catch(NullPointerException npe){
			throw new IllegalArgumentException("No such property.");
		}
		catch(ClassCastException cce){
			throw new IllegalArgumentException("Property name given did not represent a boolean.");
		}
	}
	
	public void addSetPropertyValue(String name, Object value){
		synchronized(props){
			Set s = (Set)getProperty(name);
			if(s == null){
				s = Collections.synchronizedSet(new HashSet());
			}
			else
				s = new HashSet(s);
			s.add(value);
			setProperty(name, s);
		}
	}
	
	public void replaceSetPropertyValues(String name, Set s){
		HashSet ns = new HashSet(s);
		setProperty(name, ns);
	}
	
	public void removeSetPropertyValue(String name, Object value){
		synchronized(props){
			Set s = (Set)getProperty(name);
			if(s == null){
				return;
			}
			s = new HashSet(s);
			s.remove(value);
			setProperty(name, s);
		}
	}
	
	public Set getSetProperty(String name){
		synchronized(props){
			Set s = (Set)getProperty(name);
			if(s == null){
				return Collections.EMPTY_SET;
			}
			else{
				return Collections.unmodifiableSet(s);
			}
		}
	}
	
	/**
	 * Removes a property from this thing.  Does nothing if the property does
	 * not exist.
	 * @param name Name of the property to remove.
	 */
	public void removeProperty(String name){
		Object oldPropValue = getProperty(name);
		props.remove(name);
		fireThingEvent(ThingEvent.PROPERTY_REMOVED, name, oldPropValue, null);
	}
	
	/**
	 * Returns a String representation of this thing. 
	 * @return String representation of this thing.
	 */
	public String toString(){
		return "Thing={" + "id=" + id + "," + props.toString() + "}";
	}
	
	/**
	 * Gets a Map representation of the name/value pair properties in this
	 * thing.
	 * @return Map representation of name/value pair properties.
	 */
	public Map getPropertyMap(){
		synchronized(props){
			return new HashMap(props);
		}
		//return (Map)props.clone();
	}
	
	/**
	 * Gets all the property names in this thing as an array
	 * of strings.
	 * @return All property names in an array.
	 */
	public String[] getAllPropertyNames(){
		Set s = props.keySet();
		return (String[])s.toArray(new String[0]);
	}
	
	static class NullPropertyValue implements java.io.Serializable{
		public boolean equals(Object o){
			if(o instanceof NullPropertyValue){
				return true;
			}
			return false;
		}
		
		public int hashCode(){
			return 0;
		}
	};
	
}


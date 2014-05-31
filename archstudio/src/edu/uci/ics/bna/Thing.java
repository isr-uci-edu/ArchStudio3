package edu.uci.ics.bna;

import java.lang.reflect.*;
import java.util.*;

/**
 * Describes a BNA Thing that has one name (a String) and zero or more properties (String to Object
 * mappings).
 */
public interface Thing extends java.io.Serializable, Cloneable{

	public void adding();	
	public void removing();
	
	/**
	 * Gets the class that maintains and draws the
	 * graphical representation of this Thing.
	 * 
	 * @return The Thing's Peer's class.
	 */
	public Class getPeerClass();
	
	public Object clone() throws CloneNotSupportedException;
	
	public Thing duplicate();

	/**
	 * Every Thing has a unique ID that identifies it; this
	 * should preferably be globally unique.
	 * 
	 * @param id New ID for this Thing.
	 */
	public void setID(String id);
	
	/**
	 * Gets the unique ID of this Thing.
	 * 
	 * @return Thing ID.
	 */
	public String getID();

	/**
	 * Gets the property lock for this Thing.  You can synchronize on
	 * this lock if you want to make multiple changes to the Thing's
	 * properties without interference from concurrent threads.
	 * 
	 * @return Property lock object for this Thing.
	 */
	public Object getPropertyLockObject();
	
	/**
	 * Adds a listener to this Thing that will be notified when the
	 * Thing changes.
	 * 
	 * @param tl Thing listener to add.
	 */
	public void addThingListener(ThingListener tl);

	/**
	 * Removes a listener to this Thing that will no longer be notified when the
	 * Thing changes.
	 * 
	 * @param tl Thing listener to remove.
	 */
	public void removeThingListener(ThingListener tl);

	/**
	 * Adds a String-Byte mapping to the property list of this thing.
	 * @param name Name of the property.
	 * @param value Value of the property.
	 */
  public void setProperty(String name, byte value);

	/**
	 * Adds a String-Short mapping to the property list of this thing.
	 * @param name Name of the property.
	 * @param value Value of the property.
	 */
  public void setProperty(String name, short value);

	/**
	 * Adds a String-Character mapping to the property list of this thing.
	 * @param name Name of the property.
	 * @param value Value of the property.
	 */
  public void setProperty(String name, char value);
	
	/**
	 * Adds a String-Integer mapping to the property list of this thing.
	 * @param name Name of the property.
	 * @param value Value of the property.
	 */
  public void setProperty(String name, int value);
	
	/**
	 * Adds a String-Long mapping to the property list of this thing.
	 * @param name Name of the property.
	 * @param value Value of the property.
	 */
  public void setProperty(String name, long value);
	
	/**
	 * Adds a String-Float mapping to the property list of this thing.
	 * @param name Name of the property.
	 * @param value Value of the property.
	 */
  public void setProperty(String name, float value);
	
	/**
	 * Adds a String-Double mapping to the property list of this thing.
	 * @param name Name of the property.
	 * @param value Value of the property.
	 */
  public void setProperty(String name, double value);
	
	/**
	 * Adds a String-Boolean mapping to the property list of this thing.
	 * @param name Name of the property.
	 * @param value Value of the property.
	 */
  public void setProperty(String name, boolean value);
	
	/**
	 * Adds a String-Object mapping to the property list of this thing.
	 * @param name Name of the property.
	 * @param value Value of the property.
	 */
  public void setProperty(String name, Object value);
 
	/**
	 * Gets a property from this thing.
	 * @param name Name of the property.
	 * @return Value of the property, or <code>null</code> if there is no such property.
	 */
	public Object getProperty(String name);
  
	public boolean hasProperty(String name);
	
	/**
	 * Gets a property from this thing as a byte value.
	 * @param name Name of the property.
	 * @return Value of the property as a byte.
	 * @exception IllegalArgumentException if the property does not exist or is
	 * not of the correct type.
	 */
	public byte getByteProperty(String name);
			
	/**
	 * Gets a property from this thing as a short value.
	 * @param name Name of the property.
	 * @return Value of the property as a short.
	 * @exception IllegalArgumentException if the property does not exist or is
	 * not of the correct type.
	 */
	public short getShortProperty(String name);
	
	/**
	 * Gets a property from this thing as a char value.
	 * @param name Name of the property.
	 * @return Value of the property as a char.
	 * @exception IllegalArgumentException if the property does not exist or is
	 * not of the correct type.
	 */
	public char getCharProperty(String name);
	
	/**
	 * Gets a property from this thing as an integer value.
	 * @param name Name of the property.
	 * @return Value of the property as an int.
	 * @exception IllegalArgumentException if the property does not exist or is
	 * not of the correct type.
	 */
	public int getIntProperty(String name);
	
	/**
	 * Gets a property from this thing as a long value.
	 * @param name Name of the property.
	 * @return Value of the property as a long.
	 * @exception IllegalArgumentException if the property does not exist or is
	 * not of the correct type.
	 */
	public long getLongProperty(String name);
	
	/**
	 * Gets a property from this thing as a short value.
	 * @param name Name of the property.
	 * @return Value of the property as a double.
	 * @exception IllegalArgumentException if the property does not exist or is
	 * not of the correct type.
	 */
	public double getDoubleProperty(String name);
	
	/**
	 * Gets a property from this thing as a float value.
	 * @param name Name of the property.
	 * @return Value of the property as a float.
	 * @exception IllegalArgumentException if the property does not exist or is
	 * not of the correct type.
	 */
	public float getFloatProperty(String name);
	
	/**
	 * Gets a property from this thing as a boolean value.
	 * @param name Name of the property.
	 * @return Value of the property as a boolean.
	 * @exception IllegalArgumentException if the property does not exist or is
	 * not of the correct type.
	 */
	public boolean getBooleanProperty(String name);

	public void addSetPropertyValue(String name, Object value);
	
	public void replaceSetPropertyValues(String name, Set s);
	
	public void removeSetPropertyValue(String name, Object value);

	public Set getSetProperty(String name);
	
	/**
	 * Removes a property from this thing.  Does nothing if the property does
	 * not exist.
	 * @param name Name of the property to remove.
	 */
	public void removeProperty(String name);
  
	
	/**
	 * Gets a Map representation of the name/value pair properties in this
	 * thing.
	 * @return Map representation of name/value pair properties.
	 */
	public Map getPropertyMap();
	
	/**
	 * Gets all the property names in this thing as an array
	 * of strings.
	 * @return All property names in an array.
	 */
	public String[] getAllPropertyNames();
}

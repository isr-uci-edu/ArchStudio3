package edu.uci.isr.registry;

import java.util.*;

public class RegistryEntry{
	
	protected String key;
	protected Set values;
	
	public RegistryEntry(String key){
		this.key = key;
		values =  Collections.synchronizedSet(new HashSet());
	}
	
	public String getKey(){
		return key;
	}
	
	public void putValue(String value){
		values.add(value);
	}

	public void putValues(Collection c){
		synchronized(c){
			for(Iterator it = c.iterator(); it.hasNext(); ){
				putValue((String)it.next());
			}
		}
	}
	
	public void putValues(String[] arr){
		for(int i = 0; i < arr.length; i++){
			putValue(arr[i]);
		}
	}
	
	public boolean hasValue(String value){
		return values.contains(value);
	}
	
	public Iterator valueIterator(){
		return values.iterator();
	}
	
	//Returns the first value if there is one, or null if there
	//are none.
	public String getValue(){
		if(values.size() == 0){
			return null;
		}
		else{
			Iterator it = valueIterator();
			return (String)it.next();
		}
	}
	
	public String[] getAllValues(){
		synchronized(values){
			String[] str = new String[values.size()];
			int i = 0;
			for(Iterator it = valueIterator(); it.hasNext(); ){
				str[i++] = (String)it.next();
			}
			return str;
		}
	}
	
	public void removeValue(String value){
		values.remove(value);
	}
	
	public void removeAllValues(){
		values.clear();
	}

	public boolean equals(Object o){
		if(!(o instanceof RegistryEntry)){
			return false;
		}
		RegistryEntry re = (RegistryEntry)o;
		synchronized(values){
			return (key.equals(re.key) && values.equals(re.values));
		}
	}

	public int hashCode(){
		return key.hashCode() ^ values.hashCode();
	}
}


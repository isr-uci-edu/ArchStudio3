package archstudio.comp.preferences;

import java.util.prefs.*;

import c2.fw.*;

//Support for "legacy" C2 components
import c2.legacy.*;

//Includes classes that allow our component to
//make Event-based Procedure Calls (EPCs)
import c2.pcwrap.*;

public class PreferencesC2Component extends AbstractC2DelegateBrick implements IPreferences{

	public PreferencesC2Component(Identifier id){
		super(id);
		
		EBIWrapperUtils.deployService( this, bottomIface, bottomIface, 
			this, new Class[]{ IPreferences.class }, 
			new Class[0] );
	}

	protected void sync(Preferences p){
		try{
			p.sync();
		}
		catch(BackingStoreException bse){
			System.err.println("Warning: backing store exception while storing preferences.");
			System.err.println("This is not fatal.");
			bse.printStackTrace();
		}
	}
	
	protected void flush(Preferences p){
		try{
			p.flush();
		}
		catch(BackingStoreException bse){
			System.err.println("Warning: backing store exception while storing preferences.");
			System.err.println("This is not fatal.");
			bse.printStackTrace();
		}
	}

	protected Preferences getRoot(int space){
		if(space == IPreferences.SYSTEM_SPACE){
			return Preferences.systemRoot();		
		}
		else if(space == IPreferences.USER_SPACE){
			return Preferences.userRoot();
		}
		else{
			throw new IllegalArgumentException("Illegal space.");
		}
	}
	
	public void setValue(int space, String nodePath, String key, boolean value){
		Preferences p = getRoot(space);
		Preferences node = p.node(nodePath);
		node.putBoolean(key, value);
		sync(p);
	}
	
	public void setValue(int space, String nodePath, String key, int value){
		Preferences p = getRoot(space);
		Preferences node = p.node(nodePath);
		node.putInt(key, value);	
		sync(p);
	}
	
	public void setValue(int space, String nodePath, String key, long value){
		Preferences p = getRoot(space);
		Preferences node = p.node(nodePath);
		node.putLong(key, value);
		sync(p);
	}
	
	public void setValue(int space, String nodePath, String key, float value){
		Preferences p = getRoot(space);
		Preferences node = p.node(nodePath);
		node.putFloat(key, value);
	}
	
	public void setValue(int space, String nodePath, String key, double value){
		Preferences p = getRoot(space);
		Preferences node = p.node(nodePath);
		node.putDouble(key, value);
		flush(p);
	}
	
	public void setValue(int space, String nodePath, String key, byte[] value){
		Preferences p = getRoot(space);
		Preferences node = p.node(nodePath);
		node.putByteArray(key, value);
		flush(p);
	}
	
	public void setValue(int space, String nodePath, String key, String value){
		Preferences p = getRoot(space);
		Preferences node = p.node(nodePath);
		node.put(key, value);
		flush(p);
	}

	public boolean getBooleanValue(int space, String nodePath, String key, boolean def){
		if(!nodeExists(space, nodePath)){
			throw new IllegalArgumentException("No such node.");
		}
		Preferences p = getRoot(space);
		sync(p);
		Preferences node = p.node(nodePath);
		return node.getBoolean(key, def);
	}
	
	public int getIntValue(int space, String nodePath, String key, int def){
		if(!nodeExists(space, nodePath)){
			throw new IllegalArgumentException("No such node.");
		}
		Preferences p = getRoot(space);
		sync(p);
		Preferences node = p.node(nodePath);
		return node.getInt(key, def);		
	}
	
	public long getLongValue(int space, String nodePath, String key, long def){
		if(!nodeExists(space, nodePath)){
			throw new IllegalArgumentException("No such node.");
		}
		Preferences p = getRoot(space);
		sync(p);
		Preferences node = p.node(nodePath);
		return node.getLong(key, def);
	}
	
	public float getFloatValue(int space, String nodePath, String key, float def){
		if(!nodeExists(space, nodePath)){
			throw new IllegalArgumentException("No such node.");
		}
		Preferences p = getRoot(space);
		sync(p);
		Preferences node = p.node(nodePath);
		return node.getFloat(key, def);
	}

	public double getDoubleValue(int space, String nodePath, String key, double def){
		if(!nodeExists(space, nodePath)){
			throw new IllegalArgumentException("No such node.");
		}
		Preferences p = getRoot(space);
		sync(p);
		Preferences node = p.node(nodePath);
		return node.getDouble(key, def);
	}
	
	public byte[] getByteArrayValue(int space, String nodePath, String key, byte[] def){
		if(!nodeExists(space, nodePath)){
			throw new IllegalArgumentException("No such node.");
		}
		Preferences p = getRoot(space);
		sync(p);
		Preferences node = p.node(nodePath);
		return node.getByteArray(key, def);
	}
	
	public String getStringValue(int space, String nodePath, String key, String def){
		if(!nodeExists(space, nodePath)){
			throw new IllegalArgumentException("No such node.");
		}
		Preferences p = getRoot(space);
		sync(p);
		Preferences node = p.node(nodePath);
		return node.get(key, def);
	}
	
		
	public boolean nodeExists(int space, String nodePath){
		try{
			Preferences p = getRoot(space);
			sync(p);
			boolean exists = p.nodeExists(nodePath);
			return exists;
		}
		catch(BackingStoreException bse){
			return false;
		}
	}
	
	public boolean keyExists(int space, String nodePath, String key){
		if(!nodeExists(space, nodePath)){
			return false;
		}
		Preferences p = getRoot(space);
		Preferences node = p.node(nodePath);
		try{
			String[] keys = node.keys();
			for(int i = 0; i < keys.length; i++){
				if(keys[i].equals(key)){
					return true;
				}
			}
			return false;
		}
		catch(BackingStoreException bse){
			return false;
		}
	}
	
	public void removeNode(int space, String nodePath){
		if(!nodeExists(space, nodePath)){
			return;
		}
		Preferences p = getRoot(space);
		Preferences node = p.node(nodePath);
		try{
			node.removeNode();
			sync(p);
		}
		catch(BackingStoreException bse){
		}
	}
	
	public void removeKey(int space, String nodePath, String key){
		if(!nodeExists(space, nodePath)){
			return;
		}
		Preferences p = getRoot(space);
		sync(p);
		Preferences node = p.node(nodePath);
		node.remove(key);
		sync(p);
	}

}

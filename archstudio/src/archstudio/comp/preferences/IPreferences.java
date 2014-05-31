package archstudio.comp.preferences;

public interface IPreferences {
	public static final int USER_SPACE = 100;
	public static final int SYSTEM_SPACE = 200;
	
	public void setValue(int space, String nodePath, String key, boolean value);
	public void setValue(int space, String nodePath, String key, int value);
	public void setValue(int space, String nodePath, String key, long value);
	public void setValue(int space, String nodePath, String key, float value);
	public void setValue(int space, String nodePath, String key, double value);
	public void setValue(int space, String nodePath, String key, byte[] value);
	public void setValue(int space, String nodePath, String key, String value);

	public boolean getBooleanValue(int space, String nodePath, String key, boolean def);
	public int getIntValue(int space, String nodePath, String key, int def);
	public long getLongValue(int space, String nodePath, String key, long def);
	public float getFloatValue(int space, String nodePath, String key, float def);
	public double getDoubleValue(int space, String nodePath, String key, double def);
	public byte[] getByteArrayValue(int space, String nodePath, String key, byte[] def);
	public String getStringValue(int space, String nodePath, String key, String def);
	
	public boolean nodeExists(int space, String nodePath);
	public boolean keyExists(int space, String nodePath, String key);
	
	public void removeNode(int space, String nodePath);
	public void removeKey(int space, String nodePath, String key);
	
}
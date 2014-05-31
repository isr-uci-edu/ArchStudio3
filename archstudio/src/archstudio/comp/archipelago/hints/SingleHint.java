package archstudio.comp.archipelago.hints;

public class SingleHint {
	
	protected String xArchID;
	protected String name;
	protected Class valueClass;
	protected Object value;

	public SingleHint(String xArchID, String name, Class valueClass, Object value){
		this.xArchID = xArchID;
		this.name = name;
		this.valueClass = valueClass;
		this.value = value;
	}
	
	public String getXArchID(){
		return xArchID;
	}
	
	public String getName(){
		return name;
	}
	
	public Class getValueClass(){
		return valueClass;
	}
	
	public Object getValue(){
		return value;
	}
	
	public String toString(){
		return getXArchID() + "|" + getName() + ":" + getValueClass() + "=" + getValue() + ";";
	}
}

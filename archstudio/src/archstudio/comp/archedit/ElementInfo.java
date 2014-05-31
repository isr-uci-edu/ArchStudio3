package archstudio.comp.archedit;


public class ElementInfo{
	private String className;
	private String packageName;
	private String displayName;
	
	public ElementInfo(String packageName, String className, String displayName){
		this.packageName = packageName;
		this.className = className;
		this.displayName = displayName;
	}
	
	public String getDisplayName(){
		return displayName;
	}
	
	public String getPackageName(){
		return packageName;
	}
	
	public String getClassName(){
		return className;
	}
	
	public String toString(){
		return "ElementInfo={packageName=\"" + packageName + "\", className=\"" +
			className + "\", displayName=\"" + displayName + "\"};";
	}
}
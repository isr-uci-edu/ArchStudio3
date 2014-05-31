package archstudio.comp.aem;

public class InvalidArchitectureDescriptionException extends java.lang.Exception implements java.io.Serializable{

	public static final int ERROR_OTHER_ERROR = 50;
	public static final int ERROR_NO_ARCHSTRUCTURE = 100;
	public static final int ERROR_NO_ARCHTYPES = 102;
	public static final int ERROR_NO_ELEMENTS = 104;
	public static final int ERROR_CANT_PROCESS_XLINK = 106;
	public static final int ERROR_INVALID_XLINK = 108;
	public static final int ERROR_INVALID_XLINK_TARGET = 110;
	public static final int ERROR_ELEMENT_MISSING_TYPE = 112;
	public static final int ERROR_TYPE_MISSING_IMPL = 114;
	public static final int ERROR_CANT_PROCESS_IMPL = 116;
	public static final int ERROR_MISSING_MAIN_CLASS = 118;
	public static final int ERROR_NOT_TWO_LINK_POINTS = 120;
	public static final int ERROR_CANT_LOAD_BRICK = 122;
	public static final int ERROR_INTERFACE_MISSING_ID = 124;
	public static final int ERROR_INVALID_INTERFACE_ID_FORMAT = 126;
	public static final int ERROR_BRICK_IMPL_MISSING_PRESCRIBED_INTERFACE = 128;
	
	private int errorCode;
	private String additionalDescription;
	
	public InvalidArchitectureDescriptionException(int errorCode){
		this.errorCode = errorCode;
		additionalDescription = null;
	}
	
	public InvalidArchitectureDescriptionException(int errorCode, String additionalDescription){
		super(getErrorMessage(errorCode) + ":" + additionalDescription);
		this.errorCode = errorCode;
		this.additionalDescription = additionalDescription;
	}
	
	public int getErrorCode(){
		return errorCode;
	}

	public String toString(){
		return "Invalid Architecture Description Error: " + getErrorMessage(errorCode) + ": " + additionalDescription;
	}

	public static String getErrorMessage(int errorCode){
		switch(errorCode){
		case ERROR_OTHER_ERROR:
			return "Other error";
		case ERROR_NO_ARCHSTRUCTURE:
			return "Architecture description does not contain ArchStructure element";
		case ERROR_NO_ARCHTYPES:
			return "Architecture description dos not contain ArchTypes element";
		case ERROR_NO_ELEMENTS:
			return "Architecture description dos not contain any components or connectors";
		case ERROR_CANT_PROCESS_XLINK:
			return "Cannot process XLink";
		case ERROR_INVALID_XLINK:
			return "Invalid XLink";
		case ERROR_INVALID_XLINK_TARGET:
			return "Cannot resolve XLink target";
		case ERROR_ELEMENT_MISSING_TYPE:
			return "Architecture component/connector is missing a type";
		case ERROR_TYPE_MISSING_IMPL:
			return "Architecture component/connector type is missing an implementation";
		case ERROR_CANT_PROCESS_IMPL:
			return "Don't know how to process type implementation";
		case ERROR_MISSING_MAIN_CLASS:
			return "Main class is missing";
		case ERROR_NOT_TWO_LINK_POINTS:
			return "Links must have exactly two endpoints";
		case ERROR_CANT_LOAD_BRICK:
			return "Cannot load architecture element implementation";
		case ERROR_INTERFACE_MISSING_ID:
			return "Interface missing identifier";
		case ERROR_INVALID_INTERFACE_ID_FORMAT:
			return "Interface IDs must be of the form BrickID.InterfaceID";
		case ERROR_BRICK_IMPL_MISSING_PRESCRIBED_INTERFACE:
			return "The architecture description specifies an interface for a brick, " +
				"but the brick's implementation does not contain an interface with that " + 
				"id";
		default:
			return "Unknown Error";
		}
	}
			
	
}


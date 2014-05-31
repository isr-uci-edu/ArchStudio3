package archstudio.comp.archipelago.hints;

import archstudio.comp.archipelago.Base64;

public class JavaSerializationPlugin extends AbstractValueCodingPlugin{

	public boolean canEncode(Class c){
		if(java.io.Serializable.class.isAssignableFrom(c)){
			return true;
		}
		return false;
	}

	public static String objectToString(Object o){
		if(o == null){
			throw new IllegalArgumentException("Null object");
		}
		if(!(o instanceof java.io.Serializable)){
			throw new IllegalArgumentException("Can't serialize: " + o.getClass());
		}
		return Base64.encodeObject((java.io.Serializable)o, false);
	}
	
	public static Object stringToObject(String s){
		return Base64.decodeToObject(s);
	}

	public String encode(Class c, Object value){
		return objectToString(value);
	}

	public Object decode(Class c, String value) throws HintDecodingException{
		return stringToObject(value);
	}

}

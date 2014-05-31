package archstudio.comp.archipelago.hints;

public abstract class AbstractValueCodingPlugin implements ValueCodingPlugin {

	public boolean canEncode(Class c){
		return false;
	}
	
	public abstract String encode(Class c, Object value);
	public abstract Object decode(Class c, String value) throws HintDecodingException;

}

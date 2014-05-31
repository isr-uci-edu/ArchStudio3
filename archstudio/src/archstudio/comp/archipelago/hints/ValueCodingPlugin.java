package archstudio.comp.archipelago.hints;

public interface ValueCodingPlugin {
	public abstract boolean canEncode(Class c);
	public abstract String encode(Class c, Object value);
	public abstract Object decode(Class c, String value) throws HintDecodingException;
}
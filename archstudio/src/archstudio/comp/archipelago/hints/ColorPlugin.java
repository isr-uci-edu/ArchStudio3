package archstudio.comp.archipelago.hints;

import java.util.StringTokenizer;

public class ColorPlugin extends AbstractValueCodingPlugin {

	public boolean canEncode(Class c){
		if(c.equals(java.awt.Color.class)){
			return true;
		}
		else return false;
	}

	public String encode(Class c, Object value){
		java.awt.Color v = (java.awt.Color)value;
		return "COLOR" + v.getRGB();
	}

	public Object decode(Class c, String value) throws HintDecodingException{
		try{
			if(!value.startsWith("COLOR")){
				throw new HintDecodingException("Couldn't decode " + value);
			}
			value = value.substring(5);
			StringTokenizer st = new StringTokenizer(value, ",");
			int rgb = Integer.parseInt(st.nextToken());
			java.awt.Color v = new java.awt.Color(rgb);
			return v;
		}
		catch(Exception e){
			throw new HintDecodingException("Can't decode: " + value);
		}
	}

}

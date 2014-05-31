package archstudio.comp.archipelago.hints;

import java.util.StringTokenizer;

public class PointPlugin extends AbstractValueCodingPlugin {

	public boolean canEncode(Class c){
		if(c.equals(java.awt.Point.class)){
			return true;
		}
		else return false;
	}

	public String encode(Class c, Object value){
		java.awt.Point v = (java.awt.Point)value;
		return "POINT" + v.x + "," + v.y;
	}

	public Object decode(Class c, String value) throws HintDecodingException{
		try{
			java.awt.Point v = new java.awt.Point();
			if(!value.startsWith("POINT")){
				throw new HintDecodingException("Couldn't decode " + value);
			}
			value = value.substring(5);
			StringTokenizer st = new StringTokenizer(value, ",");
			String xs = st.nextToken();
			v.x = Integer.parseInt(xs);
			String ys = st.nextToken();
			v.y = Integer.parseInt(ys);
			return v;
		}
		catch(Exception e){
			throw new HintDecodingException("Can't decode: " + value);
		}
	}

}

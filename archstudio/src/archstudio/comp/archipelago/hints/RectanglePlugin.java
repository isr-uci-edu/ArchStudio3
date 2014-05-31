package archstudio.comp.archipelago.hints;

import java.util.StringTokenizer;

public class RectanglePlugin extends AbstractValueCodingPlugin {

	public boolean canEncode(Class c){
		if(c.equals(java.awt.Rectangle.class)){
			return true;
		}
		else return false;
	}

	public String encode(Class c, Object value){
		java.awt.Rectangle r = (java.awt.Rectangle)value;
		return "RECT" + r.x + "," + r.y + "," + r.width + "," + r.height;
	}

	public Object decode(Class c, String value) throws HintDecodingException{
		try{
			java.awt.Rectangle r = new java.awt.Rectangle();
			if(!value.startsWith("RECT")){
				throw new HintDecodingException("Couldn't decode " + value);
			}
			value = value.substring(4);
			StringTokenizer st = new StringTokenizer(value, ",");
			String xs = st.nextToken();
			r.x = Integer.parseInt(xs);
			String ys = st.nextToken();
			r.y = Integer.parseInt(ys);
			String ws = st.nextToken();
			r.width = Integer.parseInt(ws);
			String hs = st.nextToken();
			r.height = Integer.parseInt(hs);
			return r;
		}
		catch(Exception e){
			throw new HintDecodingException("Can't decode: " + value);
		}
	}

}

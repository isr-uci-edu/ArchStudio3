package archstudio.comp.archipelago.hints;

import java.util.StringTokenizer;

public class TypeWrapperPlugin extends AbstractValueCodingPlugin {

	public boolean canEncode(Class c){
		if(c.equals(java.lang.Boolean.class)){
			return true;
		}
		else if(c.equals(java.lang.Byte.class)){
			return true;
		}
		else if(c.equals(java.lang.Short.class)){
			return true;
		}
		else if(c.equals(java.lang.Integer.class)){
			return true;
		}
		else if(c.equals(java.lang.Long.class)){
			return true;
		}
		else if(c.equals(java.lang.Double.class)){
			return true;
		}
		else if(c.equals(java.lang.Float.class)){
			return true;
		}
		else if(c.equals(java.lang.Byte.class)){
			return true;
		}
		else if(c.equals(java.lang.Character.class)){
			return true;
		}
		else return false;
	}

	public String encode(Class c, Object value){
		if(c.equals(java.lang.Boolean.class)){
			return "BOO" + ((java.lang.Boolean)value).toString();
		}
		if(c.equals(java.lang.Byte.class)){
			return "BYT" + ((java.lang.Byte)value).toString();
		}
		if(c.equals(java.lang.Short.class)){
			return "SHO" + ((java.lang.Short)value).toString();
		}
		if(c.equals(java.lang.Integer.class)){
			return "INT" + ((java.lang.Integer)value).toString();
		}
		if(c.equals(java.lang.Long.class)){
			return "LON" + ((java.lang.Long)value).toString();
		}
		if(c.equals(java.lang.Double.class)){
			return "DOU" + ((java.lang.Double)value).toString();
		}
		if(c.equals(java.lang.Float.class)){
			return "FLO" + ((java.lang.Float)value).toString();
		}
		if(c.equals(java.lang.Character.class)){
			return "CHA" + ((java.lang.Character)value).toString();
		}
		return null;
	}

	public Object decode(Class c, String value) throws HintDecodingException{
		try{
			
			if(c.equals(java.lang.Boolean.class)){
				if(!value.startsWith("BOO")){
					throw new HintDecodingException("Couldn't decode " + value);
				}
				value = value.substring(3);
				return new java.lang.Boolean(value);
			}
			if(c.equals(java.lang.Byte.class)){
				if(!value.startsWith("BYT")){
					throw new HintDecodingException("Couldn't decode " + value);
				}
				value = value.substring(3);
				return new java.lang.Byte(value);
			}
			if(c.equals(java.lang.Short.class)){
				if(!value.startsWith("SHO")){
					throw new HintDecodingException("Couldn't decode " + value);
				}
				value = value.substring(3);
				return new java.lang.Short(value);
			}
			if(c.equals(java.lang.Integer.class)){
				if(!value.startsWith("INT")){
					throw new HintDecodingException("Couldn't decode " + value);
				}
				value = value.substring(3);
				return new java.lang.Integer(value);
			}
			if(c.equals(java.lang.Long.class)){
				if(!value.startsWith("LON")){
					throw new HintDecodingException("Couldn't decode " + value);
				}
				value = value.substring(3);
				return new java.lang.Long(value);
			}
			if(c.equals(java.lang.Double.class)){
				if(!value.startsWith("DOU")){
					throw new HintDecodingException("Couldn't decode " + value);
				}
				value = value.substring(3);
				return new java.lang.Double(value);
			}
			if(c.equals(java.lang.Float.class)){
				if(!value.startsWith("FLO")){
					throw new HintDecodingException("Couldn't decode " + value);
				}
				value = value.substring(3);
				return new java.lang.Float(value);
			}
			if(c.equals(java.lang.Character.class)){
				if(!value.startsWith("CHA")){
					throw new HintDecodingException("Couldn't decode " + value);
				}
				value = value.substring(3);
				return new java.lang.Character(value.charAt(0));
			}
			throw new HintDecodingException("Couldn't decode " + value);
		}
		catch(Exception e){
			throw new HintDecodingException("Can't decode: " + value);
		}
	}

}

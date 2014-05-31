package archstudio.comp.archipelago.hints;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import c2.util.ClassArrayEncoder;

public class Hints {

	protected List valueCodingPlugins = new ArrayList();

	public static void main(String[] args){
		SingleHint sh = new SingleHint("someID-1000340.003030.0030430", "boundingBox", java.awt.Rectangle.class, new java.awt.Rectangle(0, 0, 50, 50));
		SingleHint sh2 = new SingleHint("someID-1000340.003030.0030455", "x", java.awt.Point.class, new java.awt.Point(25, 25));
		Hints h = new Hints();
		try{
			String encodedHints = h.encodeHints(new SingleHint[]{sh, sh2});
			System.out.println(encodedHints);
			
			SingleHint[] decodedHints = h.decodeHints(encodedHints);
			for(int i = 0; i < decodedHints.length; i++){
				System.out.println(decodedHints[i]);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public Hints(){
		addValueCodingPlugin(new TypeWrapperPlugin());
		addValueCodingPlugin(new ColorPlugin());
		addValueCodingPlugin(new PointPlugin());
		addValueCodingPlugin(new RectanglePlugin());
		//Note: this MUST come last if you want it to be the
		//encoder of last resort
		addValueCodingPlugin(new JavaSerializationPlugin());
	}

	public void addValueCodingPlugin(ValueCodingPlugin plugin){
		synchronized(valueCodingPlugins){
			valueCodingPlugins.add(plugin);
		}
	}
	
	public void removeValueCodingPlugin(ValueCodingPlugin plugin){
		synchronized(valueCodingPlugins){
			valueCodingPlugins.remove(plugin);
		}
	}
	
	private static String escape(String s){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < s.length(); i++){
			char ch = s.charAt(i);
			switch(ch){
				case '\\':
				case '|':
				case ':':
				case '=':
				case ';':
					sb.append('\\');
					break;
				default:
			}
			sb.append(ch);
		}
		return sb.toString();
	}

	public String encodeHints(SingleHint[] singleHints) throws HintEncodingException{
		StringBuffer sb = new StringBuffer();
		ValueCodingPlugin[] vcps = null;
		synchronized(valueCodingPlugins){
			vcps = (ValueCodingPlugin[])valueCodingPlugins.toArray(new ValueCodingPlugin[0]);
		}
		
		for(int i = 0; i < singleHints.length; i++){
			sb.append(escape(singleHints[i].getXArchID()));
			sb.append("|");
			sb.append(escape(singleHints[i].getName()));
			sb.append(":");
			Class c = singleHints[i].getValueClass();
			if(c == null){
				sb.append("null=null;");
			}
			else{
				sb.append(ClassArrayEncoder.classToString(c));
				sb.append("=");
				String encodedValue = null;
				for(int j = 0; j < vcps.length; j++){
					if(vcps[j].canEncode(c)){
						encodedValue = vcps[j].encode(c, singleHints[i].getValue());
						break;
					}
				}
				if(encodedValue == null){
					throw new HintEncodingException("Couldn't find encoder for hint: " + singleHints[i].getName() + ":" + c.getName());
				}
				sb.append(encodedValue);
				sb.append(";");
			}
		}
		return sb.toString();
	}

	static class IntermediateHint{
		String xArchIDString;
		String nameString;
		String classString;
		String valueString;
	}

	public SingleHint[] decodeHints(String encodedHints) throws HintDecodingException{
		List decodedHints = new ArrayList();
		
		List intermediateHints = new ArrayList();
		
		final int XARCHID_PHASE = 0;
		final int NAME_PHASE = 1;
		final int CLASS_PHASE = 2;
		final int VALUE_PHASE = 3;
		
		int phase = XARCHID_PHASE;
		StringBuffer xArchIDBuf = new StringBuffer();
		StringBuffer nameBuf = new StringBuffer();
		StringBuffer classBuf = new StringBuffer();
		StringBuffer valueBuf = new StringBuffer();
		int len = encodedHints.length();
		for(int i = 0; i < len; i++){
			char ch = encodedHints.charAt(i);
			if(ch == '\\'){
				i++;
				if(i == len){
					throw new HintDecodingException("Unexpected end of hints at char " + i + ".");
				}
				ch = encodedHints.charAt(i);
			}
			else{
				if((phase == XARCHID_PHASE) && (ch == '|')){
					phase++;
					continue;
				}
				if((phase == NAME_PHASE) && (ch == ':')){
					phase++;
					continue;
				}
				else if((phase == CLASS_PHASE) && (ch == '=')){
					phase++;
					continue;
				}
				else if((phase == VALUE_PHASE) && (ch == ';')){
					IntermediateHint ih = new IntermediateHint();
					ih.xArchIDString = xArchIDBuf.toString();
					ih.nameString = nameBuf.toString();
					ih.classString = classBuf.toString();
					ih.valueString = valueBuf.toString();
					intermediateHints.add(ih);
					xArchIDBuf.setLength(0);
					nameBuf.setLength(0);
					classBuf.setLength(0);
					valueBuf.setLength(0);
					phase = XARCHID_PHASE;
					continue;
				}
			}
			if(phase == XARCHID_PHASE){
				xArchIDBuf.append(ch);
			}
			else if(phase == NAME_PHASE){
				nameBuf.append(ch);
			}
			else if(phase == CLASS_PHASE){
				classBuf.append(ch);
			}
			else if(phase == VALUE_PHASE){
				valueBuf.append(ch);
			}
		}
		if(phase != XARCHID_PHASE){
			throw new HintDecodingException("Unexpected end of hints.");
		}
		else{
			if(xArchIDBuf.length() + nameBuf.length() + classBuf.length() + valueBuf.length() > 0){
				throw new HintDecodingException("Unexpected end of hints.");
			}
		}
		
		IntermediateHint[] ihs = (IntermediateHint[])intermediateHints.toArray(new IntermediateHint[0]);
		for(int i = 0; i < ihs.length; i++){
			String shXArchID = ihs[i].xArchIDString;
			String shName = ihs[i].nameString;
			if((ihs[i].classString == null) || (ihs[i].classString.equals("null"))){
				SingleHint sh = new SingleHint(shXArchID, shName, null, null);
				decodedHints.add(sh);
				continue;
			}
			try{
				Class shClass = c2.util.ClassArrayEncoder.stringToClass(ihs[i].classString);
				ValueCodingPlugin[] vcps = null;
				synchronized(valueCodingPlugins){
					vcps = (ValueCodingPlugin[])valueCodingPlugins.toArray(new ValueCodingPlugin[0]);
				}
				Object decodedValue = null;
				for(int j = 0; j < vcps.length; j++){
					if(vcps[j].canEncode(shClass)){
						try{
							decodedValue = vcps[j].decode(shClass, ihs[i].valueString);
							break;
						}
						catch(Exception e){
							//Ignore for now, maybe some later plugin can decode it.
						}
					}
				}
				if(decodedValue == null){
					throw new HintDecodingException("Couldn't decode hint: " + shName + ":" + shClass.getName() + "=" + ihs[i].valueString);
				}
				SingleHint sh = new SingleHint(shXArchID, shName, shClass, decodedValue);
				decodedHints.add(sh);
			}
			catch(ClassNotFoundException cnfe){
				throw new HintDecodingException(cnfe);
			}
			
		}
		
		return (SingleHint[])decodedHints.toArray(new SingleHint[0]);
	}
}

package archstudio.comp.archipelago;

import java.awt.Font;
import java.util.*;

import javax.swing.tree.TreePath;

import edu.uci.ics.bna.BNAUtils;

import archstudio.comp.preferences.IPreferences;

public class ArchipelagoUtils {

	private ArchipelagoUtils(){
		super();
	}

	public static ListDiff diffLists(List l1, List l2){
		Object[] arr1 = l1.toArray();
		Object[] arr2 = l2.toArray();
		
		ArrayList addList = new ArrayList();
		ArrayList noChangeList = new ArrayList();
		ArrayList removeList = new ArrayList();
		
		for(int i = 0; i < arr1.length; i++){
			Object o1 = arr1[i];
			boolean found = false;
			for(int j = 0; j < arr2.length; j++){
				Object o2 = arr2[j];
				if(o1.equals(o2)){
					noChangeList.add(o2);
					found = true;
					break;
				}
			}
			if(!found){
				removeList.add(o1);
			}
		}
		
		for(int i = 0; i < arr2.length; i++){
			Object o2 = arr2[i];
			boolean found = false;
			for(int j = 0; j < arr1.length; j++){
				Object o1 = arr1[j];
				if(o2.equals(o1)){
					found = true;
					break;
				}
			}
			if(!found){
				addList.add(o2);
			}
		}
		
		ListDiff ld = new ListDiff(addList, noChangeList, removeList);
		return ld;
	}

	public static class ListDiff{
		private List addList;
		private List noChangeList;
		private List removeList;
		
		public ListDiff(List addList, List noChangeList, List removeList){
			this.addList = addList;
			this.noChangeList = noChangeList;
			this.removeList = removeList;
		}
		
		public List getAddList(){
			return addList;
		}
		
		public List getNoChangeList(){
			return noChangeList;
		}
		
		public List getRemoveList(){
			return removeList;
		}
	}
	
	public static boolean getAntialiasText(IPreferences preferences){
		if(preferences.keyExists(IPreferences.USER_SPACE, 
		"/archstudio/comp/archipelago", "antialiasText")){
			String val = preferences.getStringValue(IPreferences.USER_SPACE, 
			"/archstudio/comp/archipelago", "antialiasText", null);
			if((val != null) && (val.equals("true"))){
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}
	
	public static boolean getAntialiasGraphics(IPreferences preferences){
		if(preferences.keyExists(IPreferences.USER_SPACE, 
		"/archstudio/comp/archipelago", "antialiasGraphics")){
			String val = preferences.getStringValue(IPreferences.USER_SPACE, 
			"/archstudio/comp/archipelago", "antialiasGraphics", null);
			if((val != null) && (val.equals("true"))){
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}

	public static boolean getGradientGraphics(IPreferences preferences){
		if(preferences.keyExists(IPreferences.USER_SPACE, 
		"/archstudio/comp/archipelago", "gradientGraphics")){
			String val = preferences.getStringValue(IPreferences.USER_SPACE, 
			"/archstudio/comp/archipelago", "gradientGraphics", null);
			if((val != null) && (val.equals("true"))){
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}
	
	public static Font getDefaultFont(IPreferences preferences){
		String fontName = BNAUtils.DEFAULT_FONT.getName();
		int fontSize = BNAUtils.DEFAULT_FONT.getSize();
		int fontStyle = BNAUtils.DEFAULT_FONT.getStyle();
		
		if(preferences.keyExists(IPreferences.USER_SPACE, 
		"/archstudio/comp/archipelago", "defaultFontName")){
			String val = preferences.getStringValue(IPreferences.USER_SPACE, 
			"/archstudio/comp/archipelago", "defaultFontName", null);
			if(val != null){
				fontName = val;
			}
		}
		
		if(preferences.keyExists(IPreferences.USER_SPACE, 
		"/archstudio/comp/archipelago", "defaultFontSize")){
			String val = preferences.getStringValue(IPreferences.USER_SPACE, 
			"/archstudio/comp/archipelago", "defaultFontSize", null);
			if(val != null){
				try{
					fontSize = Integer.parseInt(val);
				}
				catch(NumberFormatException nfe){}
			}
		}

		if(preferences.keyExists(IPreferences.USER_SPACE, 
		"/archstudio/comp/archipelago", "defaultFontStyle")){
			String val = preferences.getStringValue(IPreferences.USER_SPACE, 
			"/archstudio/comp/archipelago", "defaultFontStyle", null);
			if(val != null){
				try{
					fontStyle = Integer.parseInt(val);
				}
				catch(NumberFormatException nfe){}
			}
		}
		
		Font f = new Font(fontName, fontStyle, fontSize);
		return f;
	}
}

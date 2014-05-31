package archstudio.preferences;

import java.io.File;

import archstudio.comp.preferences.IPreferences;

public class RecentDirectoryPreferenceUtils{

	private RecentDirectoryPreferenceUtils(){}
	
	public static String getGoodRecentDirectory(IPreferences preferences){
		return getGoodRecentDirectory(preferences, null);
	}
	
	public static String getGoodRecentDirectory(IPreferences preferences, String forWhat){
		String recentDirectory = getRecentDirectory(preferences, forWhat);
		if(recentDirectory == null){
			return null;
		}
		File f = new File(recentDirectory);
		if(!f.exists()){
			return null;
		}
		if(!f.isDirectory()){
			return null;
		}
		if(!f.canRead()){
			return null;
		}
		return recentDirectory;
	}
	
	public static String getRecentDirectory(IPreferences preferences){
		return getRecentDirectory(preferences, null);
	}
	
	public static String getRecentDirectory(IPreferences preferences, String forWhat){
		if(forWhat == null){
			forWhat = "";
		}
		else{
			forWhat = "-" + forWhat;
		}
		
		if(preferences.keyExists(IPreferences.USER_SPACE, 
			"/archstudio/preferences", "recentDirectory" + forWhat)){
				String recentDirectory = preferences.getStringValue(IPreferences.USER_SPACE, 
				"/archstudio/preferences", "recentDirectory" + forWhat, null);
				return recentDirectory;
			}
			return null;
	}
	
	public static void storeRecentDirectory(IPreferences preferences, String recentDirectory){
		storeRecentDirectory(preferences, null, recentDirectory);
	}

	public static void storeRecentDirectory(IPreferences preferences, String forWhat, String recentDirectory){
		if(forWhat == null){
			forWhat = "";
		}
		else{
			forWhat = "-" + forWhat;
		}
		preferences.setValue(IPreferences.USER_SPACE, "/archstudio/preferences", 
			"recentDirectory" + forWhat, recentDirectory);
	}
}

package archstudio;

import java.io.*;

public class VersionInfo{

	public static final String VERSION_FILE_NAME = "archstudio.version";
	
	public static void main(String[] args){
		System.out.println(getVersion("[unknown version]"));
	}
	
	public static String getVersion(){
		BufferedReader br = null;
		try{
			InputStream is = ClassLoader.getSystemResourceAsStream(VERSION_FILE_NAME);
			if(is == null){
				return null;
			}
			br = new BufferedReader(new InputStreamReader(is));
			String version = br.readLine();
			version = version.trim();
			return version;
		}
		catch(IOException ioe){
			return null;
		}
		finally{
			if(br != null){
				try{
					br.close();
				}
				catch(IOException ioe2){}
			}
		}
	}
	
	public static boolean isVersionAvailable(){
		return getVersion() != null;
	}
	
	public static String getVersion(String defaultResponse){
		String version = getVersion();
		if(version == null){
			return defaultResponse;
		}
		else{
			return version;
		}
	}
}

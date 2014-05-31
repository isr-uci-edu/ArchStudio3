package archstudio.comp.tron.tools.schematron;

import archstudio.comp.preferences.IPreferences;
import archstudio.tron.*;

import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;

import edu.uci.ics.nativeutils.SystemUtils;

public class SchematronTestManager{

	public static final String DEFAULT_TEST_FILE_URL = "res:/archstudio/comp/tron/tools/schematron/res/";
	public static final String RULE_FILE_INDEX_NAME = "rulefileindex.txt";
	
	protected String toolID;
	protected IPreferences preferences;
	protected String[] testFileBaseURLs = null;
	protected String[] testFileURLs = null;
	protected SchematronTestFile[] testFiles = null;
	protected TronTest[] tronTests = null;
	
	//Either strings or Throwables
	protected Object[] warnings = new Object[0];
	
	public SchematronTestManager(String toolID, IPreferences preferences){
		this.toolID = toolID;
		this.preferences = preferences;
	}
	
	public SchematronTestFile[] getAllTestFiles(){
		return testFiles;
	}
	
	public TronTest getTronTest(String uid){
		TronTest[] tronTests = getAllTronTests();
		for(int i = 0; i < tronTests.length; i++){
			if(tronTests[i].getUID().equals(uid)){
				return tronTests[i];
			}
		}
		return null;
	}

	public TronTest[] getAllTronTests(){
		return tronTests;
	}
	
	public Object[] getWarnings(){
		return warnings;
	}
	
	public void reload(){
		clearWarnings();
		reloadBaseURLs();
		reloadFileURLs();
		reloadTestFiles();
		reloadTronTests();
	}
	
	private void clearWarnings(){
		warnings = new Object[0];
	}
	
	private void reloadBaseURLs(){
		List testFileBaseURLList = new ArrayList();
		testFileBaseURLList.add(DEFAULT_TEST_FILE_URL);
		
		int i = 0;
		while(true){
			if(preferences.keyExists(IPreferences.USER_SPACE, 
				"/archstudio/comp/tron/schematron", "testFileURL_" + i)){
				
				String newURI = preferences.getStringValue(IPreferences.USER_SPACE, 
					"/archstudio/comp/tron/schematron", "testFileURL_" + i, null);
				testFileBaseURLList.add(newURI);
			}
			else{
				break;
			}
			i++;
		}
		testFileBaseURLs = (String[])testFileBaseURLList.toArray(new String[0]);
	}
	
	private void reloadFileURLs(){
		List testFileURLList = new ArrayList();
		List newWarningsList = new ArrayList();
		
		for(int i = 0; i < testFileBaseURLs.length; i++){
			String urlString = testFileBaseURLs[i];
			if(!urlString.endsWith("/")){
				urlString += "/";
			}
			String ruleFileIndexURLString = urlString + RULE_FILE_INDEX_NAME;
			try{
				InputStream is = SystemUtils.openURL(ruleFileIndexURLString);
				if(is == null){
					throw new FileNotFoundException("Could not find file: " + ruleFileIndexURLString);
				}
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				while(true){
					String fileName = br.readLine();
					if(fileName == null){
						break;
					}
					fileName = fileName.trim();
					if(fileName.length() == 0){
						continue;
					}
					String ruleFileURLString = urlString + fileName;
					testFileURLList.add(ruleFileURLString);
				}
				br.close();
			}
			catch(MalformedURLException mue){
				newWarningsList.add(mue);
			}
			catch(FileNotFoundException fnfe){
				newWarningsList.add(fnfe);
			}
			catch(IOException ioe){
				newWarningsList.add(ioe);
			}
		}
		testFileURLs = (String[])testFileURLList.toArray(new String[0]);
		newWarningsList.addAll(Arrays.asList(warnings));
		warnings = newWarningsList.toArray();
	}
	
	private void reloadTestFiles(){
		List testFileList = new ArrayList();
		List newWarningsList = new ArrayList();
		
		for(int i = 0; i < testFileURLs.length; i++){
			try{
				SchematronTestFile stf = SchematronTestFile.create(toolID, testFileURLs[i]);
				testFileList.add(stf);
				String[] additionalWarnings = stf.getParseWarnings();
				for(int j = 0; j < additionalWarnings.length; j++){
					newWarningsList.add(additionalWarnings[j]);
				}
			}
			catch(SchematronTestFileParseException stfpe){
				newWarningsList.add(stfpe);
			}
			catch(MalformedURLException mue){
				newWarningsList.add(mue);
			}
			catch(FileNotFoundException fnfe){
				newWarningsList.add(fnfe);
			}
			catch(IOException ioe){
				newWarningsList.add(ioe);
			}
		}

		testFiles = (SchematronTestFile[])testFileList.toArray(new SchematronTestFile[0]);
		newWarningsList.addAll(Arrays.asList(warnings));
		warnings = newWarningsList.toArray();
	}

	private void reloadTronTests(){
		List tronTestList = new ArrayList();
		List newWarningsList = new ArrayList();
		Set testUIDs = new HashSet();
		for(int i = 0; i < testFiles.length; i++){
			TronTest[] fileTests = testFiles[i].getTronTests();
			for(int j = 0; j < fileTests.length; j++){
				String testUID = fileTests[j].getUID();
				if(testUIDs.contains(testUID)){
					SchematronInitializationException e = 
						new SchematronInitializationException("Duplicate Test UID: " + testUID);
					newWarningsList.add(e);
				}
				else{
					tronTestList.add(fileTests[j]);
					testUIDs.add(testUID);
				}
			}
		}
		newWarningsList.addAll(Arrays.asList(warnings));
		warnings = newWarningsList.toArray();
		tronTests = (TronTest[])tronTestList.toArray(new TronTest[0]);
	}
	
}

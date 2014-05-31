package archstudio.comp.tron.tools.schematron;

import archstudio.comp.preferences.IPreferences;
import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;
import archstudio.preferences.PreferencesUtils;
import archstudio.tron.*;

import c2.fw.*;
import c2.legacy.*;
import c2.pcwrap.*;
import c2.util.MessageSendProxy;

import java.util.*;

import edu.uci.ics.nativeutils.SystemUtils;
import edu.uci.ics.xarchutils.ObjRef;

public class SchematronC2Component extends AbstractC2DelegateBrick {

	public static String TOOL_ID = "Schematron";
	protected XArchFlatTransactionsInterface xarch = null;
	protected IPreferences preferences;

	protected SchematronTestManager testManager;
	protected TronDefaultToolNoticeManager toolNoticeManager;
	protected boolean xalanVersionOK = false;
	
	protected TronToolStatusMessage statusMessage = new TronToolStatusMessage(TOOL_ID, "Idle", -1);
	
	public SchematronC2Component(Identifier id){
		super(id);
		
		xarch = (XArchFlatTransactionsInterface)EBIWrapperUtils.addExternalService(this, topIface, XArchFlatTransactionsInterface.class);

		preferences = (IPreferences)EBIWrapperUtils.addExternalService(this,
			topIface, IPreferences.class);

		toolNoticeManager = new TronDefaultToolNoticeManager(TOOL_ID, this);
		testManager = new SchematronTestManager(TOOL_ID, preferences);
		addLifecycleProcessor(new SchematronLifecycleProcessor());
		addMessageProcessor(new SchematronMessageProcessor());

		SchematronPreferencePanel spp = new SchematronPreferencePanel();
		PreferencesUtils.deployPreferencesService(this, bottomIface, "ArchStudio 3/Schematron", spp);

	}
	
	protected void sendReplaceTestsMessage(TronTest[] tests){
		TronReplaceTestsMessage trtm = new TronReplaceTestsMessage(TOOL_ID, tests);
		sendToAll(trtm, topIface);
	}
	
	protected void reloadTests(){
		toolNoticeManager.addNotice("Reloading tests at [" + SystemUtils.getDateAndTime() + "]");
		testManager.reload();
		TronTest[] newTests = testManager.getAllTronTests();
		sendReplaceTestsMessage(newTests);

		Object[] warnings = testManager.getWarnings();
		if(warnings.length > 0){
			for(int i = 0; i < warnings.length; i++){
				if(warnings[i] instanceof String){
					toolNoticeManager.addNotice("Warning:" + warnings[i]);
				}
				else if(warnings[i] instanceof Throwable){
					Throwable t = (Throwable)warnings[i];
					toolNoticeManager.addNotice("Error: " + t.getMessage(), t);
				}
			}
		}
	}
	
	class SchematronLifecycleProcessor extends LifecycleAdapter{
		public void begin(){
			toolNoticeManager.addNotice("Schematron Tron Tool Initialized at [" + SystemUtils.getDateAndTime() + "]");
			String xalanVersion = SchematronUtils.getXalanVersion();
			if(xalanVersion == null){
				xalanVersionOK = false;
				toolNoticeManager.addNotice("Error: No Xalan version found.  Tests cannot run.");
			}
			else{
				xalanVersionOK = true;
				toolNoticeManager.addNotice("Xalan version " + xalanVersion);
			}
			reloadTests();
			sendToolStatus();
		}
		
		public void end(){
			sendToolStatus(TronToolStatusMessage.ENDING_STATUS, -1);
		}
	}
	
	class SchematronMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof TronRunTestsMessage){
				handleRunTests((TronRunTestsMessage)m);
			}
			else if(m instanceof TronRefreshTestsMessage){
				handleRefreshTests((TronRefreshTestsMessage)m);
			}
			else if(m instanceof TronGetAllToolStatusesMessage){
				handleGetAllToolStatuses((TronGetAllToolStatusesMessage)m);
			}
		}
	}
	
	public void handleGetAllToolStatuses(TronGetAllToolStatusesMessage m){
		sendToolStatus();
	}

	public void handleRunTests(TronRunTestsMessage m){
		List schematronTestErrorList = new ArrayList();
		List tronTestResultList = new ArrayList();
		
		if(!xalanVersionOK){
			SchematronTestException ste = new SchematronTestException("Schematron requires Xalan; but the version of Xalan available was not sufficient to run Schematron tests.");
			schematronTestErrorList.add(ste);
		}
		else{
			SchematronTestFile[] testFiles = testManager.getAllTestFiles();
			Set filesToRun = new HashSet();
			String[] testUIDsToRun = m.getTestUIDs();
			for(int i = 0; i < testUIDsToRun.length; i++){
				for(int j = 0; j < testFiles.length; j++){
					TronTest[] testsInFile = testFiles[j].getTronTests();
					for(int k = 0; k < testsInFile.length; k++){
						if(testUIDsToRun[i].equals(testsInFile[k].getUID())){
							filesToRun.add(testFiles[j]);
							break;
						}
					}
				}
			}
			ObjRef docRef = m.getDocumentRef();
			String xmlDocument = xarch.serialize(docRef);
			int filesToRunSize = filesToRun.size();
			int f = 0;
			for(Iterator it = filesToRun.iterator(); it.hasNext(); f++){
				SchematronTestFile fileToRun = (SchematronTestFile)it.next();
				try{
					fileToRun = SchematronTestFile.create(fileToRun, testUIDsToRun);
				}
				catch(SchematronTestFileParseException stfpe){
					SchematronTestException ste = new SchematronTestException(
						"Error parsing Schematron test file.", stfpe);
					schematronTestErrorList.add(ste);
				}
				if(fileToRun != null){
					SchematronTester tester = new SchematronTester(xmlDocument, fileToRun);
					toolNoticeManager.addNotice("Processing: " + fileToRun.getSourceURL());
					float pct = (float)f / (float)filesToRunSize;
					pct *= 100;
					if(pct == 0) pct = 5;
					sendToolStatus("Running Tests", (int)pct);
					try{
						tester.runTest();
						Object[] results = SchematronTestResultParser.parseTestResults(xarch, docRef, TOOL_ID, tester.getResult());
						for(int i = 0; i < results.length; i++){
							if(results[i] instanceof SchematronTestException){
								schematronTestErrorList.add(results[i]);
							}
							else if(results[i] instanceof TronTestResult){
								//System.out.println("result: " + results[i]);
								tronTestResultList.add(results[i]);
							}
						}
					}
					catch(SchematronInitializationException sie){
						SchematronTestException ste = new SchematronTestException("Error initializing Schematron", sie);
						schematronTestErrorList.add(ste);
					}
					catch(SchematronTestException ste){
						schematronTestErrorList.add(ste);
					}
				}
			}
			sendToolStatus("Idle", -1);
		}
		//Now we have two lists: a list of TronTestResults and a list of
		//SchematronTestExceptions if anything went wrong during testing.

		TronTestResult[] testResults =
			(TronTestResult[])tronTestResultList.toArray(new TronTestResult[0]);
		if(testResults.length > 0){
			List testResultMessageList = new ArrayList(testResults.length);
			for(int i = 0; i < testResults.length; i++){
				String testUID = testResults[i].getTestUID();
				TronTest test = testManager.getTronTest(testUID);
				if(test == null){
					SchematronTestException ste = new SchematronTestException("Can't find test in Schematron Test Manager");
					ste.setTestUID(testUID);
					schematronTestErrorList.add(ste);
				}
				else{
					TronTestResultMessage ttrm = new TronTestResultMessage(
						test, testResults[i].getDocumentRef(), testResults[i].getIssues());
					testResultMessageList.add(ttrm);
				}
			}
			TronTestResultMessage[] testResultMessages = 
				(TronTestResultMessage[])testResultMessageList.toArray(new TronTestResultMessage[0]);
			TronTestResultSetMessage ttrsm = new TronTestResultSetMessage(testResultMessages);
			sendToAll(ttrsm, topIface);
		}

		//Send off the errors (if any).
		SchematronTestException[] testExceptions = 
			(SchematronTestException[])schematronTestErrorList.toArray(new SchematronTestException[0]);
		if(testExceptions.length > 0){
			TronTestError[] testErrors = new TronTestError[testExceptions.length];
			for(int i = 0; i < testExceptions.length; i++){
				String message = testExceptions[i].getMessage();
				if(message == null) message = "Test Error";
				testErrors[i] = new TronTestError(testExceptions[i].getTestUID(), message, null, testExceptions[i]);
			}
			TronTestErrorsMessage testErrorMessage = new TronTestErrorsMessage(TOOL_ID, testErrors);
			sendToAll(testErrorMessage, bottomIface);
		}
	}
	
	public void handleRefreshTests(TronRefreshTestsMessage m){
		reloadTests();
	}
	
	public void sendToolStatus(String status, int progressPercent){
		statusMessage = new TronToolStatusMessage(TOOL_ID, status, progressPercent);
		sendToolStatus();
	}
	
	protected void sendToolStatus(){
		sendToAll(statusMessage, bottomIface);
	}
}

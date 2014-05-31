package archstudio.comp.tron.gui;

import archstudio.tron.*;
import java.util.*;

public class TronGUIConsoleModel{

	public static final String TEST_ERROR_TOOLNAME = "Test Errors";
	
	//Maps tool IDs to TronToolNotice[]s.
	protected Map toolNoticesMap = new HashMap();
	
	public TronGUIConsoleModel(){
	}
	
	public synchronized String[] getAllToolIDs(){
		return (String[])toolNoticesMap.keySet().toArray(new String[0]);
	}
	
	public synchronized void setToolNotices(String toolID, TronToolNotice[] newNotices){
		toolNoticesMap.put(toolID, newNotices);
		fireConsoleModelChanged();
	}
	
	public synchronized TronToolNotice[] getToolNotices(String toolID){
		return (TronToolNotice[])toolNoticesMap.get(toolID);
	}
	
	public void handleAllToolNotices(TronAllToolNoticesMessage m){
		String toolID = m.getToolID();
		TronToolNotice[] toolNotices = m.getToolNotices();
		setToolNotices(toolID, toolNotices);
	}
	
	public synchronized void handleTestErrors(TronTestErrorsMessage m, TronTest[] allTests){
		String toolID = m.getToolID();
		TronTestError[] testErrors = m.getTestErrors();
		
		List combinedTestErrors = new ArrayList();
		TronToolNotice[] oldErrors = getToolNotices(TEST_ERROR_TOOLNAME);
		if(oldErrors != null){
			combinedTestErrors.addAll(Arrays.asList(oldErrors));
		}
		TronToolNotice[] newErrors = new TronToolNotice[testErrors.length];
		String currentDateTime = edu.uci.ics.nativeutils.SystemUtils.getDateAndTime();
		for(int i = 0; i < newErrors.length; i++){
			String testCategory = null;
			String testUID = testErrors[i].getTestUID();
			if(testUID != null){
				for(int j = 0; j < allTests.length; j++){
					if(allTests[j].getUID().equals(testUID)){
						testCategory = allTests[j].getCategory();
						break;
					}
				}
			}
			String detail = testErrors[i].getAdditionalDetail();
			if(testCategory != null){
				if(detail == null){
					detail = "";
				}
				detail = "Test: " + testCategory + "\n\n";
			}
			newErrors[i] = new TronToolNotice("Error: [" + currentDateTime + "] " + 
				testErrors[i].getMessage(),	detail, 
				testErrors[i].getError());
		}
		combinedTestErrors.addAll(Arrays.asList(newErrors));
		setToolNotices(TEST_ERROR_TOOLNAME, (TronToolNotice[])combinedTestErrors.toArray(new TronToolNotice[0]));
	}
	
	protected Vector tronGUIConsoleModelListeners = new Vector();
	
	public void addTronGUIConsoleModelListener(TronGUIConsoleModelListener l){
		tronGUIConsoleModelListeners.addElement(l);
	}

	public void removeTronGUIConsoleModelListener(TronGUIConsoleModelListener l){
		tronGUIConsoleModelListeners.removeElement(l);
	}
	
	protected void fireConsoleModelChanged(){
		synchronized(tronGUIConsoleModelListeners){
			for(Iterator it = tronGUIConsoleModelListeners.iterator(); it.hasNext(); ){
				((TronGUIConsoleModelListener)it.next()).consoleModelChanged(this);
			}
		}
	}
}

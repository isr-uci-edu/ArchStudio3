package archstudio.comp.tron.testadt;

import java.util.*;

import archstudio.tron.*;
import edu.uci.ics.xarchutils.ObjRef;
	
public class TronTestStore{

	private static final TronTest[] emptyTestArray = new TronTest[0];
	
	protected List testList;
	
	public TronTestStore(){
		testList = new ArrayList();
	}
	
	public synchronized TronTest[] getAllTests(){
		return (TronTest[])testList.toArray(emptyTestArray);
	}
	
	public synchronized TronTest[] getAllTests(String toolID){
		List matchingList = new ArrayList();
		for(Iterator it = testList.iterator(); it.hasNext(); ){
			TronTest test = (TronTest)it.next();
			if((test.getToolID() != null) && (test.getToolID().equals(toolID))){
				matchingList.add(test);
			}
		}
		return (TronTest[])matchingList.toArray(emptyTestArray);
	}
	
	public synchronized TronTest getTest(String testUID){
		for(Iterator it = testList.iterator(); it.hasNext(); ){
			TronTest test = (TronTest)it.next();
			if((test.getUID() != null) && (test.getUID().equals(testUID))){
				return test;
			}
		}
		return null;
	}
	
	public void addTests(TronTest[] tests){
		for(int i = 0; i < tests.length; i++){
			testList.add(tests[i]);
		}
	}
	
	public void removeTests(TronTest[] tests){
		for(int i = 0; i < tests.length; i++){
			testList.remove(tests[i]);
		}
	}
	
}

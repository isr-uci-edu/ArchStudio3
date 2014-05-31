package archstudio.tron;

import java.util.*;

public class TronTestListDiff{

	protected TronTest[] testsToRemove;
	protected TronTest[] testsToAdd;
	
	public TronTestListDiff(TronTest[] testsToRemove, TronTest[] testsToAdd){
		this.testsToRemove = testsToRemove;
		this.testsToAdd = testsToAdd;
	}

	public TronTest[] getTestsToAdd(){
		return testsToAdd;
	}

	public TronTest[] getTestsToRemove(){
		return testsToRemove;
	}
	
	public static TronTestListDiff diffLists(TronTest[] oldList, TronTest[] newList){
		List t2r = new ArrayList();
		List t2a = new ArrayList();
		
		//Find tests to add - tests that are in new list that are not in old list.
		for(int i = 0; i < newList.length; i++){
			boolean found = false;
			for(int j = 0; j < oldList.length; j++){
				if(newList[i].equals(oldList[j])){
					found = true;
					break;
				}
			}
			if(!found){
				t2a.add(newList[i]);
			}
		}
		
		//Find tests to remove - tests that are in old list that are not in new list.
		for(int i = 0; i < oldList.length; i++){
			boolean found = false;
			for(int j = 0; j < newList.length; j++){
				if(oldList[i].equals(newList[j])){
					found = true;
					break;
				}
			}
			if(!found){
				t2r.add(oldList[i]);
			}
		}
		
		TronTest[] testsToRemove = (TronTest[])t2r.toArray(new TronTest[0]);
		TronTest[] testsToAdd = (TronTest[])t2a.toArray(new TronTest[0]);
		return new TronTestListDiff(testsToRemove, testsToAdd);
	}
}

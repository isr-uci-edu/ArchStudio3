package archstudio.comp.tron.gui;

import java.util.*;

public class TronGUIDocTestListDiff{

	protected TronGUIDocTest[] addedDocTests;
	protected TronGUIDocTest[] changedDocTestsBefore;
	protected TronGUIDocTest[] changedDocTestsAfter;
	protected TronGUIDocTest[] removedDocTests;
	
	public TronGUIDocTestListDiff(TronGUIDocTest[] addedDocTests,
		TronGUIDocTest[] changedDocTestsBefore, TronGUIDocTest[] changedDocTestsAfter, 
		TronGUIDocTest[] removedDocTests){
		super();
		this.addedDocTests = addedDocTests;
		this.changedDocTestsBefore = changedDocTestsBefore;
		this.changedDocTestsAfter = changedDocTestsAfter;
		this.removedDocTests = removedDocTests;
	}
	
	public TronGUIDocTest[] getAddedDocTests(){
		return addedDocTests;
	}
	
	public void setAddedDocTests(TronGUIDocTest[] addedDocTests){
		this.addedDocTests = addedDocTests;
	}
	
	public TronGUIDocTest[] getChangedDocTestsBefore(){
		return changedDocTestsBefore;
	}
	
	public void setChangedDocTestsBefore(TronGUIDocTest[] changedDocTestsBefore){
		this.changedDocTestsBefore = changedDocTestsBefore;
	}
	
	public TronGUIDocTest[] getChangedDocTestsAfter(){
		return changedDocTestsAfter;
	}
	
	public void setChangedDocTestsAfter(TronGUIDocTest[] changedDocTestsAfter){
		this.changedDocTestsAfter = changedDocTestsAfter;
	}
	
	public TronGUIDocTest[] getRemovedDocTests(){
		return removedDocTests;
	}
	
	public void setRemovedDocTests(TronGUIDocTest[] removedDocTests){
		this.removedDocTests = removedDocTests;
	}
	
	public static TronGUIDocTestListDiff diffLists(TronGUIDocTest[] oldTests, TronGUIDocTest[] newTests){
		List at = new ArrayList();
		List ctb = new ArrayList();
		List cta = new ArrayList();
		List rt = new ArrayList();
		
		//Find adds (tests that are in the new list that aren't in the old list)
		//and changes (tests in both lists that are different.
		for(int i = 0; i < newTests.length; i++){
			boolean found = false;
			for(int j = 0; j < oldTests.length; j++){
				String newTestUID = newTests[i].getTestUID();
				String oldTestUID = oldTests[j].getTestUID();
				if((newTestUID != null) && (oldTestUID != null) && (newTestUID.equals(oldTestUID))){
					//found a match. 
					found = true;
					//Let's see if it's changed.
					if(newTests[i].isEnabled() != oldTests[j].isEnabled()){
						//It changed state.
						ctb.add(oldTests[j]);
						cta.add(newTests[j]);
					}
					break;
				}
			}
			if(!found){
				//It was added
				at.add(newTests[i]);
			}
		}
		
		
		//Find removes (tests that are in the old list that aren't in the new list)
		for(int i = 0; i < oldTests.length; i++){
			boolean found = false;
			for(int j = 0; j < newTests.length; j++){
				String oldTestUID = oldTests[i].getTestUID();
				String newTestUID = newTests[j].getTestUID();
				if((newTestUID != null) && (oldTestUID != null) && (newTestUID.equals(oldTestUID))){
					//found a match. 
					found = true;
					break;
				}
			}
			if(!found){
				//It was removed.
				rt.add(oldTests[i]);
			}
		}
		TronGUIDocTest[] addedTests = (TronGUIDocTest[])at.toArray(new TronGUIDocTest[0]);
		TronGUIDocTest[] changedTestsBefore = (TronGUIDocTest[])ctb.toArray(new TronGUIDocTest[0]);
		TronGUIDocTest[] changedTestsAfter = (TronGUIDocTest[])cta.toArray(new TronGUIDocTest[0]);
		TronGUIDocTest[] removedTests = (TronGUIDocTest[])rt.toArray(new TronGUIDocTest[0]);
		return new TronGUIDocTestListDiff(addedTests, changedTestsBefore, changedTestsAfter, removedTests);
	}
}

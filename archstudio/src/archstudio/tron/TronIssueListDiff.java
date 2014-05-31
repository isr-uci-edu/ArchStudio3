package archstudio.tron;

import java.util.*;

public class TronIssueListDiff{

	protected TronIssue[] issuesToRemove;
	protected TronIssue[] issuesToAdd;
	
	public TronIssueListDiff(TronIssue[] issuesToRemove, TronIssue[] issuesToAdd){
		this.issuesToRemove = issuesToRemove;
		this.issuesToAdd = issuesToAdd;
	}

	public TronIssue[] getIssuesToAdd(){
		return issuesToAdd;
	}

	public TronIssue[] getIssuesToRemove(){
		return issuesToRemove;
	}
	
	public static TronIssueListDiff diffLists(TronIssue[] oldList, TronIssue[] newList){
		List i2r = new ArrayList();
		List i2a = new ArrayList();
		
		//Find issues to add - issues that are in new list that are not in old list.
		for(int i = 0; i < newList.length; i++){
			boolean found = false;
			for(int j = 0; j < oldList.length; j++){
				if(newList[i].equals(oldList[j])){
					found = true;
					break;
				}
			}
			if(!found){
				i2a.add(newList[i]);
			}
		}
		
		//Find issues to remove - issues that are in old list that are not in new list.
		for(int i = 0; i < oldList.length; i++){
			boolean found = false;
			for(int j = 0; j < newList.length; j++){
				if(oldList[i].equals(newList[j])){
					found = true;
					break;
				}
			}
			if(!found){
				i2r.add(oldList[i]);
			}
		}
		
		TronIssue[] issuesToRemove = (TronIssue[])i2r.toArray(new TronIssue[0]);
		TronIssue[] issuesToAdd = (TronIssue[])i2a.toArray(new TronIssue[0]);
		return new TronIssueListDiff(issuesToRemove, issuesToAdd);
	}
}

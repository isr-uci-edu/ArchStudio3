package archstudio.comp.tron.gui;

import archstudio.tron.*;

import javax.swing.*;
import javax.swing.table.*;

import edu.uci.ics.xarchutils.ObjRef;

import java.util.*;

public class TronGUITableModel extends AbstractTableModel{

	public static final int SEVERITY_COLUMN_INDEX = 0;
	public static final int ISSUE_COLUMN_INDEX = 1;
	public static final int DOCUMENT_COLUMN_INDEX = 2;
	
	public static final String[] COLUMN_NAMES = new String[]{
		" ",
		"Issue Found",
		"Document"
	};
	
	public static final String ERROR_INDICATOR = "$$error$$";
	public static final String WARNING_INDICATOR = "$$warning$$";
	public static final String INFO_INDICATOR = "$$info$$";
	
	protected List issueList = Collections.synchronizedList(new ArrayList());
	
	protected TronGUITreeModel treeModel;
	
	public TronGUITableModel(TronGUITreeModel treeModel){
		super();
		this.treeModel = treeModel;
	}
	
	public TronIssue getIssueAt(int index){
		try{
			return (TronIssue)issueList.get(index);
		}
		catch(Exception e){
			return null;
		}
	}

  public int getRowCount(){
  	return issueList.size();
  }

  public String getColumnName(int index){
  	return COLUMN_NAMES[index];
  }
  
  public int getColumnCount(){
  	return COLUMN_NAMES.length;
  }
  
  public Object getValueAt(int row, int column){
  	TronIssue issue = (TronIssue)issueList.get(row);
  	switch(column){
  	case SEVERITY_COLUMN_INDEX:
  		int severity = issue.getSeverity();
  		if(severity == TronIssue.SEVERITY_ERROR) return ERROR_INDICATOR;
  		if(severity == TronIssue.SEVERITY_WARNING) return WARNING_INDICATOR;
  		return INFO_INDICATOR;
  	case ISSUE_COLUMN_INDEX:
  		return issue.getHeadline();
  	case DOCUMENT_COLUMN_INDEX:
  		String result = "[unknown]";
  		ObjRef documentRef = issue.getDocumentRef();
  		if(documentRef != null){
    		TronGUITreeDocumentNode docNode = treeModel.getDocumentNode(documentRef);
  			if(docNode != null){
  				result = docNode.toString();
  			}
  		}
  		return result;
  	default:
  		//This shouldn't happen
  		throw new IllegalArgumentException("Invalid column name.");
  	}
  }
  
  public void handleAllIssues(TronAllIssuesMessage m){
  	synchronized(issueList){
    	issueList.clear();
    	issueList.addAll(Arrays.asList(m.getAllIssues()));
  	}
  	fireTableDataChanged();
  }
  
  public void handleIssuesChanged(TronIssuesChangedMessage m){
  	synchronized(issueList){
  		TronIssueListDiff issueListDiff = m.getIssueListDiff();
  		TronIssue[] issuesToRemove = issueListDiff.getIssuesToRemove();
  		TronIssue[] issuesToAdd = issueListDiff.getIssuesToAdd();
  		for(int i = 0; i < issuesToRemove.length; i++){
  			issueList.remove(issuesToRemove[i]);
  		}
  		for(int i = 0; i < issuesToAdd.length; i++){
  			issueList.add(issuesToAdd[i]);
  		}
  	}
		fireTableDataChanged();
  }
}

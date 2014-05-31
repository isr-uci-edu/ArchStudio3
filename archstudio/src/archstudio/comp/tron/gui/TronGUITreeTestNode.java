package archstudio.comp.tron.gui;

import archstudio.tron.*;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import edu.uci.ics.widgets.CompositeIcon;
import edu.uci.ics.widgets.IIconable;
import edu.uci.ics.xadlutils.Resources;

public class TronGUITreeTestNode extends DefaultMutableTreeNode implements IIconable{
	public static final Icon NO_TEST_FOLDER_ICON =
		Resources.FOLDER_ICON;
	public static final Icon NOT_APPLIED_FOLDER_ICON =
		new CompositeIcon(Resources.CHECKBOX_UNCHECKED_OVERLAY_ICON, Resources.FOLDER_ICON);
	public static final Icon APPLIED_FOLDER_ICON =
		new CompositeIcon(Resources.CHECKBOX_CHECKED_OVERLAY_ICON, Resources.FOLDER_ICON);
	public static final Icon DISABLED_FOLDER_ICON =
		new CompositeIcon(Resources.RED_X_OVERLAY_ICON, Resources.FOLDER_ICON);
		
	public static final Icon NO_TEST_DOCUMENT_ICON =
		Resources.DOCUMENT_ICON;
	public static final Icon NOT_APPLIED_DOCUMENT_ICON =
		new CompositeIcon(Resources.CHECKBOX_UNCHECKED_OVERLAY_ICON, Resources.DOCUMENT_ICON);
	public static final Icon APPLIED_DOCUMENT_ICON =
		new CompositeIcon(Resources.CHECKBOX_CHECKED_OVERLAY_ICON, Resources.DOCUMENT_ICON);
	public static final Icon DISABLED_DOCUMENT_ICON =
		new CompositeIcon(Resources.RED_X_OVERLAY_ICON, Resources.DOCUMENT_ICON);

	protected TronTest test;
	protected boolean isApplied;
	protected boolean isEnabled;
	protected boolean isUnknown;
	
	public TronGUITreeTestNode(String nodeName){
		super(nodeName);
		this.test = null;
		this.isApplied = false;
		this.isEnabled = false;
		this.isUnknown = false;
	}
	
	public TronGUITreeTestNode(TronTest test, boolean isApplied, boolean isEnabled){
		super(getNodeName(test));
		this.test = test;
		this.isApplied = isApplied;
		this.isEnabled = isEnabled;
		this.isUnknown = false;
	}

	public javax.swing.Icon getIcon(){
		if(getChildCount() > 0){
			//Folder icon
			if(test == null){
				return NO_TEST_FOLDER_ICON;
			}
			else if(!isApplied){
				return NOT_APPLIED_FOLDER_ICON;
			}
			else if(!isEnabled){
				return DISABLED_FOLDER_ICON;
			}
			else{
				return APPLIED_FOLDER_ICON;
			}
		}
		else{
			//Document icon
			if(test == null){
				return NO_TEST_DOCUMENT_ICON;
			}
			else if(!isApplied){
				return NOT_APPLIED_DOCUMENT_ICON;
			}
			else if(!isEnabled){
				return DISABLED_DOCUMENT_ICON;
			}
			else{
				return APPLIED_DOCUMENT_ICON;
			}
		}
	}
	
	public boolean getAllowsChildren(){
		return getChildCount() > 0;
	}
		
	public void setTest(TronTest test){
		this.test = test;
		setUserObject(getNodeName(test));
	}
	
	public TronTest getTest(){
		return test;
	}

	public void setApplied(boolean isApplied){
		this.isApplied = isApplied;
	}
	
	public boolean isApplied(){
		return isApplied;
	}
	
	public void setEnabled(boolean isEnabled){
		this.isEnabled = isEnabled;
	}
	
	public boolean isEnabled(){
		return isEnabled;
	}
	
	public boolean isUnknown(){
		return isUnknown;
	}
	
	public void setUnknown(boolean unknown){
		this.isUnknown = unknown;
	}

	protected static String getNodeName(TronTest test){
		String category = test.getCategory();
		if(category == null){
			return "[null]";
		}
		String[] pathSegments = TronTest.getCategoryPathComponents(category);
		if(pathSegments.length == 0){
			return "[unknown]";
		}
		return pathSegments[pathSegments.length - 1];
	}

}

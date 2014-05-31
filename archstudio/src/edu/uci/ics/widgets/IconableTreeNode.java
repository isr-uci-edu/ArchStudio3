package edu.uci.ics.widgets;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import edu.uci.ics.xarchutils.ObjRef;

public class IconableTreeNode extends DefaultMutableTreeNode implements IIconable{

	protected Icon icon;

	public IconableTreeNode(){
		super();
	}

	public IconableTreeNode(Object arg0){
		super(arg0);
	}

	public IconableTreeNode(Object arg0, boolean arg1){
		super(arg0, arg1);
	}
	
	public void setIcon(Icon icon){
		this.icon = icon;
	}
	
	public Icon getIcon(){
		return icon;
	}
	
	private Boolean isLeafOverride = null;
	
	public void setOverrideIsLeaf(boolean ilo){
		isLeafOverride = new Boolean(ilo);
	}
	
	public void clearOverrideIsLeaf(){
		isLeafOverride = null;
	}
	
	public boolean isLeaf(){
		if(isLeafOverride != null){
			return isLeafOverride.booleanValue();
		}
		else{
			return super.isLeaf();
		}
	}

}

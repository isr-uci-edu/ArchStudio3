package edu.uci.ics.widgets;

import javax.swing.*;
import javax.swing.tree.*;

import java.awt.Component;

public class IconableTreeCellRenderer extends DefaultTreeCellRenderer{

	protected boolean customIconsOnlyForLeafs = true;

	public IconableTreeCellRenderer(){
		super();
	}
	
	public void setCustomIconsOnlyForLeafs(boolean ciofl){
		this.customIconsOnlyForLeafs = ciofl;
	}
	
	public Component getTreeCellRendererComponent(JTree tree, 
	Object value, boolean sel, boolean expanded, 
	boolean leaf, int row, boolean hasFocus){
		
		super.getTreeCellRendererComponent(
						tree, value, sel,
						expanded, leaf, row,
						hasFocus);

		if(!customIconsOnlyForLeafs || (leaf && customIconsOnlyForLeafs)){
			TreePath pathForRow = tree.getPathForRow(row);
			if (pathForRow != null) {
				Object lastPathComponent =
					pathForRow.getLastPathComponent();
				if(lastPathComponent instanceof IIconable){
					IIconable atn =
						(IIconable) lastPathComponent;
					Icon icon = atn.getIcon();
					if(icon != null){
						setIcon(icon);
					}
				}
			}
		}

		return this;
	}

}

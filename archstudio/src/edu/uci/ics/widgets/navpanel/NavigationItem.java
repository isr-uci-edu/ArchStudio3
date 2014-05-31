package edu.uci.ics.widgets.navpanel;

import javax.swing.Icon;

public class NavigationItem{

	protected String description;
	protected Object userData;
	protected Icon icon = null;

	public NavigationItem(String description, Object userData){
		this.description = description;
		this.userData = userData;
	}
	
	public String toString(){
		return description;
	}
	
	public String getDescription(){
		return description;
	}
	
	public Object getUserData(){
		return userData;
	}
	
	public void setIcon(Icon icon){
		this.icon = icon;
	}
	
	public Icon getIcon(){
		return icon;
	}

}

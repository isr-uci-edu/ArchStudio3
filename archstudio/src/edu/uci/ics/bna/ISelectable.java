package edu.uci.ics.bna;

public interface ISelectable extends Thing{

	public static final String SELECTED_PROPERTY_NAME = "selected";
	
	public void setSelected(boolean selected);
	public boolean isSelected();
	

}
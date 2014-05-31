package edu.uci.ics.bna;

public interface IUserEditable {
	
	public static final String USER_EDITABLE_PROPERTY_NAME = "userEditable";
	
	public boolean isUserEditable();
	public void setUserEditable(boolean userEditable);

}

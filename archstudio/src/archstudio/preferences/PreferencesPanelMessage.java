package archstudio.preferences;

import c2.fw.Message;
import c2.fw.NamedPropertyMessage;

public class PreferencesPanelMessage extends NamedPropertyMessage{
	
	public static final int SERVICE_ADVERTISED = 200;
	public static final int SERVICE_UNADVERTISED = 250;	
	
	public PreferencesPanelMessage(int state, String preferencesTreePath, archstudio.preferences.PreferencePanel preferencePanel){
		super("PreferencesPanelMessage");
		super.addParameter("state", state);
		super.addParameter("preferencesTreePath", preferencesTreePath);
		super.addParameter("preferencePanel", preferencePanel);
	}

	protected PreferencesPanelMessage(PreferencesPanelMessage copyMe){
		super(copyMe);
	}

	public Message duplicate(){
		return new PreferencesPanelMessage(this);
	}

	public void setPreferencesTreePath(String preferencesTreePath){
		addParameter("preferencesTreePath", preferencesTreePath);
	}

	public String getPreferencesTreePath(){
		return (String)getParameter("preferencesTreePath");
	}

	public void setPreferencePanel(archstudio.preferences.PreferencePanel preferencePanel){
		addParameter("preferencePanel", preferencePanel);
	}

	public archstudio.preferences.PreferencePanel getPreferencePanel(){
		return (archstudio.preferences.PreferencePanel)getParameter("preferencePanel");
	}
	
	public void setState(int state){
		addParameter("state", state);
	}
	
	public int getState(){
		return getIntParameter("state");
	}

}


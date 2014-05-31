package archstudio.preferences;

import c2.fw.*;

public class PreferencesLifecycleProcessor extends LifecycleAdapter{
	
	protected DelegateBrick target;
	protected Interface preferencesInterface;
	protected String path;
	protected PreferencePanel preferencePanel;
	
	public PreferencesLifecycleProcessor(DelegateBrick target, Interface preferencesInterface, String path, PreferencePanel preferencePanel){
		this.target = target;
		this.preferencesInterface = preferencesInterface;
		this.path = path;
		this.preferencePanel = preferencePanel;
	}
	
	public DelegateBrick getBrick(){
		return target;
	}
	
	public void setPreferencePanel(PreferencePanel newPreferencePanel){
		this.preferencePanel = newPreferencePanel;
	}
	
	public Interface getInterface(){
		return preferencesInterface;
	}
	
	public String getPath(){
		return path;
	}

	private void sendAdvertisement(){
		PreferencesPanelMessage ppm = new PreferencesPanelMessage(PreferencesPanelMessage.SERVICE_ADVERTISED, 
			path, preferencePanel);
		target.sendToAll(ppm, preferencesInterface);
	}
	
	private void sendUnadvertisement(){
		PreferencesPanelMessage ppm = new PreferencesPanelMessage(PreferencesPanelMessage.SERVICE_UNADVERTISED, 
			path, preferencePanel);
		target.sendToAll(ppm, preferencesInterface);
	}
	
	public void begin(){
		sendAdvertisement();
	}
	
	public void end(){
		sendUnadvertisement();
	}

}

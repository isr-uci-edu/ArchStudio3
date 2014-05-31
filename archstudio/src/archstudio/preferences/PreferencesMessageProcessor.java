package archstudio.preferences;

import c2.fw.*;

public class PreferencesMessageProcessor implements MessageProcessor{

	protected DelegateBrick target;
	protected Interface preferencesInterface;
	protected String path;
	protected PreferencePanel preferencePanel;
	
	public PreferencesMessageProcessor(DelegateBrick target, Interface preferencesInterface, String path, PreferencePanel preferencePanel){
		this.target = target;
		this.preferencesInterface = preferencesInterface;
		this.path = path;
		this.preferencePanel = preferencePanel;
	}
	
	public void setPreferencePanel(PreferencePanel newPreferencePanel, boolean readvertise){
		this.preferencePanel = newPreferencePanel;
		if(readvertise){
			sendAdvertisement();
		}
	}
	
	public DelegateBrick getBrick(){
		return target;
	}
	
	public Interface getInterface(){
		return preferencesInterface;
	}
	
	public String getPath(){
		return path;
	}
	
	//These two functions are so this component's interface can be started by the Invoker.
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
	
	//We have to handle a request from the preferences gui that is asking us,
	//asynchronously, whether we have a preferences panel.
	public void handle(Message m){
		//If the incoming message is a QueryPreferencePanelMessage, we have
		//to send our advertisement of availability.
		if(m instanceof QueryPreferencePanelMessage){
			sendAdvertisement();
			return;
		}
	}

}

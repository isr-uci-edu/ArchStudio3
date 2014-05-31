package archstudio.preferences;

import java.awt.*;
import javax.swing.*;

import archstudio.comp.preferences.IPreferences;

public abstract class PreferencePanel implements java.io.Serializable{

	protected JComponent component;
	protected transient IPreferences preferences;
	
	public PreferencePanel(){
		setComponent(null);
	}
	
	public PreferencePanel(JComponent component){
		setComponent(component);
	}
	
	public abstract void apply();
	public abstract void reset();
	
	public JComponent getComponent() {
		return component;
	}

	public void setComponent(JComponent component) {
		this.component = component;
	}

	public IPreferences getPreferences() {
		return preferences;
	}

	public void setPreferences(IPreferences preferences) {
		this.preferences = preferences;
	}

}

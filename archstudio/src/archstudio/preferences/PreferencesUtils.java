package archstudio.preferences;

import c2.fw.*;

public class PreferencesUtils {

	public static void deployPreferencesService(DelegateBrick b, Interface preferencesInterface,
	String path, PreferencePanel preferencePanel){
		PreferencesMessageProcessor pmp = new PreferencesMessageProcessor(b, preferencesInterface,
			path, preferencePanel);
		PreferencesLifecycleProcessor plp = new PreferencesLifecycleProcessor(b, preferencesInterface,
			path, preferencePanel);
		
		b.addMessageProcessor(pmp);
		b.addLifecycleProcessor(plp);	
	}

	public static void redeployPreferencesService(DelegateBrick brick, Interface preferencesInterface,
	String path, PreferencePanel preferencePanel){
		
		MessageProcessor[] mps = brick.getMessageProcessors();
		LifecycleProcessor[] lps = brick.getLifecycleProcessors();
		
		for(int i = 0; i < mps.length; i++){
			if(mps[i] instanceof PreferencesMessageProcessor){
				PreferencesMessageProcessor pmp = (PreferencesMessageProcessor)mps[i];
				if(pmp.getInterface().equals(preferencesInterface)){
					if(pmp.getPath().equals(path)){
						pmp.setPreferencePanel(preferencePanel, true);
						break;
					}
				}
			}
		}
		
		for(int i = 0; i < lps.length; i++){
			if(lps[i] instanceof PreferencesLifecycleProcessor){
				PreferencesLifecycleProcessor plp = (PreferencesLifecycleProcessor)lps[i];
				if(plp.getInterface().equals(preferencesInterface)){
					if(plp.getPath().equals(path)){
						plp.setPreferencePanel(preferencePanel);
						break;
					}
				}
			}
		}
		
		
	}
	
	public static void undeployPreferencesService(DelegateBrick brick, Interface preferencesInterface,
	String path, PreferencePanel preferencePanel){
		
		MessageProcessor[] mps = brick.getMessageProcessors();
		LifecycleProcessor[] lps = brick.getLifecycleProcessors();
		
		for(int i = 0; i < mps.length; i++){
			if(mps[i] instanceof PreferencesMessageProcessor){
				PreferencesMessageProcessor pmp = (PreferencesMessageProcessor)mps[i];
				if(pmp.getInterface().equals(preferencesInterface)){
					if(pmp.getPath().equals(path)){
						brick.removeMessageProcessor(pmp);
						break;
					}
				}
			}
		}
		
		for(int i = 0; i < lps.length; i++){
			if(lps[i] instanceof PreferencesLifecycleProcessor){
				PreferencesLifecycleProcessor plp = (PreferencesLifecycleProcessor)lps[i];
				if(plp.getInterface().equals(preferencesInterface)){
					if(plp.getPath().equals(path)){
						brick.removeLifecycleProcessor(plp);
						break;
					}
				}
			}
		}
	}

}

package edu.uci.ics.bna.swingthing;

import javax.swing.JComponent;

public class SwingThingUtils {

	private SwingThingUtils(){}

	public static void requestFocusOnDisplay(JComponent c){
		final JComponent c2 = c;
		
		c2.addAncestorListener(
			new javax.swing.event.AncestorListener(){
				public void ancestorAdded(javax.swing.event.AncestorEvent event){
					c2.requestFocus();
					c2.removeAncestorListener(this);
				}
				public void ancestorRemoved(javax.swing.event.AncestorEvent event){}
				public void ancestorMoved(javax.swing.event.AncestorEvent event){}
			}
		);
	}

}

/*
 * Created on Nov 24, 2005
 *
 */
package archstudio.comp.archipelago.security;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import edu.uci.ics.bna.logic.AbstractMainMenuLogic;
import edu.uci.ics.widgets.WidgetUtils;

public class AccessControlMainMenuLogic extends AbstractMainMenuLogic implements ActionListener{
	
	protected JMenuItem 		miAccessing;
	protected JMenuItem 		miAccessed;
	protected JMenuItem 		miCheck;
	protected JMenuItem 		miClear;
	protected JMenu 			mAccessControl;
	protected ArchSecurityTreePlugin 		archSecurityTreePlugin;
	
	public final static String	ACCESS_CONTROL = "Access Control";
	public final static String  ACCESSING_INTERFACE = "Accessing: "; 
	public final static String  ACCESSED_INTERFACE = "Accessed: ";
	
	public AccessControlMainMenuLogic(JMenuBar mainMenu, ArchSecurityTreePlugin archSecurityTreePlugin) {
		super(mainMenu);
		
		this.archSecurityTreePlugin = archSecurityTreePlugin;
		
		mAccessControl = WidgetUtils.getSubMenu(mainMenu, ACCESS_CONTROL);
		if (mAccessControl!=null) {
			getMainMenu().remove(mAccessControl);
		}

		mAccessControl = new JMenu(ACCESS_CONTROL);
		WidgetUtils.setMnemonic(mAccessControl, 'A');
		mainMenu.add(mAccessControl);
		miAccessing = new JMenuItem(ACCESSING_INTERFACE);
		miAccessed = new JMenuItem(ACCESSED_INTERFACE);
		miCheck = new JMenuItem("Check Access Control");
		miClear = new JMenuItem("Clear");
		WidgetUtils.setMnemonic(miAccessing, 'A');
		WidgetUtils.setMnemonic(miAccessed, 'S');
		WidgetUtils.setMnemonic(miCheck, 'K');
		WidgetUtils.setMnemonic(miClear, 'C');
		miAccessing.addActionListener(this);
		miAccessed.addActionListener(this);
		miClear.addActionListener(this);
		miCheck.addActionListener(this);
		miAccessing.setEnabled(false);
		miAccessed.setEnabled(false);
		miCheck.setEnabled(false);
		mAccessControl.add(miAccessing);
		mAccessControl.add(miAccessed);
		mAccessControl.add(miCheck);
		mAccessControl.add(miClear);
	}
	
	public void destroy(){
		if (miAccessing != null)
			mAccessControl.remove(miAccessing);
		if (miAccessed != null)
			mAccessControl.remove(miAccessed);
		if (miCheck != null)
			mAccessControl.remove(miCheck);
		if (miClear != null)
			mAccessControl.remove(miClear);
		if(mAccessControl.getItemCount() == 0){
			getMainMenu().remove(mAccessControl);
		}
	}	

	// These two set methods are accessed from SecurityContextMenuPlugin
	// They just handle the text and enabling of the menus 
	public void setAccessingInterface(String accessingInterface) {
		miAccessing.setText(ACCESSING_INTERFACE + accessingInterface);
		if (archSecurityTreePlugin.getAccessedInterface() != null)
			miCheck.setEnabled(true);
	}
	
	public void setAccessedInterface(String accessedInterface) {
		miAccessed.setText(ACCESSED_INTERFACE + accessedInterface);
		if (archSecurityTreePlugin.getAccessingInterface() != null)
			miCheck.setEnabled(true);
	}

	public void handleClearAccess() {
		archSecurityTreePlugin.setAccessingInterface(null);
		archSecurityTreePlugin.setAccessedInterface(null);
		miAccessing.setText(ACCESSING_INTERFACE);
		miAccessed.setText(ACCESSED_INTERFACE);
		miCheck.setEnabled(false);
	}
	
	public void actionPerformed(ActionEvent evt){
		if(evt.getSource() == miClear){
			handleClearAccess();
		}
		else if(evt.getSource() == miCheck){
			archSecurityTreePlugin.handleCheck();
		}
	}
}

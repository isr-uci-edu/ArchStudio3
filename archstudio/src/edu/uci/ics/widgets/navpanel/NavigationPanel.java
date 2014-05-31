package edu.uci.ics.widgets.navpanel;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

public class NavigationPanel extends JPanel implements ActionListener{

	protected NavigationButton backButton;
	protected NavigationButton forwardButton;

	protected Vector navigationItems;
	protected int navigationIndex = 0;
	protected int buttonSize;

	public NavigationPanel(){
		this(32);
	}

	public NavigationPanel(int buttonSize){
		super();
		this.buttonSize = buttonSize;
		navigationItems = new Vector();
		init();
	}

	public void init(){
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		backButton = new NavigationButton(NavigationButton.BACK_BUTTON);
		backButton.setMainArrowDimension(buttonSize);
		backButton.addActionListener(this);
		
		forwardButton = new NavigationButton(NavigationButton.FORWARD_BUTTON);
		forwardButton.setMainArrowDimension(buttonSize);
		forwardButton.addActionListener(this);
		
		this.add(backButton);
		this.add(forwardButton);
		
		checkButtons();
	}

	protected Vector navigationPanelListeners = new Vector();
		
	public void addNavigationPanelListener(NavigationPanelListener npl){
		navigationPanelListeners.add(npl);
	}
	
	public void removeNavigationPanelListener(NavigationPanelListener npl){
		navigationPanelListeners.remove(npl);
	}
	
	protected void fireNavigateTo(NavigationItem ni){
		synchronized(navigationPanelListeners){
			for(Iterator it = navigationPanelListeners.iterator(); it.hasNext(); ){
				((NavigationPanelListener)it.next()).navigateTo(this, ni);
			}
		}
	}

	public void actionPerformed(ActionEvent evt){
		if(evt.getSource() == backButton){
			String command = evt.getActionCommand();
			if(command.equals(NavigationButton.CLICKED_SECTION_MAIN)){
				//They clicked "back"
				if(itemExists(navigationIndex + 1)){
					navigationIndex++;
					checkButtons();
					NavigationItem newCurrentItem = getCurrentItem();
					fireNavigateTo(newCurrentItem);
				}
			}
			else if(command.equals(NavigationButton.CLICKED_SECTION_AUX)){
				//They want the back menu
				JPopupMenu popupMenu = new JPopupMenu();
				JMenuItem[] menuItems = getBackMenuItems();
				if(menuItems.length == 0){
					JMenuItem huh = new JMenuItem("[No items]");
					huh.setEnabled(false);
					popupMenu.add(huh);
				}
				else{
					for(int i = 0; i < menuItems.length; i++){
						popupMenu.add(menuItems[i]);
					}
				}
				Point popupMenuPoint = backButton.getPopupMenuPoint();
				popupMenu.show(backButton, popupMenuPoint.x, popupMenuPoint.y);
			}
		}
		else if(evt.getSource() == forwardButton){
			String command = evt.getActionCommand();
			if(command.equals(NavigationButton.CLICKED_SECTION_MAIN)){
				//They clicked "forward"
				if(itemExists(navigationIndex - 1)){
					navigationIndex--;
					checkButtons();
					NavigationItem newCurrentItem = getCurrentItem();
					fireNavigateTo(newCurrentItem);
				}
			}
			else if(command.equals(NavigationButton.CLICKED_SECTION_AUX)){
				//They want the forward menu
				JPopupMenu popupMenu = new JPopupMenu();
				JMenuItem[] menuItems = getForwardMenuItems();
				if(menuItems.length == 0){
					JMenuItem huh = new JMenuItem("[No items]");
					huh.setEnabled(false);
					popupMenu.add(huh);
				}
				else{
					for(int i = 0; i < menuItems.length; i++){
						popupMenu.add(menuItems[i]);
					}
				}
				Point popupMenuPoint = forwardButton.getPopupMenuPoint();
				popupMenu.show(forwardButton, popupMenuPoint.x, popupMenuPoint.y);
			}
		}
		else if(evt.getSource() instanceof ContextMenuItem){
			ContextMenuItem cmi = (ContextMenuItem)evt.getSource();
			NavigationItem ni = cmi.getNavigationItem();
			int newNavigationIndex = getNavigationIndexOf(ni);
			if(newNavigationIndex != -1){
				if(itemExists(newNavigationIndex)){
					navigationIndex = newNavigationIndex;
					checkButtons();
					NavigationItem newCurrentItem = getCurrentItem();
					fireNavigateTo(newCurrentItem);
				} 
			}
		}
	}
	
	protected void checkButtons(){
		if(navigationItems.size() == 0){
			backButton.setEnabled(false);
			forwardButton.setEnabled(false);
			return;
		}
		if(navigationIndex == 0){
			forwardButton.setEnabled(false);
		}
		else{
			forwardButton.setEnabled(true);
		}
		
		if(itemExists(navigationIndex + 1)){
			backButton.setEnabled(true);
		}
		else{
			backButton.setEnabled(false);
		}
	}
	
	protected boolean itemExists(int ni){
		synchronized(navigationItems){
			if(navigationItems.size() == 0){
				return false;
			}
			int vectorIndex = navigationItems.size() - ni - 1;
			if(vectorIndex < 0){
				return false;
			}
			else{
				return vectorIndex < navigationItems.size();
			}
		}
	}
	
	protected JMenuItem[] getForwardMenuItems(){
		ArrayList menuItemList = new ArrayList();
		for(int i = 0; i < 5; i++){
			int ni = navigationIndex - i - 1;
			if(itemExists(ni)){
				NavigationItem navigationItem = getItemAt(ni);
				ContextMenuItem mi = new ContextMenuItem(navigationItem);
				Icon icon = navigationItem.getIcon();
				if(icon != null){
					mi.setIcon(icon);
				}
				mi.addActionListener(this);
				menuItemList.add(mi);
			}
		}
		return (JMenuItem[])menuItemList.toArray(new JMenuItem[0]);
	}
	
	protected JMenuItem[] getBackMenuItems(){
		ArrayList menuItemList = new ArrayList();
		for(int i = 0; i < 5; i++){
			int ni = navigationIndex + i + 1;
			if(itemExists(ni)){
				NavigationItem navigationItem = getItemAt(ni);
				ContextMenuItem mi = new ContextMenuItem(navigationItem);
				Icon icon = navigationItem.getIcon();
				if(icon != null){
					mi.setIcon(icon);
				}
				mi.addActionListener(this);
				menuItemList.add(mi);
			}
		}
		return (JMenuItem[])menuItemList.toArray(new JMenuItem[0]);
	}
	
	public void addNavigationItem(NavigationItem ni){
		synchronized(navigationItems){
			//Remove everything in the 'forward' list,
			//since we're forking the path
			navigationItems.setSize(navigationItems.size() - navigationIndex);
			navigationIndex = 0;
			navigationItems.add(ni);
			checkButtons();
		}
	}

	public NavigationItem getCurrentItem(){
		return getItemAt(navigationIndex);
	}
	
	public NavigationItem getItemAt(int ni){
		synchronized(navigationItems){
			if(navigationItems.size() == 0){
				return null;
			}
			return (NavigationItem)navigationItems.elementAt(navigationItems.size() - ni - 1);
		}
	}
	
	protected int getNavigationIndexOf(NavigationItem navigationItem){
		synchronized(navigationItems){
			if(navigationItems.size() == 0){
				return -1;
			}
			int index = navigationItems.indexOf(navigationItem);
			if(index == -1){
				return -1; 
			}
			return navigationItems.size() - index - 1;
		}
	}
	
	public void clearAll(){
		navigationItems.setSize(0);
		navigationIndex = 0;
		checkButtons();
	}

	static class ContextMenuItem extends JMenuItem{
		protected NavigationItem ni;
		
		public ContextMenuItem(NavigationItem ni){
			super(ni.getDescription());
			this.ni = ni;	
		}
		
		public NavigationItem getNavigationItem(){
			return ni;
		}
	}
}

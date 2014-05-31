package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.widgets.WidgetUtils;

public class AntialiasingModeMainMenuLogic extends AbstractMainMenuLogic implements ActionListener{
	
	protected JMenu viewMenu;
	protected JCheckBoxMenuItem miAntialiasText;
	protected JCheckBoxMenuItem miAntialiasGraphics;
	protected JSeparator miSeparator;
	
	public AntialiasingModeMainMenuLogic(JMenuBar mainMenu){
		super(mainMenu);
		
		viewMenu = WidgetUtils.getSubMenu(mainMenu, "View");
		if(viewMenu == null){
			viewMenu = new JMenu("View");
			WidgetUtils.setMnemonic(viewMenu, 'V');
			mainMenu.add(viewMenu);
		}
		
		miAntialiasText = new JCheckBoxMenuItem("Antialias Text");
		miAntialiasText.addActionListener(this);
		
		miAntialiasGraphics = new JCheckBoxMenuItem("Antialias Graphics");
		miAntialiasGraphics.addActionListener(this);
		
		viewMenu.add(miAntialiasText);
		viewMenu.add(miAntialiasGraphics);
		miSeparator = new JSeparator();
		viewMenu.add(miSeparator);
	}
	
	public void destroy(){
		viewMenu.remove(miAntialiasText);
		viewMenu.remove(miAntialiasGraphics);
		viewMenu.remove(miSeparator);
		
		if(viewMenu.getItemCount() == 0){
			getMainMenu().remove(viewMenu);
		}
	}
	
	public void actionPerformed(ActionEvent e){
		boolean shouldAntialiasText = miAntialiasText.getState();
		boolean shouldAntialiasGraphics = miAntialiasGraphics.getState();
		
		BNAComponent c = getBNAComponent();
		if(c != null){
			c.setAntialiasText(shouldAntialiasText);
			c.setAntialiasGraphics(shouldAntialiasGraphics);
		}
	}
}


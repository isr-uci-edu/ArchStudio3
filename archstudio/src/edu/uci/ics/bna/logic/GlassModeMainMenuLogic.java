package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.BNAModel;
import edu.uci.ics.bna.BNAModelEvent;
import edu.uci.ics.bna.BNAUtils;
import edu.uci.ics.bna.IGlassable;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.bna.UserNotificationThing;
import edu.uci.ics.widgets.WidgetUtils;

public class GlassModeMainMenuLogic extends AbstractMainMenuLogic implements ActionListener{
	
	protected JMenu viewMenu;
	protected JCheckBoxMenuItem miGlassMode;
	protected JSeparator miSeparator;
	
	public GlassModeMainMenuLogic(JMenuBar mainMenu){
		super(mainMenu);
		
		viewMenu = WidgetUtils.getSubMenu(mainMenu, "View");
		if(viewMenu == null){
			viewMenu = new JMenu("View");
			WidgetUtils.setMnemonic(viewMenu, 'V');
			mainMenu.add(viewMenu);
		}
		
		miGlassMode = new JCheckBoxMenuItem("Glass Mode");
		miGlassMode.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.Event.CTRL_MASK));
		miGlassMode.addActionListener(this);
		
		viewMenu.add(miGlassMode);
		miSeparator = new JSeparator();
		viewMenu.add(miSeparator);
	}
	
	public void destroy(){
		clearGlassed();
		viewMenu.remove(miGlassMode);
		viewMenu.remove(miSeparator);
		if(viewMenu.getItemCount() == 0){
			getMainMenu().remove(viewMenu);
		}
	}
	
	public void clearGlassed(){
		try{
			getBNAComponent().getModel().beginBulkChange();
			if(getBNAComponent() != null){
				for(Iterator it = getBNAComponent().getModel().getThingIterator(); it.hasNext(); ){
					Thing t = (Thing)it.next();
					if(t instanceof IGlassable){
						IGlassable gt = (IGlassable)t;
						gt.setGlassed(false);
					}
				}
			}
		}finally{
			getBNAComponent().getModel().endBulkChange();
		}
	}
	
	public void bnaModelChanged(BNAModelEvent evt){
		if(evt.getEventType() == BNAModelEvent.THING_ADDED){
			Thing t = evt.getTargetThing();
			if(t instanceof IGlassable){
				boolean shouldBeGlass = miGlassMode.getState();
				((IGlassable)t).setGlassed(shouldBeGlass);
				if(shouldBeGlass){
					((IGlassable)t).setGlassedTransparency(0.50f);
				}
			}
		}
	}
	
	public void actionPerformed(ActionEvent e){
		final boolean shouldBeGlass = miGlassMode.getState();
		final BNAComponent c = getBNAComponent();
		if(c != null){
			final BNAModel m = c.getModel();
			
			Thread th = new Thread(){
				public void run(){
					if(shouldBeGlass){
						
						BNAUtils.showUserNotificationUL(c, "Glass Mode: On");
						
						try{
							c.getModel().beginBulkChange();
							for(Iterator it = m.getThingIterator(); it.hasNext(); ){
								Thing t = (Thing)it.next();
								if(t instanceof IGlassable){
									IGlassable gt = (IGlassable)t;
									gt.setGlassed(shouldBeGlass);
								}
							}
						}finally{
							c.getModel().endBulkChange();
						}
						
						for(int i = 100; i > 50; i--){
							float glassedTransparency = (float)i / 100;
							try{
								Thread.sleep(20);
							}
							catch(InterruptedException e1){}
							
							try{
								c.getModel().beginBulkChange();
								for(Iterator it = m.getThingIterator(); it.hasNext(); ){
									Thing t = (Thing)it.next();
									if(t instanceof IGlassable){
										IGlassable gt = (IGlassable)t;
										gt.setGlassedTransparency(glassedTransparency);
									}
								}
							}finally{
								c.getModel().endBulkChange();
							}
						}
					}
					else{
						BNAUtils.showUserNotificationUL(c, "Glass Mode: Off");
						
						for(int i = 50; i <= 100; i++){
							float glassedTransparency = (float)i / (float)100.0f;
							try{
								Thread.sleep(20);
							}
							catch(InterruptedException e2){}
							
							c.getModel().beginBulkChange();
							for(Iterator it = m.getThingIterator(); it.hasNext(); ){
								Thing t = (Thing)it.next();
								if(t instanceof IGlassable){
									IGlassable gt = (IGlassable)t;
									gt.setGlassedTransparency(glassedTransparency);
								}
							}
							c.getModel().endBulkChange();
						}
						c.getModel().beginBulkChange();
						for(Iterator it = m.getThingIterator(); it.hasNext(); ){
							Thing t = (Thing)it.next();
							if(t instanceof IGlassable){
								IGlassable gt = (IGlassable)t;
								gt.setGlassed(shouldBeGlass);
							}
						}
						c.getModel().endBulkChange();
					}			
				}
			};
			th.start();
		}
	}
}


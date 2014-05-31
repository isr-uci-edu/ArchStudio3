package edu.uci.ics.bna.debug;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.logic.*;
import edu.uci.ics.bna.swingthing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import edu.uci.ics.widgets.*;
import edu.uci.ics.widgets.windowheader.*;

public class TestMainMenuLogic extends AbstractMainMenuLogic implements ActionListener{
	
	public static final int NUM_TESTS = 5;
	
	protected JMenuBar mainMenu;
	protected JMenu testMenu;
	protected JMenuItem[] miTestArray;
	protected JSeparator miSeparator;
	
	public TestMainMenuLogic(JMenuBar mainMenu){
		super(mainMenu);
		this.mainMenu = mainMenu;
		
		testMenu = WidgetUtils.getSubMenu(mainMenu, "Test");
		if(testMenu == null){
			testMenu = new JMenu("Test");
			WidgetUtils.setMnemonic(testMenu, 'T');
			mainMenu.add(testMenu);
		}
		
		miTestArray = new JMenuItem[NUM_TESTS];
		for(int i = 0; i < NUM_TESTS; i++){
			miTestArray[i] = new JMenuItem("Test Item " + i);
			miTestArray[i].addActionListener(this);			
			testMenu.add(miTestArray[i]);
		}
		
		miSeparator = new JSeparator();
		testMenu.add(miSeparator);
	}
	
	public void destroy(){
		for(int i = 0; i < miTestArray.length; i++){
			testMenu.remove(miTestArray[i]);
		}
		testMenu.remove(miSeparator);
		if(testMenu.getItemCount() == 0){
			mainMenu.remove(testMenu);
		}
	}
	
	public void doTestItem0(){
		BNAComponent c = getBNAComponent();
		if(c == null){
			return;
		}
		BNAModel m = c.getModel();
		if(m == null){
			return;
		}
		
		SwingPanelThing spt = new SwingPanelThing();
		
		Thing firstThing = null;
		for(Iterator it = m.getThingIterator(); it.hasNext(); ){
			Thing t = (Thing)it.next();
			if(t instanceof edu.uci.ics.bna.BoxThing){
				firstThing = t;
				break;
			}
		}
		String firstThingId = firstThing.getID();
		//System.out.println("firstthing=" + firstThing);
		
		spt.setIndicatorThingId(firstThingId);
		JPanel p = new JPanelGR(Color.LIGHT_GRAY, Color.GRAY);
		//p.setLayout(new BorderLayout());
		p.setLayout(new BorderLayout());
		
		
		p.add("North", new WindowHeaderPanel("Window 1 Is Here"));
		//p.add("Center", new JButton("Button"));
		//p.add("Center", new WindowHeaderPanel("Window 1 Is Here"));
		
		//JPanel buttons = new JDebugPanel();
		//buttons.setLayout(new FlowLayout(FlowLayout.LEFT));
		//buttons.add(new JDebugButton("B1"));
		//buttons.add(new JDebugButton("B2"));
		//p.add("Center", buttons);
		
		spt.setPanel(p);
		m.addThing(spt);
	}

	public void actionPerformed(ActionEvent e){
		Object src = e.getSource();
		for(int i = 0; i < NUM_TESTS; i++){
			if(src == miTestArray[i]){
				Class c = getClass();
				try{
					Method m = c.getMethod("doTestItem" + i, new Class[0]);
					if(m != null){
						m.invoke(this, new Object[0]);
					}
				}
				catch(NoSuchMethodException nsme){	
					nsme.printStackTrace();
				}
				catch(Exception ge){
					ge.printStackTrace();
				}
			}
		}
	}
}


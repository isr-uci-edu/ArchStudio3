package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.BNAModel;
import edu.uci.ics.bna.IBoxBounded;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.widgets.finddialog.*;
import edu.uci.ics.widgets.WidgetUtils;

public class FindByLabelMainMenuLogic extends AbstractMainMenuLogic implements ActionListener, FindDialogListener{
	
	protected JMenu editMenu;
	protected JMenuItem miFindByLabel;
	protected JSeparator miSeparator;
	protected FindDialog fd = null;
	
	public FindByLabelMainMenuLogic(JMenuBar mainMenu){
		super(mainMenu);
		
		editMenu = WidgetUtils.getSubMenu(mainMenu, "Edit");
		if(editMenu == null){
			editMenu = new JMenu("Edit");
			WidgetUtils.setMnemonic(editMenu, 'E');
			mainMenu.add(editMenu);
		}
		
		miFindByLabel = new JMenuItem("Find by Label...");
		miFindByLabel.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.Event.CTRL_MASK));		
		miFindByLabel.addActionListener(this);
		
		editMenu.add(miFindByLabel);
		miSeparator = new JSeparator();
		editMenu.add(miSeparator);
	}
	
	public void destroy(){
		editMenu.remove(miFindByLabel);
		editMenu.remove(miSeparator);
		if(editMenu.getItemCount() == 0){
			getMainMenu().remove(editMenu);
		}
	}	
	
	public void actionPerformed(ActionEvent e){
		BNAComponent c = getBNAComponent();
		if(c != null){
			//System.out.println("fd = " + fd);
			if(fd != null){
				fd.setVisible(true);
				fd.requestFocus();
			}
			else{
				Frame f = WidgetUtils.getAncestorFrame(c);
				fd = new FindDialog(f, "Find by Label...");
				fd.setSize(300, 300);
				WidgetUtils.setWindowPosition(fd, WidgetUtils.SOUTHEAST, 5);
				fd.addFindDialogListener(this);
				fd.setVisible(true);
			}
		}
	}

	public void doFind(FindDialog fd, String text){
		BNAComponent c = getBNAComponent();
		if(c == null) return;
		fd.setSearching();
		BNAModel m = c.getModel();
		boolean found = false;
		for(Iterator it = m.getThingIterator(); it.hasNext(); ){
			Thing t = (Thing)it.next();
			Object labelObj = t.getProperty("label");
			if(labelObj != null){
				String label = labelObj.toString();
				String llc = label.toLowerCase();
				String tlc = text.toLowerCase();
				if(llc.indexOf(tlc) != -1){	 //It's a hit!
					FindResult fr = new FindResult();
					fr.label = label;
					fr.thing = t;
					fd.addResult(fr);
					found = true;
				}
			}
		}
		if(!found){
			fd.setNoResults();
		}
	}
	
	public void doGoto(FindDialog fd, Object o){
		if(o instanceof FindResult){
			Thing t = ((FindResult)o).thing;
			if(t instanceof IBoxBounded){
				Rectangle r = ((IBoxBounded)t).getBoundingBox();
				int x = r.x + (r.width / 2);
				int y = r.y + (r.height / 2);
				FlyToLogic.flyTo(getBNAComponent(), x, y); 
			}
		}
	}
	
	public void isClosing(FindDialog fd){
		this.fd = null;
	}

	static class FindResult{
		public String label;
		public Thing thing;
		
		public String toString(){
			return label;
		}
	}
}

package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.BNAModel;
import edu.uci.ics.bna.SplineThing;
import edu.uci.ics.bna.Thing;
import edu.uci.ics.widgets.WidgetUtils;

public class ResetSplineMainMenuLogic extends AbstractMainMenuLogic implements ActionListener{
	
	protected JMenu editMenu;
	protected JMenu mSplineReset;
	protected JMenuItem miRectilinear;
	protected JSeparator miSeparator;
	protected JMenuItem miBSpline;
	
	public ResetSplineMainMenuLogic(JMenuBar mainMenu){
		super(mainMenu);
		
		editMenu = WidgetUtils.getSubMenu(mainMenu, "Edit");
		if(editMenu == null){
			editMenu = new JMenu("Edit");
			WidgetUtils.setMnemonic(editMenu, 'E');
			mainMenu.add(editMenu);
		}
		
		mSplineReset = new JMenu("Reset all Splines");

		miRectilinear = new JMenuItem("Rectilinear");
		miRectilinear.addActionListener(this);
		
		miBSpline = new JMenuItem("B-Spline Curves");
		miBSpline.addActionListener(this);
		
		mSplineReset.add(miRectilinear);
		mSplineReset.add(miBSpline);
		
		editMenu.add(mSplineReset);
		miSeparator = new JSeparator();
		editMenu.add(miSeparator);
	}
	
	public void destroy(){
		mSplineReset.remove(miRectilinear);
		mSplineReset.remove(miBSpline);
		
		if(mSplineReset.getItemCount() == 0){
			editMenu.remove(mSplineReset);
		}
		
		editMenu.remove(miSeparator);

		if(editMenu.getItemCount() == 0){
			getMainMenu().remove(editMenu);
		}
	}
	
	public void actionPerformed(ActionEvent e){
		int splineMode = SplineThing.SPLINE_MODE_RECTILINEAR;
		
		if(e.getSource() == miRectilinear){
			splineMode = SplineThing.SPLINE_MODE_RECTILINEAR;
		}
		else if(e.getSource() == miBSpline){
			splineMode = SplineThing.SPLINE_MODE_BSPLINE;
		}
		
		final BNAComponent c = getBNAComponent();
		if(c != null){
			final BNAModel m = c.getModel();
			
			for(Iterator it = m.getThingIterator(); it.hasNext(); ){
				Thing t = (Thing)it.next();
				if(t instanceof SplineThing){
					SplineThing rs = (SplineThing)t;
					rs.setSplineMode(splineMode);
				}
			}
		}
	}
}


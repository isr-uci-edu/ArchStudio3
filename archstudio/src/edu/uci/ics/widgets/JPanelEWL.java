package edu.uci.ics.widgets;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class JPanelEWL extends JPanel implements java.io.Serializable{
	
	private int lgap = 0;
	private JComponent comp;
	
	public static void main(String[] args){
		JPanelEWL pwl = new JPanelEWL(new JLabel("<HTML><B>In a New York Minute Everything can change.</B></HTML>"));
		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout());
		p2.add("West", new JLabel("Foo is the word!"));
		p2.add("Center", pwl);
		
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add("Center", new JScrollPane(p2));
		frame.setSize(400, 300);
		frame.setVisible(true);
		frame.validate();
	}
	
	
	public JPanelEWL(JComponent comp){
		this.comp = comp;
		//this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setLayout(new BorderLayout());
		//this.addComponentListener(new CA());
		this.add("Center", comp);
		//this.add(BoxThing.createHorizontalGlue());
	}

	public JPanelEWL(JComponent comp, int lgap){
		this(comp);
		this.lgap = lgap;
	}
	
	public Dimension getPreferredSize(){
		JScrollPane parentPane = null;
		Component c = this;
		int x = getX();
		while(true){
			c = c.getParent();
			if(c == null){
				return comp.getPreferredSize();
			}
			if(c instanceof JComponent){
				((JComponent)c).revalidate();
			}
			if(c instanceof Component){
				x += c.getX();
			}
			if(c instanceof JScrollPane){
				int width = (int)((JScrollPane)c).getViewportBorderBounds().getWidth() - x - 15;
				width -= lgap;
				if(width < 20) width = comp.getSize().width;
				//return new Dimension(width,	comp.getPreferredSize().height);
				Dimension d = new Dimension(width,	comp.getPreferredSize().height);
				//System.out.println("pref size = " + d);
				return d;
			}
		}
	}
	
	/*
	public Dimension getMinimumSize(){
		Dimension ms = new Dimension(label.getMinimumSize());
		return ms;
	}
	*/
	
	public Dimension getMaximumSize(){
		//return getPreferredSize();
		return comp.getMaximumSize();
	}
	

}
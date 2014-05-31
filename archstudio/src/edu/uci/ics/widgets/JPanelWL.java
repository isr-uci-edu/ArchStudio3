package edu.uci.ics.widgets;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.*;

public class JPanelWL extends JPanel implements java.io.Serializable{
	
	private int lgap = 0;
	private JComponent label;
	private Container container;
	
	public JPanelWL(JComponent label, Container container){
		this.label = label;
		this.container = container;
		
		final JComponent flabel = label;
		container.addComponentListener(new ComponentAdapter(){
			public void componentResized(ComponentEvent e){
				flabel.invalidate();
				//flabel.validate();
			}
		});
		
		//this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setLayout(new BorderLayout());
		this.add("Center", label);
	}

	public JPanelWL(JComponent label, Container container, int lgap){
		this(label, container);
		this.lgap = lgap;
	}
	
	public Dimension getPreferredSize(){
		Container parentPane = null;
		Component c = this;
		int x = getX();
		while(true){
			c = c.getParent();
			if(c == null){
				return label.getPreferredSize();
			}
			if(c instanceof Component){
				//x += c.getX();
				x += c.getBounds().x;
			}
			if(c.equals(container)){
				Dimension lps = label.getPreferredSize();
				int width = (int)((Container)c).getBounds().getWidth() - x - 15;
				width -= lgap;
				if(width < 20) width = label.getSize().width;

				if(width > lps.width) width = lps.width;
				//return new Dimension(width,	comp.getPreferredSize().height);
				Dimension d = new Dimension(width,	lps.height);
				//System.out.println("pref size = " + d);
				return d;
			}
		}
	}

/*
	public Dimension getMinimumSize(){
		return label.getMinimumSize();
	}
*/
	
	public Dimension getMaximumSize(){
		//return getPreferredSize();
		return label.getMaximumSize();
	}
	
	

}

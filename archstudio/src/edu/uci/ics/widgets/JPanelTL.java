package edu.uci.ics.widgets;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class JPanelTL extends JPanel implements java.io.Serializable{
	
	public JPanelTL(Component componentToWrap, String title, int lrInset){
		super();
		this.setLayout(new BorderLayout());
		this.setBorder(new TitledBorder(title));
		this.add("Center", new JPanelIS(componentToWrap, new Insets(0, lrInset, 0, lrInset)));
	}
	
	public JPanelTL(Component componentToWrap, String title, Insets insets){
		super();
		this.setLayout(new BorderLayout());
		this.setBorder(new TitledBorder(title));
		this.add("Center", new JPanelIS(componentToWrap, insets));
	}
	

}

package edu.uci.ics.widgets;

import java.awt.*;
import javax.swing.*;

public class JPanelIS extends JPanel implements java.io.Serializable{
	
	private Insets myInsets;
	
	public JPanelIS(Component componentToWrap, int uniformInset){
		super();
		this.myInsets = new Insets(uniformInset, uniformInset, uniformInset, uniformInset);
		this.setLayout(new BorderLayout());
		this.add("Center", componentToWrap);
	}
	
	public JPanelIS(Component componentToWrap, Insets insets){
		super();
		this.myInsets = insets;
		this.setLayout(new BorderLayout());
		this.add("Center", componentToWrap);
	}
	
	public Insets getInsets(){
		return myInsets;
	}

}

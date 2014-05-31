package edu.uci.ics.widgets;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Panel that wraps and "squishes" a Component so that it will
 * be in the upper-left corner of the layout instead of the center,
 * and will not "spread out" to fill any given area, but remain
 * relatively compact.
 * @author Eric M. Dashofy
 */
public class JPanelUL extends JPanel implements java.io.Serializable{
	
	private Component componentToWrap;
	
	public JPanelUL(Component componentToWrap){
		this.componentToWrap = componentToWrap;
		this.setLayout(new BorderLayout());
		JPanel internalPanel = new JPanel();
		internalPanel.setLayout(new BorderLayout());
		internalPanel.add("West", componentToWrap);
		this.add("North", internalPanel);
	}

	public JPanelUL(Component componentToWrap, String vertSquashDir, String horizSquashDir){
		this.componentToWrap = componentToWrap;
		this.setLayout(new BorderLayout());
		JPanel internalPanel = new JPanel();
		internalPanel.setLayout(new BorderLayout());
		internalPanel.add(horizSquashDir, componentToWrap);
		this.add(vertSquashDir, internalPanel);
	}
}

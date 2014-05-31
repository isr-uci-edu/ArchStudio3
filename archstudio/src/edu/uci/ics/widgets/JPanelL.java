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
public class JPanelL extends JPanel implements java.io.Serializable{
	
	private Component componentToWrap;
	
	public JPanelL(Component componentToWrap){
		this.componentToWrap = componentToWrap;
		this.setLayout(new BorderLayout());
		this.add("West", componentToWrap);
	}

}

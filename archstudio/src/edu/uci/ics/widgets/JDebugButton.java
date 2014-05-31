package edu.uci.ics.widgets;

import java.awt.*;
import javax.swing.*;

public class JDebugButton extends JButton{

	public JDebugButton() {
		super();
	}

	public JDebugButton(String title){
		super(title);
	}
	
	public void paint(Graphics g){
		System.out.println("Button[" + this.toString() + "]::paint()");
		super.paint(g);
	}
}

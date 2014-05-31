package edu.uci.ics.widgets;

import java.awt.*;
import javax.swing.*;

public class JDebugPanel extends JPanel{

	public JDebugPanel() {
		super();
	}

	public void paint(Graphics g){
		System.out.println("Panel[" + this.toString() + "]::paint()");
		super.paint(g);
	}
	
	public void paintComponent(Graphics g){
		System.out.println("Panel[" + this.toString() + "]::paintComponent()");
		super.paintComponent(g);
	}
	
	public void paintChildren(Graphics g){
		System.out.println("Panel[" + this.toString() + "]::paintChildren()");
		super.paintChildren(g);
	}
}

package edu.uci.ics.widgets;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class CompositeIcon implements Icon{

	protected Icon[] elementIcons;
	
	public CompositeIcon(Icon topIcon, Icon bottomIcon){
		elementIcons = new Icon[2];
		elementIcons[0] = topIcon;
		elementIcons[1] = bottomIcon;
	}
	
	public CompositeIcon(Icon[] elementIcons){
		this.elementIcons = elementIcons;
	}
	
	public int getIconHeight(){
		if(elementIcons.length > 0){
			return elementIcons[0].getIconHeight();
		}
		else{
			return 0;
		}
	}
	
	public int getIconWidth(){
		if(elementIcons.length > 0){
			return elementIcons[0].getIconWidth();
		}
		else{
			return 0;
		}
	}
	
	public void paintIcon(Component c, Graphics g, int x, int y){
		for(int i = elementIcons.length - 1; i >= 0; i--){
			elementIcons[i].paintIcon(c, g, x, y);
		}
	}
}

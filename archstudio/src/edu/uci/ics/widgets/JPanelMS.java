package edu.uci.ics.widgets;

import java.awt.*;
import javax.swing.*;

public class JPanelMS extends JPanel implements java.io.Serializable{
	int mw = -1;
	int mh = -1;
	
	int maxw = -1;
	int maxh = -1;
	
	Insets newInsets = null;
	
	public JPanelMS(){
		super();
	}
	
	public JPanelMS(Insets newInsets){
		super();
		this.newInsets = newInsets;
	}
	
	public Insets getInsets(){
		if(newInsets == null){
			return super.getInsets();
		}
		else{
			return newInsets;
		}
	}
	
	public void setMaximumHeight(int maxh){
		this.maxh = maxh;
	}
	
	public void setMaximumWidth(int maxw){
		this.maxw = maxw;
	}
	
	public void setMinimumHeight(int mh){
		this.mh = mh;
	}
	
	public void setMinimumWidth(int mw){
		this.mw = mw;
	}
	
	public Dimension getMinimumSize(){
		Dimension d = super.getMinimumSize();
		if(mw != -1){
			if(mw > d.width){
				d.width = mw;
			}
		}
		if(mh != -1){
			if(mh > d.height){
				d.height = mh;
			}
		}
		/*
		Insets in = getInsets();
		if(in != null){
			d.width += in.left + in.right;
			d.height += in.top + in.bottom;
		}
		*/
		
		return d;
	}

	public Dimension getPreferredSize(){
		Dimension d = super.getPreferredSize();
		if(mw != -1){
			if(mw > d.width){
				d.width = mw;
			}
		}
		if(maxw != -1){
			if(d.width > maxw){
				d.width = maxw;
			}
		}
		if(mh != -1){
			if(mh > d.height){
				d.height = mh;
			}
		}
		if(maxh != -1){
			if(d.height > maxh){
				d.height = maxh;
			}
		}
		
		Insets in = getInsets();
		if(in != null){
			d.width += in.left + in.right;
			d.height += in.top + in.bottom;
		}
		
		return d;
	}

	public Dimension getMaximumSize(){
		Dimension d = super.getMaximumSize();
		if(maxw != -1){
			if(maxw < d.width){
				d.width = maxw;
			}
		}
		if(maxh != -1){
			if(maxh < d.height){
				d.height = maxh;
			}
		}
		
		/*
		Insets in = getInsets();
		if(in != null){
			d.width += in.left + in.right;
			d.height += in.top + in.bottom;
		}
		*/
		
		return d;
	}
}



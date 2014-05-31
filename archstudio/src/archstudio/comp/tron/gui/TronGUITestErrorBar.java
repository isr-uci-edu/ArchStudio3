package archstudio.comp.tron.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import edu.uci.ics.widgets.WidgetUtils;

public class TronGUITestErrorBar extends JButton{

	protected boolean visible;
	private int height = 0;

	public TronGUITestErrorBar(ActionListener viewConsoleActionListener){
		super("Testing errors occurred. Click here for the console.", 
			edu.uci.ics.xadlutils.Resources.ERROR_ICON_16);
		this.addActionListener(viewConsoleActionListener);
		this.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				hide();
			}
		});
	}
	
	public Dimension getPreferredSize(){
		Dimension d = super.getPreferredSize();
		d.height = height;
		return d;
	}
	
	public synchronized void hide(){
		Frame ancestorFrame = WidgetUtils.getAncestorFrame(this);
		height = 0;
		invalidate();
		ancestorFrame.validate();
	}
	
	public synchronized void popup(){
		final int psheight = super.getPreferredSize().height;
		final Frame ancestorFrame = WidgetUtils.getAncestorFrame(this);
		Runnable r = new Runnable(){
			public void run(){
				for(int i = 0; i < psheight; i+=2){
					height = i;
					invalidate();
					ancestorFrame.validate();
					//ancestorFrame.repaint();
					try{
						Thread.sleep(20);
					}
					catch(InterruptedException ie){}
				}
			}
		};
		Thread th = new Thread(r);
		th.start();
	}
	
}

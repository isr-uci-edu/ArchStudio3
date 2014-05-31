package edu.uci.ics.widgets.windowheader;

import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.*;

public class WindowHeaderPanel extends JPanel implements ActionListener{

	//protected String title;
	protected JLabel titleLabel;
	protected CloseButton closeButton;

	public WindowHeaderPanel(String title){
		this(title, true);
	}
	
	public WindowHeaderPanel(String title, boolean closeable){
		super();
		titleLabel = new JLabel();
		
		this.setLayout(new BorderLayout());
		
		JPanel labelPanel = new JPanel();
		labelPanel.setOpaque(false);
		labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		labelPanel.add(titleLabel);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setOpaque(false);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		closeButton = new CloseButton();
		closeButton.addActionListener(this);
		if(closeable)
			buttonPanel.add(closeButton);
		
		this.add("Center", labelPanel);
		this.add("East", buttonPanel);
		
		//this.setDebugGraphicsOptions(DebugGraphics.LOG_OPTION);
		
		setTitle(title);
	}
	
	public void paintComponent(Graphics g){
		Graphics2D g2d = (Graphics2D)g;
		Rectangle bounds = getVisibleRect();
		bounds.width--;
		bounds.height--;
		
		Paint oldPaint = g2d.getPaint();
		
		g2d.setPaint(new GradientPaint(bounds.x, bounds.y, Color.LIGHT_GRAY.brighter(), bounds.x, bounds.y + bounds.height, Color.GRAY));
		g2d.fill(bounds);
		
		g2d.setColor(Color.BLACK);
		//g2d.setPaint(Color.BLACK);
		//g2d.draw(bounds);
		g2d.drawLine(bounds.x, bounds.y + bounds.height, bounds.x + bounds.width, bounds.y + bounds.height);
		g2d.setPaint(oldPaint);
	}
	
	
	public void setTitle(String title){
		titleLabel.setText(title);
	}
	
	public String getTitle(){
		return titleLabel.getText();
	}

	private Vector windowHeaderPanelListeners = new Vector();
	
	public void addWindowHeaderPanelListener(WindowHeaderPanelListener l){
		windowHeaderPanelListeners.addElement(l);
	}
	
	public void removeWindowHeaderPanelListener(WindowHeaderPanelListener l){
		windowHeaderPanelListeners.removeElement(l);
	}
	
	protected void fireCloseButtonPressedEvent(){
		synchronized(windowHeaderPanelListeners){
			for(Iterator it = windowHeaderPanelListeners.iterator(); it.hasNext(); ){
				((WindowHeaderPanelListener)it.next()).closeButtonPressed(this);
			}
		}
	}
	
	public void actionPerformed(ActionEvent evt){
		if(evt.getSource() == closeButton){
			fireCloseButtonPressedEvent();
		}
	}
}

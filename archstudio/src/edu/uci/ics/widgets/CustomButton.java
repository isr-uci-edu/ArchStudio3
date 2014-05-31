package edu.uci.ics.widgets;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.util.Vector;
import java.util.Iterator;

public abstract class CustomButton extends JPanel{

	protected boolean hasKeyboardFocus = false;
	protected boolean mouseOver = false;
	protected boolean mouseDown = false;

	public CustomButton() {
		super();
		this.setFocusable(true);
		this.setFocusTraversalKeysEnabled(true);
		addAdapters();
	}
	
	protected void addAdapters(){
		this.addMouseListener(new CustomButtonMouseAdapter());
		this.addFocusListener(new CustomButtonFocusAdapter());
		this.addKeyListener(new CustomButtonKeyAdapter());
	}

	public void paintComponent(Graphics g){
		super.paintComponent(g);
		if(mouseDown){
			paintPushedIn(g);
		}
		else{
			if(!mouseOver){
				paintPushedOut(g);
			}
			else{
				paintLightened(g);
			}
		}
	}
	
	public Dimension getPreferredSize(){
		return new Dimension(15, 15);
	}
	
	public void doClick(){
		mouseDown = true;
		invalidate();
		repaint();
		try{
			Thread.sleep(100);
		}
		catch(InterruptedException ie){}
		mouseDown = false;
		invalidate();
		repaint();

		ActionEvent evt = new ActionEvent(CustomButton.this, ActionEvent.ACTION_FIRST, "clicked");
		fireActionEvent(evt);		
	}
	
	public void paintPushedOut(Graphics g){
		//super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		Rectangle bounds = getVisibleRect();
		bounds.width--;
		bounds.height--;
		
		if(!hasKeyboardFocus){
			g2d.setColor(Color.BLACK);
		}
		else{
			g2d.setColor(Color.LIGHT_GRAY);
		}
		g2d.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
		g2d.setColor(Color.GRAY);
		g2d.fill3DRect(bounds.x+1, bounds.y+1, bounds.width-1, bounds.height-1, true);
		
		Paint oldPaint = g2d.getPaint();
		g2d.setPaint(new GradientPaint(bounds.x+1, bounds.y+1, Color.LIGHT_GRAY, bounds.x+1+bounds.width-2, bounds.y+1+bounds.height-2, Color.DARK_GRAY));
		
		Rectangle internalBounds = new Rectangle(bounds.x+2, bounds.y+2, bounds.width-3, bounds.height-3);
		g2d.fill(internalBounds);
		g2d.setPaint(oldPaint);
		
		g2d.setColor(Color.BLACK);
		drawIcon(g);
	}
	
	public abstract void drawIcon(Graphics g);
	
	public void paintLightened(Graphics g){
		//super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		Rectangle bounds = getVisibleRect();
		bounds.width--;
		bounds.height--;
		
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
		g2d.setColor(Color.GRAY);
		g2d.fill3DRect(bounds.x+1, bounds.y+1, bounds.width-1, bounds.height-1, true);
		
		Paint oldPaint = g2d.getPaint();
		g2d.setPaint(new GradientPaint(bounds.x+1, bounds.y+1, Color.LIGHT_GRAY, bounds.x+1+bounds.width-2, bounds.y+1+bounds.height-2, Color.DARK_GRAY));
		
		Rectangle internalBounds = new Rectangle(bounds.x+2, bounds.y+2, bounds.width-3, bounds.height-3);
		g2d.fill(internalBounds);
		g2d.setPaint(oldPaint);
		
		g2d.setColor(Color.LIGHT_GRAY);
		drawIcon(g);
	}
	
	public boolean hasKeyboardFocus(){
		return hasKeyboardFocus;
	}
	
	public void paintPushedIn(Graphics g){
		//super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		Rectangle bounds = getVisibleRect();
		bounds.width--;
		bounds.height--;
		
		if(!hasKeyboardFocus){
			g2d.setColor(Color.BLACK);
		}
		else{
			g2d.setColor(Color.LIGHT_GRAY);
		}
		g2d.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
		g2d.setColor(Color.GRAY);
		g2d.fill3DRect(bounds.x+1, bounds.y+1, bounds.width-1, bounds.height-1, false);
		
		Paint oldPaint = g2d.getPaint();
		g2d.setPaint(new GradientPaint(bounds.x+1, bounds.y+1, Color.DARK_GRAY, bounds.x+1+bounds.width-2, bounds.y+1+bounds.height-2, Color.LIGHT_GRAY));		
		Rectangle internalBounds = new Rectangle(bounds.x+2, bounds.y+2, bounds.width-3, bounds.height-3);
		g2d.fill(internalBounds);
		g2d.setPaint(oldPaint);
		
		g2d.setColor(Color.BLACK);
		drawIcon(g);
	}
	
	protected Vector actionListeners = new Vector();
	
	public void addActionListener(ActionListener l){
		synchronized(actionListeners){
			actionListeners.addElement(l);
		}
	}
	
	public void removeActionListener(ActionListener l){
		synchronized(actionListeners){
			actionListeners.removeElement(l);
		}
	}
	
	protected void fireActionEvent(ActionEvent evt){
		synchronized(actionListeners){
			for(Iterator it = actionListeners.iterator(); it.hasNext(); ){
				((ActionListener)it.next()).actionPerformed(evt);
			}
		}
	}
	
	protected class CustomButtonFocusAdapter extends FocusAdapter{
		public CustomButtonFocusAdapter(){
			super();
		}

		public void focusGained(FocusEvent evt){
			hasKeyboardFocus = true;
			invalidate();
			repaint();
		}
		
		public void focusLost(FocusEvent evt){
			hasKeyboardFocus = false;
			invalidate();
			repaint();
		}
	}
	
	protected class CustomButtonMouseAdapter extends MouseAdapter{
		public CustomButtonMouseAdapter(){
			super();
		}
		
		public void mouseEntered(MouseEvent me){
			mouseOver = true;
			invalidate();
			repaint();
		}
		
		public void mouseExited(MouseEvent me){
			mouseOver = false;
			invalidate();
			repaint();
		}

		public void mousePressed(MouseEvent me){
			mouseDown = true;
			invalidate();
			repaint();
		}
		
		public void mouseReleased(MouseEvent me){
			mouseDown = false;
			invalidate();
			repaint();
		}
		
		public void mouseClicked(MouseEvent me){
			//System.out.println("mouseClicked!");
			ActionEvent evt = new ActionEvent(CustomButton.this, ActionEvent.ACTION_FIRST, "clicked");
			fireActionEvent(evt);
		}
	}
	
	protected class CustomButtonKeyAdapter extends KeyAdapter{
		public CustomButtonKeyAdapter(){
			super();
		}

		public void keyPressed(KeyEvent ke){
			int key = ke.getKeyCode();
			if(key == KeyEvent.VK_SPACE){
				if(hasKeyboardFocus){
					mouseDown = true;
					invalidate();
					repaint();
				}
			}
		}
		
		public void keyReleased(KeyEvent ke){
			int key = ke.getKeyCode();
			if(key == KeyEvent.VK_SPACE){
				if(hasKeyboardFocus){
					mouseDown = false;
					invalidate();
					repaint();
					ActionEvent evt = new ActionEvent(CustomButton.this, ActionEvent.ACTION_FIRST, "clicked");
					fireActionEvent(evt);
				}
			}
		}

	}
	
}

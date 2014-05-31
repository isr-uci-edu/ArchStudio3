package archstudio.comp.archipelago;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class ArchipelagoStatusBar extends JPanel{

	public static final int STATUS_BAR_HEIGHT = 20;
	public static final Color PROGRESS_BAR_COLOR = new Color(143, 159, 178);
	
	private int min = 0;
	private int max = 100;
	private int val = 0;
	private boolean indeterminate = false;
	private String text = null;
	
	public ArchipelagoStatusBar(){
		super();
		this.setLayout(new BorderLayout());
		this.setBorder(new BevelBorder(BevelBorder.LOWERED));
	}
	
	public void reset(){
		setText(null);
		setMinimum(0);
		setMaximum(100);
		setValue(0);
		setIndeterminate(false);
		repaint();
	}
	
	public Insets getInsets(){
		return new Insets(0, 3, 0, 3);
	}
	
	public Dimension getMinimumSize(){
		Dimension ms = super.getMinimumSize();
		ms.height = STATUS_BAR_HEIGHT;
		return ms;
	}
	
	public Dimension getPreferredSize(){
		Dimension ps = super.getPreferredSize();
		ps.height = STATUS_BAR_HEIGHT;
		return ps;
	}
	
	public void setText(String text){
		this.text = text;
		repaint();
	}
	
	public String getText(){
		return text;
	}
	
	public void setComponent(JComponent component){
		this.removeAll();
		this.add("Center", component);
		validate();
		repaint();
	}
	
	public void setValue(int val){
		this.val = val;
		repaint();
	}
	
	public int getValue(){
		return val;
	}
	
	public void setMinimum(int min){
		this.min = min;
		repaint();
	}
	
	public int getMinimum(){
		return min;
	}
	
	public void setMaximum(int max){
		this.max = max;
		repaint();
	}
	
	public int getMaximum(){
		return max;
	}
	
	public void setIndeterminate(boolean indeterminate){
		this.indeterminate = indeterminate;
		repaint();
	}
	
	public boolean isIndeterminate(){
		return indeterminate;
	}

	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g.create();
		g2d.setColor(PROGRESS_BAR_COLOR);
		if(isIndeterminate()){
			Graphics2D g2d2 = (Graphics2D)g2d.create();
			g2d2.setStroke(new BasicStroke(4.0f));
			for(int i = -20; i < getWidth(); i += 10){
				g2d2.drawLine(i, 0, i + STATUS_BAR_HEIGHT, STATUS_BAR_HEIGHT);
			}
		}
		else{
			if(val >= min){
				if((max - min) < 1){
					return;
				}
				double dpct = (double)(val - min) / (double)(max - min);
				int drawWidth = (int)((double)getWidth() * (double)dpct);
				g2d.fillRect(0, 0, drawWidth, getHeight());
			}
		}
		if(getText() != null){
			Graphics2D g2d2 = (Graphics2D)g2d.create();
			g2d2.setPaint(Color.BLACK);
			Font f = new Font("Dialog", Font.BOLD, 12);
			g2d2.setFont(f);
			FontMetrics fm = g2d2.getFontMetrics(f);
			g2d2.setXORMode(PROGRESS_BAR_COLOR);
			g2d2.drawString(getText(), 5, getHeight() - fm.getDescent() - 2);
		}
	}
	
}

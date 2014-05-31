package edu.uci.ics.widgets;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class ProgressDialog extends JDialog implements ChangeListener{

	protected Frame parent;
	protected JProgressBar pb;
	protected JLabel label;
	
	public ProgressDialog(Frame parent, String title, String initialMessage){
		super(parent, title);
		this.parent = parent;
		this.label = new JLabel(initialMessage);
		this.pb = new JProgressBar(){
			public Dimension getPreferredSize(){
				return new Dimension(150, super.getPreferredSize().height);
			}
		};
		this.pb.setIndeterminate(false);
		pb.addChangeListener(this);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(new JPanelUL(label));
		JPanelMS pbPanel = new JPanelMS();
		//pbPanel.setMinimumWidth(160);
		//pbPanel.setMaximumWidth(160);
		pbPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		pbPanel.add(pb);
		mainPanel.add(new JPanelUL(pbPanel));
		
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add("Center", new JPanelIS(new JPanelUL(mainPanel), 5));
		
		this.setSize(200, 100);
	}
	
	public void stateChanged(ChangeEvent e){
		this.paint(this.getGraphics());
		this.validate();
	}

	public void doPopup(){
		Rectangle parentBounds = parent.getBounds();
		int cx = parentBounds.x + (parentBounds.width / 2);
		int cy = parentBounds.y + (parentBounds.height / 2);
		
		int x = cx - 100;
		int y = cy - 50;
		
		this.setLocation(x, y);
		this.setVisible(true);
		this.validate();
		this.repaint();
	}
	
	public JProgressBar getProgressBar(){
		return pb;
	}
	
	public void doDone(){
		pb.removeChangeListener(this);
		this.setVisible(false);
		this.dispose();
		parent.requestFocus();
	}

}

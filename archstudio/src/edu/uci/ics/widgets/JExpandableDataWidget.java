package edu.uci.ics.widgets;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class JExpandableDataWidget extends JPanel implements ActionListener, java.io.Serializable{

	protected ImageIcon ICON_ARROW_RIGHT = null;
	protected ImageIcon ICON_ARROW_DOWN = null;
	
	protected JPanel leftPanel;
	protected JPanel rightPanel;
	protected JButton arrowButton;
	protected boolean expanded = false;
	
	protected Component headline;
	protected Component body;
	
	public static void main(String[] args){
		JExpandableDataWidget t = new JExpandableDataWidget(
			new JLabel("<html>In a New York Minute</html>"),
			new JPanelEWL(new JLabel("<html>Everything can change.  Everything can change.  Everything can change.</html>"))
		);
		
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add("Center", new JScrollPane(t));
		frame.setSize(400, 300);
		frame.setVisible(true);
	}
	
	public JExpandableDataWidget(Component headline, Component body){
		super();
		//this.addComponentListener(new CA());
		this.headline = headline;
		this.body = body;
		
		ICON_ARROW_RIGHT = WidgetUtils.getImageIcon("res/arrowright.gif");
		ICON_ARROW_DOWN = WidgetUtils.getImageIcon("res/arrowdown.gif");
	
		leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		arrowButton = new JButton(ICON_ARROW_RIGHT);
		arrowButton.setBorderPainted(false);
		arrowButton.setContentAreaFilled(false);
		arrowButton.setMargin(new Insets(0,0,0,3));
		arrowButton.addActionListener(this);
		leftPanel.add("North", arrowButton);
		
		rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add("North", headline);
		
		this.setLayout(new BorderLayout());
		this.add("West", leftPanel);
		this.add("Center", rightPanel);
		
	}
	
	class CA extends ComponentAdapter{
		public void componentResized(ComponentEvent evt){
			revalidate();
		}
	}
	
	public void doExpand(){
		arrowButton.setIcon(ICON_ARROW_DOWN);
		rightPanel.add("Center", body);
		expanded = true;
		this.revalidate();
		//validate();
		repaint();
	}
	
	public void doCollapse(){
		arrowButton.setIcon(ICON_ARROW_RIGHT);
		rightPanel.remove(body);
		expanded = false;
		this.revalidate();
		//validate();
		repaint();
	}
	
	public void actionPerformed(ActionEvent evt){
		if(expanded){
			doCollapse();
		}
		else{
			doExpand();
		}
		
	}
	
	
	
	
	

}

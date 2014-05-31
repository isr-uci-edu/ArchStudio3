package edu.uci.ics.widgets;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public class JCheckboxTree extends JTree{

	public static void main(String[] args){
		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BorderLayout());
		
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Hello!");
		DefaultMutableTreeNode child1 = new DefaultMutableTreeNode("Child1");
		DefaultMutableTreeNode child2 = new DefaultMutableTreeNode("Child1");
		
		DefaultTreeModel m = new DefaultTreeModel(rootNode);
		rootNode.add(child1);
		rootNode.add(child2);
		
		JCheckboxTree tree = new JCheckboxTree();
		tree.setModel(m);
		tree.setEditable(false);
		tree.addMouseListener(tree.new TreeMouseAdapter());
		f.getContentPane().add("Center", tree);
		f.setSize(500, 400);
		f.setLocation(100, 100);
		f.setVisible(true);
	}

	public JCheckboxTree() {
		super();
		this.setCellRenderer(new JCheckboxTreeCellRenderer());
	}

	class JCheckboxTreeCellRenderer implements TreeCellRenderer{
		public Component getTreeCellRendererComponent(
			JTree arg0,
			Object arg1,
			boolean arg2,
			boolean arg3,
			boolean arg4,
			int arg5,
			boolean arg6) {
			String s = arg1.toString();
			JPanel p = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.LEFT));
			p.add(new JLabel(s));
			JCheckBox cb1 = new JCheckBox();
			JCheckBox cb2 = new JCheckBox();
			p.add(cb1);
			p.add(cb2);
			return p;
		}

	}
	
	class TreeMouseAdapter extends MouseAdapter{
		public void mouseClicked(MouseEvent arg0){
			Component[] components = getComponents();
			for(int i = 0; i < components.length; i++){
				components[i].dispatchEvent(arg0);
			}
		}

		public void mousePressed(MouseEvent arg0) {
			Component[] components = getComponents();
			for(int i = 0; i < components.length; i++){
				components[i].dispatchEvent(arg0);
			}
		}

		public void mouseReleased(MouseEvent arg0) {
			Component[] components = getComponents();
			for(int i = 0; i < components.length; i++){
				components[i].dispatchEvent(arg0);
			}
		}


	}
}

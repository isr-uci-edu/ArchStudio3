package edu.uci.ics.widgets;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

//Two column table for a list of name-value pairs.  The left column
//will adjust its width to fit the components so the right column
//is all nice and left-justified.
public class JStaticTable extends JPanel implements java.io.Serializable, TableModelListener{
	
	//private JPanel gridPanel;
	
	private boolean drawRowSplits = false;
	private int hgap = 0;
	private int vgap = 0;
	
	//Must be of Components
	private TableModel model;
	private java.awt.Component emptyView = null;
	
	public static void main(String[] args){
		DefaultTableModel tm = new DefaultTableModel();
		tm.addColumn("Column1");
		tm.addColumn("Column2");
		
		JStaticTable t = new JStaticTable(tm, 5, 5);
		t.setDrawRowSplits(false);
		
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add("Center", new JScrollPane(new JPanelIS(new JPanelUL(t), new Insets(5, 5, 5, 5))));
		frame.setSize(400, 300);
		frame.setVisible(true);
		
		try{
			Thread.sleep(2500);
		}
		catch(InterruptedException ie){}
		
		tm.addRow(new Component[]{new JLabel("Foo"), new JLabel("<HTML><B>Abracadabra!</B></HTML>")});

		try{
			Thread.sleep(2500);
		}
		catch(InterruptedException ie){}
		
		tm.addRow(new Component[]{new JLabel("Foobarish Nutz"), new JLabel("<HTML><I>Abracadabra! Supercalifragilisticexpialidocious!</I></HTML>")});

		try{
			Thread.sleep(2500);
		}
		catch(InterruptedException ie){}
		
		JExpandableDataWidget edw = new JExpandableDataWidget(new JLabel("<HTML><I>In a New York Minute</I></HTML>"), new JLabel("Everything can change."));
		tm.addRow(new Component[]{new JLabel("Row 3"), edw});	

		try{
			Thread.sleep(2500);
		}
		catch(InterruptedException ie){}
		
		tm.addRow(new Component[]{new JLabel("Row 4"), new JLabel("<HTML><I>Abracadabra</I></HTML>")});

		try{
			Thread.sleep(2500);
		}
		catch(InterruptedException ie){}
		tm.removeRow(1);
	}
	
	public JStaticTable(TableModel model){
		this(model, 0, 0);
	}
	
	public JStaticTable(TableModel model, int hgap, int vgap){
		super();
		this.model = model;
		model.addTableModelListener(this);
		this.hgap = hgap;
		this.vgap = vgap;
		layoutTable();
	}

	public void destroy(){
		model.removeTableModelListener(this);
	}
	
	public TableModel getModel(){
		return model;
	}
	
	public void setEmptyViewComponent(java.awt.Component c){
		this.emptyView = c;
		if(model.getRowCount() == 0){
			layoutTable();
		}
	}
	
	public void setDrawRowSplits(boolean drawRowSplits){
		if(this.drawRowSplits == drawRowSplits){
			return;
		}
		this.drawRowSplits = drawRowSplits;
		layoutTable();
	}
	
	private void addRowSplit(){
		for(int j = 0; j < model.getColumnCount(); j++){
			if(j != (model.getColumnCount() - 1)){
				this.add(new JSeparator());
			}
			else{
				this.add(new JPanelEWL(new JSeparator()));
			}
		}
	}
	
	public void tableChanged(TableModelEvent evt){
		layoutTable();
	}
	
	public void layoutTable(){
		this.removeAll();
		if(model.getRowCount() == 0){
			if(emptyView != null){
				this.setLayout(new BorderLayout());
				this.add("Center", emptyView);
			}
			this.revalidate();
			this.repaint();
			return;
		}

		int numRows = model.getRowCount();
		int numColumns = model.getColumnCount();
		
		int rowsInLayout = numRows;
		if(drawRowSplits){
			rowsInLayout = numRows + numRows + 1;
		}
		
		this.setLayout(new GridLayout2(rowsInLayout, numColumns, hgap, vgap));
		if(drawRowSplits){
			addRowSplit();
		}
		for(int i = 0; i < numRows; i++){
			for(int j = 0; j < numColumns; j++){
				this.add(new JPanelUL((Component)model.getValueAt(i, j)));
			}
			if(drawRowSplits){
				addRowSplit();
			}
		}

		this.revalidate();
		this.validate();
		this.repaint();
	}
	
}

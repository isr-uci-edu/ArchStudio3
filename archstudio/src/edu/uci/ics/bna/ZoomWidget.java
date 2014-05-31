package edu.uci.ics.bna;

import javax.swing.*;
import javax.swing.event.*;

import edu.uci.ics.widgets.JPanelUL;

import java.awt.*;
import java.awt.event.*;

public class ZoomWidget{

	private ZoomWidget(){}

	public static JComponent getZoomWidget(ScrollableBNAComponent s){
		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout());
		p2.add("West", new JLabel("Zoom: "));
		p2.add("Center", new ZoomComboBox(s));
		return new JPanelUL(p2, "South", "East");
	}
	
	static class ZoomComboBox extends JComboBox{
		private boolean suppressNextActionEvent = false;

		DefaultComboBoxModel model;
		ScrollableBNAComponent s;
		
		public ZoomComboBox(ScrollableBNAComponent s){
			this.s = s;
			model = new DefaultComboBoxModel();
			
			for(int i = ScrollableBNAComponent.ZOOM_VALUES.length - 1; i >= 0; i--){
				ZoomValue zv = new ZoomValue(ScrollableBNAComponent.ZOOM_VALUES[i]);
				model.addElement(zv);
			}
			this.setModel(model);
			this.setEditable(true);
			s.getBNAComponent().getCoordinateMapper().addCoordinateMapperListener(new CoordinateMapperAdapter());
			this.addActionListener(new ActionAdapter());
		}
		
		class CoordinateMapperAdapter implements CoordinateMapperListener{
			public void coordinateMappingsChanged(CoordinateMapperEvent evt){
				double scale = evt.getNewScale();
				double realpct = scale * 100.0d;
				int realpctInt = (int)realpct;
				String realpctString = realpctInt + "%";
				
				String curComboString = ZoomComboBox.this.getSelectedItem().toString();
				if(realpctString.equals(curComboString)){
					return;
				}
				else{
					//We need to kill the next action event or else the change
					//in the value will cause a loop which we don't want.
					suppressNextActionEvent = true;
					ZoomComboBox.this.getModel().setSelectedItem(new ZoomValue(scale));
				}
			}
		}
		
		class ActionAdapter implements ActionListener{
			public void actionPerformed(ActionEvent evt){
				if(suppressNextActionEvent){
					suppressNextActionEvent = false;
					return;
				}
				String currentComboBoxValue = ZoomComboBox.this.getSelectedItem().toString();
				currentComboBoxValue = currentComboBoxValue.trim();
				if(currentComboBoxValue.endsWith("%")){
					currentComboBoxValue = currentComboBoxValue.substring(0, currentComboBoxValue.length() - 1);
				}
				try{
					int pctInt = Integer.parseInt(currentComboBoxValue);
					if(pctInt < 1){
						throw new NumberFormatException();
					}
					double newScale = (double)pctInt / 100.0d;
					s.rescaleAbsolute(newScale);
				}
				catch(NumberFormatException nfe){
					double scale = s.getBNAComponent().getCoordinateMapper().getScale();
					double realpct = scale * 100.0d;
					int realpctInt = (int)realpct;
					String realpctString = realpctInt + "%";
					ZoomComboBox.this.getModel().setSelectedItem(new ZoomValue(scale));
				}
			}
		}
	}
	
	
	static class ZoomValue{
		double zoomValue;
		
		public ZoomValue(double d){
			this.zoomValue = d;
		}
		
		public String toString(){
			double pct = zoomValue * 100.0d;
			int pctInt = (int)pct;
			return pctInt + "%";
		}
	}

	/*
	JSlider slider;
	ScrollableBNAComponent sbna;
	
	public ZoomWidget(ScrollableBNAComponent s){
		super("Zoomer");
		this.sbna = s;
		slider = new JSlider(0, ScrollableBNAComponent.ZOOM_VALUES.length, ScrollableBNAComponent.ZOOM_VALUES.length / 2);
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add("Center", slider);
		slider.addChangeListener(
			new ChangeListener(){
				public void stateChanged(ChangeEvent e){
					int val = slider.getValue();
					sbna.getBNAComponent().rescaleAbsolute(ScrollableBNAComponent.ZOOM_VALUES[val]);
				}
			}
		);
		this.setVisible(true);
		this.setSize(200, 200);
		this.validate();
		this.repaint();
	}
	*/
	

}

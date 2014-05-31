package edu.uci.ics.bna.demo;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import edu.uci.ics.bna.*;

public class BNADemo1{

	private JFrame fFrame;
	private BNAComponent bnaComponent;
	private BNAModel bnaModel;

	public static void main(String[] args){
		new BNADemo1();
	}

	public BNADemo1(){
		JFrame fFrame = new JFrame("BNA Demo 1");

		//Create the BNA Model
		bnaModel = new DefaultBNAModel();
		
		//Create the BNA component 
		bnaComponent = new BNAComponent("BNA1", bnaModel);
		
		//Set the background color
		bnaComponent.setBackground(Color.WHITE);
		
		//Create a Scrollable BNA Component
		ScrollableBNAComponent scrollableBNAComponent = 
			new ScrollableBNAComponent(bnaComponent);
		
		//Populate the model with some elements
		populateModel();
		
		fFrame.getContentPane().setLayout(new BorderLayout());
		fFrame.getContentPane().add("Center", scrollableBNAComponent);
		
		fFrame.setSize(500, 400);
		fFrame.setLocation(100, 100);
		fFrame.setVisible(true);
		fFrame.validate();
		fFrame.repaint();
	}
	
	private void populateModel(){
		//Figure out where the center of the world is; this will be local
		//coordinate 0,0 when the app starts up.
		int worldCenterX = bnaComponent.getCoordinateMapper().getWorldCenterX();
		int worldCenterY = bnaComponent.getCoordinateMapper().getWorldCenterY();
		
		BoxThing box1 = new BoxThing();
		box1.setBoundingBox(new Rectangle(worldCenterX + 50, worldCenterY + 50, 70, 70));
		box1.setColor(Color.CYAN);
		box1.setLabel("Box 1");
		bnaModel.addThing(box1);
		
		BoxThing box2 = new BoxThing();
		box2.setBoundingBox(new Rectangle(worldCenterX + 200, worldCenterY + 50, 70, 70));
		box2.setColor(Color.PINK);
		box2.setLabel("Box 2");
		bnaModel.addThing(box2);
		
		//Add some endpoints
		EndpointThing endpoint1 = new EndpointThing();
		endpoint1.setFlow(EndpointThing.FLOW_IN);
		endpoint1.setColor(Color.WHITE);
		endpoint1.setTargetThingID(box1.getID());
		bnaModel.addThing(endpoint1);
		
		EndpointThing endpoint2 = new EndpointThing();
		endpoint2.setFlow(EndpointThing.FLOW_OUT);
		endpoint2.setColor(Color.WHITE);
		endpoint2.setTargetThingID(box2.getID());
		bnaModel.addThing(endpoint2);
		
		

	}
	
	private void setupEndpoints(BoxThing box1, BoxThing box2){
		int worldCenterX = bnaComponent.getCoordinateMapper().getWorldCenterX();
		int worldCenterY = bnaComponent.getCoordinateMapper().getWorldCenterY();
		
	}

}

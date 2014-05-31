package edu.uci.ics.bna.debug;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.container.*;
import edu.uci.ics.bna.logic.*;
import edu.uci.ics.bna.floatingtable.*;
import edu.uci.ics.bna.swingthing.*;
import edu.uci.ics.bna.thumbnail.*;

public class BNASandbox{
	
	protected BNAComponent bna;
	
	public static void main(String[] args){
		new BNASandbox();
	}
	
	public BNASandbox(){
		JFrame f = new JFrame();
		DefaultBNAModel bm = new DefaultBNAModel();
		
		BNAComponent c = new BNAComponent("MainBNA", bm);
		SelectionTrackingLogic stl = new SelectionTrackingLogic();
		c.addThingLogic(stl);
		BoundingBoxTrackingLogic bbtl = new BoundingBoxTrackingLogic();
		c.addThingLogic(bbtl);
		AnchorPointTrackingLogic aptl = new AnchorPointTrackingLogic();
		c.addThingLogic(aptl);
		StickyBoxTrackingLogic sbtl = new StickyBoxTrackingLogic();
		c.addThingLogic(sbtl);
		TagThingTrackingLogic tttl = new TagThingTrackingLogic();
		c.addThingLogic(tttl);
		ModelBoundsTrackingLogic mbtl = new ModelBoundsTrackingLogic(bbtl);
		c.addThingLogic(mbtl);
		
		c.addThingLogic(new DragMovableSelectionLogic());
		c.addThingLogic(new NoThingLogic());
		c.addThingLogic(new OneClickSelectionLogic());
		c.addThingLogic(new BoxReshapeLogic(stl, bbtl));
		c.addThingLogic(new SplineReshapeLogic(stl));
		c.addThingLogic(new StickySplineLogic(sbtl));
		c.addThingLogic(new CustomCursorLogic());
		c.addThingLogic(new ToolTipLogic());
		c.addThingLogic(new MoveEndpointLogic(bbtl));
		c.addThingLogic(new MoveIndicatorPointLogic(bbtl));
		c.addThingLogic(new MoveTogetherLogic(bbtl, aptl));
		c.addThingLogic(new RotaterThingLogic());
		
		ModelBoundingLogic mbl = new ModelBoundingLogic(mbtl);
		mbl.setMargin(20);
		c.addThingLogic(mbl);
		
		//c.addThingLogic(new DropContainableThingLogic(stl));
		
		c.addThingLogic(new TaggingLogic(tttl));
		
		bna = c;

		BoxThing b1 = new BoxThing();
		b1.setBoundingBox(c.getCoordinateMapper().getWorldCenterX() + 200, c.getCoordinateMapper().getWorldCenterY() + 200, 300, 100);
		b1.setLabel("BoxThing 1");
		
		bm.addThing(b1);
		
		EndpointThing ep1 = new EndpointThing();
		ep1.setToolTip("EndPoint");
		ep1.setFlow(EndpointThing.FLOW_OUT);
		ep1.setTargetThingID(b1.getID());
		bm.addThing(ep1, b1);
		
		OutlineBoxThing mbt = new OutlineBoxThing();
		bm.addThing(mbt);
		bm.sendToBack(mbt);
		mbl.setModelBoundingThing(mbt);
		
		/*
		TagThing tag1 = new TagThing();
		tag1.setAnchorPoint(new Point(c.getCoordinateMapper().getWorldCenterX() + 200, c.getCoordinateMapper().getWorldCenterY() + 200));
		tag1.setText("Test Test Test");
		tag1.setRotationAngle(-60);
		tag1.setIndicatorThingId(ep1.getID());
		tag1.setMoveTogetherThingId(ep1.getID());
		bm.addThing(tag1, ep1);
		*/
		/*
		RotaterThing rotater1 = new RotaterThing();
		rotater1.setAnchorPoint(tag1.getAnchorPoint());
		rotater1.setRadius(50);
		rotater1.setAdjustmentIncrement(15);
		rotater1.setRotationAngle(tag1.getRotationAngle());
		rotater1.setRotatedThingId(tag1.getID());
		rotater1.setMoveTogetherThingId(tag1.getID());
		bm.addThing(rotater1, tag1);
		*/
		
		FloatingTableThing t1 = new FloatingTableThing();
		//t1.setBoundingBox(CoordinateMapper.WORLD_CENTER_X + 300, CoordinateMapper.WORLD_CENTER_Y + 300, 300, 100);
		TableData td = new TableData();
		TwoColumnTableUtils tctu = new TwoColumnTableUtils(TableThemes.DEFAULT_THEME);
		//TwoColumnTableUtils tctu = new TwoColumnTableUtils(TableThemes.createTheme(edu.uci.ics.widgets.Colors.MEDIUM_COBALT));
		
		tctu.applyTheme(td);
		td.addRow(tctu.createHeaderRow("Header"));
		td.addRow(tctu.createSubheadRow("Subheading"));
		td.addRow(tctu.createBodyRow("Name1", "Value1"));
		td.addRow(tctu.createBodyRow("Name2", "Value2"));
		td.addRows(tctu.createMultiValueBodyRows("Name3", new String[]{"Value3", "Value4", "Value5"}));
		td.setBorder(1);
		t1.setTableData(td);
		
		Dimension t1ps = FloatingTableThingPeer.getPreferredSize(td);
		t1.setBoundingBox(c.getCoordinateMapper().getWorldCenterX() + 300, c.getCoordinateMapper().getWorldCenterY() + 300, t1ps.width, t1ps.height);
		t1.setTransparency(0.60f);
		t1.setIndicatorThingId(b1.getID());
		bm.addThing(t1);
		
		//System.out.println(td.getHtml());
		
		/*
		ContainerThing ct = new ContainerThing();
		ct.setBoundingBox(CoordinateMapper.WORLD_CENTER_X + 100, CoordinateMapper.WORLD_CENTER_Y + 100, 300, 100);
		ct.setColor(Color.GRAY);
		BNAUtils.setStackingPriority(ct, BNAUtils.STACKING_PRIORITY_ALWAYS_ON_BOTTOM);
		
		bm.addThing(ct);
		*/
		
		/*
		
		DefaultBNAModel bm2 = new DefaultBNAModel();
		
		BNAComponent c2 = new BNAComponent("MainBNA", bm2);
		SelectionTrackingLogic stl2 = new SelectionTrackingLogic();
		c2.addThingLogic(stl2);
		BoundingBoxTrackingLogic bbtl2 = new BoundingBoxTrackingLogic();
		c2.addThingLogic(bbtl2);
		StickyBoxTrackingLogic sbtl2 = new StickyBoxTrackingLogic();
		c2.addThingLogic(sbtl2);
		
		c2.addThingLogic(new DragMovableSelectionLogic());
		c2.addThingLogic(new NoThingLogic());
		c2.addThingLogic(new OneClickSelectionLogic());
		c2.addThingLogic(new BoxReshapeLogic(stl2, bbtl2));
		c2.addThingLogic(new SplineReshapeLogic(stl2));
		c2.addThingLogic(new StickySplineLogic(sbtl2));
		c2.addThingLogic(new CustomCursorLogic());
		c2.addThingLogic(new ToolTipLogic());
		c2.addThingLogic(new MoveEndpointLogic(bbtl2));
		
		ct.setComponent(new ScrollableBNAComponent(c2));
		
		for(int i = 0; i < 100; i++){
			BoxThing nb = new BoxThing();
			nb.setBoundingBox(CoordinateMapper.WORLD_CENTER_X + (10*i), CoordinateMapper.WORLD_CENTER_Y + (10*i), 100, 100);
			nb.setLabel("StressTest BoxThing " + i);
			bm2.addThing(nb);
			
			EndpointThing ep = new EndpointThing();
			ep.setFlow(EndpointThing.FLOW_INOUT);
			ep.setTargetThingID(nb.getID());
			bm2.addThing(ep);
		}
		
		bm.addThing(ct);
		*/
		/*
		
		BoxThing b2 = new BoxThing();
		b2.setLabel("BoxThing 2");
		bm.addThing(b2);

		FloatingLabel fl1 = new FloatingLabel();
		fl1.setX(CoordinateMapper.WORLD_CENTER_X + 300);
		fl1.setY(CoordinateMapper.WORLD_CENTER_Y + 100);
		fl1.setLabel("Floating Label 1");
		bm.addThing(fl1);
		
		SplineThing rs = new SplineThing();
		rs.addPoint(new java.awt.Point(CoordinateMapper.WORLD_CENTER_X, CoordinateMapper.WORLD_CENTER_Y));
		rs.addPoint(new java.awt.Point(CoordinateMapper.WORLD_CENTER_X + 100, CoordinateMapper.WORLD_CENTER_Y + 100));
		rs.addPoint(new java.awt.Point(CoordinateMapper.WORLD_CENTER_X + 300, CoordinateMapper.WORLD_CENTER_Y + 100));
		bm.addThing(rs);
		
		EndpointThing ep = new EndpointThing();
		ep.setOrientation(EndpointThing.ORIENTATION_W);
		ep.setFlow(EndpointThing.FLOW_OUT);
		ep.setX(CoordinateMapper.WORLD_CENTER_X + 50);
		ep.setY(CoordinateMapper.WORLD_CENTER_Y + 50);
		ep.setTargetThingID(b1.getID());
		bm.addThing(ep);

		
		*/
		
		//---start thumbing stuff
		
		DefaultBNAModel bmt = new DefaultBNAModel();
		
		for(int i = 0; i < 100; i++){
			BoxThing nb = new BoxThing();
			nb.setBoundingBox(c.getCoordinateMapper().getWorldCenterX() + (10*i), c.getCoordinateMapper().getWorldCenterY() + (10*i), 100, 100);
			nb.setLabel("StressTest BoxThing " + i);
			bmt.addThing(nb);
			
			//EndpointThing ep = new EndpointThing();
			//ep.setFlow(EndpointThing.FLOW_INOUT);
			//ep.setTargetThingID(nb.getID());
			//bmt.addThing(ep);
		}
		
		Thumbnail thumb = new Thumbnail(c, bmt);
		b1.setThumbnail(thumb);
		
		//---end thumbing stuff
		
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add("Center", new ScrollableBNAComponent(c));
		f.setSize(500, 400);
		f.setVisible(true);
		f.validate();
		f.repaint();
		
		try{
			Thread.sleep(5000);
		}
		catch(InterruptedException ie){
		}
		
		//b1.setColor(Color.BLUE);
		
		/*
		double scale = 1.00;
		for(int i = 0; i < 50; i++){
			//b1.setX1(b1.getX1() + 1);
			//b1.setY1(b1.getY1() + 1);
			scale -= 0.05d;
			c.rescaleAbsolute(scale);
			try{
				Thread.sleep(100);
			}
			catch(InterruptedException ie){
			}
		}
		*/
	}
	
	
		
}

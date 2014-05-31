package edu.uci.ics.bna;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class ScrollableBNAComponent extends JComponent implements AdjustmentListener, MouseMotionListener, MouseListener, MouseWheelListener, CoordinateMapperListener{
	protected BNAComponent bna;
	
	protected BoundedRangeModel hsbModel;
	protected BoundedRangeModel vsbModel;
	protected JScrollBar hsb;
	protected JScrollBar vsb;
	
	protected int lastMouseX = -1;
	protected int lastMouseY = -1;
	protected boolean button2Down = false;
	
	protected Cursor backupCursor = null;
	protected Object mouseMotionLock = new Object();
	
	protected boolean allowsZooming = true;
	protected boolean allowsVerticalScrolling = true;
	protected boolean allowsHorizontalScrolling = true;
	
	protected static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);

	public static final double[] ZOOM_VALUES = new double[]
	{ 0.10, 0.20, 0.30, 0.40, 0.50, 0.60, 0.70, 0.80, 0.90, 1.00, 1.25, 1.50, 1.75, 2.00, 4.00, 8.00, 16.00};
	
	/*
	public static void main(String[] args){
		System.out.println(getNextHighestZoomValue(15.0));
		System.out.println(getNextLowestZoomValue(15.0));
		System.out.println(getNextHighestZoomValue(5.0));
		System.out.println(getNextLowestZoomValue(5.0));
	}
	*/
	
	public ScrollableBNAComponent(BNAComponent bna){
		this(bna, true, true, true);
	}
	
	public BNAComponent getBNAComponent(){
		return bna;
	}
	
	public ScrollableBNAComponent(BNAComponent bna, boolean allowsZooming, boolean allowsHorizontalScrolling, boolean allowsVerticalScrolling){
		this.bna = bna;
		this.allowsVerticalScrolling = allowsVerticalScrolling;
		this.allowsHorizontalScrolling = allowsHorizontalScrolling;
		this.allowsZooming = allowsZooming;
		
		bna.addComponentListener(new BNAResizeListener());
		bna.getCoordinateMapper().addCoordinateMapperListener(this);
		bna.addMouseListener(this);
		bna.addMouseMotionListener(this);
		bna.addMouseWheelListener(this);
		
		//System.out.println(bna.getWorldOriginX());
		//System.out.println(bna.getWidth());
		
		if(allowsHorizontalScrolling){
			hsbModel = new DefaultBoundedRangeModel(bna.getWorldOriginX(), bna.getWidth(), 
				bna.getCoordinateMapper().getWorldMinX(), 
				bna.getCoordinateMapper().getWorldMaxX());
			hsb = new JScrollBar(JScrollBar.HORIZONTAL);
			hsb.setModel(hsbModel);
			hsb.setUnitIncrement(hsbModel.getExtent() / 5);
			hsb.setBlockIncrement(hsbModel.getExtent() / 5);
			hsb.addAdjustmentListener(this);
		}
		
		if(allowsVerticalScrolling){
			vsbModel = new DefaultBoundedRangeModel(bna.getWorldOriginY(), bna.getHeight(), 
				bna.getCoordinateMapper().getWorldMinY(), 
				bna.getCoordinateMapper().getWorldMaxY());
			vsb = new JScrollBar(JScrollBar.VERTICAL);
			vsb.setModel(vsbModel);
			vsb.setUnitIncrement(vsbModel.getExtent() / 5);
			vsb.setBlockIncrement(vsbModel.getExtent() / 5);
			vsb.addAdjustmentListener(this);
		}
		
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		
		if(allowsVerticalScrolling && allowsHorizontalScrolling){
			southPanel.add("East", new CornerBlockComponent());
		}
		if(allowsHorizontalScrolling){
			southPanel.add("Center", hsb);
		}
		
		this.setLayout(new BorderLayout());
		this.add("Center", bna);
		this.add("South", southPanel);
		if(allowsVerticalScrolling){
			this.add("East", vsb);
		}
	}
	
	class BNAResizeListener extends ComponentAdapter{
		public void componentResized(ComponentEvent e){
			if(allowsHorizontalScrolling){
				hsbModel.setExtent(bna.getWidth());
				hsb.setUnitIncrement(hsbModel.getExtent() / 5);
				hsb.setBlockIncrement(hsbModel.getExtent() / 5);
			}
			if(allowsVerticalScrolling){
				vsbModel.setExtent(bna.getHeight());
				vsb.setUnitIncrement(vsbModel.getExtent() / 5);
				vsb.setBlockIncrement(vsbModel.getExtent() / 5);
			}
		}
	}
	
	public void setAllowsZooming(boolean allowsZooming){
		this.allowsZooming = allowsZooming;
	}
	
	public boolean getAllowsZooming(){
		return this.allowsZooming;
	}
	
	boolean suppressScrollbarChanges = false;
	
	public void adjustmentValueChanged(AdjustmentEvent e){
		if(suppressScrollbarChanges) return;
		
		int hsbValue;
		int vsbValue;
		
		if(allowsHorizontalScrolling){
			hsbValue = hsb.getValue();
		}
		else{
			hsbValue = bna.getCoordinateMapper().localXtoWorldX(0);
		}			
		if(allowsVerticalScrolling){
			vsbValue = vsb.getValue();
		}
		else{
			vsbValue = bna.getCoordinateMapper().localYtoWorldY(0);
		}
		bna.repositionAbsolute(hsbValue, vsbValue);
	}
	
	public void mouseDragged(MouseEvent e){
		synchronized(mouseMotionLock){
			if(button2Down){
				CoordinateMapper cm = bna.getCoordinateMapper();
				
				int dx = cm.localXtoWorldX(e.getX()) - cm.localXtoWorldX(lastMouseX);
				int dy = cm.localYtoWorldY(e.getY()) - cm.localYtoWorldY(lastMouseY);
				
				//int dx = (int)(((double)e.getX() * scale) - ((double)lastMouseX * scale));
				//int dy = (int)(((double)e.getY() * scale) - ((double)lastMouseY * scale));
				
				//int dx = e.getX() - lastMouseX;
				//int dy = e.getY() - lastMouseY;
				
				if(!allowsVerticalScrolling){
					dy = 0;
				}
				if(!allowsHorizontalScrolling){
					dx = 0;
				}
				
				if((dx != 0) || (dy != 0)){
					bna.repositionRelative(-dx, -dy);
				}
				lastMouseX = e.getX();
				lastMouseY = e.getY();
			}
		}
	}
	
	public void mouseMoved(MouseEvent e){
	}

	public void mouseClicked(MouseEvent e){
	}
	
	public void mouseEntered(MouseEvent e){
	}
	
	public void mouseExited(MouseEvent e){
	}
	
	public void mousePressed(MouseEvent e){
		if(e.getButton() == MouseEvent.BUTTON2){
			synchronized(mouseMotionLock){
				button2Down = true;
				lastMouseX = e.getX();
				lastMouseY = e.getY();
				backupCursor = bna.getCursor();
				bna.setCursor(HAND_CURSOR);
			}
		}
	}
	
	public void mouseReleased(MouseEvent e){
		if(e.getButton() == MouseEvent.BUTTON2){
			synchronized(mouseMotionLock){
				button2Down = false;
				bna.setCursor(backupCursor);
			}
		}
	}
	
	public static double getNextLowestZoomValue(double currentZoomValue){
		double lowestValue = ZOOM_VALUES[0];
		if(currentZoomValue <= lowestValue){
			return currentZoomValue;
		}
		else{
			for(int i = 0; i < ZOOM_VALUES.length; i++){
				if(currentZoomValue == ZOOM_VALUES[i]){
					int index = i-1;
					if(index < 0) index = 0;
					return ZOOM_VALUES[index];
				}
				if(currentZoomValue < ZOOM_VALUES[i]){
					int index = i-1;
					if(index < 0) index = 0;
					return ZOOM_VALUES[index];
				}
			}
		}
		return ZOOM_VALUES[ZOOM_VALUES.length - 1];
	}
	
	public static double getNextHighestZoomValue(double currentZoomValue){
		double highestValue = ZOOM_VALUES[ZOOM_VALUES.length - 1];
		if(currentZoomValue >= highestValue){
			return currentZoomValue;
		}
		else{
			for(int i = (ZOOM_VALUES.length - 1); i >= 0; i--){
				if(currentZoomValue == ZOOM_VALUES[i]){
					int index = i+1;
					if(index >= ZOOM_VALUES.length) index = ZOOM_VALUES.length - 1;
					return ZOOM_VALUES[index];
				}
				if(currentZoomValue > ZOOM_VALUES[i]){
					int index = i+1;
					if(index >= ZOOM_VALUES.length) index = ZOOM_VALUES.length - 1;
					return ZOOM_VALUES[index];
				}
			}
		}
		return ZOOM_VALUES[0];
		
	}
	
	
	public void rescaleAbsolute(double scale){
		suppressScrollbarChanges = true;
		bna.rescaleAbsolute(scale);
		suppressScrollbarChanges = false;
	}

	public void mouseWheelMoved(MouseWheelEvent e){
		if(allowsZooming){
			int clicks = e.getWheelRotation();
			
			//double scrollDelta = 0.05 * (double)(-clicks);
			double scale = bna.getCoordinateMapper().getScale();
			clicks = -clicks;
			suppressScrollbarChanges = true;
			if(clicks < 0){
				bna.rescaleAbsolute(getNextLowestZoomValue(scale));
			}
			else if(clicks > 0){
				bna.rescaleAbsolute(getNextHighestZoomValue(scale));
			}
			suppressScrollbarChanges = false;
			
			/*
			if((scrollDelta < 0) && (scale <= 0.10d)){
				return;
			}
			if((scrollDelta > 0) && (scale >= 10.00d)){
				return;
			}
			bna.rescaleRelative(scrollDelta);
			*/
		}
		else{
			if(allowsVerticalScrolling){
				int clicks = e.getWheelRotation();
				bna.repositionRelative(0, vsb.getBlockIncrement() * clicks);
			}
		}
	}
		
	public void coordinateMappingsChanged(CoordinateMapperEvent evt){
		if(allowsHorizontalScrolling){
			hsb.setValue(evt.getNewWorldOriginX());
		}
		if(allowsVerticalScrolling){
			vsb.setValue(evt.getNewWorldOriginY());
		}
	}
	
	class CornerBlockComponent extends JComponent{
		public Dimension getPreferredSize(){
			return new Dimension(vsb.getWidth(), hsb.getHeight());
		}
		
		public Dimension getMinimumSize(){
			return new Dimension(vsb.getWidth(), hsb.getHeight());
		}

		public Dimension getMaximumSize(){
			return new Dimension(vsb.getWidth(), hsb.getHeight());
		}
	}
}
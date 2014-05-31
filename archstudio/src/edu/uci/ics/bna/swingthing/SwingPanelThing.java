package edu.uci.ics.bna.swingthing;

import edu.uci.ics.bna.*;

import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;

public class SwingPanelThing extends AbstractThing implements ILocalDragMovable, 
ILocalBoxBounded, IResizableLocalBoxBounded, IToolTip, ISelectable, 
IIndicator{

	public static final String PANEL_PROPERTY_NAME = "panel";
	
	public SwingPanelThing(){
		super(c2.util.UIDGenerator.generateUID("SwingPanelThing"));
		Rectangle r = new Rectangle();
		r.x = 10;
		r.y = 10;
		r.width = 50;
		r.height = 50;
		setLocalBoundingBox(r);
		setSelected(false);
		setColor(java.awt.Color.WHITE);
		setTrimColor(java.awt.Color.BLACK);
	}
	
	protected int getMinimumHeight(){
		return 5;
	}
	
	protected int getMinimumWidth(){
		return 5;
	}
	
	public Class getPeerClass(){
		return SwingPanelThingPeer.class;
	}
	
	public void setX(int x){
		Rectangle r = getLocalBoundingBox();
		r.x = x;
		setLocalBoundingBox(r);
	}
	
	public void setY(int y){
		Rectangle r = getLocalBoundingBox();
		r.y = y;
		setLocalBoundingBox(r);		
	}
	
	public void setWidth(int width){
		Rectangle r = getLocalBoundingBox();
		r.width = width;
		setLocalBoundingBox(r);
	}
	
	public void setHeight(int height){
		Rectangle r = getLocalBoundingBox();
		r.height = height;
		setLocalBoundingBox(r);
	}
	
	public String getToolTipText(){
		return getLabel();
	}
	
	public void setLabel(String label){
		setProperty("label", label);
	}
	
	public int getX(){
		Rectangle r = getLocalBoundingBox();
		return r.x;
	}
	
	public int getX2(){
		Rectangle r = getLocalBoundingBox();
		return r.x + r.width;
	}
	
	public int getY(){
		Rectangle r = getLocalBoundingBox();
		return r.y;
	}
	
	public int getY2(){
		Rectangle r = getLocalBoundingBox();
		return r.y + r.height;
	}
	
	public int getHeight(){
		Rectangle r = getLocalBoundingBox();
		return r.height;
	}
	
	public int getWidth(){
		Rectangle r = getLocalBoundingBox();
		return r.width;
	}

	public void setSelected(boolean selected){
		setProperty(SELECTED_PROPERTY_NAME, selected);
	}
	
	public boolean isSelected(){
		return getBooleanProperty(SELECTED_PROPERTY_NAME);
	}
	
	public String getLabel(){
		return (String)getProperty("label");
	}
	
	public void setColor(java.awt.Color c){
		setProperty("color", c);
	}
	
	public java.awt.Color getColor(){
		return (java.awt.Color)getProperty("color");
	}
	
	public void setTrimColor(java.awt.Color c){
		setProperty("trimColor", c);
	}
	
	public java.awt.Color getTrimColor(){
		return (java.awt.Color)getProperty("trimColor");
	}
	
	public void localMoveRelative(int dx, int dy){
		setX(getX() + dx);
		setY(getY() + dy);
	}
	
	public Rectangle getLocalBoundingBox(){
		return new Rectangle((Rectangle)getProperty("boundingBox"));
	}
	
	/*
	public Rectangle getStickyBox(){
		return getLocalBoundingBox();
	}
	*/
	
	public void setLocalBoundingBox(Rectangle r){
		Rectangle nr = BNAUtils.normalizeRectangle(r);
		if(nr.height < getMinimumHeight()){
			nr.height = getMinimumHeight();
		}
		
		if(nr.width < getMinimumWidth()){
			nr.width = getMinimumWidth();
		}
		setProperty("boundingBox", nr);
	}
	
	public void setLocalBoundingBox(int x, int y, int width, int height){
		Rectangle r = new Rectangle();
		r.x = x;
		r.y = y;
		if(width >= getMinimumWidth()){
			r.width = width;
		}
		else{
			r.width = getMinimumWidth();
		}
		if(height >= getMinimumHeight()){
			r.height = height;
		}
		else{
			r.height = getMinimumHeight();
		}
		setLocalBoundingBox(r);
	}

	public void setIndicatorPoint(java.awt.Point indicatorPoint){
		setProperty(IIndicator.INDICATOR_POINT_PROPERTY_NAME, indicatorPoint);
	}
	
	public java.awt.Point getIndicatorPoint(){
		return (java.awt.Point) getProperty(IIndicator.INDICATOR_POINT_PROPERTY_NAME);
	}

	public void setIndicatorThingId(String indicatorThingId){
		setProperty(IIndicator.INDICATOR_THING_ID_PROPERTY_NAME, indicatorThingId);
	}
	
	public String getIndicatorThingId(){
		return (String)getProperty(IIndicator.INDICATOR_THING_ID_PROPERTY_NAME);
	}
	
	public void setPanel(JComponent p){
		p.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK));
		this.setProperty(PANEL_PROPERTY_NAME, p);
	}
	
	public JComponent getPanel(){
		return (JComponent)this.getProperty(PANEL_PROPERTY_NAME);
	}

	public boolean shouldDrawEvenIfOffscreen(){
		return true;
	}

}

/*
package edu.uci.ics.bna.swingthing;

import java.awt.*;
import javax.swing.*;

import edu.uci.ics.bna.BoxThing;
import edu.uci.ics.bna.IDrawnOffscreen;
import edu.uci.ics.bna.Thing;

//Okay, the peer is going to draw this thing in LOCAL, not WORLD coordinates,
//so set the bounding box accordingly.  The reason is that it's damn near
//impossible to get swing components to scale arbitrarily, and I don't feel
//like writing ButtonThings and TextFieldThings and such, and they wouldn't
//be much use at 0.075 zoom.

public class SwingPanelThing extends BoxThing implements Thing, IDrawnOffscreen{
	
	public static final String PANEL_PROPERTY_NAME = "panel";
	
	public SwingPanelThing(){
		super();
		setPanel(new JPanel());
		//setShouldScale(true);
	}
	
	public void setPanel(JComponent p){
		this.setProperty(PANEL_PROPERTY_NAME, p);
	}
	
	public JComponent getPanel(){
		return (JComponent)this.getProperty(PANEL_PROPERTY_NAME);
	}
	
	public Class getPeerClass(){
		return SwingPanelThingPeer.class;
	}
	
	public boolean shouldDrawEvenIfOffscreen(){
		return true;
	}
	
}
*/
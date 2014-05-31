package edu.uci.ics.bna.floatingtable;

import java.awt.Rectangle;
import java.awt.Stroke;

import edu.uci.ics.bna.AbstractThing;
import edu.uci.ics.bna.BNAUtils;
import edu.uci.ics.bna.CoordinateMapper;
import edu.uci.ics.bna.IBoxBounded;
import edu.uci.ics.bna.IColored;
import edu.uci.ics.bna.IDragMovable;
import edu.uci.ics.bna.IIndicator;
import edu.uci.ics.bna.IResizableBoxBounded;
import edu.uci.ics.bna.IStroked;
import edu.uci.ics.bna.IToolTip;
import edu.uci.ics.bna.ITrimColored;
import edu.uci.ics.bna.ISelectable;

public class FloatingTableThing extends AbstractThing implements IDragMovable, IBoxBounded, 
/*IResizableBoxBounded,*/ IToolTip, IIndicator, IColored, ITrimColored, IStroked, ISelectable{

	public static final String TRANSPARENCY_PROPERTY_NAME = "transparency";
	public static final String TABLEDATA_PROPERTY_NAME = "tableData";
	
	public FloatingTableThing(){
		super(c2.util.UIDGenerator.generateUID("FloatingTableThing"));
		Rectangle r = new Rectangle();
		r.x = 0;
		r.y = 0;
		r.width = 50;
		r.height = 50;
		setBoundingBox(r);
		setTransparency(1.0f);
		
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
		return FloatingTableThingPeer.class;
	}
	
	public String getToolTipText(){
		//TODO implement something useful here
		return "";
	}
	
	public void setX(int x){
		Rectangle r = getBoundingBox();
		r.x = x;
		setBoundingBox(r);
	}
	
	public void setY(int y){
		Rectangle r = getBoundingBox();
		r.y = y;
		setBoundingBox(r);		
	}
	
	public void setWidth(int width){
		Rectangle r = getBoundingBox();
		r.width = width;
		setBoundingBox(r);
	}
	
	public void setHeight(int height){
		Rectangle r = getBoundingBox();
		r.height = height;
		setBoundingBox(r);
	}
	
	public int getX(){
		Rectangle r = getBoundingBox();
		return r.x;
	}
	
	public int getX2(){
		Rectangle r = getBoundingBox();
		return r.x + r.width;
	}
	
	public int getY(){
		Rectangle r = getBoundingBox();
		return r.y;
	}
	
	public int getY2(){
		Rectangle r = getBoundingBox();
		return r.y + r.height;
	}
	
	public int getHeight(){
		Rectangle r = getBoundingBox();
		return r.height;
	}
	
	public int getWidth(){
		Rectangle r = getBoundingBox();
		return r.width;
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

	public void setSelected(boolean selected){
		setProperty(SELECTED_PROPERTY_NAME, selected);
	}
	
	public boolean isSelected(){
		return getBooleanProperty(SELECTED_PROPERTY_NAME);
	}
	
	public void setTransparency(float transparency){
		setProperty(TRANSPARENCY_PROPERTY_NAME, transparency);
	}
	
	public float getTransparency(){
		return getFloatProperty(TRANSPARENCY_PROPERTY_NAME);
	}
	
	public void moveRelative(int dx, int dy){
		setX(getX() + dx);
		setY(getY() + dy);
	}
	
	public Rectangle getBoundingBox(){
		return new Rectangle((Rectangle)getProperty("boundingBox"));
	}
	
	/*
	public Rectangle getStickyBox(){
		return getBoundingBox();
	}
	*/
	
	public void setBoundingBox(Rectangle r){
		Rectangle nr = BNAUtils.normalizeRectangle(r);
		if(nr.height < getMinimumHeight()){
			nr.height = getMinimumHeight();
		}
		
		if(nr.width < getMinimumWidth()){
			nr.width = getMinimumWidth();
		}
		setProperty("boundingBox", nr);
	}
	
	public void setBoundingBox(int x, int y, int width, int height){
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
		setBoundingBox(r);
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
	
	public void setStroke(Stroke s){
		setProperty(IStroked.STROKE_PROPERTY_NAME, s);
	}
	
	public Stroke getStroke(){
		return (Stroke)getProperty(IStroked.STROKE_PROPERTY_NAME);
	}

	public void setTableData(TableData tableData){
		setProperty(TABLEDATA_PROPERTY_NAME, tableData);
	}
	
	public TableData getTableData(){
		return (TableData)getProperty(TABLEDATA_PROPERTY_NAME);
	}
}

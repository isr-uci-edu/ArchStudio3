package edu.uci.ics.bna;

import java.awt.Rectangle;
import java.awt.Stroke;

public class EndpointThing extends AbstractThing implements IBoxBounded, 
IStickyBoxBounded, IToolTip, IStroked, IVisible, IUserEditable{
	
	public static final int ORIENTATION_N = 1000;
	public static final int ORIENTATION_E = 1010;
	public static final int ORIENTATION_S = 1020;
	public static final int ORIENTATION_W = 1030;
	
	public static final int FLOW_NONE = 2000;
	public static final int FLOW_IN = 2010;
	public static final int FLOW_OUT = 2020;
	public static final int FLOW_INOUT = 2030;
	
	public EndpointThing(){
		super(c2.util.UIDGenerator.generateUID("EndpointThing"));
		
		Rectangle r = new Rectangle();
		r.x = 0;
		r.y = 0;
		r.width = getMinimumWidth();
		r.height = getMinimumHeight();
		setBoundingBox(r);

		setOrientation(ORIENTATION_N);
		setFlow(FLOW_NONE);
		
		setColor(java.awt.Color.WHITE);
		setTrimColor(java.awt.Color.BLACK);
		
		setVisible(true);
		setUserEditable(true);
	}
	
	public Class getPeerClass(){
		return EndpointThingPeer.class;
	}
	
	protected int getMinimumHeight(){
		return 10;
	}
	
	protected int getMinimumWidth(){
		return 10;
	}
	
	public void setOrientation(int orientation){
		setProperty("orientation", orientation);
	}
	
	public int getOrientation(){
		return getIntProperty("orientation");
	}
	
	public void setFlow(int flow){
		setProperty("flow", flow);
	}
	
	public int getFlow(){
		return getIntProperty("flow");
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
	
	public String getToolTipText(){
		return (String)getProperty("toolTip");
	}
	
	public void setToolTip(String toolTip){
		setProperty("toolTip", toolTip);
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
	
	public void moveRelative(int dx, int dy){
		setX(getX() + dx);
		setY(getY() + dy);
	}
	
	public Rectangle getBoundingBox(){
		return new Rectangle((Rectangle)getProperty("boundingBox"));
	}
	
	public Rectangle getStickyBox(){
		Rectangle r = getBoundingBox();
		
		Rectangle sb = new Rectangle();
		sb.x = r.x + (r.width / 2);
		sb.y = r.y + (r.height / 2);
		sb.width = 1;
		sb.height = 1;
		return sb;
	}
	
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
	
	public void setTargetThingID(String targetThingID){
		setProperty("targetThingID", targetThingID);
	}
	
	public String getTargetThingID(){
		return (String)getProperty("targetThingID");
	}
	
	public void setStroke(Stroke s){
		setProperty(IStroked.STROKE_PROPERTY_NAME, s);
	}
	
	public Stroke getStroke(){
		return (Stroke)getProperty(IStroked.STROKE_PROPERTY_NAME);
	}
	
	public void setVisible(boolean visible){
		setProperty(IVisible.VISIBLE_PROPERTY_NAME, visible);
	}
	
	public boolean isVisible() {
		return getBooleanProperty(IVisible.VISIBLE_PROPERTY_NAME);
	}	
	
	public void setUserEditable(boolean userEditable){
		setProperty(IUserEditable.USER_EDITABLE_PROPERTY_NAME, userEditable);
	}
	
	public boolean isUserEditable() {
		return getBooleanProperty(IUserEditable.USER_EDITABLE_PROPERTY_NAME);
	}	
}
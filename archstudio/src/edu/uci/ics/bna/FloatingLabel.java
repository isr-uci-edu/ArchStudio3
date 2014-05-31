package edu.uci.ics.bna;

public class FloatingLabel extends AbstractThing implements 
IDragMovable, IBoxBounded, ISelectable, IOffset, IToolTip,
IMarqueeSelectable{
	
	public FloatingLabel(){
		super(c2.util.UIDGenerator.generateUID("FloatingLabel"));
		setX(0);
		setY(0);
		setProperty("#width", 1);
		setProperty("#height", 1);
		setSelected(false);
		setLabel("[No Label]");
		setColor(java.awt.Color.BLACK);
		setOffset(0);
	}
	
	public Class getPeerClass(){
		return FloatingLabelPeer.class;
	}
	
	public void setOffset(int offset){
		setProperty("offset", offset);
	}
	
	public int getOffset(){
		return getIntProperty("offset");
	}
	
	public void setX(int x1){
		setProperty("x", x1);
	}
	
	public void setY(int y1){
		setProperty("y", y1);
	}
	
	public String getToolTipText(){
		return getLabel();
	}
	
	public void setLabel(String label){
		setProperty("label", label);
	}
	
	public int getX(){
		return getIntProperty("x");
	}
	
	public int getY(){
		return getIntProperty("y");
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
	
	public void moveRelative(int dx, int dy){
		setX(getX() + dx);
		setY(getY() + dy);
	}
	
	public void setSelected(boolean selected){
		setProperty(SELECTED_PROPERTY_NAME, selected);
	}
	
	public boolean isSelected(){
		return getBooleanProperty(SELECTED_PROPERTY_NAME);
	}
	
	public java.awt.Rectangle getBoundingBox(){
		java.awt.Rectangle r = new java.awt.Rectangle();
		r.x = getX();
		r.y = getY();
		r.width = getIntProperty("#width");
		r.height = getIntProperty("#height");
		return r;
	}
	
}
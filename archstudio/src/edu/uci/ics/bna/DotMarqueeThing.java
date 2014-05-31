package edu.uci.ics.bna;

public class DotMarqueeThing extends AbstractThing implements IBoxBounded, IOffset{
	
	public DotMarqueeThing(){
		super(c2.util.UIDGenerator.generateUID("DotMarqueeThing"));
		setX1(0);
		setY1(0);
		setWidth(50);
		setHeight(50);
		setOffset(0);
	}
	
	public Class getPeerClass(){
		return DotMarqueeThingPeer.class;
	}
	
	public void setOffset(int offset){
		setProperty("offset", offset);
	}
	
	public void setX1(int x1){
		setProperty("x1", x1);
	}
	
	public void setX2(int x2){
		setProperty("x2", x2);
	}
	
	public void setY1(int y1){
		setProperty("y1", y1);
	}
	
	public void setY2(int y2){
		setProperty("y2", y2);
	}
	
	public void setWidth(int width){
		setProperty("x2", getX1() + width);
	}
	
	public void setHeight(int height){
		setProperty("y2", getY1() + height);
	}
	
	public void setTargetObjectID(String targetObjectID){
		setProperty("targetObjectID", targetObjectID);
	}
	
	public int getX1(){
		return getIntProperty("x1");
	}
	
	public int getX2(){
		return getIntProperty("x2");
	}
	
	public int getY1(){
		return getIntProperty("y1");
	}
	
	public int getY2(){
		return getIntProperty("y2");
	}
	
	public int getHeight(){
		return getY2() - getY1();
	}
	
	public int getWidth(){
		return getX2() - getX1();
	}
	
	public int getOffset(){
		return getIntProperty("offset");
	}
	
	public String getTargetObjectID(){
		return (String)getProperty("targetObjectID");
	}
	
	public java.awt.Rectangle getBoundingBox(){
		java.awt.Rectangle r = new java.awt.Rectangle();
		r.x = getX1();
		r.y = getY1();
		r.width = getWidth();
		if(r.width < 0){
			r.x = getX2();
			r.width = -r.width;
		}
		
		r.height = getHeight();
		if(r.height < 0){
			r.y = getY2();
			r.height = -r.height;
		}
		return r;
	}
	
}
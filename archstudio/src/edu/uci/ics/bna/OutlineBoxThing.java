package edu.uci.ics.bna;

import java.awt.Rectangle;
import java.awt.Stroke;

import edu.uci.ics.bna.thumbnail.Thumbnail;

public class OutlineBoxThing extends AbstractThing implements IBoxBounded, 
IResizableBoxBounded, IStroked, ITrimColored{
	
	public OutlineBoxThing(){
		super(c2.util.UIDGenerator.generateUID("OutlineBoxThing"));
		Rectangle r = new Rectangle();
		r.x = 0;
		r.y = 0;
		r.width = 50;
		r.height = 50;
		setBoundingBox(r);
		
		setTrimColor(java.awt.Color.BLACK);
	}
	
	public OutlineBoxThing(OutlineBoxThing copyMe){
		super(copyMe);
	}
	
	public Class getPeerClass(){
		return OutlineBoxThingPeer.class;
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
	
	/*
	public Rectangle getStickyBox(){
		return getBoundingBox();
	}
	*/
	
	public void setBoundingBox(Rectangle r){
		Rectangle nr = BNAUtils.normalizeRectangle(r);
		setProperty("boundingBox", nr);
	}
	
	public void setBoundingBox(int x, int y, int width, int height){
		Rectangle r = new Rectangle();
		r.x = x;
		r.y = y;
		r.width = width;
		r.height = height;
		setBoundingBox(r);
	}

	public void setStroke(Stroke s){
		setProperty(IStroked.STROKE_PROPERTY_NAME, s);
	}
	
	public Stroke getStroke(){
		return (Stroke)getProperty(IStroked.STROKE_PROPERTY_NAME);
	}

}

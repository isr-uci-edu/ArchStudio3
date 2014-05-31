package edu.uci.ics.bna;

import java.awt.Rectangle;
import java.awt.Stroke;

import edu.uci.ics.bna.thumbnail.Thumbnail;

public class BoxThing extends AbstractThing implements IDragMovable, ISelectable, IBoxBounded, 
IResizableBoxBounded, IOffset, IToolTip, IMarqueeSelectable, IGlassable, 
IIndicator, IContainsThumbnail, IStroked, IColored, ITrimColored, ITextColored, IVisible, IUserEditable{
	
	public BoxThing(){
		super(c2.util.UIDGenerator.generateUID("BoxThing"));
		Rectangle r = new Rectangle();
		r.x = 0;
		r.y = 0;
		r.width = 50;
		r.height = 50;
		setBoundingBox(r);
		
		setSelected(false);
		setColor(java.awt.Color.WHITE);
		setTextColor(java.awt.Color.BLACK);
		setTrimColor(java.awt.Color.BLACK);
		setOffset(0);
		setGlassed(false);
		setGlassedTransparency(1.00f);
		setThumbnailInset(15);
		setWrapLabel(false);
		setDoubleBorder(false);
		
		setVisible(true);
		setUserEditable(true);
	}
	
	public BoxThing(BoxThing copyMe){
		super(copyMe);
	}
	
	protected int getMinimumHeight(){
		return 5;
	}
	
	protected int getMinimumWidth(){
		return 5;
	}
	
	public Class getPeerClass(){
		return BoxThingPeer.class;
	}
	
	public void setOffset(int offset){
		setProperty(OFFSET_PROPERTY_NAME, offset);
	}
	
	public int getOffset(){
		return getIntProperty(OFFSET_PROPERTY_NAME);
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
		return getLabel();
	}
	
	public void setLabel(String label){
		setProperty("label", label);
	}
	
	public void setWrapLabel(boolean wrapLabel){
		setProperty("wrapLabel", wrapLabel);
	}
	
	public void setDoubleBorder(boolean doubleBorder){
		setProperty("doubleBorder", doubleBorder);
	}
	
	public boolean getDoubleBorder(){
		return getBooleanProperty("doubleBorder");
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

	public String getLabel(){
		return (String)getProperty("label");
	}
	
	public boolean getWrapLabel(){
		return getBooleanProperty("wrapLabel");
	}
	
	public void setColor(java.awt.Color c){
		setProperty(COLOR_PROPERTY_NAME, c);
	}
	
	public java.awt.Color getColor(){
		return (java.awt.Color)getProperty(COLOR_PROPERTY_NAME);
	}
	
	public void setTextColor(java.awt.Color c){
		setProperty(TEXT_COLOR_PROPERTY_NAME, c);
	}
	
	public java.awt.Color getTextColor(){
		return (java.awt.Color)getProperty(TEXT_COLOR_PROPERTY_NAME);
	}
	
	public void setTrimColor(java.awt.Color c){
		setProperty(TRIM_COLOR_PROPERTY_NAME, c);
	}
	
	public java.awt.Color getTrimColor(){
		return (java.awt.Color)getProperty(TRIM_COLOR_PROPERTY_NAME);
	}
	
	public void setSelected(boolean selected){
		setProperty(SELECTED_PROPERTY_NAME, selected);
	}
	
	public boolean isSelected(){
		return getBooleanProperty(SELECTED_PROPERTY_NAME);
	}
	
	public void setGlassed(boolean glassed){
		setProperty(GLASSED_PROPERTY_NAME, glassed);
	}
	
	public boolean isGlassed(){
		return getBooleanProperty(GLASSED_PROPERTY_NAME);
	}
	
	public void setGlassedTransparency(float transparency){
		setProperty(GLASSED_TRANSPARENCY_PROPERTY_NAME, transparency);
	}
	
	public float getGlassedTransparency(){
		return getFloatProperty(GLASSED_TRANSPARENCY_PROPERTY_NAME);
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
	
	public void setThumbnail(Thumbnail thumbnail){
		setProperty(IContainsThumbnail.THUMBNAIL_PROPERTY_NAME, thumbnail);
	}

	public Thumbnail getThumbnail(){
		return (Thumbnail)getProperty(IContainsThumbnail.THUMBNAIL_PROPERTY_NAME);
	}

	public void setThumbnailInset(int thumbnailInset){
		setProperty(IContainsThumbnail.THUMBNAIL_INSET_PROPERTY_NAME, thumbnailInset);
	}

	public int getThumbnailInset(){
		return getIntProperty(IContainsThumbnail.THUMBNAIL_INSET_PROPERTY_NAME);
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

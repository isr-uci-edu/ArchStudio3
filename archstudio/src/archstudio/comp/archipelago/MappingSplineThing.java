package archstudio.comp.archipelago;

import edu.uci.ics.bna.*;

import java.awt.Point;
import java.awt.Rectangle;

public class MappingSplineThing extends AbstractThing implements IToolTip, IBoxBounded{

	public static final String POINT1_PROPERTY_NAME = "point1";
	public static final String POINT2_PROPERTY_NAME = "point2";

	public MappingSplineThing(){
		super(c2.util.UIDGenerator.generateUID("MappingSplineThing"));
	}

	public MappingSplineThing(Thing copyMe) {
		super(copyMe);
	}

	public Class getPeerClass(){
		return MappingSplineThingPeer.class;
	}

	public void setFirstPoint(int x, int y){
		setProperty(POINT1_PROPERTY_NAME, new Point(x, y));
	}
	
	public void setFirstPoint(Point p){
		setProperty(POINT1_PROPERTY_NAME, p);
	}
	
	public Point getFirstPoint(){
		return (Point)getProperty(POINT1_PROPERTY_NAME);
	}
	
	public void setSecondPoint(int x, int y){
		setProperty(POINT2_PROPERTY_NAME, new Point(x, y));
	}
	
	public void setSecondPoint(Point p){
		setProperty(POINT2_PROPERTY_NAME, p);
	}
	
	public Point getSecondPoint(){
		return (Point)getProperty(POINT2_PROPERTY_NAME);
	}

	public void setToolTipText(String toolTip){
		setProperty(TOOLTIP_TEXT_PROPERTY_NAME, toolTip);
	}
	
	public String getToolTipText(){
		return (String)getProperty(TOOLTIP_TEXT_PROPERTY_NAME);
	}

	public Rectangle getBoundingBox(){
		Point p1 = getFirstPoint();
		Point p2 = getSecondPoint();
		
		if((p1 == null) || (p2 == null)){
			return null;
		}
		
		Rectangle bb = new Rectangle();
		bb.x = p1.x;
		bb.y = p1.y;
		bb.add(p2.x, p2.y);
		return bb;
	}

}

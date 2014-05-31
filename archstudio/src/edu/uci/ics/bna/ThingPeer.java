package edu.uci.ics.bna;

import java.awt.*;

public abstract class ThingPeer{
	
	protected BNAComponent bnaComponent;
	protected Thing thing;
	
	public ThingPeer(BNAComponent bnaComponent, Thing thing){
		this.bnaComponent = bnaComponent;
		this.thing = thing;
	}
	
	/**
	 * Draws the peer's associated Thing on the given Graphics2D,
	 * using the given CoordinateMapper to map all world coordinates
	 * to local coordinates if necessary.
	 * 
	 * @param g The Graphics2D on which to draw the Thing.
	 * @param cm The coordinate mapper used to map world to local coordinates.
	 */
	public abstract void draw(Graphics2D g, CoordinateMapper cm);
	
	/**
	 * Determine if the given point (in world coordinates) is "in"
	 * the peer's associated Thing.  That is, if the user clicked the
	 * mouse on the given world-point, would they reasonably expect to
	 * have hit this Thing?
	 * 
	 * @param g The Graphics2D context in which to make the determination.
	 * @param cm The coordinate mapper used to map world to local coordinates.
	 * @param worldX The X-coordinate of the world point to test.
	 * @param worldY The Y-coordinate of the world point to test.
	 * @return <code>true</code> if the point is "in" the Thing, <code>false</code> otherwise.
	 */
	public abstract boolean isInThing(Graphics2D g, CoordinateMapper cm, int worldX, int worldY);

	/**
	 * Determine the local bounding box of the peer's associated Thing.
	 * This is used mostly for drawing optimization and as such is an optional
	 * operation; Things that return <code>null</code> from this routine
	 * will not be optimized.  As such, well-behaved Things should not return
	 * <code>null</code>.
	 * 
	 * @param g The Graphics2D context in which to make the determination.
	 * @param cm The Coordinate mapper used to map world coordinates to local coordinates.
	 * @return a <code>Rectangle</code> containing the Thing's bounding box in local coordinates.
	 */
	public abstract Rectangle getLocalBoundingBox(Graphics2D g, CoordinateMapper cm);
	
}

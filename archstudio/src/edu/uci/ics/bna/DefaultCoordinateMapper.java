package edu.uci.ics.bna;


public class DefaultCoordinateMapper implements CoordinateMapper{

	public static final int DEFAULT_WORLD_WIDTH = 20000;
	public static final int DEFAULT_WORLD_HEIGHT = 20000;
	
	private int worldWidth = 20000;
	private int worldHeight = 20000;
	
	private int originWorldX = 10000;
	private int originWorldY = 10000;
	
	private double scale = 1.0d;
	
	public DefaultCoordinateMapper(){
		this(DEFAULT_WORLD_WIDTH, DEFAULT_WORLD_HEIGHT);
	}
	
	public DefaultCoordinateMapper(int worldWidth, int worldHeight){
		this.worldWidth = worldWidth;
		this.worldHeight = worldHeight;
		this.originWorldX = worldWidth / 2;
		this.originWorldY = worldWidth / 2;
	}
	
	public int getWorldMinX(){
		return 0;
	}
	
	public int getWorldMinY(){
		return 0;
	}
	
	public int getWorldCenterX(){
		return worldWidth / 2;
	}
	
	public int getWorldCenterY(){
		return worldHeight / 2;
	}
	
	public int getWorldMaxX(){
		return worldWidth;
	}
	
	public int getWorldMaxY(){
		return worldHeight;
	}
	
	public int worldXtoLocalX(int worldX){
		//return worldX - originWorldX;
		return BNAUtils.round(((double)worldX - (double)originWorldX) * scale);
	}
	
	public int worldYtoLocalY(int worldY){
		//return worldY - originWorldY;
		return BNAUtils.round(((double)worldY - (double)originWorldY) * scale);
	}
	
	public int localXtoWorldX(int localX){
		//return originWorldX + localX;
		int xx = BNAUtils.round((double)localX / scale);
		return originWorldX + xx;
	}
	
	public int localYtoWorldY(int localY){
		//return originWorldY + localY;
		int yy = BNAUtils.round((double)localY / scale);
		return originWorldY + yy;
	}
	
	public void repositionRelative(int dx, int dy){
		originWorldX += dx;
		if(originWorldX < 0) originWorldX = 0;
		originWorldY += dy;
		if(originWorldY < 0) originWorldY = 0;
		fireCoordinateMapperEvent(new CoordinateMapperEvent(originWorldX, originWorldY, scale));
	}
	
	public void repositionAbsolute(int x, int y){
		originWorldX = x;
		if(originWorldX < 0) originWorldX = 0;
		originWorldY = y;
		if(originWorldY < 0) originWorldY = 0;
		fireCoordinateMapperEvent(new CoordinateMapperEvent(originWorldX, originWorldY, scale));
	}

	public double getScale(){
		return scale;
	}
	
	public void rescaleAbsolute(double newScale){
		scale = newScale;
		fireCoordinateMapperEvent(new CoordinateMapperEvent(originWorldX, originWorldY, scale));
	}		
	
	public void rescaleRelative(double ds){
		scale += ds;
		fireCoordinateMapperEvent(new CoordinateMapperEvent(originWorldX, originWorldY, scale));
	}		
	
	protected java.util.Vector listeners = new java.util.Vector();
	
	public void addCoordinateMapperListener(CoordinateMapperListener l){
		synchronized(listeners){
			listeners.addElement(l);
		}
	}
	
	public void removeCoordinateMapperListener(CoordinateMapperListener l){
		synchronized(listeners){
			listeners.removeElement(l);
		}
	}
	
	protected void fireCoordinateMapperEvent(CoordinateMapperEvent evt){
		synchronized(listeners){
			for(int i = 0; i < listeners.size(); i++){
				((CoordinateMapperListener)listeners.elementAt(i)).coordinateMappingsChanged(evt);
			}
		}
	}
}

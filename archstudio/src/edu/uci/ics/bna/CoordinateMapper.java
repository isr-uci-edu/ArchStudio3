package edu.uci.ics.bna;

public interface CoordinateMapper{
	
	//public static final int WORLD_MIN_X = 0;
	//public static final int WORLD_MIN_Y = 0;
	
	//public static final int WORLD_CENTER_X = 10000;
	//public static final int WORLD_CENTER_Y = 10000;
	//public static final int WORLD_MAX_X = 20000;
	//public static final int WORLD_MAX_Y = 20000;

	//public static final int WORLD_CENTER_X = Integer.MAX_VALUE / 2;
	//public static final int WORLD_CENTER_Y = Integer.MAX_VALUE / 2;
	//public static final int WORLD_MAX_X = Integer.MAX_VALUE - 64;
	//public static final int WORLD_MAX_Y = Integer.MAX_VALUE - 64;
	
	public int worldXtoLocalX(int worldX);
	public int worldYtoLocalY(int worldY);
	
	public int localXtoWorldX(int worldX);
	public int localYtoWorldY(int worldY);
	
	public double getScale();
	
	public int getWorldMinX();
	public int getWorldCenterX();
	public int getWorldMaxX();
	
	public int getWorldMinY();
	public int getWorldCenterY();
	public int getWorldMaxY();
	
	public void addCoordinateMapperListener(CoordinateMapperListener l);
	public void removeCoordinateMapperListener(CoordinateMapperListener l);
}

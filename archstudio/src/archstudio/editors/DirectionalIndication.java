package archstudio.editors;

import java.awt.Color;

public class DirectionalIndication implements Indication{

	public static final int TOWARD_NEITHER = 0;
	public static final int TOWARD_ENDPOINT_1 = 1;
	public static final int TOWARD_ENDPOINT_2 = 2;
	public static final int TOWARD_BOTH = TOWARD_ENDPOINT_1 | TOWARD_ENDPOINT_2;

	protected int directionMask = 0;

	public DirectionalIndication(int directionMask){
		this.directionMask = directionMask;
	}
	
	public void setDirection(int directionMask){
		this.directionMask = directionMask;
	}
	
	public int getDirection(){
		return directionMask;
	}
	
	public boolean isTowardEndpoint1(){
		return (directionMask & TOWARD_ENDPOINT_1) != 0;
	}

	public boolean isTowardEndpoint2(){
		return (directionMask & TOWARD_ENDPOINT_2) != 0;
	}
}

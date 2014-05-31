package edu.uci.ics.bna;

public interface IStickyEndpointsSpline extends IReshapableSpline{
	public void setFirstEndpointStuckToID(String firstEndpointStuckToID);
	public void setSecondEndpointStuckToID(String secondEndpointStuckToID);	
	public String getFirstEndpointStuckToID();
	public String getSecondEndpointStuckToID();
}

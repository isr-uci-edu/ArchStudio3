package edu.uci.ics.bna;

public interface IDrawnOffscreen{
	//This is a used to indicate whether the
	//BNAComponent should always call draw() on the thing's peer,
	//even if its bounds indicate that it might be offscreen.
	
	public boolean shouldDrawEvenIfOffscreen();
}

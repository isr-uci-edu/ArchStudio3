package edu.uci.ics.bna.logic;

import java.awt.*;

import c2.util.ArrayUtils;
import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.CoordinateMapper;
import edu.uci.ics.bna.ThingLogicAdapter;

public class FlyToLogic extends ThingLogicAdapter {

	public void flyTo(Point p){
		flyTo(p.x, p.y);
	}
	
	public void flyTo(int worldX, int worldY){
		BNAComponent c = getBNAComponent();
		FlyToLogic.flyTo(c, worldX, worldY);
	}
	
	public static synchronized void flyTo(BNAComponent comp, int wx, int wy){
		final BNAComponent c = comp;
		final int worldX = wx;
		final int worldY = wy;
		if(c != null){
			Thread flyThread = new Thread(){
				public void run(){
					synchronized(FlyToLogic.class){
						int componentWidth = c.getSize().width;
						int componentHeight = c.getSize().height;
				
						int lcx = componentWidth / 2;
						int lcy = componentWidth / 2;
				
						CoordinateMapper cm = c.getCoordinateMapper();
						int ox = cm.localXtoWorldX(0);
						int oy = cm.localYtoWorldY(0);
				
						int cx = cm.localXtoWorldX(lcx);
						int cy = cm.localYtoWorldY(lcy);
				
						//System.out.println("Calculating fly to from " + cx + "," + cy + " to " + worldX + "," + worldY + ".");
				
						int dx = worldX - cx;
						int dy = worldY - cy;
						
						int lineLength = (int)Math.round(Math.sqrt((dx * dx) + (dy * dy)));
						
						int[] segLengths = calcSteps(lineLength, ((int)Math.round(lg(lineLength)) * 2));
						//int[] segLengths = calcSteps(lineLength, 20);
						
						Point o = new Point(ox, oy);
						Point d = new Point(ox + dx, oy + dy);
						
						//System.out.println("O = " + o.x + "," + o.y + " ; D =  " + d.x + "," + d.y + ".");
						double oscale = cm.getScale();
						
						//System.out.println(c2.util.ArrayUtils.arrayToString(segLengths));
						
						double scaleFactor = oscale / 50.0d;
						
						Point lastPoint = o;
						for(int i = 0; i < segLengths.length; i++){
							
							try{
								c.getModel().beginBulkChange();
								Point p = edu.uci.ics.widgets.WidgetUtils.calcPointOnLineAtDist(o, d, segLengths[i]);
								
								if(i < (segLengths.length / 2)){
									c.rescaleRelative(-scaleFactor);
								}
								if(i > (segLengths.length / 2)){
									c.rescaleRelative(scaleFactor);
								}
								
								c.repositionRelative(p.x - lastPoint.x, p.y - lastPoint.y);
								lastPoint = p;
							}finally{
								c.getModel().endBulkChange();
							}
							
							try{
								Thread.sleep(100);
							}
							catch(InterruptedException e){}
						}
						
						c.rescaleAbsolute(oscale);
						c.repositionAbsolute(ox + dx, oy + dy);	
					}
				}
			};
			flyThread.start();
		}
	}
	
	public static int[] calcSteps(int numPixels, int numSteps){
		if((numSteps % 2) == 0){
			numSteps++;
		}
		
		int[] steps = new int[numSteps];
		
		int np = numPixels;
		int mp = numSteps / 2;
		for(int i = mp; i >= 0; i--){
			np = np / 2;
			steps[i] = np;
		}
		
		//int j = 1;
		for(int i = mp + 1; i < numSteps; i++){
			steps[i] = steps[mp] + steps[mp] - steps[numSteps - i - 1];
			//j++;
		}
		return steps;
	}

	//Base-2 logarithm
	private static double lg(double x) {
		double a = Math.log(x);
		double b = Math.log(2.0);
		return (a/b);
	}

	/*
	public static void main(String[] args){
		int[] steps = calcSteps(100, 15);
		for(int i = 0; i < steps.length; i++){
			System.out.println(steps[i]);
		}
	}
	*/
	
}

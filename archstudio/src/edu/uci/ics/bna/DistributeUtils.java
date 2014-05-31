package edu.uci.ics.bna;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Comparator;

public class DistributeUtils {

	private DistributeUtils(){}

	static final XComparator xComparator = new XComparator();
	static final YComparator yComparator = new YComparator();

	public static void distributeHorizontallyTight(Thing[] distributableThings){
		Arrays.sort(distributableThings, xComparator);
		int distributeAt = ((BoxThing)distributableThings[0]).getX() + ((BoxThing)distributableThings[0]).getWidth() + 10;
		for(int i = 1; i < distributableThings.length; i++){
			Rectangle currentBounds = ((BoxThing)distributableThings[i]).getBoundingBox();
			currentBounds.setLocation(distributeAt, currentBounds.y);
			((BoxThing)distributableThings[i]).setBoundingBox(currentBounds);
			distributeAt = distributeAt + currentBounds.width + 10;
		}
	}

	public static void distributeVerticallyTight(Thing[] distributableThings){
		Arrays.sort(distributableThings, yComparator);
		int distributeAt = ((BoxThing)distributableThings[0]).getY() + ((BoxThing)distributableThings[0]).getHeight() + 10;
		for(int i = 1; i < distributableThings.length; i++){
			Rectangle currentBounds = ((BoxThing)distributableThings[i]).getBoundingBox();
			currentBounds.setLocation(currentBounds.x, distributeAt);
			((BoxThing)distributableThings[i]).setBoundingBox(currentBounds);
			distributeAt = distributeAt + currentBounds.height + 10;
		}
	}
	
	public static void distributeHorizontallyLoose(Thing[] distributableThings){
		Arrays.sort(distributableThings, xComparator);
		int min = ((BoxThing)distributableThings[0]).getX();
		int max = ((BoxThing)distributableThings[0]).getX();

		for(int i = 1; i < distributableThings.length; i++){
			int lmin = ((BoxThing)distributableThings[i]).getX();
			int lmax = ((BoxThing)distributableThings[i]).getX();
					
			if(lmin < min) min = lmin;
			if(lmax > max) max = lmax;
		}
				
		int delta = (max - min) / distributableThings.length;
				
		int distributeAt = ((BoxThing)distributableThings[0]).getX() + delta;
		for(int i = 1; i < distributableThings.length; i++){
			Rectangle currentBounds = ((BoxThing)distributableThings[i]).getBoundingBox();
			currentBounds.setLocation(distributeAt, currentBounds.y);
			((BoxThing)distributableThings[i]).setBoundingBox(currentBounds);
			distributeAt = distributeAt + delta;
		}
	}

	public static void distributeVerticallyLoose(Thing[] distributableThings){
		Arrays.sort(distributableThings, yComparator);
		int min = ((BoxThing)distributableThings[0]).getY();
		int max = ((BoxThing)distributableThings[0]).getY();

		for(int i = 1; i < distributableThings.length; i++){
			int lmin = ((BoxThing)distributableThings[i]).getY();
			int lmax = ((BoxThing)distributableThings[i]).getY();
					
			if(lmin < min) min = lmin;
			if(lmax > max) max = lmax;
		}
				
		int delta = (max - min) / distributableThings.length;
				
		int distributeAt = ((BoxThing)distributableThings[0]).getY() + delta;
		for(int i = 1; i < distributableThings.length; i++){
			Rectangle currentBounds = ((BoxThing)distributableThings[i]).getBoundingBox();
			currentBounds.setLocation(currentBounds.x, distributeAt);
			((BoxThing)distributableThings[i]).setBoundingBox(currentBounds);
			distributeAt = distributeAt + delta;
		}
	}
	
	static class XComparator implements Comparator{
		public int compare(Object o1, Object o2){
			BoxThing b1 = (BoxThing)o1;
			BoxThing b2 = (BoxThing)o2;
			int x1 = b1.getX();
			int x2 = b2.getX();
			if(x1 < x2){
				return -1;
			}
			else if(x1 == x2){
				return 0;
			}
			else{
				return 1;
			}
		}
	}

	static class YComparator implements Comparator{
		public int compare(Object o1, Object o2){
			BoxThing b1 = (BoxThing)o1;
			BoxThing b2 = (BoxThing)o2;
			int y1 = b1.getY();
			int y2 = b2.getY();
			if(y1 < y2){
				return -1;
			}
			else if(y1 == y2){
				return 0;
			}
			else{
				return 1;
			}
		}
	}
}

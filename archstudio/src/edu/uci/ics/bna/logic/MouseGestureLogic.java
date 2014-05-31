package edu.uci.ics.bna.logic;

import edu.uci.ics.bna.*;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import hhreco.recognition.*;
import hhreco.toolbox.*;		//needed if you want to use the stroke filters

public class MouseGestureLogic extends ThingLogicAdapter {

	public static final String DEFAULT_RESOURCE_NAME = "edu/uci/ics/bna/res/default.sml";

	protected HHRecognizer reco = null;
	protected ApproximateStrokeFilter approx = null; 
	protected InterpolateStrokeFilter interp = null;

	public static final double APPROXIMATE_STROKE_FILTER_DISTANCE = 1.0;
	public static final double INTERPOLATE_STROKE_FILTER_SPACING = 10.0;
	
	//public static final double APPROXIMATE_STROKE_FILTER_DISTANCE = 
	//	ApproximateStrokeFilter.DEFAULT_THRESH_DISTANCE;
	//public static final double INTERPOLATE_STROKE_FILTER_SPACING = 
	//	InterpolateStrokeFilter.DEFAULT_SPACING;

	protected TimedStroke stroke = null;

	protected Map actionMap = null;

	public MouseGestureLogic(){
		reco = new HHRecognizer();
		approx = new ApproximateStrokeFilter(APPROXIMATE_STROKE_FILTER_DISTANCE);
		interp = new InterpolateStrokeFilter(INTERPOLATE_STROKE_FILTER_SPACING);
		actionMap = new HashMap();
		initRecognizer();
	}

	public void setAction(String gestureName, String gestureDisplayName, String actionDisplayName, ThingLogicAction action){
		ActionItem ai = new ActionItem();
		ai.gestureName = gestureName;
		ai.gestureDisplayName = gestureDisplayName;
		ai.actionDisplayName = actionDisplayName;
		ai.action = action;
		actionMap.put(gestureName, ai);
	}

	protected void initRecognizer(){
		try{
			InputStream resourceInputStream = null;

			resourceInputStream = BNAComponent.class.getResourceAsStream(DEFAULT_RESOURCE_NAME);

			if(resourceInputStream == null){
				resourceInputStream = ClassLoader.getSystemResourceAsStream(DEFAULT_RESOURCE_NAME);
			}
			if(resourceInputStream == null){
				throw new RuntimeException("Can't open default gesture resource.");
			}

			Reader r = new InputStreamReader(resourceInputStream);
			MSTrainingParser parser = new MSTrainingParser();
			MSTrainingModel trainModel = (MSTrainingModel)parser.parse(r);
			
			//for each example in the training set, call HHRecognizer.preprocess and pass
			//in the filters.
			MSTrainingModel model = new MSTrainingModel();
			for (Iterator iter = trainModel.types(); iter.hasNext();) {
				String type = (String)iter.next();
				for(Iterator iter2 = trainModel.positiveExamples(type); iter2.hasNext();) {
					TimedStroke[] strokes = (TimedStroke[])iter2.next();
					strokes = HHRecognizer.preprocess(strokes, approx, interp, null);
					model.addPositiveExample(type, strokes);
				}
			}
			
			reco.train(model);
			
		}
		
		catch(Exception e){
			throw new RuntimeException(e);
		}			
	}

	int lastX = -1;
	int lastY = -1;

	public void addRecognizePoint(int lx, int ly, int wx, int wy){
		if(stroke != null){
			stroke.addVertex(lx, ly, System.currentTimeMillis());
		
			GestureTrackThing gt = new GestureTrackThing();
			if(lastX > 0){
				gt.setPoint1(new Point(lastX, lastY));
			}
			gt.setPoint2(new Point(wx, wy));
			lastX = wx;
			lastY = wy;
			BNAUtils.setStackingPriority(gt, BNAUtils.STACKING_PRIORITY_ALWAYS_ON_TOP);
			getBNAComponent().getModel().addThing(gt);
		}
	}

	public void mouseMoved(Thing t, MouseEvent evt, int worldX, int worldY){
		if(isRecognizing){
			addRecognizePoint(evt.getX(), evt.getY(), worldX, worldY);
		}
	}

	protected void doBeginRecognizing(){
		stroke = new TimedStroke();
	}

	protected void doEndRecognizing(){
		int lx = getBNAComponent().getCoordinateMapper().worldXtoLocalX(lastX);
		int ly = getBNAComponent().getCoordinateMapper().worldYtoLocalY(lastY);
		
		TimedStroke[] inputStrokes = new TimedStroke[]{stroke};
		stroke = null;
		lastX = -1;
		lastY = -1;
		
		if(inputStrokes[0].getVertexCount() < 15){
			if(inputStrokes[0].getVertexCount() < 2){
				return;
			}
			long lastTime = inputStrokes[0].getTimestamp(inputStrokes[0].getVertexCount() - 1);
			long firstTime = inputStrokes[0].getTimestamp(0);
			
			if(lastTime - firstTime < 1000){
				//Not enough data
				return;
			}
		}
		
		String gestureName = null;
		if(recognizeHorizontalWag(inputStrokes[0])){
			gestureName = "horizontalwag";
		}
		else if(recognizeVerticalWag(inputStrokes[0])){
			gestureName = "verticalwag";
		}
		else{
			inputStrokes = HHRecognizer.preprocess(inputStrokes, approx, interp, null);
			RecognitionSet rset = reco.sessionCompleted(inputStrokes);
			Recognition r = rset.getHighestValueRecognition();
			gestureName = r.getType().getID();
		}
		
		//System.out.println(gestureName);
		if(gestureName != null){
			ActionItem ai = (ActionItem)actionMap.get(gestureName);
			
			if(ai != null){
				String notification = 
					"Gesture: " + ai.gestureDisplayName + "\n" +
					"Function: " + ai.actionDisplayName;
				
				UserNotificationThing unt = new UserNotificationThing();
				unt.setLabel(notification);
				unt.setInitialPointLocal(new Point(5, 5));
			
				BNAUtils.setStackingPriority(unt, BNAUtils.STACKING_PRIORITY_ALWAYS_ON_TOP);
				getBNAComponent().getModel().addThing(unt);
			
				ai.action.invoke();		
			}
		}
	}

	protected void doCancelRecognizing(){
		stroke = null;
		lastX = -1;
		lastY = -1;
	}


	protected boolean isRecognizing = false;
	
	protected void beginRecognizing(){
		isRecognizing = true;
		doBeginRecognizing();
	}

	protected void cancelRecognizing(){
		isRecognizing = false;
		doCancelRecognizing();
	}
	
	protected void endRecognizing(){
		isRecognizing = false;
		doEndRecognizing();
	}
	
	public void shiftDown(){
		if(!isRecognizing){
			beginRecognizing();
		}
	}
	
	public void shiftUp(){
		if(isRecognizing){
			endRecognizing();
		}
	}
	
	protected boolean shiftIsDown = false;
	
	public void keyPressed(KeyEvent evt){
		if(evt.getKeyCode() == KeyEvent.VK_SHIFT){
			if(shiftIsDown){
				return;
			}
			shiftIsDown = true;
			shiftDown();
		}
		else{
			if(isRecognizing){
				cancelRecognizing();
			}
		}
	}

	public void keyReleased(KeyEvent evt){
		if(evt.getKeyCode() == KeyEvent.VK_SHIFT){
			shiftIsDown = false;		
			shiftUp();
		}
	}

	public void mousePressed(Thing t, MouseEvent evt, int worldX, int worldY){
		if(isRecognizing){
			cancelRecognizing();
		}
	}

	public void mouseReleased(Thing t, MouseEvent evt, int worldX, int worldY){
		if(isRecognizing){
			cancelRecognizing();
		}
	}

	protected static boolean recognizeHorizontalWag(TimedStroke ts){
		int count = ts.getVertexCount();
		if(count < 10) return false;
		
		int[] xcoords = new int[count];
		
		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;
		
		for(int i = 0; i < count; i++){
			int x = (int)ts.getX(i);
			int y = (int)ts.getY(i);
			xcoords[i] = x;
			
			if(x > maxX) maxX = x;
			if(x < minX) minX = x;
			
			if(y > maxY) maxY = y;
			if(y < minY) minY = y;			
		}
		
		int dx = maxX - minX;
		int dy = maxY - minY;
		
		if(dy == 0) dy = 1;
		
		//Check to see if the ratio of dx to dy is high
		if((dx / dy) < 7){
			//If it isn't, it's not a horizontal wag.
			return false;
		}
		
		//Check to see if they wagged more than a couple times
		
		int midX = minX + (dx / 2);
		int numWags = 0;
		
		boolean lastWasBelow = (xcoords[0] <= midX);
		for(int i = 1; i < count; i++){
			boolean wasBelow = (xcoords[i] <= midX);
			if(lastWasBelow != wasBelow){
				numWags++;
			}
			lastWasBelow = wasBelow;
		}
		if(numWags >= 3){
			return true;
		}
		else{
			return false;
		}
	}

	protected static boolean recognizeVerticalWag(TimedStroke ts){
		int count = ts.getVertexCount();
		if(count < 10) return false;
		
		int[] ycoords = new int[count];
		
		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		
		int minY = Integer.MAX_VALUE;
		int maxY = Integer.MIN_VALUE;
		
		for(int i = 0; i < count; i++){
			int x = (int)ts.getX(i);
			int y = (int)ts.getY(i);
			ycoords[i] = y;
			
			if(x > maxX) maxX = x;
			if(x < minX) minX = x;
			
			if(y > maxY) maxY = y;
			if(y < minY) minY = y;			
		}
		
		int dx = maxX - minX;
		int dy = maxY - minY;
		
		if(dx == 0) dx = 1;
		
		//Check to see if the ratio of dy to dx is high
		if((dy / dx) < 7){
			//If it isn't, it's not a vertical wag.
			return false;
		}
		
		//Check to see if they wagged more than a couple times
		
		int midY = minY + (dy / 2);
		int numWags = 0;
		
		boolean lastWasBelow = (ycoords[0] <= midY);
		for(int i = 1; i < count; i++){
			boolean wasBelow = (ycoords[i] <= midY);
			if(lastWasBelow != wasBelow){
				numWags++;
			}
			lastWasBelow = wasBelow;
		}
		if(numWags >= 3){
			return true;
		}
		else{
			return false;
		}
	}

	static class ActionItem{
		public String gestureName;
		public String gestureDisplayName;
		public String actionDisplayName;
		public ThingLogicAction action;
	}

}


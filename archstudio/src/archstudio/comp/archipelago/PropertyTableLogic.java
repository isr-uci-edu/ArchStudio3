package archstudio.comp.archipelago;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.*;

import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.floatingtable.*;
import edu.uci.ics.widgets.WidgetUtils;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchFileEvent;
import edu.uci.ics.xarchutils.XArchFlatEvent;

public class PropertyTableLogic extends ThingLogicAdapter implements MappingLogic{
	protected BNAModel bnaModel;
	protected ThingIDMap thingIDMap;

	protected Vector propertyTablePlugins;
	protected RefreshPropertyTableThread refreshPropertyTableThread;
	protected PopupTableThread popupTableThread;

	private boolean qdown = false;

	public PropertyTableLogic(BNAModel bnaModel, XArchFlatTransactionsInterface xarch, ThingIDMap thingIDMap){
		super();
		this.bnaModel = bnaModel;
		this.thingIDMap = thingIDMap;
		propertyTablePlugins = new Vector();
		refreshPropertyTableThread = new RefreshPropertyTableThread();
		refreshPropertyTableThread.start();
		popupTableThread = new PopupTableThread();
		popupTableThread.start();
	}

	public void destroy(){
		refreshPropertyTableThread.terminate();
		popupTableThread.terminate();	
	}
	
	public void addPropertyTablePlugin(PropertyTablePlugin p){
		this.propertyTablePlugins.addElement(p);
	}
	
	public void removePropertyTablePlugin(PropertyTablePlugin p){
		this.propertyTablePlugins.removeElement(p);
	}
	
	public void bnaModelChanged(BNAModelEvent evt){
		if(evt.getEventType() == BNAModelEvent.THING_REMOVING){
			Thing targetThing = evt.getTargetThing();
			if(targetThing != null){
				String targetThingID = targetThing.getID();
				//System.out.println("removing: " + targetThingID);
				FloatingTableThing ftt = (FloatingTableThing)pt.get(targetThingID);
				if(ftt != null){
					bnaModel.removeThing(ftt);
					pt.remove(targetThingID);
				}
			}
		}
	}

	public void handleXArchFileEvent(XArchFileEvent evt){
	}

	public void handleXArchFlatEvent(XArchFlatEvent evt){
		refreshPropertyTableThread.refresh();
	}

	public void keyPressed(KeyEvent evt){
		if(evt.getKeyChar() == 'q'){
			qdown = true;
		}
	}

	public void keyReleased(KeyEvent evt){
		if(evt.getKeyChar() == 'q'){
			qdown = false;
		}
	}
	
	public void focusLost(FocusEvent evt){
		qdown = false;
	}
	
	public void mouseClicked(Thing t, MouseEvent evt, int worldX, int worldY){
		if(t instanceof FloatingTableThing){
			if(qdown || ((evt.getClickCount() == 2) && (evt.getButton() == MouseEvent.BUTTON1))){
				String indicatedThingID = ((FloatingTableThing)t).getIndicatorThingId();
				if(indicatedThingID != null){
					Thing indicatedThing = bnaModel.getThing(indicatedThingID);
					if(indicatedThing != null){
						doQClick(indicatedThing);
					}
				}
			}
		}
		else if(qdown && (t != null)){
			doQClick(t);
		}
	}

	private HashMap pt = new HashMap();

	private FloatingTableThing createFloatingTableThing(Thing t){
		synchronized(propertyTablePlugins){
			TableData td = createTableData(t);
			if(td == null){
				return null;
			}
			FloatingTableThing ftt = new FloatingTableThing();
			ftt.setTransparency(0.58f);
			BNAUtils.setStackingPriority(ftt, BNAUtils.STACKING_PRIORITY_ALWAYS_ON_TOP);
			ftt.setTableData(td);
			return ftt;
		}
	}
	
	private TableData createTableData(Thing t){
		ObjRef tRef = thingIDMap.getXArchRef(t.getID());
		if(tRef == null){
			//no mapping!
			return null;
		}
		TableData td = new TableData();
		for(Iterator it = propertyTablePlugins.iterator(); it.hasNext(); ){
			PropertyTablePlugin ptp = (PropertyTablePlugin)it.next();
			ptp.addProperties(t, tRef, td);
		}
		return td;
	}
	
	protected synchronized void refreshOpenTables(){
		for(Iterator it = pt.keySet().iterator(); it.hasNext(); ){
			String targetThingID = (String)it.next();
			Thing targetThing = bnaModel.getThing(targetThingID);
			if(targetThing == null){
				continue;
			}
			
			FloatingTableThing ftt = (FloatingTableThing)pt.get(targetThingID);
			TableData oldTableData = ftt.getTableData();
			TableData newTableData = createTableData(targetThing);
			if(!oldTableData.equals(newTableData)){
				//We have to refresh this table.
				Dimension newSize = FloatingTableThingPeer.getPreferredSize(newTableData);
				Rectangle fttBoundingBox = new Rectangle(ftt.getBoundingBox());
				fttBoundingBox.height = newSize.height;
				fttBoundingBox.width = newSize.width;
				ftt.setBoundingBox(fttBoundingBox);
				ftt.setTableData(newTableData);
			}
		}
	}

	private synchronized void doQClick(Thing t){
		//System.out.println("qclick! " + t);
		String tID = t.getID();
		FloatingTableThing ftt = (FloatingTableThing)pt.get(tID);
		if(ftt == null){
			//No table for this guy.
			if(t instanceof IBoxBounded){
				ftt = createFloatingTableThing(t);
				if(ftt == null){
					BNAUtils.showUserNotificationUL(bnaModel, "No properties for that element.");
					return;
				}
				Rectangle boundingBox = ((IBoxBounded)t).getBoundingBox();
				int cx = boundingBox.x + (boundingBox.width / 2);
				int cy = boundingBox.y + (boundingBox.height / 2);
				Point indicatorPoint = new Point(cx, cy);
				ftt.setIndicatorThingId(t.getID());
				ftt.setIndicatorPoint(indicatorPoint);
				Dimension fttSize = FloatingTableThingPeer.getPreferredSize(ftt.getTableData());
				Rectangle fttBoundingBox = new Rectangle(cx + 10, cy + 10, fttSize.width, fttSize.height);
				PopupTableThreadTask task = new PopupTableThreadTask(tID, ftt,
					fttBoundingBox, true);
				popupTableThread.addTask(task);
			}
			pt.put(tID, ftt);
		}
		else{
			//There was a table for this guy.
			Point indicatorPoint = ftt.getIndicatorPoint();
			if(indicatorPoint == null){
				Rectangle boundingBox = ((IBoxBounded)t).getBoundingBox();
				int cx = boundingBox.x + (boundingBox.width / 2);
				int cy = boundingBox.y + (boundingBox.height / 2);
				indicatorPoint = new Point(cx, cy);
				ftt.setIndicatorPoint(indicatorPoint);
			}
			//ftt.setIndicatorThingId(t.getID());
			Dimension fttSize = FloatingTableThingPeer.getPreferredSize(ftt.getTableData());
			Rectangle fttBoundingBox = ftt.getBoundingBox();
			PopupTableThreadTask task = new PopupTableThreadTask(tID, ftt,
				fttBoundingBox, false);
			popupTableThread.addTask(task);
			pt.remove(tID);
		}
	}
	
	static class PopupTableThreadTask{
		private String targetThingID;
		private FloatingTableThing tableThing;
		private Rectangle targetBoundingBox;
		private int popupState;
		private float popupIncrement;
		
		public PopupTableThreadTask(String targetThingID,
		FloatingTableThing tableThing,
		Rectangle targetBoundingBox, boolean popup){
			this.targetThingID = targetThingID;
			this.tableThing = tableThing;
			this.targetBoundingBox = targetBoundingBox;
			if(popup){
				popupState = 0;
				popupIncrement = 1.0f;
			}
			else{
				popupState = 100;
				popupIncrement = -1.0f;
			}
		}
		
		public void bumpState(){
			popupState = popupState + Math.round(popupIncrement);
			popupIncrement = popupIncrement * 1.50f;
		}
		
		public String getTargetThingID(){
			return targetThingID;
		}

		public Point getIndicatorPoint(){
			return tableThing.getIndicatorPoint();
		}
		
		public void setPopupIncrement(float popupIncrement){
			this.popupIncrement = popupIncrement;
		}

		public float getPopupIncrement() {
			return popupIncrement;
		}

		public int getPopupState() {
			return popupState;
		}
		
		public void setPopupState(int popupState){
			this.popupState = popupState;
		}

		public FloatingTableThing getTableThing() {
			return tableThing;
		}

		public Rectangle getTargetBoundingBox() {
			return targetBoundingBox;
		}
	}
	
	class RefreshPropertyTableThread extends Thread{
		//The purpose of this thread is a little weird.
		//Basically its job is to kick off refreshing the
		//open property tables when a xADL event is received.
		//However, if we get four xADL events in rapid succession,
		//we don't want to refresh all the tables every four seconds.
		//So, what this thread does is set off a timer when a xADL event
		//is received, and then refreshes after a couple seconds, so if
		//more than one event is received, we'll refresh only once.
		private Object lock = new Object();
		private boolean doRefresh = false;
		private boolean terminate = false;
		
		public RefreshPropertyTableThread(){
			this.setDaemon(true);
		}
		
		public void refresh(){
			doRefresh = true;
			synchronized(lock){
				lock.notifyAll();
			}
		}
		
		public void terminate(){
			synchronized(lock){
				this.terminate = true;
				lock.notifyAll();
			}
			//this.interrupt();
		}

		public void run(){
			while(true){
				if(terminate){
					return;
				}
				if(doRefresh){
					refreshOpenTables();
					doRefresh = false;
				}
				try{
					synchronized(lock){
						lock.wait();
					}
					Thread.sleep(1500);
				}
				catch(InterruptedException e){
				}
			}
		}
	}

	class PopupTableThread extends Thread{
		private boolean isAnimating = false;
		private boolean shouldTerminate = false;
		
		private Object lock = new Object();
		private List taskList = Collections.synchronizedList(new ArrayList());
		
		public PopupTableThread(){
			this.setDaemon(true);
		}

		public boolean isAnimating(){
			return isAnimating;
		}
		
		public void terminate(){
			synchronized(lock){
				this.shouldTerminate = true;
				lock.notifyAll();
			}
		}
		
		
		private void addTask(PopupTableThreadTask newTask){
			synchronized(lock){
				String newTaskTargetThingID = newTask.getTargetThingID();
				boolean foundOldTask = false;
				PopupTableThreadTask[] oldTasks = (PopupTableThreadTask[])taskList.toArray(new PopupTableThreadTask[0]);
				for(int i = 0; i < oldTasks.length; i++){
					PopupTableThreadTask oldTask = oldTasks[i];
					String oldTaskTargetThingID = oldTask.getTargetThingID();
					if(oldTaskTargetThingID.equals(newTaskTargetThingID)){
						//This is a new task for the same target thing.
						foundOldTask = true;
						boolean otPos = oldTask.getPopupIncrement() > 0;
						boolean ntPos = newTask.getPopupIncrement() > 0;
						if(otPos && !ntPos){
							//We were popping up but are now popping down.
							oldTask.setPopupIncrement(newTask.getPopupIncrement());
							break;
						}
						else if(!otPos && ntPos){
							//We were popping down but are now popping up.
							//Remove the old ftt...
							bnaModel.removeThing(oldTask.getTableThing());
							//Add the new task with the new FTT, but set the new task's
							//popup state to the old one's
							newTask.setPopupState(oldTask.getPopupState());
							taskList.remove(oldTask);
							taskList.add(newTask);
							break;
						}
						else if(otPos && ntPos){
							//Eh?  We were popping up but are now popping up?
							bnaModel.removeThing(oldTask.getTableThing());
							//Add the new task with the new FTT, but set the new task's
							//popup state to the old one's
							newTask.setPopupState(oldTask.getPopupState());
							taskList.remove(oldTask);
							taskList.add(newTask);
							break;
						}
						else{ //!otPos && !ntPos
							//Eh? We were popping down and are still popping down.
							//Just do nothing...
						}
					}
				}
				
				if(!foundOldTask){
					//this is a brand new task
					taskList.add(newTask);
				}
				if(!isAnimating()){
					lock.notifyAll();
				}
			}
		}
		
		private void doTask(PopupTableThreadTask task, List doomedList){
			FloatingTableThing ftt = task.getTableThing();
			String fttID = ftt.getID();

			task.bumpState();
			int taskState = task.getPopupState();
			//Handle task terminating cases
			if(taskState <= 0){
				bnaModel.removeThing(ftt);
				doomedList.add(task);
				return;
			}
			else if(taskState >= 100){
				ftt.setBoundingBox(task.getTargetBoundingBox());
				doomedList.add(task);
				return;
			}
			
			if(bnaModel.getThing(fttID) == null){
				//This ftt is not part of the model, let's add it.
				String tID = ftt.getIndicatorThingId();
				Thing t = bnaModel.getThing(tID);
				bnaModel.addThing(ftt/*, t*/);
			}
			
			//Now we have to calculate the bounding box of the thing
			//per its task state, indicator point, and target bounding box.
			
			//There are two lines to consider...the line connecting the 
			//indicator point to the target bounding box's upper left
			//corner and the line connecting the indicator point to
			//the target bounding box's lower right corner.  We will
			//calculate the distance along these lines using the
			//percentage specified in the tasks's popup state.
			Point ip = task.getIndicatorPoint();
			Rectangle targetBoundingBox = task.getTargetBoundingBox();
			Point ulp = new Point(targetBoundingBox.x, targetBoundingBox.y);
			Point lrp = new Point(targetBoundingBox.x + targetBoundingBox.width,
				targetBoundingBox.y + targetBoundingBox.height);
			
			double pct = task.getPopupState() / 100.0d;
			
			int ulLength = (int)WidgetUtils.calcLineLength(ip.x, ip.y, ulp.x, ulp.y);
			int ulDist = (int)Math.round(((double)ulLength) * pct);
			int lrLength = (int)WidgetUtils.calcLineLength(ip.x, ip.y, lrp.x, lrp.y);
			int lrDist = (int)Math.round(((double)lrLength) * pct);
			
			Point nulp = WidgetUtils.calcPointOnLineAtDist(ip, ulp, ulDist);
			Point nlrp = WidgetUtils.calcPointOnLineAtDist(ip, lrp, lrDist);
			
			ftt.setBoundingBox(nulp.x, nulp.y, nlrp.x - nulp.x, nlrp.y - nulp.y);
		}
		
		public void run(){
			synchronized(lock){
				while(true){
					if(shouldTerminate) return;
					List doomedList = new ArrayList();
					while(taskList.size() > 0){
						isAnimating = true;
						try{
							bnaModel.beginBulkChange();
							for(Iterator it = taskList.iterator(); it.hasNext(); ){
								PopupTableThreadTask t = (PopupTableThreadTask)it.next();
								doTask(t, doomedList);
							}
							for(Iterator it = doomedList.iterator(); it.hasNext(); ){
								taskList.remove(it.next());
							}
						}finally{
							bnaModel.endBulkChange();
						}
						if(taskList.size() > 0){
							try{
								lock.wait(33);
							}
							catch(InterruptedException ie){
							}
						}
						isAnimating = false;
					}
					try{
						lock.wait();
					}
					catch(InterruptedException e){
						if(shouldTerminate){
							return;
						}
						else{
							continue;
						}
					}
				}
			}
		}
	}
	
	public static PropertyTableLogic getPropertyTableLogic(BNAComponent bnaComponent){
		MappingLogic[] mls = AbstractArchipelagoTreePlugin.getAllMappingLogics(bnaComponent);
		for(int i = 0; i < mls.length; i++){
			if(mls[i] instanceof PropertyTableLogic){
				return (PropertyTableLogic)mls[i];
			}
		}
		return null;
	}
}

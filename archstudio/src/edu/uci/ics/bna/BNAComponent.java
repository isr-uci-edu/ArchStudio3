package edu.uci.ics.bna;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import javax.swing.*;
import java.util.*;

public class BNAComponent extends JComponent implements BNAModelListener, CoordinateMapperListener{
	protected BNAModel model;
	protected CoordinateMapper cm;
	protected PeerCache peerCache;
	
	protected String id;
	
	protected int lastMouseX, lastMouseY;
	
	public BNAComponent(String id, BNAModel model){
		setLayout(null);
		this.id = id;
		this.model = model;
		model.addBNAModelListener(this);
		this.cm = new DefaultCoordinateMapper();
		cm.addCoordinateMapperListener(this);
		peerCache = new PeerCache(this);
		
		BNAMouseAdapter mouseAdapter = new BNAMouseAdapter();
		this.addMouseListener(mouseAdapter);
		this.addMouseMotionListener(mouseAdapter);
		BNAKeyAdapter keyAdapter = new BNAKeyAdapter();
		this.addKeyListener(keyAdapter);
		BNAFocusAdapter focusAdapter = new BNAFocusAdapter();
		this.addFocusListener(focusAdapter);
		
		this.setFocusable(true);
		this.setRequestFocusEnabled(true);
		this.setOpaque(true);
		this.applyEnvironmentProperties();
	}
	
	protected void applyEnvironmentProperties(){
		EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(model);
		if(ept != null){
			repositionAbsolute(ept.getWorldOriginX(), ept.getWorldOriginY());
			rescaleAbsolute(ept.getScale());
		}
	}
	
	protected Map propertyMap = new HashMap();
	
	public void setProperty(String propertyName, Object value){
		synchronized(propertyMap){
			propertyMap.put(propertyName, value);
		}
	}
	
	public Object getProperty(String propertyName){
		synchronized(propertyMap){
			return propertyMap.get(propertyName);
		}
	}
	
	public Map getProperties(){
		synchronized(propertyMap){
			return Collections.unmodifiableMap(propertyMap);
		}
	}
	
	public void removeProperty(String propertyName){
		synchronized(propertyMap){
			propertyMap.remove(propertyName);
		}
	}
	
	public String getID(){
		return id;
	}
	
	public BNAModel getModel(){
		return model;
	}
	
	public CoordinateMapper getCoordinateMapper(){
		return cm;
	}
	
	public void setPeerCache(PeerCache peerCache){
		this.peerCache = peerCache;
	}
	
	public PeerCache getPeerCache(){
		return peerCache;
	}
	
	public ThingPeer createPeer(Thing th){
		return peerCache.createPeer(th);
	}
	
	public ThingPeer getPeer(Thing th){
		return peerCache.getPeer(th);
	}	
	
	public void destroy(){
		ThingLogic[] arr = logicArray;
		for(int i = 0; i < arr.length; i++){
			removeThingLogic(arr[i]);
		}
		cm.removeCoordinateMapperListener(this);
		model.removeBNAModelListener(this);
	}
	
	private synchronized void clearLocalBoundingBoxMap(){
		boundingBoxMap.clear();
	}
	
	public synchronized void repositionRelative(int dx, int dy){
		clearLocalBoundingBoxMap();
		((DefaultCoordinateMapper)cm).repositionRelative(dx, dy);
	}
	
	public synchronized void repositionAbsolute(int newOriginX, int newOriginY){
		clearLocalBoundingBoxMap();
		((DefaultCoordinateMapper)cm).repositionAbsolute(newOriginX, newOriginY);
	}
	
	public void rescaleAbsolute(double newScale){
		synchronized(this){
			clearLocalBoundingBoxMap();
			int lcx = getWidth() / 2;
			int lcy = getHeight() / 2;
			int worldCenterX = cm.localXtoWorldX(lcx);
			int worldCenterY = cm.localYtoWorldY(lcy);
			
			((DefaultCoordinateMapper)cm).rescaleAbsolute(newScale);
			
			int newWorldCenterX = cm.localXtoWorldX(lcx);
			int newWorldCenterY = cm.localYtoWorldY(lcy);
			int dwcx = newWorldCenterX - worldCenterX;
			int dwcy = newWorldCenterY - worldCenterY;
			repositionRelative(-dwcx, -dwcy);
		}
	}
	
	public void rescaleRelative(double ds){
		synchronized(this){
			clearLocalBoundingBoxMap();
			int lcx = getWidth() / 2;
			int lcy = getHeight() / 2;
			int worldCenterX = cm.localXtoWorldX(lcx);
			int worldCenterY = cm.localYtoWorldY(lcy);
			
			((DefaultCoordinateMapper)cm).rescaleRelative(ds);
			
			int newWorldCenterX = cm.localXtoWorldX(lcx);
			int newWorldCenterY = cm.localYtoWorldY(lcy);
			int dwcx = newWorldCenterX - worldCenterX;
			int dwcy = newWorldCenterY - worldCenterY;
			repositionRelative(-dwcx, -dwcy);
		}
	}
	
	public int getWorldOriginX(){
		return cm.localXtoWorldX(0);
	}
	
	public int getWorldOriginY(){
		return cm.localYtoWorldY(0);
	}
	
	public double getScale(){
		return cm.getScale();
	}
	
	protected int holdPaintingLevel = 0;
	protected boolean holdPaintingDirtyAll;
	protected Rectangle holdPaintingDirtyRectangle;
	protected HashMap boundingBoxMap = new HashMap();

	public synchronized void bnaModelChanged(BNAModelEvent evt){
		if(evt.getEventType() == BNAModelEvent.STREAM_NOTIFICATION_EVENT){
			return;
		}
		else if(evt.getEventType() == BNAModelEvent.BULK_CHANGE_BEGIN){
			holdPaintingLevel++;
			if(holdPaintingLevel == 1){
				holdPaintingDirtyAll = false;
				holdPaintingDirtyRectangle = null; 
			}
			return;
		}
		else if(evt.getEventType() == BNAModelEvent.BULK_CHANGE_END){
			holdPaintingLevel--;
			if(holdPaintingLevel == 0){
				if(holdPaintingDirtyAll){
					repaint();
				}
				else{
					if(holdPaintingDirtyRectangle != null){
						repaint(holdPaintingDirtyRectangle.x,
							holdPaintingDirtyRectangle.y,
							holdPaintingDirtyRectangle.width + 1,
							holdPaintingDirtyRectangle.height + 1);
					}
				}
			}
			return;
		}
		else if(evt.getEventType() == BNAModelEvent.THING_ADDED){
			Thing t = evt.getTargetThing();
			if(t != null){
				String tID = t.getID();
				ThingPeer peer = getPeer(t);
				Rectangle newBoundingBox = 
					peer.getLocalBoundingBox((Graphics2D)this.getGraphics(), getCoordinateMapper());
				if(newBoundingBox != null) newBoundingBox = new Rectangle(newBoundingBox);
				boundingBoxMap.put(tID, newBoundingBox);
			}
		}
		else if(evt.getEventType() == BNAModelEvent.THING_REMOVED){
			Thing removedThing = evt.getTargetThing();
			boundingBoxMap.remove(removedThing.getID());
		}
		
		Thing t = evt.getTargetThing();
		Rectangle oldBoundingBox = null;
		Rectangle newBoundingBox = null;
		String tID = t.getID();
		oldBoundingBox = (Rectangle)boundingBoxMap.get(tID);
		
		ThingPeer peer = getPeer(t);
		newBoundingBox = peer.getLocalBoundingBox((Graphics2D)this.getGraphics(), getCoordinateMapper());
		if(newBoundingBox == null){
			boundingBoxMap.remove(tID);
		}
		else{
			if(evt.getEventType() != BNAModelEvent.THING_REMOVED){
				boundingBoxMap.put(tID, new Rectangle(newBoundingBox));
			}
		}
		
		Rectangle r = null;
		if((oldBoundingBox != null) && (newBoundingBox == null)){
			//System.out.println("not-full paint");
			r = oldBoundingBox;
		}
		else if((oldBoundingBox == null) && (newBoundingBox != null)){
			//r = new Rectangle(newBoundingBox);
			//We have to force a whole-repaint here because we don't
			//know where the element was.  The next time around though
			//we'll know.
			//System.out.println("full paint");
			r = null;
		}
		else if((oldBoundingBox != null) && (newBoundingBox != null)){
			//System.out.println("not-full paint");
			r = oldBoundingBox.union(newBoundingBox);
		}
			
		if(r != null){
			//System.out.println("repaint: " + r.x + "," + r.y + "," + r.width + "," + r.height);
			if(holdPaintingLevel > 0){
				if(holdPaintingDirtyRectangle == null){
					holdPaintingDirtyRectangle = new Rectangle(r.x, r.y, r.width + 1, r.height + 1);
				}
				else{
					holdPaintingDirtyRectangle = holdPaintingDirtyRectangle.union(new Rectangle(r.x, r.y, r.width + 1, r.height + 1));
				}
			}
			else{
				repaint(0L, r.x, r.y, r.width + 1, r.height + 1);
			}
		}
		else{
			if(holdPaintingLevel > 0){
				holdPaintingDirtyAll = true;
			}
			else{
				repaint();
			}
		}
	}
	
	protected boolean shouldAntialiasText = false;
	protected boolean shouldAntialiasGraphics = false;
	protected boolean shouldGradientGraphics = false;
	
	public void setAntialiasText(boolean antialiasText){
		this.shouldAntialiasText = antialiasText;
		repaint();
	}
	
	public boolean shouldAntialiasText(){
		return this.shouldAntialiasText;
	}
	
	public void setAntialiasGraphics(boolean antialiasGraphics){
		this.shouldAntialiasGraphics = antialiasGraphics;
		repaint();
	}
	
	public boolean shouldAntialiasGraphics(){
		return this.shouldAntialiasGraphics;
	}
	
	public void setGradientGraphics(boolean gradientGraphics){
		this.shouldGradientGraphics = gradientGraphics;
	}
	
	
	public boolean shouldGradientGraphics(){
		return shouldGradientGraphics;
	}
	
	public synchronized void repaint(){
		//This if is here because repaint() should never get called if
		//painting is held.  If it did, it got called from somewhere else
		//(e.g. a coordinate mapper event, the OS, some other code)
		//and so we just dirty everything because we'll eventually get an 
		//end bulk change event and repaint the whole thing.
		if(holdPaintingLevel > 0){
			holdPaintingDirtyAll = true;
			return;
		}
		super.repaint();
	}
	
	public void repaint(long tm, int x, int y, int w, int h){
		//This if is here because repaint(...) should never get called if
		//painting is held.  If it did, it got called from somewhere else
		//(e.g. a coordinate mapper event, the OS, some other code)
		//and so we just dirty the region because we'll eventually get an 
		//end bulk change event and repaint the dirty region then.
		if(holdPaintingLevel > 0){
			if(holdPaintingDirtyRectangle == null){
				holdPaintingDirtyRectangle = new Rectangle(x, y, w + 1, h + 1);
			}
			else{
				holdPaintingDirtyRectangle = holdPaintingDirtyRectangle.union(new Rectangle(x, y, w + 1, h + 1));
			}
			return;
		}
		super.repaint(tm, x, y, w, h);
	}
	
	public synchronized void paintComponent(Graphics g){
		//if(dontPaint) return;
		Graphics2D g2d = (Graphics2D)g.create();
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	
		Rectangle clip = g.getClipBounds();
		
		//The following is necessary because intersects() doesn't work
		//right on right-left abutting borders.
		clip.x--;
		clip.y--;
		clip.width++;
		clip.height++;
		//System.out.println("clip: " + clip);
	
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			shouldAntialiasGraphics ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
			shouldAntialiasText ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
	
		if(isOpaque()){
			g2d.setPaint(getBackground());
			g2d.fillRect(clip.x, clip.y, clip.width, clip.height);
		}

		/*
		visibleRectWorld.x = cm.localXtoWorldX(clip.x);
		visibleRectWorld.y = cm.localYtoWorldY(clip.y);
		int wx2 = cm.localXtoWorldX(clip.x + clip.width);
		int wy2 = cm.localYtoWorldY(clip.y + clip.height);
		visibleRectWorld.width = wx2 - visibleRectWorld.x;
		visibleRectWorld.height = wy2 - visibleRectWorld.y;
		*/
	
		synchronized(model.getLock()){
			for(Iterator it = model.getThingIterator(); it.hasNext(); ){
				Thing th = (Thing)it.next();
				//Don't draw elements outside the clip
		
				ThingPeer peer = getPeer(th);

				//See if the Thing has a 'don't optimize' flag on:
				if(!((th instanceof IDrawnOffscreen) && (((IDrawnOffscreen)th).shouldDrawEvenIfOffscreen()))){
					//OK, we're allowed to optimize the Thing
					Rectangle localBoundingBox = peer.getLocalBoundingBox(g2d, cm);
					if(localBoundingBox != null){
						if(!clip.intersects(localBoundingBox)){
							continue;
						}
					}
				}
				peer.draw(g2d, cm);
			}
		}
	}
	
	public synchronized Thing getThingAt(int lx, int ly){
		int wx = cm.localXtoWorldX(lx);
		int wy = cm.localYtoWorldY(ly);
		
		synchronized(model.getLock()){
			for(ListIterator it = model.getThingListIterator(model.getNumThings()); it.hasPrevious(); ){
				Thing th = (Thing)it.previous();
				ThingPeer peer = getPeer(th);
				if(peer.isInThing((Graphics2D)getGraphics(), cm, wx, wy)){
					return th;
				}
			}
		}
		return null;
	}
	
	public void coordinateMappingsChanged(CoordinateMapperEvent evt){
		repaint();
	}
	
	protected ThingLogic[] logicArray = new ThingLogic[0];
	
	public void addThingLogic(ThingLogic l){
		ArrayList list = new ArrayList(Arrays.asList(logicArray));
		l.setComponent(this);
		getModel().addBNAModelListener(l);
		list.add(l);
		logicArray = (ThingLogic[])list.toArray(new ThingLogic[0]);
	}
	
	public void removeThingLogic(ThingLogic l){
		ArrayList list = new ArrayList(Arrays.asList(logicArray));
		l.setComponent(null);
		getModel().removeBNAModelListener(l);
		list.remove(l);
		logicArray = (ThingLogic[])list.toArray(new ThingLogic[0]);
	}
	
	public ThingLogic[] getThingLogics(){
		return logicArray;
	}
	
	protected void fireMouseClickedLogicEvent(Thing t, MouseEvent evt, int worldX, int worldY){
		ThingLogic[] arr = logicArray;
		for(int i = 0; i < arr.length; i++){
			arr[i].mouseClicked(t, evt, worldX, worldY);
		}
	}
	
	protected void fireMousePressedLogicEvent(Thing t, MouseEvent evt, int worldX, int worldY){
		ThingLogic[] arr = logicArray;
		for(int i = 0; i < arr.length; i++){
			arr[i].mousePressed(t, evt, worldX, worldY);
		}
	}
	
	protected void fireMouseReleasedLogicEvent(Thing t, MouseEvent evt, int worldX, int worldY){
		ThingLogic[] arr = logicArray;
		for(int i = 0; i < arr.length; i++){
			arr[i].mouseReleased(t, evt, worldX, worldY);
		}
	}
	
	protected void fireMouseDraggedLogicEvent(Thing t, MouseEvent evt, int worldX, int worldY){
		ThingLogic[] arr = logicArray;
		for(int i = 0; i < arr.length; i++){
			arr[i].mouseDragged(t, evt, worldX, worldY);
		}
	}
	
	protected void fireMouseMovedLogicEvent(Thing t, MouseEvent evt, int worldX, int worldY){
		ThingLogic[] arr = logicArray;
		for(int i = 0; i < arr.length; i++){
			arr[i].mouseMoved(t, evt, worldX, worldY);
		}
	}
	
	protected void fireMouseEnteredLogicEvent(Thing t, MouseEvent evt, int worldX, int worldY){
		ThingLogic[] arr = logicArray;
		for(int i = 0; i < arr.length; i++){
			arr[i].mouseEntered(t, evt, worldX, worldY);
		}
	}
	
	protected void fireMouseExitedLogicEvent(Thing t, MouseEvent evt, int worldX, int worldY){
		ThingLogic[] arr = logicArray;
		for(int i = 0; i < arr.length; i++){
			arr[i].mouseExited(t, evt, worldX, worldY);
		}
	}
	
	protected void fireKeyPressedLogicEvent(KeyEvent evt){
		ThingLogic[] arr = logicArray;
		for(int i = 0; i < arr.length; i++){
			arr[i].keyPressed(evt);
		}
	}

	protected void fireKeyReleasedLogicEvent(KeyEvent evt){
		ThingLogic[] arr = logicArray;
		for(int i = 0; i < arr.length; i++){
			arr[i].keyReleased(evt);
		}
	}

	protected void fireKeyTypedLogicEvent(KeyEvent evt){
		ThingLogic[] arr = logicArray;
		for(int i = 0; i < arr.length; i++){
			arr[i].keyTyped(evt);
		}
	}
	
	protected void fireFocusGainedLogicEvent(FocusEvent evt){
		ThingLogic[] arr = logicArray;
		for(int i = 0; i < arr.length; i++){
			arr[i].focusGained(evt);
		}
	}

	protected void fireFocusLostLogicEvent(FocusEvent evt){
		ThingLogic[] arr = logicArray;
		for(int i = 0; i < arr.length; i++){
			arr[i].focusLost(evt);
		}
	}
	
	public Thing getDNDThing(){
		//System.out.println("grabbed " + getThingAt(lastMouseX, lastMouseY) + " from " + getID());
		Thing t = getThingAt(lastMouseX, lastMouseY);
		t.setProperty("#dndsourceid", getID());
		return t;
	}
	
	public void setDNDThing(Thing t){
		//System.out.println("dropped " + t + " on " + getID() + "at (" + lastMouseX + "," + lastMouseY + ")");
	}

	class BNAFocusAdapter extends FocusAdapter implements FocusListener{
		public void focusGained(FocusEvent evt){
			fireFocusGainedLogicEvent(evt);
		}

		public void focusLost(FocusEvent evt){
			fireFocusLostLogicEvent(evt);
		}
	}

	class BNAKeyAdapter extends KeyAdapter implements KeyListener{
		public void keyPressed(KeyEvent evt){
			fireKeyPressedLogicEvent(evt);
		}

		public void keyReleased(KeyEvent evt){
			fireKeyReleasedLogicEvent(evt);
		}

		public void keyTyped(KeyEvent evt){
			fireKeyTypedLogicEvent(evt);
		}
	}
	
	class BNAMouseAdapter extends MouseAdapter implements MouseMotionListener{
		public void mouseClicked(MouseEvent evt){
			lastMouseX = evt.getX();
			lastMouseY = evt.getY();
			Thing clickedOn = getThingAt(evt.getX(), evt.getY());
			int worldX = cm.localXtoWorldX(evt.getX());
			int worldY = cm.localYtoWorldY(evt.getY());
			fireMouseClickedLogicEvent(clickedOn, evt, worldX, worldY);
		}
		
		public void mousePressed(MouseEvent evt){
			lastMouseX = evt.getX();
			lastMouseY = evt.getY();
			Thing clickedOn = getThingAt(evt.getX(), evt.getY());
			int worldX = cm.localXtoWorldX(evt.getX());
			int worldY = cm.localYtoWorldY(evt.getY());
			requestFocus();
			fireMousePressedLogicEvent(clickedOn, evt, worldX, worldY);
		}
		
		public void mouseReleased(MouseEvent evt){
			lastMouseX = evt.getX();
			lastMouseY = evt.getY();
			Thing clickedOn = getThingAt(evt.getX(), evt.getY());
			int worldX = cm.localXtoWorldX(evt.getX());
			int worldY = cm.localYtoWorldY(evt.getY());
			requestFocus();
			fireMouseReleasedLogicEvent(clickedOn, evt, worldX, worldY);
		}
		
		public void mouseDragged(MouseEvent evt){
			lastMouseX = evt.getX();
			lastMouseY = evt.getY();
			Thing clickedOn = getThingAt(evt.getX(), evt.getY());
			int worldX = cm.localXtoWorldX(evt.getX());
			int worldY = cm.localYtoWorldY(evt.getY());
			fireMouseDraggedLogicEvent(clickedOn, evt, worldX, worldY);
		}
		
		public void mouseMoved(MouseEvent evt){
			lastMouseX = evt.getX();
			lastMouseY = evt.getY();
			Thing clickedOn = getThingAt(evt.getX(), evt.getY());
			int worldX = cm.localXtoWorldX(evt.getX());
			int worldY = cm.localYtoWorldY(evt.getY());
			fireMouseMovedLogicEvent(clickedOn, evt, worldX, worldY);
		}

		public void mouseEntered(MouseEvent evt){
			lastMouseX = evt.getX();
			lastMouseY = evt.getY();
			Thing clickedOn = getThingAt(evt.getX(), evt.getY());
			int worldX = cm.localXtoWorldX(evt.getX());
			int worldY = cm.localYtoWorldY(evt.getY());
			fireMouseEnteredLogicEvent(clickedOn, evt, worldX, worldY);
		}

		public void mouseExited(MouseEvent evt){
			lastMouseX = evt.getX();
			lastMouseY = evt.getY();
			Thing clickedOn = null;
			int worldX = cm.localXtoWorldX(evt.getX());
			int worldY = cm.localYtoWorldY(evt.getY());
			fireMouseExitedLogicEvent(clickedOn, evt, worldX, worldY);
		}
	}
	
	
}

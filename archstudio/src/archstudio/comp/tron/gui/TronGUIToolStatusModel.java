package archstudio.comp.tron.gui;

import archstudio.tron.TronToolStatusMessage;

import java.util.*;

public class TronGUIToolStatusModel{

	//Maps Tool ID strings to TronToolStatusMessages
	protected Map toolStatusMap = Collections.synchronizedMap(new HashMap());

	public TronGUIToolStatusModel(){
	}
	
	public synchronized String[] getAllToolIDs(){
		return (String[])toolStatusMap.keySet().toArray(new String[0]);
	}
	
	public synchronized TronToolStatusMessage[] getAllToolStatuses(){
		return (TronToolStatusMessage[])toolStatusMap.values().toArray(new TronToolStatusMessage[0]);
	}
	
	public synchronized TronToolStatusMessage getToolStatus(String toolID){
		return (TronToolStatusMessage)toolStatusMap.get(toolID);
	}
	
	public synchronized void handleToolStatus(TronToolStatusMessage m){
		String toolID = m.getToolID();
		String toolStatus = m.getStatus();
		if(toolStatus.equals(TronToolStatusMessage.ENDING_STATUS)){
			toolStatusMap.remove(toolID);
		}
		else{
			toolStatusMap.put(toolID, m);
		}
		fireModelChangedEvent();
	}
	
	protected Vector listeners = new Vector();
	
	public void addTronGUIToolStatusModelListener(TronGUIToolStatusModelListener l){
		listeners.addElement(l);
	}
	
	public void removeTronGUIToolStatusModelListener(TronGUIToolStatusModelListener l){
		listeners.removeElement(l);
	}
	
	protected void fireModelChangedEvent(){
		synchronized(listeners){
			for(Iterator it = listeners.iterator(); it.hasNext(); ){
				((TronGUIToolStatusModelListener)it.next()).modelChanged(this);
			}
		}
	}
}

package archstudio.comp.tron.gui;

import archstudio.tron.*;

import java.util.*;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;
//import java.awt.event.*;
import javax.swing.*;

import edu.uci.ics.widgets.JPanelIS;
import edu.uci.ics.widgets.WidgetUtils;

public class TronGUIToolStatusPanel extends JPanel implements TronGUIToolStatusModelListener{

	protected TronGUIToolStatusModel model;
	
	protected java.util.List paneList = new ArrayList();
	
	public TronGUIToolStatusPanel(TronGUIToolStatusModel model){
		this.model = model;
		model.addTronGUIToolStatusModelListener(this);
		syncPanel();
	}
	
	public void syncPanel(){
		synchronized(model){
			String[] toolIDs = model.getAllToolIDs();
			for(int i = 0; i < toolIDs.length; i++){
				ToolStatusPane pane = getStatusPane(toolIDs[i]);
				if(pane == null){
					pane = new ToolStatusPane(toolIDs[i]);
					addStatusPane(pane);
				}
				TronToolStatusMessage m = model.getToolStatus(toolIDs[i]);
				if(m != null){
					pane.setToolStatus(m.getStatus());
					pane.setProgressPercent(m.getProgressPercent());
				}
			}
			
			ToolStatusPane[] allStatusPanes = getAllStatusPanes();
			toolIDs = model.getAllToolIDs();
			for(int i = 0; i < allStatusPanes.length; i++){
				boolean found = false;
				for(int j = 0; j < toolIDs.length; j++){
					if(allStatusPanes[i].getToolID().equals(toolIDs[j])){
						found = true;
						break;
					}
				}
				if(!found){
					removeStatusPane(allStatusPanes[i].getToolID());
				}
			}
		}
	}
	
	private void addStatusPane(ToolStatusPane newPane){
		synchronized(paneList){
			paneList.add(newPane);
			Collections.sort(paneList);
			refreshPanel();
		}
	}
	
	private void removeStatusPane(String toolID){
		synchronized(paneList){
			ToolStatusPane markedForDeath = null;
			for(Iterator it = paneList.iterator(); it.hasNext(); ){
				ToolStatusPane p = (ToolStatusPane)it.next();
				if(p.getToolID().equals(toolID)){
					markedForDeath = p;
					break;
				}
			}
			paneList.remove(markedForDeath);
			refreshPanel();
		}
	}
	
	private void refreshPanel(){
		synchronized(paneList){
			//System.out.println("refreshing: " + paneList.size());
			this.removeAll();
			this.setLayout(new GridLayout(paneList.size(), 1));
			for(Iterator it = paneList.iterator(); it.hasNext(); ){
				ToolStatusPane p = (ToolStatusPane)it.next();
				this.add(p);
			}
			WidgetUtils.validateAndRepaintInAWTThread(this);
		}
	}
	
	private ToolStatusPane getStatusPane(String toolID){
		synchronized(paneList){
			for(Iterator it = paneList.iterator(); it.hasNext(); ){
				ToolStatusPane p = (ToolStatusPane)it.next();
				if(p.getToolID().equals(toolID)){
					return p;
				}
			}
			return null;
		}
	}
	
	private ToolStatusPane[] getAllStatusPanes(){
		synchronized(paneList){
			return (ToolStatusPane[])paneList.toArray(new ToolStatusPane[0]);
		}
	}
	
	public void modelChanged(TronGUIToolStatusModel src){
		syncPanel();
	}	
	
	private static class ToolStatusPane extends JPanel implements Comparable{
		private JLabel label;
		private JProgressBar pb;
		
		private String toolID = "unknown";
		private String toolStatus = "unknown";
		private int progressPercent = 0;
		
		public ToolStatusPane(String toolID){
			this.toolID = toolID;
			this.setLayout(new BorderLayout());
			label = new JLabel();
			pb = new JProgressBar();
			pb.setMinimum(0);
			pb.setMaximum(100);
			
			this.add("West", new JPanelIS(label, new Insets(0, 0, 0, 5)));
			this.add("Center", pb);
			
			updatePane();
		}
		
		public Insets getInsets(){
			return new Insets(2, 4, 2, 4);
		}
		
		public int compareTo(Object o){
			if(!(o instanceof ToolStatusPane)){
				throw new IllegalArgumentException();
			}
			return toolID.compareTo(((ToolStatusPane)o).getToolID());
		}
		
		public String getToolID(){
			return toolID;
		}
		
		public void setToolStatus(String toolStatus){
			this.toolStatus = toolStatus;
			updatePane();
		}
		
		public void setProgressPercent(int progressPercent){
			this.progressPercent = progressPercent;
			updatePane();
		}
		
		public void updatePane(){
			label.setText("<html><div style=\"font-weight: normal\"><b>" + 
				toolID + ":</b> " + toolStatus + "</div></html>");
			if(progressPercent == -1){
				pb.setVisible(false);
			}
			else{
				pb.setVisible(true);
				pb.setValue(progressPercent);
			}
			WidgetUtils.validateAndRepaintInAWTThread(this);
		}
	}
}

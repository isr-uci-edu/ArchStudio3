package archstudio.comp.criticgui;

import archstudio.invoke.*;
import archstudio.critics.*;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

import edu.uci.ics.widgets.*;
import edu.uci.ics.xarchutils.*;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class CriticGUIC2Component extends AbstractC2DelegateBrick implements InvokableBrick, MessageListener{

	protected Vector artistMessages = new Vector();
	protected DefaultTableModel tableModel;
	protected CriticGUIFrame guiFrame = null;
	
	public CriticGUIC2Component(Identifier id){
		super(id);
		tableModel = new DefaultTableModel();
		tableModel.addColumn("CategoryLabel");
		tableModel.addColumn("IssueDetails");
		addMessageProcessor(new CriticGUIMessageProcessor());
		InvokeUtils.deployInvokableService(this, bottomIface, "Critic GUI", 
			"Critic User Interface for ArchStudio 3.");
	}

	public void invoke(InvokeMessage m){
		//Let's open a new window.
		newWindow();
	}
	
	public void closeWindow(){
		if(guiFrame != null){
			guiFrame.setVisible(false);
			guiFrame.dispose();
			guiFrame = null;
		}
	}
	
	public void newWindow(){
		//This makes sure we only have one active window open.
		if(guiFrame == null){
			guiFrame = new CriticGUIFrame();
		}
		else{
			guiFrame.requestFocus();
		}
	}
	
	public void messageSent(Message m){
		//System.out.println("Critic gui got message: " + m);
		sendToAll(m, bottomIface);
	}
	
	class CriticGUILifecycleProcessor extends c2.fw.LifecycleAdapter{
		public void begin(){
			sendToAll(new CriticGetIssuesMessage(), topIface);
		}
		
		public void end(){
			closeWindow();
		}
	}
	
	protected static CriticArtistMessage getUnrenderedCriticArtistMessage(CriticIssue issue){
		return new CriticArtistMessage(issue, new JLabel("[Rendering Issue...]"), new JLabel(issue.getHeadline()), CriticArtistMessage.FIDELITY_UNRENDERED);
	}
	
	class CriticGUIMessageProcessor implements MessageProcessor{
		//Keeps the critic issues in the order they
		//are rendered in the table.
		protected Vector issuesByRow = new Vector();
		/*
		protected void dumpIssuesByRow(){
			for(int i = 0; i < issuesByRow.size(); i++){
				String row = ((CriticIssue)issuesByRow.elementAt(i)).getHeadline();
				System.out.println(row);
			}
			System.out.println();
		}
		*/
		
		public synchronized void handle(Message m){
			if(m instanceof CriticArtistMessage){
				CriticArtistMessage cam = (CriticArtistMessage)m;
				CriticIssue issue = cam.getIssue();
				
				for(int i = 0; i < artistMessages.size(); i++){
					CriticArtistMessage ecam = (CriticArtistMessage)artistMessages.elementAt(i);
					if(ecam.getIssue().equals(issue)){
						//Replace the existing issue representation with a new one.
						if(ecam.getFidelity() < cam.getFidelity()){
							artistMessages.setElementAt(cam, i);
							if(cam.getLabelComponent() instanceof MessageProvider){
								((MessageProvider)cam.getLabelComponent()).addMessageListener(CriticGUIC2Component.this);
							}
							if(cam.getContentComponent() instanceof MessageProvider){
								((MessageProvider)cam.getContentComponent()).addMessageListener(CriticGUIC2Component.this);
							}
							for(int j = 0; j < issuesByRow.size(); j++){
								CriticIssue rowIssue = (CriticIssue)issuesByRow.elementAt(j);
								if(issue.equals(rowIssue)){
									tableModel.setValueAt(cam.getLabelComponent(), j, 0);
									tableModel.setValueAt(cam.getContentComponent(), j, 1);
								}
							}
						}
						return;
					}
				}
				
				//It's not in there
				artistMessages.addElement(cam);
			}
			else if(m instanceof CriticIssueMessage){
				CriticIssueMessage cim = (CriticIssueMessage)m;
				CriticIssue issue = cim.getIssue();
				if(cim.getStatus() == CriticIssueMessage.ISSUE_OPEN){
					//See if we're already rendering this issue
					for(int i = 0; i < issuesByRow.size(); i++){
						CriticIssue ei = (CriticIssue)issuesByRow.elementAt(i);
						if(ei.equals(issue)){
							return;
						}
					}
					CriticArtistMessage theCam = null;
					for(int i = 0; i < artistMessages.size(); i++){
						CriticArtistMessage ecam = (CriticArtistMessage)artistMessages.elementAt(i);
						if(ecam.getIssue().equals(issue)){
							theCam = ecam;
							break;
						}
					}
					if(theCam == null){
						//We have an open issue but we have not yet got a rendering for it.
						theCam = getUnrenderedCriticArtistMessage(issue);
						artistMessages.addElement(theCam);
					}
					
					if(theCam.getLabelComponent() instanceof MessageProvider){
						((MessageProvider)theCam.getLabelComponent()).addMessageListener(CriticGUIC2Component.this);
					}
					if(theCam.getContentComponent() instanceof MessageProvider){
						((MessageProvider)theCam.getContentComponent()).addMessageListener(CriticGUIC2Component.this);
					}
					tableModel.addRow(new java.awt.Component[]{theCam.getLabelComponent(), theCam.getContentComponent()});
					issuesByRow.add(issue);
					//dumpIssuesByRow();
				}
				else if(cim.getStatus() == CriticIssueMessage.ISSUE_CLOSED){
					for(int i = 0; i < artistMessages.size(); i++){
						CriticArtistMessage ecam = (CriticArtistMessage)artistMessages.elementAt(i);
						if(ecam.getIssue().equals(issue)){
							artistMessages.removeElementAt(i);
							break;
						}
					}
					for(int i = 0; i < issuesByRow.size(); i++){
						CriticIssue ei = (CriticIssue)issuesByRow.elementAt(i);
						if(ei.equals(issue)){
							issuesByRow.remove(i);
							tableModel.removeRow(i);
							//dumpIssuesByRow();
						}
					}
				}
			}
		}
	}
	
	class CriticGUIFrame extends JFrame{
		private JStaticTable tab;
		
		public CriticGUIFrame(){
			super("ArchStudio Critic GUI");
			archstudio.Branding.brandFrame(this);
			
			this.getContentPane().setLayout(new BorderLayout());
			
			tab = new JStaticTable(tableModel, 4, 4);
			tab.setDrawRowSplits(true);
			tab.setEmptyViewComponent(new JPanelUL(new JPanelEWL(new JLabel("<html>No open issues at this time.<br>&nbsp;<br>Note: Some critics may be disabled and are not reporting issues; launch the Critic Manager to check.</html>"))));
			JPanelUL p = new JPanelUL(tab);
			JPanelIS ip = new JPanelIS(p, new Insets(5,5,5,5));
			JScrollPane scrollPane = new JScrollPane(ip);
			scrollPane = WidgetUtils.adjustScrollPaneMovement(scrollPane);
			
			this.getContentPane().add("Center", scrollPane);
			
			Toolkit tk = getToolkit();
			Dimension screenSize = tk.getScreenSize();
			double xSize = (400);
			double ySize = (140);
			double xPos = (screenSize.getWidth() * 0.15);
			double yPos = (screenSize.getHeight() * 0.75);
			
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			addWindowListener(new CriticGUIWindowAdapter());
			
			setVisible(true);
			setSize((int)xSize, (int)ySize);
			setLocation((int)xPos, (int)yPos);
			validate();
			paint(getGraphics());
		}
		
		public void dispose(){
			if(tab != null){
				tab.destroy();
			}
			super.dispose();
		}
		
		class CriticGUIWindowAdapter extends WindowAdapter{
			public void windowClosing(WindowEvent we){
				closeWindow();
			}
		}
	}
}

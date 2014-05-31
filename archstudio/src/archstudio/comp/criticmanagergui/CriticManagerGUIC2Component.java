package archstudio.comp.criticmanagergui;


import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

import archstudio.critics.*;
import archstudio.invoke.*;

import edu.uci.ics.widgets.*;

public class CriticManagerGUIC2Component extends AbstractC2DelegateBrick implements InvokableBrick{

	protected CriticManagerGUIFrame guiFrame;
	protected DefaultTableModel tableModel;
	
	//Vector of CriticStatusMessages 
	//containing status of the various critics.
	//Element # in vector corresponds to row # in table model
	protected Vector criticStatuses;
	
	public CriticManagerGUIC2Component(Identifier id){
		super(id);
		criticStatuses = new Vector();
		tableModel = new DefaultTableModel();
		tableModel.addColumn("ActivateButton");
		tableModel.addColumn("CriticDescription");
		
		this.addLifecycleProcessor(new CriticManagerGUILifecycleProcessor());
		this.addMessageProcessor(new CriticStatusMessageProcessor());
		
		InvokeUtils.deployInvokableService(this, bottomIface, "Critic Manager", 
			"Critic Manager for ArchStudio 3");
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
			guiFrame = new CriticManagerGUIFrame();
		}
		else{
			guiFrame.requestFocus();
		}
	}

	private java.awt.Component[] getRowComponents(CriticStatusMessage csm){
		JPanel topButtonPanel = new JPanel();
		topButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		int status = csm.getStatus();
		//JLabel l;
		JButton b;
		final Identifier id = csm.getCriticID();
		if((status == CriticStatuses.STAT_AVAILABLE_ACTIVE) ||
			(status == CriticStatuses.STAT_AVAILABLE_ACTIVE_BUSY) ||
			(status == CriticStatuses.STAT_AVAILABLE_ACTIVE_WAITING)){
			//l = new JLabel("<html>Active</html>");
			//l.setFont(WidgetUtils.SANSSERIF_BOLD_MEDIUM_FONT);
			//l.setForeground(new Color(0, 128, 0));
			if(status == CriticStatuses.STAT_AVAILABLE_ACTIVE){
				b = new JButton("<html><center>Status: <font color=008800>Active</font><br>Click to Deactivate</center></html>");
			}
			else if(status == CriticStatuses.STAT_AVAILABLE_ACTIVE_WAITING){
				b = new JButton("<html><center>Status: <font color=000088>Waiting</font><br>Click to Deactivate</center></html>");
			}
			else{
				b = new JButton("<html><center>Status: <font color=888800>Working</font><br>Click to Deactivate</center></html>");
			}
			//b = new JButton("Deactivate");
			b.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent evt){
						CriticSetStatusMessage cssm = new CriticSetStatusMessage(id, CriticStatuses.STAT_AVAILABLE_INACTIVE, false);
						sendToAll(cssm, topIface);
					}
				}
			);
		}
		else{
			//l = new JLabel("<html>Inactive</html>");
			//l.setFont(WidgetUtils.SANSSERIF_BOLD_MEDIUM_FONT);
			//l.setForeground(Color.darkGray);
			b = new JButton("<html>Status: <font color=BB0000>Inactive</font><br>Click to Activate</html>");
			b.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent evt){
						CriticSetStatusMessage cssm = new CriticSetStatusMessage(id, CriticStatuses.STAT_AVAILABLE_ACTIVE, false);
						sendToAll(cssm, topIface);
					}
				}
			);
		}
		//topButtonPanel.add(l);
		
		//JPanel bottomButtonPanel = new JPanel();
		//bottomButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		//bottomButtonPanel.add(b);
		
		//JPanel buttonGridPanel = new JPanel();
		//buttonGridPanel.setLayout(new GridLayout(2,1));
		
		//buttonGridPanel.add(topButtonPanel);
		//buttonGridPanel.add(bottomButtonPanel);
		
		//JPanelUL buttonPanel = new JPanelUL(buttonGridPanel);
		JPanelUL buttonPanel = new JPanelUL(b);
		
		JLabel headlineLabel = new JLabel("<html>" + csm.getCriticID().toString() + "</html>");
		headlineLabel.setFont(WidgetUtils.SANSSERIF_BOLD_MEDIUM_FONT);
		
		JLabel descriptionLabel = new JLabel("<html>" + csm.getDescription() + "</html>");
		descriptionLabel.setFont(WidgetUtils.SANSSERIF_PLAIN_MEDIUM_FONT);
		
		JPanelEWL descriptionPanel = new JPanelEWL(descriptionLabel);
		
		JExpandableDataWidget edw = new JExpandableDataWidget(headlineLabel,  new JPanelUL(descriptionPanel));
		
		return new java.awt.Component[]{buttonPanel, edw};
	}
	
	private void replaceTableRow(int row, java.awt.Component[] comps){
		for(int i = 0; i < comps.length; i++){
			tableModel.setValueAt(comps[i], row, i);
		}
	}
	
	public synchronized void addCriticStatus(CriticStatusMessage csm){
		String csmIDString = csm.getCriticID().toString();
		for(int i = 0; i < criticStatuses.size(); i++){
			CriticStatusMessage thisCsm = (CriticStatusMessage)criticStatuses.elementAt(i);
			if(thisCsm.getCriticID().equals(csm.getCriticID())){
				//We're replacing this one.
				if(csm.equals(thisCsm)){
					//No change...duplicate message.  Forget it.
					return;
				}
				criticStatuses.setElementAt(csm, i);
				replaceTableRow(i, getRowComponents(csm));
				//tableModel.removeRow(i);
				//tableModel.insertRow(i, getRowComponents(csm));
				return;
			}
			String thisCsmIDString = thisCsm.getCriticID().toString();
			if(csmIDString.compareTo(thisCsmIDString) <= 0){
				//We need to insert at this position (sorted list):
				criticStatuses.insertElementAt(csm, i);
				tableModel.insertRow(i, getRowComponents(csm));
				return;
			}
		}
		//Not found, add at end
		criticStatuses.addElement(csm);
		tableModel.addRow(getRowComponents(csm));
	}
	
	public synchronized void removeCriticStatus(CriticStatusMessage csm){
		String csmIDString = csm.getCriticID().toString();
		for(int i = 0; i < criticStatuses.size(); i++){
			CriticStatusMessage thisCsm = (CriticStatusMessage)criticStatuses.elementAt(i);
			if(thisCsm.getCriticID().equals(csm.getCriticID())){
				//We're removing this one.
				criticStatuses.removeElementAt(i);
				tableModel.removeRow(i);
				return;
			}
		}
		return;
	}
	
	private void dumpTableModel(TableModel tm){
		int numCols = tm.getColumnCount();
		int numRows = tm.getRowCount();
		for(int j = 0; j < numRows; j++){
			for(int i = 0; i < numCols; i++){
				System.out.print(tm.getValueAt(j, i));
				System.out.print(";");
			}
			System.out.println();
		}
	}
	
	class CriticManagerGUILifecycleProcessor extends LifecycleAdapter{
		public void begin(){
			sendToAll(new CriticGetStatusMessage(new Identifier[0]), topIface);
		}
		
		public void end(){
			closeWindow();
		}
	}
	
	class CriticStatusMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof CriticStatusMessage){
				CriticStatusMessage csm = (CriticStatusMessage)m;
				if(!csm.getIsApproved()){
					//it's not verified by the critic manager.  Return.
					return;
				}
				if((csm.getStatus() == CriticStatuses.STAT_AVAILABLE_ACTIVE) ||
					(csm.getStatus() == CriticStatuses.STAT_AVAILABLE_ACTIVE_BUSY) ||
					(csm.getStatus() == CriticStatuses.STAT_AVAILABLE_ACTIVE_WAITING) ||
					(csm.getStatus() == CriticStatuses.STAT_AVAILABLE_INACTIVE)){
					addCriticStatus(csm);
				}
				else if(csm.getStatus() == CriticStatuses.STAT_UNAVAILABLE){
					//System.out.println("Got unavailable message: " + csm);
					removeCriticStatus(csm);
				}
			}
			else if(m instanceof CriticDependencyMissingMessage){
				CriticDependencyMissingMessage cdmm = (CriticDependencyMissingMessage)m;
				JOptionPane.showMessageDialog(guiFrame, "Critic Missing Dependency: " + cdmm.getMissingDependencyID(), 
					"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	class CriticManagerGUIFrame extends JFrame{
		private JStaticTable tab;
		
		public CriticManagerGUIFrame(){
			super("ArchStudio Critic Manager");
			archstudio.Branding.brandFrame(this);
			
			this.getContentPane().setLayout(new BorderLayout());
			
			tab = new JStaticTable(tableModel, 4, 4);
			tab.setDrawRowSplits(true);
			tab.setEmptyViewComponent(new JLabel("<html>No critics available at this time.</html>"));
			JPanelUL p = new JPanelUL(tab);
			JPanelIS ip = new JPanelIS(p, new Insets(5,5,5,5));
			JScrollPane scrollPane = new JScrollPane(ip);
			scrollPane = WidgetUtils.adjustScrollPaneMovement(scrollPane);
			
			this.getContentPane().add("Center", scrollPane);
			
			Toolkit tk = getToolkit();
			Dimension screenSize = tk.getScreenSize();
			double xSize = (400);
			double ySize = (140);
			double xPos = (screenSize.getWidth() * 0.10);
			double yPos = (screenSize.getHeight() * 0.55);
			
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			addWindowListener(new CriticManagerGUIWindowAdapter());
			
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
		
		class CriticManagerGUIWindowAdapter extends WindowAdapter{
			public void windowClosing(WindowEvent we){
				closeWindow();
			}
		}
	}

}



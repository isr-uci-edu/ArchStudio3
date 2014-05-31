package archstudio.comp.tron.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;

import archstudio.tron.TronIssue;
import archstudio.tron.TronToolNotice;

import edu.uci.ics.widgets.*;
import edu.uci.ics.xadlutils.Resources;

public class TronGUITabbedPane extends JTabbedPane implements TronGUIConsoleModelListener, ChangeListener, ListSelectionListener{

	public static final String TEST_RESULTS_TAB_NAME = "Test Results";
	public static final String TEST_ERRORS_TAB_NAME = TronGUIConsoleModel.TEST_ERROR_TOOLNAME;
	
	protected TronGUITableModel tronTableModel;
	protected TronGUITable tronTable;

	protected TronGUIConsoleModel tronConsoleModel;
	
	public TronGUITabbedPane(TronGUITableModel tronTableModel, TronGUIConsoleModel tronConsoleModel){
		super(JTabbedPane.BOTTOM);
		this.tronTableModel = tronTableModel;
		this.tronConsoleModel = tronConsoleModel;

		tronTable = new TronGUITable(tronTableModel);

		JPanelGR tronTablePane = new JPanelGR(java.awt.Color.WHITE, new java.awt.Color(0xdd, 0xdd, 0xdd));
		JScrollPane tronTableScrollPane = new JScrollPane(tronTable);
		tronTableScrollPane.setOpaque(false);
		tronTable.setOpaque(false);
		tronTablePane.setLayout(new BorderLayout());
		tronTablePane.add("Center", tronTableScrollPane);

		java.awt.Component[] allComponents = WidgetUtils.getHierarchyRecursive(tronTableScrollPane);
		for(int i = 0; i < allComponents.length; i++){
			if(allComponents[i] instanceof JComponent){
				((JComponent)allComponents[i]).setOpaque(false);
			}
		}
		tronTable.getSelectionModel().addListSelectionListener(this);
		
		this.addTab(TEST_RESULTS_TAB_NAME, tronTablePane);
		
		this.addChangeListener(this);
		
		tronConsoleModel.addTronGUIConsoleModelListener(this);
		refreshConsole();
	}
	
	private Vector tronGUITabbedPaneListeners = new Vector();
	
	public void addTronGUITabbedPaneListener(TronGUITabbedPaneListener l){
		tronGUITabbedPaneListeners.addElement(l);
	}
	
	public void removeTronGUITabbedPaneListener(TronGUITabbedPaneListener l){
		tronGUITabbedPaneListeners.removeElement(l);
	}
	
	protected void fireTronGUITabbedPaneChanged(){
		synchronized(tronGUITabbedPaneListeners){
			for(Iterator it = tronGUITabbedPaneListeners.iterator(); it.hasNext(); ){
				((TronGUITabbedPaneListener)it.next()).stateChanged(this);
			}
		}
	}
	
	public void selectTab(String tabNameToSelect){
		int tabCount = getTabCount();
		for(int i = 0; i < tabCount; i++){
			String tabName = getTitleAt(i);
			if(tabName != null){
				if(tabName.equals(tabNameToSelect)){
					setSelectedIndex(i);
					return;
				}
			}
		}
	}

	public void selectTestErrorsTab(){
		selectTab(TEST_ERRORS_TAB_NAME);
	}
	
	public void selectTestResultsTab(){
		selectTab(TEST_RESULTS_TAB_NAME);
	}
	
	public void valueChanged(ListSelectionEvent e){
		fireTronGUITabbedPaneChanged();
	}

	public void stateChanged(ChangeEvent e){
		fireTronGUITabbedPaneChanged();
	}
	
	public void clearSelection(){
		tronTable.getSelectionModel().clearSelection();
		JList[] lists = getAllConsolePanelLists();
		for(int i = 0; i < lists.length; i++){
			lists[i].clearSelection();
		}
	}
	
	public Object getSelectedItem(){
		Component tabShowing = getSelectedComponent();
		if(tabShowing == null){
			return null;
		}
		Component[] descendants = WidgetUtils.getHierarchyRecursive(tabShowing);
		for(int i = 0; i < descendants.length; i++){
			if(descendants[i] instanceof TronGUITable){
				TronGUITable table = (TronGUITable)descendants[i];
				int selectedTableRow = table.getSelectedRow();
				if(selectedTableRow == -1){
					return null;
				}
				TronGUITableModel tableModel = (TronGUITableModel)table.getModel();
				TronIssue selectedIssue = tableModel.getIssueAt(selectedTableRow);
				return selectedIssue;
			}
			else if(descendants[i] instanceof TronGUIConsolePanel){
				TronGUIConsolePanel consolePanel = (TronGUIConsolePanel)descendants[i];
				return consolePanel.getSelectedNotice();
			}
		}
		return null;
	}
	
	protected TronGUIConsolePanel[] getAllConsolePanels(){
		java.util.List tronGUIConsolePanelList = new ArrayList();
		
		Component[] descendants = WidgetUtils.getHierarchyRecursive(this);
		for(int j = 0; j < descendants.length; j++){
			if(descendants[j] instanceof TronGUIConsolePanel){
				tronGUIConsolePanelList.add(descendants[j]);
			}
		}
		return (TronGUIConsolePanel[])tronGUIConsolePanelList.toArray(new TronGUIConsolePanel[0]);
	}
	
	protected JList[] getAllConsolePanelLists(){
		TronGUIConsolePanel[] consolePanels = getAllConsolePanels();
		JList[] lists = new JList[consolePanels.length];
		for(int i = 0; i < consolePanels.length; i++){
			lists[i] = consolePanels[i].getNoticesList();
		}
		return lists;
	}
	
	/* Console Stuff */
	public void consoleModelChanged(TronGUIConsoleModel model){
		refreshConsole();
		fireTronGUITabbedPaneChanged();
	}
	
	public void refreshConsole(){
		synchronized(tronConsoleModel){
			String[] toolIDs = tronConsoleModel.getAllToolIDs();
			for(int i = 0; i < toolIDs.length; i++){
				//See if a tab already exists.
				TronGUIConsolePanel consolePanel = null;
				int indexOfTool = this.indexOfTab(toolIDs[i]);
				if(indexOfTool != -1){
					Component c = this.getComponentAt(indexOfTool);
					Component[] descendants = WidgetUtils.getHierarchyRecursive(c);
					for(int j = 0; j < descendants.length; j++){
						if(descendants[j] instanceof TronGUIConsolePanel){
							consolePanel = (TronGUIConsolePanel)descendants[j];
							break;
						}
					}
				}
				else{
					consolePanel = new TronGUIConsolePanel();
					this.addTab(toolIDs[i], consolePanel);
				}
				if(consolePanel != null){
					consolePanel.setToolNotices(tronConsoleModel.getToolNotices(toolIDs[i]));
				}
			}
		}
	}
	
	class TronGUIConsolePanel extends JPanel /*implements ListSelectionListener*/{
		protected TronToolNotice[] toolNotices;
		protected DefaultListModel listModel;
		protected JList lNotices;
		//protected JTextArea taInfo;
		
		public TronGUIConsolePanel(){
			this.setLayout(new BorderLayout());
			
			listModel = new DefaultListModel();
			lNotices = new JList(listModel);
			lNotices.setCellRenderer(new ConsoleListCellRenderer());
			lNotices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			lNotices.addListSelectionListener(TronGUITabbedPane.this);
			JScrollPane noticesPane = new JScrollPane(lNotices);
			WidgetUtils.adjustScrollPaneMovement(noticesPane);
			/*
			taInfo = new JTextArea();
			taInfo.setEditable(false);
			taInfo.setWrapStyleWord(true);
			taInfo.setLineWrap(true);
			JScrollPane textPane = new JScrollPane(taInfo);
			WidgetUtils.adjustScrollPaneMovement(textPane);
			
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, noticesPane, textPane);
			this.add("Center", splitPane);
			*/
			
			this.add("Center", noticesPane);

			//splitPane.setDividerLocation(0.50d);
			//checkSelection();
		}
		
		public TronToolNotice getSelectedNotice(){
			int selectedIndex = lNotices.getSelectedIndex();
			if(selectedIndex == -1){
				return null;
			}
			TronToolNotice notice = toolNotices[selectedIndex];
			return notice;
		}
		
		public JList getNoticesList(){
			return lNotices;
		}

		/*
		public void valueChanged(ListSelectionEvent e){
			checkSelection();
		}
		*/
		
		public void setToolNotices(TronToolNotice[] toolNotices){
			this.toolNotices = toolNotices;
			listModel.removeAllElements();
			for(int i = 0; i < toolNotices.length; i++){
				listModel.addElement(toolNotices[i].getMessage());
			}
			WidgetUtils.validateAndRepaintInAWTThread(this);
		}
		
		/*
		public void checkSelection(){
			int selectedIndex = lNotices.getSelectedIndex();
			if(selectedIndex == -1){
				taInfo.setText("Select a notice to see additional data.");
				return;
			}
			else{
				try{
					TronToolNotice notice = toolNotices[selectedIndex];
					String additionalDetail = notice.getAdditionalDetail();
					Throwable error = notice.getError();
					StringBuffer buf = new StringBuffer();
					if(additionalDetail != null){
						buf.append(additionalDetail).append("\n\n");
					}
					while(error != null){
						buf.append(error.toString()).append("\n");
						StackTraceElement[] elts = error.getStackTrace();
						for(int i = 0; i < elts.length; i++){
							buf.append("  " + elts[i].toString()).append("\n");
						}
						error = error.getCause();
						if(error != null){
							buf.append("Caused by:\n");
						}
					}
					if((additionalDetail == null) && (error == null)){
						buf.append("No additional detail.");
					}
					taInfo.setText(buf.toString());
					return;
				}
				catch(Exception e){
					taInfo.setText("- error -");
					return;
				}
			}
		}
		*/
	}
	
	static class ConsoleListCellRenderer extends DefaultListCellRenderer{
		public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus){
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if(value != null){
				String val = value.toString();
				if(val.startsWith("Error:")){
					setIcon(Resources.ERROR_ICON_16);
				}
				else if(val.startsWith("Warning:")){
					setIcon(Resources.WARNING_ICON_16);
				}
				else{
					setIcon(Resources.INFO_ICON_16);
				}
			}
			return this;
		}
	}
	
}

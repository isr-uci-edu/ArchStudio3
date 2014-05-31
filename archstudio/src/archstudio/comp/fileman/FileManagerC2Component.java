package archstudio.comp.fileman;

import archstudio.VersionInfo;
import archstudio.comp.editorprefs.EditorPrefsUtils;
import archstudio.comp.preferences.IPreferences;
import archstudio.comp.preferencesgui.ShowPreferencesDialogMessage;
import archstudio.editors.FocusEditorMessage;
import archstudio.invoke.*;
import archstudio.notifydoc.*;
import archstudio.preferences.RecentDirectoryPreferenceUtils;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;
import c2.util.ApprovalProcessor;

import edu.uci.ics.xarchutils.*;
import edu.uci.ics.widgets.GenericFileFilter;
import edu.uci.ics.widgets.WidgetUtils;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

public class FileManagerC2Component extends AbstractC2DelegateBrick implements c2.fw.Component{
	public static final String PRODUCT_NAME = "ArchStudio File Manager";
	//public static final String PRODUCT_VERSION = "build " + VersionInfo.getVersion("[unofficial build]");
	
	public static final int DEFAULT_APPROVAL_WAIT = 60000;
	public static final int NUM_RECENT_FILES = 5;
	
	protected XArchFlatInterface xarch;
	protected IPreferences preferences;
	protected FileManagerFrame fmFrame;	
	
	protected Hashtable availableServices = new Hashtable();
	protected Hashtable notifyDocComponents = new Hashtable();
	protected Hashtable approvalProcessors = new Hashtable();
	
	protected static int nextDocumentNum = 1;
	
	public FileManagerC2Component(Identifier id){
		super(id);
		this.addLifecycleProcessor(new FileManagerLifecycleProcessor());
		
		xarch = (XArchFlatInterface)EBIWrapperUtils.addExternalService(this, topIface, XArchFlatInterface.class);
		preferences = (IPreferences)EBIWrapperUtils.addExternalService(this,
			topIface, IPreferences.class);

		EBIWrapperUtils.addThreadMessageProcessor(this, new MessageProcessor[]{
		new InvokableStateMessageProcessor(), new NotifyDocStateMessageProcessor()
		});
		
		EBIWrapperUtils.addThreadMessageProcessor(this, new MessageProcessor[]{
			new NotifyDocDoneMessageProcessor()
			});
		
		XArchFileEventProvider xarchFileEventProvider = 
			(XArchFileEventProvider)EBIWrapperUtils.createStateChangeProviderProxy(this, topIface,
			XArchFileEventProvider.class);
		
		XArchFileListener fileListener = new XArchFileListener(){
			public void handleXArchFileEvent(XArchFileEvent evt){
				if(fmFrame != null)
					fmFrame.handleFileEvent(evt);
			}
		};
		xarchFileEventProvider.addXArchFileListener(fileListener);

		XArchFlatEventProvider xarchFlatEventProvider = 
			(XArchFlatEventProvider)EBIWrapperUtils.createStateChangeProviderProxy(this, topIface,
			XArchFlatEventProvider.class);
		
		XArchFlatListener flatListener = new XArchFlatListener(){
			public void handleXArchFlatEvent(XArchFlatEvent evt){
				if(fmFrame != null)
					fmFrame.handleFlatEvent(evt);
			}
		};
		xarchFlatEventProvider.addXArchFlatListener(flatListener);
	}
	
	private void queryServices(){
		QueryInvokableMessage qim = new QueryInvokableMessage();
		//System.out.println("Querying services.");
		sendRequest(qim);
	}
	
	private void queryNotifyDocs(){
		QueryNotifyDocMessage qndm = new QueryNotifyDocMessage();
		//System.out.println("Querying services.");
		sendRequest(qndm);
	}
	
	class FileManagerLifecycleProcessor extends LifecycleAdapter{
		public void begin(){
			archstudio.Branding.splashSmall(5000);
			fmFrame = new FileManagerFrame();		
			queryServices();
			queryNotifyDocs();
		}
		
		public void end(){
			fmFrame.setVisible(false);
			fmFrame.dispose();
		}
	}
	
	public void invokeService(Identifier componentId, String serviceName, String architectureURL){
		InvokeMessage m = new InvokeMessage(componentId, serviceName, architectureURL);
		sendRequest(m);
	}
	
	class InvokableStateMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof InvokableStateMessage){
				handleInvokableStateMessage((InvokableStateMessage)m);
				return;
			}
		}
	}
	
	class NotifyDocStateMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof NotifyDocStateMessage){
				handleNotifyDocStateMessage((NotifyDocStateMessage)m);
				return;
			}
		}
	}
	
	class NotifyDocDoneMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof NotifyDocDoneMessage){
				handleNotifyDocDoneMessage((NotifyDocDoneMessage)m);
				return;
			}
		}
	}
	
	protected void handleNotifyDocDoneMessage(NotifyDocDoneMessage m){
		String uid = m.getUid();
		Identifier componentID = m.getComponentId();
		
		synchronized(approvalProcessors){
			ApprovalProcessor ap = (ApprovalProcessor)approvalProcessors.get(uid);
			if(ap != null){
				ap.approve(componentID);
			}
		}
	}
	
	public String[] getAvailableServices(){
		synchronized(availableServices){
			String[] arr = new String[availableServices.size()];
			int i = 0;
			for(Enumeration en = availableServices.keys(); en.hasMoreElements(); ){
				arr[i++] = (String)en.nextElement();
			}
			return arr;
		}
	}
	
	public Identifier[] getNotifyDocComponents(){
		synchronized(notifyDocComponents){
			return (Identifier[])notifyDocComponents.keySet().toArray(new Identifier[0]);
		}
	}
	
	public void invokeService(String serviceName, String url){
		InvokableStateMessage m = (InvokableStateMessage)availableServices.get(serviceName);
		if(m == null){
			return;
		}
		invokeService(m.getComponentId(), serviceName, url);
	}
	
	
	public void handleInvokableStateMessage(InvokableStateMessage m){
		int messageType = m.getMessageType();
		if(messageType == InvokableStateMessage.SERVICE_ADVERTISED){
			if(fmFrame != null){
				availableServices.put(m.getServiceName(), m);
				fmFrame.updateInvokeMenu();
			}
		}
		else if(messageType == InvokableStateMessage.SERVICE_UNADVERTISED){
			if(fmFrame != null){
				availableServices.remove(m.getServiceName());
				fmFrame.updateInvokeMenu();
			}
		}
	}
	
	public void handleNotifyDocStateMessage(NotifyDocStateMessage m){
		int messageType = m.getMessageType();
		if(messageType == NotifyDocStateMessage.START_NOTIFY){
			if(fmFrame != null){
				notifyDocComponents.put(m.getComponentId(), m);
			}
		}
		else if(messageType == NotifyDocStateMessage.STOP_NOTIFY){
			if(fmFrame != null){
				notifyDocComponents.remove(m.getComponentId());
			}
		}
	}
	
	protected String[] getRecentFiles(){
		ArrayList recentFiles = new ArrayList();
		for(int i = 0; i < NUM_RECENT_FILES; i++){
			if(preferences.keyExists(IPreferences.USER_SPACE, 
			"/archstudio/comp/fileman", "recentFile" + i)){
				String recentFile = preferences.getStringValue(IPreferences.USER_SPACE, 
				"/archstudio/comp/fileman", "recentFile" + i, null);
				if(recentFile != null){
					recentFiles.add(recentFile);
				}
			}
		}
		return (String[])recentFiles.toArray(new String[0]);
	}
	
	protected void addRecentFile(String recentFile){
		String[] recentFiles = getRecentFiles();
		ArrayList list = new ArrayList();
		for(int i = 0; i < recentFiles.length; i++){
			//Don't dupe existing elements, just leave them out
			//since it'll get added at the front.
			if(!recentFiles[i].equals(recentFile)){
				list.add(recentFiles[i]);
			}
		}
		
		list.add(0, recentFile);
		List trueList = list;
		if(list.size() > NUM_RECENT_FILES){
			trueList = list.subList(0, NUM_RECENT_FILES);
		}
		String[] newRecentFiles = (String[])trueList.toArray(new String[0]);
		
		for(int i = 0; i < NUM_RECENT_FILES; i++){
			if(i < newRecentFiles.length){
				preferences.setValue(IPreferences.USER_SPACE, 
					"/archstudio/comp/fileman", "recentFile" + i, newRecentFiles[i]);
			}
			else{
				preferences.removeKey(IPreferences.USER_SPACE, 
					"/archstudio/comp/fileman", "recentFile" + i);
			}
		}
	}
	
	class FileManagerFrame extends JFrame implements ActionListener{
		private JMenu mInvoke;
		private JMenu mRecentFiles;
		private JList urlList;
		
		public FileManagerFrame(){
			super(PRODUCT_NAME);
			//super(PRODUCT_NAME + " (" + PRODUCT_VERSION + ")");
			archstudio.Branding.brandFrame(this);
			init();
			
		}
		
		private void init(){
			Toolkit tk = getToolkit();
			Dimension screenSize = tk.getScreenSize();
			double xSize = (350);
			double ySize = (130);
			double xPos = (screenSize.getWidth() * 0.60);
			double yPos = (screenSize.getHeight() * 0.75);
			
			JMenuBar mb = new JMenuBar();
			JMenu mFile = new JMenu("File");
			WidgetUtils.setMnemonic(mFile, 'F');
			
			JMenuItem miNewArchitecture = new JMenuItem("New Architecture...");
			miNewArchitecture.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.Event.CTRL_MASK));
			WidgetUtils.setMnemonic(miNewArchitecture, 'N');
			miNewArchitecture.addActionListener(this);
			
			JMenuItem miOpenFile = new JMenuItem("Open File...");
			miOpenFile.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.Event.CTRL_MASK));
			WidgetUtils.setMnemonic(miOpenFile, 'F');
			miOpenFile.addActionListener(this);
			
			JMenuItem miOpenURL = new JMenuItem("Open URL...");
			miOpenURL.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.Event.CTRL_MASK));
			WidgetUtils.setMnemonic(miOpenURL, 'U');
			miOpenURL.addActionListener(this);
			
			JMenuItem miDuplicate = new JMenuItem("Duplicate Selected Architecture...");
			WidgetUtils.setMnemonic(miDuplicate, 'D');
			miDuplicate.addActionListener(this);
			
			JMenuItem miSave = new JMenuItem("Save Selected Architecture");
			miSave.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.Event.CTRL_MASK));
			WidgetUtils.setMnemonic(miSave, 'S');
			miSave.addActionListener(this);
			
			JMenuItem miSaveAs = new JMenuItem("Save Selected Architecture As...");
			WidgetUtils.setMnemonic(miSaveAs, 'A');
			miSaveAs.addActionListener(this);
			
			JMenuItem miCloseSelected = new JMenuItem("Close Selected Architecture");
			WidgetUtils.setMnemonic(miCloseSelected, 'C');
			miCloseSelected.addActionListener(this);
			
			JMenuItem miRefreshOpenFiles = new JMenuItem("Refresh File List");
			WidgetUtils.setMnemonic(miRefreshOpenFiles, 'R');
			miRefreshOpenFiles.addActionListener(this);
			
			mRecentFiles = new JMenu("Recent Files");
			WidgetUtils.setMnemonic(mRecentFiles, 'E');
			
			JMenuItem miExitArchStudio = new JMenuItem("Exit ArchStudio 3");
			WidgetUtils.setMnemonic(miExitArchStudio, 'X');
			miExitArchStudio.addActionListener(this);
			
			mFile.add(miNewArchitecture);
			mFile.add(miOpenFile);
			mFile.add(miOpenURL);
			mFile.add(miDuplicate);
			mFile.add(miSave);
			mFile.add(miSaveAs);
			mFile.add(miCloseSelected);
			mFile.add(miRefreshOpenFiles);
			mFile.add(new JSeparator());
			mFile.add(mRecentFiles);
			mFile.add(new JSeparator());
			mFile.add(miExitArchStudio);
			
			JMenu mEdit = new JMenu("Edit");
			WidgetUtils.setMnemonic(mEdit, 'E');

			JMenuItem miEditPreferences = new JMenuItem("Preferences...");
			WidgetUtils.setMnemonic(miEditPreferences, 'P');
			miEditPreferences.addActionListener(this);
			mEdit.add(miEditPreferences);
			
			mInvoke = new JMenu("Invoke");
			WidgetUtils.setMnemonic(mInvoke, 'I');
			
			JMenu mHelp = new JMenu("Help");
			WidgetUtils.setMnemonic(mHelp, 'H');
			
			JMenuItem miHelpAbout = new JMenuItem("About ArchStudio 3...");
			WidgetUtils.setMnemonic(miHelpAbout, 'A');
			miHelpAbout.addActionListener(this);
			mHelp.add(miHelpAbout);
			
			mb.add(mFile);
			mb.add(mEdit);
			mb.add(mInvoke);
			mb.add(mHelp);
			
			this.setJMenuBar(mb);
			
			urlList = new JList(new DefaultListModel());
	    // Add a listener for mouse clicks
	    urlList.addMouseListener(new ListMouseAdapter());
			
			JPanel mainPanel = new JPanel();
			
			mainPanel.setLayout(new BorderLayout());
			mainPanel.add("North", new JLabel("Open URIs:"));
			mainPanel.add("Center", urlList);
			
			this.getContentPane().add(new JScrollPane(mainPanel));
			
			updateOpenURIs();
			updateInvokeMenu();
			
			this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			this.addWindowListener(
				new WindowAdapter(){
					public void windowClosing(WindowEvent evt){
						exitArchStudio(true, 0);
					}
				}
				);
			
			initDND();
			syncRecentFiles();
			
			setVisible(true);
			setSize((int)xSize, (int)ySize);
			setLocation((int)xPos, (int)yPos);
			setVisible(true);
			WidgetUtils.validateAndRepaintInAWTThread(this);
		}
		
		class ListMouseAdapter extends MouseAdapter{
			public void mouseClicked(MouseEvent evt) {
				try{
  				JList list = (JList)evt.getSource();
          if(evt.getClickCount() == 2){          // Double-click
          	// Get item index
          	int index = list.locationToIndex(evt.getPoint());
          	Object o = list.getModel().getElementAt(index);
          	if(o != null){
          		FileManagerListEntry fmle = (FileManagerListEntry)o;
          		String uri = fmle.getURI();
          		String defaultEditor = EditorPrefsUtils.getDefaultEditor(preferences);
          		if(defaultEditor == null){
  							JOptionPane.showMessageDialog(FileManagerFrame.this, "No default editor selected.", 
  								"Error", JOptionPane.ERROR_MESSAGE);
          		}
          		else{
          			FocusEditorMessage fem = new FocusEditorMessage(new String[]{defaultEditor}, 
          				xarch.getOpenXArch(uri),
          				(ObjRef)null, FocusEditorMessage.FOCUS_OPEN_EDITORS);
          			sendToAll(fem, topIface);
          		}
          	}
          }
      	}
      	catch(Exception e){
      	}
      }
		}
		
		public void syncRecentFiles(){
			String[] recentFiles = getRecentFiles();
			mRecentFiles.removeAll();
			if(recentFiles.length == 0){
				JMenuItem miNone = new JMenuItem("[None]");
				miNone.setEnabled(false);
				mRecentFiles.add(miNone);
				return;
			}
			for(int i = 0; i < recentFiles.length; i++){
				final String fn = recentFiles[i];
				String numString = "" + (i+1);
				JMenuItem miRecent = new JMenuItem(numString + " - " + fn);
				if(numString.length() == 1){
					WidgetUtils.setMnemonic(miRecent, numString.charAt(0));
				}
				miRecent.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent evt){
						try{
							ObjRef xarchRef = xarch.parseFromFile(fn);
						}
						catch(Exception e){
							JOptionPane.showMessageDialog(FileManagerFrame.this, e.toString(), 
								"Error", JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
							return;
						}
						addRecentFile(fn);
						syncRecentFiles();	
					}
				});
				mRecentFiles.add(miRecent);
			}
			
		}
		
		public void initDND(){
			FileDropTargetListener fdtl = new FileDropTargetListener();
			DropTarget dropTarget = new DropTarget(urlList, fdtl);
		}
		
		class FileDropTargetListener implements DropTargetListener{
			public void dragEnter(DropTargetDragEvent dtde){}
			public void dragExit(DropTargetEvent dte){}
			public void dragOver(DropTargetDragEvent dtde){}
			public void dropActionChanged(DropTargetDragEvent dtde){}
			
			public void drop(DropTargetDropEvent dtde){
				//System.out.println(dtde);
				Transferable t = dtde.getTransferable();
				if(t != null){
					if(t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
						dtde.acceptDrop(DnDConstants.ACTION_COPY);
						try{
							java.util.List l = (java.util.List)t.getTransferData(DataFlavor.javaFileListFlavor);
							if(l != null){
								for(Iterator it = l.iterator(); it.hasNext(); ){
									Object item = it.next();
									if(item instanceof File){
										String filename = ((File)item).getPath();
										try{
											ObjRef xarchRef = xarch.parseFromFile(filename);
										}
										catch(Exception e){
											JOptionPane.showMessageDialog(FileManagerFrame.this, e.toString(), 
												"Error", JOptionPane.ERROR_MESSAGE);
											e.printStackTrace();
											return;
										}
										addRecentFile(filename);
										syncRecentFiles();
									}
								}
							}
						}
						catch(UnsupportedFlavorException ufe){
						}
						catch(IOException ioe){
						}
						dtde.dropComplete(true);
						return;
					}
				}
				dtde.rejectDrop();
				dtde.dropComplete(false);
			}
		}
		
		public void actionPerformed(ActionEvent evt){
			if(evt.getSource() instanceof InvokeMenuItem){
				String serviceName = ((InvokeMenuItem)evt.getSource()).getServiceName();
				String selectedURI = null;
				FileManagerListEntry selectedEntry = (FileManagerListEntry)urlList.getSelectedValue();
				if(selectedEntry != null){
					selectedURI = selectedEntry.getURI();
				}
				invokeService(serviceName, selectedURI);
			}
			else if(evt.getSource() instanceof JMenuItem){
				String label = ((JMenuItem)evt.getSource()).getText();
				if(label.equals("New Architecture...")){
					handleNewArchitecture();
				}
				else if(label.equals("Open File...")){
					handleOpenFile();
				}
				else if(label.equals("Open URL...")){
					handleOpenURL();
				}
				else if(label.equals("Duplicate Selected Architecture...")){
					handleDuplicate();
				}
				else if(label.equals("Save Selected Architecture")){
					handleSave();
				}
				else if(label.equals("Save Selected Architecture As...")){
					handleSaveAs();
				}
				else if(label.equals("Close Selected Architecture")){
					handleCloseSelected();
				}
				else if(label.equals("Refresh File List")){
					handleRefreshFiles();
				}
				else if(label.equals("Exit ArchStudio 3")){
					exitArchStudio(true, 0);
				}
				else if(label.equals("Preferences...")){
					handleEditPreferences();
				}
				else if(label.equals("About ArchStudio 3...")){
					handleHelpAbout();
				}
			}
		}
		
		private void addMenuItemSorted(JMenu menu, JMenuItem mi){
			int size = menu.getItemCount();
			String miString = mi.toString();
			for(int i = 0; i < size; i++){
				JMenuItem currentMenuItem = menu.getItem(i);
				String currentMenuItemString = currentMenuItem.toString();
				if(miString.compareTo(currentMenuItemString) <= 0){
					menu.insert(mi, i);
					return;
				}
			}
			menu.add(mi);
		}
		
		private Object menuLock = new Object();
		
		public void updateInvokeMenu(){
			synchronized(menuLock){
				mInvoke.removeAll();
				String[] serviceNames = getAvailableServices();
				if(serviceNames.length == 0){
					JMenuItem mi = new JMenuItem("[No Services Available]");
					mi.setEnabled(false);
					addMenuItemSorted(mInvoke, mi);
					//mInvoke.add(mi);
					return;
				}
				for(int i = 0; i < serviceNames.length; i++){
					String[] serviceNameSegments = serviceNames[i].split("/");
					String lastServiceNameSegment = serviceNameSegments[serviceNameSegments.length - 1];
					
					InvokableStateMessage ism = (InvokableStateMessage)availableServices.get(serviceNames[i]);
					JMenuItem mi = new InvokeMenuItem(ism);
					if(ism != null){
						String serviceDescription = ism.getServiceDescription();
						if(serviceDescription != null){
							mi.setToolTipText(serviceDescription);
						}
					}
					mi.setEnabled(true);
					mi.addActionListener(this);
					
					JMenu menu = mInvoke;
					for(int j = 0; j < (serviceNameSegments.length - 1); j++){
						//Each of these is a JMenu
						String menuName = serviceNameSegments[j];
						JMenu subMenu = null;
						for(int k = 0; k < menu.getItemCount(); k++){
							JMenuItem potentialMatch = menu.getItem(k);
							if(potentialMatch instanceof JMenu){
								String potentialMatchName = potentialMatch.getText();
								if((potentialMatchName != null) && (potentialMatchName.equals(menuName))){
									subMenu = (JMenu)potentialMatch;
									break;
								}
							}
						}
						if(subMenu == null){
							subMenu = new JMenu(menuName);
							addMenuItemSorted(menu, subMenu);
						}
						menu = subMenu;
					}
					
					addMenuItemSorted(menu, mi);
					//mInvoke.add(mi);
				}
			}
		}
		
		protected class InvokeMenuItem extends JMenuItem{
			protected String serviceName;
			
			public InvokeMenuItem(InvokableStateMessage m){
				super();
				this.serviceName = m.getServiceName();
				String text = serviceName.substring(serviceName.lastIndexOf("/") + 1);
				setText(text);
			}
			
			public String getServiceName(){
				return serviceName;
			}
		}
		
		class FileManagerListEntry{
			protected String uri;
			protected ObjRef xArchRef;
			protected boolean hasBeenModified;
			
			public FileManagerListEntry(String uri, ObjRef xArchRef, boolean hasBeenModified){
				this.uri = uri;
				this.xArchRef = xArchRef;
				this.hasBeenModified = hasBeenModified;
			}
			
			public void setURI(String uri){
				this.uri = uri;
			}
			
			public void setHasBeenModified(boolean hasBeenModified){
				this.hasBeenModified = hasBeenModified;
			}
			
			public String getURI(){
				return uri;
			}
			
			public void setXArchRef(ObjRef xArchRef){
				this.xArchRef = xArchRef;
			}
			
			public ObjRef getXArchRef(){
				return xArchRef;
			}
			
			public boolean getHasBeenModified(){
				return hasBeenModified;
			}
			
			public String toString(){
				StringBuffer sb = new StringBuffer(uri);
				if(hasBeenModified){
					sb.append("*");
				}
				return sb.toString();
			}
		}
		
		public void handleFlatEvent(XArchFlatEvent evt){
			if(evt.getIsAttached()){
				ObjRef ref = evt.getSource();
				if(ref == null){
					Object target = evt.getTarget();
					if(target instanceof ObjRef){
						ref = (ObjRef)target;
					}
				}
				if(ref == null){
					return;
				}
				try{
					ObjRef xArchRef = xarch.getXArch(ref);
					if(xArchRef != null){
						setHasBeenModified(xArchRef, true);
					}
				}
				catch(Exception e){
				}
			}
		}
		
		public void handleFileEvent(XArchFileEvent evt){
			updateOpenURIs();
		}
		
		public void updateOpenURIs(){
			synchronized(urlList){
				ObjRef[] xArchRefs = xarch.getOpenXArches();
				String[] uris = new String[xArchRefs.length];
				for(int i = 0; i < xArchRefs.length; i++){
					try{
						uris[i] = xarch.getXArchURI(xArchRefs[i]);
					}
					catch(Exception e){
						uris[i] = "urn:unknown" + System.currentTimeMillis();
					}
				}
				DefaultListModel lm = (DefaultListModel)urlList.getModel();
				
				FileManagerListEntry[] oldEntries = new FileManagerListEntry[lm.getSize()];
				lm.copyInto(oldEntries);
				
				Object[] selectedValues = urlList.getSelectedValues();
								
				lm.removeAllElements();
				
				List selectedIndexList = new ArrayList();
				for(int i = 0; i < xArchRefs.length; i++){
					FileManagerListEntry listEntry = new FileManagerListEntry(uris[i], xArchRefs[i], false);
					//See if this document has been modified.
					for(int j = 0; j < oldEntries.length; j++){
						if(xArchRefs[i].equals(oldEntries[j].getXArchRef())){
							listEntry.setHasBeenModified(oldEntries[j].getHasBeenModified());
						}
					}
					lm.addElement(listEntry);
					for(int j = 0; j < selectedValues.length; j++){
						FileManagerListEntry oldSelectedEntry = (FileManagerListEntry)selectedValues[j];
						ObjRef oldSelectedXArchRef = oldSelectedEntry.getXArchRef();
						if(oldSelectedXArchRef.equals(listEntry.getXArchRef())){
							selectedIndexList.add(new Integer(lm.indexOf(listEntry)));
						}
					}
				}
				int[] selectedIndices = new int[selectedIndexList.size()];
				for(int i = 0; i < selectedIndices.length; i++){
					selectedIndices[i] = ((Integer)selectedIndexList.get(i)).intValue();
				}
				urlList.setSelectedIndices(selectedIndices);
				
				WidgetUtils.validateAndRepaintInAWTThread(this);
			}
		}
		
		protected void setHasBeenModified(ObjRef xArchRef, boolean hasBeenModified){
			synchronized(urlList){
				DefaultListModel lm = (DefaultListModel)urlList.getModel();
				FileManagerListEntry[] oldEntries = new FileManagerListEntry[lm.getSize()];
				lm.copyInto(oldEntries);
	
				for(int j = 0; j < oldEntries.length; j++){
					if(xArchRef.equals(oldEntries[j].getXArchRef())){
						if(oldEntries[j].getHasBeenModified() != hasBeenModified){
							oldEntries[j].setHasBeenModified(hasBeenModified);
							lm.set(j, oldEntries[j]);
						}
					}
				}
			}	
		}
		
		public void handleNewArchitecture(){
			String documentURI = "urn:Architecture" + (nextDocumentNum++);
			
			/*
			String documentURI = JOptionPane.showInputDialog(this, "Enter URI for new architecture:", "New Architecture", JOptionPane.QUESTION_MESSAGE);
			if(documentURI == null){
				return;
			}
			*/
			
			try{
				xarch.createXArch(documentURI);
			}
			catch(Exception e){
				JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return;
			}				
		}
		
		public void handleOpenFile(){
			String startingDirectory = RecentDirectoryPreferenceUtils.getGoodRecentDirectory(preferences);
			if(startingDirectory == null){
				startingDirectory = ".";
			}

			JFileChooser chooser = new JFileChooser(startingDirectory);
			
			GenericFileFilter xadlFileFilter = new GenericFileFilter();
			xadlFileFilter.addExtension("xml");
			xadlFileFilter.setDescription("XML Files");
			chooser.addChoosableFileFilter(xadlFileFilter);
			chooser.setFileFilter(xadlFileFilter);
			
			int returnVal = chooser.showOpenDialog(this);

			File currentDir = chooser.getCurrentDirectory();
			if(currentDir != null){
				RecentDirectoryPreferenceUtils.storeRecentDirectory(preferences, currentDir.getPath());
			}
			
			if(returnVal == JFileChooser.APPROVE_OPTION){
				String filename = chooser.getSelectedFile().getPath();
				try{
					//System.out.println("parseFromFile called.");
					ObjRef xarchRef = xarch.parseFromFile(filename);
					//System.out.println("parseFromFile returned.");
				}
				catch(Exception e){
					JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					return;
				}	
				addRecentFile(filename);
				syncRecentFiles();
			}
		}
		
		public void handleOpenURL(){
			String inputValue = JOptionPane.showInputDialog(this, "Enter URL:", "Open URL", JOptionPane.QUESTION_MESSAGE);
			
			if(inputValue == null){
				return;
			}
			
			try{
				ObjRef xarchRef = xarch.parseFromURL(inputValue);
			}
			catch(MalformedURLException mue){
				JOptionPane.showMessageDialog(this, "Malformed URL: " + mue.getMessage(), "Error: Bad URL", JOptionPane.ERROR_MESSAGE);
				return;
			}
			catch(FileNotFoundException fnfe){
				JOptionPane.showMessageDialog(this, "Not Found: " + fnfe.getMessage(), "Error: Not Found", JOptionPane.ERROR_MESSAGE);
				return;
			}
			catch(Exception e){
				JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return;
			}	
		}
		
		public void handleDuplicate(){
			String selectedURI = null;
			FileManagerListEntry selectedEntry = (FileManagerListEntry)urlList.getSelectedValue();
			if(selectedEntry != null){
				selectedURI = selectedEntry.getURI();
			}
			if(selectedURI == null){
				JOptionPane.showMessageDialog(this, "No architecture selected.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			String inputValue = JOptionPane.showInputDialog(this, "Enter URL for duplicate:", "Duplicate Architecture", JOptionPane.QUESTION_MESSAGE);
			
			if(inputValue == null){
				return;
			}
			
			try{
				ObjRef xArchRef = xarch.getOpenXArch(selectedURI);
				xarch.cloneXArch(xArchRef, inputValue);
			}
			catch(Exception e){
				JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return;
			}	
		}
		
		public boolean handleSave(){
			FileManagerListEntry selectedEntry = (FileManagerListEntry)urlList.getSelectedValue();
			boolean result = doSave(selectedEntry);
			//if(urlList != null) urlList.requestFocus();
			return result;
		}
		
		public boolean doSave(FileManagerListEntry selectedEntry){
			String selectedURI = null;
			if(selectedEntry != null){
				selectedURI = selectedEntry.getURI();
			}
			if(selectedURI == null){
				JOptionPane.showMessageDialog(this, "No architecture selected.", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			if(!selectedURI.startsWith("file:")){
				return handleSaveAs();
			}
			
			try{
				URL fileURL = new URL(selectedURI);
				ObjRef xArchRef = xarch.getOpenXArch(selectedURI);
				return doSave(fileURL, xArchRef);
			}
			catch(Exception e){
				JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return false;
			}
		}
		
		public boolean handleSaveAs(){
			String selectedURI = null;
			FileManagerListEntry selectedEntry = (FileManagerListEntry)urlList.getSelectedValue();
			if(selectedEntry != null){
				selectedURI = selectedEntry.getURI();
			}
			if(selectedURI == null){
				JOptionPane.showMessageDialog(this, "No architecture selected.", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			String startingDirectory = RecentDirectoryPreferenceUtils.getGoodRecentDirectory(preferences);
			if(startingDirectory == null){
				startingDirectory = ".";
			}

			JFileChooser chooser = new JFileChooser(startingDirectory);

			GenericFileFilter xadlFileFilter = new GenericFileFilter();
			xadlFileFilter.addExtension("xml");
			xadlFileFilter.setDescription("XML Files");
			chooser.addChoosableFileFilter(xadlFileFilter);
			chooser.setFileFilter(xadlFileFilter);

			int returnVal = chooser.showSaveDialog(this);

			File currentDir = chooser.getCurrentDirectory();
			if(currentDir != null){
				RecentDirectoryPreferenceUtils.storeRecentDirectory(preferences, currentDir.getPath());
			}

			if(returnVal == JFileChooser.APPROVE_OPTION){
				try{
					File f = chooser.getSelectedFile();
					String fileName = f.getName();
					if(fileName.indexOf(".") == -1){
						String pathName = f.getPath();
						pathName += ".xml";
						f = new File(pathName);
					}
					if(f.exists()){
						int result = JOptionPane.showConfirmDialog(null,
							"Overwrite Existing File?", "Confirm Overwrite",
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if(result != JOptionPane.YES_OPTION){
							return false;
						}
					}
					
					URL fileURL = f.toURL();
					ObjRef xArchRef = xarch.getOpenXArch(selectedURI);
					//String content = xarch.serialize(xArchRef);
					return doSave(fileURL, xArchRef);
				}
				catch(Exception e){
					JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					return false;
				}
			}
			else{
				return false;
			}
		}
		
		private boolean doSave(URL fileURL, ObjRef xArchRef){
			try{
				//String path = fileURL.getPath();
				String file = fileURL.getFile();		//Amazingly, this works (albeit for file:// URLs only)
				//path = path.replace('/', File.separatorChar);
				if(doSave(new File(file), xArchRef)){
					String oldURI = xarch.getXArchURI(xArchRef);
					String newURI = fileURL.toString();
					setHasBeenModified(xArchRef, false);
					xarch.renameXArch(oldURI, newURI);
					return true;
				}
				else{
					return false;
				}
			}
			catch(Exception e){
				JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return false;
			}
		}
		
		
		private boolean doSave(File file, ObjRef xArchRef){
			try{
				waitForApproval(NotifyDocMessage.OPERATION_SAVING, xArchRef, DEFAULT_APPROVAL_WAIT);
				xarch.writeToFile(xArchRef, file.getPath());
				addRecentFile(file.getPath());
				syncRecentFiles();	
				//Writer w = new BufferedWriter(new FileWriter(file));
				//w.write(content, 0, content.length());
				//w.flush();
				//w.close();
				return true;
			}
			catch(IOException e){
				JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return false;
			}	
		}
		
		public void handleCloseSelected(){
			String selectedURI = null;
			FileManagerListEntry selectedEntry = (FileManagerListEntry)urlList.getSelectedValue();
			if(selectedEntry != null){
				selectedURI = selectedEntry.getURI();
			}
			if(selectedURI == null){
				JOptionPane.showMessageDialog(this, "No architecture selected.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			waitForApproval(NotifyDocMessage.OPERATION_CLOSING, selectedEntry.getXArchRef(), DEFAULT_APPROVAL_WAIT);
			if(!checkSaveChanges(selectedEntry)){
				return;
			}
			xarch.close(selectedEntry.getXArchRef());
		}
		
		public void handleRefreshFiles(){
			updateOpenURIs();
		}
		
		public void handleEditPreferences(){
			ShowPreferencesDialogMessage spdm = new ShowPreferencesDialogMessage();
			sendToAll(spdm, topIface);
		}
		
		public void handleHelpAbout(){
			archstudio.Branding.showSplashScreen();
		}
		
		public boolean checkSaveChangesAll(){
			DefaultListModel lm = (DefaultListModel)urlList.getModel();
			FileManagerListEntry[] listEntries = new FileManagerListEntry[lm.getSize()];
			lm.copyInto(listEntries);
			
			for(int i = 0; i < listEntries.length; i++){
				FileManagerListEntry listEntry = listEntries[i];
				if(listEntry.getHasBeenModified() == true){
				  JOptionPane pane = new JOptionPane("File " + listEntry.getURI() + " has changed; save changes?",
				  JOptionPane.QUESTION_MESSAGE);
				  pane.setOptions(new Object[]{"Yes", "No", "Cancel", "No to All"});
				  JDialog dialog = pane.createDialog(null, "Save Changes?");
				  dialog.setVisible(true);
				  Object selectedValue = pane.getValue();
			  	if(selectedValue == null){
			  		//User closed, abort the exit.
			  		return false;
			  	}
			  	if(selectedValue.equals("Yes")){
						boolean saveResult = doSave(listEntry);
						if(saveResult == false){
							//Save was cancelled, abort the exit.
							return false;
						}
						else{
							//Save succeeded, continue the exit.
							continue;
						}
			  	}
			  	else if(selectedValue.equals("No")){
			  		//Do nothing, continue the exit.
			  		continue;
			  	}
			  	else if(selectedValue.equals("Cancel")){
			  		//Do nothing, abort the exit.
			  		return false;
			  	}
			  	else if(selectedValue.equals("No to All")){
			  		//Do nothing, perform exit without checking any more.
			  		return true;
			  	}
			  	else{
			  		//???
			  		return false;
			  	}
				}
			}
			//All done, perform the exit.
			return true;
		}
		
		//Returns true if user saved or didn't save or there were no changes to save
		//Returns false if user hit "cancel"
		public boolean checkSaveChanges(FileManagerListEntry listEntry){
			if(listEntry.getHasBeenModified() == false){
				//No modification, just return and allow file close
				return true;
			}
			int result = JOptionPane.showConfirmDialog(null, 
				"File " + listEntry.getURI() + " has changed; save changes?", "Save Changes?", JOptionPane.YES_NO_CANCEL_OPTION);
			if(result == JOptionPane.NO_OPTION){
				//Do nothing and continue the close
				return true;
			}
			else if(result == JOptionPane.YES_OPTION){
				boolean saveResult = doSave(listEntry);
				if(saveResult == false){
					//Save was cancelled, abort the close.
					return false;
				}
				else{
					//Save succeeded, continue the close.
					return true;
				}
			}
			else{
				//Do nothing and abort the close
				return false;
			}
		}

		private void exitArchStudio(boolean askFirst, int exitCode){
			if(askFirst){
				int selection = JOptionPane.showConfirmDialog(null, 
					"Really Exit?", "Really Exit?", JOptionPane.YES_NO_OPTION);
				if(selection == JOptionPane.NO_OPTION){
					return;
				}
			}
			if(!checkSaveChangesAll()){
				return;
			}
			ShutdownArchMessage sam = new ShutdownArchMessage(0, ShutdownArchMessage.SHUTDOWN_NORMAL,
				"Normal termination.");
			sendArchMessage(sam);
			//System.exit(exitCode);
		}
		
		protected synchronized boolean waitForApproval(int operation, ObjRef documentRef, int maxWaitMillis){
			Identifier[] approvers = getNotifyDocComponents();
			if(approvers.length == 0) return true;

			NotifyDocMessage ndm = new NotifyDocMessage(operation, documentRef);
			String uid = ndm.getUID();
			ApprovalProcessor ap = new ApprovalProcessor(approvers, null);
			approvalProcessors.put(uid, ap);
			
			//System.err.println("sending initial do your stuff message");
			sendToAll(ndm, topIface);
			//Now we wait for approval.
			int totalTimeWaited = 0;
			while(totalTimeWaited <= maxWaitMillis){
				try{
					Thread.sleep(100);
					totalTimeWaited += 100;
				}
				catch(InterruptedException ie){}
				if(ap.isApproved()){
					approvalProcessors.remove(uid);
					return true;
				}
			}
			//System.err.println("failj00r");
			approvalProcessors.remove(uid);
			return false;
		}

	}
	
}

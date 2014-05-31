package archstudio.comp.tron.gui;

import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;
import archstudio.editors.AllEditorsStatusMessage;
import archstudio.invoke.*;
import archstudio.tron.*;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;
import c2.util.MessageSendProxy;

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
import javax.swing.tree.*;

public class TronGUIC2Component extends AbstractC2DelegateBrick implements InvokableBrick{

	protected XArchFlatTransactionsInterface xarch = null;
	protected TronGUIFrame guiFrame = null;
	protected TronGUIEditorModel guiEditorModel = null;
	protected TronGUITreeModel guiTreeModel = null;
	protected TronGUITableModel guiTableModel = null;
	protected TronGUIConsoleModel guiConsoleModel = null;
	protected TronGUIToolStatusModel guiToolStatusModel = null;
	
	protected MessageSendProxy requestProxy = new MessageSendProxy(this, topIface);
	protected MessageSendProxy notificationProxy = new MessageSendProxy(this, bottomIface);
	
	public TronGUIC2Component(Identifier id){
		super(id);
		xarch = (XArchFlatTransactionsInterface)EBIWrapperUtils.addExternalService(this, topIface, XArchFlatTransactionsInterface.class);

		guiEditorModel = new TronGUIEditorModel();
		guiTreeModel = new TronGUITreeModel(xarch, requestProxy);
		guiTableModel = new TronGUITableModel(guiTreeModel);
		guiConsoleModel = new TronGUIConsoleModel();
		guiToolStatusModel = new TronGUIToolStatusModel();
		
		addLifecycleProcessor(new TronGUILifecycleProcessor());
		addMessageProcessor(new TronGUIMessageProcessor());

		XArchEventProvider xarchEventProvider = 
			(XArchEventProvider)EBIWrapperUtils.createStateChangeProviderProxy(this, topIface,
			XArchEventProvider.class);
		
		XArchFileListener fileListener = new XArchFileListener(){
			public void handleXArchFileEvent(XArchFileEvent evt) {
				handleFileEvent(evt);
			}
		};
		xarchEventProvider.addXArchFileListener(fileListener);
		
		XArchFlatListener flatListener = new XArchFlatListener(){
			public void handleXArchFlatEvent(XArchFlatEvent evt) {
				handleFlatEvent(evt);
			}
		};
		xarchEventProvider.addXArchFlatListener(flatListener);
		
		InvokeUtils.deployInvokableService(this, bottomIface, 
			"Analysis Tools/Tron", 
			"An Analysis Framework for ArchStudio 3");
	}

	public void invoke(InvokeMessage m){
		//Let's open a new window.
		newWindow();
	}
	
	public void handleFileEvent(XArchFileEvent evt){
		guiTreeModel.handleFileEvent(evt);
	}

	public void handleFlatEvent(XArchFlatEvent evt){
		guiTreeModel.handleFlatEvent(evt);
	}
	
	class TronGUILifecycleProcessor extends c2.fw.LifecycleAdapter{
		public void begin(){
			sendToAll(new TronGetAllIssuesMessage(), topIface);
			sendToAll(new TronGetAllToolNoticesMessage(), topIface);
			sendToAll(new TronGetAllToolStatusesMessage(), topIface);
		}
		
		public void end(){
			closeWindow();
		}
	}

	public void newWindow(){
		//This makes sure we only have one active window open.
		if(guiFrame == null){
			guiFrame = new TronGUIFrame(this, requestProxy, notificationProxy, xarch, 
				guiEditorModel, guiTreeModel,	guiTableModel, guiConsoleModel, guiToolStatusModel);
			guiFrame.addWindowListener(new TronGUIWindowAdapter());
		}
		else{
			guiFrame.requestFocus();
		}
	}

	public void closeWindow(){
		if(guiFrame != null){
			guiFrame.setVisible(false);
			guiFrame.dispose();
			guiFrame = null;
		}
	}
	
	class TronGUIWindowAdapter extends WindowAdapter{
		public void windowClosing(WindowEvent we){
			closeWindow();
		}
	}

	class TronGUIMessageProcessor implements MessageProcessor{
		public synchronized void handle(Message m){
			if(m instanceof TronAllTestsMessage){
				guiTreeModel.handleAllTests((TronAllTestsMessage)m);
				//System.out.println(c2.util.ArrayUtils.arrayToString(guiTestManager.getAllTests()));
			}
			else if(m instanceof TronTestsChangedMessage){
				guiTreeModel.handleTestsChanged((TronTestsChangedMessage)m);
				//System.out.println(c2.util.ArrayUtils.arrayToString(guiTestManager.getAllTests()));
			}
			else if(m instanceof TronTestErrorsMessage){
				if(guiFrame != null){
					guiFrame.handleTestErrors((TronTestErrorsMessage)m);
				}
				guiConsoleModel.handleTestErrors((TronTestErrorsMessage)m, guiTreeModel.getAllTests());
			}
			else if(m instanceof TronAllToolNoticesMessage){
				guiConsoleModel.handleAllToolNotices((TronAllToolNoticesMessage)m);
			}
			else if(m instanceof TronAllIssuesMessage){
				//System.out.println("all issues: " + m);
				guiTableModel.handleAllIssues((TronAllIssuesMessage)m);
			}
			else if(m instanceof TronIssuesChangedMessage){
				//System.out.println("issues changed: " + m);
				guiTableModel.handleIssuesChanged((TronIssuesChangedMessage)m);
			}
			else if(m instanceof AllEditorsStatusMessage){
				guiEditorModel.handleAllEditorsStatus((AllEditorsStatusMessage)m);
			}
			else if(m instanceof TronToolStatusMessage){
				guiToolStatusModel.handleToolStatus((TronToolStatusMessage)m);
			}
		}
	}

	
}

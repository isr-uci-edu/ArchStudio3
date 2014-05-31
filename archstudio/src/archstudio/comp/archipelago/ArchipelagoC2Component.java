package archstudio.comp.archipelago;

import archstudio.comp.booleannotation.IBooleanNotation;
import archstudio.comp.graphlayout.IGraphLayout;
import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;
import archstudio.comp.xarchtrans.XArchTransactionEvent;
import archstudio.invoke.*;
import archstudio.editors.*;
import archstudio.notifydoc.*;

import archstudio.preferences.*;
import archstudio.comp.preferences.*;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

import edu.uci.ics.bna.*;
import edu.uci.ics.xarchutils.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.dnd.peer.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

public class ArchipelagoC2Component extends AbstractC2DelegateBrick 
implements c2.fw.Component, InvokableBrick, NotifyDocBrick{
	public static final String PRODUCT_NAME = "Archipelago";
	public static final String SERVICE_NAME = "Editors/Archipelago";
	public static final String PREFERENCE_NAME = "ArchStudio 3/Archipelago";
	
	protected Vector openWindows;
	protected XArchFlatTransactionsInterface xarch;
	protected IPreferences preferences;
	protected IGraphLayout gli;
	protected IBooleanNotation bni;
		
	public ArchipelagoC2Component(Identifier id){
		super(id);
		EditorUtils.registerEditor(this, topIface, PRODUCT_NAME);
		xarch = (XArchFlatTransactionsInterface)EBIWrapperUtils.addExternalService(this, topIface, XArchFlatTransactionsInterface.class);
		gli = (IGraphLayout)EBIWrapperUtils.addExternalService(this, topIface, IGraphLayout.class);
		bni = (IBooleanNotation)EBIWrapperUtils.addExternalService(this, topIface, IBooleanNotation.class);

		openWindows = new Vector();
		this.addLifecycleProcessor(new ArchipelagoLifecycleProcessor());
		this.addMessageProcessor(new FocusEditorMessageProcessor());
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
			public void handleXArchFlatEvent(XArchFlatEvent evt){
				handleStateChangeEvent(evt);
			}
		};
		xarchEventProvider.addXArchFlatListener(flatListener);
		this.addMessageProcessor(new ArchipelagoMessageProcessor());
		//this.addMessageProcessor(new FocusEditorMessageProcessor());
		//EBIWrapperUtils.addThreadMessageProcessor(this, new MessageProcessor[]{ new StateChangeMessageProcessor()});
		//addMessageProcessor(new DebugMessageProcessor());
		InvokeUtils.deployInvokableService(this, bottomIface, 
			SERVICE_NAME, 
			"A graphical boxes-and-arrows architecture editor");
		
		preferences = (IPreferences)EBIWrapperUtils.addExternalService(this,
			topIface, IPreferences.class);
		ArchipelagoPreferencePanel pp = new ArchipelagoPreferencePanel();
		PreferencesUtils.deployPreferencesService(this, bottomIface, PREFERENCE_NAME, pp);
		
		Map additionalPreferencePanels = ArchipelagoFrame.getPreferencePanels();
		for(Iterator it = additionalPreferencePanels.keySet().iterator(); it.hasNext(); ){
			String name = (String)it.next();
			PreferencePanel app = (PreferencePanel)additionalPreferencePanels.get(name);
			PreferencesUtils.deployPreferencesService(this, bottomIface, name, app);
		}
		
		NotifyDocUtils.deployNotifyDocService(this, bottomIface);
	}

	class DebugMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			System.out.println(m);
			System.out.println();
		}
	}

	class WindowClosingAdapter extends WindowAdapter{
		public void windowClosed(WindowEvent evt){
			openWindows.removeElement(evt.getWindow());
		}
	}
	
	class ArchipelagoLifecycleProcessor extends LifecycleAdapter{
		public void end(){
			JFrame[] allFrames = null;
			synchronized(openWindows){
				allFrames = (JFrame[])openWindows.toArray(new JFrame[0]);
			}
			for(int i = 0; i < allFrames.length; i++){
				if(allFrames[i] != null){
					allFrames[i].setVisible(false);
					allFrames[i].dispose();
				}
			}
			NotifyDocUtils.undeployNotifyDocService(ArchipelagoC2Component.this, bottomIface);
		}
	}

	public void docSaving(ObjRef documentRef){
		ArchipelagoFrame f = getWindow(documentRef);
		if(f != null){
			f.writeHints();
		}
	}
	
	public void docClosing(ObjRef documentRef){
		ArchipelagoFrame f = getWindow(documentRef);
		if(f != null){
			f.writeHints();
		}
	}
	
	public void invoke(InvokeMessage im){
		if(im.getServiceName().equals(SERVICE_NAME)){
			String url = im.getArchitectureURL();

			if(url == null){
				newWindow();
			}
			else{
				ObjRef xArchRef = xarch.getOpenXArch(url);
				if(xArchRef != null){
					ArchipelagoFrame f = getWindow(xArchRef);
					if(f != null){
						showWindow(xArchRef, null);
						f.requestFocus();
					}
					else{
						newWindow(xArchRef);
					}
				}
				else{
					newWindow(url);
				}
			}
		}
	}

	class FocusEditorMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof FocusEditorMessage){
				FocusEditorMessage fem = (FocusEditorMessage)m;
				if(!EditorUtils.appliesToEditor(PRODUCT_NAME, fem)){
					return;
				}
				ObjRef xArchRef = fem.getXArchRef();
				ObjRef[] refs = fem.getRefs();
				ObjRef ref = null;
				if((refs != null) && (refs.length > 0)){
					ref = refs[0];
				}
				int focusType = fem.getFocusType();
				if(focusType == FocusEditorMessage.FOCUS_EXISTING_DOCS){
					ArchipelagoFrame f = getWindow(xArchRef);
					if(f == null){
						//No open window on that document.
						return;
					}
					if(ref != null){
						f.requestFocus();
						f.showRef(ref);
					}
				}
				else if(focusType == FocusEditorMessage.FOCUS_OPEN_DOCS){
					if(openWindows.size() == 0){
						return;
					}
					ArchipelagoFrame f = getWindow(xArchRef);
					if(f == null){
						f = newWindow(xArchRef);
					}
					if(ref != null){
						f.requestFocus();
						f.showRef(ref);
					}
				}
				else if(focusType == FocusEditorMessage.FOCUS_OPEN_EDITORS){
					ArchipelagoFrame f = getWindow(xArchRef);
					if(f == null){
						f = newWindow(xArchRef);
					}
					if(ref != null){
						f.requestFocus();
						f.showRef(ref);
					}
				}
			}
		}
	}
	
	//-----------------------------------------
	
	class ArchipelagoMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			//Forward all messages to the Frames in case they care.
			synchronized(openWindows){
				for(int i = 0; i < openWindows.size(); i++){
					((ArchipelagoFrame)openWindows.elementAt(i)).handle(m);
				}
			}
		}
	}
	/*
	class StateChangeMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof XArchTransactionEvent){
				XArchTransactionEvent evt = (XArchTransactionEvent)m;
			}
			else if(m instanceof NamedPropertyMessage){
				NamedPropertyMessage npm = (NamedPropertyMessage)m;
				try{
					if(npm.getBooleanParameter("stateChangeMessage")){
						//System.out.println("Got state change message: " + m);
						//System.out.println("ArchEditComponent component got state change message: " + m);
						Object evtObject = npm.getParameter("paramValue0");
						
						if(evtObject instanceof XArchFlatEvent){
							XArchFlatEvent evt = (XArchFlatEvent)evtObject;
							handleStateChangeEvent(evt);
						}
						else if(evtObject instanceof XArchFileEvent){
							XArchFileEvent evt = (XArchFileEvent)evtObject;
							handleFileEvent(evt);
						}
						return;
					}
				}
				catch(Exception e){
					//if(!(e instanceof IllegalArgumentException)){
					//	e.printStackTrace();
					//}
				}
			}
			
			return;
		}
	}
	*/
	
	public void handleFileEvent(XArchFileEvent evt){
		synchronized(openWindows){
			ObjRef xArchRef = evt.getXArchRef();
			int size = openWindows.size();
			for(int i = (size - 1); i >= 0; i--){
				ArchipelagoFrame f = (ArchipelagoFrame)openWindows.elementAt(i);
				
				if(!f.isShowing()){
					openWindows.removeElementAt(i);
				}
				else{
					ObjRef fXArchRef = f.getDocumentSource();
					if((fXArchRef != null) && (xArchRef.equals(fXArchRef))){
						f.handleXArchFileEvent(evt);
						if(evt.getEventType() == XArchFileEvent.XARCH_CLOSED_EVENT){
							f.closeDocument(false);
						}
					}
				}
			}
		}			
	}
	
	public void handleStateChangeEvent(XArchFlatEvent evt){
		ArchipelagoFrame hasFocus = null;
		ObjRef elt = evt.getSource();
		
		synchronized(openWindows){
			int size = openWindows.size();
			if(size > 0){
				ObjRef xArchRef = null;
				try{
					xArchRef = xarch.getXArch(elt);
				}
				catch(Exception e){
					return;
				}

				for(int i = (size - 1); i >= 0; i--){
					ArchipelagoFrame f = (ArchipelagoFrame)openWindows.elementAt(i);
					if(f.hasFocus()){
						hasFocus = f;
						break;
					}
					else if(f.getFocusOwner() != null){
						hasFocus = f;
						break;
					}
				}
				for(int i = (size - 1); i >= 0; i--){
					ArchipelagoFrame f = (ArchipelagoFrame)openWindows.elementAt(i);
					if(!f.isShowing()){
						openWindows.removeElementAt(i);
					}
					else{
						ObjRef fXArchRef = f.getDocumentSource();
						if((fXArchRef != null) && (xArchRef.equals(fXArchRef))){
							f.handleXArchFlatEvent(evt);
						}
					}
				}
				if(hasFocus != null){
					hasFocus.requestFocus();
				}
			}
			
		}
	}
	
	public ArchipelagoFrame newWindow(){
		synchronized(openWindows){
			ArchipelagoFrame f = new ArchipelagoFrame(this, xarch, preferences, gli, bni);
			f.addWindowListener(new WindowClosingAdapter());
			openWindows.addElement(f);
			return f;
		}
	}
	
	public ArchipelagoFrame newWindow(String url){
		ObjRef xArchRef = xarch.getOpenXArch(url);
		return newWindow(xArchRef);
	}
	
	public ArchipelagoFrame newWindow(ObjRef xArchRef){
		ArchipelagoFrame f = newWindow();
		f.openXArch(xArchRef);
		return f;
	}
	
	public ArchipelagoFrame getWindow(ObjRef xArchRef){
		synchronized(openWindows){
			int size = openWindows.size();
			for(int i = (size - 1); i >= 0; i--){
				ArchipelagoFrame f = (ArchipelagoFrame)openWindows.elementAt(i);
				ObjRef fXArchRef = f.getDocumentSource();
				if(fXArchRef != null){
					if(fXArchRef.equals(xArchRef)){
						return f;
					}
				}
			}
		}
		return null;
	}
	
	public void showWindow(ObjRef xArchRef, String id){
		synchronized(openWindows){
			int size = openWindows.size();
			for(int i = (size - 1); i >= 0; i--){
				ArchipelagoFrame f = (ArchipelagoFrame)openWindows.elementAt(i);
				ObjRef fXArchRef = f.getDocumentSource();
				if((fXArchRef != null) && (xArchRef.equals(fXArchRef))){
					f.showID(id);
					return;
				}
			}
			//We didn't find a window with that URL
			ArchipelagoFrame newFrame = newWindow(xArchRef);
			newFrame.showID(id);
		}
	}
	
}

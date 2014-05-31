package archstudio.comp.typewrangler;

import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;
import archstudio.invoke.*;
import archstudio.editors.*;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

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

public class TypeWranglerC2Component extends AbstractC2DelegateBrick 
implements c2.fw.Component, InvokableBrick{
	public static final String PRODUCT_NAME = "Type Wrangler";
	public static final String SERVICE_NAME = "Editors/Type Wrangler";
	
	protected Vector openWindows;
	protected XArchFlatTransactionsInterface xarch;
		
	public TypeWranglerC2Component(Identifier id){
		super(id);
		xarch = (XArchFlatTransactionsInterface)EBIWrapperUtils.addExternalService(this, topIface, XArchFlatTransactionsInterface.class);

		openWindows = new Vector();
		this.addLifecycleProcessor(new TypeWranglerLifecycleProcessor());
		this.addMessageProcessor(new InvokeTypeWranglerMessageProcessor());

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

		//this.addMessageProcessor(new FocusEditorMessageProcessor());
		//EBIWrapperUtils.addThreadMessageProcessor(this, new MessageProcessor[]{ new StateChangeMessageProcessor()});
		//addMessageProcessor(new DebugMessageProcessor());
		InvokeUtils.deployInvokableService(this, bottomIface, 
			SERVICE_NAME, 
			"A GUI-based tool for managing bricks, types, interfaces, and signatures.");
	}

	class DebugMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			System.out.println(m);
			System.out.println();
		}
	}

	class InvokeTypeWranglerMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof InvokeTypeWranglerMessage){
				InvokeTypeWranglerMessage itwm = (InvokeTypeWranglerMessage)m;
				String url = itwm.getUrl();
				ObjRef elementRef = itwm.getElementRef();
				String id = (String)xarch.get(elementRef, "id");
				if(id == null){
					newWindow(url);
				}
				else{
					ObjRef xArchRef = xarch.getOpenXArch(url);
					if(xArchRef != null){
						showWindow(xArchRef, id);
					}
				}
			}
		}
	}
	
	class WindowClosingAdapter extends WindowAdapter{
		public void windowClosed(WindowEvent evt){
			openWindows.removeElement(evt.getWindow());
		}
	}
	
	class TypeWranglerLifecycleProcessor extends LifecycleAdapter{
		public void end(){
			for(int i = 0; i < openWindows.size(); i++){
				((JFrame)openWindows.elementAt(i)).setVisible(false);
				((JFrame)openWindows.elementAt(i)).dispose();
			}
		}
	}
	
	public void invoke(InvokeMessage im){
		if(im.getServiceName().equals(SERVICE_NAME)){
			String url = im.getArchitectureURL();

			if(url == null){
				newWindow();
			}
			else{
				newWindow(url);
			}
		}
	}

	/*
	class FocusEditorMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof FocusEditorMessage){
				FocusEditorMessage fem = (FocusEditorMessage)m;
				String uri = fem.getArchitectureURI();
				ObjRef[] refs = fem.getRefs();
				ObjRef ref = null;
				if((refs != null) && (refs.length > 0)){
					ref = refs[0];
				}
				int focusType = fem.getFocusType();
				if(focusType == FocusEditorMessage.FOCUS_EXISTING_DOCS){
					ArchEditFrame f = getWindow(uri);
					if(f == null){
						//No open window on that document.
						return;
					}
					if(ref != null){
						f.showRef(ref);
					}
				}
				else if(focusType == FocusEditorMessage.FOCUS_OPEN_DOCS){
					if(openWindows.size() == 0){
						return;
					}
					ArchEditFrame f = getWindow(uri);
					if(f == null){
						f = newWindow(uri);
					}
					if(ref != null){
						f.showRef(ref);
					}
				}
				else if(focusType == FocusEditorMessage.FOCUS_OPEN_EDITORS){
					ArchEditFrame f = getWindow(uri);
					if(f == null){
						f = newWindow(uri);
					}
					if(ref != null){
						f.showRef(ref);
					}
				}
			}
		}
	}
	*/
	
	//-----------------------------------------
	
	public void handleFileEvent(XArchFileEvent evt){
		synchronized(openWindows){
			int size = openWindows.size();
			for(int i = (size - 1); i >= 0; i--){
				TypeWranglerFrame f = (TypeWranglerFrame)openWindows.elementAt(i);
				ObjRef fXArchRef = f.getDocumentSource();
				if((fXArchRef != null) && (fXArchRef.equals(evt.getXArchRef()))){
					if(evt.getEventType() == XArchFileEvent.XARCH_RENAMED_EVENT){
						f.setWindowTitle(evt.getAsURL());
					}
					else if(evt.getEventType() == XArchFileEvent.XARCH_CLOSED_EVENT){
						f.closeDocument();
					}
				}
			}
		}			
	}
	
	public void handleStateChangeEvent(XArchFlatEvent evt){
		TypeWranglerFrame hasFocus = null;
		synchronized(openWindows){
			int size = openWindows.size();
			for(int i = (size - 1); i >= 0; i--){
				TypeWranglerFrame f = (TypeWranglerFrame)openWindows.elementAt(i);
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
				TypeWranglerFrame f = (TypeWranglerFrame)openWindows.elementAt(i);
				if(!f.isShowing()){
					openWindows.removeElementAt(i);
				}
				else{
					f.handleXArchFlatEvent(evt);
				}
			}
			if(hasFocus != null){
				hasFocus.requestFocus();
			}
		}
	}
	
	public TypeWranglerFrame newWindow(){
		synchronized(openWindows){
			TypeWranglerFrame f = new TypeWranglerFrame(this, xarch);
			f.addWindowListener(new WindowClosingAdapter());
			openWindows.addElement(f);
			return f;
		}
	}
	
	public TypeWranglerFrame newWindow(String uri){
		ObjRef xArchRef = xarch.getOpenXArch(uri);
		if(xArchRef != null){
			return newWindow(xArchRef);
		}
		return null;
	}
	
	public TypeWranglerFrame newWindow(ObjRef xArchRef){
		TypeWranglerFrame f = newWindow();
		f.openXArch(xArchRef);
		return f;
	}
	
	public TypeWranglerFrame getWindow(ObjRef xArchRef){
		synchronized(openWindows){
			int size = openWindows.size();
			for(int i = (size - 1); i >= 0; i--){
				TypeWranglerFrame f = (TypeWranglerFrame)openWindows.elementAt(i);
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
				TypeWranglerFrame f = (TypeWranglerFrame)openWindows.elementAt(i);
				ObjRef fXArchRef = f.getDocumentSource();
				if(fXArchRef.equals(xArchRef)){
					f.showID(id);
					f.requestFocus();
					return;
				}
			}
			//We didn't find a window with that URL
			TypeWranglerFrame newFrame = newWindow(xArchRef);
			newFrame.showID(id);
			newFrame.requestFocus();
		}
	}
	
}

package archstudio.comp.helloworld;

//Includes classes that will help to make our component
//"Invokable" from the ArchStudio File Manager/Invoker
import archstudio.invoke.*;

//The c2.fw framework
import c2.fw.*;

//Support for "legacy" C2 components
import c2.legacy.*;

//Includes classes that allow our component to
//make Event-based Procedure Calls (EPCs)
import c2.pcwrap.*;

//Imported to support xArchADT use
import edu.uci.ics.xarchutils.*;

//Standard Java imports
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

//HelloWorldC2Component is a "legacy" C2 component that will be
//inserted in the architecture.  This means it has only 2 interfaces:
//TOP and BOTTOM.  A lot of boilerplate functionality for such
//components is implemented in AbstractC2DelegateBrick, so we extend
//that class when making our new C2 component.

public class HelloWorldC2Component extends AbstractC2DelegateBrick implements InvokableBrick{
	public static final String PRODUCT_NAME = "Hello World Component";
	public static final String PRODUCT_VERSION = "1.0";
	
	//This XArchFlatInterface is an EPC interface implemented in another component,
	//in this case xArchADT.  Because this component uses c2.fw's EBI wrapper mechanism, 
	//we can call functions in this interface directly and all the communication gets translated
	//from procedure calls (PC) to EPC.
	protected XArchFlatInterface xarch;
	
	//The main application window
	protected HelloWorldFrame hwFrame = null;

	public HelloWorldC2Component(Identifier id){
		super(id);
		this.addMessageProcessor(new StateChangeMessageProcessor());
		xarch = (XArchFlatInterface)EBIWrapperUtils.addExternalService(this, topIface, XArchFlatInterface.class);
		InvokeUtils.deployInvokableService(this, bottomIface, 
			"ArchStudio 3 Development/Hello World Component", 
			"A demonstration component for ArchStudio 3.");
		addLifecycleProcessor(new HelloWorldLifecycleProcessor());
	}

	public HelloWorldC2Component(Identifier id, InitializationParameter[] initParams){
		this(id);
		for(int i = 0; i < initParams.length; i++){
			System.out.println(initParams[i]);
		}	
	}

	class HelloWorldLifecycleProcessor extends LifecycleAdapter{
		public void end(){
			if(hwFrame != null){
				hwFrame.setVisible(false);
				hwFrame.dispose();
			}
		}
	}
	
	//This gets called automatically when we get an invoke message from
	//the invoker.
	public void invoke(InvokeMessage m){
		//Let's open a new window.
		newWindow();
	}

	public void newWindow(){
		//This makes sure we only have one active window open.
		if(hwFrame == null){
			hwFrame = new HelloWorldFrame();
		}
		else{
			hwFrame.requestFocus();
		}
	}
	
	
	class StateChangeMessageProcessor implements MessageProcessor{
		
		//This message processor handles state change messages
		//coming in from our top interface.  We're looking for
		//state change messages that have, as their 0th parameter,
		//an XArchFileEvent
		public void handle(Message m){
			if(m instanceof NamedPropertyMessage){
				NamedPropertyMessage npm = (NamedPropertyMessage)m;
				try{
					//If we get a state change message from above, it may have come from
					//xArchADT.  We need to pay attention to this message if it indicates
					//a file was opened or closed.
					if(npm.getBooleanParameter("stateChangeMessage")){
						//The first parameter of the state change message, if it does
						//indicate a change in the open-file-list, will be an XArchFileEvent
						if(npm.getParameter("paramValue0") instanceof XArchFileEvent){
							XArchFileEvent evt = (XArchFileEvent)npm.getParameter("paramValue0");
							
							//If it was, then we'll cheat a little bit, and instead of modifying our
							//list based on the event, we'll just re-ask xArchADT to give us an updated
							//list of all the open files.
							if(hwFrame != null){
								hwFrame.updateOpenURLs();
								return;
							}
							else{
								return;
							}
						}
					}
					return;
				}
				catch(Exception e){
					return;
				}
			}
		}
	}
	
	class HelloWorldFrame extends JFrame{
		
		protected JList urlList;
		
		public HelloWorldFrame(){
			super(PRODUCT_NAME + " " + PRODUCT_VERSION);
			init();
		}
		
		//This is pretty standard Swing GUI stuff in Java.
		private void init(){
			Toolkit tk = getToolkit();
			Dimension screenSize = tk.getScreenSize();
			double xSize = (200);
			double ySize = (100);
			double xPos = (screenSize.getWidth() * 0.70);
			double yPos = (screenSize.getHeight() * 0.70);

			urlList = new JList(new DefaultListModel());
			JPanel mainPanel = new JPanel();
			
			mainPanel.setLayout(new BorderLayout());
			mainPanel.add("North", new JLabel("Open URLs:"));
			mainPanel.add("Center", urlList);
			
			this.getContentPane().add(new JScrollPane(mainPanel));

			setVisible(true);
			setSize((int)xSize, (int)ySize);
			setLocation((int)xPos, (int)yPos);
			setVisible(true);
			paint(getGraphics());

			this.addWindowListener(new HelloWorldWindowAdapter());
			
			//Go get the initial list of URLs.
			updateOpenURLs();
		}
		
		class HelloWorldWindowAdapter extends WindowAdapter{
			public void windowClosing(WindowEvent e){
				destroy();
				dispose();
				setVisible(false);
				hwFrame = null;
			}
		}				
		
		public void updateOpenURLs(){
			DefaultListModel lm = (DefaultListModel)urlList.getModel();
			lm.removeAllElements();
			
			//This innocuous call gets translated by the local proxy
			//(XArchFlatInterface xarch) into an EPC and sent off, via an event
			//to xArchADT.  The result is marshalled into an event and returned
			//here.  All this magic happens under the covers because of the
			//services provided by EBIWrapperComponent.
			String[] urls = xarch.getOpenXArchURIs();
			for(int i = 0; i < urls.length; i++){
				lm.addElement(urls[i]);
			}
			repaint();
		}
	}

}

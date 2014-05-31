package archstudio.comp.pladiffdriver;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

//This is imported to support xArchADT use
import edu.uci.ics.xarchutils.*;
import archstudio.invoke.*;
import archstudio.comp.pladiff.*;

//Standard Java imports
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * This is the C2 Componenent for the PLADiffDriver.
 * It provides the basic UI for invoking the PLADiff service.   
 * @author Ping Chen <A HREF="mailto:pchen@isc.uci.com">(critchlm@uci.edu)</A>
 */
public class PLADiffDriverC2Component extends AbstractC2DelegateBrick 
	implements InvokableBrick, c2.fw.Component
{
	public static final String PRODUCT_NAME = "PLADiff Driver Component";
	public static final String PRODUCT_VERSION = "2.0";
	
	//This XArchFlatInterface is an EPC interface implemented on another component,
	//in this case xArchADT.  Because this is an EBIWrapperComponent, we can call
	//functions in this interface directly and all the communication gets translated
	//from procedure calls (PC) to EPC.
	protected XArchFlatInterface xArch;
	
	protected String archDiffDriverID;
	protected PLADiffDriverFrame archDiffFrame = null;

	
	/**
	 * The PLADiff driver listens to asynchronous messages which are passed between
	 * C2 components. Currently the driver has message processor inner classes which can
	 * handle StateChangeMessages
	 * @param id		The unique identifier for the archDiffDriverC2Componenent
	 */
	public PLADiffDriverC2Component( Identifier id )
	{
		super( id );
		archDiffDriverID = id.toString();

		XArchEventProvider xarchEventProvider = 
			(XArchEventProvider)EBIWrapperUtils.createStateChangeProviderProxy(this, topIface,
			XArchEventProvider.class);
		
		XArchFileListener fileListener = new XArchFileListener(){
			public void handleXArchFileEvent(XArchFileEvent evt) {
				if(archDiffFrame != null){
					archDiffFrame.updateOpenURLs();
				}
			}
		};
		xarchEventProvider.addXArchFileListener(fileListener);
		
		this.addMessageProcessor( new StatusMessageProcessor());
		
		xArch = ( XArchFlatInterface )EBIWrapperUtils.addExternalService(
			this, topIface, XArchFlatInterface.class );
		
		InvokeUtils.deployInvokableService(this, bottomIface, 
			"Differencing and Merging/Product Line Differencing Engine GUI", 
			"Generates architectural differences from within product lines");
	}
	
	public void invoke(InvokeMessage im){
		newWindow();
	}
	
	//This is called when we get an invoke message from the invoker.
	public void newWindow(){
		//System.out.println("New window.");
		
		//This makes sure we only have one active window open.
		if(archDiffFrame == null){
			//System.out.println("Creating new frame.");
			archDiffFrame = new PLADiffDriverFrame();
		}
		else
		{
			archDiffFrame.requestFocus();
			//archDiffFrame.setVisible(true);
		}
	}
	
	/**
	 * The StatusMessageProcessor listens to messages which may have come from either the PLADiff.
	 * If this is found to be true, then the driver must update the progress.
	 * This is done by calling updateProgress() which is a method in the archDiffFrame class.
	 */
	class StatusMessageProcessor implements MessageProcessor
	{
		public void handle(Message m)
		{
			if(m instanceof PLADiffStatusMessage)
			{
				PLADiffStatusMessage message = (PLADiffStatusMessage)m;
				if(archDiffDriverID == message.getComponentID())
				{
					archDiffFrame.updateProgress(message);
				}
			}
		}
	}
	
	/**
	 * This is the main GUI Driver for PLADiff component in archstudio.
	 * This class provides the outside frame that will house everything
	 */
	class PLADiffDriverFrame extends JFrame
	{
		
		protected PLADiffPanel archDiffPanel;
		protected MessageListener reqListener;  // this is the listener
												// that will broadcast requests
												
												
		class PLADiffDriverWindowAdapter extends WindowAdapter
		{
			public void windowClosing(WindowEvent e)
			{
				destroy();
				dispose();
				setVisible(false);
				archDiffFrame = null;
			}
		}
		
		public PLADiffDriverFrame()
		{
			super(PRODUCT_NAME + " " + PRODUCT_VERSION);
			archstudio.Branding.brandFrame(this);
			init();
		}
		
		//This is pretty standard Swing GUI stuff in Java.
		private void init()
		{
			Toolkit tk = getToolkit();
			Dimension screenSize = tk.getScreenSize();
			double xSize = (950);
			double ySize = (475);
			double xPos = (screenSize.getWidth() * 0.05);
			double yPos = (screenSize.getHeight() * 0.10);

			
			//This is the tabbedPabe for the create table panel and the corresponding panels
			JTabbedPane tabbedPane = new JTabbedPane();
			archDiffPanel = new PLADiffPanel( xArch, archDiffDriverID );
			
			// This is to allow the inner panels to sent their messages
			reqListener = new MessageListener( )
						{
							public void messageSent(Message m)
							{
								sendToAll(m, topIface);
							}
						};
			archDiffPanel.addMessageListener( reqListener );
			
			
			// adds diff driver panel to the overall tab
			tabbedPane.addTab( "PLADiff", archDiffPanel );

			
			/*************** End of Progress Panel ***************/
			this.getContentPane( ).add( tabbedPane );
			this.setVisible( true );
			this.setSize( ( int )xSize, ( int )ySize );
			this.setLocation( ( int )xPos, ( int )yPos );
			this.setVisible( true );
			this.paint( getGraphics( ) );
			this.addWindowListener( new PLADiffDriverWindowAdapter( ) );

		}
		/**
		 * This method is necessary to invoke when a StateChangeMessage has been 
		 * sent from the xArchADT. The list of open urls is removed, and a new list is created by generating 
		 * a new call to getOpenXArchURLs, thus repopulating the list with the current available architectures. 
		 */
		public void updateOpenURLs( )
		{
			archDiffPanel.updateOpenURLs( );
		}
		
		/**
		 * This method is necessary to invoke when a StatusMessage has been sent from the PLADiff.
		 */
		public void updateProgress( Message msg )
		{
			if( msg instanceof PLADiffStatusMessage )
			{
				PLADiffStatusMessage diffStatMsg = ( PLADiffStatusMessage )msg;
				
				archDiffPanel.updateProgress( diffStatMsg );
			}
		}
	}
}



package archstudio.comp.plamergedriver;


import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

//This is imported to support xArchADT use
import edu.uci.ics.xarchutils.*;
import archstudio.invoke.*;
import archstudio.comp.plamerge.*;

//Standard Java imports
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * This is the C2 Componenent for the PLAMergeDriver.
 * It provides the basic UI for invoking the PLAMerge service.   
 * @author Ping Chen <A HREF="mailto:pchen@isc.uci.com">(critchlm@uci.edu)</A>
 */
public class PLAMergeDriverC2Component extends AbstractC2DelegateBrick 
	implements InvokableBrick, c2.fw.Component
{
	public static final String PRODUCT_NAME = "PLAMerge Driver Component";
	public static final String PRODUCT_VERSION = "2.0";
	
	//This XArchFlatInterface is an EPC interface implemented on another component,
	//in this case xArchADT.  Because this is an EBIWrapperComponent, we can call
	//functions in this interface directly and all the communication gets translated
	//from procedure calls (PC) to EPC.
	protected XArchFlatInterface xArch;
	
	protected String archMergeDriverID;
	protected PLAMergeDriverFrame archMergeFrame = null;

	
	/**
	 * The PLAMerge driver listens to asynchronous messages which are passed between
	 * C2 components. Currently the driver has message processor inner classes which can
	 * handle StateChangeMessages
	 * @param id		The unique identifier for the PLAMergeDriverC2Componenent
	 */
	public PLAMergeDriverC2Component( Identifier id )
	{
		super( id );
		archMergeDriverID = id.toString();

		XArchEventProvider xarchEventProvider = 
			(XArchEventProvider)EBIWrapperUtils.createStateChangeProviderProxy(this, topIface,
			XArchEventProvider.class);
		
		XArchFileListener fileListener = new XArchFileListener(){
			public void handleXArchFileEvent(XArchFileEvent evt) {
				if(archMergeFrame != null){
					archMergeFrame.updateOpenURLs();
				}
			}
		};
		xarchEventProvider.addXArchFileListener(fileListener);
		
		this.addMessageProcessor( new StatusMessageProcessor());
		
		xArch = ( XArchFlatInterface )EBIWrapperUtils.addExternalService(
			this, topIface, XArchFlatInterface.class );
		
		InvokeUtils.deployInvokableService(this, bottomIface, 
			"Differencing and Merging/Product Line Merging Engine", 
			"Merges product-line differences into a product line");
	}
	
	public void invoke(InvokeMessage im){
		newWindow();
	}
	
	//This is called when we get an invoke message from the invoker.
	public void newWindow(){
		//System.out.println("New window.");
		
		//This makes sure we only have one active window open.
		if(archMergeFrame == null){
			//System.out.println("Creating new frame.");
			archMergeFrame = new PLAMergeDriverFrame();
		}
		else
		{
			archMergeFrame.requestFocus();
			//archMergeFrame.setVisible(true);
		}
	}

	/**
	 * The StatusMessageProcessor listens to messages which may have come from either the PLAMerge.
	 * If this is found to be true, then the driver must update the progress.
	 * This is done by calling updateProgress() which is a method in the archMergeFrame class.
	 */
	class StatusMessageProcessor implements MessageProcessor
	{
		public void handle(Message m)
		{
			if(m instanceof PLAMergeStatusMessage)
			{
				PLAMergeStatusMessage message = (PLAMergeStatusMessage)m;
				if(archMergeDriverID == message.getComponentID())
				{
					archMergeFrame.updateProgress(message);
				}
			}
		}
	}
	
	/**
	 * This is the main GUI Driver for PLAMerge component in archstudio.
	 * This class provides the outside frame that will house everything
	 */
	class PLAMergeDriverFrame extends JFrame
	{
		
		protected PLAMergePanel archMergePanel;
		protected MessageListener reqListener;  // this is the listener
												// that will broadcast requests
												
												
		class PLAMergeDriverWindowAdapter extends WindowAdapter
		{
			public void windowClosing(WindowEvent e)
			{
				destroy();
				dispose();
				setVisible(false);
				archMergeFrame = null;
			}
		}
		
		public PLAMergeDriverFrame()
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
			double xSize = (750);
			double ySize = (425);
			double xPos = (screenSize.getWidth() * 0.05);
			double yPos = (screenSize.getHeight() * 0.10);

			
			//This is the tabbedPabe for the create table panel and the corresponding panels
			JTabbedPane tabbedPane = new JTabbedPane();
			archMergePanel = new PLAMergePanel( xArch, archMergeDriverID );
			
			// This is to allow the inner panels to sent their messages
			reqListener = new MessageListener( )
						{
							public void messageSent(Message m)
							{
								sendToAll(m, topIface);
							}
						};
			archMergePanel.addMessageListener( reqListener );
			
			
			// adds merge driver panel to the overall tab
			tabbedPane.addTab( "PLAMerge", archMergePanel );

			
			/*************** End of Progress Panel ***************/
			this.getContentPane( ).add( tabbedPane );
			this.setVisible( true );
			this.setSize( ( int )xSize, ( int )ySize );
			this.setLocation( ( int )xPos, ( int )yPos );
			this.setVisible( true );
			this.paint( getGraphics( ) );
			this.addWindowListener( new PLAMergeDriverWindowAdapter( ) );

		}
		/**
		 * This method is necessary to invoke when a StateChangeMessage has been 
		 * sent from the xArchADT. The list of open urls is removed, and a new list is created by generating 
		 * a new call to getOpenXArchURLs, thus repopulating the list with the current available architectures. 
		 */
		public void updateOpenURLs( )
		{
			archMergePanel.updateOpenURLs( );
		}
		
		/**
		 * This method is necessary to invoke when a StatusMessage has been sent from the PLAMerge.
		 */
		public void updateProgress( Message msg )
		{
			if( msg instanceof PLAMergeStatusMessage )
			{
				PLAMergeStatusMessage mergeStatMsg = ( PLAMergeStatusMessage )msg;
				
				archMergePanel.updateProgress( mergeStatMsg );
			}
		}
	}
}



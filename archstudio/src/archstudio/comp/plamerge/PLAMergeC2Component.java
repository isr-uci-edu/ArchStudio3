// Akash Garg
// PLAMergeC2Component.java
// Most of this code is adopted from Ping's PLADiffC2Component.java

package archstudio.comp.plamerge;

//The c2.fw framework
import c2.fw.*;

//Support for "legacy" C2 components
import c2.legacy.*;

//Includes classes that allow our component to
//make Event-based Procedure Calls (EPCs)
import c2.pcwrap.*;

//Imported to support xArchADT use
import edu.uci.ics.xarchutils.*;

/**
 * This is the outside wrapper for PLAMerge component.  The PLAMerge component is
 * asynchronous and this class provides the message handling functions.
 * 
 * @author Akash Garg <A HREF="mailto:agarg@uci.edu">(agarg@uci.edu)</A>
 */

public class PLAMergeC2Component extends AbstractC2DelegateBrick
{
	// constants from the boiler plate code
	public static final String PRODUCT_NAME = "PLAMerge Component";
	public static final String PRODUCT_VERSION = "2.0";
	
	//This XArchFlatInterface is an EPC interface implemented
	//in another component, in this case xArchADT.  Because
	// this component uses c2.fw's EBI wrapper mechanism,
	//we can call functions in this interface directly and
	//all the communication gets translated from procedure
	//calls (PC) to EPC.
	protected XArchFlatInterface xArch;

	// This stores an instance of a message listener which will 
	// just blast out a message to the bottom interface on invocation
	protected MessageListener msgListener;
	
	// This internal class handles all the messages
	public class PLAMergeMessageProcessor implements MessageProcessor
	{
		public void handle( Message m )
		{
			// Check to see if its an invokem essage
			if( m instanceof PerformPLAMergeMessage )
			{
				PerformPLAMergeMessage msg = ( PerformPLAMergeMessage ) m;
				PLAMergeImpl archMerge = new PLAMergeImpl( xArch );
				
				// Extract all the arguments from the message
				String archURL = msg.getArchURL( );
				String targetArchURL = msg.getTargetArchURL( );
				String diffURL = msg.getDiffURL( );
				
				String startingID = msg.getStartingID( );
				boolean isStructural = msg.getIsStructural( );
				
				// we store the ID so that the status messages will also contain this ID
				// thus allowing particular components to only get the messages he cares about
				String msgID = msg.getComponentID( );
				
				try
				{
					archMerge.merge( diffURL, archURL, targetArchURL, 
						startingID, isStructural /*, msgID*/ );
					
					// Just to send off a message that we are all done here.
					PLAMergeStatusMessage completeMsg = new PLAMergeStatusMessage(
						targetArchURL, msgID, 1, 1, true, false, null );
					
					sendToAll( completeMsg, bottomIface );
					
				}
				// Something went wrong...
				catch( Exception e )
				{
					
					// Send off a message to report the error.
					PLAMergeStatusMessage errMsg = new PLAMergeStatusMessage(
						targetArchURL, msgID, 0, 0, true, true, e );
					sendToAll( errMsg, bottomIface );
					
				}
			}
		}
	}
	
	
	public PLAMergeC2Component( Identifier id )
	{
		super(id);
		
		// gets xArch adt
		xArch = ( XArchFlatInterface )EBIWrapperUtils.addExternalService( this,
			topIface, XArchFlatInterface.class );

		msgListener = new MessageListener( )
						{
							public void messageSent(Message m)
							{
								sendToAll(m, bottomIface);
							}
						};
		
		// Adds the message handler for this component
		// This allows the PLAMerge to be asynchronous and completely event based
		addMessageProcessor( new PLAMergeMessageProcessor( ) );
	}
}

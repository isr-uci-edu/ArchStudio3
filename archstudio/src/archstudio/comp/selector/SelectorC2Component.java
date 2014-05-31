// Ping Chen
// SelectorC2Component.java

package archstudio.comp.selector;

//The c2.fw framework
import c2.fw.*;

//Support for "legacy" C2 components
import c2.legacy.*;

//Includes classes that allow our component to
//make Event-based Procedure Calls (EPCs)
import c2.pcwrap.*;

//Imported to support xArchADT use
import edu.uci.ics.xarchutils.*;
import archstudio.comp.booleaneval.*;

/**
 * This is the outside wrapper for Selector component.  The selector component is
 * asynchronous and this class provides the message handling as well as sends the messages
 *
 * 
 * @author Ping Hsin Chen <A HREF="mailto:pingc@hotmail.com">(pingc@hotmail.com)</A>
 */
 
public class SelectorC2Component extends AbstractC2DelegateBrick
{
    // constants from the boiler plate code
    public static final String PRODUCT_NAME = "Selector Component";
	public static final String PRODUCT_VERSION = "2.3";
    
    //This XArchFlatInterface is an EPC interface implemented
	//in another component, in this case xArchADT.  Because
	// this component uses c2.fw's EBI wrapper mechanism,
	//we can call functions in this interface directly and
	//all the communication gets translated from procedure
	//calls (PC) to EPC.
	protected XArchFlatInterface xarch;
	protected IBooleanEval boolEval;
	
	protected SelectorImpl selector;
	
	// This internal class handles all the messages
	public class SelectorMessageProcessor implements MessageProcessor
	{
		public void handle( Message m )
		{
			// Check to see if its an invokem essage
			if( m instanceof PerformArchSelectorMessage )
			{
				PerformArchSelectorMessage msg = ( PerformArchSelectorMessage ) m;
				
				// Extract all the arguments from the message
				String archURL = msg.getArchURL( );
				String targetURL = msg.getTargetArchURL( );
				SymbolTable symTab = msg.getSymbolTable( );
				String startingID = msg.getStartingID( );
				boolean isStructural = msg.getIsStructural( );
				// we store the ID so that the status messages will also contain this ID
				// thus allowing particular components to only get the messages he cares about
				String msgID = msg.getComponentID( );
				
				try
				{
					selector.select( archURL, targetURL, symTab, startingID, isStructural, msgID );
					
					// Just to send off a message that we are all done here.
					ArchSelectorStatusMessage completeMsg = new ArchSelectorStatusMessage(
						targetURL, msgID, 1, 1, true, false, null );
					
					sendToAll( completeMsg, bottomIface );
				}
				// Something went wrong...
				catch( Exception e )
				{
					// Send off a message to report the error.
					ArchSelectorStatusMessage errMsg = new ArchSelectorStatusMessage(
						targetURL, msgID, 0, 0, true, true, e );
					sendToAll( errMsg, bottomIface );
				}
			}
		}
	}
		
	public SelectorC2Component( Identifier id )
	{
		super(id);
        
        // gets xArch adt
		xarch = ( XArchFlatInterface )EBIWrapperUtils.addExternalService( this,
			topIface, XArchFlatInterface.class );
		// need to add the boolean evaluator's service
		boolEval = ( IBooleanEval )EBIWrapperUtils.addExternalService( this,
			topIface, IBooleanEval.class );


		selector = new SelectorImpl( xarch, boolEval );
		// Adds the message listener for broadcasting the progress messages
		selector.addMessageListener(
			new MessageListener( )
			{
				public void messageSent(Message m)
				{
					sendToAll(m, bottomIface);
				}
			}
		);

		/*  OLD EBI stuff
		EBIWrapperUtils.deployService( this, bottomIface, bottomIface, 
			selector, new Class[]{ ISelector.class }, 
			new Class[0] );
		*/
		
		// Adds the message handler for this component
		addMessageProcessor( new SelectorMessageProcessor( ) );
	}
}

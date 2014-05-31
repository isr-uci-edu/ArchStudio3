package archstudio.comp.pruneversions;

// The c2.fw framework
import c2.fw.*;

// Support for "legacy" C2 components
import c2.legacy.*;

// Includes classes that allow our component to
// make Event-based Procedure Calls (EPCs)
import c2.pcwrap.*;

// Imported to support xArchADT use
import edu.uci.ics.xarchutils.*;

/**
 * This is the EBI wrapper for the PruneVersions component.  This basically uses the EBI
 * (Event-Based Interface) wrapper mechanism to hide event-based procedure calls.
 * 
 * @author Christopher Van der Westhuizen <A HREF="mailto:vanderwe@uci.edu">(vanderwe@uci.edu)</A>
 */
public class PruneVersionsC2Component extends AbstractC2DelegateBrick
{
	// Constants from the boiler plate code
	public static final String PRODUCT_NAME = "PruneVersions Component";
	public static final String PRODUCT_VERSION = "1.0";
	
	// This XArchFlatInterface is an EPC interface implemented
	// in another component, in this case xArchADT.  Because
	// this component uses c2.fw's EBI wrapper mechanism,
	// we can call functions in this interface directly and
	// all the communication gets translated from procedure
	// calls (PC) to EPC.
	protected XArchFlatInterface xarch;
	
	protected PruneVersionsImpl impl;
	
	/**
	 * This is the constructor for PruneVersionsC2Component
	 *
	 * @param id The unique Identifier associated with this component.
	 */
	public PruneVersionsC2Component(Identifier id)
	{
		super(id);
		
		// Get xArchADT
		xarch = (XArchFlatInterface)EBIWrapperUtils.addExternalService(this,
			topIface, XArchFlatInterface.class);
		impl = new PruneVersionsImpl(xarch);
		
		// Add the message listener for broadcasting the progress messages
		impl.addMessageListener(
			new MessageListener()
			{
				public void messageSent(Message m)
				{
					sendToAll(m, bottomIface);
				}
			}
			);
		
		// Add message handler
		addMessageProcessor(new PruneVersionsMessageProcessor());
	}
	
	private class PruneVersionsMessageProcessor implements MessageProcessor
	{
		public void handle(Message m)
		{
			if(m instanceof PerformPruneVersionsMessage)
			{
				PerformPruneVersionsMessage msg = (PerformPruneVersionsMessage)m;
				String archURL = msg.getArchURL();
				String targetURL = msg.getTargetArchURL();
				String msgID = msg.getComponentID();
				
				try
				{
					impl.pruneVersions(archURL, targetURL, msgID);
					PruneVersionsStatusMessage statusMsg = new PruneVersionsStatusMessage(targetURL, msgID, 1, 1, true, false, null);
					sendToAll(statusMsg, bottomIface);
				}
				catch(Exception e)
				{
					PruneVersionsStatusMessage statusMsg = new PruneVersionsStatusMessage(targetURL, msgID, 0, 0, true, true, e);
					sendToAll(statusMsg, bottomIface);
				}
			}
		}
	}
}
package archstudio.comp.archpruner;


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
 * This is the EBI wrapper for the ArchPruner component.  This is basically using Eric's
 * magic to hide event based procedure calls.
 * 
 * @author Christopher Van der Westhuizen <A HREF="mailto:vanderwe@uci.edu">(vanderwe@uci.edu)</A>
 */
public class ArchPrunerC2Component extends AbstractC2DelegateBrick
{
	// Constants from the boiler plate code
	public static final String PRODUCT_NAME = "ArchPruner Component";
	public static final String PRODUCT_VERSION = "1.2";
	
	// This XArchFlatInterface is an EPC interface implemented
	// in another component, in this case xArchADT.  Because
	// this component uses c2.fw's EBI wrapper mechanism,
	// we can call functions in this interface directly and
	// all the communication gets translated from procedure
	// calls (PC) to EPC.
	protected XArchFlatInterface xarch;
	protected ArchPrunerImpl impl;
	
	public ArchPrunerC2Component(Identifier id)
	{
		super(id);
		
		// Get xArchADT
		xarch = (XArchFlatInterface)EBIWrapperUtils.addExternalService(this,
			topIface, XArchFlatInterface.class);
		impl = new ArchPrunerImpl(xarch);
		
		// Adds the message listener for broadcasting the progress messages
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
		addMessageProcessor(new ArchPrunerMessageProcessor());
	}
	
	class ArchPrunerMessageProcessor implements MessageProcessor
	{
		public void handle(Message m)
		{
			if(m instanceof PerformArchPrunerMessage)
			{
				PerformArchPrunerMessage papm = (PerformArchPrunerMessage)m;
				String archURL = papm.getArchURL();
				String targetArchURL = papm.getTargetArchURL();
				String startingID = papm.getStartingID();
				boolean isStructural = papm.getIsStructural();
				String msgID = papm.getComponentID();
				
				try
				{
					impl.prune(archURL, targetArchURL, startingID, isStructural, msgID);
					ArchPrunerStatusMessage apsm = new ArchPrunerStatusMessage(targetArchURL, msgID, 1, 1, true, false, null);
					sendToAll(apsm, bottomIface);
				}
				catch(Exception e)
				{
					ArchPrunerStatusMessage apsm = new ArchPrunerStatusMessage(targetArchURL, msgID, 0, 0, true, true, e);
					sendToAll(apsm, bottomIface);
				}
			}
		}
	}
}
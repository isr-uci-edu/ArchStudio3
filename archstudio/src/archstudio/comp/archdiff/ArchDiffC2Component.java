// Ping Chen
// ArchDiffC2Component.java

package archstudio.comp.archdiff;

/********************** ERIC'S MAGIC AND MORE MAGIC *****************************/
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
 * This is the EBI wrapper for the ArchDiff component.  This is basically using Eric's
 * magic to hide event based procedure calls.
 * 
 * @author Ping Hsin Chen <A HREF="mailto:pingc@hotmail.com">(pingc@hotmail.com)</A>
 */

public class ArchDiffC2Component extends AbstractC2DelegateBrick
{
	// constants from the boiler plate code
	public static final String PRODUCT_NAME = "ArchDiff Component";
	public static final String PRODUCT_VERSION = "1.0";
	
	//This XArchFlatInterface is an EPC interface implemented
	//in another component, in this case xArchADT.  Because
	// this component uses c2.fw's EBI wrapper mechanism,
	//we can call functions in this interface directly and
	//all the communication gets translated from procedure
	//calls (PC) to EPC.
	protected XArchFlatInterface xarch;
	protected ArchDiffImpl impl;
	
	public ArchDiffC2Component( Identifier id )
	{
		super(id);
		
		// gets xArch adt
		xarch = ( XArchFlatInterface )EBIWrapperUtils.addExternalService( this,
			topIface, XArchFlatInterface.class );
		impl = new ArchDiffImpl(xarch);
		
		addMessageProcessor(new ArchDiffMessageProcessor());
		
		
		// tells the world that this component is now providing its service (diff)
		//EBIWrapperUtils.deployService( this, bottomIface, bottomIface, 
		//	new ArchDiffImpl( xarch ), new Class[]{ IArchDiff.class }, 
		//	new Class[0] );
	}
	
	class ArchDiffMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof PerformArchDiffMessage){
				PerformArchDiffMessage padm = (PerformArchDiffMessage)m;
				String oldArchURI = padm.getOrigArchURI();
				String newArchURI = padm.getNewArchURI();
				String diffURI = padm.getDiffArchURI();
				
				try{
					impl.diff(oldArchURI, newArchURI, diffURI);
					ArchDiffStatusMessage adsm = new ArchDiffStatusMessage(diffURI, false, null);
					sendToAll(adsm, bottomIface);
				}
				catch(Exception e){
					ArchDiffStatusMessage adsm = new ArchDiffStatusMessage(diffURI, true, e);
					sendToAll(adsm, bottomIface);
				}
			}
		}
	}
	
}

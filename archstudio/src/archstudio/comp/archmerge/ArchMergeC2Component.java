// Ping Chen
// ArchMergeC2Component.java

package archstudio.comp.archmerge;

/********************** ERIC'S MAGIC AND MORE MAGIC *****************************/
//The c2.fw framework
import c2.fw.*;

//Support for "legacy" C2 components
import c2.legacy.*;

//Includes classes that allow our component to
//make Event-based Procedure Calls (EPCs)
import c2.pcwrap.*;

//Imported to support transactions
import archstudio.comp.xarchtrans.*;


/**
 * This is the EBI wrapper for the ArchMerge component.  This is basically using Eric's
 * magic to hide event based procedure calls.
 * 
 * @author Ping Hsin Chen <A HREF="mailto:pingc@hotmail.com">(pingc@hotmail.com)</A>
 */

public class ArchMergeC2Component extends AbstractC2DelegateBrick
{
	// constants from the boiler plate code
	public static final String PRODUCT_NAME = "ArchMerge Component";
	public static final String PRODUCT_VERSION = "1.0";
	
	//This XArchFlatTransactionInterface is just a local proxy
	// it not only supports all functions of the xarch flat interface, but also
	// supports transactions
	protected XArchFlatTransactionsInterface xarchTrans;
	protected ArchMergeImpl impl;
	
	public ArchMergeC2Component( Identifier id )
	{
		super(id);
		
		// gets xArch adt
		xarchTrans = ( XArchFlatTransactionsInterface )EBIWrapperUtils.addExternalService( this,
			topIface, XArchFlatTransactionsInterface.class );
		
		// tells the world that this component is now providing its service (merge)
		impl = new ArchMergeImpl(xarchTrans);
		addMessageProcessor(new ArchMergeMessageProcessor());
		
		//EBIWrapperUtils.deployService( this, bottomIface, bottomIface, 
		//	impl, new Class[]{ IArchMerge.class }, 
		//	new Class[0] );
	}
	
	class ArchMergeMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof PerformArchMergeMessage){
				PerformArchMergeMessage pamm = (PerformArchMergeMessage)m;
				String archURI = pamm.getArchitectureURI();
				String diffURI = pamm.getDiffURI();
				
				try{
					impl.merge(diffURI, archURI);
					ArchMergeStatusMessage amsm = new ArchMergeStatusMessage(archURI, diffURI, false, null);
					sendToAll(amsm, bottomIface);
				}
				catch(Exception e){
					ArchMergeStatusMessage amsm = new ArchMergeStatusMessage(archURI, diffURI, true, e);
					sendToAll(amsm, bottomIface);
				}
			}
		}
	}
}
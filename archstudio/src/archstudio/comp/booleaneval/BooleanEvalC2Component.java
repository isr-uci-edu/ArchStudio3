// Ping Chen
// BooleanEvalC2Component.java

package archstudio.comp.booleaneval;

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
 * This is the EBI wrapper for the BooleanEval component.  This is basically using Eric's
 * magic to hide event based procedure calls.
 * 
 * @author Ping Hsin Chen <A HREF="mailto:pingc@hotmail.com">(pingc@hotmail.com)</A>
 */

public class BooleanEvalC2Component extends AbstractC2DelegateBrick
{
	// constants from the boiler plate code
	public static final String PRODUCT_NAME = "Boolean Evaluator Component";
	
	//This XArchFlatInterface is an EPC interface implemented
	//in another component, in this case xArchADT.  Because
	// this component uses c2.fw's EBI wrapper mechanism,
	//we can call functions in this interface directly and
	//all the communication gets translated from procedure
	//calls (PC) to EPC.
	protected XArchFlatInterface xarch;
	
	public BooleanEvalC2Component( Identifier id )
	{
		super(id);
		
		// gets xArch adt
		xarch = ( XArchFlatInterface )EBIWrapperUtils.addExternalService( this,
			topIface, XArchFlatInterface.class );
		// tells the world that this component is now providing its service (boolean evaluation)
		EBIWrapperUtils.deployService( this, bottomIface, bottomIface, 
			new BooleanEvalImpl( xarch ), new Class[]{ IBooleanEval.class }, 
			new Class[0] );
	}
}
package archstudio.comp.booleannotation;

//The c2.fw framework
import c2.fw.*;

//Support for "legacy" C2 components
import c2.legacy.*;

//Includes classes that allow our component to
//make Event-based Procedure Calls (EPCs)
import c2.pcwrap.*;

//Imported to support xArchADT use
import edu.uci.ics.xarchutils.*;

public class BooleanNotationC2Component extends AbstractC2DelegateBrick
{
	// constants from the boiler plate code
	public static final String PRODUCT_NAME = "Boolean Notation Component";
	public static final String PRODUCT_VERSION = "1.0";
	
	protected XArchFlatInterface xarch;
	
	public BooleanNotationC2Component(Identifier id){
		super(id);
		
		// gets xArch adt
		xarch = ( XArchFlatInterface )EBIWrapperUtils.addExternalService( this,
			topIface, XArchFlatInterface.class );
		// tells the world that this component is now providing its service (selection)
		EBIWrapperUtils.deployService( this, bottomIface, bottomIface, 
			new BooleanNotationImpl(xarch), new Class[]{IBooleanNotation.class}, 
			new Class[0] );
	}
	
	public static class BooleanNotationImpl implements IBooleanNotation{
		public XArchFlatInterface xarch;
		
		public BooleanNotationImpl(XArchFlatInterface xarch){
			this.xarch = xarch;
		}
		
		public String booleanGuardToString(ObjRef optionalRef) {
			return BooleanGuardConverter.booleanGuardToString(xarch, optionalRef);
		}
		
		public ObjRef parseBooleanGuard(String expression, ObjRef xArchRef)
		throws ParseException, TokenMgrError{
			return BooleanGuardConverter.parseBooleanGuard(xarch, expression, xArchRef);
		}
	}
}
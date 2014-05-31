// Ping Chen
// SelectorImpl.java

package archstudio.comp.selector;

import edu.uci.ics.xarchutils.*;  // this is to use the xArchADT
import archstudio.comp.booleaneval.*;

// For firing progress bar messages
import c2.fw.*;

import java.util.Hashtable;
import java.util.Vector;

/**
 * This is the implementation of the Selector.  The selector is reponsible for 
 * selecting what variant and optional elements should be instantiated based 
 * on the variables and their states that are passed in.  It will create a new xADL 
 * document with the appropriate components instantiated and the appropriate types.
 *
 * The algorithm is hierarchical, it can either start at an arch structure, or a type-version 
 * and evaluating all the elements that is within the starting point.
 * @author Ping Hsin Chen <A HREF="mailto:pingc@hotmail.com">(pingc@hotmail.com)</A>
 */
 
public class SelectorImpl
    implements ISelector, MessageProvider
{
	// This is just a simple struct to contain the most common set of
	// arguments that is specific to the architecture we are currently selecting.
	// This is to allow for reentrant code.
    protected class SelectorArguments
	{
		public String targetURL;		// This is the URL of the new architecture
		public ObjRef arch;   			// this is the reference to the actual architecture
		public ObjRef[] structures;     // this contains all the references to arch structures within 
										// this architecture
		public SymbolTable symTab;		// This is the symbol table that is passed in
		public Hashtable visitedTypes;  // This is a hashtable to keep track of what types
										// we have already visited (selected on), it is based
										// on the IDs of the type.
		public double lowerBound;		// This is the "current" progress
		public double upperBound;		// This is the upper bound for the progress bar. 
		public String msgID;		    // This is going to be the ID used for all the outgoing
										// messages.  This allows the requesting component to
										// filter out events that it doesn't want.
		
		public ElementCollector elementsToRemove;  // This stores all the elements that needs
												   // to be removed.  This collects them all so they
												   // can be removed with fewer calls to xarch ADT
	}
	// default size for the hashtable
	protected static final int NUM_OF_TYPES = 100;
	// Default max bound for the progress bar
	protected static final int DEFAULT_UPPER_BOUND = 1000;
	
    protected XArchFlatInterface xArch; 		  // this is the local reference to the xArchADT
	protected IBooleanEval boolEval;  		      // this is the references to the boolean evaluator
    
	// This contains a list of all the listeners we are sending messages to.
	protected Vector messageListeners = new Vector( 3 );
	
    public SelectorImpl( XArchFlatInterface xArchInst, IBooleanEval evaluator )
    {
        xArch = xArchInst;
		boolEval = evaluator;
    }
    
	/**
	 * This is the function that will call the selection algorithm.  It will try to 
	 * select the appropriate variant and optional element based on the symbol table
	 * passed in.  If part of a guard can not be resolved, it will leave the partial 
	 * guard in place (the algorithm will resolve the parts of the guard whose conditions
	 * are met).  All elements are cloned before selection.
	 * The after fully resolving a variant type, all elements of that variant type will 
	 * be modified to be of the new resolved type.
	 * Note: Only 1 variant can be true at any 1 time.
	 * 
	 * @param archURL This is the url of the xADL architecture to be selected.
	 * @param newArchURL This is the url of the new xADL architecture that will be created based on selection algorithm.
	 * @param symbolTable This is the table that contains all the variables and their values.
	 * @param startingID This is the ID of element the selection process should start from
	 * @param isStructural True if the ID passed in is a archStruct, False if the ID
	 * 		corresponds to a type & version
	 * @param msgID This is the ID of the component requesting the selection service. 
	 * 		This is used so that the status messages will carry this ID thus allowing the
	 *		requesting component to only process status messages that he cares about.
	 * 		This field may be null.
	 *
	 * @exception InvalidURLException This exception is thrown when the string passed in
	 *      for the architecture is not an already openned architecture.
	 * @exception MissingElementException This exception is thrown when the selector
	 *      cannot find a required element in the architecture description.
	 * @exception NoSuchTypeException This exception is thrown when it encounters
	 *      an unknown/invalid type when evaluating.
	 * @exception TypeMismatchException This exception is thrown when the type
	 *      of the operands do not match during an evaluation.
	 * @exception VariantEvaluationException This exception is thrown when a variant type 
	 * 		has an improper (too many or no) variants that evaluated to true.
	 * @exception BrokenLinkException This exception is thrown when a href resolves to null
	 */
	public void select( String archURL, String newArchURL, SymbolTable table,
		String startingID, boolean isStructural, String msgID )
		throws InvalidURLException, MissingElementException,
			NoSuchTypeException, TypeMismatchException, VariantEvaluationException, 
			BrokenLinkException
    {
        ObjRef sourceArch = xArch.getOpenXArch( archURL );
        if( sourceArch == null )
        {
            throw new InvalidURLException( "Error: " + archURL 
                + " is not an open xArch document." );
        }
        // creates a duplicate of the openned architecture
        ObjRef targetArch = xArch.cloneXArch( sourceArch, newArchURL );
		
		ObjRef typesContext = xArch.createContext( targetArch, "types" );
		
		// Package all the arguments we must pass around
		SelectorArguments arguments = new SelectorArguments();
		
		arguments.targetURL = newArchURL;
		arguments.arch = targetArch;
		// we need to get a listing of all the arch structures so that it will
		// be possible to update all the type links (if a variant type was resolved)
		arguments.structures = xArch.getAllElements( typesContext, "ArchStructure", targetArch );
		arguments.symTab = table;
		arguments.visitedTypes = new Hashtable( NUM_OF_TYPES );
		
		// Progress stuff
		arguments.msgID = msgID;
		arguments.lowerBound = 0;
		arguments.upperBound = DEFAULT_UPPER_BOUND;
		
		arguments.elementsToRemove = new ElementCollector( xArch );
		
        archSelector( arguments, isStructural, startingID );
	}
       

		
    // This is the function that is responsible for selecting the document by starting
	// at the correct type-version or arch structure based on the isStructural flag and
	// the ID.
    protected void archSelector( SelectorArguments arguments, boolean isStructural,
		String startingID )
        throws MissingElementException, NoSuchTypeException, TypeMismatchException,
		VariantEvaluationException, BrokenLinkException
    {
		// fire initial message...
		fireMessageSent( newStatusMessage( arguments ) );
		
		ObjRef startingPoint = xArch.getByID( arguments.arch, startingID );
		
		if( startingPoint != null )
		{
			if( isStructural )
			{
				structureSelector( startingPoint, arguments, arguments.upperBound );
			}
			else
			{
				// Since we are selecting on the type for a arch structure, we don't really
				// need to passing in anything for the structural type string 
				// (since it won't be a variant type thus it won't be used)
				typeSelector( startingPoint, "", arguments, arguments.upperBound);
			}
			
			// after selection, we remove all the elements
			arguments.elementsToRemove.removeAll( );
			
			// fire 100% progress message...
			arguments.lowerBound = arguments.upperBound;
			fireMessageSent( newStatusMessage( arguments ) );
		}
		else
		{
			throw new MissingElementException( "Error: Starting point with element ID: " + 
				startingID + " not found." );
		}
    }

	// This is a basic factory function for simplifying the creation of
	// progress messages.
	protected ArchSelectorStatusMessage newStatusMessage( SelectorArguments arguments )
	{
		/*
		System.out.println( "Selecting: " + ( arguments.lowerBound / 
			arguments.upperBound * 100 ) + "% - " + arguments.lowerBound );
		*/
		int currentProgress = ( int )arguments.lowerBound;
		int max = ( int )arguments.upperBound;
		// just to make sure we don't have over 100% due to rounding
		if( currentProgress > max )
		{
			currentProgress = max;
		}
		
		return new ArchSelectorStatusMessage(arguments.targetURL, arguments.msgID,
			currentProgress, max, false, false, null );
	}
	
    /************************************************************************
	THE FOLLOWING SECTION IS FOR STRUCTURAL SELECTION
	 *************************************************************************/
    
    // this function is just a control point where it start the work to perform selection 
	// on an archStructure element.  This includes components, connectors and links.
	// 
	// structure is the ObjRef to the archStructure we are selecting on
	// progressIncrement is the amount of "progress" allocated to this
	// 		archStructure.  
    protected void structureSelector( ObjRef structure, SelectorArguments arguments, 
		double progressIncrement )
        throws MissingElementException, NoSuchTypeException, TypeMismatchException, 
		BrokenLinkException, VariantEvaluationException
    {        
        // get all the components     
        ObjRef [] componentRefs = xArch.getAll( structure, "Component" );
		// get all the connectors
        ObjRef[] connectorRefs = xArch.getAll( structure, "Connector" );
		// get all the links
        ObjRef[] linkRefs = xArch.getAll( structure, "Link" );

		// calculate the progress increment that is allocated to this archStructure
		// right now we only consider components and connectors.
		double increment = 0;
		if( componentRefs != null )
		{
			increment += componentRefs.length;
		}
		if( connectorRefs != null )
		{
			increment += connectorRefs.length;
		}
		// find out how much to increment the progress by for each component and
		// connector within this archstructure
		increment = progressIncrement / increment;
		
        if( componentRefs != null )
        {
            // calls the selector on the components
            optionalElementSelector( componentRefs, 
                "edu.uci.isr.xarch.options.IOptionalComponent", "Component", true,
				true, arguments, true, increment );
        }        
        if( connectorRefs != null )
        {
            optionalElementSelector( connectorRefs,
                "edu.uci.isr.xarch.options.IOptionalConnector", "Connector", true,
				true, arguments, true, increment );
        }
        if( linkRefs != null )
        {
            optionalElementSelector( linkRefs,
                "edu.uci.isr.xarch.options.IOptionalLink", "Link", false, 
				false, arguments, false, 0 );
        }
    }
    
    // this is the actual function that will check the all the elements in the array
    // to see if it is a particular optional element specified by "type" parameter.  
	// It will then attempt to evaluate the bool guards.
    // It is also responsbile for removing any elements that should not be
    // instantiated as well as the "optional" tag of those that should be instantiated.
	// This function also checks to see if it needs to select on optional interfaces or
	// the element has a type that needs to be selected.
	//
	// elementRefs[] - array of elements to check
	// className - the full path of the class ie. "edu.uci.isr.xarch.options.IOptionalComponent
    // object - type of the element that we are selecting on (e.g. "Component" )
	// hasInterfaces - if true, we check for optional interfaces
	// hasType - if this is true, we then perform selection on the type of this element
	// arugments - contains all the other essential parameters that must be passed around (
	//			architecture, structures, symbol table...)
	// countProgress True if the progress should be incremented and progress message should be
	// 			sent to reflect the new progress.  False otherwise.
	// progressIncrement is the amount to increment the current progress by for each element
	// 			inside elementRefs
    protected void optionalElementSelector( ObjRef[] elementRefs,
        String className, String object,  boolean hasInterfaces, 
		boolean hasType, SelectorArguments arguments, 
		boolean countProgress, double progressIncrement )
        throws MissingElementException, NoSuchTypeException, TypeMismatchException,
		BrokenLinkException, VariantEvaluationException
    {
		boolean wasRemoved;
		boolean hasSubArchitecture;
		
        for( int i = 0; i < elementRefs.length; i++ )
        {
			ObjRef element = elementRefs[i];
            wasRemoved = false;
			hasSubArchitecture = false;
			// check to see if the element is of type object
            if( xArch.isInstanceOf( element, 
                className ) )
            {
				// we now evaluate the optional guard
               	wasRemoved = selectOptional( element, object, arguments );
            }
			
			// Stop if this element was removed
			if( !wasRemoved )
			{
				// we want to extract the interface and check to see if its optional
				// and also if the element was already removed.  If it was removed,
				// we don't need to worry about it.
				if( hasInterfaces )
				{
					ObjRef[] interfaces = xArch.getAll( element, "Interface" );
					// makes sure it has interfaces
					if( interfaces != null )
					{
						for( int j = 0; j < interfaces.length; j++ )
						{
							ObjRef interf = interfaces[j];
							// if its an optional interface
							if( xArch.isInstanceOf( interf, 
								"edu.uci.isr.xarch.options.IOptionalInterface" ) )
							{
								selectOptional( interf, "Interface", arguments );
							}
						}
					}
				}
				// now perform selection based on its type if necessary
				if( hasType )
				{
					ObjRef link = ( ObjRef )xArch.get( element, "Type" );
					if( link != null )
					{
						// now we select based on its type
						ObjRef type = resolveLink( link, arguments.arch );

						String id = ( String )xArch.get( type, "Id" );
						// check to see if the type has been visited before
						if( arguments.visitedTypes.get( id ) == null )
						{
													
							// the type has not been visited, so we visit it
							hasSubArchitecture = typeSelector( type, object, arguments, progressIncrement );		
						}
					}
				}
			}
			
			// before going on to the next element, fire off a progress message if necessary
			// we don't want to over count it if it has subarchitecture (that portion has been
			// allocated to the subarchitecture itself)
			if( countProgress && !hasSubArchitecture )
			{
				arguments.lowerBound += progressIncrement;
				
				fireMessageSent( newStatusMessage( arguments ) );
			}
        }
    }
	
	// This function will handle the evaluation of optional elements.
	// It will remove the guard belonging to any optional element that needs to be 
	// instantiated.  And remove the element itself if the guard evaluated to false
	// element - optional element being selected
	// object - the type of the element (e.g. Component)
	// arguments - The argument struct that contains all the common arguments
	// returns true if the element was removed!!!!
	protected boolean selectOptional( ObjRef element,
		String object, SelectorArguments arguments )
		throws MissingElementException, NoSuchTypeException, TypeMismatchException
	{
		ObjRef optional = ( ObjRef ) xArch.get( element, "Optional" );
		// checks to make sure the Optional component exists
		if( optional != null )
		{
			ObjRef guard = ( ObjRef ) xArch.get( optional, "Guard" );
			// checks to see if the guard exists, and if it is a boolean guard
			if( guard == null )
			{
				throw new MissingElementException( "Error: " + object + ' ' + ( String ) 
					xArch.get( element, "Id" ) + " is missing a guard." );
			}
			if( xArch.isInstanceOf( guard, 
				"edu.uci.isr.xarch.boolguard.IBooleanGuard" ) )
			{
				ObjRef exp = ( ObjRef ) xArch.get( guard, "BooleanExp" );
				if( exp == null )
				{
					throw new MissingElementException( "Error: " + object + ' ' + ( String ) 
						xArch.get( element, "Id" ) + " has a boolean guard " +
						"without a boolean expression" );
				}
				ObjRef result = boolEval.eval( exp, arguments.symTab );
				// check to see if its a simple bool value 
				ObjRef bool = ( ObjRef )xArch.get( result, "Bool" );
				if( bool != null )
				{
					// the expression evaluated to true
					if( boolEval.boolValue( bool ) )
					{
						xArch.clear( element, "Optional" );
					}
					// false
					else
					{
						//  we want to remove the element all together
						ObjRef parent = ( ObjRef ) xArch.getParent( element );
						arguments.elementsToRemove.addElement( parent, object, element );
						//xArch.remove( parent, object, element );

						return true;
					}
					
				}
				// the result is a partial evaluation and the expression
				// is already pruned.
			}
		}
		return false;
	}
    /************************************************************************
	END OF STRUCTURAL SELECTION
	 *************************************************************************/
    
	/*****************************************************************************
	THE FOLLOWING SECTION IS FOR TYPES
	 ******************************************************************************/
	// this is the function that will handle the selection for a type element
	// it can take either connector or component types.
	// it will evaluate the variants of the type
	// if more than one variant evaluates to true, an exception is thrown
	// if no variant evaluates to true, an exception is thrown.
	// if there is atleast 1 unknown, it will try to evaluate and remove as many 
	// 	variants as it can.  But it will not fully resolve the types (even if some 
	// 	variant was true.
	// if only 1 variant was ture and all else false, it will search through
	// 	all components/connectors that has this variant type and update their types to 
	//	the new type.
	// This function also evaluates sub architectures if they exist
	// 
	// type is the type element we are selecting on
	// structuralType is the this type's arch structure element type
	//	(connector/component)
	// progressIncrement - This is mainly used to pass into evaluating sub-architectures
	// 		this represents the amount of progress allocated to this particular element
	//
	// returns whether or not this type has a subarchitecture
	protected boolean typeSelector( ObjRef type, String structuralType,
		SelectorArguments arguments, double progressIncrement )
		throws VariantEvaluationException, TypeMismatchException,
		NoSuchTypeException, MissingElementException, BrokenLinkException
	{
		boolean hasTrue = false; 	// flags set for when encountering a true in variant
		boolean hasUnknown = false; // flags for when encountered an partial evaluation
		boolean hasSubArchitecture = false;
		// add the type to the visited list
		String id = ( String )xArch.get( type, "Id" );
		arguments.visitedTypes.put( id, id );
		
		// Check to see if this type has a sub-architecture
		ObjRef subArch = ( ObjRef ) xArch.get( type, "SubArchitecture" );
		if( subArch != null )
		{
			ObjRef link = ( ObjRef )xArch.get( subArch, "ArchStructure" );
			
			if( link != null )
			{
				ObjRef archStruct = resolveLink( link, arguments.arch );
	
				// we make a note that this type has a subarchitecture so we don't
				// over-count the progress
				hasSubArchitecture = true;
				// Perform selection on the sub architecture's archStructure
				structureSelector( archStruct, arguments, progressIncrement );
				
				// evluation of optional signatures mappings
				ObjRef[] sigMappings = xArch.getAll( subArch, "SignatureInterfaceMapping" );
				if( sigMappings != null )
				{
					optionalElementSelector( sigMappings, 
						"edu.uci.isr.xarch.options.IOptionalSignatureInterfaceMapping",
						"SignatureInterfaceMapping", false, false, arguments, false, 0  );
				}
			}
			else
			{
				throw new MissingElementException( "Error: SubArchitecture " + ( String )
					xArch.get( subArch, "Id" ) + " is missing its ArchStructure link." );
			}
		}
		// check to see if its a variant type component or connector
		else if( isVariantType( type ) )
		{	
			ObjRef[] variants = xArch.getAll( type, "Variant" );
			if( variants != null && variants.length != 0 )
			{
				Vector variantsToRemove = new Vector( 5 );
				
				for( int j = 0; j < variants.length; j++ )
				{
					ObjRef guard = ( ObjRef )xArch.get( variants[j], "Guard" );
					// makes sure it has a guard
					// if it doesn't, its an error.
					if( guard == null )
					{
						throw new MissingElementException( "Error: Variant Type " +
							xArch.get( type, "Id" ) + " is missing a guard" +
							" on the " + j + "th variant." );
					}
					// makes sure that the guard is bool guard
					if( xArch.isInstanceOf( guard, 
                    	"edu.uci.isr.xarch.boolguard.IBooleanGuard" ) )
					{
						ObjRef boolExp = ( ObjRef )xArch.get( guard, "BooleanExp" );
						if( boolExp != null )
						{
							ObjRef result = boolEval.eval( boolExp, arguments.symTab );
							// check to see if its a simple bool value 
							ObjRef bool = ( ObjRef )xArch.get( result, "Bool" );
							if( bool != null )
							{
								// the expression evaluated to true
								if( boolEval.boolValue( bool ) )
								{
									// already encountered a true
									if( hasTrue )
									{
										throw new VariantEvaluationException( "Error: Variant Type " +
											xArch.get( type, "Id" ) + 
											" has multiple variants that evaluated to true." );
									}
									// first time
									else
									{
										hasTrue = true;
									}
								}
								// false
								else
								{
									//  we want to remove the variant all together
									variantsToRemove.add( variants[j] );
									// xArch.remove( type, "Variant", variants[j] );                            
								}
							}
							// we encountered an unknown, partial eval
							else
							{
								hasUnknown = true;
							}
						}
						// missing the expression.
						else
						{
							throw new MissingElementException( "Error: Variant Type " +
								xArch.get( type, "Id" ) + 
								" is missing boolean expression" +
								" on the " + j + "th variant." );
						}
					}
					// its not a boolean guard, so we can't evaluate it,
					// treat it as an unknown case
					else
					{
						hasUnknown = true;
					}
				}	
				
				// Finished evaluating all the variants, so now remove them all together
				// provides slight efficiency gain
				ObjRef[] thingsToRemove = new ObjRef[variantsToRemove.size( )];
				variantsToRemove.toArray( thingsToRemove );
				
				xArch.remove( type, "Variant", thingsToRemove );
				
				// no variants evaluated to true
				// AND there were no unknowns
				if( !hasTrue && !hasUnknown )
				{
					throw new VariantEvaluationException( "Error: Variant Type " +
						xArch.get( type, "Id" ) + 
						" has no variants that evaluated to true." );
				}
				// 1 variant evaluated to true and there were no unknowns
				// so we need to update the types
				else if( !hasUnknown )
				{
					ObjRef[] result = xArch.getAll( type, "Variant" );
					// final double check
					if( result != null && result.length == 1 )
					{
						// should be only 1 variant left, at index 0 now
						ObjRef link = ( ObjRef )xArch.get( result[0], "VariantType" );
						if( link == null )
						{
							throw new MissingElementException( "Error: One of Variant Type " +
								xArch.get( type, "Id" ) + 
								"'s variants is missing a VariantType." );
						}
						
						// This only happens when we begin selection on a type
						// that has variants.  So in this case, we don't know the
						// exact structural type of this "type"
						if( structuralType.equals( "" ) )
						{
							// now we have to guess if its component or connector
							updateTypes( type, link, arguments.structures, "Component", 
								arguments.arch );
							updateTypes( type, link, arguments.structures, "Connector", 
								arguments.arch );
						}
						else // the structural type was specified
						{
							updateTypes( type, link, arguments.structures, structuralType, 
								arguments.arch );
						}
						
						// now we must go and select the variant type that was selected
						ObjRef selectedType = resolveLink( link, arguments.arch );
						
						String selectedId = ( String ) xArch.get( selectedType, "Id" );
						// if the selected type has not been visited, we visit it
						if( arguments.visitedTypes.get( selectedId ) == null )
						{						
							// the structural type should not change...
							hasSubArchitecture = typeSelector( selectedType, structuralType, 
								arguments, progressIncrement );
						}
					}
					else
						throw new VariantEvaluationException( 
							"This should never happen. Please contact the developer immediately." );
				}
				// had unknowns, we've done all we could. move on to next guy
			}
		}
		
		// evluation of optional signatures
		ObjRef[] sigs = xArch.getAll( type, "Signature" );
		if( sigs != null )
		{
			optionalElementSelector( sigs, "edu.uci.isr.xarch.options.IOptionalSignature",
				"Signature", false, false, arguments, false, 0 );
		}
		
		return hasSubArchitecture;
	}
			
	// this function will go through the arch strucutres and update all the xml links
	// that pointed to a variant type
	// variant type is the variant type that was being pointed to, contains the variants
	protected void updateTypes( ObjRef variantType, ObjRef typeLink,
		ObjRef[] archStructures, String structureType, ObjRef arch )
		throws MissingElementException, BrokenLinkException
	{

		// loops through all hte arch structure elements
		for( int i = 0; i < archStructures.length; i++ )
		{
			ObjRef[] elements = xArch.getAll( archStructures[i], structureType );
			if( elements != null )
			{
				for( int j = 0; j < elements.length; j++ )
				{
					ObjRef link = ( ObjRef )xArch.get( elements[j], "Type" );
					ObjRef elementType = null;
					if( link != null )
					{
						elementType = resolveLink( link, arch );
					}
		
					// according to eric, this should work
					// checks to see if the element is of the variant type
					if( elementType != null && elementType.equals( variantType ) )
					{							
						// clears the old href and replaces with the new one 
						// that refers to the resolved type
						xArch.clear( elements[j], "Type" );
						xArch.set( elements[j], "Type", xArch.cloneElement( typeLink, 
							edu.uci.isr.xarch.IXArchElement.DEPTH_INFINITY ) );
					}
				}
			}			
		}
	}
			
	// This function checks to see if the type passed in is one fo the possible variant 
	// types (variant component and variant connector.
	protected boolean isVariantType( ObjRef type )
	{
		return xArch.isInstanceOf( type, "edu.uci.isr.xarch.variants.IVariantComponentType" ) ||
			   xArch.isInstanceOf( type, "edu.uci.isr.xarch.variants.IVariantConnectorType" );
	}

	/*****************************************************************************
	END OF SECTION FOR TYPES
	 ******************************************************************************/
	
	// This function takes an element such as a component and first extracts the
	// link tag corresponding to the type parameter.  It then resolves the link
	// and returns the corresponding ObjRef
	// 
	// link - the acutal ObjRef to the link that needs to be resolved
	// arch - the reference to the architecture that this link belongs to
	// returns the ObjRef of the resolved href
	protected ObjRef resolveLink( ObjRef link, ObjRef arch )
		throws BrokenLinkException, MissingElementException
	{
		String href = null;
		ObjRef result = null;

		href = ( String )xArch.get( link, "Href" );
		if( href != null )
		{							
			result = ( ObjRef )xArch.resolveHref( arch, href );
			
			if( result == null )
				throw new BrokenLinkException( "Error: Unable to resolve link: " +
					href );
		}
		else
		{
			throw new MissingElementException( "Error: Missing href for link " + 
				xArch.get( link, "Id" ) );
		}
		return result;
	}
	
	/*****************************************************************************
	Sending Messages
	******************************************************************************/
	protected void fireMessageSent(Message m)
	{
		int size = messageListeners.size();
		for(int i = 0; i < size; i++)
		{
			((MessageListener)messageListeners.elementAt(i)).messageSent(m);
		}
	}
	
	public void addMessageListener(MessageListener l)
	{
		messageListeners.addElement(l);
	}
	
	public void removeMessageListener(MessageListener l)
	{
		messageListeners.removeElement(l);
	}
}
package archstudio.comp.aem;

import java.util.*;

import c2.fw.*;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.*;
import archstudio.comp.xarchtrans.*;

public class ManagedSystem implements MessageProvider, ArchMessageListener{
	protected String managedSystemURI;
	protected int engineType;
	
	protected XArchFlatTransactionsInterface realxarch = null;
	protected ArchitectureController controller = null;

	//Maps things (bricks, interfaces, whatever) in the 
	//runtime world to ObjRefs in the design-time world
	protected HashMap map = new HashMap();
	
	//Maps ObjRefs in the design-time world into
	//things in the runtime world.
	protected HashMap reverseMap = new HashMap();
	
	protected ObjRef xArchRef;	//for setting the context of the whole thing and
															//for resolving xlinks
	
	protected String structureURI = null;
	
	public ManagedSystem(String managedSystemURI, XArchFlatTransactionsInterface realxarch, int engineType){
		this.managedSystemURI = managedSystemURI;
		this.realxarch = realxarch;
		this.engineType = engineType;
		
		Class[] addlClasses = new Class[]{};
		ArchitectureEngine engine = null;
		if(engineType == AEMInstantiateMessage.ENGINETYPE_ONETHREADPERBRICK){
			engine = new OneThreadPerBrickArchitectureEngine();
		}
		else if(engineType == AEMInstantiateMessage.ENGINETYPE_ONETHREADSTEPPABLE){
			engine = new OneThreadSteppableArchitectureEngine();
			c2.util.GUISteppableEngineManager sem = new c2.util.GUISteppableEngineManager(
				(SteppableArchitectureEngine)engine);
			addlClasses = new Class[]{SteppableArchitectureEngine.class};
		}
		
		controller = ArchitectureControllerFactory.createController(
			createManager(), createHandler(), engine, addlClasses);
	}
	
	protected MessageHandler createHandler() {
		return new OneQueuePerInterfaceMessageHandler();
	}

	protected LocalArchitectureManager createManager() {
		return new SimpleArchitectureManager();
	}
	
	public Identifier[] getAllBrickIdentifiers(){
		return controller.getBrickIdentifiers();
	}
	
	public ObjRef lookup(Object o){
		return (ObjRef)map.get(o);
	}
	
	public Object reverseLookup(ObjRef o){
		return reverseMap.get(o);
	}
	
	public void put(Object o, ObjRef objRef){
		map.put(o, objRef);
		reverseMap.put(objRef, o);
	}
	
	public void remove(Object o){
		ObjRef val = lookup(o);
		if(val != null){
			reverseMap.remove(val);
		}
		map.remove(o);
	}
	
	private static ObjRef createXLink(XArchFlatTransactionsInterface xarch, 
	ObjRef contextObjectRef, String href){
		ObjRef linkRef = xarch.create(contextObjectRef, "XMLLink");
		xarch.set(linkRef, "type", "simple");
		xarch.set(linkRef, "href", href);
		return linkRef;
	}
	
	private static ObjRef createXLink(XArchFlatTransactionsInterface xarch, Transaction t, 
	ObjRef contextObjectRef, String href){
		ObjRef linkRef = xarch.create(contextObjectRef, "XMLLink");
		xarch.set(t, linkRef, "type", "simple");
		xarch.set(t, linkRef, "href", href);
		return linkRef;
	}
	
	/*
	private String getDescription(ObjRef ref){
		ObjRef descriptionRef = (ObjRef)xarch.get(ref, "Description");
		if(descriptionRef == null) return null;
		String desc = (String)xarch.get(descriptionRef, "Value");
		return desc;
	}
	
	private String getDirection(ObjRef ref){
		ObjRef directionRef = (ObjRef)xarch.get(ref, "Direction");
		if(directionRef == null) return null;
		String dir = (String)xarch.get(directionRef, "Value");
		return dir;
	}
	*/

	public XArchBulkQuery getBulkQuery(ObjRef xArchRef){
		XArchBulkQuery q = new XArchBulkQuery(xArchRef);
		q.addQueryPath("archStructure*/component*/id");
		q.addQueryPath("archStructure*/component*/description/value");
		q.addQueryPath("archStructure*/component*/interface*");
		q.addQueryPath("archStructure*/component*/interface*/id");
		q.addQueryPath("archStructure*/component*/interface*/description/value");
		q.addQueryPath("archStructure*/component*/interface*/direction/value");
		q.addQueryPath("archStructure*/component*/interface*/signature/type");
		q.addQueryPath("archStructure*/component*/interface*/signature/href");
		q.addQueryPath("archStructure*/component*/interface*/type/type");
		q.addQueryPath("archStructure*/component*/interface*/type/href");
		q.addQueryPath("archStructure*/component*/type/type");
		q.addQueryPath("archStructure*/component*/type/href");

		q.addQueryPath("archStructure*/connector*/id");
		q.addQueryPath("archStructure*/connector*/description/value");
		q.addQueryPath("archStructure*/connector*/interface*");
		q.addQueryPath("archStructure*/connector*/interface*/id");
		q.addQueryPath("archStructure*/connector*/interface*/description/value");
		q.addQueryPath("archStructure*/connector*/interface*/direction/value");
		q.addQueryPath("archStructure*/connector*/interface*/signature/type");
		q.addQueryPath("archStructure*/connector*/interface*/signature/href");
		q.addQueryPath("archStructure*/connector*/interface*/type/type");
		q.addQueryPath("archStructure*/connector*/interface*/type/href");
		q.addQueryPath("archStructure*/connector*/type/type");
		q.addQueryPath("archStructure*/connector*/type/href");

		q.addQueryPath("archTypes/componentType*/id");
		q.addQueryPath("archTypes/componentType*/description/value");
		q.addQueryPath("archTypes/componentType*/implementation*/mainClass/javaClassName/value");
		q.addQueryPath("archTypes/componentType*/implementation*/mainClass/url/type");
		q.addQueryPath("archTypes/componentType*/implementation*/mainClass/url/href");
		q.addQueryPath("archTypes/componentType*/implementation*/mainClass/url/href");
		q.addQueryPath("archTypes/componentType*/implementation*/mainClass/initializationParameter*/name");
		q.addQueryPath("archTypes/componentType*/implementation*/mainClass/initializationParameter*/value");
		q.addQueryPath("archTypes/componentType*/signature*");
		q.addQueryPath("archTypes/componentType*/signature*/id");
		q.addQueryPath("archTypes/componentType*/signature*/description/value");
		q.addQueryPath("archTypes/componentType*/signature*/direction/value");
		q.addQueryPath("archTypes/componentType*/subArchitecture/archStructure/type");
		q.addQueryPath("archTypes/componentType*/subArchitecture/archStructure/href");

		q.addQueryPath("archTypes/componentType*/subArchitecture/signatureInterfaceMapping*/id");
		q.addQueryPath("archTypes/componentType*/subArchitecture/signatureInterfaceMapping*/description/value");
		q.addQueryPath("archTypes/componentType*/subArchitecture/signatureInterfaceMapping*/outerSignature/type");
		q.addQueryPath("archTypes/componentType*/subArchitecture/signatureInterfaceMapping*/outerSignature/href");
		q.addQueryPath("archTypes/componentType*/subArchitecture/signatureInterfaceMapping*/innerInterface/type");
		q.addQueryPath("archTypes/componentType*/subArchitecture/signatureInterfaceMapping*/innerInterface/href");

		q.addQueryPath("archTypes/connectorType*/id");
		q.addQueryPath("archTypes/connectorType*/description/value");
		q.addQueryPath("archTypes/connectorType*/implementation*/mainClass/javaClassName/value");
		q.addQueryPath("archTypes/connectorType*/implementation*/mainClass/url/type");
		q.addQueryPath("archTypes/connectorType*/implementation*/mainClass/url/href");
		q.addQueryPath("archTypes/connectorType*/implementation*/mainClass/url/href");
		q.addQueryPath("archTypes/connectorType*/implementation*/mainClass/initializationParameter*/name");
		q.addQueryPath("archTypes/connectorType*/implementation*/mainClass/initializationParameter*/value");
		q.addQueryPath("archTypes/connectorType*/signature*");
		q.addQueryPath("archTypes/connectorType*/signature*/id");
		q.addQueryPath("archTypes/connectorType*/signature*/description/value");
		q.addQueryPath("archTypes/connectorType*/signature*/direction/value");
		q.addQueryPath("archTypes/connectorType*/subArchitecture/archStructure/type");
		q.addQueryPath("archTypes/connectorType*/subArchitecture/archStructure/href");

		q.addQueryPath("archTypes/connectorType*/subArchitecture/signatureInterfaceMapping*/id");
		q.addQueryPath("archTypes/connectorType*/subArchitecture/signatureInterfaceMapping*/description/value");
		q.addQueryPath("archTypes/connectorType*/subArchitecture/signatureInterfaceMapping*/outerSignature/type");
		q.addQueryPath("archTypes/connectorType*/subArchitecture/signatureInterfaceMapping*/outerSignature/href");
		q.addQueryPath("archTypes/connectorType*/subArchitecture/signatureInterfaceMapping*/innerInterface/type");
		q.addQueryPath("archTypes/connectorType*/subArchitecture/signatureInterfaceMapping*/innerInterface/href");

		q.addQueryPath("archStructure*/link*/id");
		q.addQueryPath("archStructure*/link*/description/value");
		q.addQueryPath("archStructure*/link*/point*/anchorOnInterface/type");
		q.addQueryPath("archStructure*/link*/point*/anchorOnInterface/href");
		
		q.addQueryPath("archTypes/interfaceType*/id");
		q.addQueryPath("archTypes/interfaceType*/description/value");
		q.addQueryPath("archTypes/interfaceType*/implementation*/name/value");
		return q;
	}

	//This function parses the document and creates the elements necessary to instantiate
	//a system, without actually starting the system.	
	public void bind(ObjRef xArchRef) throws InvalidArchitectureDescriptionException{
		try{
			System.out.println("Binding.");
			this.xArchRef = xArchRef;
			structureURI = realxarch.getXArchURI(xArchRef);
			
			ObjRef instanceContextRef = null;
			ObjRef instanceTypesContextRef = null;
			ObjRef instanceXArchRef = null;
			ObjRef archInstanceRef = null;
			
			XArchBulkQuery q = getBulkQuery(xArchRef);
			XArchBulkQueryResults qr = realxarch.bulkQuery(q);
			XArchFlatQueryInterface xarchBulk = new XArchBulkQueryResultProxy(realxarch, qr);
			
			String structureURI = realxarch.getXArchURI(xArchRef);
			ObjRef typesContextRef = realxarch.createContext(xArchRef, "types");

			ObjRef archStructureRef = xarchBulk.getElement(typesContextRef, "ArchStructure", xArchRef);
			if(archStructureRef == null){
				throw new InvalidArchitectureDescriptionException(
					InvalidArchitectureDescriptionException.ERROR_NO_ARCHSTRUCTURE);
			}
			
			//Map the controller (the whole system) to the archStructure
			put(controller, archStructureRef);
			
			ObjRef archTypesRef = xarchBulk.getElement(typesContextRef, "ArchTypes", xArchRef);
			if(archTypesRef == null){
				throw new InvalidArchitectureDescriptionException(
					InvalidArchitectureDescriptionException.ERROR_NO_ARCHTYPES);
			}
			
			ObjRef[] componentRefs = xarchBulk.getAll(archStructureRef, "Component");
			ObjRef[] connectorRefs = xarchBulk.getAll(archStructureRef, "Connector");
			
			//System.out.println(componentRefs.length + " components.");
			//System.out.println(connectorRefs.length + " connectors.");
			
			if((componentRefs.length == 0) && (connectorRefs.length == 0)){
				throw new InvalidArchitectureDescriptionException(
					InvalidArchitectureDescriptionException.ERROR_NO_ELEMENTS);
			}
			
			ObjRef[] elementRefs = new ObjRef[componentRefs.length + connectorRefs.length];
			int p = 0;
			for(int i = 0; i < componentRefs.length; i++){
				elementRefs[p++] = componentRefs[i];
			}
			for(int i = 0; i < connectorRefs.length; i++){
				elementRefs[p++] = connectorRefs[i];
			}
			
			//SimpleIdentifier[] identifiers = new SimpleIdentifier[elementRefs.length];
			//JavaClassBrickDescription[] brickDescriptions = new JavaClassBrickDescription[elementRefs.length];
			//Collection[] initParams = new Collection[elementRefs.length];
			
			for(int i = 0; i < elementRefs.length; i++){
				bindBrick(xarchBulk, elementRefs[i]);
			}
			
			ObjRef[] linkRefs = xarchBulk.getAll(archStructureRef, "Link");
			for(int i = 0; i < linkRefs.length; i++){
				bindLink(xarchBulk, linkRefs[i]);
			}
		}
		catch(InvalidArchitectureDescriptionException iade){
			throw iade;
		}
	}
	
	private static ObjRef getJavaImplementation(XArchFlatQueryInterface xarch, ObjRef typeRef){
		ObjRef[] implementationRefs = xarch.getAll(typeRef, "implementation");
		for(int i = 0; i < implementationRefs.length; i++){
			boolean isJavaImplementation = xarch.isInstanceOf(implementationRefs[i], "edu.uci.isr.xarch.javaimplementation.IJavaImplementation");
			if(isJavaImplementation){
				return implementationRefs[i];
			}
		}
		return null;
	}

	protected Identifier bindBrick(XArchFlatQueryInterface xarchBulk, ObjRef elementRef) throws InvalidArchitectureDescriptionException{
		String id = (String)xarchBulk.get(elementRef, "id");
		if(id == null){
			id = c2.util.UIDGenerator.generateUID("Element");
		}
		Identifier brickId = new SimpleIdentifier(id);

		//Get all the interfaces...
		ObjRef[] interfaceRefs = xarchBulk.getAll(elementRef, "Interface");
		String[] interfaceIds = new String[interfaceRefs.length];
		for(int j = 0; j < interfaceRefs.length; j++){
			interfaceIds[j] = null;
			
			//First try to get it from a LookupImplementation
			try{
				ObjRef interfaceType = 	resolveTypeLink(xarchBulk, interfaceRefs[j]);
				if(interfaceType != null){
					ObjRef[] implementationRefs = xarchBulk.getAll(interfaceType, "implementation");
					for(int k = 0; k < implementationRefs.length; k++){
						if(xarchBulk.isInstanceOf(implementationRefs[k], "edu.uci.isr.xarch.lookupimplementation.ILookupImplementation")){
							ObjRef nameRef = (ObjRef)xarchBulk.get(implementationRefs[k], "name");
							if(nameRef != null){
								String name = (String)xarchBulk.get(nameRef, "value");
								interfaceIds[j] = name; 
							}
						}
					}
				}
			}
			catch(InvalidArchitectureDescriptionException iade){
				//Fall through to default case of checking ID.
			}
			
			//We didn't get it from the lookupimplementation, let's try the
			//older BrickID.InterfaceID strategy
			if(interfaceIds[j] == null){
				interfaceIds[j] = (String)xarchBulk.get(interfaceRefs[j], "Id");
				if(interfaceIds[j] == null){
					throw new InvalidArchitectureDescriptionException(
						InvalidArchitectureDescriptionException.ERROR_INTERFACE_MISSING_ID, " on brick " + id);
				}
				int dotIndex = interfaceIds[j].indexOf(".");
				if(dotIndex == -1){
					throw new InvalidArchitectureDescriptionException(
						InvalidArchitectureDescriptionException.ERROR_INVALID_INTERFACE_ID_FORMAT, " on brick " + id);
				}
				String interfaceIdBrickPart = interfaceIds[j].substring(0, dotIndex);
				String interfaceIdInterfacePart = interfaceIds[j].substring(dotIndex+1);
				if(!interfaceIdBrickPart.equals(id)){
					throw new InvalidArchitectureDescriptionException(
						InvalidArchitectureDescriptionException.ERROR_INVALID_INTERFACE_ID_FORMAT, " on brick " + id);
				}
				//Chop off the "brickID." part.
				interfaceIds[j] = interfaceIdInterfacePart;
				//Print a warning
				System.err.println("WARNING: Architecture description uses the BrickID.InterfaceID hack");
				System.err.println("for interface implementation mapping.  This strategy is deprecated");
				System.err.println("and should be replaced with a lookup implementation.");
			}
		}
		
		//resolve type link
		ObjRef elementTypeRef = resolveTypeLink(xarchBulk, elementRef);
		
		String elementTypeId = (String)xarchBulk.get(elementTypeRef, "id");
		
		if(xarchBulk.isInstanceOf(elementTypeRef, "edu.uci.isr.xarch.types.IComponentType")){
			if(!xarchBulk.isInstanceOf(elementTypeRef, "edu.uci.isr.xarch.implementation.IVariantComponentTypeImpl")){
				throw new InvalidArchitectureDescriptionException(
					InvalidArchitectureDescriptionException.ERROR_TYPE_MISSING_IMPL, elementTypeId);
			}
		}
		else if(xarchBulk.isInstanceOf(elementTypeRef, "edu.uci.isr.xarch.types.IConnectorType")){
			if(!xarchBulk.isInstanceOf(elementTypeRef, "edu.uci.isr.xarch.implementation.IVariantConnectorTypeImpl")){
				throw new InvalidArchitectureDescriptionException(
					InvalidArchitectureDescriptionException.ERROR_TYPE_MISSING_IMPL, elementTypeId);
			}
		}
		
		ObjRef implementationRef = getJavaImplementation(xarchBulk, elementTypeRef);
		//System.out.println(implementationRef);
		//System.out.println("Implemention type = " + xarch.getType(implementationRef));
		if((implementationRef == null) || (!xarchBulk.isInstanceOf(implementationRef, "edu.uci.isr.xarch.javaimplementation.IJavaImplementation"))){
			throw new InvalidArchitectureDescriptionException(
				InvalidArchitectureDescriptionException.ERROR_CANT_PROCESS_IMPL, elementTypeId);
		}
		
		ObjRef mainClassFileRef = (ObjRef)xarchBulk.get(implementationRef, "MainClass");
		if(mainClassFileRef == null){
			throw new InvalidArchitectureDescriptionException(
				InvalidArchitectureDescriptionException.ERROR_MISSING_MAIN_CLASS);
		}
		ObjRef mainClassNameRef = (ObjRef)xarchBulk.get(mainClassFileRef, "JavaClassName");
		if(mainClassNameRef == null){
			throw new InvalidArchitectureDescriptionException(
				InvalidArchitectureDescriptionException.ERROR_MISSING_MAIN_CLASS);
		}
		
		InitializationParameter[] initParams = null;
		String urlString = null;
	
		ObjRef url = (ObjRef)xarchBulk.get(mainClassFileRef, "url");
		if(url != null){
			String href = edu.uci.ics.xadlutils.XadlUtils.getHref(xarchBulk, url);
			if(href != null){
				urlString = href;
			}
		}

		if(xarchBulk.isInstanceOf(mainClassFileRef, "edu.uci.isr.xarch.javainitparams.IJavaClassFileParams")){
			//Get the parameters
			ObjRef[] ipRefs = xarchBulk.getAll(mainClassFileRef, "InitializationParameter");
			initParams = new InitializationParameter[ipRefs.length];
			for(int k = 0; k < ipRefs.length; k++){
				String paramName = (String)xarchBulk.get(ipRefs[k], "Name");
				String paramValue = (String)xarchBulk.get(ipRefs[k], "Value");
				initParams[k] = new InitializationParameter(paramName, paramValue);
			}
		}
		
		String mainClassValue = (String)xarchBulk.get(mainClassNameRef, "value");
		BrickDescription brickDescription = null;
		
		if(urlString == null){
			brickDescription = new JavaClassBrickDescription(mainClassValue);
		}
		else{
			brickDescription = new JavaClassBrickDescription(mainClassValue, new Object[]{urlString});
		}
		
		//The brick ID is in brickID
		//The brick description is in brickDescription
		//The init params are in initParams
		//The brickRef is elementRef
		//The interface refs are in interfaceRefs,
		//The interface IDs are in interfaceIds
		addBrick(brickId, brickDescription, initParams, elementRef, interfaceRefs, interfaceIds);
		return brickId;
	}
	
	private String getInterfaceName(XArchFlatQueryInterface xarchBulk, ObjRef interfaceRef) throws InvalidArchitectureDescriptionException{
		ObjRef interfaceType = 	resolveTypeLink(xarchBulk, interfaceRef);
		if(interfaceType != null){
			ObjRef[] implementationRefs = xarchBulk.getAll(interfaceType, "implementation");
			for(int k = 0; k < implementationRefs.length; k++){
				if(xarchBulk.isInstanceOf(implementationRefs[k], "edu.uci.isr.xarch.lookupimplementation.ILookupImplementation")){
					ObjRef nameRef = (ObjRef)xarchBulk.get(implementationRefs[k], "name");
					if(nameRef != null){
						String name = (String)xarchBulk.get(nameRef, "value");
						return name; 
					}
				}
			}
		}
		return null;
	}
	
	protected Weld bindLink(XArchFlatQueryInterface xarchBulk, ObjRef linkRef) throws InvalidArchitectureDescriptionException{
		ObjRef[] pointRefs = xarchBulk.getAll(linkRef, "Point");
		if(pointRefs.length != 2){
			throw new InvalidArchitectureDescriptionException(
				InvalidArchitectureDescriptionException.ERROR_NOT_TWO_LINK_POINTS);
		}
		ObjRef anchor1Ref = (ObjRef)xarchBulk.get(pointRefs[0], "AnchorOnInterface");
		ObjRef anchor2Ref = (ObjRef)xarchBulk.get(pointRefs[1], "AnchorOnInterface");
		
		ObjRef anchor1Target = resolveXLink(xarchBulk, anchor1Ref);
		ObjRef anchor2Target = resolveXLink(xarchBulk, anchor2Ref);
		
		ObjRef parent1 = xarchBulk.getParent(anchor1Target);
		ObjRef parent2 = xarchBulk.getParent(anchor2Target);
		
		String brick1Id = (String)xarchBulk.get(parent1, "Id");
		String brick2Id = (String)xarchBulk.get(parent2, "Id");

		String interface1Id = null;
		String interface2Id = null;
		
		try{
			interface1Id = getInterfaceName(xarchBulk, anchor1Target);
		}
		catch(InvalidArchitectureDescriptionException iade){
			//Fall through to default case of checking ID.
		}
		if(interface1Id == null){
			interface1Id = (String)xarchBulk.get(anchor1Target, "Id");
			int dotIndex1 = interface1Id.indexOf(".");
			if(dotIndex1 == -1){
				throw new InvalidArchitectureDescriptionException(
					InvalidArchitectureDescriptionException.ERROR_INVALID_INTERFACE_ID_FORMAT, " on brick " + brick1Id);
			}
			interface1Id = interface1Id.substring(dotIndex1 + 1);
		}
		
		try{
			interface2Id = getInterfaceName(xarchBulk, anchor2Target);
		}
		catch(InvalidArchitectureDescriptionException iade2){
			//Fall through to default case of checking ID.
		}
		if(interface2Id == null){
			interface2Id = (String)xarchBulk.get(anchor2Target, "Id");
			int dotIndex2 = interface2Id.indexOf(".");
			if(dotIndex2 == -1){
				throw new InvalidArchitectureDescriptionException(
					InvalidArchitectureDescriptionException.ERROR_INVALID_INTERFACE_ID_FORMAT, " on brick " + brick2Id);
			}
			interface2Id = interface2Id.substring(dotIndex2 + 1);
		}
		
		BrickInterfaceIdPair pair1 = 
			new BrickInterfaceIdPair(new SimpleIdentifier(brick1Id), new SimpleIdentifier(interface1Id));
		BrickInterfaceIdPair pair2 = 
			new BrickInterfaceIdPair(new SimpleIdentifier(brick2Id), new SimpleIdentifier(interface2Id));
		
		Weld w = new SimpleWeld(pair1, pair2);
		
		addWeld(w, linkRef);
		return w;
	}
	
	protected ObjRef resolveXLink(XArchFlatQueryInterface xarch, ObjRef xLinkRef) throws InvalidArchitectureDescriptionException{
		if(xLinkRef == null){
			throw new InvalidArchitectureDescriptionException(
				InvalidArchitectureDescriptionException.ERROR_ELEMENT_MISSING_TYPE);
		}
		String xlinkType = (String)xarch.get(xLinkRef, "type");
		if(xlinkType == null){
			throw new InvalidArchitectureDescriptionException(
				InvalidArchitectureDescriptionException.ERROR_INVALID_XLINK);
		}				
		if(!xlinkType.equals("simple")){
			throw new InvalidArchitectureDescriptionException(
				InvalidArchitectureDescriptionException.ERROR_CANT_PROCESS_XLINK);
		}
		String xlinkHref = (String)xarch.get(xLinkRef, "href");
		
		//System.out.println("Got type href:" + xlinkHref);
		
		if(xlinkHref == null){
			throw new InvalidArchitectureDescriptionException(
				InvalidArchitectureDescriptionException.ERROR_INVALID_XLINK);
		}
		ObjRef targetRef = xarch.resolveHref(xArchRef, xlinkHref);
		if(targetRef == null){
			throw new InvalidArchitectureDescriptionException(
				InvalidArchitectureDescriptionException.ERROR_INVALID_XLINK_TARGET);
		}
		
		return targetRef;		
	}
	
	protected ObjRef resolveTypeLink(XArchFlatQueryInterface xarch, ObjRef brickRef) throws InvalidArchitectureDescriptionException{
		ObjRef typeLinkRef = (ObjRef)xarch.get(brickRef, "type");
		return resolveXLink(xarch, typeLinkRef);
	}
	
	protected void addBrick(Identifier brickId, BrickDescription brickDescription, 
	InitializationParameter[] initParams, ObjRef brickRef, ObjRef[] interfaceRefs,
	String[] interfaceIds) throws InvalidArchitectureDescriptionException{
		try{
			if((initParams == null) || (initParams.length == 0)){
				controller.addBrick(brickDescription, brickId);
			}
			else{
				controller.addBrick(brickDescription, brickId, initParams);
			}
			LocalArchitectureManager lam = (LocalArchitectureManager)controller;
			Brick brick = lam.getBrick(brickId);
			
			//Map the brick
			put(brick, brickRef);
			
			for(int i = 0; i < interfaceRefs.length; i++){
				Interface iface = brick.getInterface(new SimpleIdentifier(interfaceIds[i]));
				if(iface == null){
					throw new InvalidArchitectureDescriptionException(
						InvalidArchitectureDescriptionException.ERROR_BRICK_IMPL_MISSING_PRESCRIBED_INTERFACE,
						" on brick " + brickId + ", interface ID should have been" + interfaceIds[i]);
				}
				put(iface, interfaceRefs[i]);
			}			
		}
		catch(BrickLoadFailureException blfe){
			throw new InvalidArchitectureDescriptionException(
				InvalidArchitectureDescriptionException.ERROR_CANT_LOAD_BRICK, blfe.toString());
		}
		catch(BrickNotFoundException bnfe){
			throw new InvalidArchitectureDescriptionException(
				InvalidArchitectureDescriptionException.ERROR_CANT_LOAD_BRICK, bnfe.toString());
		}
		catch(UnsupportedBrickDescriptionException ubde){
			throw new InvalidArchitectureDescriptionException(
				InvalidArchitectureDescriptionException.ERROR_CANT_LOAD_BRICK, ubde.toString());
		}
		catch(BrickCreationException bce){
			throw new InvalidArchitectureDescriptionException(
				InvalidArchitectureDescriptionException.ERROR_CANT_LOAD_BRICK, bce.toString());
		}
	}

	protected void addWeld(Weld w, ObjRef linkRef) throws InvalidArchitectureDescriptionException{
		controller.addWeld(w);
		put(w, linkRef);
	}

	public void startSystem() throws InvalidArchitectureDescriptionException{
		try{
			System.out.println("Starting the engine.");
			controller.startEngine();
			System.out.println("Waiting for engine to start.");
			controller.waitEngineState(ArchitectureEngine.ENGINESTATE_STARTED);
			System.out.println("Engine started.");
	
			System.out.println("Starting all bricks.");
			controller.startAll();
			System.out.println("Waiting for all bricks to start.");
			controller.waitStateAll(ArchitectureEngine.STATE_OPEN_RUNNING);
			System.out.println("Bricks started.");
		}
		catch(InterruptedException e){
		}
		
		System.out.println("Reticulating splines.");
		//new c2.util.GUISteppableEngineManager((SteppableArchitectureEngine)controller);

		System.out.println("Beginning all bricks.");
		
		controller.addArchMessageListener(this);
		Identifier[] ids = controller.getBrickIdentifiers();
		for(int i = 0; i < ids.length; i++){
			controller.begin(ids[i]);
		}
		System.out.println("Done beginning all bricks.");
		System.out.println("System started.");
	}

	public void shutdownSystem(int shutdownCode) throws InvalidArchitectureDescriptionException{
		controller.removeArchMessageListener(this);
		
		if(shutdownCode == ShutdownArchMessage.SHUTDOWN_NORMAL){
			System.out.println("Ending all bricks.");
			Identifier[] ids = controller.getBrickIdentifiers();
			for(int i = 0; i < ids.length; i++){
				controller.end(ids[i]);
			}
			System.out.println("Done ending all bricks.");
		}
		
		try{
			System.out.println("Stopping all bricks.");
			controller.stopAll();
			if(shutdownCode == ShutdownArchMessage.SHUTDOWN_NORMAL){
				System.out.println("Waiting for all bricks to stop.");
				controller.waitStateAll(ArchitectureEngine.STATE_CLOSED_COMPLETED);
				System.out.println("Bricks stopped.");
			}
			
			System.out.println("Stopping the engine.");
			controller.stopEngine();
			if(shutdownCode == ShutdownArchMessage.SHUTDOWN_NORMAL){
				System.out.println("Waiting for engine to stop.");
				controller.waitEngineState(ArchitectureEngine.ENGINESTATE_STOPPED);
				System.out.println("Engine stopped.");
			}
		}
		catch(InterruptedException e){
		}
		
		System.out.println("Finalizing; collecting garbage.");
		System.runFinalization();
		System.gc();
		
		System.out.println("System shutdown complete.");
		if(shutdownCode == ShutdownArchMessage.SHUTDOWN_NOW){
			System.out.println("Shutdown was abnormal; terminate environment ASAP.");
		}
		
	}
	
	public void createInstanceModel(XArchFlatTransactionsInterface xarch, String instanceURI){
		ObjRef instanceContextRef = null;
		ObjRef instanceTypesContextRef = null;
		ObjRef instanceXArchRef = null;
		ObjRef archInstanceRef = null;
		//Create the document and the contexts and the like
		instanceXArchRef = xarch.createXArch(instanceURI);
		
		fireMessageSent(
			new AEMProgressMessage(managedSystemURI, "CREATE_INSTANCE_MODEL",
			0, 100, "Creating Model..."));
		
		Transaction t = xarch.createTransaction(instanceXArchRef);
		
		instanceContextRef = xarch.createContext(instanceXArchRef, "instance");
		instanceTypesContextRef = xarch.createContext(instanceXArchRef, "types");
		archInstanceRef = xarch.createElement(instanceContextRef, "ArchInstance");
		xarch.add(t, instanceXArchRef, "Object", archInstanceRef);

		Brick[] bricks = ((LocalArchitectureManager)controller).getAllBricks();
		Weld[] welds = controller.getWelds();
		
		int numberOfBricksAndLinks = bricks.length + welds.length;
		
		for(int i = 0; i < bricks.length; i++){
			ObjRef elementInstanceRef = null;
			ObjRef elementStructureRef = lookup(bricks[i]);
			boolean isComponent = true;
			
			if(bricks[i] instanceof c2.fw.Component){
				isComponent = true;
			}
			else if(bricks[i] instanceof c2.fw.Connector){
				isComponent = false;
			}
			else{  //It's a brick or something.  Check the type link
				if(elementStructureRef != null){
					if(xarch.isInstanceOf(elementStructureRef, "edu.uci.isr.xarch.types.IComponent")){
						isComponent = true;
					}
					else if(xarch.isInstanceOf(elementStructureRef, "edu.uci.isr.xarch.types.IConnector")){
						isComponent = false;
					}
				}
			}

			if(isComponent){
				elementInstanceRef = xarch.create(instanceContextRef, "ComponentInstance");
				xarch.add(t, archInstanceRef, "ComponentInstance", elementInstanceRef);
			}
			else{
				//It's a connector
				elementInstanceRef = xarch.create(instanceContextRef, "ConnectorInstance");
				xarch.add(t, archInstanceRef, "ConnectorInstance", elementInstanceRef);
			}
			
			//Set the ID
			String brickId = bricks[i].getIdentifier().toString();

			fireMessageSent(
				new AEMProgressMessage(managedSystemURI, "CREATE_INSTANCE_MODEL",
				i, numberOfBricksAndLinks + 1, "Modeling brick: " + brickId));
			
			xarch.set(t, elementInstanceRef, "Id", brickId);
			
			//System.out.println("Creating instances for brick" + brickId);
	
			//Set the description
			ObjRef descriptionInstanceRef = xarch.create(instanceContextRef, "Description");
			
			String descriptionStructureVal = XadlUtils.getDescription(xarch, elementStructureRef);
			if(descriptionStructureVal == null){
				descriptionStructureVal  = brickId;
			}
			descriptionStructureVal += " Run-time Instance";
			xarch.set(t, descriptionInstanceRef, "Value", descriptionStructureVal);
			xarch.set(t, elementInstanceRef, "Description", descriptionInstanceRef);
			
			//Do the interfaces
			
			Interface[] ifaces = bricks[i].getAllInterfaces();
			for(int j = 0; j < ifaces.length; j++){
				ObjRef interfaceStructureRef = lookup(ifaces[j]);

				ObjRef interfaceInstanceRef = xarch.create(instanceContextRef, "InterfaceInstance");
				
				//Set the ID on the interface instance
				String interfaceId = brickId + "." + ifaces[j].getIdentifier().toString();
				
				//fireMessageSent(
				//	new AEMProgressMessage(managedSystemURI, "CREATE_INSTANCE_MODEL",
				//	i, numberOfBricksAndLinks, "Modeling interface: " + interfaceId));
				
				xarch.set(t, interfaceInstanceRef, "Id", interfaceId);
				
				//Set the interface's description
				String interfaceStructureDescriptionVal = null;
				if(interfaceStructureRef != null){
					interfaceStructureDescriptionVal = XadlUtils.getDescription(xarch, interfaceStructureRef);
				}
				if(interfaceStructureDescriptionVal == null){
					interfaceStructureDescriptionVal = interfaceId;
				}
				
				interfaceStructureDescriptionVal += " Run-time Instance";
				ObjRef interfaceInstanceDescriptionRef = xarch.create(instanceContextRef, "Description");
				xarch.set(t, interfaceInstanceDescriptionRef, "Value", interfaceStructureDescriptionVal);
				xarch.set(t, interfaceInstanceRef, "Description", interfaceInstanceDescriptionRef);

				//Set the interface's direction (if one is specified)
				if(interfaceStructureRef != null){
					String interfaceStructureDirectionVal = XadlUtils.getDirection(xarch, interfaceStructureRef);
					if(interfaceStructureDirectionVal != null){
						ObjRef interfaceInstanceDirectionRef = xarch.create(instanceContextRef, "Direction");
						xarch.set(t, interfaceInstanceDirectionRef, "Value", interfaceStructureDirectionVal);
						xarch.set(t, interfaceInstanceRef, "Direction", interfaceInstanceDirectionRef);
					}
				}
				
				//Map it to the design-time element.
				if(interfaceStructureRef != null){
					//Promote to a prescribed interface instance
					xarch.promoteTo(t, instanceTypesContextRef, "PrescribedInterfaceInstance", interfaceInstanceRef);
					String originalInterfaceStructureID = (String)xarch.get(interfaceStructureRef, "Id");
					ObjRef structureLink = createXLink(xarch, t, instanceContextRef, structureURI + "#" + originalInterfaceStructureID);
					xarch.set(t, interfaceInstanceRef, "structure", structureLink);
				}

				//Add the interface instance to the brick instance
				xarch.add(t, elementInstanceRef, "InterfaceInstance", interfaceInstanceRef);
			}
			
			//Set the design-time link for the element (brick).
			if(elementStructureRef != null){
				if(isComponent){
					xarch.promoteTo(t, instanceTypesContextRef, "PrescribedComponentInstance", elementInstanceRef);
				}
				else{
					xarch.promoteTo(t, instanceTypesContextRef, "PrescribedConnectorInstance", elementInstanceRef);
				}
				String structureID = (String)xarch.get(elementStructureRef, "Id");
				ObjRef structureLink = createXLink(xarch, t, instanceContextRef, structureURI + "#" + structureID);
				xarch.set(t, elementInstanceRef, "structure", structureLink);
			}
		}

		//-----Do the links-----------
		fireMessageSent(
			new AEMProgressMessage(managedSystemURI, "CREATE_INSTANCE_MODEL",
			bricks.length + 1, numberOfBricksAndLinks + 1, "Modeling links..."));
		HashSet usedLinkIds = new HashSet();
		for(int w = 0; w < welds.length; w++){
			ObjRef linkInstanceRef = xarch.create(instanceContextRef, "LinkInstance");
			
			String linkId = null;
			ObjRef linkStructureRef = lookup(welds[w]);
			if(linkStructureRef == null){
				linkId = welds[w].getFirstEndpoint().getBrickIdentifier().toString() + "." +
					welds[w].getFirstEndpoint().getInterfaceIdentifier().toString() + "_to_" +
					welds[w].getSecondEndpoint().getBrickIdentifier().toString() + "." +
					welds[w].getFirstEndpoint().getInterfaceIdentifier().toString();
			}
			else{
				String linkStructureId = (String)xarch.get(linkStructureRef, "Id");
				linkId = linkStructureId + "_instance";
			}
			
			//System.out.println("Creating instances for link" + linkId);
	
			String baseLinkId = linkId;
			int c = 0;
			while(usedLinkIds.contains(linkId)){
				linkId = baseLinkId + (c++);
			}
			usedLinkIds.add(linkId);
			
			xarch.set(t, linkInstanceRef, "Id", linkId);
			
			String linkInstanceDescription = linkId;
			if(linkStructureRef != null){
				String linkStructureDescription = XadlUtils.getDescription(xarch, linkStructureRef);
				if(linkStructureDescription != null){
					linkInstanceDescription = linkStructureDescription;
				}
			}
			
			ObjRef linkDescriptionInstanceRef = xarch.create(instanceContextRef, "Description");
			xarch.set(t, linkDescriptionInstanceRef, "Value", linkInstanceDescription);
			xarch.set(t, linkInstanceRef, "Description", linkDescriptionInstanceRef);
			
			for(int j = 0; j < 2; j++){
				BrickInterfaceIdPair pair;
				if(j == 0){
					pair = welds[w].getFirstEndpoint();
				}
				else{
					pair = welds[w].getSecondEndpoint();
				}
				
				String weldBrickId = pair.getBrickIdentifier().toString();
				String weldInterfaceId = pair.getInterfaceIdentifier().toString();
				
				String endpointId = weldBrickId + "." + weldInterfaceId;
				
				ObjRef pointRef = xarch.create(instanceContextRef, "Point");
				
				ObjRef anchorOnInterfaceRef = createXLink(xarch, t, instanceContextRef, "#" + endpointId);
				xarch.set(t, pointRef, "AnchorOnInterface", anchorOnInterfaceRef);
				xarch.add(t, linkInstanceRef, "Point", pointRef);
			}
			
			//Map it to the design-time element.
			if(linkStructureRef != null){
				//Promote to a prescribed interface instance
				xarch.promoteTo(t, instanceTypesContextRef, "PrescribedLinkInstance", linkInstanceRef);
				String originalLinkStructureID = (String)xarch.get(linkStructureRef, "Id");
				ObjRef structureLink = createXLink(xarch, t, instanceContextRef, structureURI + "#" + originalLinkStructureID);
				xarch.set(t, linkInstanceRef, "structure", structureLink);
			}
			
			//Add the link to the ArchStructure
			xarch.add(t, archInstanceRef, "LinkInstance", linkInstanceRef);
		}

		fireMessageSent(
			new AEMProgressMessage(managedSystemURI, "CREATE_INSTANCE_MODEL",
			numberOfBricksAndLinks, numberOfBricksAndLinks + 1, "Committing transaction..."));
		
		xarch.commit(t);

		fireMessageSent(
			new AEMProgressMessage(managedSystemURI, "CREATE_INSTANCE_MODEL",
			numberOfBricksAndLinks + 1, numberOfBricksAndLinks + 1, "Done"));
	}
	
	protected Vector messageListeners = new Vector();
	
	protected void fireMessageSent(Message m){
		int size = messageListeners.size();
		for(int i = 0; i < size; i++){
			((MessageListener)messageListeners.elementAt(i)).messageSent(m);
		}
	}
	
	public void addMessageListener(MessageListener l){
		messageListeners.addElement(l);
	}
	
	public void removeMessageListener(MessageListener l){
		messageListeners.removeElement(l);
	}
	
	//-----Da Dynamism Schtuff--------------------
	public void handleTransactionEvent(XArchTransactionEvent transEvt){
		XArchFlatEvent[] evts = transEvt.getEvents();
		if(evts.length == 0){
			return;
		}
		ObjRef firstSrc = null;
		int x = 0;
		while(firstSrc == null){
			firstSrc = evts[x].getSource();
			x++;
			if(x == evts.length && firstSrc==null){
				return;
			}
		}
		
		ObjRef evtXArchRef = realxarch.getXArch(firstSrc);
		if(evtXArchRef.equals(xArchRef)){
			//We found one!
			ObjRef boundStructureRef = lookup(controller);
			if(boundStructureRef == null){
				return;
			}
			
			ArrayList linkRemovals = new ArrayList();
			ArrayList brickRemovals = new ArrayList();
			ArrayList brickAdds = new ArrayList();
			ArrayList linkAdds = new ArrayList();
			
			for(int i = 0; i < evts.length; i++){	
				if(evts[i].getSource().equals(boundStructureRef)){
					System.out.println(evts[i]);
					if(evts[i].getEventType() == XArchFlatEvent.REMOVE_EVENT){
						String targetName = evts[i].getTargetName();
						if(targetName.equalsIgnoreCase("link")){
							linkRemovals.add(evts[i]);
						}
						else if(targetName.equalsIgnoreCase("component")){
							brickRemovals.add(evts[i]);
						}
						else if(targetName.equalsIgnoreCase("connector")){
							brickRemovals.add(evts[i]);
						}
					}
					else if(evts[i].getEventType() == XArchFlatEvent.ADD_EVENT){
						String targetName = evts[i].getTargetName();
						if(targetName.equalsIgnoreCase("link")){
							linkAdds.add(evts[i]);
						}
						else if(targetName.equalsIgnoreCase("component")){
							brickAdds.add(evts[i]);
						}
						else if(targetName.equalsIgnoreCase("connector")){
							brickAdds.add(evts[i]);
						}
					}
				}
			}
			int index = 0;
			Identifier[] addedBrickIds = new Identifier[brickAdds.size()];
			int numRemovedBrickIds = brickRemovals.size();
			Identifier[] removedBrickIds = new Identifier[brickRemovals.size()];
			
			//Stop doomed bricks
			for(Iterator it = brickRemovals.iterator(); it.hasNext(); ){
				XArchFlatEvent evt = (XArchFlatEvent)it.next();
				ObjRef brickStructureRef = (ObjRef)evt.getTarget();
				Brick b = (Brick)reverseLookup(brickStructureRef);
				if(b == null){
					numRemovedBrickIds--;
					continue;
				}
				
				removedBrickIds[index] = b.getIdentifier();
				index++;
			}
		
			if(numRemovedBrickIds < removedBrickIds.length){
				Identifier[] rbi = new Identifier[numRemovedBrickIds];
				for(int j = 0; j < rbi.length; j++){
					rbi[j] = removedBrickIds[j];
				}
				removedBrickIds = rbi;
			}
			
			HashSet suspendedBrickIdSet = new HashSet();
			
			if(removedBrickIds.length > 0){
				//Suspend all connected bricks while we do this
				Weld[] allWelds = controller.getWelds();
				for(int i = 0; i < removedBrickIds.length; i++){
					for(int j = 0; j < allWelds.length; j++){
						Identifier firstBrickId = allWelds[j].getFirstEndpoint().getBrickIdentifier();
						Identifier secondBrickId = allWelds[j].getSecondEndpoint().getBrickIdentifier();
						if(firstBrickId.equals(removedBrickIds[i])){
							suspendedBrickIdSet.add(secondBrickId);
						}
						else if(secondBrickId.equals(removedBrickIds[i])){
							suspendedBrickIdSet.add(firstBrickId);
						}
					}
				}
				//Don't suspend doomed bricks cause we'll just end up trying to resume
				//them after we remove them and cause an exception.
				for(int i = 0; i < removedBrickIds.length; i++){
					suspendedBrickIdSet.remove(removedBrickIds[i]);
				}
				
				Identifier[] suspendedBrickIds = (Identifier[])suspendedBrickIdSet.toArray(new Identifier[0]);
				
				System.out.println("Suspending border bricks.");
				controller.suspend(suspendedBrickIds);
				try{
					System.out.println("Waiting for bricks to suspend.");
					controller.waitState(suspendedBrickIds, ArchitectureEngine.STATE_OPEN_NOTRUNNING_SUSPENDED);
				}
				catch(InterruptedException wontHappen){}
				
				System.out.println("Ending bricks.");
				for(int i = 0; i < removedBrickIds.length; i++){
					controller.end(removedBrickIds[i]);
				}
				
				/*
				try{
					Thread.sleep(2500);
				}
				catch(InterruptedException ie){}
				*/
				
				System.out.println("Stopping bricks.");
				controller.stop(removedBrickIds);
				try{
					System.out.println("Waiting for bricks to stop.");
					controller.waitState(removedBrickIds, ArchitectureEngine.STATE_CLOSED_COMPLETED);
				}
				catch(InterruptedException wontHappen){}
			}
			
			//Process link removals
			for(Iterator it = linkRemovals.iterator(); it.hasNext(); ){
				XArchFlatEvent evt = (XArchFlatEvent)it.next();
				ObjRef linkStructureRef = (ObjRef)evt.getTarget();
				
				Weld w = (Weld)reverseLookup(linkStructureRef);
				if(w != null){
					controller.removeWeld(w);
					remove(w);
				}
			}
			
			//Process brick removals
			for(int i = 0; i < removedBrickIds.length; i++){
				Brick b = ((LocalArchitectureManager)controller).getBrick(removedBrickIds[i]);
				Interface[] ifaces = b.getAllInterfaces();
				for(int j = 0; j < ifaces.length; j++){
					remove(ifaces[j]);
				}
				remove(b);
				controller.removeBrick(removedBrickIds[i]);
			}
			
			index = 0;
			//Process brick adds
			for(Iterator it = brickAdds.iterator(); it.hasNext(); ){
				XArchFlatEvent evt = (XArchFlatEvent)it.next();
				try{
					Identifier brickId = bindBrick(realxarch, (ObjRef)evt.getTarget());
					addedBrickIds[index] = brickId;
					index++;
				}
				catch(InvalidArchitectureDescriptionException iade){
					throw new RuntimeException(iade);
				}
			}
			
			//Process link adds
			for(Iterator it = linkAdds.iterator(); it.hasNext(); ){
				XArchFlatEvent evt = (XArchFlatEvent)it.next();
				try{
					bindLink(realxarch, (ObjRef)evt.getTarget());
				}
				catch(InvalidArchitectureDescriptionException iade){
					throw new RuntimeException(iade);
				}
			}
			
			//	the addedBrickIds might contain null ids of those insecure bricks
			int	truelyAdded = 0;	
			for (int i = 0; i<addedBrickIds.length; i++) {
				if (addedBrickIds[i] != null)
					truelyAdded++;
			}
			Identifier[]	truelyAddedBrickIds = new Identifier[truelyAdded];
			for (int i = 0; i<addedBrickIds.length; i++) { 
				if (addedBrickIds[i] != null)
					truelyAddedBrickIds[i] = addedBrickIds[i];
			}
			System.out.println("Starting new bricks.");
			controller.start(truelyAddedBrickIds);
			try{
				System.out.println("Waiting for new bricks to start.");
				controller.waitState(truelyAddedBrickIds, ArchitectureEngine.STATE_OPEN_RUNNING);
			}
			catch(InterruptedException wontHappen){}
			
			Identifier[] suspendedBrickIds = (Identifier[])suspendedBrickIdSet.toArray(new Identifier[0]);
			
			System.out.println("Resuming border bricks.");
			controller.resume(suspendedBrickIds);
			try{
				System.out.println("Waiting for bricks to resume.");
				controller.waitState(suspendedBrickIds, ArchitectureEngine.STATE_OPEN_RUNNING);
			}
			catch(InterruptedException wontHappen){}

			System.out.println("Beginning new bricks.");
			for(int j = 0; j < truelyAddedBrickIds.length; j++){
				controller.begin(truelyAddedBrickIds[j]);
			}
			System.out.println("Evolution complete.");
		}
	}
	
	public void archMessageSent(Message m){
		if(m instanceof ShutdownArchMessage){
			try{
				ShutdownArchMessage sam = (ShutdownArchMessage)m;
				shutdownSystem(sam.getShutdownType());
			}
			catch(InvalidArchitectureDescriptionException e){
				e.printStackTrace();
			}
		}
		WrappedArchMessage wam = new WrappedArchMessage(managedSystemURI, m);
		fireMessageSent(wam);
	}
}

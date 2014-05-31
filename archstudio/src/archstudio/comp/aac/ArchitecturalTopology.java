/*
 * Created on Nov 22, 2005
 *
 */
package archstudio.comp.aac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import archstudio.comp.aac.AACC2Component.SecurityProperties;
import archstudio.comp.aem.InvalidArchitectureDescriptionException;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchBulkQuery;
import edu.uci.ics.xarchutils.XArchBulkQueryResultProxy;
import edu.uci.ics.xarchutils.XArchBulkQueryResults;
import edu.uci.ics.xarchutils.XArchFlatInterface;
import edu.uci.ics.xarchutils.XArchFlatProxyUtils;
import edu.uci.ics.xarchutils.XArchFlatQueryInterface;
import edu.uci.isr.xarch.security.IPrivilege;
import edu.uci.isr.xarch.security.IPrivileges;
import edu.uci.isr.xarch.security.ISecureComponent;
import edu.uci.isr.xarch.security.ISecureComponentType;
import edu.uci.isr.xarch.security.ISecureConnector;
import edu.uci.isr.xarch.security.ISecureConnectorType;
import edu.uci.isr.xarch.security.ISecurityPropertyType;

/**
 * Maintain a topology for a xADL document.  
 * 
 * @author Jie Ren
 */
public class ArchitecturalTopology {
	public static String 	DIRECTION_INOUT = "inout";
	public static String 	DIRECTION_IN 	= "in";
	public static String 	DIRECTION_OUT   = "out";
	public static int		TOP_DOWN = 0x1;			// from in to out
	public static int		BOTTOM_UP = 0x2;		// from out to in

	XArchFlatInterface		realxarch;
	XArchFlatQueryInterface	xarch;
	ObjRef					xArchRef;
	int						flags;
	
	// Sub structures will be refered by the arch types.
	// bi-dreictional map betwen types and their sub architecture
	Map						typeToSubArchitecture = new HashMap();
	Map						subArchitectureToType = new HashMap();
	// This set records those types that has a subArchitecture and multiple instances
	// This requires generating multiple sets of ids for the instances when flatening the graph
	Set						typeWithInstances = new HashSet();
	// map from archStructure to its InterfaceGraph
	Map						archStructureToGraph = new HashMap();
	// all archStructures
	Set						allArchStructures = new HashSet();
	// archStructures that are contained by other archStructures
	Map						archStructureToContainer = new HashMap();
	// The reverse map of the above map, from an archStructure to those that are contained within it
	Map						archStructureToContainee = new HashMap();
	// archStructures that are not contained by anyone
	Set						topArchStructures = new HashSet();
	// brick to its associated archStructure
	Map						brickToSubArchitecture = new HashMap();
	
	/**
	 * Construct a topology for the architecture
	 *
	 * @param realxarch			the query interface
	 * @param xArchRef			the architectural reference
	 * @param flags				TOP_DOWN, BOTTOM_UP, or both
	 * @return a topology graph
	 */
	public ArchitecturalTopology(XArchFlatInterface realxarch, ObjRef xArchRef, int flags) {
		this.realxarch = realxarch;
		this.xArchRef = xArchRef;
		this.flags = flags;
		
		XArchBulkQuery q = new XArchBulkQuery(xArchRef);
		q.addQueryPath("archStructure*/id");
		q.addQueryPath("archStructure*/description/value");
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
		
		q.addQueryPath("renderingHints/hints*/hintedThing/type");
		q.addQueryPath("renderingHints/hints*/hintedThing/href");
		
		addSecurityQuery(q);
		
		XArchBulkQueryResults qr = realxarch.bulkQuery(q);
		xarch = new XArchBulkQueryResultProxy(realxarch, qr);
		ObjRef  	typeContext = null; //realxarch.createContext(xArchRef, "Types");
		
		ObjRef[]	typeRefs = xarch.getAllElements(typeContext, "ArchTypes", xArchRef);
		for (int i = 0; i<typeRefs.length; i++) {
			// should be only one ArchType
			ObjRef[] componentTypeRefs = xarch.getAll(typeRefs[i], "ComponentType");
			ObjRef[] connectorTypeRefs = xarch.getAll(typeRefs[i], "ConnectorType");
			for (int j = 0; j<componentTypeRefs.length; j++) {
				ObjRef subRef = (ObjRef)xarch.get(componentTypeRefs[j], "subArchitecture");
				if (subRef != null) {
					ObjRef subArchitecture = XadlUtils.resolveXLink(xarch, (ObjRef)xarch.get(subRef, "archStructure"));
					typeToSubArchitecture.put(componentTypeRefs[j], subArchitecture);
					subArchitectureToType.put(subArchitecture, componentTypeRefs[j]);
				}
			}
			for (int j = 0; j<connectorTypeRefs.length; j++) {
				ObjRef subRef = (ObjRef)xarch.get(connectorTypeRefs[j], "subArchitecture");
				if (subRef != null) {
					ObjRef subArchitecture = XadlUtils.resolveXLink(xarch, (ObjRef)xarch.get(subRef, "archStructure"));
					typeToSubArchitecture.put(connectorTypeRefs[j], subArchitecture);
					subArchitectureToType.put(subArchitecture, connectorTypeRefs[j]);
				}
			}
		}
		
		ObjRef[]	structureRef = xarch.getAllElements(typeContext, "ArchStructure", xArchRef);

		for (int j = 0; j<structureRef.length; j++) {
			InterfaceGraph g = new InterfaceGraph();
			ObjRef			archStructureRef = structureRef[j];
			ObjRef[]		refsToArchStructure = xarch.getReferences(xArchRef, 
					XadlUtils.getID(xarch, archStructureRef));
			boolean			referencedByType = false;
			for (int i = 0; i<refsToArchStructure.length; i++) {
				try {
					// See if any of these references are from types
					// Rendering Hints can reference the archStructure
					ObjRef typeRef = xarch.getParent(xarch.getParent(refsToArchStructure[i]));
					if (xarch.isInstanceOf(typeRef, "edu.uci.isr.xarch.types.IConnectorType") ||
						xarch.isInstanceOf(typeRef, "edu.uci.isr.xarch.types.IComponentType")) {
							ObjRef[]		instancesOfType = xarch.getReferences(xArchRef, 
									XadlUtils.getID(xarch, typeRef));
							int		numberOfInstances = 0;
							for (int k = 0; k<instancesOfType.length; k++) {
								ObjRef brickRef = null;
								try {
									brickRef = xarch.getParent(instancesOfType[k]);
								}
								catch (NullPointerException npe) {
								}
								catch (RuntimeException npe) {
								}
								if (brickRef != null && (
									xarch.isInstanceOf(brickRef, "edu.uci.isr.xarch.types.IConnector") ||
									xarch.isInstanceOf(brickRef, "edu.uci.isr.xarch.types.IComponent"))) {
									// This type is used for instances
									numberOfInstances++;
									referencedByType = true;
									ObjRef	referencingArchStructure = 
										xarch.getParent(brickRef);
									if (!archStructureRef.equals(referencingArchStructure)) {
										// an archStructure can reference itself, as in the DOM/Frame case
										// do not record the container/containee relationship for this "self" case
										// otherwise an endless loop results
										// record the container/containee relationship
										Set		containers = (Set)archStructureToContainer.get(archStructureRef); 
										if (containers == null) {
											containers = new HashSet();
											archStructureToContainer.put(archStructureRef, containers);
										}
										containers.add(referencingArchStructure);
										Set		containees = (Set)archStructureToContainee.get(referencingArchStructure); 
										if (containees == null) {
											containees = new HashSet();
											archStructureToContainee.put(referencingArchStructure, containees);
										}
										containees.add(archStructureRef);
									}
								}
							}
							if (numberOfInstances > 1) {
								typeWithInstances.add(typeRef);
							}
					}
				}
				catch (NullPointerException npe) {
				}
				catch (RuntimeException npe) {
				}
			}
			if (!referencedByType)
				topArchStructures.add(archStructureRef);
			
			ObjRef[] componentRefs = xarch.getAll(archStructureRef, "Component");
			ObjRef[] connectorRefs = xarch.getAll(archStructureRef, "Connector");
			ObjRef[] linkRefs = xarch.getAll(archStructureRef, "Link");
		
			for(int i = 0; i < linkRefs.length; i++){
			    XadlUtils.LinkInfo	linkInfo = XadlUtils.getLinkInfo(xarch, linkRefs[i], true);
			    ObjRef  target1 = linkInfo.getPoint1Target();
			    ObjRef  target2 = linkInfo.getPoint2Target();
			    // Add the edge between two interfaces
			    String  interface1 = XadlUtils.getID(xarch, target1);
			    String  interface2 = XadlUtils.getID(xarch, target2);
			    String	direction1 = XadlUtils.getDirection(xarch, target1);
			    String  direction2 = XadlUtils.getDirection(xarch, target2);
			    String  fromInterface = interface1;
			    String  toInterface = interface2;
			    if (direction1.equals(DIRECTION_IN)) {
			        fromInterface = interface2;
			        toInterface = interface1;
			        if (direction2.equals(DIRECTION_IN)) {
			            System.err.println(interface1 + " and " + interface2 + " are all in interfaces.");
			        }
			        else if (direction2.equals(DIRECTION_OUT)) {
			            
			        }
			        else { // Assume direction2 INOUT, uses as OUT
			            
			        }
			    }
			    else if (direction1.equals(DIRECTION_OUT)) {
			        fromInterface = interface1;
			        toInterface = interface2;
			        if (direction2.equals(DIRECTION_IN)) {

			        }
			        else if (direction2.equals(DIRECTION_OUT)) {
			            System.err.println(interface1 + " and " + interface2 + " are all out interfaces.");
			        }
			        else { // Assume direction2 INOUT, used as INT
			            
			        }
			    }
			    else {	// Assume direction1 INOUT
			        if (direction2.equals(DIRECTION_IN)) {
			            fromInterface = interface1;
			            toInterface = interface2;
			        }
			        else if (direction2.equals(DIRECTION_OUT)) {
			            fromInterface = interface2;
			            toInterface = interface1;
			        }
			        else { // Assume directin2 INOUT
			            // This also assumes the interface1, which is the 
			            // first interface appeared in xADL, takes the top position,
			            if ((flags & TOP_DOWN) != 0) {
			                // add one direction
			                fromInterface = interface1;
			                toInterface = interface2;
			                g.addEdge(fromInterface, toInterface);
			            }
			            if ((flags & BOTTOM_UP) != 0) {
				            // add one more direction
				            fromInterface = interface2;
				            toInterface = interface1;
			            }
			        }
			    }
			    g.addEdge(fromInterface, toInterface);
			}

			// Remember all the in and out interfaces for each brick
			Map		inInterfaces = new HashMap();
			Map		outInterfaces = new HashMap();
			for(int i = 0; i < componentRefs.length; i++){
			    handleBrickInterfaces(componentRefs[i], inInterfaces, outInterfaces, g);
			}
	
			for(int i = 0; i < connectorRefs.length; i++){
			    handleBrickInterfaces(connectorRefs[i], inInterfaces, outInterfaces, g);
			}
			
			archStructureToGraph.put(archStructureRef, g);
		}
		
		// get the topological order of the referencing archStrcutures
		// the first are the leaf archStructures that contain none
		// the last are the top archStructures that are not contained by any
		List	order = new ArrayList();
		while (order.size() < archStructureToContainer.size()) {
			for (Iterator i = archStructureToContainer.keySet().iterator(); i.hasNext();) {
				ObjRef		o = (ObjRef)i.next();
				Set containees = (Set)archStructureToContainee.get(o);
				if ((containees == null || order.containsAll(containees)) && !order.contains(o))
					order.add(o);
			}
		}
		order.addAll(topArchStructures);
		
		// Flattening the architectural topology
		for (Iterator i = order.iterator(); i.hasNext(); ) {
			ObjRef	archStructureRef = (ObjRef)i.next();
			ObjRef[] componentRefs = xarch.getAll(archStructureRef, "Component");
			ObjRef[] connectorRefs = xarch.getAll(archStructureRef, "Connector");
			ObjRef[] brickRefs = new ObjRef[componentRefs.length + connectorRefs.length];
			for (int j = 0; j<componentRefs.length; j++) {
				brickRefs[j] = componentRefs[j];
			}
			for (int j = 0; j<connectorRefs.length; j++) {
				brickRefs[j + componentRefs.length] = connectorRefs[j];
			}

			// get the graph for this archStructure
			InterfaceGraph container = (InterfaceGraph)archStructureToGraph.get(archStructureRef);
			for (int j = 0; j<brickRefs.length; j++) {
				if (brickToSubArchitecture.containsKey(brickRefs[j])) {
					// flattening this brick's subArchitecture graph
					InterfaceGraph containee = (InterfaceGraph)archStructureToGraph.get(
							brickToSubArchitecture.get(brickRefs[j]));
					ObjRef	typeRef = getType(brickRefs[j]);
					String	prefix = "";
					if (typeRef != null && typeWithInstances.contains(typeRef)) {
						// If this is a type with multiple instances,
						// then the graph needs a name prefix to avoid duplicate nodes
						//
						// TODO: In the most general case, the prefix should
						// include the names from the higher containers, not 
						// just the immediate brick. For now we settle for 
						// a simple, one-instance-per-type scenario.
						//
						// Another solution would be dynamically search through
						// the subArchitectures, without flatenning the 
						// complete topology in the first place. This would be
						// similar to a proned search. The difficulty is where
						// we should guide the dynamic search without the 
						// complete picture. The current complete flatening
						// is not expensive, compared to other factors.
						//
						// The current UI sets the interface ids as 
						// accessing/accessed. This also needs to be retrieved
						// in the right context
						//
						prefix = XadlUtils.getID(xarch, brickRefs[j]) + "-";
					}
					container.addEges(containee, prefix);
					// Now connect the outinterface to the inner interface, through signature
					ObjRef[]	interfaceRefs = xarch.getAll(brickRefs[j], "Interface");
					for (int k = 0; k<interfaceRefs.length; k++) {
						String	outerInterface = XadlUtils.getID(xarch, interfaceRefs[k]);
						ObjRef  sRef = (ObjRef)xarch.get(interfaceRefs[k], "signature");
						if (sRef != null) {
							ObjRef	signatureRef = XadlUtils.resolveXLink(xarch, sRef);
							ObjRef[] maps = XadlUtils.getSignatureInterfaceMappings(
									xarch, typeRef, signatureRef);
							for (int l = 0; l<maps.length; l++) {
								ObjRef	inner = (ObjRef)xarch.get(maps[l], "innerInterface");
								String  innerInterface = XadlUtils.getID(xarch, XadlUtils.resolveXLink(xarch, inner));
								container.addEdge(outerInterface, prefix + innerInterface);
							}
						}
					}
				}
			}
		}

		for (Iterator i = order.iterator(); i.hasNext(); ) {
			InterfaceGraph g = (InterfaceGraph)archStructureToGraph.get(i.next());
			// Calculate the reachability closure for interfaces
			g.calculateClosure();
		}
	}
	
	/**
	 * Get the path between two interfaces
	 * 
	 * @param from	the id of the starting interface
	 * @param to	the id of the ending interface
	 * @return	the path between the interfaces
	 */
	public List getPath(String from, String to) {
		List	result = null;
		ObjRef	fromRef = xarch.getByID(from);
		ObjRef	toRef = xarch.getByID(to);
		if (fromRef != null && toRef != null) {
			ObjRef fromStructure = xarch.getParent(xarch.getParent(fromRef));
			ObjRef toStructure = xarch.getParent(xarch.getParent(toRef));
			InterfaceGraph 	g = null;
			if (fromStructure.equals(toStructure)) {
				// both to and from belong to the same structure, so use it
				// TODO: there are many different interpretations on how the 
				// 	graph should be returned, such as a non-flattened version
				g = (InterfaceGraph)archStructureToGraph.get(fromStructure);
				result = g.getPath(from, to);
			}
			else {
				// they belong to different structures, so use top structures
				// TODO: again, it can be either the top level, or the common ancestor
				for (Iterator i = topArchStructures.iterator(); i.hasNext(); ) {
					// if we can find a path 
					g = (InterfaceGraph)archStructureToGraph.get(i.next());
					result = g.getPath(from, to);
					if (result != null)
						break;
				}
			}
		}
		return result;
	}
	
	/**
	 * Get the list of secuity properties for the container hierarchy of a brick
	 * 
	 * @param brickRef		a reference to the brick
	 * @return	a list of properties, the first item is of the immediate container, 
	 * the last item is of the top most container 
	 */
	public List getContainerProperties(ObjRef brickRef) {
		List	containerRefs = getContainers(brickRef);
		
		List	containerProperties = new ArrayList();
		// The security property can be attached to both the type and the archStructure
		// Since the type is more "prominent", the general rule is that all security 
		// properties are attached with types, with the types have subArchitecture or not
		// The only security attached to archStructure would be for the top archStructure,
		// which usually has no type
		// TODO: document this
		for (Iterator i = containerRefs.iterator(); i.hasNext(); ) {
			ObjRef archStructureRef = (ObjRef)i.next();
			ObjRef 	typeWithSubArchitectureRef = (ObjRef)subArchitectureToType.get(archStructureRef);
			// First, see whether the type has a security attached
			ObjRef	topSecurity = null;
			if (typeWithSubArchitectureRef != null) // a top structure might not have a type
				topSecurity = (ObjRef)xarch.get(typeWithSubArchitectureRef, "Security");
			// Then, see whether the substrcture has a security attached.
			// So, we give preference to types, but we do not miss anything
			if (topSecurity == null)
				topSecurity = (ObjRef)xarch.get(archStructureRef, "Security");
			if (topSecurity != null) {
			    ISecurityPropertyType iSecurity = (ISecurityPropertyType)
			    	XArchFlatProxyUtils.proxy(realxarch, topSecurity);
			    containerProperties.add(iSecurity);
			}
		}
		return containerProperties;
	}
	
	/**
	 * Get the list of secuity properties for the container hierarchy of a brick
	 * 
	 * @param brickRef		a reference to the brick
	 * @return	a list of properties, the first item is of the immediate container, 
	 * the last item is of the top most container 
	 */
	public List getContainers(ObjRef brickRef) {
		List	containerRefs = new ArrayList();
		ObjRef	brick = brickRef;
		ObjRef	archStructureRef = xarch.getParent(brick);
		boolean	keepGoing = true;
		while (keepGoing) {
			// remember the container
			containerRefs.add(archStructureRef);
			keepGoing = false;
			ObjRef	typeWithSubArchitectureRef = (ObjRef)subArchitectureToType.get(archStructureRef);
			if (typeWithSubArchitectureRef != null) {
				ObjRef[]	instancesOfType = xarch.getReferences(xArchRef, 
						XadlUtils.getID(xarch, typeWithSubArchitectureRef));
				for (int i = 0; i<instancesOfType.length; i++) {
					// Get the parents, which would be the real instance
					try {
						instancesOfType[i] = xarch.getParent(instancesOfType[i]);
					}
					catch (Exception e) {
						instancesOfType[i] = null;
					}
				}
				for (int i = 0; i<instancesOfType.length; i++) {
					if (instancesOfType[i] != null && 
						(xarch.isInstanceOf(instancesOfType[i], "edu.uci.isr.xarch.types.IConnector") ||
						 xarch.isInstanceOf(instancesOfType[i], "edu.uci.isr.xarch.types.IComponent"))) {
						// The ObjRef may be something else, such as a rendering hint
						// Assume all instances of the type are used in just one archStructure
						// This is a different assumption than archStructureToReferencings, 
						// where an archStructure can be referenced by multiple archStructure
						brick = instancesOfType[i];
						// Get the container of this brick
						archStructureRef = xarch.getParent(brick);
						keepGoing = true;
						break;
					}
				}
			}
		}
		return containerRefs;
	}
	
	/**
	 *  Add queries related to security to a bulk query
	 *  
	 *  @param result a query to receive security related queries
	 */
	public static void addSecurityQuery(XArchBulkQuery result) {
		result.addQueryPath("archStructure*/component*/interface*/safeguards/safeguard*/value");
		// Add component and connector initialization parameters that we are interested
		result.addQueryPath("archStructure*/component*/initializationParameter*/name");
		result.addQueryPath("archStructure*/component*/initializationParameter*/value");
		result.addQueryPath("archStructure*/component*/initializationParameter*/priority");
		// The security properties on bricks and brick types: subject, privilege, and policy
		addOneSecurityQuery(result, "archStructure*/component*");

		result.addQueryPath("archStructure*/connector*/interface*/safeguards/safeguard*/value");
		result.addQueryPath("archStructure*/connector*/initializationParameter*/name");
		result.addQueryPath("archStructure*/connector*/initializationParameter*/value");
		result.addQueryPath("archStructure*/connector*/initializationParameter*/priority");
		addOneSecurityQuery(result, "archStructure*/connector*");

		result.addQueryPath("archTypes/componentType*/signature*/safeguards/safeguard*/value");
		addOneSecurityQuery(result, "archStructure*/componentType*");

		result.addQueryPath("archTypes/connectorType*/signature*/safeguards/safeguard*/value");
		addOneSecurityQuery(result, "archStructure*/connectorType*");

		addOneSecurityQuery(result, "archStructure*");
	}
	
	public static void addOneSecurityQuery(XArchBulkQuery result, String prefix) {
		result.addQueryPath(prefix + "/security");
		result.addQueryPath(prefix + "/security/subject*");
		result.addQueryPath(prefix + "/security/subject*/value");
		result.addQueryPath(prefix + "/security/privileges");
		result.addQueryPath(prefix + "/security/privileges/privilege*");
		result.addQueryPath(prefix + "/security/privileges/privilege*/value");
		result.addQueryPath(prefix + "/security/principals");
		result.addQueryPath(prefix + "/security/principals/principal*");
		result.addQueryPath(prefix + "/security/principals/principal*/value");
		result.addQueryPath(prefix + "/security/policies");
		result.addQueryPath(prefix + "/security/policies/PolicySet*");
		result.addQueryPath(prefix + "/security/policies/PolicySet*/Policy");
	}
	
	public ObjRef getType(ObjRef brickRef) {
		ObjRef 		typeRef = null;
        try {
            typeRef = XadlUtils.resolveTypeLink(xarch, xArchRef, brickRef);
        }
        catch (InvalidArchitectureDescriptionException e) {
        	// if there is no type, so be it
            // e.printStackTrace();
        }
        return typeRef;
	}
	
	// connect the in interfaces of a brick to its out interfaces
	private void handleBrickInterfaces(ObjRef brickRef, Map inInterfaces, Map outInterfaces, InterfaceGraph g) {
	    String		brick = XadlUtils.getID(xarch, brickRef); 
	    Set		in = (Set)inInterfaces.get(brick);
	    if (in == null) {
	        in = new HashSet();
	        inInterfaces.put(brick, in);
	    }
	    Set		out = (Set)outInterfaces.get(brick);
	    if (out == null) {
	        out = new HashSet();
	        outInterfaces.put(brick, out);
	    }
	    
	    // Get in and out interfaces for each brick
	    ObjRef[]	interfaces = xarch.getAll(brickRef, "Interface");
	    for (int i = 0; i<interfaces.length; i++) {
	        String direction = XadlUtils.getDirection(xarch, interfaces[i]);
	        String id = XadlUtils.getID(xarch, interfaces[i]);
	        if (direction.equals(DIRECTION_IN)) {
	            in.add(id);
	        }
	        else if (direction.equals(DIRECTION_OUT)) {
	            out.add(id);
	        }
	        else {
	            in.add(id);
	            out.add(id);
	        }
	    }

	    // Get its type
		ObjRef 		typeRef = getType(brickRef);
        // If this component is of a type with its subArchitecture, 
        // we will postpone it for later flatenning 
        if (typeRef != null && typeToSubArchitecture.containsKey(typeRef)) {
        	brickToSubArchitecture.put(brickRef, typeToSubArchitecture.get(typeRef));
        	return;
        }
	    
	    // Add the internal link between interfaces of a brcick
	    for (Iterator i = in.iterator(); i.hasNext();) {
	        String	inInterface = (String)i.next();
	        for (Iterator o = out.iterator(); o.hasNext(); ) {
	            String outInterface = (String)o.next();
	            if (!inInterface.equals(outInterface)) {
	            	boolean		add = true;
	                if (((inInterface.indexOf("IFACE_BOTTOM") != -1) && (flags & BOTTOM_UP) == 0) || 
	                    ((inInterface.indexOf("IFACE_TOP") != -1) && (flags & TOP_DOWN) == 0))
	                	// If this is a C2 interface, then only add it in the right direction
	                	add = false;
	                if (add)
	                    g.addEdge(inInterface, outInterface);
	            }
	        }
	    }
	}
	
	/**
	 * Get the related security properties of a brick.
	 * 
	 * @param brickRef			the reference of the brick
	 * @return the security properties for the brick, including the instance, 
	 * the type, the container, and the global properties. 
	 */
	public SecurityProperties getSecurityProperties(ObjRef brickRef) {
		SecurityProperties 	result = new SecurityProperties();
		if (brickRef != null) {
		    // Get its type
			ObjRef 		typeRef = getType(brickRef);
	        // Get security for the brick and its type
	        ISecureConnector		iConnector = null;
			ISecureComponent		iComponent = null;
			ISecureConnectorType	iConnectorType = null;
			ISecureComponentType	iComponentType = null;
			if(xarch.isInstanceOf(brickRef, "edu.uci.isr.xarch.security.ISecureConnector")) {
				iConnector = (ISecureConnector)XArchFlatProxyUtils.
									proxy(realxarch, brickRef);
				result.brick = iConnector.getSecurity();
			}
			if(xarch.isInstanceOf(brickRef, "edu.uci.isr.xarch.security.ISecureComponent")) { 
				iComponent = (ISecureComponent)XArchFlatProxyUtils.
									proxy(realxarch, brickRef);
				result.brick = iComponent.getSecurity();
			}
			if (typeRef != null) {
				if(xarch.isInstanceOf(typeRef, "edu.uci.isr.xarch.security.ISecureConnectorType")) {
					iConnectorType	= (ISecureConnectorType)XArchFlatProxyUtils.
										proxy(realxarch, typeRef);
					result.brickType = iConnectorType.getSecurity();
				}
				if(xarch.isInstanceOf(typeRef, "edu.uci.isr.xarch.security.ISecureComponentType")) { 
					iComponentType = (ISecureComponentType)XArchFlatProxyUtils.
										proxy(realxarch, typeRef);
					result.brickType = iComponentType.getSecurity();
				}
			}
			result.containers = getContainerProperties(brickRef);
		}
		return result;
	}
	
	/**
	 * Return the security property associated with the architecture. 
	 * This method assumes there is only one archStructure that is the top structure 
	 * 
	 * @return the security property associated with the top architecture, or null if 
	 * no security is specified for the top architecture
	 */
	public ISecurityPropertyType getArchitecturalProperty() {
		ISecurityPropertyType	result = null;
		for (Iterator i = topArchStructures.iterator(); i.hasNext(); ) {
			ObjRef		archStructureRef = (ObjRef)i.next();
			ObjRef		securityRef = (ObjRef)xarch.get(archStructureRef, "security");
			if (securityRef != null) {
				// If we find one, then return it
				result = (ISecurityPropertyType)XArchFlatProxyUtils.proxy(
						realxarch, securityRef);
				break;
			}
		}
		return result;
	}

	/**
	 * Get the privileges from different security properties
	 * 
	 * @param sp 				the security properties to get the privileges for
	 * @return					the most specific privileges, in the order of 
	 * instance/type/container/global.
	 */
	public static Set getPrivileges(SecurityProperties sp) {
		Collection				result = null;
		ISecurityPropertyType	iSecurity = sp.brick;
		ISecurityPropertyType	iSecurityType = sp.brickType;
		IPrivileges				iPrivileges = null;
		if (iSecurity != null || iSecurityType != null) {
			// Get privileges from the brick
	        iPrivileges = iSecurity.getPrivileges();
	        if (iPrivileges != null) {
	            result = iPrivileges.getAllPrivileges();
	        }
	        else {
	            // found no privileges from the brick, try the type
	            iPrivileges = iSecurityType.getPrivileges();
	            if (iPrivileges != null) {
	                result = iPrivileges.getAllPrivileges();
	            }
	        }
		}
		if (result == null ) {
			// nothing from brick and type, let's try containers
			for (Iterator i = sp.containers.iterator(); i.hasNext(); ) {
				iSecurity = (ISecurityPropertyType)i.next();
				iPrivileges = iSecurity.getPrivileges();
	            if (iPrivileges != null) {
	                result = iPrivileges.getAllPrivileges();
	                if (!result.isEmpty())
	                	// we will stop after finding one
	                	break;
	            }
			}
		}
		// Get privilege strings
        Set	s = null;
        if (result != null) {
            s = new HashSet();
			for (Iterator i = result.iterator(); i.hasNext(); ) {
			    IPrivilege privilege = (IPrivilege)i.next();
			    s.add(privilege.getValue());
			}
        }
        return s;
	}

	/**
	 * Get the privileges for the brick of an interface. 
	 * 
	 * @param interfaceName		the name of the interface
	 * @return the set of privileges. Null if no privileges.
	 */
	public Set getPrivileges(String interfaceId) {
	    ObjRef	interfaceRef = xarch.getByID(xArchRef, interfaceId);
	    if (interfaceRef == null)
	        return null;
	    return getPrivileges(interfaceRef);
	}
	
	/**
	 * Get the privileges for the brick of an interface. 
	 * 
	 * @param interfaceRef		the reference of the interface
	 * @return the set of privileges. Null if no privileges.
	 */
	public Set getPrivileges(ObjRef interfaceRef) {
		// Get the brick of the interface
	    ObjRef		brickRef = xarch.getParent(interfaceRef);
        // Get security for the brick and its type
		SecurityProperties sp = getSecurityProperties(brickRef);
		return getPrivileges(sp);
	}
}

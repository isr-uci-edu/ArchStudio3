package archstudio.comp.archipelago.types;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.logic.MouseTrackingLogic;
import edu.uci.ics.bna.thumbnail.Thumbnail;
import edu.uci.ics.widgets.WidgetUtils;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.*;

import archstudio.comp.archipelago.*;
import archstudio.comp.xarchtrans.*;

import java.awt.*;
import java.util.*;

import c2.util.UIDGenerator;

public class BrickMappingLogic extends AbstractMappingLogic{
	
	//public static final Color DEFAULT_COMPONENT_COLOR = edu.uci.ics.widgets.Colors.PALE_DULL_CYAN;
	//public static final Color DEFAULT_CONNECTOR_COLOR = edu.uci.ics.widgets.Colors.PALE_DULL_MAGENTA;
	
	public static final Color DEFAULT_COMPONENT_COLOR = new Color(0xC5CBFF);
	public static final Color DEFAULT_CONNECTOR_COLOR = new Color(0xFFEABF);
	
	protected ThingIDMap thingIDMap;
	
	protected ArchStructureTreePlugin archStructureTreePlugin;
	protected BNAModel structureBNAModel;
	protected MouseTrackingLogic mtl = null;
	protected ObjRef mainStructureRef;

	protected archstudio.comp.archipelago.RenderingHints renderingHints;
	
	protected NoThing brickParent;
	
	static int offset = 0;
	
	protected Vector brickMappingLogicListeners = new Vector();
	
	protected XArchFlatQueryInterface xarchbulk = null;
	
	public BrickMappingLogic(ArchStructureTreePlugin astp, ObjRef mainStructureRef,
	BNAModel[] bnaModels, MouseTrackingLogic mtl,
	XArchFlatTransactionsInterface xarch,	ThingIDMap thingIDMap, 
	archstudio.comp.archipelago.RenderingHints renderingHints){
		super(bnaModels, xarch);
		this.archStructureTreePlugin = astp;
		this.mainStructureRef = mainStructureRef;
		this.structureBNAModel = bnaModels[0];
		this.mtl = mtl;
		this.thingIDMap = thingIDMap;
		this.renderingHints = renderingHints;
		
		brickParent = new NoThing("$$BrickParent");
		structureBNAModel.addThing(brickParent);
		
		runBulkQuery();
	}
	
	private void runBulkQuery(){
		ObjRef xArchRef = xarch.getXArch(mainStructureRef);
		XArchBulkQuery q = getBulkQuery(xArchRef);
		XArchBulkQueryResults qr = xarch.bulkQuery(q);
		xarchbulk = new XArchBulkQueryResultProxy(xarch, qr);
	}
	
	public static XArchBulkQuery getBulkQuery(ObjRef xArchRef){
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
		
		return q;
	}
	
	public void addBrickMappingLogicListener(BrickMappingLogicListener l){
		brickMappingLogicListeners.addElement(l);
	}
	
	public void removeBrickMappingLogicListener(BrickMappingLogicListener l){
		brickMappingLogicListeners.removeElement(l);
	}
	
	protected void fireComponentUpdating(ObjRef brickRef, ComponentThing ct){
		synchronized(brickMappingLogicListeners){
			for(Iterator it = brickMappingLogicListeners.iterator(); it.hasNext(); ){
				((BrickMappingLogicListener)it.next()).componentUpdating(brickRef, ct);
			}
		}
	}
	
	protected void fireComponentUpdated(ObjRef brickRef, ComponentThing ct){
		synchronized(brickMappingLogicListeners){
			for(Iterator it = brickMappingLogicListeners.iterator(); it.hasNext(); ){
				((BrickMappingLogicListener)it.next()).componentUpdated(brickRef, ct);
			}
		}
	}

	protected void fireComponentRemoving(ObjRef brickRef, ComponentThing ct){
		synchronized(brickMappingLogicListeners){
			for(Iterator it = brickMappingLogicListeners.iterator(); it.hasNext(); ){
				((BrickMappingLogicListener)it.next()).componentRemoving(brickRef, ct);
			}
		}
	}
	
	protected void fireComponentRemoved(ObjRef brickRef, ComponentThing ct){
		synchronized(brickMappingLogicListeners){
			for(Iterator it = brickMappingLogicListeners.iterator(); it.hasNext(); ){
				((BrickMappingLogicListener)it.next()).componentRemoved(brickRef, ct);
			}
		}
	}
	
	protected void fireConnectorUpdating(ObjRef brickRef, ConnectorThing ct){
		synchronized(brickMappingLogicListeners){
			for(Iterator it = brickMappingLogicListeners.iterator(); it.hasNext(); ){
				((BrickMappingLogicListener)it.next()).connectorUpdating(brickRef, ct);
			}
		}
	}
	
	protected void fireConnectorUpdated(ObjRef brickRef, ConnectorThing ct){
		synchronized(brickMappingLogicListeners){
			for(Iterator it = brickMappingLogicListeners.iterator(); it.hasNext(); ){
				((BrickMappingLogicListener)it.next()).connectorUpdated(brickRef, ct);
			}
		}
	}

	protected void fireConnectorRemoving(ObjRef brickRef, ConnectorThing ct){
		synchronized(brickMappingLogicListeners){
			for(Iterator it = brickMappingLogicListeners.iterator(); it.hasNext(); ){
				((BrickMappingLogicListener)it.next()).connectorRemoving(brickRef, ct);
			}
		}
	}
	
	protected void fireConnectorRemoved(ObjRef brickRef, ConnectorThing ct){
		synchronized(brickMappingLogicListeners){
			for(Iterator it = brickMappingLogicListeners.iterator(); it.hasNext(); ){
				((BrickMappingLogicListener)it.next()).connectorRemoved(brickRef, ct);
			}
		}
	}
	
	public BNAModel getBNAModel(){
		return structureBNAModel;
	}
	
	public void handleXArchFlatEvent(XArchFlatEvent evt){
		XArchPath sourcePath = evt.getSourcePath();
		String sourcePathString = null;
		if(sourcePath != null) sourcePathString = sourcePath.toTagsOnlyString();
		
		XArchPath targetPath = evt.getTargetPath();
		String targetPathString = null;
		if(targetPath != null) targetPathString = targetPath.toTagsOnlyString();

		//System.out.println("sourcePathString: " + sourcePathString);
		//System.out.println("targetPathString: " + targetPathString);

		//System.out.println("evt: " + evt);
		
		if((evt.getEventType() == XArchFlatEvent.CLEAR_EVENT) || (evt.getEventType() == XArchFlatEvent.REMOVE_EVENT)){
			if((sourcePathString != null) && (sourcePathString.equals("xArch/archStructure"))){
				if((targetPathString != null) && (targetPathString.equals("component"))){
					runBulkQuery();
					//System.out.println("rcomp");
					removeComponent((ObjRef)evt.getTarget());
				}
				else if((targetPathString != null) && (targetPathString.equals("connector"))){
					runBulkQuery();
					//System.out.println("rconn");
					removeConnector((ObjRef)evt.getTarget());
				}
			}
			else if((sourcePathString != null) && (sourcePathString.startsWith("xArch/archStructure/component"))){
				runBulkQuery();
				//System.out.println("ucomp");
				doUpdateComponent(evt);
			}
			else if((sourcePathString != null) && (sourcePathString.startsWith("xArch/archStructure/connector"))){
				runBulkQuery();
				//System.out.println("uconn");
				doUpdateConnector(evt);
			}
		}
		
		//System.out.println("sourcePath: " + sourcePathString);
		//System.out.println("targetPath: " + targetPathString);
		
		if(targetPathString == null){
			targetPathString = sourcePathString;
		}
		
		if((targetPathString != null) && (targetPathString.startsWith("xArch/archStructure/component"))){
			//System.out.println("updating component");
			runBulkQuery();
			doUpdateComponent(evt);
		}
		else if((targetPathString != null) && (targetPathString.startsWith("xArch/archStructure/connector"))){
			runBulkQuery();
			doUpdateConnector(evt);
		}
	}
	
	private void doUpdateComponent(XArchFlatEvent evt){
		//It's a component event
		
		XArchPath targetPath = evt.getTargetPath();
		if(targetPath != null){
			String targetPathString = targetPath.toTagsOnlyString();
			if(targetPathString.equals("xArch/archStructure/component")){
				updateComponent((ObjRef)evt.getTarget());
				return;
			}
		}
		
		ObjRef src = evt.getSource();
		if(src != null){
			ObjRef[] ancestors = null;
			try{
				ancestors = xarchbulk.getAllAncestors(src);
			}
			catch(Exception e){
				ancestors = xarch.getAllAncestors(src);
			}
			//System.out.println("ancestors.length = " + ancestors.length);
			//The component will be ancestor # ancestors.length - 3
			ObjRef componentRef = ancestors[ancestors.length - 3];
			//System.out.println(xarch.getType(componentRef));
			updateComponent(componentRef);
		}
	}
	
	private void doUpdateConnector(XArchFlatEvent evt){
		//It's a connector event

		XArchPath targetPath = evt.getTargetPath();
		if(targetPath != null){
			String targetPathString = targetPath.toTagsOnlyString();
			if(targetPathString.equals("xArch/archStructure/connector")){
				updateConnector((ObjRef)evt.getTarget());
				return;
			}
		}

		ObjRef src = evt.getSource();
		if(src != null){
			ObjRef[] ancestors = null;
			try{
				ancestors = xarchbulk.getAllAncestors(src);
			}
			catch(Exception e){
				ancestors = xarch.getAllAncestors(src);
			}
			
			ObjRef connectorRef = ancestors[ancestors.length - 3];
			//System.out.println(xarch.getType(connectorRef));
			updateConnector(connectorRef);
		}
	}
	
	public void handleXArchFileEvent(XArchFileEvent evt){}
	
	public void updateComponent(ObjRef componentRef){
		ObjRef structureRef = xarchbulk.getParent(componentRef);
		if(structureRef != null){
			if(!structureRef.equals(mainStructureRef)){
				return;
			}
		}
		ComponentThing ct;
		
		String existingThingID = thingIDMap.getThingID(componentRef);
		if(existingThingID != null){
			try{
				ct = (ComponentThing)structureBNAModel.getThing(existingThingID);
			}
			catch(ClassCastException cce){
				System.err.println("Warning; ID/type mismatch.");
				cce.printStackTrace();
				return;
			}
		}
		else{
			ct = new ComponentThing();
		}
		
		fireComponentUpdating(componentRef, ct);
		updateBrick(componentRef, ct, existingThingID);
		fireComponentUpdated(componentRef, ct);
	}
	
	public void updateConnector(ObjRef connectorRef){
		ObjRef structureRef = xarchbulk.getParent(connectorRef);
		if(structureRef != null){
			if(!structureRef.equals(mainStructureRef)){
				return;
			}
		}

		ConnectorThing ct;
		
		String existingThingID = thingIDMap.getThingID(connectorRef);
		if(existingThingID != null){
			try{
				ct = (ConnectorThing)structureBNAModel.getThing(existingThingID);
			}
			catch(ClassCastException cce){
				System.err.println("Warning; ID/type mismatch.");
				cce.printStackTrace();
				return;
			}
		}
		else{
			ct = new ConnectorThing();
		}
		
		fireConnectorUpdating(connectorRef, ct);
		updateBrick(connectorRef, ct, existingThingID);
		fireConnectorUpdated(connectorRef, ct);
	}
	
	private void updateBrick(ObjRef brickRef, BrickThing ct, String existingThingID){
		try{
			structureBNAModel.beginBulkChange();
			String ctID = edu.uci.ics.xadlutils.XadlUtils.getID(xarchbulk, brickRef);
			ct.setXArchID(ctID);
			thingIDMap.mapRefToID(brickRef, ct.getID());
		
			String label = "(No Description)";
			String desc = edu.uci.ics.xadlutils.XadlUtils.getDescription(xarchbulk, brickRef);
			if(desc != null) label = desc;
			ct.setLabel(label);
		
			if(existingThingID == null){
				//If it hasn't been placed already...
				//Set a default bounding box
				if(mtl.getLastWorldX() == -1){
					ct.setBoundingBox((DefaultCoordinateMapper.DEFAULT_WORLD_WIDTH / 2) + offset,
					(DefaultCoordinateMapper.DEFAULT_WORLD_HEIGHT / 2) + offset, 100, 100);
					offset += 10;
				}
				else{
					ct.setBoundingBox(mtl.getLastWorldX(),
					mtl.getLastWorldY(), 100, 100);
				}
			
				//Set a default color and border
				if(ct instanceof ComponentThing){
					Color c = archStructureTreePlugin.getDefaultComponentColor();
					ct.setColor(c);
					ct.setDoubleBorder(true);
				}
				else if(ct instanceof ConnectorThing){
					Color c = archStructureTreePlugin.getDefaultConnectorColor();
					ct.setColor(c);
				}
				else{
					ct.setColor(Color.WHITE);
				}
			
				renderingHints.applyRenderingHints(ctID, ct);

				structureBNAModel.addThing(ct, brickParent);
			}
			//See if we're inheriting color from type
			updateInheritColorFromType(brickRef, ct);
		
			updateInterfaces(brickRef, ct);
			updateSubArchitecture(brickRef, ct);
		}
		finally{
			structureBNAModel.endBulkChange();
		}
	}
	
	public void updateInheritColorFromType(ObjRef brickRef, BrickThing ct){
		//See if we're inheriting color from type
		boolean inheritColorFromType = ct.getInheritColorFromType();
		if(inheritColorFromType){
			Color typeColor = getTypeColor(brickRef, ct);
			if(typeColor != null){
				ct.setColor(typeColor);
			}
		}
	}
	
	private Color getTypeColor(ObjRef brickRef, BrickThing ct){
		ObjRef typeLinkRef = (ObjRef)xarchbulk.get(brickRef, "type");
		if(typeLinkRef != null){
			String href = XadlUtils.getHref(xarchbulk, typeLinkRef);
			if(href != null){
				ObjRef xArchRef = xarchbulk.getXArch(typeLinkRef);
				ObjRef typeRef = xarchbulk.resolveHref(xArchRef, href);
				if(typeRef != null){
					ArchTypesTreePlugin attp = archStructureTreePlugin.getArchTypesTreePlugin();
					if(attp != null){
						ArchTypesTreeNode typeTreeNode = attp.getArchTypesTreeNode(typeRef);
						if(typeTreeNode != null){
							BNAModel typeBNAModel = typeTreeNode.getBNAModel();
							if(typeBNAModel != null){
								Thing[] allTypeThings = typeBNAModel.getAllThings();
								for(int i = 0; i < allTypeThings.length; i++){
									if(allTypeThings[i] instanceof BrickTypeThing){
										BrickTypeThing btt = (BrickTypeThing)allTypeThings[i];
										Color c = btt.getColor();
										return c;
									}
								}
							}
						}
						//If we get here we may not have inited the type, maybe we can
						//ask the rendering hints
						String typeID = XadlUtils.getID(xarchbulk, typeRef);
						if(typeID != null){
							//Create a dummy thing to apply the RHes to
							Color c = null;
							if(ct instanceof ComponentThing){
								ComponentTypeThing btt = new ComponentTypeThing();
								btt.setColor(attp.getDefaultComponentTypeColor());
								renderingHints.applyRenderingHints(typeID, btt);
								c = btt.getColor();
							}
							else if(ct instanceof ConnectorThing){
								ConnectorTypeThing btt = new ConnectorTypeThing();
								btt.setColor(attp.getDefaultConnectorTypeColor());
								renderingHints.applyRenderingHints(typeID, btt);
								c = btt.getColor();
							}
							if(c != null){
								return c;
							}
						}
					}
					//If we get to this point, the component probably has
					//a type, but the type has no color.  Return the
					//default color.
					if(ct instanceof ComponentThing){
						return attp.getDefaultComponentTypeColor();
					}
					else if(ct instanceof ConnectorThing){
						return attp.getDefaultConnectorTypeColor();
					}
				}
			}
		}
		return null;
	}
	
	private void updateSubArchitecture(ObjRef brickRef, BrickThing ct){
		ObjRef typeLinkRef = (ObjRef)xarchbulk.get(brickRef, "type");
		//System.out.println("found type");
		boolean foundSubArchitecture = false;
		if(typeLinkRef != null){
			String href = XadlUtils.getHref(xarchbulk, typeLinkRef);
			if(href != null){
				//System.out.println("found href");
				ObjRef xArchRef = xarchbulk.getXArch(typeLinkRef);
				ObjRef typeRef = xarchbulk.resolveHref(xArchRef, href);
				if(typeRef != null){
					boolean isAppropriateType = false;
					if(ct instanceof ComponentThing){
						if(xarch.isInstanceOf(typeRef, "edu.uci.isr.xarch.types.IComponentType")){
							isAppropriateType = true;
						}
					}
					if(ct instanceof ConnectorThing){
						if(xarch.isInstanceOf(typeRef, "edu.uci.isr.xarch.types.IConnectorType")){
							isAppropriateType = true;
						}
					}
					if(isAppropriateType){
						ObjRef subArchitectureRef = (ObjRef)xarchbulk.get(typeRef, "subArchitecture");
						if(subArchitectureRef != null){
							//System.out.println("found subarch");
							//OK, it should have a subarchitecture.  Let's get the BNAModel
							//for the type.
							ArchTypesTreePlugin attp = archStructureTreePlugin.getArchTypesTreePlugin();
							if(attp != null){
								ArchTypesTreeNode attn = attp.getArchTypesTreeNode(typeRef);
								if(attn != null){
									BNAModel typeBNAModel = attn.getBNAModel();
									ThingIDMap typeThingIDMap = attn.getThingIDMap();
									if(typeBNAModel == null){
										ScrollableBNAComponent c = attp.createTypeBNAComponent(attn);
										attp.destroyTypeBNAComponent(c.getBNAComponent());
										typeBNAModel = attn.getBNAModel();
										typeThingIDMap = attn.getThingIDMap();
									}
									if((typeBNAModel != null) && (typeThingIDMap != null)){
										//OK, we have the BNA model for the type and its
										//ThingIDMap.  Let's get the BrickTypeThing.
										String ttID = typeThingIDMap.getThingID(typeRef);
										if(ttID != null){
											Thing tt = typeBNAModel.getThing(ttID);
											if((tt != null) && (tt instanceof BrickTypeThing)){
												BrickTypeThing btt = (BrickTypeThing)tt;
												Thumbnail bttThumbnail = btt.getThumbnail();
												if(bttThumbnail != null){
													BNAModel thumbnailInternalModel = bttThumbnail.getModel();
													if(thumbnailInternalModel != null){
														Thumbnail structureThumbnail = new Thumbnail(thumbnailInternalModel);
														ct.setThumbnail(structureThumbnail);
														//System.out.println("Updating IIM Mappings");
														updateInterfaceInterfaceMappings(xArchRef, brickRef, typeRef, subArchitectureRef, ct);
														foundSubArchitecture = true;
													}
												}
											}
										}
									}
								}
							}
						}						
					}
				}
			}
		}
		if(!foundSubArchitecture){
			ArrayList doomedIimThings = new ArrayList();
			synchronized(structureBNAModel.getLock()){
				String thingID = ct.getID();
				for(Iterator it = structureBNAModel.getThingIterator(); it.hasNext(); ){
					Thing t = (Thing)it.next();
					if(t instanceof InterfaceInterfaceMappingThing){
						//System.out.println("dooming: " + t);
						InterfaceInterfaceMappingThing iimThing = (InterfaceInterfaceMappingThing)t;
						if(iimThing.getBrickThingID().equals(thingID)){
							doomedIimThings.add(t);
						}
					}
				}
			}
			for(Iterator it = doomedIimThings.iterator(); it.hasNext(); ){
				Thing t = (Thing)it.next();
				structureBNAModel.removeThing(t);
			}
			
			ct.setThumbnail(null);
		}
	}
	
	private void updateInterfaces(ObjRef brickRef, BrickThing ct){
		ObjRef[] interfaceRefs = xarchbulk.getAll(brickRef, "Interface");
		
		//Tag the existing interfaces for destruction
		//(they may be saved if we find a corresponding one
		//while iterating).
		ArrayList doomedInterfaceThings = new ArrayList();
		synchronized(structureBNAModel.getLock()){
			String thingID = ct.getID();
			for(Iterator it = structureBNAModel.getThingIterator(); it.hasNext(); ){
				Thing t = (Thing)it.next();
				if(t instanceof InterfaceThing){
					InterfaceThing ifaceThing = (InterfaceThing)t;
					String targetThingID = ifaceThing.getTargetThingID();
					if((targetThingID != null) && (targetThingID.equals(thingID))){
						doomedInterfaceThings.add(ifaceThing);
					}
				}
			}
		}
		
		//Iterate through the existing interfaces and make (or update)
		//corresponding things
		for(int j = 0; j < interfaceRefs.length; j++){
			InterfaceThing it;
			String existingInterfaceThingID = thingIDMap.getThingID(interfaceRefs[j]);
			if(existingInterfaceThingID != null){
				try{
					it = (InterfaceThing)structureBNAModel.getThing(existingInterfaceThingID);
					//Remove it from the list of doomed interface things,
					//so we don't end up removing it later.
					doomedInterfaceThings.remove(it);
				}
				catch(ClassCastException cce){
					System.err.println("Warning; ID/type mismatch.");
					cce.printStackTrace();
					return;
				}
			}
			else{
				it = new InterfaceThing();
			}
			
			String iID = edu.uci.ics.xadlutils.XadlUtils.getID(xarchbulk, interfaceRefs[j]);
			if(iID == null){
				continue;
			}
			it.setXArchID(iID);
			thingIDMap.mapRefToID(interfaceRefs[j], it.getID());
			it.setTargetThingID(ct.getID());
			
			String iDescription = edu.uci.ics.xadlutils.XadlUtils.getDescription(xarchbulk, interfaceRefs[j]);
			if(iDescription == null) iDescription = "";
			it.setToolTip(iDescription);
			
			String iDirection = edu.uci.ics.xadlutils.XadlUtils.getDirection(xarchbulk, interfaceRefs[j]);
			if(iDirection == null){
				it.setFlow(InterfaceThing.FLOW_NONE);
			}
			else if(iDirection.equals("inout")){
				it.setFlow(InterfaceThing.FLOW_INOUT);
			}
			else if(iDirection.equals("in")){
				it.setFlow(InterfaceThing.FLOW_IN);
			}
			else if(iDirection.equals("out")){
				it.setFlow(InterfaceThing.FLOW_OUT);
			}
			else{
				it.setFlow(InterfaceThing.FLOW_NONE);
			}
			
			if(existingInterfaceThingID == null){
				structureBNAModel.addThing(it, ct);
				//This waits for the model to process the add
				//event before we move the interface because
				//the move code expects one of the logics to process
				//the interface first.
				if(structureBNAModel instanceof DefaultBNAModel){
					((DefaultBNAModel)structureBNAModel).waitForProcessing();
				}
				it.setColor(Color.WHITE);
				
				//Do some smart stuff
				String lcdesc = iDescription.toLowerCase();
				int ctx = ct.getX();
				int cty = ct.getY();
				
				it.setX(ctx - (it.getWidth() / 2));
				it.setY(cty - (it.getHeight() / 2));
				
				if(lcdesc.indexOf("top") != -1){
					it.moveRelative(ct.getWidth() / 2, 0);
					it.setOrientation(EndpointThing.ORIENTATION_N);
				}
				else if(lcdesc.indexOf("bottom") != -1){
					it.moveRelative(ct.getWidth() / 2, ct.getHeight());
					it.setOrientation(EndpointThing.ORIENTATION_S);
				}
				else if(lcdesc.indexOf("left") != -1){
					it.moveRelative(0, ct.getHeight());
					it.setOrientation(EndpointThing.ORIENTATION_W);
				}
				else if(lcdesc.indexOf("right") != -1){
					it.moveRelative(ct.getWidth(), ct.getHeight() / 2);
					it.setOrientation(EndpointThing.ORIENTATION_E);
				}
				
				renderingHints.applyRenderingHints(iID, it);
			}
		}
		for(Iterator it = doomedInterfaceThings.iterator(); it.hasNext(); ){
			Thing t = (Thing)it.next();
			structureBNAModel.removeThing(t);
		}
	}

	private void updateInterfaceInterfaceMappings(ObjRef xArchRef, ObjRef brickRef, 
	ObjRef typeRef, ObjRef subArchitectureRef, BrickThing ct){
		
		//Tag the existing sims for destruction
		//(they may be saved if we find a corresponding one
		//while iterating).
		ArrayList doomedIimThings = new ArrayList();
		synchronized(structureBNAModel.getLock()){
			String thingID = ct.getID();
			for(Iterator it = structureBNAModel.getThingIterator(); it.hasNext(); ){
				Thing t = (Thing)it.next();
				if(t instanceof InterfaceInterfaceMappingThing){
					InterfaceInterfaceMappingThing iimThing = (InterfaceInterfaceMappingThing)t;
					if(iimThing.getBrickThingID().equals(ct.getID())){
						doomedIimThings.add(t);
					}
				}
			}
		}
		
		//if(xarch.isAttached(brickRef)){
			if(subArchitectureRef != null){
				//System.out.println("got SubArchitecture");
				ObjRef[] interfaceRefs = xarchbulk.getAll(brickRef, "interface");
			
				for(int j = 0; j < interfaceRefs.length; j++){
					//System.out.println("checking interface " + j);
					ObjRef interfaceRef = interfaceRefs[j];
					ObjRef signatureLinkRef = (ObjRef)xarchbulk.get(interfaceRef, "signature");
					if(signatureLinkRef != null){
						//System.out.println("got sig link ref");
						String signatureHref = XadlUtils.getHref(xarchbulk, signatureLinkRef);
						if(signatureHref != null){
							//System.out.println("got sig href");
							ObjRef signatureRef = xarchbulk.resolveHref(xArchRef, signatureHref);
							if(signatureRef != null){
								//System.out.println("got sig");
								//OK, we've found the signature that's linked; let's see
								//if there are any signature-interface mappings associated with it.
								ObjRef[] associatedSimRefs = XadlUtils.getSignatureInterfaceMappings(xarchbulk, typeRef, signatureRef);
								for(int k = 0; k < associatedSimRefs.length; k++){
									//System.out.println("checking associated SIM ref " + k);
									//Each SIM is going to be responsible for an i-i mapping
									//between interfaceRef (outer) and its linked interface (inner).
									ObjRef innerInterfaceLinkRef = (ObjRef)xarchbulk.get(associatedSimRefs[k], "innerInterface");
									if(innerInterfaceLinkRef != null){
										//System.out.println("got inner interface link ref");
										String innerInterfaceLinkHref = XadlUtils.getHref(xarchbulk, innerInterfaceLinkRef);
										if(innerInterfaceLinkHref != null){
											//System.out.println("got inner interface link href");
											ObjRef innerInterfaceRef = xarchbulk.resolveHref(xArchRef, innerInterfaceLinkHref);
											if(innerInterfaceRef != null){
												//System.out.println("got inner interface ref");
												//Okay, we have the outer interface ref and the inner interface ref now
												//Let's get the IDs.
												String interfaceXArchID = XadlUtils.getID(xarchbulk, interfaceRef);
												String innerInterfaceXArchID = XadlUtils.getID(xarchbulk, innerInterfaceRef);
												if((interfaceXArchID != null) && (innerInterfaceXArchID != null)){
													//System.out.println("got both ids");
													//OK we have both IDs.  
													//First, let's get the ThingIDs that map to these.
													String interfaceThingID = thingIDMap.getThingID(interfaceRef);
													String innerInterfaceThingID = null;
													Thumbnail thumbnail = ct.getThumbnail();
													if(thumbnail != null){
														//System.out.println("got thumb");
														BNAModel innerModel = thumbnail.getModel();
														if(innerModel != null){
															//System.out.println("got innerModel");
															InterfaceThing innerInterfaceThing = getInterfaceThingByXArchID(innerModel, innerInterfaceXArchID);
															innerInterfaceThingID = innerInterfaceThing.getID();
														}
													}
													if((interfaceThingID != null) && (innerInterfaceThingID != null)){
														//System.out.println("got both thing ids");
														//Let's see if some existing IID thing
														//already links these two (that we haven't already undoomed)
														InterfaceInterfaceMappingThing iimt = null;
														for(Iterator it = doomedIimThings.iterator(); it.hasNext(); ){
															InterfaceInterfaceMappingThing possibleMatch = (InterfaceInterfaceMappingThing)it.next();
															String pmoitID = possibleMatch.getOuterInterfaceThingID();
															String pmiitID = possibleMatch.getInnerInterfaceThingID();
															if((pmoitID != null) && (pmiitID != null)){
																if(pmoitID.equals(interfaceThingID) && pmiitID.equals(innerInterfaceThingID)){
																	 iimt = possibleMatch;
																	 break;
																}
															}
														}
														if(iimt != null){
															//System.out.println("iimt found");
															doomedIimThings.remove(iimt);
														}
														else{
															//System.out.println("iimt not found");
															iimt = new InterfaceInterfaceMappingThing();
															iimt.setBrickThingID(ct.getID());
															iimt.setOuterInterfaceThingID(interfaceThingID);
															iimt.setInnerInterfaceThingID(innerInterfaceThingID);
															//System.out.println("adding new one");
															structureBNAModel.addThing(iimt, ct);
															structureBNAModel.sendToBack(iimt);
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		//}
		for(Iterator it = doomedIimThings.iterator(); it.hasNext(); ){
			Thing t = (Thing)it.next();
			//System.out.println("removing!!! " + t);
			structureBNAModel.removeThing(t);
		}
		
	}

	private static InterfaceThing getInterfaceThingByXArchID(BNAModel m, String xArchID){
		Thing[] allThings = m.getAllThings();
		for(int i = 0; i < allThings.length; i++){
			if(allThings[i] instanceof InterfaceThing){
				InterfaceThing it = (InterfaceThing)allThings[i];
				String itXArchID = it.getXArchID();
				if((itXArchID != null) && (itXArchID.equals(xArchID))){
					return it;
				}
			}
		}
		return null;
	}
	
	public void removeComponent(ObjRef componentRef){
		//do NOT change to xarchbulk
		ObjRef structureRef = xarch.getParent(componentRef);
		if(structureRef != null){
			if(!structureRef.equals(mainStructureRef)){
				return;
			}
		}

		String existingThingID = thingIDMap.getThingID(componentRef);
		if(existingThingID == null){
			//It's already gone. (?)
			return;
		}
		ComponentThing ct = (ComponentThing)structureBNAModel.getThing(existingThingID);
		fireComponentRemoving(componentRef, ct);
		structureBNAModel.removeThingAndChildren(ct);
		fireComponentRemoved(componentRef, ct);
	}
	
	public void removeConnector(ObjRef connectorRef){
		//do NOT change to xarchbulk
		ObjRef structureRef = xarch.getParent(connectorRef);
		if(structureRef != null){
			if(!structureRef.equals(mainStructureRef)){
				return;
			}
		}

		String existingThingID = thingIDMap.getThingID(connectorRef);
		if(existingThingID == null){
			//It's already gone. (?)
			return;
		}
		ConnectorThing ct = (ConnectorThing)structureBNAModel.getThing(existingThingID);
		fireConnectorRemoving(connectorRef, ct);
		structureBNAModel.removeThingAndChildren(ct);
		fireConnectorRemoved(connectorRef, ct);
	}

	public static BrickMappingLogic getBrickMappingLogic(BNAComponent bnaComponent){
		MappingLogic[] mls = AbstractArchipelagoTreePlugin.getAllMappingLogics(bnaComponent);
		for(int i = 0; i < mls.length; i++){
			if(mls[i] instanceof BrickMappingLogic){
				return (BrickMappingLogic)mls[i];
			}
		}
		return null;
	}

}

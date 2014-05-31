package archstudio.comp.archipelago.types;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.thumbnail.*;
import edu.uci.ics.widgets.WidgetUtils;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.*;

import archstudio.comp.archipelago.*;
import archstudio.comp.xarchtrans.*;

import java.awt.*;
import java.util.*;

public class TypeMappingLogic extends AbstractMappingLogic{
	
	//public static final Color DEFAULT_COMPONENT_TYPE_COLOR = edu.uci.ics.widgets.Colors.PALE_WEAK_CYAN;
	//public static final Color DEFAULT_CONNECTOR_TYPE_COLOR = edu.uci.ics.widgets.Colors.PALE_WEAK_MAGENTA;
	
	public static final Color DEFAULT_COMPONENT_TYPE_COLOR = new Color(0x6871BF);
	public static final Color DEFAULT_CONNECTOR_TYPE_COLOR = new Color(0xBF9F60);
	
	public static final int COMPONENT_TYPE = 100;
	public static final int CONNECTOR_TYPE = 200;
	public static final int INTERFACE_TYPE = 300;
	
	protected ThingIDMap thingIDMap;

	protected ArchTypesTreePlugin archTypesTreePlugin;	
	protected BNAModel typeBNAModel;
	protected ObjRef mainTypeRef;
	
	protected archstudio.comp.archipelago.RenderingHints renderingHints;
	
	protected NoThing typeParent;
	
	protected Vector typeMappingLogicListeners = new Vector();
	
	public TypeMappingLogic(ArchTypesTreePlugin attp, ObjRef mainTypeRef,
		BNAModel typeBNAModel, XArchFlatTransactionsInterface xarch, 
		ThingIDMap thingIDMap, archstudio.comp.archipelago.RenderingHints renderingHints){
		super(new BNAModel[]{typeBNAModel}, xarch);

		this.archTypesTreePlugin = attp;

		this.typeBNAModel = typeBNAModel;
		this.mainTypeRef = mainTypeRef;
		this.thingIDMap = thingIDMap;
		this.renderingHints = renderingHints;
		
		typeParent = new NoThing("$$TypeParent");
		typeBNAModel.addThing(typeParent);
	}
	
	public void addTypeMappingLogicListener(TypeMappingLogicListener l){
		typeMappingLogicListeners.addElement(l);
	}
	
	public void removeTypeMappingLogicListener(TypeMappingLogicListener l){
		typeMappingLogicListeners.removeElement(l);
	}
	
	protected void fireComponentTypeUpdating(ObjRef typeRef, ComponentTypeThing tt){
		synchronized(typeMappingLogicListeners){
			for(Iterator it = typeMappingLogicListeners.iterator(); it.hasNext(); ){
				((TypeMappingLogicListener)it.next()).componentTypeUpdating(typeRef, tt);
			}
		}
	}
	
	protected void fireComponentTypeUpdated(ObjRef typeRef, ComponentTypeThing tt){
		synchronized(typeMappingLogicListeners){
			for(Iterator it = typeMappingLogicListeners.iterator(); it.hasNext(); ){
				((TypeMappingLogicListener)it.next()).componentTypeUpdated(typeRef, tt);
			}
		}
	}
	
	protected void fireComponentTypeRemoving(ObjRef typeRef, ComponentTypeThing tt){
		synchronized(typeMappingLogicListeners){
			for(Iterator it = typeMappingLogicListeners.iterator(); it.hasNext(); ){
				((TypeMappingLogicListener)it.next()).componentTypeRemoving(typeRef, tt);
			}
		}
	}
	
	protected void fireComponentTypeRemoved(ObjRef typeRef, ComponentTypeThing tt){
		synchronized(typeMappingLogicListeners){
			for(Iterator it = typeMappingLogicListeners.iterator(); it.hasNext(); ){
				((TypeMappingLogicListener)it.next()).componentTypeRemoved(typeRef, tt);
			}
		}
	}

	protected void fireConnectorTypeUpdating(ObjRef typeRef, ConnectorTypeThing tt){
		synchronized(typeMappingLogicListeners){
			for(Iterator it = typeMappingLogicListeners.iterator(); it.hasNext(); ){
				((TypeMappingLogicListener)it.next()).connectorTypeUpdating(typeRef, tt);
			}
		}
	}
	
	protected void fireConnectorTypeUpdated(ObjRef typeRef, ConnectorTypeThing tt){
		synchronized(typeMappingLogicListeners){
			for(Iterator it = typeMappingLogicListeners.iterator(); it.hasNext(); ){
				((TypeMappingLogicListener)it.next()).connectorTypeUpdated(typeRef, tt);
			}
		}
	}
	
	protected void fireConnectorTypeRemoving(ObjRef typeRef, ConnectorTypeThing tt){
		synchronized(typeMappingLogicListeners){
			for(Iterator it = typeMappingLogicListeners.iterator(); it.hasNext(); ){
				((TypeMappingLogicListener)it.next()).connectorTypeRemoving(typeRef, tt);
			}
		}
	}
	
	protected void fireConnectorTypeRemoved(ObjRef typeRef, ConnectorTypeThing tt){
		synchronized(typeMappingLogicListeners){
			for(Iterator it = typeMappingLogicListeners.iterator(); it.hasNext(); ){
				((TypeMappingLogicListener)it.next()).connectorTypeRemoved(typeRef, tt);
			}
		}
	}
	
	protected void fireInterfaceTypeUpdating(ObjRef typeRef, InterfaceTypeThing tt){
		synchronized(typeMappingLogicListeners){
			for(Iterator it = typeMappingLogicListeners.iterator(); it.hasNext(); ){
				((TypeMappingLogicListener)it.next()).interfaceTypeUpdating(typeRef, tt);
			}
		}
	}
	
	protected void fireInterfaceTypeUpdated(ObjRef typeRef, InterfaceTypeThing tt){
		synchronized(typeMappingLogicListeners){
			for(Iterator it = typeMappingLogicListeners.iterator(); it.hasNext(); ){
				((TypeMappingLogicListener)it.next()).interfaceTypeUpdated(typeRef, tt);
			}
		}
	}
	
	protected void fireInterfaceTypeRemoving(ObjRef typeRef, InterfaceTypeThing tt){
		synchronized(typeMappingLogicListeners){
			for(Iterator it = typeMappingLogicListeners.iterator(); it.hasNext(); ){
				((TypeMappingLogicListener)it.next()).interfaceTypeRemoving(typeRef, tt);
			}
		}
	}
	
	protected void fireInterfaceTypeRemoved(ObjRef typeRef, InterfaceTypeThing tt){
		synchronized(typeMappingLogicListeners){
			for(Iterator it = typeMappingLogicListeners.iterator(); it.hasNext(); ){
				((TypeMappingLogicListener)it.next()).interfaceTypeRemoved(typeRef, tt);
			}
		}
	}
	
	public BNAModel getBNAModel(){
		return typeBNAModel;
	}
	
	public void handleXArchFlatEvent(XArchFlatEvent evt){
		XArchPath sourcePath = evt.getSourcePath();
		String sourcePathString = null;
		if(sourcePath != null) sourcePathString = sourcePath.toTagsOnlyString();
		
		//System.out.println("source path: " + sourcePathString);
		
		XArchPath targetPath = evt.getTargetPath();
		String targetPathString = null;
		if(targetPath != null) targetPathString = targetPath.toTagsOnlyString();

		//System.out.println("target path: " + targetPathString);

		if((evt.getEventType() == XArchFlatEvent.CLEAR_EVENT) || (evt.getEventType() == XArchFlatEvent.REMOVE_EVENT)){
			if((sourcePathString != null) && (sourcePathString.equals("xArch/archTypes"))){
				if((targetPathString != null) && (targetPathString.equals("componentType"))){
					removeType((ObjRef)evt.getTarget(), COMPONENT_TYPE);
				}
				else if((targetPathString != null) && (targetPathString.equals("connectorType"))){
					removeType((ObjRef)evt.getTarget(), CONNECTOR_TYPE);
				}
				else if((targetPathString != null) && (targetPathString.equals("interfaceType"))){
					removeType((ObjRef)evt.getTarget(), INTERFACE_TYPE);
				}
			}
			else if((sourcePathString != null) && (sourcePathString.startsWith("xArch/archTypes/componentType"))){
				doUpdateComponentType(evt);
			}
			else if((sourcePathString != null) && (sourcePathString.startsWith("xArch/archTypes/connectorType"))){
				doUpdateConnectorType(evt);
			}
			else if((sourcePathString != null) && (sourcePathString.startsWith("xArch/archTypes/interfaceType"))){
				doUpdateInterfaceType(evt);
			}
		}
		
		if(targetPathString == null){
			targetPathString = sourcePathString;
		}

		if((targetPathString != null) && (targetPathString.startsWith("xArch/archTypes/componentType"))){
			doUpdateComponentType(evt);
		}
		else if((targetPathString != null) && (targetPathString.startsWith("xArch/archTypes/connectorType"))){
			doUpdateConnectorType(evt);
		}
		else if((targetPathString != null) && (targetPathString.startsWith("xArch/archTypes/interfaceType"))){
			doUpdateInterfaceType(evt);
		}
	}
	
	private void doUpdateComponentType(XArchFlatEvent evt){
		//It's a component event
		ObjRef src = evt.getSource();
		ObjRef typeRef = null;
		if(src != null){
			ObjRef[] ancestors = xarch.getAllAncestors(src);
			//System.out.println("ancestors.length = " + ancestors.length);
			//The component will be ancestor #
			if(ancestors.length >= 3){
				typeRef = ancestors[ancestors.length - 3];
				updateComponentType(typeRef);
			}
			else{
				//Maybe the target was the component type?
				Object targetObj = evt.getTarget();
				if(targetObj instanceof ObjRef){
					ObjRef target = (ObjRef)targetObj;
					if(target != null){
						if(xarch.isInstanceOf(target, "edu.uci.isr.xarch.types.IComponentType")){
							typeRef = target;
						}
						else{
							ancestors = xarch.getAllAncestors(target);
							if(ancestors.length >= 3){
								typeRef = ancestors[ancestors.length - 3];
							}
						}
						if(typeRef != null){
							updateComponentType(typeRef);
						}
					}
				}
			}
		}
	}
	
	private void doUpdateConnectorType(XArchFlatEvent evt){
		//It's a connector event
		ObjRef src = evt.getSource();
		ObjRef typeRef = null;
		if(src != null){
			ObjRef[] ancestors = xarch.getAllAncestors(src);
			//System.out.println("ancestors.length = " + ancestors.length);
			//The component will be ancestor #
			if(ancestors.length >= 3){
				typeRef = ancestors[ancestors.length - 3];
				updateConnectorType(typeRef);
			}
			else{
				//Maybe the target was the component type?
				Object targetObj = evt.getTarget();
				if(targetObj instanceof ObjRef){
					ObjRef target = (ObjRef)targetObj;
					if(target != null){
						if(xarch.isInstanceOf(target, "edu.uci.isr.xarch.types.IConnectorType")){
							typeRef = target;
						}
						else{
							ancestors = xarch.getAllAncestors(target);
							if(ancestors.length >= 3){
								typeRef = ancestors[ancestors.length - 3];
							}
						}
						if(typeRef != null){
							updateConnectorType(typeRef);
						}
					}
				}
			}
		}
	}
	
	private void doUpdateInterfaceType(XArchFlatEvent evt){
		//It's an interface event
		ObjRef src = evt.getSource();
		ObjRef typeRef = null;
		if(src != null){
			ObjRef[] ancestors = xarch.getAllAncestors(src);
			//System.out.println("ancestors.length = " + ancestors.length);
			//The component will be ancestor #
			if(ancestors.length >= 3){
				typeRef = ancestors[ancestors.length - 3];
				updateInterfaceType(typeRef);
			}
			else{
				//Maybe the target was the component type?
				Object targetObj = evt.getTarget();
				if(targetObj instanceof ObjRef){
					ObjRef target = (ObjRef)targetObj;
					if(target != null){
						if(xarch.isInstanceOf(target, "edu.uci.isr.xarch.types.IInterfaceType")){
							typeRef = target;
						}
						else{
							ancestors = xarch.getAllAncestors(target);
							if(ancestors.length >= 3){
								typeRef = ancestors[ancestors.length - 3];
							}
						}
						if(typeRef != null){
							updateInterfaceType(typeRef);
						}
					}
				}
			}
		}
	}
	
	public void handleXArchFileEvent(XArchFileEvent evt){
	}
	
	public void removeType(ObjRef typeRef, int typeOfType){
		if(!mainTypeRef.equals(typeRef)){
			return;
		}

		String existingThingID = thingIDMap.getThingID(typeRef);
		if(existingThingID == null){
			//It's already gone. (?)
			return;
		}
		
		Thing existingThing = typeBNAModel.getThing(existingThingID);
		if(existingThing != null){
			//We're viewing it.
			switch(typeOfType){
				case COMPONENT_TYPE:
					fireComponentTypeRemoving(typeRef, (ComponentTypeThing)existingThing);
					break;
				case CONNECTOR_TYPE:
					fireConnectorTypeRemoving(typeRef, (ConnectorTypeThing)existingThing);
					break;
				case INTERFACE_TYPE:
					fireInterfaceTypeRemoving(typeRef, (InterfaceTypeThing)existingThing);
					break;
			}
			typeBNAModel.removeThingAndChildren(existingThing);
			switch(typeOfType){
				case COMPONENT_TYPE:
					fireComponentTypeRemoved(typeRef, (ComponentTypeThing)existingThing);
					break;
				case CONNECTOR_TYPE:
					fireConnectorTypeRemoved(typeRef, (ConnectorTypeThing)existingThing);
					break;
				case INTERFACE_TYPE:
					fireInterfaceTypeRemoved(typeRef, (InterfaceTypeThing)existingThing);
					break;
			}
		}
	}
	
	public void updateType(ObjRef typeRef){
		if(!mainTypeRef.equals(typeRef)){
			return;
		}
		
		if(xarch.isInstanceOf(typeRef, "edu.uci.isr.xarch.types.IComponentType")){
			updateComponentType(typeRef);
		}
		else if(xarch.isInstanceOf(typeRef, "edu.uci.isr.xarch.types.IConnectorType")){
			updateConnectorType(typeRef);
		}
		else if(xarch.isInstanceOf(typeRef, "edu.uci.isr.xarch.types.IInterfaceType")){
			updateInterfaceType(typeRef);
		}
	}
	
	public void updateComponentType(ObjRef componentTypeRef){
		ComponentTypeThing ctt = null;
		
		String existingThingID = thingIDMap.getThingID(componentTypeRef);
		if(existingThingID != null){
			try{
				ctt = (ComponentTypeThing)typeBNAModel.getThing(existingThingID);
			}
			catch(ClassCastException cce){
				System.err.println("Warning; ID/type mismatch.");
				cce.printStackTrace();
				return;
			}
		}
		if(ctt == null){
			if(componentTypeRef.equals(mainTypeRef)){
				ctt = new ComponentTypeThing();
			}
			else{
				//This occurs if a type gets updated that we're not currently viewing.
				return;
			}
		}
		fireComponentTypeUpdating(componentTypeRef, ctt);
		updateBrickType(componentTypeRef, ctt, existingThingID);
		fireComponentTypeUpdated(componentTypeRef, ctt);
	}

	public void updateConnectorType(ObjRef connectorTypeRef){
		ConnectorTypeThing ctt = null;
		
		String existingThingID = thingIDMap.getThingID(connectorTypeRef);
		if(existingThingID != null){
			try{
				ctt = (ConnectorTypeThing)typeBNAModel.getThing(existingThingID);
			}
			catch(ClassCastException cce){
				System.err.println("Warning; ID/type mismatch.");
				cce.printStackTrace();
				return;
			}
		}
		if(ctt == null){
			if(connectorTypeRef.equals(mainTypeRef)){
				ctt = new ConnectorTypeThing();
			}
			else{
				//This occurs if a type gets updated that we're not currently viewing.
				return;
			}
		}
		fireConnectorTypeUpdating(connectorTypeRef, ctt);
		updateBrickType(connectorTypeRef, ctt, existingThingID);
		fireConnectorTypeUpdated(connectorTypeRef, ctt);
	}
	
	private void updateBrickType(ObjRef brickTypeRef, BrickTypeThing ctt, String existingThingID){
		String cttID = edu.uci.ics.xadlutils.XadlUtils.getID(xarch, brickTypeRef);
		ctt.setXArchID(cttID);
		thingIDMap.mapRefToID(brickTypeRef, ctt.getID());
		
		String label = "(No Description)";
		String desc = edu.uci.ics.xadlutils.XadlUtils.getDescription(xarch, brickTypeRef);
		if(desc != null) label = desc;
		ctt.setLabel(label);
		
		if(existingThingID == null){
			//If it hasn't been placed already...
			//Set a default bounding box
			
			ctt.setBoundingBox((DefaultCoordinateMapper.DEFAULT_WORLD_WIDTH / 2) + 50,
				(DefaultCoordinateMapper.DEFAULT_WORLD_HEIGHT / 2) + 50,	200, 150);
			
			//Set a default color and border
			if(ctt instanceof ComponentTypeThing){
				ctt.setDoubleBorder(true);
				Color c = archTypesTreePlugin.getDefaultComponentTypeColor();
				ctt.setColor(c);
			}
			else if(ctt instanceof ConnectorTypeThing){
				Color c = archTypesTreePlugin.getDefaultConnectorTypeColor();
				ctt.setColor(c);
			}
			else{
				ctt.setColor(Color.WHITE);
			}
			
			renderingHints.applyRenderingHints(cttID, ctt);

			Color c = ctt.getColor();
			if(WidgetUtils.isDark(c)){
				ctt.setTextColor(Color.WHITE);
			}
			else{
				ctt.setTextColor(Color.BLACK);
			}
			
			typeBNAModel.addThing(ctt, typeParent);
		}
		
		updateSignatures(brickTypeRef, ctt);
		updateSubArchitecture(brickTypeRef, ctt);
	}

	private void updateSignatures(ObjRef brickRef, BrickTypeThing ctt){
		ObjRef[] signatureRefs = xarch.getAll(brickRef, "Signature");

		//Tag the existing signatures for destruction
		//(they may be saved if we find a corresponding one
		//while iterating).
		ArrayList doomedSignatureThings = new ArrayList();
		synchronized(typeBNAModel.getLock()){
			String thingID = ctt.getID();
			for(Iterator it = typeBNAModel.getThingIterator(); it.hasNext(); ){
				Thing t = (Thing)it.next();
				if(t instanceof SignatureThing){
					SignatureThing sigThing = (SignatureThing)t;
					String targetThingID = sigThing.getTargetThingID();
					if((targetThingID != null) && (targetThingID.equals(thingID))){
						doomedSignatureThings.add(sigThing);
					}
				}
			}
		}

		for(int j = 0; j < signatureRefs.length; j++){
			SignatureThing st;
			String existingSignatureThingID = thingIDMap.getThingID(signatureRefs[j]);
			
			if(existingSignatureThingID != null){
				try{
					st = (SignatureThing)typeBNAModel.getThing(existingSignatureThingID);
					//Remove it from the list of doomed signature things,
					//so we don't end up removing it later.
					doomedSignatureThings.remove(st);
				}
				catch(ClassCastException cce){
					System.err.println("Warning; ID/type mismatch.");
					cce.printStackTrace();
					return;
				}
			}
			else{
				st = new SignatureThing();
			}
			
			String sID = edu.uci.ics.xadlutils.XadlUtils.getID(xarch, signatureRefs[j]);
			if(sID == null){
				continue;
			}
			st.setXArchID(sID);
			thingIDMap.mapRefToID(signatureRefs[j], st.getID());
			st.setTargetThingID(ctt.getID());
			
			String iDescription = edu.uci.ics.xadlutils.XadlUtils.getDescription(xarch, signatureRefs[j]);
			if(iDescription == null) iDescription = "";
			st.setToolTip(iDescription);
			
			String iDirection = edu.uci.ics.xadlutils.XadlUtils.getDirection(xarch, signatureRefs[j]);
			if(iDirection == null){
				st.setFlow(InterfaceThing.FLOW_NONE);
			}
			else if(iDirection.equals("inout")){
				st.setFlow(InterfaceThing.FLOW_INOUT);
			}
			else if(iDirection.equals("in")){
				st.setFlow(InterfaceThing.FLOW_IN);
			}
			else if(iDirection.equals("out")){
				st.setFlow(InterfaceThing.FLOW_OUT);
			}
			else{
				st.setFlow(InterfaceThing.FLOW_NONE);
			}
			
			if(existingSignatureThingID == null){
				typeBNAModel.addThing(st, ctt);
				st.setColor(Color.WHITE);
				
				//Do some smart stuff
				String lcdesc = iDescription.toLowerCase();
				int cttx = ctt.getX();
				int ctty = ctt.getY();
				
				st.setX(cttx - (st.getWidth() / 2));
				st.setY(ctty - (st.getHeight() / 2));
				
				if(lcdesc.indexOf("top") != -1){
					st.moveRelative(ctt.getWidth() / 2, 0);
				}
				else if(lcdesc.indexOf("bottom") != -1){
					st.moveRelative(ctt.getWidth() / 2, ctt.getHeight());
				}
				else if(lcdesc.indexOf("left") != -1){
					st.moveRelative(0, ctt.getHeight());
				}
				else if(lcdesc.indexOf("right") != -1){
					st.moveRelative(ctt.getWidth(), ctt.getHeight() / 2);
				}
				
				renderingHints.applyRenderingHints(sID, st);
			}
		}

		for(Iterator it = doomedSignatureThings.iterator(); it.hasNext(); ){
			Thing t = (Thing)it.next();
			typeBNAModel.removeThing(t);
		}
		
	}


	private void updateSubArchitecture(ObjRef brickRef, BrickTypeThing ctt){
		ObjRef subArchitectureRef = (ObjRef)xarch.get(brickRef, "subArchitecture");
		boolean hasSubArchitecture = false;
		if(subArchitectureRef != null){
			//System.out.println("has subArchitectureRef");
			ObjRef subStructureLinkRef = (ObjRef)xarch.get(subArchitectureRef, "archStructure");
			if(subStructureLinkRef != null){
				//System.out.println("has subStructureLinkRef");
				String href = edu.uci.ics.xadlutils.XadlUtils.getHref(xarch, subStructureLinkRef);
				if(href != null){
					//System.out.println("has href");
					ObjRef xArchRef = xarch.getXArch(brickRef);
					ObjRef subStructureRef = xarch.resolveHref(xArchRef, href);
					if(subStructureRef != null){
						//System.out.println("has subStructureRef");
						ArchStructureTreePlugin astp = archTypesTreePlugin.getArchStructureTreePlugin();
						if(astp != null){
							//System.out.println("has astp");
							ArchStructureTreeNode astn = astp.getArchStructureTreeNode(subStructureRef);
							if(astn != null){
								//System.out.println("has astn");
								BNAModel subStructureBNAModel = astn.getBNAModel();
								if(subStructureBNAModel == null){
									//We haven't viewed it yet.
									ScrollableBNAComponent c = astp.createStructureBNAComponent(astn);
									astp.destroyStructureBNAComponent(c.getBNAComponent());
									subStructureBNAModel = astn.getBNAModel();
								}
								if(subStructureBNAModel != null){
									//System.out.println("has subStructureBNAModel");
									Thumbnail thumbnail = new Thumbnail(subStructureBNAModel);
									ctt.setThumbnail(thumbnail);
									ctt.setThumbnailInset(4);
									updateSignatureInterfaceMappings(xArchRef, brickRef, ctt, subArchitectureRef, astn, thumbnail);
									hasSubArchitecture = true;
								}
							}
						}
					}
				}
			}
		}
		if(!hasSubArchitecture){
			ArrayList doomedSimThings = new ArrayList();
			synchronized(typeBNAModel.getLock()){
				String thingID = ctt.getID();
				for(Iterator it = typeBNAModel.getThingIterator(); it.hasNext(); ){
					Thing t = (Thing)it.next();
					if(t instanceof SignatureInterfaceMappingThing){
						SignatureInterfaceMappingThing simThing = (SignatureInterfaceMappingThing)t;
						//System.out.println("dooming: " + t);
						doomedSimThings.add(t);
					}
				}
			}
			for(Iterator it = doomedSimThings.iterator(); it.hasNext(); ){
				Thing t = (Thing)it.next();
				typeBNAModel.removeThing(t);
			}
			
			ctt.setThumbnail(null);
		}
	}

	private void updateSignatureInterfaceMappings(ObjRef xArchRef, ObjRef brickRef, 
	BrickTypeThing ctt, ObjRef subArchitectureRef, ArchStructureTreeNode subStructureNode,
	Thumbnail thumbnail){
		
		ObjRef[] simRefs = xarch.getAll(subArchitectureRef, "signatureInterfaceMapping");

		//Tag the existing sims for destruction
		//(they may be saved if we find a corresponding one
		//while iterating).
		ArrayList doomedSimThings = new ArrayList();
		synchronized(typeBNAModel.getLock()){
			String thingID = ctt.getID();
			for(Iterator it = typeBNAModel.getThingIterator(); it.hasNext(); ){
				Thing t = (Thing)it.next();
				if(t instanceof SignatureInterfaceMappingThing){
					SignatureInterfaceMappingThing simThing = (SignatureInterfaceMappingThing)t;
					//System.out.println("dooming: " + t);
					doomedSimThings.add(t);
				}
			}
		}

		for(int j = 0; j < simRefs.length; j++){
			//System.out.println("checking simt " + j);
			
			SignatureInterfaceMappingThing simt;
			String existingSimThingID = thingIDMap.getThingID(simRefs[j]);
			
			if(existingSimThingID != null){
				//System.out.println("using old simt");
				try{
					simt = (SignatureInterfaceMappingThing)typeBNAModel.getThing(existingSimThingID);
					//Remove it from the list of doomed signature things,
					//so we don't end up removing it later.
					//System.out.println("undooming: " + simt);
					doomedSimThings.remove(simt);
				}
				catch(ClassCastException cce){
					System.err.println("Warning; ID/type mismatch.");
					cce.printStackTrace();
					return;
				}
			}
			else{
				//System.out.println("creating new simt");
				simt = new SignatureInterfaceMappingThing();
			}
			
			String simID = edu.uci.ics.xadlutils.XadlUtils.getID(xarch, simRefs[j]);
			if(simID == null){
				continue;
			}
			simt.setXArchID(simID);
			thingIDMap.mapRefToID(simRefs[j], simt.getID());
			
			String iDescription = edu.uci.ics.xadlutils.XadlUtils.getDescription(xarch, simRefs[j]);
			simt.setToolTipText(iDescription);
			
			simt.setBrickTypeThingID(ctt.getID());
			
			ObjRef signatureLinkRef = (ObjRef)xarch.get(simRefs[j], "outerSignature");
			ObjRef signatureRef = null;
			//System.out.println("signatureLinkRef = " + signatureLinkRef);
			if(signatureLinkRef != null){
				String signatureLinkHref = XadlUtils.getHref(xarch, signatureLinkRef);
				if(signatureLinkHref != null){
					signatureRef = xarch.resolveHref(xArchRef, signatureLinkHref);
					//System.out.println("signatureRef = " + signatureRef);
				}
			}
			
			ObjRef interfaceLinkRef = (ObjRef)xarch.get(simRefs[j], "innerInterface");
			ObjRef interfaceRef = null;
			//System.out.println("interfaceLinkRef = " + interfaceLinkRef);
			if(interfaceLinkRef != null){
				String interfaceLinkHref = XadlUtils.getHref(xarch, interfaceLinkRef);
				if(interfaceLinkHref != null){
					interfaceRef = xarch.resolveHref(xArchRef, interfaceLinkHref);
					//System.out.println("interfaceRef = " + interfaceRef);
				}
			}
			
			if((signatureRef == null) || (interfaceRef == null)){
				//System.out.println("dooming thing");
				doomedSimThings.add(simt);
				continue;
			}
			
			String signatureThingID = thingIDMap.getThingID(signatureRef);
			simt.setSignatureThingID(signatureThingID);
			
			//Get the interface thing ID from the substructure node's
			//ThingIDMap, NOT this one.
			String interfaceThingID = subStructureNode.getThingIDMap().getThingID(interfaceRef);
			simt.setInterfaceThingID(interfaceThingID);
			
			if(existingSimThingID == null){
				//System.out.println("adding thing");
				typeBNAModel.addThing(simt, ctt);
				typeBNAModel.sendToBack(simt);
				renderingHints.applyRenderingHints(simID, simt);
			}

			//OK, the IDs and second endpoint are set up, now a 
			//logic will have to see these changes and make sure that 
			//the first endpoint is in the right place.
		}

		for(Iterator it = doomedSimThings.iterator(); it.hasNext(); ){
			Thing t = (Thing)it.next();
			typeBNAModel.removeThing(t);
		}
		
	}
	
	private void updateInterfaceType(ObjRef interfaceTypeRef){
		InterfaceTypeThing itt = null;
		
		String existingThingID = thingIDMap.getThingID(interfaceTypeRef);
		if(existingThingID != null){
			try{
				itt = (InterfaceTypeThing)typeBNAModel.getThing(existingThingID);
			}
			catch(ClassCastException cce){
				System.err.println("Warning; ID/type mismatch.");
				cce.printStackTrace();
				return;
			}
		}
		if(itt == null){
			if(interfaceTypeRef.equals(mainTypeRef)){
				itt = new InterfaceTypeThing();
			}
			else{
				//This occurs if a type gets updated that we're not currently viewing.
				return;
			}
		}
		
		fireInterfaceTypeUpdating(interfaceTypeRef, itt);

		String ittID = edu.uci.ics.xadlutils.XadlUtils.getID(xarch, interfaceTypeRef);
		itt.setXArchID(ittID);
		thingIDMap.mapRefToID(interfaceTypeRef, itt.getID());
		
		String label = "(No Description)";
		String desc = edu.uci.ics.xadlutils.XadlUtils.getDescription(xarch, interfaceTypeRef);
		if(desc != null) label = desc;
		itt.setToolTip(label);
		
		if(existingThingID == null){
			//If it hasn't been placed already...
			//Set a default bounding box
			
			itt.setBoundingBox((DefaultCoordinateMapper.DEFAULT_WORLD_WIDTH / 2) + 50,
				(DefaultCoordinateMapper.DEFAULT_WORLD_HEIGHT / 2) + 50,	50, 50);
			
			itt.setOrientation(InterfaceTypeThing.ORIENTATION_N);
			itt.setFlow(InterfaceTypeThing.FLOW_INOUT);
		
			//Set a default color
			itt.setColor(Color.WHITE);
			
			renderingHints.applyRenderingHints(ittID, itt);
			typeBNAModel.addThing(itt, typeParent);
		}
		fireInterfaceTypeUpdated(interfaceTypeRef, itt);
	}

	public static TypeMappingLogic getTypeMappingLogic(BNAComponent bnaComponent){
		MappingLogic[] mls = AbstractArchipelagoTreePlugin.getAllMappingLogics(bnaComponent);
		for(int i = 0; i < mls.length; i++){
			if(mls[i] instanceof TypeMappingLogic){
				return (TypeMappingLogic)mls[i];
			}
		}
		return null;
	}
	
	
}

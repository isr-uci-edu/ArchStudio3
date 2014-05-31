package archstudio.comp.archipelago.types;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;

import javax.swing.JOptionPane;

import c2.util.UIDGenerator;

import archstudio.comp.archipelago.ThingIDMap;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.logic.AbstractDropTargetLogic;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchFlatInterface;
import edu.uci.ics.xarchutils.XArchPath;

public class SetTypeDropTargetLogic extends AbstractDropTargetLogic {

	private GlowboxThing gb = null;

	protected XArchFlatInterface xarch;
	protected ThingIDMap thingIDMap;
	protected ObjRef archStructureRef;
	
	public SetTypeDropTargetLogic(ThingIDMap thingIDMap, XArchFlatInterface xarch, ObjRef archStructureRef){
		super();
		this.thingIDMap = thingIDMap;
		this.xarch = xarch;
		this.archStructureRef = archStructureRef;
	}

	//For when doing archtypes for setting signature types by D&D
	public SetTypeDropTargetLogic(ThingIDMap thingIDMap, XArchFlatInterface xarch){
		super();
		this.thingIDMap = thingIDMap;
		this.xarch = xarch;
		this.archStructureRef = null;
	}
	
	public static boolean isTypeDrag(Transferable t){
		if(!t.isDataFlavorSupported(ObjRefTransferable.OBJREF_DATA_FLAVOR)){
			return false;
		}
		ObjRefTransferable ort = null;
		try{
			ort = (ObjRefTransferable)t.getTransferData(ObjRefTransferable.OBJREF_DATA_FLAVOR);
			XArchPath refPath = ort.getXArchPath();
			if(refPath.toTagsOnlyString().equals("xArch/archTypes/componentType")){
				return true;
			}
			else if(refPath.toTagsOnlyString().equals("xArch/archTypes/connectorType")){
				return true;
			}
			else if(refPath.toTagsOnlyString().equals("xArch/archTypes/interfaceType")){
				return true;
			}
		}
		catch(Exception e){
			return false;
		}
		return false;
	}
	
	public void dragEnter(DropTargetDragEvent dtde, Thing thingOnTop, int worldX, int worldY){}
	public void dragExit(DropTargetEvent dtde){
		removeGlowbox();
	}
	
	public synchronized void dragOver(DropTargetDragEvent dtde, Thing thingOnTop, int worldX, int worldY){
		BNAComponent c = getBNAComponent();
		if(c == null){
			return;
		}
		if(thingOnTop instanceof IHasXadlType){
			if(thingOnTop instanceof IBoxBounded){
				if(gb == null){
					gb = new GlowboxThing();
					BNAUtils.setStackingPriority(gb, BNAUtils.STACKING_PRIORITY_ALWAYS_ON_TOP);
					c.getModel().addThing(gb);
				}
				java.awt.Rectangle boundingBox = ((IBoxBounded)thingOnTop).getBoundingBox();
				java.awt.Rectangle gbBoundingBox = gb.getBoundingBox();
				if(!boundingBox.equals(gbBoundingBox)){
					java.awt.Rectangle newBoundingBox = new java.awt.Rectangle(boundingBox);
					gb.setBoundingBox(newBoundingBox);
				}
			}
		}
		else{
			removeGlowbox();
		}
	}
	
	private void removeGlowbox(){
		BNAComponent c = getBNAComponent();
		if(c != null){
			if(gb != null){
				c.getModel().removeThing(gb);
				gb = null;
			}
		}
	}
	
	private static final int WAS_COMPONENT = 100;
	private static final int WAS_CONNECTOR = 200;
	
	private static String typeToInstanceDescription(String typeDescription){
		try{
			if(typeDescription.toLowerCase().endsWith("_type")){
				return typeDescription.substring(0, typeDescription.length() - 5).trim();
			}
			else if(typeDescription.toLowerCase().endsWith("type")){
				return typeDescription.substring(0, typeDescription.length() - 4).trim();
			}
			return typeDescription;
		}
		catch(Exception e){
			return typeDescription;
		}
	}
	
	private static String sigToIfaceDescription(String sigDescription){
		try{
			if(sigDescription.toLowerCase().endsWith("_signature")){
				return sigDescription.substring(0, sigDescription.length() - 10).trim();
			}
			else if(sigDescription.toLowerCase().endsWith("signature")){
				return sigDescription.substring(0, sigDescription.length() - 9).trim();
			}
			else if(sigDescription.toLowerCase().endsWith("_sig")){
				return sigDescription.substring(0, sigDescription.length() - 4).trim();
			}
			else if(sigDescription.toLowerCase().endsWith("sig")){
				return sigDescription.substring(0, sigDescription.length() - 3).trim();
			}
			return sigDescription;
		}
		catch(Exception e){
			return sigDescription;
		}
	}
	
	private boolean stampOutType(BNAComponent c, DropTargetDropEvent dtde, Thing thingOnTop, int worldX, int worldY, ObjRef typeRef, String typeID){
		ObjRef xArchRef = xarch.getXArch(typeRef);
		ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
		
		ObjRef brickRef = null;
		
		int kindOfBrick = 0;
		if(xarch.isInstanceOf(typeRef, "edu.uci.isr.xarch.types.IComponentType")){
			brickRef = xarch.create(typesContextRef, "Component");
			String brickID = c2.util.UIDGenerator.generateUID("component");
			xarch.set(brickRef, "id", brickID);
			
			String brickDescription = "(New Component)";
			String typeDescription = XadlUtils.getDescription(xarch, typeRef);
			if(typeDescription != null){
				brickDescription = typeToInstanceDescription(typeDescription);
			}

			ObjRef brickDescriptionRef = xarch.create(typesContextRef, "Description");
			xarch.set(brickDescriptionRef, "Value", brickDescription);
			xarch.set(brickRef, "Description", brickDescriptionRef);
			
			kindOfBrick = WAS_COMPONENT;
		}
		else if(xarch.isInstanceOf(typeRef, "edu.uci.isr.xarch.types.IConnectorType")){
			brickRef = xarch.create(typesContextRef, "Connector");
			String brickID = c2.util.UIDGenerator.generateUID("connector");
			xarch.set(brickRef, "id", brickID);

			String brickDescription = "(New Connector)";
			String typeDescription = XadlUtils.getDescription(xarch, typeRef);
			if(typeDescription != null){
				brickDescription = typeToInstanceDescription(typeDescription);
			}

			ObjRef brickDescriptionRef = xarch.create(typesContextRef, "Description");
			xarch.set(brickDescriptionRef, "Value", brickDescription);
			xarch.set(brickRef, "Description", brickDescriptionRef);

			kindOfBrick = WAS_CONNECTOR;
		}
		else{
			return false;
		}
		
		ObjRef[] signatureRefs = xarch.getAll(typeRef, "signature");
		for(int i = 0; i < signatureRefs.length; i++){
			ObjRef signatureRef = signatureRefs[i];
			
			String signatureDirection = XadlUtils.getDirection(xarch, signatureRef);
			
			ObjRef signatureTypeLinkRef = (ObjRef)xarch.get(signatureRef, "type");
			String signatureTypeLinkType = null;
			String signatureTypeLinkHref = null;
			if(signatureTypeLinkRef != null){
				signatureTypeLinkType = (String)xarch.get(signatureTypeLinkRef, "type");
				signatureTypeLinkHref = (String)xarch.get(signatureTypeLinkRef, "href");
			}
			
			ObjRef interfaceRef = xarch.create(typesContextRef, "Interface");
			String interfaceID = UIDGenerator.generateUID("interface");
			xarch.set(interfaceRef, "id", interfaceID);
			
			String interfaceDescription = "(New Interface)";
			String sigDescription = XadlUtils.getDescription(xarch, signatureRef);
			if(sigDescription != null){
				interfaceDescription = sigToIfaceDescription(sigDescription);
			}

			ObjRef interfaceDescriptionRef = xarch.create(typesContextRef, "Description");
			xarch.set(interfaceDescriptionRef, "Value", interfaceDescription);
			xarch.set(interfaceRef, "Description", interfaceDescriptionRef);
			
			if(signatureDirection != null){
				ObjRef interfaceDirectionRef = xarch.create(typesContextRef, "Direction");
				xarch.set(interfaceDirectionRef, "Value", signatureDirection);
				xarch.set(interfaceRef, "Direction", interfaceDirectionRef);
			}
			
			String signatureID = XadlUtils.getID(xarch, signatureRef);
			if(signatureID != null){
				ObjRef interfaceSignatureLinkRef = xarch.create(typesContextRef, "XMLLink");
				xarch.set(interfaceSignatureLinkRef, "type", "simple");
				xarch.set(interfaceSignatureLinkRef, "href", "#" + signatureID);
				xarch.set(interfaceRef, "signature", interfaceSignatureLinkRef);
			}
			
			ObjRef interfaceTypeLinkRef = xarch.create(typesContextRef, "XMLLink");
			if(signatureTypeLinkType != null){
				xarch.set(interfaceTypeLinkRef, "type", signatureTypeLinkType);
			}
			if(signatureTypeLinkHref != null){
				xarch.set(interfaceTypeLinkRef, "href", signatureTypeLinkHref);
			}
			xarch.set(interfaceRef, "type", interfaceTypeLinkRef);
			xarch.add(brickRef, "interface", interfaceRef);
		}
		
		ObjRef brickTypeLinkRef = xarch.create(typesContextRef, "XMLLink");
		xarch.set(brickTypeLinkRef, "type", "simple");
		xarch.set(brickTypeLinkRef, "href", "#" + typeID);
		
		xarch.set(brickRef, "type", brickTypeLinkRef);
		
		if(kindOfBrick == WAS_COMPONENT){
			xarch.add(archStructureRef, "component", brickRef);
		}
		else if(kindOfBrick == WAS_CONNECTOR){
			xarch.add(archStructureRef, "connector", brickRef);
		}
		
		BNAUtils.showUserNotificationUL(c, "Type successfully stamped out.");
		return true;
	}
	
	public boolean drop(DropTargetDropEvent dtde, Thing thingOnTop, int worldX, int worldY){
		BNAComponent c = getBNAComponent();
		if(c == null){
			return false;
		}
		removeGlowbox();
		
		Transferable t = dtde.getTransferable();
		if(!t.isDataFlavorSupported(ObjRefTransferable.OBJREF_DATA_FLAVOR)){
			return false;
		}
		
		if(isTypeDrag(t)){
			ObjRefTransferable ort = null;
			try{
				ort = (ObjRefTransferable)t.getTransferData(ObjRefTransferable.OBJREF_DATA_FLAVOR);
			}
			catch(Exception e){
				return false;
			}

			ObjRef ref = ort.getObjRef();
			
			String typeID = XadlUtils.getID(xarch, ref);
			if(typeID == null){
				JOptionPane.showMessageDialog(c, "Type has no ID.", "Can't Set Type", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			if(thingOnTop == null){
				if(archStructureRef != null){
					boolean ok = stampOutType(c, dtde, thingOnTop, worldX, worldY, ref, typeID);
					if(ok){
						//dtde.acceptDrop(dtde.getDropAction());
						return true;
					}
				}
				return false;
			}
			
			ObjRef thingOnTopRef = thingIDMap.getXArchRef(thingOnTop.getID());
			if(thingOnTopRef == null){
				JOptionPane.showMessageDialog(c, "Thing not mapped to xArch Element", "Can't Set Type", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			if(thingOnTop instanceof IHasXadlType){
				ObjRef typeRef = (ObjRef)xarch.get(thingOnTopRef, "type");
				if(typeRef == null){
					//Might happen if it has no type link at all.
					ObjRef xArchRef = xarch.getXArch(thingOnTopRef);
					ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
					typeRef = xarch.create(typesContextRef, "XMLLink");
					xarch.set(thingOnTopRef, "type", typeRef);
				}
				
				//Set the link parameters.
				xarch.set(typeRef, "type", "simple");
				xarch.set(typeRef, "href", "#" + typeID);
				
				BNAUtils.showUserNotificationUL(c, "Type successfully assigned.");
				return true;
			}
			else{
				JOptionPane.showMessageDialog(c, "Thing not allowed to have a type.", "Can't Set Type", JOptionPane.ERROR_MESSAGE);
			}
		}
		return false;
	}
	
	public void dropActionChanged(DropTargetDragEvent dtde, Thing thingOnTop, int worldX, int worldY){}


}

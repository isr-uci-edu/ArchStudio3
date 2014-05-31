package archstudio.comp.archipelago.types;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;

import javax.swing.JOptionPane;

import archstudio.comp.archipelago.ThingIDMap;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.logic.AbstractDropTargetLogic;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchFlatInterface;
import edu.uci.ics.xarchutils.XArchPath;

public class SetSubarchitectureDropTargetLogic extends AbstractDropTargetLogic {

	private GlowboxThing gb = null;

	protected XArchFlatInterface xarch;
	protected ThingIDMap thingIDMap;
	
	public static boolean isStructureDrag(Transferable t){
		if(!t.isDataFlavorSupported(ObjRefTransferable.OBJREF_DATA_FLAVOR)){
			return false;
		}
		ObjRefTransferable ort = null;
		try{
			ort = (ObjRefTransferable)t.getTransferData(ObjRefTransferable.OBJREF_DATA_FLAVOR);
			XArchPath refPath = ort.getXArchPath();
			if(refPath.toTagsOnlyString().equals("xArch/archStructure")){
				return true;
			}
		}
		catch(Exception e){
			return false;
		}
		return false;
	}
	
	public SetSubarchitectureDropTargetLogic(ThingIDMap thingIDMap, XArchFlatInterface xarch){
		super();
		this.thingIDMap = thingIDMap;
		this.xarch = xarch;
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
		if(thingOnTop instanceof BrickTypeThing){
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
		
		ObjRefTransferable ort = null;
		try{
			ort = (ObjRefTransferable)t.getTransferData(ObjRefTransferable.OBJREF_DATA_FLAVOR);
		}
		catch(Exception e){
			return false;
		}

		if(isStructureDrag(t)){
			ObjRef archStructureRef = ort.getObjRef();
			String structureID = XadlUtils.getID(xarch, archStructureRef);
			if(structureID == null){
				JOptionPane.showMessageDialog(c, "Structure has no ID.", "Can't Set Substructure", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			if((thingOnTop != null) && (thingOnTop instanceof BrickTypeThing)){
				ObjRef typeRef = thingIDMap.getXArchRef(thingOnTop.getID());
				if(typeRef == null){
					JOptionPane.showMessageDialog(c, "Thing not mapped to xArch Element", "Can't Set Type", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				
				int result = JOptionPane.showConfirmDialog(c, 
					"Are you sure?", "Confirm Set/Replace Sub-architecture", JOptionPane.YES_NO_OPTION);
				if(result != JOptionPane.YES_OPTION){
					return true;
				}
				
				ObjRef xArchRef = xarch.getXArch(archStructureRef);
				ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
				
				ObjRef subArchitectureRef = xarch.create(typesContextRef, "subArchitecture");
				ObjRef archStructureLinkRef = xarch.create(typesContextRef, "XMLLink");
				xarch.set(archStructureLinkRef, "type", "simple");
				xarch.set(archStructureLinkRef, "href", "#" + structureID);
				
				xarch.set(subArchitectureRef, "archStructure", archStructureLinkRef);
				
				xarch.clear(typeRef, "subArchitecture");
				xarch.set(typeRef, "subArchitecture", subArchitectureRef);
				
				BNAUtils.showUserNotificationUL(c, "Sub-architecture successfully assigned.");
				return true;
			}
			else{
				//Dropping a structure on something other than a brick type
				JOptionPane.showMessageDialog(c, "Can only drop structures on types to set substructure.", "Wrong Drop Type", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return false;
	}
	
	public void dropActionChanged(DropTargetDragEvent dtde, Thing thingOnTop, int worldX, int worldY){}
}

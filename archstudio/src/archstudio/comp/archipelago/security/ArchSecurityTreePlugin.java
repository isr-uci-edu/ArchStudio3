package archstudio.comp.archipelago.security;

import java.awt.BasicStroke;
import java.awt.Stroke;

import javax.swing.JOptionPane;

import archstudio.comp.aac.AACC2Component;
import archstudio.comp.archipelago.AbstractArchipelagoTreePlugin;
import archstudio.comp.archipelago.ArchipelagoFrame;
import archstudio.comp.archipelago.ArchipelagoHintsInfo;
import archstudio.comp.archipelago.ArchipelagoTree;
import archstudio.comp.archipelago.PropertyTableLogic;
import archstudio.comp.archipelago.types.ArchStructureTreeNode;
import archstudio.comp.archipelago.types.ArchStructureTreePlugin;
import archstudio.comp.archipelago.types.ArchStructureTreePluginListener;
import archstudio.comp.archipelago.types.ArchTypesTreeNode;
import archstudio.comp.archipelago.types.ArchTypesTreePlugin;
import archstudio.comp.archipelago.types.ArchTypesTreePluginListener;
import archstudio.comp.archipelago.types.BrickMappingLogic;
import archstudio.comp.archipelago.types.BrickMappingLogicAdapter;
import archstudio.comp.archipelago.types.BrickMappingLogicListener;
import archstudio.comp.archipelago.types.BrickTypeThing;
import archstudio.comp.archipelago.types.ComponentThing;
import archstudio.comp.archipelago.types.ComponentTypeThing;
import archstudio.comp.archipelago.types.ConnectorTypeThing;
import archstudio.comp.archipelago.types.TypeMappingLogic;
import archstudio.comp.archipelago.types.TypeMappingLogicAdapter;
import archstudio.comp.archipelago.types.TypeMappingLogicListener;
import archstudio.comp.booleannotation.IBooleanNotation;
import archstudio.comp.preferences.IPreferences;
import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;
import c2.util.MessageSendProxy;
import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.bna.IStroked;
import edu.uci.ics.bna.contextmenu.SelectionBasedContextMenuLogic;
import edu.uci.ics.widgets.navpanel.NavigationItem;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchPath;

public class ArchSecurityTreePlugin extends AbstractArchipelagoTreePlugin
implements ArchStructureTreePluginListener, ArchTypesTreePluginListener{

	public static final Stroke SECURITY_STROKE = new BasicStroke(1.0f, 
		BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 1.0f, 
		new float[]{4.0f, 3.0f, 1.0f, 3.0f}, 0.0f);

	protected IBooleanNotation bni;

	protected ArchStructureTreePlugin 	archStructureTreePlugin;
	protected ArchTypesTreePlugin 		archTypesTreePlugin;
	protected ArchipelagoFrame			frame;

	public ArchSecurityTreePlugin(MessageSendProxy topIfaceSender, MessageSendProxy bottomIfaceSender,
	ArchipelagoFrame frame, ArchipelagoTree tree,
	XArchFlatTransactionsInterface xarch, IPreferences preferences, IBooleanNotation bni,
	ArchStructureTreePlugin astp, ArchTypesTreePlugin attp){
		super(topIfaceSender, bottomIfaceSender, frame, tree, xarch, preferences);
		this.bni = bni;
		
		this.archStructureTreePlugin = astp;
		archStructureTreePlugin.addArchStructureTreePluginListener(this);
		
		this.archTypesTreePlugin = attp;
		archTypesTreePlugin.addArchTypesTreePluginListener(this);
		this.frame = frame;
	}

	public void handle(c2.fw.Message m){
	}
	
	public ArchipelagoHintsInfo[] getHintsInfo(){
		return new ArchipelagoHintsInfo[0];
	}
	
	public boolean navigateTo(NavigationItem navigationItem) {
		return false;
	}
	
	public void typeBNAComponentCreated(ArchTypesTreeNode node,
	BNAComponent typeBNAComponent){
		TypeMappingLogic typeMappingLogic = TypeMappingLogic.getTypeMappingLogic(typeBNAComponent);
		typeMappingLogic.addTypeMappingLogicListener(new SecurityTypeMappingLogic());
		
		SelectionBasedContextMenuLogic sbcml = 
			SelectionBasedContextMenuLogic.getSelectionBasedContextMenuLogic(
			typeBNAComponent);
		sbcml.addPlugin(new SecurityContextMenuPlugin(typeBNAComponent, this,
			node.getThingIDMap(), xarch, bni, null));

		PropertyTableLogic ptl = PropertyTableLogic.getPropertyTableLogic(typeBNAComponent);
		if(ptl != null){
			ptl.addPropertyTablePlugin(new SecurityPropertyTablePlugin(xarch, bni));
		}
	}

	public void typeBNAComponentDestroyed(BNAComponent typeBNAComponent){
	}

	public void typeBNAComponentDestroying(BNAComponent typeBNAComponent){
	}
	
	class SecurityTypeMappingLogic extends TypeMappingLogicAdapter
	implements TypeMappingLogicListener{
		
		public void componentTypeUpdated(ObjRef typeRef, ComponentTypeThing ctt){
			if(typeRef == null){
				return;
			}
			if(xarch.isInstanceOf(typeRef, "edu.uci.isr.xarch.security.ISecureComponentType")){
				updateSecureComponentType(typeRef, ctt);
			}
		}
		
		public void connectorTypeUpdated(ObjRef typeRef, ConnectorTypeThing ctt){
			if(typeRef == null){
				return;
			}
			if(xarch.isInstanceOf(typeRef, "edu.uci.isr.xarch.security.ISecureConnectorType")){
				updateSecureConnectorType(typeRef, ctt);
			}
		}
		
		public void updateSecureComponentType(ObjRef typeRef, ComponentTypeThing ctt){
			updateSecureType(typeRef, ctt);
		}
		
		public void updateSecureConnectorType(ObjRef typeRef, ConnectorTypeThing ctt){
			updateSecureType(typeRef, ctt);
		}
		
		public void updateSecureType(ObjRef typeRef, BrickTypeThing btt){
		}
	}

	public boolean showRef(ObjRef ref, XArchPath path){
		return false;
	}

	//--Structure stuff--
	public void structureBNAComponentCreated(ArchStructureTreeNode node,
	BNAComponent structureBNAComponent){
		AccessControlMainMenuLogic acmml = new AccessControlMainMenuLogic(frame.getJMenuBar(), this);
		structureBNAComponent.addThingLogic(acmml);
		BrickMappingLogic brickMappingLogic = BrickMappingLogic.getBrickMappingLogic(structureBNAComponent);
		brickMappingLogic.addBrickMappingLogicListener(new SecurityBrickMappingLogic());

		SelectionBasedContextMenuLogic sbcml = 
			SelectionBasedContextMenuLogic.getSelectionBasedContextMenuLogic(
			structureBNAComponent);
		sbcml.addPlugin(new SecurityContextMenuPlugin(structureBNAComponent, this,
			node.getThingIDMap(), xarch, bni, acmml));
	}

	ObjRef		accessingInterface = null;
	
	public void setAccessingInterface(ObjRef accessingInterface) {
		this.accessingInterface = accessingInterface;
	}
	
	// These two properties are put here, because the interfaces can be at 
	// different levels of archStructure, so they need to be at the container
	// of these archStructures
	public ObjRef getAccessingInterface() {
		return accessingInterface;
	}
	
	ObjRef		accessedInterface = null;
	
	public void setAccessedInterface(ObjRef accessedInterface) {
		this.accessedInterface = accessedInterface;
	}
	
	public ObjRef getAccessedInterface() {
		return accessedInterface;
	}
	
	public void handleCheck() {
		boolean		result = AACC2Component.checkAccessControl(xarch, 
				frame.getDocumentSource(), accessingInterface, accessedInterface);
		if (result) 
			JOptionPane.showMessageDialog(frame, "Access is allowed");
		else
			JOptionPane.showMessageDialog(frame, "Access is denied");
	}
	
	public void structureBNAComponentDestroyed(BNAComponent structureBNAComponent){
	}
	
	public void structureBNAComponentDestroying(BNAComponent structureBNAComponent){
	}

	class SecurityBrickMappingLogic extends BrickMappingLogicAdapter	implements BrickMappingLogicListener{
		public void componentUpdated(ObjRef brickRef, ComponentThing ct){
			if(brickRef == null) return;

			ObjRef typeRef = XadlUtils.resolveXLink(xarch, brickRef, "type");
			if(typeRef == null) return;
			
			if(xarch.isInstanceOf(typeRef, "edu.uci.isr.xarch.security.ISecureComponentType")){
				updateSecureComponent(brickRef, ct, typeRef);
			}
			else{
				if((ct.getStroke() != null) && ct.getStroke().equals(SECURITY_STROKE)){
					ct.removeProperty(IStroked.STROKE_PROPERTY_NAME);
				}
			}
		}

		public void updateSecureComponent(ObjRef brickRef, ComponentThing ct, ObjRef secureTypeRef){
		}
	}
}

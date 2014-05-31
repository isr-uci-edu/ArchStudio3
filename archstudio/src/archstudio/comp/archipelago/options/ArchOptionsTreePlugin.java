package archstudio.comp.archipelago.options;

import java.awt.Stroke;
import java.awt.BasicStroke;

import c2.util.MessageSendProxy;
import edu.uci.ics.bna.*;
import edu.uci.ics.bna.contextmenu.SelectionBasedContextMenuLogic;
import edu.uci.ics.widgets.navpanel.NavigationItem;
import edu.uci.ics.xarchutils.*;
import archstudio.comp.archipelago.*;
import archstudio.comp.archipelago.types.*;
import archstudio.comp.booleannotation.IBooleanNotation;
import archstudio.comp.guardtracker.GetAllGuardsMessage;
import archstudio.comp.guardtracker.GuardsMessage;

import archstudio.comp.preferences.IPreferences;
import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;

public class ArchOptionsTreePlugin extends AbstractArchipelagoTreePlugin
implements ArchStructureTreePluginListener, ArchTypesTreePluginListener{

	public static final String OPTIONAL_BRICK_MAPPING_LOGIC_PROPERTY_NAME = "optionalBrickMappingLogic";
	public static final String OPTIONAL_LINK_MAPPING_LOGIC_PROPERTY_NAME = "optionalLinkMappingLogic";
	public static final String OPTIONAL_TYPE_MAPPING_LOGIC_PROPERTY_NAME = "optionalTypeMappingLogic";

	public static final Stroke OPTIONAL_STROKE = new BasicStroke(1.0f, 
		BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 1.0f, 
		new float[]{3.0f, 3.0f}, 0.0f);

	protected IBooleanNotation bni;

	protected ArchStructureTreePlugin archStructureTreePlugin;
	protected ArchTypesTreePlugin archTypesTreePlugin;
	
	protected String[] documentGuardStrings = new String[0];
	
	public ArchOptionsTreePlugin(MessageSendProxy topIfaceSender, MessageSendProxy bottomIfaceSender,
	ArchipelagoFrame frame, ArchipelagoTree tree,
	XArchFlatTransactionsInterface xarch, IPreferences preferences, IBooleanNotation bni,
	ArchipelagoTreePlugin atp){
		super(topIfaceSender, bottomIfaceSender, frame, tree, xarch, preferences);
		this.bni = bni;
		
		if(atp instanceof ArchStructureTreePlugin){
			this.archStructureTreePlugin = (ArchStructureTreePlugin)atp;
			this.archStructureTreePlugin.addArchStructureTreePluginListener(this);
		}
		else if(atp instanceof ArchTypesTreePlugin){
			this.archTypesTreePlugin = (ArchTypesTreePlugin)atp;
			this.archTypesTreePlugin.addArchTypesTreePluginListener(this);
		}
		else{
			throw new IllegalArgumentException("ArchOptionsTreePlugin only works with " +				"ArchStructureTreePlugins or ArchTypesTreePlugins.");
		}
		
		topIfaceSender.send(new GetAllGuardsMessage());
	}
	
	public void handle(c2.fw.Message m){
		if(m instanceof GuardsMessage){
			GuardsMessage gm = (GuardsMessage)m;
			ObjRef xArchRef = gm.getXArchRef();
			if((xArchRef != null) && (xArchRef.equals(frame.getDocumentSource()))){
				documentGuardStrings = gm.getGuardStrings();
			}
		}
	}
	
	public String[] getDocumentGuardStrings(){
		return documentGuardStrings;
	}

	public ArchipelagoHintsInfo[] getHintsInfo(){
		return new ArchipelagoHintsInfo[0];
	}
	
	public boolean navigateTo(NavigationItem navigationItem) {
		return false;
	}
	
	public void structureBNAComponentCreated(ArchStructureTreeNode node,
	BNAComponent structureBNAComponent){
		BrickMappingLogic brickMappingLogic = BrickMappingLogic.getBrickMappingLogic(structureBNAComponent);
		LinkMappingLogic linkMappingLogic = LinkMappingLogic.getLinkMappingLogic(structureBNAComponent);

		OptionalBrickMappingLogic optionalBrickMappingLogic = new OptionalBrickMappingLogic(structureBNAComponent);
		brickMappingLogic.addBrickMappingLogicListener(optionalBrickMappingLogic);
		structureBNAComponent.setProperty(OPTIONAL_BRICK_MAPPING_LOGIC_PROPERTY_NAME, 
			optionalBrickMappingLogic);

		OptionalLinkMappingLogic optionalLinkMappingLogic = new OptionalLinkMappingLogic();
		linkMappingLogic.addLinkMappingLogicListener(optionalLinkMappingLogic);
		structureBNAComponent.setProperty(OPTIONAL_LINK_MAPPING_LOGIC_PROPERTY_NAME, 
			optionalLinkMappingLogic);
		
		SelectionBasedContextMenuLogic sbcml = 
			SelectionBasedContextMenuLogic.getSelectionBasedContextMenuLogic(
			structureBNAComponent);
		sbcml.addPlugin(new OptionalContextMenuPlugin(structureBNAComponent, this, 
			node.getThingIDMap(), xarch, bni));
			
		PropertyTableLogic ptl = PropertyTableLogic.getPropertyTableLogic(structureBNAComponent);
		if(ptl != null){
			ptl.addPropertyTablePlugin(new OptionsPropertyTablePlugin(xarch, bni));
		}
	}



	public void structureBNAComponentDestroyed(BNAComponent structureBNAComponent){
	}

	public void structureBNAComponentDestroying(BNAComponent structureBNAComponent){
		BrickMappingLogic brickMappingLogic = BrickMappingLogic.getBrickMappingLogic(structureBNAComponent);
		if(brickMappingLogic != null){
			OptionalBrickMappingLogic optionalBrickMappingLogic = 
				(OptionalBrickMappingLogic)structureBNAComponent.getProperty(OPTIONAL_BRICK_MAPPING_LOGIC_PROPERTY_NAME);
			if(optionalBrickMappingLogic != null){
				brickMappingLogic.removeBrickMappingLogicListener(optionalBrickMappingLogic);
			}
		}

		LinkMappingLogic linkMappingLogic = LinkMappingLogic.getLinkMappingLogic(structureBNAComponent);
		if(linkMappingLogic != null){
			OptionalLinkMappingLogic optionalLinkMappingLogic = 
				(OptionalLinkMappingLogic)structureBNAComponent.getProperty(OPTIONAL_LINK_MAPPING_LOGIC_PROPERTY_NAME);
			if(optionalLinkMappingLogic != null){
				linkMappingLogic.removeLinkMappingLogicListener(optionalLinkMappingLogic);
			}
		}
	}

	public void typeBNAComponentCreated(ArchTypesTreeNode node, BNAComponent typeBNAComponent){
		TypeMappingLogic typeMappingLogic = TypeMappingLogic.getTypeMappingLogic(typeBNAComponent);

		OptionalTypeMappingLogic optionalTypeMappingLogic = new OptionalTypeMappingLogic(typeBNAComponent);
		typeMappingLogic.addTypeMappingLogicListener(optionalTypeMappingLogic);
		typeBNAComponent.setProperty(OPTIONAL_TYPE_MAPPING_LOGIC_PROPERTY_NAME, 
			optionalTypeMappingLogic);

		SelectionBasedContextMenuLogic sbcml = 
			SelectionBasedContextMenuLogic.getSelectionBasedContextMenuLogic(
			typeBNAComponent);
		sbcml.addPlugin(new OptionalContextMenuPlugin(typeBNAComponent, this, node.getThingIDMap(),
			xarch, bni));

		PropertyTableLogic ptl = PropertyTableLogic.getPropertyTableLogic(typeBNAComponent);
		if(ptl != null){
			ptl.addPropertyTablePlugin(new OptionsPropertyTablePlugin(xarch, bni));
		}
	}

	public void typeBNAComponentDestroyed(BNAComponent typeBNAComponent){
	}

	public void typeBNAComponentDestroying(BNAComponent typeBNAComponent){
		TypeMappingLogic typeMappingLogic = TypeMappingLogic.getTypeMappingLogic(typeBNAComponent);
		if(typeMappingLogic != null){
			OptionalTypeMappingLogic optionalTypeMappingLogic = 
				(OptionalTypeMappingLogic)typeBNAComponent.getProperty(OPTIONAL_TYPE_MAPPING_LOGIC_PROPERTY_NAME);
			if(optionalTypeMappingLogic != null){
				typeMappingLogic.removeTypeMappingLogicListener(optionalTypeMappingLogic);
			}
		}

	}
	
	class OptionalTypeMappingLogic extends TypeMappingLogicAdapter
	implements TypeMappingLogicListener{
		
		protected BNAComponent bnaComponent;
		protected ThingIDMap thingIDMap;

		public OptionalTypeMappingLogic(BNAComponent bnaComponent){
			this.bnaComponent = bnaComponent;
			this.thingIDMap = 
				(ThingIDMap)bnaComponent.getProperty(ArchTypesTreePlugin.THING_ID_MAP_PROPERTY_NAME);
		}
		
		public void componentTypeUpdated(ObjRef brickTypeRef,	ComponentTypeThing ct){
			if(brickTypeRef == null){
				return;
			}
			updateOptionalSignatures(brickTypeRef, ct);
		}

		public void connectorTypeUpdated(ObjRef brickTypeRef,	ConnectorTypeThing ct){
			if(brickTypeRef == null){
				return;
			}
			updateOptionalSignatures(brickTypeRef, ct);
		}
		
		public void updateOptionalSignatures(ObjRef brickTypeRef, BrickTypeThing bt){
			ObjRef[] signatureRefs = xarch.getAll(brickTypeRef, "signature");
			for(int i = 0; i < signatureRefs.length; i++){
				if(xarch.isInstanceOf(signatureRefs[i], "edu.uci.isr.xarch.options.IOptionalSignature")){
					String stID = thingIDMap.getThingID(signatureRefs[i]);
					if(stID != null){
						SignatureThing st = (SignatureThing)bnaComponent.getModel().getThing(stID);
						updateOptionalSignature(signatureRefs[i], st);
					}
				}
			}
		}
		
		private void updateOptionalSignature(ObjRef signatureRef, SignatureThing st){
			ObjRef optionalRef = (ObjRef)xarch.get(signatureRef, "optional");
			if(optionalRef != null){
				ObjRef guardRef = (ObjRef)xarch.get(optionalRef, "guard");
				if(guardRef != null){
					st.setStroke(OPTIONAL_STROKE);
					return;
				}
			}
			
			st.removeProperty(IStroked.STROKE_PROPERTY_NAME);
		}
	}

 	class OptionalBrickMappingLogic extends BrickMappingLogicAdapter
	implements BrickMappingLogicListener{
		
		protected BNAComponent bnaComponent;
		protected ThingIDMap thingIDMap;
		
		public OptionalBrickMappingLogic(BNAComponent bnaComponent){
			this.bnaComponent = bnaComponent;
			this.thingIDMap = 
				(ThingIDMap)bnaComponent.getProperty(ArchStructureTreePlugin.THING_ID_MAP_PROPERTY_NAME);
		}
		
		public void componentUpdated(ObjRef brickRef, ComponentThing ct){
			if(brickRef == null){
				return;
			}
			if(xarch.isInstanceOf(brickRef, "edu.uci.isr.xarch.options.IOptionalComponent")){
				updateOptionalComponent(brickRef, ct);
			}
			updateOptionalInterfaces(brickRef, ct);
		}
		
		public void connectorUpdated(ObjRef brickRef, ConnectorThing ct){
			if(brickRef == null){
				return;
			}
			if(xarch.isInstanceOf(brickRef, "edu.uci.isr.xarch.options.IOptionalConnector")){
				updateOptionalConnector(brickRef, ct);
			}
			updateOptionalInterfaces(brickRef, ct);
		}
		
		public void updateOptionalComponent(ObjRef brickRef, ComponentThing ct){
			updateOptionalBrick(brickRef, ct);
		}
		
		public void updateOptionalConnector(ObjRef brickRef, ConnectorThing ct){
			updateOptionalBrick(brickRef, ct);
		}
		
		public void updateOptionalBrick(ObjRef brickRef, BrickThing bt){
			ObjRef optionalRef = (ObjRef)xarch.get(brickRef, "optional");
			if(optionalRef != null){
				ObjRef guardRef = (ObjRef)xarch.get(optionalRef, "guard");
				if(guardRef != null){
					if(bt.getStroke() == null){
						bt.setStroke(OPTIONAL_STROKE);
					}
					return;
				}
			}
			
			if((bt.getStroke() != null) && bt.getStroke().equals(OPTIONAL_STROKE)){
				bt.removeProperty(IStroked.STROKE_PROPERTY_NAME);
			}
		}
		
		public void updateOptionalInterfaces(ObjRef brickRef, BrickThing bt){
			ObjRef[] interfaceRefs = xarch.getAll(brickRef, "interface");
			for(int i = 0; i < interfaceRefs.length; i++){
				if(xarch.isInstanceOf(interfaceRefs[i], "edu.uci.isr.xarch.options.IOptionalInterface")){
					String itID = thingIDMap.getThingID(interfaceRefs[i]);
					if(itID != null){
						InterfaceThing it = (InterfaceThing)bnaComponent.getModel().getThing(itID);
						updateOptionalInterface(interfaceRefs[i], it);
					}
				}
			}
		}
		
		private void updateOptionalInterface(ObjRef interfaceRef, InterfaceThing it){
			ObjRef optionalRef = (ObjRef)xarch.get(interfaceRef, "optional");
			if(optionalRef != null){
				ObjRef guardRef = (ObjRef)xarch.get(optionalRef, "guard");
				if(guardRef != null){
					it.setStroke(OPTIONAL_STROKE);
					return;
				}
			}
			
			it.removeProperty(IStroked.STROKE_PROPERTY_NAME);
		}
	}


	class OptionalLinkMappingLogic extends LinkMappingLogicAdapter
	implements LinkMappingLogicListener{
		
		public void linkUpdated(ObjRef linkRef, LinkThing lt){
			if(linkRef == null){
				return;
			}
			if(xarch.isInstanceOf(linkRef, "edu.uci.isr.xarch.options.IOptionalLink")){
				updateOptionalLink(linkRef, lt);
			}
		}
		
		public void updateOptionalLink(ObjRef linkRef, LinkThing lt){
			ObjRef optionalRef = (ObjRef)xarch.get(linkRef, "optional");
			if(optionalRef != null){
				ObjRef guardRef = (ObjRef)xarch.get(optionalRef, "guard");
				if(guardRef != null){
					lt.setStroke(OPTIONAL_STROKE);
					return;
				}
			}
			
			lt.removeProperty(BoxThing.STROKE_PROPERTY_NAME);
		}
	}
	
	public boolean showRef(ObjRef ref, XArchPath path){
		return false;
	}
}

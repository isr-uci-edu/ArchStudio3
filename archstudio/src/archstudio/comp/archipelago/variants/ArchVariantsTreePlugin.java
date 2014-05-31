package archstudio.comp.archipelago.variants;

import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.BasicStroke;

import c2.util.MessageSendProxy;
import edu.uci.ics.bna.*;
import edu.uci.ics.bna.contextmenu.SelectionBasedContextMenuLogic;
import edu.uci.ics.bna.thumbnail.Thumbnail;
import edu.uci.ics.widgets.navpanel.NavigationItem;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.*;
import archstudio.comp.archipelago.*;
import archstudio.comp.archipelago.types.*;
import archstudio.comp.booleannotation.IBooleanNotation;
import archstudio.comp.guardtracker.GuardsMessage;

import archstudio.comp.preferences.IPreferences;
import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;

public class ArchVariantsTreePlugin extends AbstractArchipelagoTreePlugin
implements ArchStructureTreePluginListener, ArchTypesTreePluginListener{

	public static final Stroke VARIANT_STROKE = new BasicStroke(1.0f, 
		BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 1.0f, 
		new float[]{4.0f, 3.0f, 1.0f, 3.0f}, 0.0f);

	protected IBooleanNotation bni;

	protected ArchStructureTreePlugin archStructureTreePlugin;
	protected ArchTypesTreePlugin archTypesTreePlugin;

	protected String[] documentGuardStrings = new String[0];
	
	public ArchVariantsTreePlugin(MessageSendProxy topIfaceSender, MessageSendProxy bottomIfaceSender,
	ArchipelagoFrame frame, ArchipelagoTree tree,
	XArchFlatTransactionsInterface xarch, IPreferences preferences, IBooleanNotation bni,
	ArchStructureTreePlugin astp, ArchTypesTreePlugin attp){
		super(topIfaceSender, bottomIfaceSender, frame, tree, xarch, preferences);
		this.bni = bni;
		
		this.archStructureTreePlugin = astp;
		archStructureTreePlugin.addArchStructureTreePluginListener(this);
		
		this.archTypesTreePlugin = attp;
		archTypesTreePlugin.addArchTypesTreePluginListener(this);
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
	
	public void typeBNAComponentCreated(ArchTypesTreeNode node,
	BNAComponent typeBNAComponent){
		TypeMappingLogic typeMappingLogic = TypeMappingLogic.getTypeMappingLogic(typeBNAComponent);
		typeMappingLogic.addTypeMappingLogicListener(new VariantTypeMappingLogic());
		
		SelectionBasedContextMenuLogic sbcml = 
			SelectionBasedContextMenuLogic.getSelectionBasedContextMenuLogic(
			typeBNAComponent);
		sbcml.addPlugin(new VariantsContextMenuPlugin(typeBNAComponent, this,
			node.getThingIDMap(), xarch, bni));

		PropertyTableLogic ptl = PropertyTableLogic.getPropertyTableLogic(typeBNAComponent);
		if(ptl != null){
			ptl.addPropertyTablePlugin(new VariantsPropertyTablePlugin(xarch, bni));
		}
	}

	public void typeBNAComponentDestroyed(BNAComponent typeBNAComponent){
	}

	public void typeBNAComponentDestroying(BNAComponent typeBNAComponent){
	}
	
	class VariantTypeMappingLogic extends TypeMappingLogicAdapter
	implements TypeMappingLogicListener{
		
		public void componentTypeUpdated(ObjRef typeRef, ComponentTypeThing ctt){
			if(typeRef == null){
				return;
			}
			if(xarch.isInstanceOf(typeRef, "edu.uci.isr.xarch.variants.IVariantComponentType")){
				updateVariantComponentType(typeRef, ctt);
			}
		}
		
		public void connectorTypeUpdated(ObjRef typeRef, ConnectorTypeThing ctt){
			if(typeRef == null){
				return;
			}
			if(xarch.isInstanceOf(typeRef, "edu.uci.isr.xarch.variants.IVariantConnectorType")){
				updateVariantConnectorType(typeRef, ctt);
			}
		}
		
		public void updateVariantComponentType(ObjRef typeRef, ComponentTypeThing ctt){
			updateVariantType(typeRef, ctt);
		}
		
		public void updateVariantConnectorType(ObjRef typeRef, ConnectorTypeThing ctt){
			updateVariantType(typeRef, ctt);
		}
		
		public void updateVariantType(ObjRef typeRef, BrickTypeThing btt){
			ObjRef[] variantRefs = xarch.getAll(typeRef, "variant");
			
			if((variantRefs != null) && (variantRefs.length > 0)){
				int offset = 0;
				DefaultBNAModel thumbnailModel = new DefaultBNAModel();
				for(int i = 0; i < variantRefs.length; i++){
					ObjRef variantTypeRef = XadlUtils.resolveXLink(xarch, variantRefs[i], "variantType");
					if(variantTypeRef != null){
						ArchTypesTreeNode variantTypeAttn = archTypesTreePlugin.getArchTypesTreeNode(variantTypeRef);
						if(variantTypeAttn != null){
							//If the model for that type has already been created, then just
							//get the model.
							BNAModel internalBNAModel = variantTypeAttn.getBNAModel();
							if(internalBNAModel == null){
								//If it doesn't, then use the ArchTypesTreePlugin to 
								//create one, assign it to the treenode, and use that.
								ScrollableBNAComponent createdScrollableBNAComponent = 
									archTypesTreePlugin.createTypeBNAComponent(variantTypeAttn);
								BNAComponent createdBNAComponent = createdScrollableBNAComponent.getBNAComponent();
								internalBNAModel = createdBNAComponent.getModel();
								variantTypeAttn.setBNAModel(internalBNAModel);
								
								//Throw away the created component, we only want the model.
								archTypesTreePlugin.destroyTypeBNAComponent(createdBNAComponent);
							}
							//Now we have to get the type thing out of the model
							BrickTypeThing variantTypeThing = null;
							Thing[] allThings = internalBNAModel.getAllThings();
							for(int j = 0; j < allThings.length; j++){
								if(allThings[j] instanceof BrickTypeThing){
									variantTypeThing = (BrickTypeThing)allThings[j];
									break;
								}
							}
							if(variantTypeThing != null){
								//OK, we have the Brick Type Thing
								//Let's add it to our internal model.
								//First, let's make a copy of it
								//because the copy is going to need
								//a different bounding box than the source.
								BrickTypeThing variantTypeThingCopy = null;
								if(variantTypeThing instanceof ComponentTypeThing){
									variantTypeThingCopy = new ComponentTypeThing((ComponentTypeThing)variantTypeThing);
								}
								else if(variantTypeThing instanceof ConnectorTypeThing){
									variantTypeThingCopy = new ConnectorTypeThing((ConnectorTypeThing)variantTypeThing);
								}
								Rectangle newBoundingBox = new Rectangle((DefaultCoordinateMapper.DEFAULT_WORLD_WIDTH / 2) + offset,
									(DefaultCoordinateMapper.DEFAULT_WORLD_HEIGHT / 2), 100, 100);
								offset += 110;
								variantTypeThingCopy.setBoundingBox(newBoundingBox);
								thumbnailModel.addThing(variantTypeThingCopy);
							}
						}
					}
				}
				if(thumbnailModel.getNumThings() > 0){
					btt.setStroke(VARIANT_STROKE);
					btt.setThumbnail(new Thumbnail(thumbnailModel));
					btt.setProperty("$thumbnailIsVariant", "true");
					return;
				}
			}
			String thumbnailIsVariantString = (String)btt.getProperty("$thumbnailIsVariant");
			if((thumbnailIsVariantString != null) && (thumbnailIsVariantString.equals("true"))){
				btt.removeProperty(IStroked.STROKE_PROPERTY_NAME);
				btt.removeProperty("$thumbnailIsVariant");
				btt.setThumbnail(null); 
			}
		}
	}

	public boolean showRef(ObjRef ref, XArchPath path){
		return false;
	}

	//--Structure stuff--
	public void structureBNAComponentCreated(ArchStructureTreeNode node,
	BNAComponent structureBNAComponent){
		BrickMappingLogic brickMappingLogic = BrickMappingLogic.getBrickMappingLogic(structureBNAComponent);
		brickMappingLogic.addBrickMappingLogicListener(new VariantBrickMappingLogic());

		SelectionBasedContextMenuLogic sbcml = 
			SelectionBasedContextMenuLogic.getSelectionBasedContextMenuLogic(
			structureBNAComponent);
		sbcml.addPlugin(new SelectVariantContextMenuPlugin(structureBNAComponent, this,
			node.getThingIDMap(), xarch));
	}

	public void structureBNAComponentDestroyed(BNAComponent structureBNAComponent){
	}
	
	public void structureBNAComponentDestroying(BNAComponent structureBNAComponent){
	}

	class VariantBrickMappingLogic extends BrickMappingLogicAdapter	implements BrickMappingLogicListener{
		public void componentUpdated(ObjRef brickRef, ComponentThing ct){
			if(brickRef == null) return;

			ObjRef typeRef = XadlUtils.resolveXLink(xarch, brickRef, "type");
			if(typeRef == null) return;
			
			if(xarch.isInstanceOf(typeRef, "edu.uci.isr.xarch.variants.IVariantComponentType")){
				updateVariantComponent(brickRef, ct, typeRef);
			}
			else{
				if((ct.getStroke() != null) && ct.getStroke().equals(VARIANT_STROKE)){
					ct.removeProperty(IStroked.STROKE_PROPERTY_NAME);
				}
			}
		}

		public void updateVariantComponent(ObjRef brickRef, ComponentThing ct, ObjRef variantTypeRef){
			ObjRef[] variantRefs = xarch.getAll(variantTypeRef, "variant");
			if(variantRefs.length > 0){
				if(ct.getStroke() == null){
					ct.setStroke(VARIANT_STROKE);
				}

				ArchTypesTreeNode variantTypeAttn = archTypesTreePlugin.getArchTypesTreeNode(variantTypeRef);
				if(variantTypeAttn != null){
					//If the model for that type has already been created, then just
					//get the model.
					BNAModel internalBNAModel = variantTypeAttn.getBNAModel();
					if(internalBNAModel == null){
						//If it doesn't, then use the ArchTypesTreePlugin to 
						//create one, assign it to the treenode, and use that.
						ScrollableBNAComponent createdScrollableBNAComponent = 
							archTypesTreePlugin.createTypeBNAComponent(variantTypeAttn);
						BNAComponent createdBNAComponent = createdScrollableBNAComponent.getBNAComponent();
						internalBNAModel = createdBNAComponent.getModel();
						variantTypeAttn.setBNAModel(internalBNAModel);
						//Throw away the created component, we only want the model.
						archTypesTreePlugin.destroyTypeBNAComponent(createdBNAComponent);
					}
					//Now we have to get the type thing out of the model
					BrickTypeThing variantTypeThing = null;
					Thing[] allThings = internalBNAModel.getAllThings();
					for(int j = 0; j < allThings.length; j++){
						if(allThings[j] instanceof BrickTypeThing){
							variantTypeThing = (BrickTypeThing)allThings[j];
							break;
						}
					}
					if(variantTypeThing != null){
						Thumbnail variantTypeThumbnail = variantTypeThing.getThumbnail();
						ct.setThumbnail(variantTypeThumbnail);
					}
				}
			}
			else{
				if((ct.getStroke() != null) && ct.getStroke().equals(VARIANT_STROKE)){
					ct.removeProperty(IStroked.STROKE_PROPERTY_NAME);
				}
			}
		}
	}
}

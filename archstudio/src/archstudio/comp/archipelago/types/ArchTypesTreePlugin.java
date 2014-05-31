package archstudio.comp.archipelago.types;

import edu.uci.ics.widgets.WidgetUtils;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchPath;
import archstudio.comp.archipelago.*;
import archstudio.comp.graphlayout.IGraphLayout;
import archstudio.comp.preferences.IPreferences;
import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;
import archstudio.preferences.RecentDirectoryPreferenceUtils;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.debug.*;
import edu.uci.ics.bna.logic.*;
import edu.uci.ics.bna.logic.action.AlignHorizontalCentersAction;
import edu.uci.ics.bna.logic.action.AlignVerticalCentersAction;
import edu.uci.ics.bna.logic.action.DistributeHorizontallyTightAction;
import edu.uci.ics.bna.logic.action.DistributeVerticallyTightAction;
import edu.uci.ics.bna.swingthing.*;
import edu.uci.ics.bna.contextmenu.*;
import edu.uci.ics.widgets.*;
import edu.uci.ics.widgets.navpanel.NavigationItem;
import edu.uci.ics.xadlutils.*;
import edu.uci.ics.xarchutils.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.tree.*;

import c2.util.MessageSendProxy;
import c2.util.UIDGenerator;

public class ArchTypesTreePlugin extends AbstractArchipelagoTreePlugin {

	public static final String TYPES_NODE_NAME = "Types";
	public static final String COMPONENT_TYPES_NODE_NAME = "Component Types";
	public static final String CONNECTOR_TYPES_NODE_NAME = "Connector Types";
	public static final String INTERFACE_TYPES_NODE_NAME = "Interface Types";

	public static final Icon TYPES_ICON;

	public static final String THING_ID_MAP_PROPERTY_NAME = "thingIDMap";
	//public static final String TYPE_MAPPING_LOGIC_PROPERTY_NAME = "typeMappingLogic";
	
	static{
		TYPES_ICON = edu.uci.ics.widgets.WidgetUtils.getImageIcon("archstudio/comp/typewrangler/res/types.gif");
	}

	protected ObjRef xArchRef = null;
	protected ObjRef typesContextRef = null;
	protected DoubleClickMouseAdapter mouseAdapter = null;
	protected IGraphLayout gli = null;

	protected archstudio.comp.archipelago.RenderingHints renderingHints = null;

	//protected DefaultBNAModel typeBNAModel;
	//protected BNAComponent typeBNAComponent;
	
	protected ArchStructureTreePlugin archStructureTreePlugin = null;

	protected Vector archTypesTreePluginListeners = new Vector();

	public ArchTypesTreePlugin(MessageSendProxy topIfaceSender, MessageSendProxy bottomIfaceSender,
	ArchipelagoFrame frame, ArchipelagoTree tree,
	XArchFlatTransactionsInterface xarch, IPreferences preferences, IGraphLayout gli){
		super(topIfaceSender, bottomIfaceSender, frame, tree, xarch, preferences);
		this.gli = gli;
	}
	
	public void addArchTypesTreePluginListener(ArchTypesTreePluginListener l){
		this.archTypesTreePluginListeners.addElement(l);
	}
	
	public void removeArchTypesTreePluginListener(ArchTypesTreePluginListener l){
		this.archTypesTreePluginListeners.removeElement(l);
	}
	
	protected void fireTypeBNAComponentCreated(ArchTypesTreeNode attn, BNAComponent typeBNAComponent){
		synchronized(archTypesTreePluginListeners){
			for(Iterator it = archTypesTreePluginListeners.iterator(); it.hasNext(); ){
				((ArchTypesTreePluginListener)it.next()).typeBNAComponentCreated(attn, typeBNAComponent);
			}
		}
	}
	
	protected void fireTypeBNAComponentDestroying(BNAComponent typeBNAComponent){
		synchronized(archTypesTreePluginListeners){
			for(Iterator it = archTypesTreePluginListeners.iterator(); it.hasNext(); ){
				((ArchTypesTreePluginListener)it.next()).typeBNAComponentDestroying(typeBNAComponent);
			}
		}
	}
	
	protected void fireTypeBNAComponentDestroyed(BNAComponent typeBNAComponent){
		synchronized(archTypesTreePluginListeners){
			for(Iterator it = archTypesTreePluginListeners.iterator(); it.hasNext(); ){
				((ArchTypesTreePluginListener)it.next()).typeBNAComponentDestroyed(typeBNAComponent);
			}
		}
	}
	
	public void setArchStructureTreePlugin(ArchStructureTreePlugin astp){
		this.archStructureTreePlugin = astp;
	}
	
	public ArchStructureTreePlugin getArchStructureTreePlugin(){
		return this.archStructureTreePlugin;
	}
	
	public void documentOpened(ObjRef xArchRef, ObjRef elementRef) {
		//When a document is opened, we want to populate the tree
		//with an ArchTypes element.
		//xArchRef = xarch.getOpenXArch(url);
		this.xArchRef = xArchRef;
		if(xArchRef != null){
			typesContextRef = xarch.createContext(xArchRef, "types");
		}
		mouseAdapter = new DoubleClickMouseAdapter();
		tree.addMouseListener(mouseAdapter);
		refreshArchTypesElement();
	}
	
	public void documentClosed(){
		removeArchTypesElement();
		tree.removeMouseListener(mouseAdapter);
		typesContextRef = null;
		xArchRef = null;
	}

	public void removeArchTypesElement(){
		DefaultMutableTreeNode rootNode = tree.getRootNode();
		
		DefaultMutableTreeNode archTypesNode = null;
		
		for(int i = 0; i < rootNode.getChildCount(); i++){
			TreeNode tn = rootNode.getChildAt(i);
			if(tn instanceof DefaultMutableTreeNode){
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)tn;
				String s = dmtn.toString();
				if(s.equals(TYPES_NODE_NAME)){
					rootNode.remove(dmtn);
					tree.refreshNode(rootNode);
				}
			}
		}
	}
	
	public ArchTypesTreeNode getArchTypesTreeNode(ObjRef ref){
		DefaultMutableTreeNode rootNode = tree.getRootNode();
		DefaultMutableTreeNode archTypesNode = null;
		DefaultMutableTreeNode componentTypesNode = null;
		DefaultMutableTreeNode connectorTypesNode = null;
		DefaultMutableTreeNode interfaceTypesNode = null;
		
		for(int i = 0; i < rootNode.getChildCount(); i++){
			TreeNode tn = rootNode.getChildAt(i);
			if(tn instanceof DefaultMutableTreeNode){
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)tn;
				String s = dmtn.toString();
				if(s.equals(TYPES_NODE_NAME)){
					archTypesNode = dmtn;
					
					for(i = 0; i < archTypesNode.getChildCount(); i++){
						TreeNode child = archTypesNode.getChildAt(i);
						if(child instanceof DefaultMutableTreeNode){
							DefaultMutableTreeNode dmtn2 = (DefaultMutableTreeNode)child;
							String s2 = dmtn2.toString();
							if(s2.equals(COMPONENT_TYPES_NODE_NAME)){
								componentTypesNode = dmtn2;
							}
							else if(s2.equals(CONNECTOR_TYPES_NODE_NAME)){
								connectorTypesNode = dmtn2;
							}
							else if(s2.equals(INTERFACE_TYPES_NODE_NAME)){
								interfaceTypesNode = dmtn2;
							}
						}
					}
					break;
				}
			}
		}
		if(archTypesNode == null){
			return null;
		}

		if(componentTypesNode != null){
			ArchTypesTreeNode retVal = getArchTypesTreeNode(componentTypesNode, ref);
			if(retVal != null) return retVal;
		}
		if(connectorTypesNode != null){
			ArchTypesTreeNode retVal = getArchTypesTreeNode(connectorTypesNode, ref);
			if(retVal != null) return retVal;
		}
		if(interfaceTypesNode != null){
			ArchTypesTreeNode retVal = getArchTypesTreeNode(interfaceTypesNode, ref);
			if(retVal != null) return retVal;
		}
		return null;
	}
	
	private DefaultMutableTreeNode getMainTypesNode(){
		DefaultMutableTreeNode rootNode = tree.getRootNode();
		for(int i = 0; i < rootNode.getChildCount(); i++){
			TreeNode tn = rootNode.getChildAt(i);
			if(tn instanceof DefaultMutableTreeNode){
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)tn;
				String s = dmtn.toString();
				if(s.equals(TYPES_NODE_NAME)){
					return dmtn;
				}
			}
		}
		return null;
	}
	
	private DefaultMutableTreeNode getTypesSubNode(DefaultMutableTreeNode archTypesTreeNode, String name){
		for(int i = 0; i < archTypesTreeNode.getChildCount(); i++){
			TreeNode tn = archTypesTreeNode.getChildAt(i);
			if(tn instanceof DefaultMutableTreeNode){
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)tn;
				String s = dmtn.toString();
				if(s.equals(name)){
					return dmtn;
				}
			}
		}
		return null;
	}

	private ArchTypesTreeNode getArchTypesTreeNode(DefaultMutableTreeNode parentNode, ObjRef ref){
		for(int i = 0; i < parentNode.getChildCount(); i++){
			TreeNode tn = parentNode.getChildAt(i);
			if(tn instanceof ArchTypesTreeNode){
				ArchTypesTreeNode attn = (ArchTypesTreeNode)tn;
				ObjRef attnRef = attn.getObjRef();
				if(ref.equals(attnRef)){
					return attn;
				}
			}
		}
		return null;
	}
		
	public void refreshArchTypesElement(){
		DefaultMutableTreeNode rootNode = tree.getRootNode();
		DefaultMutableTreeNode archTypesNode = null;
		DefaultMutableTreeNode componentTypesNode = null;
		DefaultMutableTreeNode connectorTypesNode = null;
		DefaultMutableTreeNode interfaceTypesNode = null;
		
		if(xArchRef == null){
			//There's no document open
			return;
		}
		//Technically there should only be one of these.
		ObjRef archTypesRef = xarch.getElement(typesContextRef, "archTypes", xArchRef);

		if(archTypesRef == null){
			//There's no ArchTypes element at all!
			removeArchTypesElement();
			tree.refreshNode(rootNode);
			tree.validate();
			tree.repaint();
			return;
		}

		boolean foundArchTypesNode = false;
		
		for(int i = 0; i < rootNode.getChildCount(); i++){
			TreeNode tn = rootNode.getChildAt(i);
			if(tn instanceof DefaultMutableTreeNode){
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)tn;
				String s = dmtn.toString();
				if(s.equals(TYPES_NODE_NAME)){
					archTypesNode = dmtn;
					
					for(i = 0; i < archTypesNode.getChildCount(); i++){
						TreeNode child = archTypesNode.getChildAt(i);
						if(child instanceof DefaultMutableTreeNode){
							DefaultMutableTreeNode dmtn2 = (DefaultMutableTreeNode)child;
							String s2 = dmtn2.toString();
							if(s2.equals(COMPONENT_TYPES_NODE_NAME)){
								componentTypesNode = dmtn2;
							}
							else if(s2.equals(CONNECTOR_TYPES_NODE_NAME)){
								connectorTypesNode = dmtn2;
							}
							else if(s2.equals(INTERFACE_TYPES_NODE_NAME)){
								interfaceTypesNode = dmtn2;
							}
						}
					}
					
					foundArchTypesNode = true;
					break;
				}
			}
		}
		if(archTypesNode == null){
			archTypesNode = new IconableTreeNode(TYPES_NODE_NAME);
			((IconableTreeNode)archTypesNode).setOverrideIsLeaf(false);
			((IconableTreeNode)archTypesNode).setIcon(TYPES_ICON);

			//archTypesNode = new DefaultMutableTreeNode(TYPES_NODE_NAME);
			WidgetUtils.addTreeNodeAlphabetically(rootNode, archTypesNode);
			
			componentTypesNode = new DefaultMutableTreeNode(COMPONENT_TYPES_NODE_NAME){
				public boolean isLeaf(){
					return false;
				}
			};

			WidgetUtils.addTreeNodeAlphabetically(archTypesNode, componentTypesNode);
			
			connectorTypesNode = new DefaultMutableTreeNode(CONNECTOR_TYPES_NODE_NAME){
				public boolean isLeaf(){
					return false;
				}
			};
			WidgetUtils.addTreeNodeAlphabetically(archTypesNode, connectorTypesNode);

			interfaceTypesNode = new DefaultMutableTreeNode(INTERFACE_TYPES_NODE_NAME){
				public boolean isLeaf(){
					return false;
				}
			};
			WidgetUtils.addTreeNodeAlphabetically(archTypesNode, interfaceTypesNode);
		}
		
		//Let's do the component types.
		ObjRef[] componentTypeRefs = xarch.getAll(archTypesRef, "ComponentType");
		refreshChildNode(componentTypesNode, componentTypeRefs, ArchTypesTreeNode.COMPONENT_TYPE);
		
		//Let's do the connector types.
		ObjRef[] connectorTypeRefs = xarch.getAll(archTypesRef, "ConnectorType");
		refreshChildNode(connectorTypesNode, connectorTypeRefs, ArchTypesTreeNode.CONNECTOR_TYPE);

		//Let's do the interface types.
		ObjRef[] interfaceTypeRefs = xarch.getAll(archTypesRef, "InterfaceType");
		refreshChildNode(interfaceTypesNode, interfaceTypeRefs, ArchTypesTreeNode.INTERFACE_TYPE);
		
		if(!foundArchTypesNode){
			tree.refreshNode(rootNode);
		}

		tree.refreshNode(componentTypesNode);
		tree.refreshNode(connectorTypesNode);
		tree.refreshNode(interfaceTypesNode);

		tree.validate();
		tree.repaint();
	}
	
	protected void refreshChildNode(DefaultMutableTreeNode parentNode, ObjRef[] typeRefs,
	int nodeType){
		java.util.List newTypeRefs = Arrays.asList(typeRefs);
		
		ArrayList oldTypeRefs = new ArrayList();
		for(int i = 0; i < parentNode.getChildCount(); i++){
			Object child = parentNode.getChildAt(i);
			if(child instanceof ArchTypesTreeNode){
				ObjRef ref = ((ArchTypesTreeNode)child).getObjRef();
				oldTypeRefs.add(ref);
			}
		}
		
		ArchipelagoUtils.ListDiff typeListDiff = ArchipelagoUtils.diffLists(oldTypeRefs, newTypeRefs);
		
		for(Iterator it = typeListDiff.getRemoveList().iterator(); it.hasNext(); ){
			ObjRef ref = (ObjRef)it.next();
			ArchTypesTreeNode attn = getArchTypesTreeNode(parentNode, ref);
			if(attn != null){
				parentNode.remove(attn);
			}
		}
		
		for(Iterator it = typeListDiff.getNoChangeList().iterator(); it.hasNext(); ){
			ObjRef ref = (ObjRef)it.next();
			ArchTypesTreeNode attn = getArchTypesTreeNode(parentNode, ref);
			if(attn != null){
				String description = edu.uci.ics.xadlutils.XadlUtils.getDescription(xarch, ref);
				if(description == null){
					description = "Type [No Description]";
				}
				parentNode.remove(attn);
				attn.setUserObject(description);
				WidgetUtils.addTreeNodeAlphabetically(parentNode, attn);
			}
		}
			
		for(Iterator it = typeListDiff.getAddList().iterator(); it.hasNext(); ){
			ObjRef ref = (ObjRef)it.next();
			
			String description = edu.uci.ics.xadlutils.XadlUtils.getDescription(xarch, ref);
			if(description == null){
				description = "Type [No Description]";
			}
				
			ArchTypesTreeNode newattn = new ArchTypesTreeNode(description);
			newattn.setObjRef(ref);
			newattn.setNodeType(nodeType);
			WidgetUtils.addTreeNodeAlphabetically(parentNode, newattn);
		}
	}
	
	public void showAndSelect(ArchTypesTreeNode node){
		TreePath tp = new TreePath(node.getPath());
		tree.expandPath(tp);
		tree.scrollPathToVisible(tp);
		tree.setSelectionPath(tp);
	}
	
	public ArchipelagoHintsInfo[] getHintsInfo(){
		java.util.List hintsInfoList = new ArrayList();
		//refreshArchTypesElement();
		DefaultMutableTreeNode mainArchTypesNode = getMainTypesNode();
		if(mainArchTypesNode != null){
			DefaultMutableTreeNode componentTypeNode = getTypesSubNode(mainArchTypesNode, COMPONENT_TYPES_NODE_NAME);
			for(int i = 0; i < componentTypeNode.getChildCount(); i++){
				TreeNode childNode = componentTypeNode.getChildAt(i);
				if(childNode instanceof ArchTypesTreeNode){
					ArchTypesTreeNode attn = (ArchTypesTreeNode)childNode;
					ObjRef attnRef = attn.getObjRef();
					BNAModel astnModel = attn.getBNAModel();
					if((attnRef != null) && (astnModel != null)){
						ArchipelagoHintsInfo ahi = new ArchipelagoHintsInfo(attnRef, astnModel);
						hintsInfoList.add(ahi);
					}
				}
			}

			DefaultMutableTreeNode connectorTypeNode = getTypesSubNode(mainArchTypesNode, CONNECTOR_TYPES_NODE_NAME);
			for(int i = 0; i < connectorTypeNode.getChildCount(); i++){
				TreeNode childNode = connectorTypeNode.getChildAt(i);
				if(childNode instanceof ArchTypesTreeNode){
					ArchTypesTreeNode attn = (ArchTypesTreeNode)childNode;
					ObjRef attnRef = attn.getObjRef();
					BNAModel astnModel = attn.getBNAModel();
					if((attnRef != null) && (astnModel != null)){
						ArchipelagoHintsInfo ahi = new ArchipelagoHintsInfo(attnRef, astnModel);
						hintsInfoList.add(ahi);
					}
				}
			}

			DefaultMutableTreeNode interfaceTypeNode = getTypesSubNode(mainArchTypesNode, INTERFACE_TYPES_NODE_NAME);
			for(int i = 0; i < interfaceTypeNode.getChildCount(); i++){
				TreeNode childNode = interfaceTypeNode.getChildAt(i);
				if(childNode instanceof ArchTypesTreeNode){
					ArchTypesTreeNode attn = (ArchTypesTreeNode)childNode;
					ObjRef attnRef = attn.getObjRef();
					BNAModel astnModel = attn.getBNAModel();
					if((attnRef != null) && (astnModel != null)){
						ArchipelagoHintsInfo ahi = new ArchipelagoHintsInfo(attnRef, astnModel);
						hintsInfoList.add(ahi);
					}
				}
			}
		}
		return (ArchipelagoHintsInfo[])hintsInfoList.toArray(new ArchipelagoHintsInfo[0]);
	}
	
	public void doOpen(ArchTypesTreeNode node){
		if(node != null){
			ArchTypesNavigationItem ni = new ArchTypesNavigationItem(node.getObjRef());
			ni.setIcon(node.getIcon());
			frame.addNavigationItem(ni);
		}
		doOpenDontChangeNav(node);
	}

	private void doOpenDontChangeNav(ArchTypesTreeNode node){
		frame.setRightComponent(null);
		frame.setWindowTitleElement(null);
		//thingIDMap.clearMaps();
		if(node != null){
			initFrame(node);
		}
	}

	protected void initFrame(ArchTypesTreeNode node){
		ScrollableBNAComponent typeScrollableBNAComponent = createTypeBNAComponent(node);
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add("Center", typeScrollableBNAComponent);
		rightPanel.add("South", ZoomWidget.getZoomWidget(typeScrollableBNAComponent));
		frame.setRightComponent(rightPanel);
		frame.setWindowTitleElement(node.toString());
	}
	
	public ScrollableBNAComponent createTypeBNAComponent(ArchTypesTreeNode node){
		BNAComponent typeBNAComponent = null;
		ThingIDMap thingIDMap;
		DefaultBNAModel typeBNAModel = null;
		
		if(node.getBNAModel() != null){
			typeBNAModel = (DefaultBNAModel)node.getBNAModel();
			thingIDMap = node.getThingIDMap();
		}
		else{
			typeBNAModel = new DefaultBNAModel();
			thingIDMap = new ThingIDMap();
			node.setBNAModel(typeBNAModel);
			node.setThingIDMap(thingIDMap);
		}
		
		typeBNAComponent = new BNAComponent("TypeBNA", typeBNAModel);
		typeBNAComponent.setBackground(Color.WHITE);
		typeBNAComponent.setProperty(THING_ID_MAP_PROPERTY_NAME, thingIDMap);
		
		//Antialiasing
		boolean antialiasText = ArchipelagoUtils.getAntialiasText(preferences);
		typeBNAComponent.setAntialiasText(antialiasText);

		boolean antialiasGraphics = ArchipelagoUtils.getAntialiasGraphics(preferences);
		typeBNAComponent.setAntialiasGraphics(antialiasGraphics);

		boolean gradientGraphics = ArchipelagoUtils.getGradientGraphics(preferences);
		typeBNAComponent.setGradientGraphics(gradientGraphics);

		Font defaultFont = ArchipelagoUtils.getDefaultFont(preferences);
		typeBNAComponent.setProperty("defaultFont", defaultFont);

		//Support Logics
		SelectionTrackingLogic stl = new SelectionTrackingLogic();
		typeBNAComponent.addThingLogic(stl);
		BoundingBoxTrackingLogic bbtl = new BoundingBoxTrackingLogic();
		typeBNAComponent.addThingLogic(bbtl);
		AnchorPointTrackingLogic aptl = new AnchorPointTrackingLogic();
		typeBNAComponent.addThingLogic(aptl);
		StickyBoxTrackingLogic sbtl = new StickyBoxTrackingLogic();
		typeBNAComponent.addThingLogic(sbtl);
		MouseTrackingLogic mtl = new MouseTrackingLogic();
		typeBNAComponent.addThingLogic(mtl);
		TagThingTrackingLogic ttl = new TagThingTrackingLogic();
		typeBNAComponent.addThingLogic(ttl);
		OffsetRotatorLogic orl = new OffsetRotatorLogic();
		typeBNAComponent.addThingLogic(orl);
		MultiDropTargetLogic mdtl = new MultiDropTargetLogic();
		typeBNAComponent.addThingLogic(mdtl);
		
		//Real logics
		typeBNAComponent.addThingLogic(new GlowboxOffsetRotatorLogic(orl));
		typeBNAComponent.addThingLogic(new TextReadabilityLogic());
		typeBNAComponent.addThingLogic(new DragMovableLogic());
		typeBNAComponent.addThingLogic(new CustomCursorLogic());
		typeBNAComponent.addThingLogic(new ToolTipLogic());
		typeBNAComponent.addThingLogic(new OneClickSelectionLogic());
		typeBNAComponent.addThingLogic(new BoxReshapeLogic(stl, bbtl));
		typeBNAComponent.addThingLogic(new MoveEndpointLogic(bbtl));
		typeBNAComponent.addThingLogic(new ToolTipLogic());
		typeBNAComponent.addThingLogic(new EphemeralLogic());
		typeBNAComponent.addThingLogic(new MoveIndicatorPointLogic(bbtl));
		typeBNAComponent.addThingLogic(new MaintainSignatureInterfaceMappingLogic());
		typeBNAComponent.addThingLogic(new MoveTogetherLogic(bbtl, aptl));
		EndpointTaggingLogic tl = new EndpointTaggingLogic(ttl);
		typeBNAComponent.addThingLogic(tl);
		typeBNAComponent.addThingLogic(new RotaterThingLogic());

		//Context Menu Logics
		SelectionBasedContextMenuLogic sbcml = new SelectionBasedContextMenuLogic(stl);
		sbcml.addPlugin(new EditDescriptionContextMenuPlugin(typeBNAComponent, thingIDMap, xarch));
		sbcml.addPlugin(new EditDirectionContextMenuPlugin(typeBNAComponent, thingIDMap, xarch));
		sbcml.addPlugin(new NewSignatureContextMenuPlugin(typeBNAComponent, node.getObjRef(), thingIDMap, xarch));
		sbcml.addPlugin(new GotoStructureContextMenuPlugin(typeBNAComponent, thingIDMap, xarch, getArchStructureTreePlugin()));
		sbcml.addPlugin(new ClearSubarchitectureContextMenuPlugin(typeBNAComponent, node.getObjRef(), thingIDMap, xarch));
		sbcml.addPlugin(new NewSIMContextMenuPlugin(typeBNAComponent, thingIDMap, xarch));
		sbcml.addPlugin(new RemoveTypesElementContextMenuPlugin(typeBNAComponent, thingIDMap, xarch));
		sbcml.addPlugin(new AssignColorsContextMenuPlugin(typeBNAComponent, null, this, thingIDMap));
		sbcml.addPlugin(new EndpointTagContextMenuPlugin(typeBNAComponent, tl, ttl, mtl));
		sbcml.addPlugin(new RotateContextMenuPlugin(typeBNAComponent));
		
		//sbcml.addPlugin(new RemoveElementContextMenuPlugin(structureBNAComponent, thingIDMap, xarch));
		typeBNAComponent.addThingLogic(sbcml);
		
		mdtl.addDropTargetLogic(new SetSubarchitectureDropTargetLogic(thingIDMap, xarch));
		mdtl.addDropTargetLogic(new SetTypeDropTargetLogic(thingIDMap, xarch));
		
		//Local Logics for Swing Integration
		typeBNAComponent.addThingLogic(new LocalDragMovableLogic());
		LocalBoundingBoxTrackingLogic lbbtl = new LocalBoundingBoxTrackingLogic();
		typeBNAComponent.addThingLogic(lbbtl);
		typeBNAComponent.addThingLogic(new LocalBoxReshapeLogic(stl, lbbtl));
		typeBNAComponent.addThingLogic(new ScrollLocalBoxBoundedThingLogic());
		
		//Main Menu Logics
		final IPreferences finalPreferences = preferences;
		IRecentDirectory recentDirectory = new IRecentDirectory(){
			public String getRecentDirectory(){
				return RecentDirectoryPreferenceUtils.getRecentDirectory(finalPreferences, "archipelagoBitmap");
			}
			public void setRecentDirectory(String dir){
				RecentDirectoryPreferenceUtils.storeRecentDirectory(finalPreferences, "archipelagoBitmap", dir);
			}
		};

		typeBNAComponent.addThingLogic(new SaveBitmapMainMenuLogic(frame.getJMenuBar(), recentDirectory));

		typeBNAComponent.addThingLogic(new ElementIndicatorLogic(thingIDMap));
		
		renderingHints = frame.getRenderingHints();
		/*
		ProgressDialog pd = new ProgressDialog(frame, "Loading Rendering Hints", "Loading Hints");
		pd.doPopup();
		renderingHints = frame.getRenderingHints(url, pd.getProgressBar());
		pd.doDone();
		*/
		//Done mapping logics

		TypeMappingLogic typeMappingLogic = new TypeMappingLogic(
			this,
			node.getObjRef(),
			typeBNAModel,
			xarch,
			thingIDMap,
			renderingHints);
		//addMappingLogic(typeMappingLogic);
		typeBNAComponent.getModel().addBNAModelListener(typeMappingLogic);
		typeBNAComponent.setProperty("typeRef", node.getObjRef());
		registerMappingLogic(typeBNAComponent, typeMappingLogic);
		//typeBNAComponent.setProperty(TYPE_MAPPING_LOGIC_PROPERTY_NAME, typeMappingLogic);
		
		PropertyTableLogic propertyTableLogic = new PropertyTableLogic(typeBNAModel, xarch, thingIDMap);
		propertyTableLogic.addPropertyTablePlugin(new ApplyThemeThemePropertyTablePlugin(xarch));
		propertyTableLogic.addPropertyTablePlugin(new DescriptionPropertyTablePlugin(xarch));
		propertyTableLogic.addPropertyTablePlugin(new TypePropertyTablePlugin(xarch));
		typeBNAComponent.getModel().addBNAModelListener(propertyTableLogic);
		registerMappingLogic(typeBNAComponent, propertyTableLogic);
		typeBNAComponent.addThingLogic(propertyTableLogic);
		
		//Load environment properties either from the extant
		//thing in the model or from the rendering hints.
		EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(typeBNAModel);
		if(ept == null){
			//System.out.println("Environment properties thing for structure: " + node.toString() + " was null.");
			ept = new EnvironmentPropertiesThing();
			ObjRef nodeRef = node.getObjRef();
			if(nodeRef != null){
				String nodeID = XadlUtils.getID(xarch, nodeRef);
				if(nodeID != null){
					//System.out.println("ept before: " + ept);
					renderingHints.applyRenderingHints(nodeID + "$$environmentProperties", ept);
					//System.out.println("ept after: " + ept);
					typeBNAModel.addThing(ept);
				}
			}
		}
		if(ept != null){
			try{
				int wox = ept.getWorldOriginX();
				int woy = ept.getWorldOriginY();
				double scale = ept.getScale();
				typeBNAComponent.repositionAbsolute(wox, woy);
				typeBNAComponent.rescaleAbsolute(scale);
			}
			catch(Exception e){
			}
		}

		//This has to come after the above or else you'll always get the environment
		//properties thing created by this logic on init.
		typeBNAComponent.addThingLogic(new MaintainEnvironmentPropertiesLogic());

		final BNAComponent ftbc = typeBNAComponent;
		final ArchTypesTreeNode fNode = node;
		typeBNAComponent.addAncestorListener(new javax.swing.event.AncestorListener(){
			public void ancestorAdded(AncestorEvent arg0){}
			public void ancestorMoved(AncestorEvent arg0){}
			public void ancestorRemoved(AncestorEvent arg0){
				destroyTypeBNAComponent(ftbc);
				EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(ftbc.getModel());
				if(ept != null){
					ObjRef nodeRef = fNode.getObjRef();
					if(nodeRef != null){
						String id = XadlUtils.getID(xarch, nodeRef);
						ept.setProperty(IHinted.XARCHID_PROPERTY_NAME, id + "$$environmentProperties");
					}
				}
			}
		}
		);

		ScrollableBNAComponent typeScrollableBNAComponent = new 
			ScrollableBNAComponent(typeBNAComponent, true, true, true);

		typeScrollableBNAComponent.invalidate();
		typeScrollableBNAComponent.validate();
		typeScrollableBNAComponent.repaint();

		fireTypeBNAComponentCreated(node, typeBNAComponent);
		initType(xArchRef, node.getObjRef(), typeMappingLogic);
		renderingHints.applyIndependentHints(typeBNAModel);
		return typeScrollableBNAComponent;
	}
	
	/*
	private void storeEnvironmentPropertiesThing(BNAComponent typeBNAComponent){
		int wox = typeBNAComponent.getWorldOriginX();
		int woy = typeBNAComponent.getWorldOriginY();
		double scale = typeBNAComponent.getScale();

		EnvironmentPropertiesThing ept = EnvironmentPropertiesThing.getEnvironmentPropertiesThing(typeBNAComponent.getModel());
		boolean foundEpt = true;
		if(ept == null){
			foundEpt = false;
			ept = new EnvironmentPropertiesThing();
		}
		ept.setWorldOriginX(wox);
		ept.setWorldOriginY(woy);
		ept.setScale(scale);
		if(!foundEpt){
			typeBNAComponent.getModel().addThing(ept);
		}
	}
	*/
	
	public void destroyTypeBNAComponent(BNAComponent typeBNAComponent){
		MappingLogic[] mls = getAllMappingLogics(typeBNAComponent);
		for(int i = 0; i < mls.length; i++){
			unregisterMappingLogic(typeBNAComponent, mls[i]);
		}

		int wox = typeBNAComponent.getWorldOriginX();
		int woy = typeBNAComponent.getWorldOriginY();
		double scale = typeBNAComponent.getScale();

		//storeEnvironmentPropertiesThing(typeBNAComponent);

		ArrayList thingsToRemove = new ArrayList();
		for(Iterator it = typeBNAComponent.getModel().getThingIterator(); it.hasNext(); ){
			Thing t = (Thing)it.next();
			if(t instanceof IEphemeral){
				thingsToRemove.add(t);
			}
			else if(t instanceof SwingPanelThing){
				thingsToRemove.add(t);
			}
		}
		for(Iterator it = thingsToRemove.iterator(); it.hasNext();){
			Thing doomedThing = (Thing)it.next();
			typeBNAComponent.getModel().removeThing(doomedThing);
		}
		
		SelectionUtils.removeAllSelections(typeBNAComponent);

		fireTypeBNAComponentDestroying(typeBNAComponent);
		typeBNAComponent.destroy();
		fireTypeBNAComponentDestroyed(typeBNAComponent);
		typeBNAComponent = null;
	}
	
	protected void initType(ObjRef xArchRef, ObjRef typeRef, TypeMappingLogic typeMappingLogic){
		ObjRef typesContextRef = xarch.createContext(xArchRef, "Types");
		
		typeMappingLogic.updateType(typeRef);
	}
	
	public void handleXArchFlatEvent(XArchFlatEvent evt){
		if(xArchRef == null){
			return;
		}
		
		XArchPath sourcePath = evt.getSourcePath();
		String sourcePathString = (sourcePath == null) ? null : sourcePath.toTagsOnlyString();
		
		XArchPath targetPath = evt.getTargetPath();
		String targetPathString = (targetPath == null) ? null : targetPath.toTagsOnlyString();

		if((sourcePathString != null) && (sourcePathString.equals("xArch"))){
			refreshArchTypesElement();
		}
		else if((sourcePathString != null) && (sourcePathString.equals("xArch/archTypes"))){
			refreshArchTypesElement();
		}		
		else if((sourcePathString != null) && (sourcePathString.equals("xArch/archTypes/componentType/description"))){
			refreshArchTypesElement();
		}
		else if((sourcePathString != null) && (sourcePathString.equals("xArch/archTypes/connectorType/description"))){
			refreshArchTypesElement();
		}
		else if((sourcePathString != null) && (sourcePathString.equals("xArch/archTypes/interfaceType/description"))){
			refreshArchTypesElement();
		}

		java.awt.Component[] children = WidgetUtils.getHierarchyRecursive(getArchipelagoFrame().getRightComponent());
		for(int i = 0; i < children.length; i++){
			if(children[i] instanceof BNAComponent){
				BNAComponent currentBNAComponent = (BNAComponent)children[i];
				if(currentBNAComponent.getID().equals("TypeBNA")){
					MappingLogic[] mls = getAllMappingLogics(currentBNAComponent);
					for(int j = 0; j < mls.length; j++){
						mls[j].handleXArchFlatEvent(evt);
					}
					break;
				}
			}
		}
		
	}
	
	public void handleXArchFileEvent(XArchFileEvent evt){
		if(xArchRef == null){
			return;
		}
		
		java.awt.Component[] children = WidgetUtils.getHierarchyRecursive(getArchipelagoFrame().getRightComponent());
		for(int i = 0; i < children.length; i++){
			if(children[i] instanceof BNAComponent){
				BNAComponent currentBNAComponent = (BNAComponent)children[i];
				if(currentBNAComponent.getID().equals("TypeBNA")){
					MappingLogic[] mls = getAllMappingLogics(currentBNAComponent);
					for(int j = 0; j < mls.length; j++){
						mls[j].handleXArchFileEvent(evt);
					}
					break;
				}
			}
		}
	}
	
	public boolean shouldAllowDrag(TreeNode node){
		java.awt.Component c = frame.getRightComponent();
		BNAComponent bnaComponent = null;
		
		java.awt.Component[] allComponents = WidgetUtils.getHierarchyRecursive(c);
		for(int i = 0; i < allComponents.length; i++){
			if(allComponents[i] instanceof BNAComponent){
				bnaComponent = (BNAComponent)allComponents[i];
				break;
			}
		}
		
		if((bnaComponent != null) && (bnaComponent.getID() != null) && (bnaComponent.getID().equals("TypeBNA"))){
			if(node instanceof ArchStructureTreeNode){
				return true; 
			}
			else if(node instanceof ArchTypesTreeNode){
				if(((ArchTypesTreeNode)node).getNodeType() == ArchTypesTreeNode.INTERFACE_TYPE){
					return true;
				}
			}
		}
		return false;
	}
	
	public DragInfo getDragInfo(TreeNode node){
		ObjRef ref = null;
		if(node instanceof ArchStructureTreeNode){
			ArchStructureTreeNode astn = (ArchStructureTreeNode)node;
			ref = astn.getObjRef();
		}
		else if(node instanceof ArchTypesTreeNode){
			ArchTypesTreeNode attn = (ArchTypesTreeNode)node;
			ref = attn.getObjRef();
		}
		
		java.awt.datatransfer.Transferable t = new ObjRefTransferable(ref, xarch.getXArchPath(ref));
		java.awt.Cursor c = java.awt.dnd.DragSource.DefaultLinkDrop;
		java.awt.dnd.DragSourceListener dsl = new DSListener();
		DragInfo dragInfo = new DragInfo(t, c, dsl);
		return dragInfo;
	}
	
	class DSListener implements java.awt.dnd.DragSourceListener{
		public void dragDropEnd(java.awt.dnd.DragSourceDropEvent dsde){
			if(dsde.getDropSuccess() == false){
			}
		} 
		public void dragEnter(java.awt.dnd.DragSourceDragEvent dsde){}
		public void dragExit(java.awt.dnd.DragSourceEvent dse){}
		public void dragOver(java.awt.dnd.DragSourceDragEvent dsde){}
		public void dropActionChanged(java.awt.dnd.DragSourceDragEvent dsde){}
	}
	
	public JMenuItem[] getPopupMenuItems(TreeNode tn){
		if(tn == null) return null;
		
		if(tn instanceof ArchTypesTreeNode){
			ArchTypesTreeNodeMenuItemSet miSet = new ArchTypesTreeNodeMenuItemSet(xarch, (ArchTypesTreeNode)tn);
			return miSet.getMenuItems();
		}
		else if(tn instanceof DefaultMutableTreeNode){
			TreeNode rootNode = tree.getRootNode();
			if(tn == rootNode){
				RootTreeNodeMenuItemSet miSet = new RootTreeNodeMenuItemSet(xarch, (DefaultMutableTreeNode)tn, xArchRef);
				return miSet.getMenuItems();
			}
			
			DefaultMutableTreeNode mainTypesNode = getMainTypesNode();
			if(mainTypesNode != null){
				ObjRef archTypesRef = xarch.getElement(typesContextRef, "archTypes", xArchRef);

				DefaultMutableTreeNode componentTypesNode = getTypesSubNode(mainTypesNode, COMPONENT_TYPES_NODE_NAME);
				if((componentTypesNode != null) && (componentTypesNode == tn)){
					TypeParentTreeNodeMenuItemSet miSet = new TypeParentTreeNodeMenuItemSet(xarch, (DefaultMutableTreeNode)tn, archTypesRef);
					return miSet.getMenuItems();
				}

				DefaultMutableTreeNode connectorTypesNode = getTypesSubNode(mainTypesNode, CONNECTOR_TYPES_NODE_NAME);
				if((connectorTypesNode != null) && (connectorTypesNode == tn)){
					TypeParentTreeNodeMenuItemSet miSet = new TypeParentTreeNodeMenuItemSet(xarch, (DefaultMutableTreeNode)tn, archTypesRef);
					return miSet.getMenuItems();
				}

				DefaultMutableTreeNode interfaceTypesNode = getTypesSubNode(mainTypesNode, INTERFACE_TYPES_NODE_NAME);
				if((interfaceTypesNode != null) && (interfaceTypesNode == tn)){
					TypeParentTreeNodeMenuItemSet miSet = new TypeParentTreeNodeMenuItemSet(xarch, (DefaultMutableTreeNode)tn, archTypesRef);
					return miSet.getMenuItems();
				}
			}
		}
		return null;
	}
	
	class DoubleClickMouseAdapter extends MouseAdapter{
		public void mouseClicked(MouseEvent e){
			if(e.getButton() == MouseEvent.BUTTON1){
				if(e.getClickCount() == 2){
					int selRow = tree.getRowForLocation(e.getX(), e.getY());
					TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
					if(selRow != -1) {
						Object o = selPath.getLastPathComponent();
						if((o != null) && (o instanceof ArchTypesTreeNode)){
							ArchTypesTreeNode node = (ArchTypesTreeNode)o;
							doOpen(node);
						}
					}
				}
			}
		}
	}
	
	static class RootTreeNodeMenuItemSet implements ActionListener{
		protected XArchFlatInterface xarch;
		protected DefaultMutableTreeNode tn;
		protected ObjRef xArchRef;
		protected JMenuItem miCreateTypesNode;
		protected java.util.List menuItems = new ArrayList();
		
		public RootTreeNodeMenuItemSet(XArchFlatInterface xarch, DefaultMutableTreeNode tn, 
		ObjRef xArchRef){
			this.xarch = xarch;
			this.tn = tn;
			this.xArchRef = xArchRef;
			init();
		}
		
		public JMenuItem[] getMenuItems(){
			return (JMenuItem[])menuItems.toArray(new JMenuItem[0]);
		}
		
		protected void init(){
			miCreateTypesNode = new JMenuItem("Create Type Set");
			WidgetUtils.setMnemonic(miCreateTypesNode, 'N');
			miCreateTypesNode.addActionListener(this);
			
			ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
			ObjRef archTypesRef = xarch.getElement(typesContextRef, "archTypes", xArchRef);
			if(archTypesRef != null){
				miCreateTypesNode.setEnabled(false);
			}

			menuItems.add(miCreateTypesNode);
		}
		
		public void actionPerformed(ActionEvent evt){
			if(evt.getSource() == miCreateTypesNode){
				newTypesNode();
			}
		}
		
		protected void newTypesNode(){
			ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
			ObjRef archTypesRef = xarch.createElement(typesContextRef, "archTypes");
			xarch.add(xArchRef, "Object", archTypesRef);
		}
	}

	static class TypeParentTreeNodeMenuItemSet implements ActionListener{
		protected XArchFlatInterface xarch;
		protected DefaultMutableTreeNode tn;
		protected ObjRef archTypesRef;
		protected JMenuItem miNewType;

		protected java.util.List menuItems = new ArrayList();
		
		public TypeParentTreeNodeMenuItemSet(XArchFlatInterface xarch, DefaultMutableTreeNode tn, 
		ObjRef archTypesRef){
			this.xarch = xarch;
			this.tn = tn;
			this.archTypesRef = archTypesRef;
			init();
		}
		
		public JMenuItem[] getMenuItems(){
			return (JMenuItem[])menuItems.toArray(new JMenuItem[0]);
		}
		
		protected void init(){
			String newTypeKind = "";
			if(tn.toString().equals(COMPONENT_TYPES_NODE_NAME)){
				newTypeKind = "Component";
			}
			else if(tn.toString().equals(CONNECTOR_TYPES_NODE_NAME)){
				newTypeKind = "Connector";
			}
			else if(tn.toString().equals(INTERFACE_TYPES_NODE_NAME)){
				newTypeKind = "Interface";
			}
			
			miNewType = new JMenuItem("New " + newTypeKind + " Type");
			WidgetUtils.setMnemonic(miNewType, 'N');
			miNewType.addActionListener(this);

			menuItems.add(miNewType);
		}
		
		public void actionPerformed(ActionEvent evt){
			if(evt.getSource() == miNewType){
				newType();
			}
		}
		
		protected void newType(){
			if(tn.toString().equals(COMPONENT_TYPES_NODE_NAME)){
				ObjRef xArchRef = xarch.getXArch(archTypesRef);
				ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
				ObjRef newTypeRef = xarch.create(typesContextRef, "componentType");
				String newID = UIDGenerator.generateUID("componentType");
				xarch.set(newTypeRef, "id", newID);
				ObjRef descriptionRef = xarch.create(typesContextRef, "Description");
				xarch.set(descriptionRef, "value", "(New Component Type)");
				xarch.set(newTypeRef, "Description", descriptionRef);
				xarch.add(archTypesRef, "componentType", newTypeRef);
			}
			else if(tn.toString().equals(CONNECTOR_TYPES_NODE_NAME)){
				ObjRef xArchRef = xarch.getXArch(archTypesRef);
				ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
				ObjRef newTypeRef = xarch.create(typesContextRef, "connectorType");
				String newID = UIDGenerator.generateUID("connectorType");
				xarch.set(newTypeRef, "id", newID);
				ObjRef descriptionRef = xarch.create(typesContextRef, "Description");
				xarch.set(descriptionRef, "value", "(New Connector Type)");
				xarch.set(newTypeRef, "Description", descriptionRef);
				xarch.add(archTypesRef, "connectorType", newTypeRef);
			}
			else if(tn.toString().equals(INTERFACE_TYPES_NODE_NAME)){
				ObjRef xArchRef = xarch.getXArch(archTypesRef);
				ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
				ObjRef newTypeRef = xarch.create(typesContextRef, "interfaceType");
				String newID = UIDGenerator.generateUID("interfaceType");
				xarch.set(newTypeRef, "id", newID);
				ObjRef descriptionRef = xarch.create(typesContextRef, "Description");
				xarch.set(descriptionRef, "value", "(New Interface Type)");
				xarch.set(newTypeRef, "Description", descriptionRef);
				xarch.add(archTypesRef, "interfaceType", newTypeRef);
			}
		}
	}
	
	static class ArchTypesTreeNodeMenuItemSet implements ActionListener{
		protected XArchFlatInterface xarch;
		protected ArchTypesTreeNode tn;
		
		protected JMenuItem miRemove;
		
		protected java.util.List menuItems = new ArrayList();

		public ArchTypesTreeNodeMenuItemSet(XArchFlatInterface xarch, ArchTypesTreeNode tn){
			this.xarch = xarch;
			this.tn = tn;
			init();
		}
		
		protected void init(){
			miRemove = new JMenuItem("Remove Type");
			WidgetUtils.setMnemonic(miRemove, 'R');
			miRemove.addActionListener(this);

			menuItems.add(miRemove);
		}
		
		public JMenuItem[] getMenuItems(){
			return (JMenuItem[])menuItems.toArray(new JMenuItem[0]);
		}

		public void actionPerformed(ActionEvent evt){
			if(evt.getSource() == miRemove){
				remove();
			}
		}
		
		protected void remove(){
			ObjRef typeRef = tn.getObjRef();
			ObjRef archTypesRef = xarch.getParent(typeRef);
			int nodeType = tn.getNodeType();
			switch(nodeType){
				case ArchTypesTreeNode.COMPONENT_TYPE:
					xarch.remove(archTypesRef, "componentType", typeRef);
					break;
				case ArchTypesTreeNode.CONNECTOR_TYPE:
					xarch.remove(archTypesRef, "connectorType", typeRef);
					break;
				case ArchTypesTreeNode.INTERFACE_TYPE:
					xarch.remove(archTypesRef, "interfaceType", typeRef);
					break;
			}
		}
	}
	
	public boolean navigateTo(NavigationItem ni){
		if(!(ni instanceof ArchTypesNavigationItem)){
			return false;
		}
		ArchTypesNavigationItem ani = (ArchTypesNavigationItem)ni;
		ObjRef ref = ani.getRef();
		ArchTypesTreeNode tn = getArchTypesTreeNode(ref);
		doOpenDontChangeNav(tn); 	//works ok even if tn == null
		return true;
	}
	
	class ArchTypesNavigationItem extends NavigationItem{
		private ObjRef ref;
		
		public ArchTypesNavigationItem(ObjRef ref){
			super(null, ref);
			this.ref = ref;
		}
		
		public ObjRef getRef(){
			return ref;
		}
		
		public String getDescription(){
			String description = XadlUtils.getDescription(xarch, ref);
			if(description == null){
				description = "Type (No Description)";
			}
			return description;
		}
	}
	
	public void handle(c2.fw.Message m){
		java.awt.Component[] children = WidgetUtils.getHierarchyRecursive(getArchipelagoFrame().getRightComponent());
		for(int i = 0; i < children.length; i++){
			if(children[i] instanceof BNAComponent){
				BNAComponent currentBNAComponent = (BNAComponent)children[i];
				if(currentBNAComponent.getID().equals("TypeBNA")){
					ThingLogic[] thingLogics = currentBNAComponent.getThingLogics();
					for(int j = 0; j < thingLogics.length; j++){
						if(thingLogics[j] instanceof c2.fw.MessageProcessor){
							((c2.fw.MessageProcessor)thingLogics[j]).handle(m);
						}
					}
					break;
				}
			}
		}
	}
	
	public boolean showRef(ObjRef ref, XArchPath path){
		if(ref == null){
			return false;
		}
		String pathString = path.toTagsOnlyString();
		if(pathString.equals("xArch/archTypes")){
			ArchTypesTreeNode treeNode = getArchTypesTreeNode(ref);
			if(treeNode != null){
				showAndSelect(treeNode);
				doOpen(treeNode);
				return true;
			}
		}
		else if(pathString.startsWith("xArch/archTypes")){
			ObjRef[] ancestors = xarch.getAllAncestors(ref);
			ObjRef structureRef = ancestors[ancestors.length - 2];
			ArchTypesTreeNode treeNode = getArchTypesTreeNode(ref);
			if(treeNode != null){
				showAndSelect(treeNode);
				doOpen(treeNode);
				return true;
			}
		}
		return false;
	}

	public Color getDefaultComponentTypeColor(){
		IPreferences preferences = getPreferences();
		
		Color defaultComponentTypeColor = TypeMappingLogic.DEFAULT_COMPONENT_TYPE_COLOR;
		if(preferences.keyExists(IPreferences.USER_SPACE, 
		"/archstudio/comp/archipelago/types", "defaultComponentTypeColor")){
			int val = preferences.getIntValue(IPreferences.USER_SPACE, 
			"/archstudio/comp/archipelago/types", "defaultComponentTypeColor", -1);
			defaultComponentTypeColor = new Color(val);
		}
		return defaultComponentTypeColor;
	}
	
	public Color getDefaultConnectorTypeColor(){
		IPreferences preferences = getPreferences();
		
		Color defaultConnectorTypeColor = TypeMappingLogic.DEFAULT_CONNECTOR_TYPE_COLOR;
		if(preferences.keyExists(IPreferences.USER_SPACE, 
		"/archstudio/comp/archipelago/types", "defaultConnectorTypeColor")){
			int val = preferences.getIntValue(IPreferences.USER_SPACE, 
			"/archstudio/comp/archipelago/types", "defaultConnectorTypeColor", -1);
			defaultConnectorTypeColor = new Color(val);
		}
		return defaultConnectorTypeColor;
	}

}

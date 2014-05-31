package archstudio.comp.archipelago.types;

import edu.uci.ics.widgets.WidgetUtils;
import edu.uci.ics.xarchutils.ObjRef;
import archstudio.comp.archipelago.*;
import archstudio.comp.archipelago.types.et.*;
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

public class ArchStructureTreePlugin extends AbstractArchipelagoTreePlugin {

	public static final String STRUCTURES_NODE_NAME = "Structures";
	
	public static final String THING_ID_MAP_PROPERTY_NAME = "thingIDMap";
	//public static final String BRICK_MAPPING_LOGIC_PROPERTY_NAME = "brickMappingLogic";
	//public static final String LINK_MAPPING_LOGIC_PROPERTY_NAME = "linkMappingLogic";
	//public static final String PROPERTY_TABLE_LOGIC_PROPERTY_NAME = "propertyTableLogic";
	
	public static final Icon STRUCTURE_ICON;
	
	static{
		STRUCTURE_ICON = edu.uci.ics.widgets.WidgetUtils.getImageIcon("archstudio/comp/typewrangler/res/structure.gif");
	}

	protected ObjRef xArchRef = null;
	protected ObjRef typesContextRef = null;
	protected ObjRef lastViewedStructure = null;
	protected DoubleClickMouseAdapter mouseAdapter = null;
	protected IGraphLayout gli = null;

	protected archstudio.comp.archipelago.RenderingHints renderingHints = null;

	protected ArchTypesTreePlugin archTypesTreePlugin = null;
	
	protected Vector archStructureTreePluginListeners = new Vector();

	public ArchStructureTreePlugin(MessageSendProxy topIfaceSender, MessageSendProxy bottomIfaceSender,
	ArchipelagoFrame frame, ArchipelagoTree tree,
	XArchFlatTransactionsInterface xarch, IPreferences preferences, 
	IGraphLayout gli, ArchTypesTreePlugin atp){
		super(topIfaceSender, bottomIfaceSender, frame, tree, xarch, preferences);
		this.gli = gli;
		this.archTypesTreePlugin = atp;
		archTypesTreePlugin.setArchStructureTreePlugin(this);
		
	}
	
	public void addArchStructureTreePluginListener(ArchStructureTreePluginListener l){
		archStructureTreePluginListeners.addElement(l);
	}
	
	public void removeArchStructureTreePluginListener(ArchStructureTreePluginListener l){
		archStructureTreePluginListeners.removeElement(l);
	}
	
	protected void fireStructureBNAComponentCreated(ArchStructureTreeNode tn,
	BNAComponent bnaComponent){
		synchronized(archStructureTreePluginListeners){
			for(Iterator it = archStructureTreePluginListeners.iterator(); it.hasNext(); ){
				((ArchStructureTreePluginListener)it.next()).structureBNAComponentCreated(tn, 
					bnaComponent);
			}
		}
	}

	protected void fireStructureBNAComponentDestroying(BNAComponent bnaComponent){
		synchronized(archStructureTreePluginListeners){
			for(Iterator it = archStructureTreePluginListeners.iterator(); it.hasNext(); ){
				((ArchStructureTreePluginListener)it.next()).structureBNAComponentDestroying(
					bnaComponent);
			}
		}
	}

	protected void fireStructureBNAComponentDestroyed(BNAComponent bnaComponent){
		synchronized(archStructureTreePluginListeners){
			for(Iterator it = archStructureTreePluginListeners.iterator(); it.hasNext(); ){
				((ArchStructureTreePluginListener)it.next()).structureBNAComponentDestroyed(
					bnaComponent);
			}
		}
	}
	
	public ArchTypesTreePlugin getArchTypesTreePlugin(){
		return archTypesTreePlugin;
	}
	
	public void documentOpened(ObjRef xArchRef, ObjRef elementRef) {
		this.xArchRef = xArchRef;
		//When a document is opened, we want to populate the tree
		//with an ArchStructure element.
		//this.url = url;
		//xArchRef = xarch.getOpenXArch(url);
		if(xArchRef != null){
			typesContextRef = xarch.createContext(xArchRef, "types");
		}
		mouseAdapter = new DoubleClickMouseAdapter();
		tree.addMouseListener(mouseAdapter);
		refreshArchStructureElement();
	}
	
	public void documentClosed(){
		removeArchStructureElement();
		tree.removeMouseListener(mouseAdapter);
		typesContextRef = null;
		xArchRef = null;
		//url = null;
	}

	public void removeArchStructureElement(){
		DefaultMutableTreeNode rootNode = tree.getRootNode();
		
		DefaultMutableTreeNode archStructuresNode = null;
		
		for(int i = 0; i < rootNode.getChildCount(); i++){
			TreeNode tn = rootNode.getChildAt(i);
			if(tn instanceof DefaultMutableTreeNode){
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)tn;
				String s = dmtn.toString();
				if(s.equals(STRUCTURES_NODE_NAME)){
					rootNode.remove(dmtn);
					tree.refreshNode(rootNode);
				}
			}
		}
	}
	
	public ArchStructureTreeNode getArchStructureTreeNode(ObjRef ref){
		DefaultMutableTreeNode rootNode = tree.getRootNode();
		
		DefaultMutableTreeNode archStructuresNode = null;
		
		for(int i = 0; i < rootNode.getChildCount(); i++){
			TreeNode tn = rootNode.getChildAt(i);
			if(tn instanceof DefaultMutableTreeNode){
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)tn;
				String s = dmtn.toString();
				if(s.equals(STRUCTURES_NODE_NAME)){
					return getArchStructureTreeNode(dmtn, ref);
				}
			}
		}
		return null;
	}
	
	public void showAndSelect(ArchStructureTreeNode node){
		TreePath tp = new TreePath(node.getPath());
		tree.expandPath(tp);
		tree.scrollPathToVisible(tp);
		tree.setSelectionPath(tp);
	}
	
	private ArchStructureTreeNode getArchStructureTreeNode(DefaultMutableTreeNode archStructuresNode, ObjRef ref){
		for(int i = 0; i < archStructuresNode.getChildCount(); i++){
			TreeNode tn = archStructuresNode.getChildAt(i);
			if(tn instanceof ArchStructureTreeNode){
				ArchStructureTreeNode astn = (ArchStructureTreeNode)tn;
				ObjRef astnRef = astn.getObjRef();
				if(ref.equals(astnRef)){
					return astn;
				}
			}
		}
		return null;
	}
	
	public DefaultMutableTreeNode getMainArchStructuresNode(){
		DefaultMutableTreeNode rootNode = tree.getRootNode();
		DefaultMutableTreeNode archStructuresNode = null;
		
		for(int i = 0; i < rootNode.getChildCount(); i++){
			TreeNode tn = rootNode.getChildAt(i);
			if(tn instanceof DefaultMutableTreeNode){
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)tn;
				String s = dmtn.toString();
				if(s.equals(STRUCTURES_NODE_NAME)){
					return dmtn;
				}
			}
		}
		return null;
	}
		
	public void refreshArchStructureElement(){
		DefaultMutableTreeNode rootNode = tree.getRootNode();
		DefaultMutableTreeNode archStructuresNode = null;
		
		if(xArchRef == null){
			//There's no document open at all
			return;
		}
		
		boolean foundArchStructuresNode = false;
		
		for(int i = 0; i < rootNode.getChildCount(); i++){
			TreeNode tn = rootNode.getChildAt(i);
			if(tn instanceof DefaultMutableTreeNode){
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)tn;
				String s = dmtn.toString();
				if(s.equals(STRUCTURES_NODE_NAME)){
					archStructuresNode = dmtn;
					foundArchStructuresNode = true;
					break;
				}
			}
		}
		if(archStructuresNode == null){
			archStructuresNode = new IconableTreeNode(STRUCTURES_NODE_NAME);
			((IconableTreeNode)archStructuresNode).setOverrideIsLeaf(false);
			((IconableTreeNode)archStructuresNode).setIcon(STRUCTURE_ICON);
			
			/*
			archStructuresNode = new DefaultMutableTreeNode(STRUCTURES_NODE_NAME){
				public boolean isLeaf(){
					return false;
				}
			};
			*/
			WidgetUtils.addTreeNodeAlphabetically(rootNode, archStructuresNode);
		}
		
		ObjRef[] archStructureRefs = xarch.getAllElements(typesContextRef, 
			"archStructure", xArchRef);
		java.util.List newObjRefsList = Arrays.asList(archStructureRefs);
			
		ArrayList oldObjRefsList = new ArrayList();
		for(int i = 0; i < archStructuresNode.getChildCount(); i++){
			Object child = archStructuresNode.getChildAt(i);
			if(child instanceof ArchStructureTreeNode){
				ObjRef ref = ((ArchStructureTreeNode)child).getObjRef();
				oldObjRefsList.add(ref);
			}
		}
		
		ArchipelagoUtils.ListDiff ld = ArchipelagoUtils.diffLists(oldObjRefsList, newObjRefsList);
		
		for(Iterator it = ld.getRemoveList().iterator(); it.hasNext(); ){
			ObjRef ref = (ObjRef)it.next();
			ArchStructureTreeNode astn = getArchStructureTreeNode(archStructuresNode, ref);
			if(astn != null){
				//This code checks to see if the currently-viewed BNA component on
				//the right side of the frame is a structure that's getting deleted.
				//If it is, then it sets the right component to the Nothing Component.
				//This will trigger the current structure's ancestorListener, which
				//will notice that it's being removed and call destroyStructureBNAComponent 
				ObjRef astnRef = astn.getObjRef();
				if((astnRef != null) && (astnRef.equals(lastViewedStructure))){
					java.awt.Component[] children = WidgetUtils.getHierarchyRecursive(getArchipelagoFrame().getRightComponent());
					for(int i = 0; i < children.length; i++){
						if(children[i] instanceof BNAComponent){
							BNAComponent currentBNAComponent = (BNAComponent)children[i];
							if(currentBNAComponent.getID().equals("StructureBNA")){
								getArchipelagoFrame().setRightComponent(ArchipelagoFrame.getNothingComponent());
								getArchipelagoFrame().setWindowTitleElement(null);
								break;
							}
						}
					}
				}
				//Actually remove the tree node.
				archStructuresNode.remove(astn);
			}
		}
		
		for(Iterator it = ld.getNoChangeList().iterator(); it.hasNext(); ){
			ObjRef ref = (ObjRef)it.next();
			ArchStructureTreeNode astn = getArchStructureTreeNode(archStructuresNode, ref);
			if(astn != null){
				String description = edu.uci.ics.xadlutils.XadlUtils.getDescription(xarch, ref);
				if(description == null){
					description = "Structure [No Description]";
				}
				astn.setUserObject(description);
				archStructuresNode.remove(astn);
				WidgetUtils.addTreeNodeAlphabetically(archStructuresNode, astn);
			}
		}
			
		for(Iterator it = ld.getAddList().iterator(); it.hasNext(); ){
			ObjRef ref = (ObjRef)it.next();
			
			String description = edu.uci.ics.xadlutils.XadlUtils.getDescription(xarch, ref);
			if(description == null){
				description = "Structure [No Description]";
			}
				
			ArchStructureTreeNode newastn = new ArchStructureTreeNode(description);
			newastn.setObjRef(ref);
			WidgetUtils.addTreeNodeAlphabetically(archStructuresNode, newastn);
		}
		 
		if(!foundArchStructuresNode){
			tree.refreshNode(rootNode);
		}
		else{
			tree.refreshNode(archStructuresNode);
		}
		tree.validate();
		tree.repaint();
	}

	public ArchipelagoHintsInfo[] getHintsInfo(){
		java.util.List hintsInfoList = new ArrayList();
		//refreshArchStructureElement();
		DefaultMutableTreeNode mainArchStructureTreeNode = getMainArchStructuresNode();
		if(mainArchStructureTreeNode != null){
			for(int i = 0; i < mainArchStructureTreeNode.getChildCount(); i++){
				TreeNode childNode = mainArchStructureTreeNode.getChildAt(i);
				if(childNode instanceof ArchStructureTreeNode){
					ArchStructureTreeNode astn = (ArchStructureTreeNode)childNode;
					ObjRef astnRef = astn.getObjRef();
					BNAModel astnModel = astn.getBNAModel();
					if((astnRef != null) && (astnModel != null)){
						ArchipelagoHintsInfo ahi = new ArchipelagoHintsInfo(astnRef, astnModel);
						hintsInfoList.add(ahi);
					}
				}
			}
		}
		return (ArchipelagoHintsInfo[])hintsInfoList.toArray(new ArchipelagoHintsInfo[0]);
	}

	public void doOpen(ArchStructureTreeNode node){
		if(node != null){
			ArchStructureNavigationItem ni = new ArchStructureNavigationItem(node.getObjRef());
			ni.setIcon(node.getIcon());
			frame.addNavigationItem(ni);
		}
		doOpenDontChangeNav(node);
	}
	
	private void doOpenDontChangeNav(ArchStructureTreeNode node){
		frame.setRightComponent(null);
		frame.setWindowTitleElement(null);
		initFrame(node);
	}
	
	protected void initFrame(ArchStructureTreeNode node){
		lastViewedStructure = node.getObjRef();
		ScrollableBNAComponent structureScrollableBNAComponent = createStructureBNAComponent(node); 
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add("Center", structureScrollableBNAComponent);
		rightPanel.add("South", ZoomWidget.getZoomWidget(structureScrollableBNAComponent));
		frame.setRightComponent(rightPanel);
		frame.setWindowTitleElement(node.toString());
	}
	
	public ScrollableBNAComponent createStructureBNAComponent(ArchStructureTreeNode node){
		ThingIDMap thingIDMap;
		DefaultBNAModel structureBNAModel;
		
		if(node.getBNAModel() != null){
			structureBNAModel = (DefaultBNAModel)node.getBNAModel();
			thingIDMap = node.getThingIDMap();
		}
		else{
			structureBNAModel = new DefaultBNAModel();
			thingIDMap = new ThingIDMap();
			node.setBNAModel(structureBNAModel);
			node.setThingIDMap(thingIDMap);
		}
		
		BNAComponent structureBNAComponent = new BNAComponent("StructureBNA", structureBNAModel);
		structureBNAComponent.setProperty(THING_ID_MAP_PROPERTY_NAME, thingIDMap);
		structureBNAComponent.setBackground(Color.WHITE);

		//Antialiasing
		boolean antialiasText = ArchipelagoUtils.getAntialiasText(preferences);
		structureBNAComponent.setAntialiasText(antialiasText);

		boolean antialiasGraphics = ArchipelagoUtils.getAntialiasGraphics(preferences);
		structureBNAComponent.setAntialiasGraphics(antialiasGraphics);
		
		boolean gradientGraphics = ArchipelagoUtils.getGradientGraphics(preferences);
		structureBNAComponent.setGradientGraphics(gradientGraphics);
		
		Font defaultFont = ArchipelagoUtils.getDefaultFont(preferences);
		structureBNAComponent.setProperty("defaultFont", defaultFont);
		
		//Logics
		//structureBNAComponent.addThingLogic(new AutoScrollEdgeLogic());
		
		SelectionTrackingLogic stl = new SelectionTrackingLogic();
		structureBNAComponent.addThingLogic(stl);
		BoundingBoxTrackingLogic bbtl = new BoundingBoxTrackingLogic();
		structureBNAComponent.addThingLogic(bbtl);
		AnchorPointTrackingLogic aptl = new AnchorPointTrackingLogic();
		structureBNAComponent.addThingLogic(aptl);
		StickyBoxTrackingLogic sbtl = new StickyBoxTrackingLogic();
		structureBNAComponent.addThingLogic(sbtl);
		MouseTrackingLogic mtl = new MouseTrackingLogic();
		structureBNAComponent.addThingLogic(mtl);
		TagThingTrackingLogic ttl = new TagThingTrackingLogic();
		structureBNAComponent.addThingLogic(ttl);
		MultiDropTargetLogic mdtl = new MultiDropTargetLogic();
		structureBNAComponent.addThingLogic(mdtl);

		structureBNAComponent.addThingLogic(new TextReadabilityLogic());
		structureBNAComponent.addThingLogic(new DragMovableSelectionLogic());
		structureBNAComponent.addThingLogic(new NoThingLogic());
		structureBNAComponent.addThingLogic(new OneClickSelectionLogic());
		structureBNAComponent.addThingLogic(new BoxReshapeLogic(stl, bbtl));
		structureBNAComponent.addThingLogic(new SplineReshapeLogic(stl));
		structureBNAComponent.addThingLogic(new StickySplineLogic(sbtl));
		structureBNAComponent.addThingLogic(new CustomCursorLogic());
		structureBNAComponent.addThingLogic(new ToolTipLogic());
		structureBNAComponent.addThingLogic(new MoveEndpointLogic(bbtl));
		structureBNAComponent.addThingLogic(new MoveIndicatorPointLogic(bbtl));
		structureBNAComponent.addThingLogic(new MoveTogetherLogic(bbtl, aptl));
		EndpointTaggingLogic tl = new EndpointTaggingLogic(ttl);
		structureBNAComponent.addThingLogic(tl);
		structureBNAComponent.addThingLogic(new RotaterThingLogic());

		//Dancing Ants
		OffsetRotatorLogic orl = new OffsetRotatorLogic();
		structureBNAComponent.addThingLogic(orl);
		structureBNAComponent.addThingLogic(new SelectedThingOffsetRotatorLogic(orl, stl));
		structureBNAComponent.addThingLogic(new DotMarqueeOffsetRotatorLogic(orl));
		structureBNAComponent.addThingLogic(new GlowboxOffsetRotatorLogic(orl));

		structureBNAComponent.addThingLogic(new ToolTipLogic());
		/*
		DragAndDropLogic structureDDL = new DragAndDropLogic();
		structureDDL.addDropHandler(
			new DropHandler(){
				public void handleDrop(String sourceBNAComponentID, BNAComponent dropTarget, Thing t, int worldX, int worldY){
					System.out.println("Drop detected: " + sourceBNAComponentID + ", " + dropTarget + ", " + t);
				}
			}
		);
		structureBNAComponent.addThingLogic(structureDDL);
		*/
		structureBNAComponent.addThingLogic(new MaintainInterfaceInterfaceMappingLogic());
		
		//Local Logics for Swing Integration
		structureBNAComponent.addThingLogic(new LocalDragMovableLogic());
		LocalBoundingBoxTrackingLogic lbbtl = new LocalBoundingBoxTrackingLogic();
		structureBNAComponent.addThingLogic(lbbtl);
		structureBNAComponent.addThingLogic(new LocalBoxReshapeLogic(stl, lbbtl));
		structureBNAComponent.addThingLogic(new ScrollLocalBoxBoundedThingLogic());
		
		//Main Menu Logics
		
		//Edit Menu:
		structureBNAComponent.addThingLogic(new FindByLabelMainMenuLogic(frame.getJMenuBar()));
		structureBNAComponent.addThingLogic(new ResetSplineMainMenuLogic(frame.getJMenuBar()));
		structureBNAComponent.addThingLogic(new GraphVizLayoutLogic(frame.getJMenuBar(), gli, node.getObjRef()));
		
		//View Menu:
		structureBNAComponent.addThingLogic(new GlassModeMainMenuLogic(frame.getJMenuBar()));
		//structureBNAComponent.addThingLogic(new AntialiasingModeMainMenuLogic(frame.getJMenuBar()));

		final IPreferences finalPreferences = preferences;
		IRecentDirectory recentDirectory = new IRecentDirectory(){
			public String getRecentDirectory(){
				return RecentDirectoryPreferenceUtils.getRecentDirectory(finalPreferences, "archipelagoBitmap");
			}
			public void setRecentDirectory(String dir){
				RecentDirectoryPreferenceUtils.storeRecentDirectory(finalPreferences, "archipelagoBitmap", dir);
			}
		};

		structureBNAComponent.addThingLogic(new SaveBitmapMainMenuLogic(frame.getJMenuBar(), recentDirectory));

		structureBNAComponent.addThingLogic(new TestMainMenuLogic(frame.getJMenuBar()));
		
		//Context Menu Logics:
		SelectionBasedContextMenuLogic sbcml = new SelectionBasedContextMenuLogic(stl);
		sbcml.addPlugin(new NewBrickContextMenuPlugin(structureBNAComponent, node.getObjRef(), thingIDMap, xarch));
		sbcml.addPlugin(new NewLinkContextMenuPlugin(structureBNAComponent, node.getObjRef(), thingIDMap, xarch));
		sbcml.addPlugin(new EditDescriptionContextMenuPlugin(structureBNAComponent, thingIDMap, xarch));
		sbcml.addPlugin(new EditDirectionContextMenuPlugin(structureBNAComponent, thingIDMap, xarch));
		sbcml.addPlugin(new EditInterfaceToSignatureContextMenuPlugin(structureBNAComponent, thingIDMap, xarch));
		sbcml.addPlugin(new NewInterfaceContextMenuPlugin(structureBNAComponent, node.getObjRef(), thingIDMap, xarch));
		sbcml.addPlugin(new GotoTypeContextMenuPlugin(structureBNAComponent, thingIDMap, xarch, archTypesTreePlugin));
		sbcml.addPlugin(new ClearTypeContextMenuPlugin(structureBNAComponent, thingIDMap, xarch));
		sbcml.addPlugin(new InvokeTypeWranglerContextMenuPlugin(structureBNAComponent, node.getObjRef(), thingIDMap, xarch, this));
		sbcml.addPlugin(new RemoveStructureElementContextMenuPlugin(structureBNAComponent, thingIDMap, xarch));
		sbcml.addPlugin(new OrderContextMenuPlugin(structureBNAComponent));
		sbcml.addPlugin(new AlignContextMenuPlugin(structureBNAComponent));
		sbcml.addPlugin(new DistributeContextMenuPlugin(structureBNAComponent));
		sbcml.addPlugin(new AssignColorsContextMenuPlugin(structureBNAComponent, this, null, thingIDMap));
		sbcml.addPlugin(new EndpointTagContextMenuPlugin(structureBNAComponent, tl, ttl, mtl));
		sbcml.addPlugin(new RotateContextMenuPlugin(structureBNAComponent));
		structureBNAComponent.addThingLogic(sbcml);

		structureBNAComponent.addThingLogic(new ElementIndicatorLogic(thingIDMap));

		mdtl.addDropTargetLogic(new SetTypeDropTargetLogic(thingIDMap, xarch, node.getObjRef()));

		//Gestures
		structureBNAComponent.addThingLogic(new EphemeralLogic());
		MouseGestureLogic mgl = new MouseGestureLogic();
		mgl.setAction("horizontalwag", "Horizontal Wag", "Align Horizontal Centers",
			new AlignHorizontalCentersAction(stl));
		mgl.setAction("verticalwag", "Vertical Wag", "Align Vertical Centers",
			new AlignVerticalCentersAction(stl));
		mgl.setAction("square", "Square", "Distribute Horizontally (Tight)",
			new DistributeHorizontallyTightAction(stl));
		mgl.setAction("heart", "Heart", "Distribute Vertically (Tight)",
			new DistributeVerticallyTightAction(stl));
		structureBNAComponent.addThingLogic(mgl);
		
		//EnclosingTypes
		//structureBNAComponent.addThingLogic(new EnclosingTypeMainMenuLogic(frame.getJMenuBar(), node.getObjRef(),
		//	thingIDMap, xarch));
		
		ScrollableBNAComponent structureScrollableBNAComponent = new 
			ScrollableBNAComponent(structureBNAComponent, true, true, true);

		structureScrollableBNAComponent.invalidate();
		structureScrollableBNAComponent.validate();
		structureScrollableBNAComponent.repaint();

		renderingHints = frame.getRenderingHints();

		//Mapping Logics
		LinkMappingLogic linkMappingLogic = new LinkMappingLogic(
			node.getObjRef(),
			new BNAModel[]{	structureBNAModel	},
			mtl, 
			xarch,
			thingIDMap,
			renderingHints);
		//addMappingLogic(linkMappingLogic);
		structureBNAComponent.getModel().addBNAModelListener(linkMappingLogic);
		registerMappingLogic(structureBNAComponent, linkMappingLogic);
		//structureBNAComponent.setProperty(LINK_MAPPING_LOGIC_PROPERTY_NAME, linkMappingLogic);

		BrickMappingLogic brickMappingLogic = new BrickMappingLogic(
			this,
			node.getObjRef(),
			new BNAModel[]{ structureBNAModel },
			mtl, 
			xarch,
			thingIDMap,
			renderingHints);
		//addMappingLogic(brickMappingLogic);
		structureBNAComponent.getModel().addBNAModelListener(brickMappingLogic);
		registerMappingLogic(structureBNAComponent, brickMappingLogic);
		//structureBNAComponent.setProperty(BRICK_MAPPING_LOGIC_PROPERTY_NAME, brickMappingLogic);

		PropertyTableLogic propertyTableLogic = new PropertyTableLogic(structureBNAModel, xarch, thingIDMap);
		propertyTableLogic.addPropertyTablePlugin(new ApplyThemeThemePropertyTablePlugin(xarch));
		propertyTableLogic.addPropertyTablePlugin(new DescriptionPropertyTablePlugin(xarch));
		propertyTableLogic.addPropertyTablePlugin(new StructurePropertyTablePlugin(xarch));
		structureBNAComponent.getModel().addBNAModelListener(propertyTableLogic);
		registerMappingLogic(structureBNAComponent, propertyTableLogic);
		structureBNAComponent.addThingLogic(propertyTableLogic);
		
		//Done mapping logics
		
		//Load environment properties either from the extant
		//thing in the model or from the rendering hints.
		EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(structureBNAModel);
		//System.out.println("got ept in archstructure: " + ept);
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
					structureBNAModel.addThing(ept);
				}
			}
		}
		if(ept != null){
			try{
				int wox = ept.getWorldOriginX();
				int woy = ept.getWorldOriginY();
				double scale = ept.getScale();
				structureBNAComponent.repositionAbsolute(wox, woy);
				structureBNAComponent.rescaleAbsolute(scale);
			}
			catch(Exception e){
			}
		}
		
		//This has to come after the above or else you'll always get the environment
		//properties thing created by this logic on init.
		structureBNAComponent.addThingLogic(new MaintainEnvironmentPropertiesLogic());
		
		final BNAComponent fsbc = structureBNAComponent;
		final ArchStructureTreeNode fNode = node;
		structureBNAComponent.addAncestorListener(new javax.swing.event.AncestorListener(){
			public void ancestorAdded(AncestorEvent arg0){}
			public void ancestorMoved(AncestorEvent arg0){}
			public void ancestorRemoved(AncestorEvent arg0){
				destroyStructureBNAComponent(fsbc);
				
				EnvironmentPropertiesThing ept = BNAUtils.getEnvironmentPropertiesThing(fsbc.getModel());
				if(ept != null){
					ObjRef nodeRef = fNode.getObjRef();
					if(nodeRef != null){
						String id = XadlUtils.getID(xarch, nodeRef);
						if(id != null){
							ept.setProperty(IHinted.XARCHID_PROPERTY_NAME, id + "$$environmentProperties");						}
					}
				}
			}
		}
		);
		fireStructureBNAComponentCreated(node, structureBNAComponent);
		
		Thing[] at = structureBNAModel.getAllThings();
		for(int i = 0; i < at.length; i++){
			if(at[i] instanceof TagThing){
				System.out.println(at[i]);
				TagThing tt = (TagThing)at[i];
				String mttid = tt.getMoveTogetherThingId();
				Thing mtt = structureBNAModel.getThing(mttid);
				System.out.println(mtt);
			}
		}
		initStructure(xArchRef, node.getObjRef(), brickMappingLogic, linkMappingLogic);
		renderingHints.applyIndependentHints(structureBNAModel);
		return structureScrollableBNAComponent;
	}
	
	/*
	private void storeEnvironmentPropertiesThing(BNAComponent structureBNAComponent){
		int wox = structureBNAComponent.getWorldOriginX();
		int woy = structureBNAComponent.getWorldOriginY();
		double scale = structureBNAComponent.getScale();

		EnvironmentPropertiesThing ept = EnvironmentPropertiesThing.getEnvironmentPropertiesThing(structureBNAComponent.getModel());
		boolean foundEpt = true;
		if(ept == null){
			foundEpt = false;
			ept = new EnvironmentPropertiesThing();
		}
		ept.setWorldOriginX(wox);
		ept.setWorldOriginY(woy);
		ept.setScale(scale);
		if(!foundEpt){
			structureBNAComponent.getModel().addThing(ept);
		}
	}
	*/
	
	public void destroyStructureBNAComponent(BNAComponent structureBNAComponent){
		fireStructureBNAComponentDestroying(structureBNAComponent);
		
		MappingLogic[] mls = getAllMappingLogics(structureBNAComponent);
		for(int i = 0; i < mls.length; i++){
			unregisterMappingLogic(structureBNAComponent, mls[i]);
		}
		
		ArrayList thingsToRemove = new ArrayList();
		for(Iterator it = structureBNAComponent.getModel().getThingIterator(); it.hasNext(); ){
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
			structureBNAComponent.getModel().removeThing(doomedThing);
		}
		
		//storeEnvironmentPropertiesThing(structureBNAComponent);

		SelectionUtils.removeAllSelections(structureBNAComponent);
		structureBNAComponent.destroy();
		fireStructureBNAComponentDestroyed(structureBNAComponent);
		//structureBNAComponent = null;
	}

	protected void initStructure(ObjRef xArchRef, ObjRef structureRef,
	BrickMappingLogic brickMappingLogic, LinkMappingLogic linkMappingLogic){
		//ObjRef xArchRef = xarch.getOpenXArch(url);
		ObjRef typesContextRef = xarch.createContext(xArchRef, "Types");
		ObjRef archStructureRef = structureRef;
		
		ObjRef[] componentRefs = xarch.getAll(archStructureRef, "Component");
		ObjRef[] connectorRefs = xarch.getAll(archStructureRef, "Connector");
		ObjRef[] linkRefs = xarch.getAll(archStructureRef, "Link");

		int totalNum = componentRefs.length + connectorRefs.length + linkRefs.length;
		
		ProgressDialog pd = new ProgressDialog(frame, "Syncing Elements", "Syncing...");
		pd.getProgressBar().setMinimum(0);
		pd.getProgressBar().setMaximum(totalNum);
		pd.getProgressBar().setStringPainted(true);
		pd.doPopup();
		
		int totalDone = 0;
		
		try{
			brickMappingLogic.getBNAModel().beginBulkChange();
			
			pd.getProgressBar().setString("Syncing Components...");
			for(int i = 0; i < componentRefs.length; i++){
				brickMappingLogic.updateComponent(componentRefs[i]);
				++totalDone;
				if((totalDone % 5) == 0) pd.getProgressBar().setValue(totalDone);
			}
			
			pd.getProgressBar().setString("Syncing Connectors...");
			for(int i = 0; i < connectorRefs.length; i++){
				brickMappingLogic.updateConnector(connectorRefs[i]);
				++totalDone;
				if((totalDone % 5) == 0) pd.getProgressBar().setValue(totalDone);
			}
		}finally{
			brickMappingLogic.getBNAModel().endBulkChange();
		}

		linkMappingLogic.getBNAModel().beginBulkChange();
		linkMappingLogic.getBNAModel().fireStreamNotificationEvent("startSyncingLinks");
		pd.getProgressBar().setString("Syncing Links...");
		for(int i = 0; i < linkRefs.length; i++){
			linkMappingLogic.updateLink(linkRefs[i]);
			++totalDone;
			if((totalDone % 5) == 0) pd.getProgressBar().setValue(totalDone);
		}
		linkMappingLogic.getBNAModel().fireStreamNotificationEvent("endSyncingLinks");
		linkMappingLogic.getBNAModel().endBulkChange();
		pd.doDone();
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
			refreshArchStructureElement();
		}
		else if((sourcePathString != null) && (sourcePathString.equals("xArch/archStructure/description"))){
			refreshArchStructureElement();
		}
		else if((targetPathString != null) && (targetPathString.equals("xArch/archStructure/description"))){
			refreshArchStructureElement();
		}
		
		java.awt.Component[] children = WidgetUtils.getHierarchyRecursive(getArchipelagoFrame().getRightComponent());
		for(int i = 0; i < children.length; i++){
			if(children[i] instanceof BNAComponent){
				BNAComponent currentBNAComponent = (BNAComponent)children[i];
				if(currentBNAComponent.getID().equals("StructureBNA")){
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
				if(currentBNAComponent.getID().equals("StructureBNA")){
					MappingLogic[] mls = getAllMappingLogics(currentBNAComponent);
					for(int j = 0; j < mls.length; j++){
						mls[j].handleXArchFileEvent(evt);
					}
					break;
				}
			}
		}
	}
	
	/*
	public void storeHints(){
		ProgressDialog pd = new ProgressDialog(frame, "Storing Rendering Hints", "Storing Hints");
		pd.doPopup();
		archstudio.comp.archipelago.RenderingHints.writeHints(xarch, xArchRef, new BNAModel[]{structureBNAModel}, pd.getProgressBar());
		pd.doDone();
	}
	*/
	
	class DoubleClickMouseAdapter extends MouseAdapter{
		public void mouseClicked(MouseEvent e){
			if(e.getButton() == MouseEvent.BUTTON1){
				if(e.getClickCount() == 2){
					int selRow = tree.getRowForLocation(e.getX(), e.getY());
					TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
					if(selRow != -1) {
						Object o = selPath.getLastPathComponent();
						if((o != null) && (o instanceof ArchStructureTreeNode)){
							ArchStructureTreeNode node = (ArchStructureTreeNode)o;
							doOpen(node);
						}
					}
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
		
		if((bnaComponent != null) && (bnaComponent.getID() != null) && (bnaComponent.getID().equals("StructureBNA"))){
			if(node instanceof ArchTypesTreeNode){
				return true; 
			}
		}
		return false;
	}

	public DragInfo getDragInfo(TreeNode node) {
		ArchTypesTreeNode attn = (ArchTypesTreeNode)node;
		
		ObjRef ref = attn.getObjRef();
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
		
		if(tn instanceof ArchStructureTreeNode){
			ArchStructureTreeNodeMenuItemSet miSet = new ArchStructureTreeNodeMenuItemSet(xarch, (ArchStructureTreeNode)tn, getArchipelagoFrame());
			return miSet.getMenuItems();
		}
		else if(tn instanceof DefaultMutableTreeNode){
			DefaultMutableTreeNode archStructuresNode = getMainArchStructuresNode();
			if((archStructuresNode != null) && (archStructuresNode == tn)){
				StructureParentTreeNodeMenuItemSet miSet = new StructureParentTreeNodeMenuItemSet(xarch, (DefaultMutableTreeNode)tn, xArchRef);
				return miSet.getMenuItems();
			}
		}
		return null;
	}
	
	static class StructureParentTreeNodeMenuItemSet extends JPopupMenu implements ActionListener{
		protected XArchFlatInterface xarch;
		protected DefaultMutableTreeNode tn;
		protected JMenuItem miNewStructure;
		protected ObjRef xArchRef;
		
		protected java.util.List menuItems = new ArrayList();

		public StructureParentTreeNodeMenuItemSet(XArchFlatInterface xarch, DefaultMutableTreeNode tn, ObjRef xArchRef){
			this.xarch = xarch;
			this.tn = tn;
			this.xArchRef = xArchRef;
			init();
		}
		
		protected void init(){
			miNewStructure = new JMenuItem("New Structure");
			WidgetUtils.setMnemonic(miNewStructure, 'N');
			miNewStructure.addActionListener(this);

			menuItems.add(miNewStructure);
		}
		
		public JMenuItem[] getMenuItems(){
			return (JMenuItem[])menuItems.toArray(new JMenuItem[0]);
		}

		public void actionPerformed(ActionEvent evt){
			if(evt.getSource() == miNewStructure){
				newStructure();
			}
		}
		
		protected void newStructure(){
			ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
			ObjRef newStructureRef = xarch.createElement(typesContextRef, "archStructure");
			String newID = UIDGenerator.generateUID("archStructure");
			xarch.set(newStructureRef, "id", newID);
			ObjRef descriptionRef = xarch.create(typesContextRef, "Description");
			xarch.set(descriptionRef, "value", "(New Structure)");
			xarch.set(newStructureRef, "Description", descriptionRef);
			xarch.add(xArchRef, "Object", newStructureRef);
		}
	}
	
	static class ArchStructureTreeNodeMenuItemSet implements ActionListener{
		protected XArchFlatInterface xarch;
		protected ArchStructureTreeNode tn;
		protected ArchipelagoFrame f;
		
		protected JMenuItem miRename;
		protected JMenuItem miRemove;
		
		protected java.util.List menuItems = new ArrayList();
		
		public ArchStructureTreeNodeMenuItemSet(XArchFlatInterface xarch, ArchStructureTreeNode tn, ArchipelagoFrame f){
			this.xarch = xarch;
			this.tn = tn;
			this.f = f;
			init();
		}
		
		protected void init(){
			miRename = new JMenuItem("Rename Structure");
			WidgetUtils.setMnemonic(miRename, 'N');
			miRename.addActionListener(this);
			
			miRemove = new JMenuItem("Remove Structure");
			WidgetUtils.setMnemonic(miRemove, 'R');
			miRemove.addActionListener(this);

			menuItems.add(miRename);
			menuItems.add(miRemove);
		}
		
		public JMenuItem[] getMenuItems(){
			return (JMenuItem[])menuItems.toArray(new JMenuItem[0]);
		}
		
		public void actionPerformed(ActionEvent evt){
			if(evt.getSource() == miRemove){
				int result = JOptionPane.showConfirmDialog(f, 
					"Are you sure?", "Confirm Remove Structure", JOptionPane.YES_NO_OPTION);
				if(result == JOptionPane.YES_OPTION){
					remove();
				}
			}
			if(evt.getSource() == miRename){
				rename();
			}
		}
		
		protected void rename(){
			ObjRef structureRef = tn.getObjRef();
			
			String currentDescription = XadlUtils.getDescription(xarch, structureRef);
			if(currentDescription == null){
				currentDescription = "";
			}
			
			Object resp = JOptionPane.showInputDialog(f, "New Description",
				"Set Description", JOptionPane.QUESTION_MESSAGE, null, null,
				currentDescription);
			
			if(resp == null){
				return;
			}
			
			ObjRef xArchRef = xarch.getXArch(structureRef);
			ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
			ObjRef newDescriptionRef = xarch.create(typesContextRef, "Description");
			xarch.set(newDescriptionRef, "Value", resp.toString());
			xarch.set(structureRef, "Description", newDescriptionRef);
		}
		
		protected void remove(){
			ObjRef structureRef = tn.getObjRef();
			ObjRef xArchRef = xarch.getXArch(structureRef);
			xarch.remove(xArchRef, "Object", structureRef);
		}
	}

	public boolean navigateTo(NavigationItem ni){
		if(!(ni instanceof ArchStructureNavigationItem)){
			return false;
		}
		ArchStructureNavigationItem ani = (ArchStructureNavigationItem)ni;
		ObjRef ref = ani.getRef();
		ArchStructureTreeNode tn = getArchStructureTreeNode(ref);
		doOpenDontChangeNav(tn); 	//works ok even if tn == null
		return true;
	}
	
	class ArchStructureNavigationItem extends NavigationItem{
		private ObjRef ref;
		
		public ArchStructureNavigationItem(ObjRef ref){
			super(null, ref);
			this.ref = ref;
		}
		
		public ObjRef getRef(){
			return ref;
		}
		
		public String getDescription(){
			String description = XadlUtils.getDescription(xarch, ref);
			if(description == null){
				description = "Structure (No Description)";
			}
			return description;
		}
	}

	public void handle(c2.fw.Message m){
		java.awt.Component[] children = WidgetUtils.getHierarchyRecursive(getArchipelagoFrame().getRightComponent());
		for(int i = 0; i < children.length; i++){
			if(children[i] instanceof BNAComponent){
				BNAComponent currentBNAComponent = (BNAComponent)children[i];
				if(currentBNAComponent.getID().equals("StructureBNA")){
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
	
	public BNAComponent getShowingStructureBNAComponent(){
		java.awt.Component[] children = WidgetUtils.getHierarchyRecursive(getArchipelagoFrame().getRightComponent());
		for(int i = 0; i < children.length; i++){
			if(children[i] instanceof BNAComponent){
				BNAComponent currentBNAComponent = (BNAComponent)children[i];
				if(currentBNAComponent.getID().equals("StructureBNA")){
					return currentBNAComponent;
				}
			}
		}
		return null;
	}
	
	public boolean showRef(ObjRef ref, XArchPath path){
		if(ref == null){
			return false;
		}
		String pathString = path.toTagsOnlyString();
		if(pathString.equals("xArch/archStructure")){
			ArchStructureTreeNode treeNode = getArchStructureTreeNode(ref);
			if(treeNode != null){
				showAndSelect(treeNode);
				doOpen(treeNode);
				return true;
			}
		}
		else if(pathString.startsWith("xArch/archStructure")){
			ObjRef[] ancestors = xarch.getAllAncestors(ref);
			ObjRef structureRef = ancestors[ancestors.length - 2];
			ArchStructureTreeNode treeNode = getArchStructureTreeNode(structureRef);
			if(treeNode != null){
				showAndSelect(treeNode);
				
				BNAComponent structureBNAComponent = getShowingStructureBNAComponent();
				if(structureBNAComponent == null){
					doOpen(treeNode);
				}
				structureBNAComponent = getShowingStructureBNAComponent();
				
				BNAModel bnaModel = (DefaultBNAModel)treeNode.getBNAModel();
				if(bnaModel != null){
					ThingIDMap thingIDMap = treeNode.getThingIDMap();
					if(thingIDMap != null){
						for(int i = ancestors.length - 1; i >= 0; i--){
							String thingID = thingIDMap.getThingID(ancestors[i]);
							if(thingID != null){
								Thing t = bnaModel.getThing(thingID);
								if(t != null){
									if(t instanceof ISelectable){
										if(structureBNAComponent != null){
											if(t instanceof IBoxBounded){
												Rectangle bb = ((IBoxBounded)t).getBoundingBox();
												FlyToLogic.flyTo(structureBNAComponent, (int)bb.getCenterX(), (int)bb.getCenterY());
											}
										}
										SelectionUtils.removeAllSelections(bnaModel);
										((ISelectable)t).setSelected(true);
										return true;
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	public Color getDefaultComponentColor(){
		IPreferences preferences = getPreferences();
		
		Color defaultComponentColor = BrickMappingLogic.DEFAULT_COMPONENT_COLOR;
		if(preferences.keyExists(IPreferences.USER_SPACE, 
		"/archstudio/comp/archipelago/types", "defaultComponentColor")){
			int val = preferences.getIntValue(IPreferences.USER_SPACE, 
			"/archstudio/comp/archipelago/types", "defaultComponentColor", -1);
			defaultComponentColor = new Color(val);
		}
		return defaultComponentColor;
	}
	
	public Color getDefaultConnectorColor(){
		IPreferences preferences = getPreferences();
		
		Color defaultConnectorColor = BrickMappingLogic.DEFAULT_CONNECTOR_COLOR;
		if(preferences.keyExists(IPreferences.USER_SPACE, 
		"/archstudio/comp/archipelago/types", "defaultConnectorColor")){
			int val = preferences.getIntValue(IPreferences.USER_SPACE, 
			"/archstudio/comp/archipelago/types", "defaultConnectorColor", -1);
			defaultConnectorColor = new Color(val);
		}
		return defaultConnectorColor;
	}
	
}

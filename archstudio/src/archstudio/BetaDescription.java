package archstudio;

import edu.uci.isr.xarch.*;
import edu.uci.isr.xarch.instance.*;
import edu.uci.isr.xarch.types.*;
import edu.uci.isr.xarch.implementation.*;
import edu.uci.isr.xarch.variants.*;
import edu.uci.isr.xarch.javaimplementation.*;
import edu.uci.isr.xarch.lookupimplementation.*;

public class BetaDescription{
	
	private static String[] commandLineArguments;
	
	private static IInstanceContext instance;
	private static ITypesContext types;
	private static IImplementationContext implementation;
	private static IVariantsContext variants;
	private static IJavaimplementationContext javaimplementation;
	private static ILookupimplementationContext lookupimplementation;
	
	public static void main(String[] args){
		commandLineArguments = args;
		System.out.println(getXml());
	}
	
	public static boolean hasArg(String arg){
		if(commandLineArguments == null) return false;
		for(int i = 0; i < commandLineArguments.length; i++){
			if(commandLineArguments[i].equals(arg)){
				return true;
			}
		}
		return false;
	}
	
	public static String getXml(){
		try{
			IXArchImplementation xArchImplementation = XArchUtils.getDefaultXArchImplementation();
			IXArch xArch = getXArch();
			return xArchImplementation.serialize(xArch, null);
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
		
	public static IXArch getXArch(){
		IXArchImplementation xArchImplementation = XArchUtils.getDefaultXArchImplementation();
		IXArch xArch = xArchImplementation.createXArch();

		instance = (IInstanceContext)xArchImplementation.createContext(xArch, "instance");
		types = (ITypesContext)xArchImplementation.createContext(xArch, "types");
		implementation = (IImplementationContext)xArchImplementation.createContext(xArch, "implementation");
		variants = (IVariantsContext)xArchImplementation.createContext(xArch, "variants");
		javaimplementation = (IJavaimplementationContext)xArchImplementation.createContext(xArch, "javaimplementation");
		lookupimplementation = (ILookupimplementationContext)xArchImplementation.createContext(xArch, "lookupimplementation");
		
		IArchStructure archStructure = types.createArchStructureElement();
		archStructure.setId("ArchStudio_3");
		archStructure.setDescription(createDescription("ArchStudio 3.0 Architecture"));
		
		IArchTypes archTypes = types.createArchTypesElement();
		
		xArch.addObject(archStructure);
		xArch.addObject(archTypes);
		
		IInterfaceType c2TopType = types.createInterfaceType();
		IInterfaceTypeImpl c2TopTypeImpl = implementation.promoteToInterfaceTypeImpl(c2TopType); 
		c2TopTypeImpl.setId("C2TopType");
		c2TopTypeImpl.setDescription(createDescription("C2 Top Interface"));
		IJavaImplementation javaImplementation = javaimplementation.createJavaImplementation();
		IJavaClassFile javaClassFile = javaimplementation.createJavaClassFile();
		IJavaClassName jcn = javaimplementation.createJavaClassName();
		jcn.setValue("c2.fw.SimpleInterface");
		javaClassFile.setJavaClassName(jcn);
		javaImplementation.setMainClass(javaClassFile);
		c2TopTypeImpl.addImplementation(javaImplementation);
		ILookupImplementation lookupImplementation = lookupimplementation.createLookupImplementation();
		ILookupName lookupName = lookupimplementation.createLookupName();
		lookupName.setValue("IFACE_TOP");
		lookupImplementation.setName(lookupName);
		c2TopTypeImpl.addImplementation(lookupImplementation);
		archTypes.addInterfaceType(c2TopTypeImpl);
		
		IInterfaceType c2BottomType = types.createInterfaceType();
		IInterfaceTypeImpl c2BottomTypeImpl = implementation.promoteToInterfaceTypeImpl(c2BottomType); 
		c2BottomTypeImpl.setId("C2BottomType");
		c2BottomTypeImpl.setDescription(createDescription("C2 Bottom Interface"));
		javaImplementation = javaimplementation.createJavaImplementation();
		javaClassFile = javaimplementation.createJavaClassFile();
		jcn = javaimplementation.createJavaClassName();
		jcn.setValue("c2.fw.SimpleInterface");
		javaClassFile.setJavaClassName(jcn);
		javaImplementation.setMainClass(javaClassFile);
		c2BottomTypeImpl.addImplementation(javaImplementation);
		ILookupImplementation lookupImplementation2 = lookupimplementation.createLookupImplementation();
		ILookupName lookupName2 = lookupimplementation.createLookupName();
		lookupName2.setValue("IFACE_BOTTOM");
		lookupImplementation2.setName(lookupName2);
		c2BottomTypeImpl.addImplementation(lookupImplementation2);
		archTypes.addInterfaceType(c2BottomTypeImpl);
		
		//archTypes.addComponentType(
		//	createComponentType("xArchADT_type", "xArchADT Type", "archstudio.comp.xarchadt.XArchADTC2Component")
		//);
		archTypes.addComponentType(
			createComponentType("Preferences_type", "Preferences Type", "archstudio.comp.preferences.PreferencesC2Component")
			);
		archTypes.addComponentType(
			createComponentType("EditorADT_type", "Editor ADT Type", "archstudio.comp.editoradt.EditorADTC2Component")
			);
		archTypes.addComponentType(
			createComponentType("xArchTrans_type", "xArchADT Transaction Manager Type", "archstudio.comp.xarchtrans.XArchTransactionsC2Component")
			);
		archTypes.addComponentType(
			createComponentType("TestTrans_type", "xArchADT Transaction Manager Tester Type", "archstudio.comp.testtrans.TestTransactionsC2Component")
			);
		archTypes.addComponentType(
			createComponentType("ArchEdit_type", "ArchEdit Type", "archstudio.comp.archedit.ArchEditC2Component")
			);
		archTypes.addComponentType(
			createComponentType("TypeWrangler_type", "Type Wrangler Type", "archstudio.comp.typewrangler.TypeWranglerC2Component")
			);
		archTypes.addComponentType(
			createComponentType("PreferencesGUI_type", "PreferencesGUI Type", "archstudio.comp.preferencesgui.PreferencesGUIC2Component")
			);
		archTypes.addComponentType(
			createComponentType("Archipelago_type", "Archipelago Type", "archstudio.comp.archipelago.ArchipelagoC2Component")
			);
		archTypes.addComponentType(
			createComponentType("AEM_type", "AEM Type", "archstudio.comp.aem.AEMC2Component")
			);
		archTypes.addComponentType(
			createComponentType("AEMDriver_type", "AEM Driver Type", "archstudio.comp.aemdriver.AEMDriverC2Component")
			);
		archTypes.addComponentType(
			createComponentType("ArchDiff_type", "ArchDiff Type", "archstudio.comp.archdiff.ArchDiffC2Component")
			);
		archTypes.addComponentType(
			createComponentType("ArchDiffDriver_type", "ArchDiff Driver Type", "archstudio.comp.archdiffdriver.ArchDiffDriverC2Component")
			);
		archTypes.addComponentType(
			createComponentType("ArchMerge_type", "ArchMerge Type", "archstudio.comp.archmerge.ArchMergeC2Component")
			);

		archTypes.addComponentType(
			createComponentType("PLADiff_type", "PLADiff Type", "archstudio.comp.pladiff.PLADiffC2Component")
			);
		archTypes.addComponentType(
			createComponentType("PLADiffDriver_type", "PLADiff Driver Type", "archstudio.comp.pladiffdriver.PLADiffDriverC2Component")
			);
		archTypes.addComponentType(
			createComponentType("PLAMerge_type", "PLAMerge Type", "archstudio.comp.plamerge.PLAMergeC2Component")
			);
		archTypes.addComponentType(
			createComponentType("PLAMergeDriver_type", "PLAMerge Driver Type", "archstudio.comp.plamergedriver.PLAMergeDriverC2Component")
			);


		archTypes.addComponentType(
			createComponentType("GraphLayout_type", "GraphLayout Type", "archstudio.comp.graphlayout.GraphLayoutC2Component")
			);
		archTypes.addComponentType(
			createComponentType("ArchMergeDriver_type", "ArchMerge Driver Type", "archstudio.comp.archmergedriver.ArchMergeDriverC2Component")
			);
		archTypes.addComponentType(
			createComponentType("HelloWorld_type", "Hello World Type", "archstudio.comp.helloworld.HelloWorldC2Component")
			);
		archTypes.addComponentType(
			createComponentType("FileManager_type", "File Manager/Invoker Type", "archstudio.comp.fileman.FileManagerC2Component")
			);
		archTypes.addComponentType(
			createComponentType("BooleanEval_type", "Boolean Evaluator Type", "archstudio.comp.booleaneval.BooleanEvalC2Component")
			);
		archTypes.addComponentType(
			createComponentType("BooleanNotation_type", "Boolean Notation Type", "archstudio.comp.booleannotation.BooleanNotationC2Component")
			);
		archTypes.addComponentType(
			createComponentType("GuardTracker_type", "Guard Tracker Type", "archstudio.comp.guardtracker.GuardTrackerC2Component")
			);
		archTypes.addComponentType(
			createComponentType("Selector_type", "Selector Type", "archstudio.comp.selector.SelectorC2Component")
			);
		archTypes.addComponentType(
			createComponentType("Pruner_type", "Pruner Type", "archstudio.comp.archpruner.ArchPrunerC2Component")
			);
		archTypes.addComponentType(
			createComponentType("PruneVersions_type", "Version Pruner Type", "archstudio.comp.pruneversions.PruneVersionsC2Component")
			);
		archTypes.addComponentType(
			createComponentType("SelectorDriver_type", "Selector Driver Type", "archstudio.comp.selectordriver.SelectorDriverC2Component")
			);
		archTypes.addComponentType(
			createComponentType("PerformanceTest_type", "Performance Test Type", "archstudio.comp.perftest.PerformanceTestC2Component")
			);
		archTypes.addComponentType(
			createComponentType("EditorPrefs_type", "Editor Prefs Type", "archstudio.comp.editorprefs.EditorPrefsC2Component")
			);
		archTypes.addComponentType(
			createComponentType("Archon_type", "Archon Type", "archstudio.comp.archon.ArchonC2Component")
			);
		
		archTypes.addComponentType(
			createComponentType("TronIssueADT_type", "Tron Issue ADT Type", "archstudio.comp.tron.issueadt.TronIssueADTC2Component")
			);
		archTypes.addComponentType(
			createComponentType("TronTestADT_type", "Tron Test ADT Type", "archstudio.comp.tron.testadt.TronTestADTC2Component")
			);
		archTypes.addComponentType(
			createComponentType("Schematron_type", "Schematron Type", "archstudio.comp.tron.tools.schematron.SchematronC2Component")
			);
		archTypes.addComponentType(
			createComponentType("TronGUI_type", "Tron GUI Type", "archstudio.comp.tron.gui.TronGUIC2Component")
			);
		archTypes.addComponentType(
			createComponentType("ArchonGUI_type", "Archon GUI Type", "archstudio.comp.archongui.ArchonGUIC2Component")
			);
		archTypes.addComponentType(
			createComponentType("VisioAgent_type", "Visio Agent Type", "archstudio.comp.visio.VisioAgentComponent")
			);
		if(hasArg("-menage")){
			archTypes.addComponentType(
				createComponentType("Menage_type", "Menage Type", "Menage.MenageC2Component")
				);
		}
		archTypes.addConnectorType(
			//createConnectorType("BusConnector_type", "Bus Connector Type", "c2.legacy.conn.BusConnector")
			createConnectorType("BusConnector_type", "Bus Connector Type", "c2.legacy.conn.FilteringBusConnector")
			);
		archTypes.addConnectorType(
			//createConnectorType("BusConnector_type", "Bus Connector Type", "c2.legacy.conn.BusConnector")
			createConnectorType("COMBottomConnector_type", "COM Bottom Connector Type", "archstudio.comp.visio.COMBottomConnector", true, false)
			);
		
		//archStructure.addComponent(
		//	createComponent("xArchADT", "xArchADT", "xArchADT_type")
		//);
		archStructure.addComponent(
			createComponent("Preferences", "Preferences", "Preferences_type")
			);
		archStructure.addComponent(
			createComponent("EditorADT", "Editor ADT", "EditorADT_type")
			);
		archStructure.addComponent(
			createComponent("xArchTrans", "xArchADT Transaction Manager", "xArchTrans_type")
			);
		archStructure.addComponent(
			createComponent("TypeWrangler", "Type Wrangler", "TypeWrangler_type")
			);
		archStructure.addComponent(
			createComponent("TestTrans", "Transaction Manager Tester", "TestTrans_type")
			);
		archStructure.addComponent(
			createComponent("ArchEdit", "ArchEdit", "ArchEdit_type")
			);
		archStructure.addComponent(
			createComponent("PreferencesGUI", "Preferences GUI", "PreferencesGUI_type")
			);
		archStructure.addComponent(
			createComponent("Archipelago", "Archipelago", "Archipelago_type")
			);
		archStructure.addComponent(
			createComponent("AEM", "AEM", "AEM_type")
			);
		archStructure.addComponent(
			createComponent("AEMDriver", "AEM Driver", "AEMDriver_type")
			);
		archStructure.addComponent(
			createComponent("HelloWorld", "Hello World", "HelloWorld_type")
			);
		archStructure.addComponent(
			createComponent("FileManager", "File Manager/Invoker", "FileManager_type")
			);
		
		archStructure.addComponent(
			createComponent("BooleanEval", "Boolean Evaluator", "BooleanEval_type")
			);
		archStructure.addComponent(
			createComponent("BooleanNotation", "Boolean Notation", "BooleanNotation_type")
			);
		archStructure.addComponent(
			createComponent("GuardTracker", "Guard Tracker", "GuardTracker_type")
			);
		archStructure.addComponent(
			createComponent("Selector", "Selector", "Selector_type")
			);
		archStructure.addComponent(
			createComponent("Pruner", "Pruner", "Pruner_type")
			);
		archStructure.addComponent(
			createComponent("PruneVersions", "Version Pruner", "PruneVersions_type")
			);
		archStructure.addComponent(
			createComponent("SelectorDriver", "Selector Driver", "SelectorDriver_type")
			);
		
		archStructure.addComponent(
			createComponent("ArchDiff", "ArchDiff", "ArchDiff_type")
			);
		archStructure.addComponent(
			createComponent("ArchDiffDriver", "ArchDiff Driver", "ArchDiffDriver_type")
			);
		archStructure.addComponent(
			createComponent("ArchMerge", "ArchMerge", "ArchMerge_type")
			);
		archStructure.addComponent(
			createComponent("ArchMergeDriver", "ArchMerge Driver", "ArchMergeDriver_type")
			);
		
		archStructure.addComponent(
			createComponent("PLADiff", "PLADiff", "PLADiff_type")
			);
		archStructure.addComponent(
			createComponent("PLADiffDriver", "PLADiff Driver", "PLADiffDriver_type")
			);
		archStructure.addComponent(
			createComponent("PLAMerge", "PLAMerge", "PLAMerge_type")
			);
		archStructure.addComponent(
			createComponent("PLAMergeDriver", "PLAMerge Driver", "PLAMergeDriver_type")
			);

		archStructure.addComponent(
			createComponent("GraphLayout", "GraphLayout", "GraphLayout_type")
			);

		archStructure.addComponent(
			createComponent("TronTestADT", "Tron Test ADT", "TronTestADT_type")
			);
		archStructure.addComponent(
			createComponent("TronIssueADT", "Tron Issue ADT", "TronIssueADT_type")
			);
		archStructure.addComponent(
			createComponent("Schematron", "Schematron Analysis Tool", "Schematron_type")
			);
		archStructure.addComponent(
			createComponent("TronGUI", "Tron GUI", "TronGUI_type")
			);
		archStructure.addComponent(
			createComponent("PerformanceTest", "Performance Test", "PerformanceTest_type")
			);
		archStructure.addComponent(
			createComponent("EditorPrefs", "Editor Preferences", "EditorPrefs_type")
			);
		archStructure.addComponent(
			createComponent("Archon", "Archon", "Archon_type")
			);
		archStructure.addComponent(
			createComponent("ArchonGUI", "Archon GUI", "ArchonGUI_type")
			);
		archStructure.addComponent(
			createComponent("VisioAgent", "Visio Agent", "VisioAgent_type")
			);
		if(hasArg("-menage")){
			archStructure.addComponent(
				createComponent("Menage", "Menage", "Menage_type")
				);
		}
		
		//archStructure.addConnector(
		//	createConnector("xArchADTBus", "xArchADT Bus Connector", "BusConnector_type")
		//);
		archStructure.addConnector(
			createConnector("xArchTransBus", "xArchADT Transaction Manager Bus Connector", "BusConnector_type")
			);
		archStructure.addConnector(
			createConnector("ConvenienceBus", "Convenience Bus Connector", "BusConnector_type")
			);
		archStructure.addConnector(
			createConnector("TrackerBus", "Tracker Bus Connector", "BusConnector_type")
			);
		archStructure.addConnector(
			createConnector("AnalysisADTBus", "Analysis ADT Bus Connector", "BusConnector_type")
			);
		archStructure.addConnector(
			createConnector("AnalysisToolBus", "Analysis Tool Bus Connector", "BusConnector_type")
			);
		archStructure.addConnector(
			createConnector("NoUIBus", "No-UI Tools Bus Connector", "BusConnector_type")
			);
		archStructure.addConnector(
			createConnector("GUIBus", "GUI Tools Bus Connector", "BusConnector_type")
			);
		archStructure.addConnector(
			createConnector("EditorBus", "Editor Tools Bus Connector", "BusConnector_type")
			);
		archStructure.addConnector(
			createConnector("PreferencesBus", "Preference Management Bus Connector", "BusConnector_type")
			);
		archStructure.addConnector(
			createConnector("VisioConnector", "Visio Connector", "COMBottomConnector_type", true, false)
			);
		
		//archStructure.addLink(createLink("xArchADT", "xArchADTBus"));
		
		//archStructure.addLink(createLink("xArchADTBus", "xArchTrans"));
		archStructure.addLink(createLink("Preferences", "xArchTransBus"));
		archStructure.addLink(createLink("EditorADT", "xArchTransBus"));
		archStructure.addLink(createLink("xArchTrans", "xArchTransBus"));
		
		archStructure.addLink(createLink("xArchTransBus", "BooleanEval"));
		archStructure.addLink(createLink("BooleanEval", "ConvenienceBus"));
		
		archStructure.addLink(createLink("xArchTransBus", "BooleanNotation"));
		archStructure.addLink(createLink("BooleanNotation", "ConvenienceBus"));
		
		archStructure.addLink(createLink("ConvenienceBus", "GuardTracker"));
		archStructure.addLink(createLink("GuardTracker", "TrackerBus"));
		
		archStructure.addLink(createLink("TrackerBus", "TronIssueADT"));
		archStructure.addLink(createLink("TronIssueADT", "AnalysisADTBus"));
		
		archStructure.addLink(createLink("TrackerBus", "TronTestADT"));
		archStructure.addLink(createLink("TronTestADT", "AnalysisADTBus"));

		archStructure.addLink(createLink("AnalysisADTBus", "Schematron"));
		archStructure.addLink(createLink("Schematron", "AnalysisToolBus"));
		
		archStructure.addLink(createLink("AnalysisToolBus", "AEM"));
		archStructure.addLink(createLink("AEM", "NoUIBus"));
		
		archStructure.addLink(createLink("AnalysisToolBus", "Selector"));
		archStructure.addLink(createLink("Selector", "NoUIBus"));
		
		archStructure.addLink(createLink("AnalysisToolBus", "Pruner"));
		archStructure.addLink(createLink("Pruner", "NoUIBus"));
		
		archStructure.addLink(createLink("AnalysisToolBus", "PruneVersions"));
		archStructure.addLink(createLink("PruneVersions", "NoUIBus"));
		
		archStructure.addLink(createLink("AnalysisToolBus", "ArchDiff"));
		archStructure.addLink(createLink("ArchDiff", "NoUIBus"));
		
		archStructure.addLink(createLink("AnalysisToolBus", "ArchMerge"));
		archStructure.addLink(createLink("ArchMerge", "NoUIBus"));
		
		archStructure.addLink(createLink("AnalysisToolBus", "PLADiff"));
		archStructure.addLink(createLink("PLADiff", "NoUIBus"));
		
		archStructure.addLink(createLink("AnalysisToolBus", "PLAMerge"));
		archStructure.addLink(createLink("PLAMerge", "NoUIBus"));
		
		archStructure.addLink(createLink("AnalysisToolBus", "EditorPrefs"));
		archStructure.addLink(createLink("EditorPrefs", "NoUIBus"));
		
		archStructure.addLink(createLink("AnalysisToolBus", "Archon"));
		archStructure.addLink(createLink("Archon", "NoUIBus"));
		
		archStructure.addLink(createLink("AnalysisToolBus", "GraphLayout"));
		archStructure.addLink(createLink("GraphLayout", "NoUIBus"));
		
		archStructure.addLink(createLink("NoUIBus", "TronGUI"));
		archStructure.addLink(createLink("TronGUI", "GUIBus"));
		
		archStructure.addLink(createLink("NoUIBus", "AEMDriver"));
		archStructure.addLink(createLink("AEMDriver", "GUIBus"));
		
		archStructure.addLink(createLink("NoUIBus", "HelloWorld"));
		archStructure.addLink(createLink("HelloWorld", "GUIBus"));
		
		archStructure.addLink(createLink("NoUIBus", "PerformanceTest"));
		archStructure.addLink(createLink("PerformanceTest", "GUIBus"));
		
		archStructure.addLink(createLink("NoUIBus", "ArchonGUI"));
		archStructure.addLink(createLink("ArchonGUI", "GUIBus"));
		
		archStructure.addLink(createLink("NoUIBus", "TestTrans"));
		archStructure.addLink(createLink("TestTrans", "GUIBus"));
		
		archStructure.addLink(createLink("NoUIBus", "SelectorDriver"));
		archStructure.addLink(createLink("SelectorDriver", "GUIBus"));
		
		archStructure.addLink(createLink("NoUIBus", "ArchDiffDriver"));
		archStructure.addLink(createLink("ArchDiffDriver", "GUIBus"));
		
		archStructure.addLink(createLink("NoUIBus", "ArchMergeDriver"));
		archStructure.addLink(createLink("ArchMergeDriver", "GUIBus"));
		
		archStructure.addLink(createLink("NoUIBus", "PLADiffDriver"));
		archStructure.addLink(createLink("PLADiffDriver", "GUIBus"));
		
		archStructure.addLink(createLink("NoUIBus", "PLAMergeDriver"));
		archStructure.addLink(createLink("PLAMergeDriver", "GUIBus"));

		archStructure.addLink(createLink("NoUIBus", "TypeWrangler"));
		archStructure.addLink(createLink("TypeWrangler", "GUIBus"));
		
		archStructure.addLink(createLink("GUIBus", "ArchEdit"));
		archStructure.addLink(createLink("ArchEdit", "EditorBus"));
		
		if(hasArg("-menage")){
			archStructure.addLink(createLink("GUIBus", "Menage"));
			archStructure.addLink(createLink("Menage", "EditorBus"));
		}

		archStructure.addLink(createLink("GUIBus", "Archipelago"));
		archStructure.addLink(createLink("Archipelago", "EditorBus"));
		
		archStructure.addLink(createLink("EditorBus", "PreferencesGUI"));
		archStructure.addLink(createLink("PreferencesGUI", "PreferencesBus"));
		
		archStructure.addLink(createLink("PreferencesBus", "FileManager"));
		
		archStructure.addLink(createLink("GUIBus", "VisioAgent"));
		archStructure.addLink(createLink("VisioAgent", "VisioConnector"));
		
		//Bus-to-Bus Links
		archStructure.addLink(createLink("xArchTransBus", "ConvenienceBus"));
		archStructure.addLink(createLink("ConvenienceBus", "TrackerBus"));
		archStructure.addLink(createLink("TrackerBus", "AnalysisADTBus"));
		archStructure.addLink(createLink("AnalysisADTBus", "AnalysisToolBus"));
		archStructure.addLink(createLink("AnalysisToolBus", "NoUIBus"));
		archStructure.addLink(createLink("NoUIBus", "GUIBus"));
		archStructure.addLink(createLink("GUIBus", "EditorBus"));
		archStructure.addLink(createLink("EditorBus", "PreferencesBus"));
		
		return xArch;
	}
	
	private static IDescription createDescription(String value){
		IDescription desc = types.createDescription();
		desc.setValue(value);
		return desc;
	}
	
	private static ILink createLink(String topId, String bottomId){
		ILink link = types.createLink();
		link.setId(topId + "_to_" + bottomId);
		link.setDescription(createDescription(topId + " to " + bottomId + " Link"));
		
		IPoint point1 = types.createPoint();
		IXMLLink anchor1 = instance.createXMLLink();
		anchor1.setType("simple");
		anchor1.setHref("#" + topId + ".IFACE_BOTTOM");
		point1.setAnchorOnInterface(anchor1);
		
		IPoint point2 = types.createPoint();
		IXMLLink anchor2 = instance.createXMLLink();
		anchor2.setType("simple");
		anchor2.setHref("#" + bottomId + ".IFACE_TOP");
		point2.setAnchorOnInterface(anchor2);
		
		link.addPoint(point1);
		link.addPoint(point2);
		return link;
	}
	
	private static IComponent createComponent(String id, String description, String typeId){
		IComponent c = types.createComponent();
		
		c.setId(id);
		IDescription desc = types.createDescription();
		desc.setValue(description);
		c.setDescription(desc);
		
		IInterface topIface = types.createInterface();
		topIface.setId(id + ".IFACE_TOP");
		topIface.setDescription(createDescription(description + " Top Interface"));
		IDirection topDirection = types.createDirection();
		topDirection.setValue("inout");
		topIface.setDirection(topDirection);
		
		IXMLLink topIfaceSig = types.createXMLLink();
		topIfaceSig.setType("simple");
		topIfaceSig.setHref("#" + typeId + "_topSig");
		topIface.setSignature(topIfaceSig);
		
		IXMLLink topIfaceType = types.createXMLLink();
		topIfaceType.setType("simple");
		topIfaceType.setHref("#C2TopType");
		
		topIface.setType(topIfaceType);
		
		IInterface bottomIface = types.createInterface();
		bottomIface.setId(id + ".IFACE_BOTTOM");
		bottomIface.setDescription(createDescription(description + " Bottom Interface"));
		IDirection bottomDirection = types.createDirection();
		bottomDirection.setValue("inout");
		bottomIface.setDirection(bottomDirection);
		
		IXMLLink bottomIfaceSig = types.createXMLLink();
		bottomIfaceSig.setType("simple");
		bottomIfaceSig.setHref("#" + typeId + "_bottomSig");
		bottomIface.setSignature(bottomIfaceSig);

		IXMLLink bottomIfaceType = types.createXMLLink();
		bottomIfaceType.setType("simple");
		bottomIfaceType.setHref("#C2BottomType");
		
		bottomIface.setType(bottomIfaceType);
		
		c.addInterface(topIface);
		c.addInterface(bottomIface);
		
		IXMLLink cType = types.createXMLLink();
		cType.setType("simple");
		cType.setHref("#" + typeId);
		c.setType(cType);
		return c;
	}
	
	private static IConnector createConnector(String id, String description, String typeId){
		return createConnector(id, description, typeId, true, true);
	}
	
	private static IConnector createConnector(String id, String description, String typeId, boolean hasTop, boolean hasBottom){
		IConnector c = types.createConnector();
		
		c.setId(id);
		IDescription desc = types.createDescription();
		desc.setValue(description);
		c.setDescription(desc);
		
		if(hasTop){
			IInterface topIface = types.createInterface();
			topIface.setId(id + ".IFACE_TOP");
			topIface.setDescription(createDescription(description + " Top Interface"));
			IDirection topDirection = types.createDirection();
			topDirection.setValue("inout");
			topIface.setDirection(topDirection);
			
			IXMLLink topIfaceSig = types.createXMLLink();
			topIfaceSig.setType("simple");
			topIfaceSig.setHref("#" + typeId + "_topSig");
			topIface.setSignature(topIfaceSig);

			IXMLLink topIfaceType = types.createXMLLink();
			topIfaceType.setType("simple");
			topIfaceType.setHref("#C2TopType");
			
			topIface.setType(topIfaceType);
			c.addInterface(topIface);
		}
		
		if(hasBottom){
			IInterface bottomIface = types.createInterface();
			bottomIface.setId(id + ".IFACE_BOTTOM");
			bottomIface.setDescription(createDescription(description + " Bottom Interface"));
			IDirection bottomDirection = types.createDirection();
			bottomDirection.setValue("inout");
			bottomIface.setDirection(bottomDirection);
			
			IXMLLink bottomIfaceSig = types.createXMLLink();
			bottomIfaceSig.setType("simple");
			bottomIfaceSig.setHref("#" + typeId + "_bottomSig");
			bottomIface.setSignature(bottomIfaceSig);

			IXMLLink bottomIfaceType = types.createXMLLink();
			bottomIfaceType.setType("simple");
			bottomIfaceType.setHref("#C2BottomType");
			
			bottomIface.setType(bottomIfaceType);
			
			c.addInterface(bottomIface);
		}
		
		IXMLLink cType = types.createXMLLink();
		cType.setType("simple");
		cType.setHref("#" + typeId);
		c.setType(cType);
		return c;
	}
	
	private static IComponentType createComponentType(String id, String description, String javaClassName){
		IComponentType ct0 = types.createComponentType();
		IVariantComponentType ct1 = variants.promoteToVariantComponentType(ct0);
		IVariantComponentTypeImpl ct = implementation.promoteToVariantComponentTypeImpl(ct1);
		
		ct.setId(id);
		IDescription desc = types.createDescription();
		desc.setValue(description);
		ct.setDescription(desc);
		
		ISignature topSig = types.createSignature();
		IDescription dSig = types.createDescription();
		dSig.setValue(id + "_topSig");
		topSig.setDescription(dSig);
		IDirection topDirection = types.createDirection();
		topDirection.setValue("inout");
		topSig.setId(id + "_topSig");
		topSig.setDirection(topDirection);
		
		IXMLLink topSigType = types.createXMLLink();
		topSigType.setType("simple");
		topSigType.setHref("#C2TopType");
		
		topSig.setType(topSigType);
		
		ISignature botSig = types.createSignature();
		dSig = types.createDescription();
		dSig.setValue(id + "_bottomSig");
		botSig.setDescription(dSig);
		IDirection botDirection = types.createDirection();
		botDirection.setValue("inout");
		botSig.setId(id + "_bottomSig");
		botSig.setDirection(botDirection);
		
		IXMLLink botSigType = types.createXMLLink();
		botSigType.setType("simple");
		botSigType.setHref("#C2BottomType");
		botSig.setType(botSigType);
		
		ct.addSignature(topSig);
		ct.addSignature(botSig);
		
		IJavaImplementation javaImplementation = javaimplementation.createJavaImplementation();
		IJavaClassFile javaClassFile = javaimplementation.createJavaClassFile();
		IJavaClassName jcn = javaimplementation.createJavaClassName();
		jcn.setValue(javaClassName);
		javaClassFile.setJavaClassName(jcn);
		javaImplementation.setMainClass(javaClassFile);
		
		ct.addImplementation(javaImplementation);
		
		return ct;
	}
	
	private static IConnectorType createConnectorType(String id, String description, String javaClassName){
		return createConnectorType(id, description, javaClassName, true, true);
	}
	
	private static IConnectorType createConnectorType(String id, String description, String javaClassName, boolean hasTop, boolean hasBottom){
		IConnectorType ct0 = types.createConnectorType();
		IVariantConnectorType ct1 = variants.promoteToVariantConnectorType(ct0);
		IVariantConnectorTypeImpl ct = implementation.promoteToVariantConnectorTypeImpl(ct1);
		
		ct.setId(id);
		IDescription desc = types.createDescription();
		desc.setValue(description);
		ct.setDescription(desc);
		
		if(hasTop){
			ISignature topSig = types.createSignature();
			IDescription dSig = types.createDescription();
			dSig.setValue(id + "_topSig");
			topSig.setDescription(dSig);
			IDirection topDirection = types.createDirection();
			topDirection.setValue("inout");
			topSig.setId(id + "_topSig");
			topSig.setDirection(topDirection);
			
			IXMLLink topSigType = types.createXMLLink();
			topSigType.setType("simple");
			topSigType.setHref("#C2TopType");
			
			topSig.setType(topSigType);
			ct.addSignature(topSig);
		}
		
		if(hasBottom){
			ISignature botSig = types.createSignature();
			IDescription dSig = types.createDescription();
			dSig.setValue(id + "_bottomSig");
			botSig.setDescription(dSig);
			IDirection botDirection = types.createDirection();
			botDirection.setValue("inout");
			botSig.setId(id + "_bottomSig");
			botSig.setDirection(botDirection);
			
			IXMLLink botSigType = types.createXMLLink();
			botSigType.setType("simple");
			botSigType.setHref("#C2BottomType");
			
			botSig.setType(botSigType);
			ct.addSignature(botSig);
		}
		
		IJavaImplementation javaImplementation = javaimplementation.createJavaImplementation();
		IJavaClassFile javaClassFile = javaimplementation.createJavaClassFile();
		IJavaClassName jcn = javaimplementation.createJavaClassName();
		jcn.setValue(javaClassName);
		javaClassFile.setJavaClassName(jcn);
		javaImplementation.setMainClass(javaClassFile);
		
		ct.addImplementation(javaImplementation);
		
		return ct;
	}
	
}

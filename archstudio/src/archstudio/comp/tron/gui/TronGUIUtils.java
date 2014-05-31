package archstudio.comp.tron.gui;

import archstudio.editors.FocusEditorMessage;
import archstudio.tron.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.tree.TreeNode;

import c2.util.MessageSendProxy;
import c2.util.UIDGenerator;

import edu.uci.ics.nativeutils.SystemUtils;
import edu.uci.ics.widgets.WidgetUtils;
import edu.uci.ics.xadlutils.Resources;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchBulkQuery;
import edu.uci.ics.xarchutils.XArchBulkQueryResultProxy;
import edu.uci.ics.xarchutils.XArchBulkQueryResults;
import edu.uci.ics.xarchutils.XArchFlatInterface;
import edu.uci.ics.xarchutils.XArchFlatQueryInterface;

public class TronGUIUtils{

	public static final Icon TRON_LOGO_32 = WidgetUtils.getImageIcon("archstudio/comp/tron/gui/res/tronlogo32.gif");
	
	protected static XArchBulkQuery getBulkQuery(ObjRef xArchRef){
		XArchBulkQuery q = new XArchBulkQuery(xArchRef);
		q.addQueryPath("archAnalysis*/analysis*/id");
		q.addQueryPath("archAnalysis*/analysis*/description/value");
		q.addQueryPath("archAnalysis*/analysis*/test*");
		q.addQueryPath("archAnalysis*/analysis*/test*/id");
		q.addQueryPath("archAnalysis*/analysis*/test*/enabled");
		q.addQueryPath("archAnalysis*/analysis*/test*/description/value");
		return q;
	}

	protected static XArchFlatQueryInterface runBulkQuery(XArchFlatInterface xarch, ObjRef xArchRef){
		XArchBulkQuery q = getBulkQuery(xArchRef);
		XArchBulkQueryResults qr = xarch.bulkQuery(q);
		return new XArchBulkQueryResultProxy(xarch, qr);
	}
	
	public static void makeDocTestApplied(XArchFlatInterface xarch, ObjRef xArchRef, TronTest testToUpdate, Boolean isEnabled){
		try{
			ObjRef analysisContext = xarch.createContext(xArchRef, "analysis");
			ObjRef tronAnalysisContext = xarch.createContext(xArchRef, "tronanalysis");

			//First see if we can find the test and just update it.
			ObjRef archAnalysisRef = null;
			ObjRef tronAnalysisRef = null;
			
			XArchFlatQueryInterface xarchbulk = runBulkQuery(xarch, xArchRef);
			archAnalysisRef = xarchbulk.getElement(analysisContext, "archAnalysis", xArchRef);
			if(archAnalysisRef != null){
				ObjRef[] analysisRefs = xarchbulk.getAll(archAnalysisRef, "analysis");
				if(analysisRefs != null){
					if(analysisRefs.length > 0){
						tronAnalysisRef = analysisRefs[0];
					}
					for(int i = 0; i < analysisRefs.length; i++){
						if(xarchbulk.isInstanceOf(analysisRefs[i], "edu.uci.isr.xarch.tronanalysis.ITronAnalysis")){
							//It's a tron analysis
							ObjRef[] testRefs = xarchbulk.getAll(analysisRefs[i], "test");
							if(testRefs != null){
								for(int j = 0; j < testRefs.length; j++){
									String testUID = (String)xarchbulk.get(testRefs[j], "id");
									if(testUID.equals(testToUpdate.getUID())){
										//Found it.  Just update it with the new enabled status.
										if(isEnabled != null){
											if(isEnabled.booleanValue()){
												xarch.set(testRefs[j], "enabled", "true");
												return;
											}
											else{
												xarch.set(testRefs[j], "enabled", "false");
												return;
											}
										}
									}
								}
							}
						}
					}
				}
			}
			//We didn't find it. Create ancestor elements if necessary.
			if(archAnalysisRef == null){
				archAnalysisRef = xarch.createElement(analysisContext, "archAnalysis");
				xarch.add(xArchRef, "Object", archAnalysisRef);
			}
			if(tronAnalysisRef == null){
				tronAnalysisRef = xarch.create(tronAnalysisContext, "tronAnalysis");
				xarch.set(tronAnalysisRef, "id", UIDGenerator.generateUID("analysis"));
				XadlUtils.setDescription(xarch, tronAnalysisRef, "Tron Analysis Tests");
				xarch.add(archAnalysisRef, "Analysis", tronAnalysisRef);
			}
			//Okay, we have the ancestor elements, let's create the test.
			ObjRef newTestRef = xarch.create(tronAnalysisContext, "test");
			xarch.set(newTestRef, "id", testToUpdate.getUID());
			String descString = "Tool: " + testToUpdate.getToolID() + "; Category: " + testToUpdate.getCategory();
			XadlUtils.setDescription(xarch, newTestRef, descString);

			if((isEnabled == null) || (isEnabled.booleanValue())){
				xarch.set(newTestRef, "enabled", "true");
			}
			else{
				xarch.set(newTestRef, "enabled", "false");
			}
			xarch.add(tronAnalysisRef, "test", newTestRef);
		}
		catch(Exception e){
		}
	}

	public static void makeDocTestNotApplied(XArchFlatInterface xarch, ObjRef xArchRef, TronTest testToUpdate){
		try{
			ObjRef analysisContext = xarch.createContext(xArchRef, "analysis");
			ObjRef tronAnalysisContext = xarch.createContext(xArchRef, "tronanalysis");

			//First see if we can find the test and just update it.
			ObjRef archAnalysisRef = null;
			ObjRef tronAnalysisRef = null;
			
			XArchFlatQueryInterface xarchbulk = runBulkQuery(xarch, xArchRef);
			archAnalysisRef = xarchbulk.getElement(analysisContext, "archAnalysis", xArchRef);
			if(archAnalysisRef != null){
				ObjRef[] analysisRefs = xarchbulk.getAll(archAnalysisRef, "analysis");
				if(analysisRefs != null){
					if(analysisRefs.length > 0){
						tronAnalysisRef = analysisRefs[0];
					}
					for(int i = 0; i < analysisRefs.length; i++){
						if(xarchbulk.isInstanceOf(analysisRefs[i], "edu.uci.isr.xarch.tronanalysis.ITronAnalysis")){
							//It's a tron analysis
							ObjRef[] testRefs = xarchbulk.getAll(analysisRefs[i], "test");
							if(testRefs != null){
								for(int j = 0; j < testRefs.length; j++){
									String testUID = (String)xarchbulk.get(testRefs[j], "id");
									if(testUID.equals(testToUpdate.getUID())){
										//Found it.  Remove it.
										xarch.remove(analysisRefs[i], "test", testRefs[j]);
										return;
									}
								}
							}
						}
					}
				}
			}
		}
		catch(Exception e){
		}
	}
	
	public static TronGUIDocTest[] loadDocTests(XArchFlatInterface xarch, ObjRef xArchRef){
		List docTestList = new ArrayList();
		try{
			ObjRef analysisContext = xarch.createContext(xArchRef, "analysis");
			ObjRef tronAnalysisContext = xarch.createContext(xArchRef, "tronanalysis");

			XArchFlatQueryInterface xarchbulk = runBulkQuery(xarch, xArchRef);
			
			ObjRef archAnalysisRef = xarchbulk.getElement(analysisContext, "archAnalysis", xArchRef);
			if(archAnalysisRef != null){
				ObjRef[] analysisRefs = xarchbulk.getAll(archAnalysisRef, "analysis");
				if(analysisRefs != null){
					for(int i = 0; i < analysisRefs.length; i++){
						if(xarchbulk.isInstanceOf(analysisRefs[i], "edu.uci.isr.xarch.tronanalysis.ITronAnalysis")){
							//It's a tron analysis
							ObjRef[] testRefs = xarchbulk.getAll(analysisRefs[i], "test");
							if(testRefs != null){
								for(int j = 0; j < testRefs.length; j++){
									String testUID = (String)xarchbulk.get(testRefs[j], "id");
									String testDescription = XadlUtils.getDescription(xarchbulk, testRefs[j]);
									if(testDescription == null) testDescription = "[no data]";
									String enabled = (String)xarchbulk.get(testRefs[j], "enabled");
									if(enabled == null) enabled = "true";
									TronGUIDocTest newDocTest = new TronGUIDocTest(testUID, testDescription, enabled.equals("true"));
									docTestList.add(newDocTest);
								}
							}
						}
					}
				}
			}
		}
		catch(Exception e){
		}
		return (TronGUIDocTest[])docTestList.toArray(new TronGUIDocTest[0]);
	}

	public static TronGUITreeDocumentNode getDocumentNode(TreeNode testNode){
		TreeNode currentNode = testNode;
		while(true){
			if(currentNode == null){
				return null;
			}
			if(currentNode instanceof TronGUITreeDocumentNode){
				return (TronGUITreeDocumentNode)currentNode;
			}
			else{
				currentNode = currentNode.getParent();
			}
		}
	}
	
	public static TronGUITreeTestNode[] getAllTestNodes(TreeNode startNode){
		List l = new ArrayList();
		addAllChildTestNodes(startNode, l);
		return (TronGUITreeTestNode[])l.toArray(new TronGUITreeTestNode[0]);
	}
	
	private static void addAllChildTestNodes(TreeNode testNode, List l){
		if(testNode instanceof TronGUITreeTestNode){
			l.add((TronGUITreeTestNode)testNode);
		}
		for(Enumeration en = testNode.children(); en.hasMoreElements(); ){
			Object child = en.nextElement();
			if(child instanceof TreeNode){
				addAllChildTestNodes((TreeNode)child, l);
			}
		}
	}

	public static void makeAllDocTestsApplied(XArchFlatInterface xarch, ObjRef xArchRef, TreeNode startNode){
		TronGUITreeTestNode[] allTestNodes = getAllTestNodes(startNode);
		for(int i = 0; i < allTestNodes.length; i++){
			TronTest test = allTestNodes[i].getTest();
			if(!allTestNodes[i].isApplied()){
				TronGUIUtils.makeDocTestApplied(xarch, xArchRef, test, null);
			}
		}
	}

	public static void makeAllDocTestsNotApplied(XArchFlatInterface xarch, ObjRef xArchRef, TreeNode startNode){
		TronGUITreeTestNode[] allTestNodes = getAllTestNodes(startNode);
		for(int i = 0; i < allTestNodes.length; i++){
			TronTest test = allTestNodes[i].getTest();
			if(allTestNodes[i].isApplied()){
				TronGUIUtils.makeDocTestNotApplied(xarch, xArchRef, test);
			}
		}
	}

	public static void makeAllAppliedDocTestsDisabled(XArchFlatInterface xarch, ObjRef xArchRef, TreeNode startNode){
		TronGUITreeTestNode[] allTestNodes = getAllTestNodes(startNode);
		for(int i = 0; i < allTestNodes.length; i++){
			TronTest test = allTestNodes[i].getTest();
			if(allTestNodes[i].isApplied()){
				if(allTestNodes[i].isEnabled()){
					TronGUIUtils.makeDocTestApplied(xarch, xArchRef, test, Boolean.FALSE);
				}
			}
		}
	}

	public static void makeAllAppliedDocTestsEnabled(XArchFlatInterface xarch, ObjRef xArchRef, TreeNode startNode){
		TronGUITreeTestNode[] allTestNodes = getAllTestNodes(startNode);
		for(int i = 0; i < allTestNodes.length; i++){
			TronTest test = allTestNodes[i].getTest();
			if(allTestNodes[i].isApplied()){
				if(!allTestNodes[i].isEnabled()){
					TronGUIUtils.makeDocTestApplied(xarch, xArchRef, test, Boolean.TRUE);
				}
			}
		}
	}
	
	public static String[] getTestUIDsToRun(TronGUITreeDocumentNode docNode){
		TronGUITreeTestNode[] testNodes = getAllTestNodes(docNode);
		List l = new ArrayList();
		for(int i = 0; i < testNodes.length; i++){
			TronTest test = testNodes[i].getTest();
			if((test != null) && testNodes[i].isApplied() && testNodes[i].isEnabled()){
				l.add(test.getUID());
			}
		}
		return (String[])l.toArray(new String[0]);
	}
	
	public static class ApplyTestActionListener implements ActionListener{
		protected XArchFlatInterface xarch;
		protected ObjRef xArchRef;
		protected TronTest test;
		
		public ApplyTestActionListener(XArchFlatInterface xarch, ObjRef xArchRef, TronTest test){
			this.xarch = xarch;
			this.xArchRef = xArchRef;
			this.test = test;
		}
		
		public void actionPerformed(ActionEvent evt){
			TronGUIUtils.makeDocTestApplied(xarch, xArchRef, test, null);
		}
	}
	
	public static class UnApplyTestActionListener implements ActionListener{
		protected XArchFlatInterface xarch;
		protected ObjRef xArchRef;
		protected TronTest test;
		
		public UnApplyTestActionListener(XArchFlatInterface xarch, ObjRef xArchRef, TronTest test){
			this.xarch = xarch;
			this.xArchRef = xArchRef;
			this.test = test;
		}
		
		public void actionPerformed(ActionEvent evt){
			TronGUIUtils.makeDocTestNotApplied(xarch, xArchRef, test);
		}
	}
	
	public static class EnableTestActionListener implements ActionListener{
		protected XArchFlatInterface xarch;
		protected ObjRef xArchRef;
		protected TronTest test;
		
		public EnableTestActionListener(XArchFlatInterface xarch, ObjRef xArchRef, TronTest test){
			this.xarch = xarch;
			this.xArchRef = xArchRef;
			this.test = test;
		}
		
		public void actionPerformed(ActionEvent evt){
			TronGUIUtils.makeDocTestApplied(xarch, xArchRef, test, Boolean.TRUE);
		}
	}

	static class DisableTestActionListener implements ActionListener{
		protected XArchFlatInterface xarch;
		protected ObjRef xArchRef;
		protected TronTest test;
		
		public DisableTestActionListener(XArchFlatInterface xarch, ObjRef xArchRef, TronTest test){
			this.xarch = xarch;
			this.xArchRef = xArchRef;
			this.test = test;
		}
		
		public void actionPerformed(ActionEvent evt){
			TronGUIUtils.makeDocTestApplied(xarch, xArchRef, test, Boolean.FALSE);
		}
	}
	
	public static class ApplyAllTestsActionListener implements ActionListener{
		protected XArchFlatInterface xarch;
		protected ObjRef xArchRef;
		protected TreeNode startNode;
		
		public ApplyAllTestsActionListener(XArchFlatInterface xarch, ObjRef xArchRef, TreeNode startNode){
			this.xarch = xarch;
			this.xArchRef = xArchRef;
			this.startNode = startNode;
		}
		
		public void actionPerformed(ActionEvent evt){
			TronGUIUtils.makeAllDocTestsApplied(xarch, xArchRef, startNode);
		}
	}

	public static class UnApplyAllTestsActionListener implements ActionListener{
		protected XArchFlatInterface xarch;
		protected ObjRef xArchRef;
		protected TreeNode startNode;
		
		public UnApplyAllTestsActionListener(XArchFlatInterface xarch, ObjRef xArchRef, TreeNode startNode){
			this.xarch = xarch;
			this.xArchRef = xArchRef;
			this.startNode = startNode;
		}
		
		public void actionPerformed(ActionEvent evt){
			TronGUIUtils.makeAllDocTestsNotApplied(xarch, xArchRef, startNode);
		}
	}

	public static class DisableAllTestsActionListener implements ActionListener{
		protected XArchFlatInterface xarch;
		protected ObjRef xArchRef;
		protected TreeNode startNode;
		
		public DisableAllTestsActionListener(XArchFlatInterface xarch, ObjRef xArchRef, TreeNode startNode){
			this.xarch = xarch;
			this.xArchRef = xArchRef;
			this.startNode = startNode;
		}
		
		public void actionPerformed(ActionEvent evt){
			TronGUIUtils.makeAllAppliedDocTestsDisabled(xarch, xArchRef, startNode);
		}
	}

	public static class EnableAllTestsActionListener implements ActionListener{
		protected XArchFlatInterface xarch;
		protected ObjRef xArchRef;
		protected TreeNode startNode;
		
		public EnableAllTestsActionListener(XArchFlatInterface xarch, ObjRef xArchRef, TreeNode startNode){
			this.xarch = xarch;
			this.xArchRef = xArchRef;
			this.startNode = startNode;
		}
		
		public void actionPerformed(ActionEvent evt){
			TronGUIUtils.makeAllAppliedDocTestsEnabled(xarch, xArchRef, startNode);
		}
	}
	
	protected static JMenuItem[] getChangeTestStateMenuItems(XArchFlatInterface xarch, TronGUITreeTestNode node){
		TronGUITreeDocumentNode docNode = TronGUIUtils.getDocumentNode(node);
		ObjRef xArchRef = null;
		if(docNode != null){
			xArchRef = docNode.getDocumentRef();
		}
		JMenuItem bApplyTest = new JMenuItem("Apply Test", TronGUITreeTestNode.APPLIED_DOCUMENT_ICON);
		JMenuItem bDisableTest = new JMenuItem("Disable Test", TronGUITreeTestNode.DISABLED_DOCUMENT_ICON);
		JMenuItem bEnableTest = new JMenuItem("Enable Test", TronGUITreeTestNode.APPLIED_DOCUMENT_ICON);
		JMenuItem bUnApplyTest = new JMenuItem("Un-apply Test", TronGUITreeTestNode.NOT_APPLIED_DOCUMENT_ICON);
		if(xArchRef != null){
			bApplyTest.addActionListener(new TronGUIUtils.ApplyTestActionListener(xarch, xArchRef, node.getTest()));
			bUnApplyTest.addActionListener(new TronGUIUtils.UnApplyTestActionListener(xarch, xArchRef, node.getTest()));
			bDisableTest.addActionListener(new TronGUIUtils.DisableTestActionListener(xarch, xArchRef, node.getTest()));
			bEnableTest.addActionListener(new TronGUIUtils.EnableTestActionListener(xarch, xArchRef, node.getTest()));
		}
		else{
			bApplyTest.setEnabled(false);
			bDisableTest.setEnabled(false);
			bEnableTest.setEnabled(false);
			bUnApplyTest.setEnabled(false);
		}
		
		if(node.isApplied()){
			bApplyTest.setEnabled(false);
			if(node.isEnabled()){
				bEnableTest.setEnabled(false);
			}
			else{
				bDisableTest.setEnabled(false);
			}
		}
		else{
			bUnApplyTest.setEnabled(false);
			bDisableTest.setEnabled(false);
			bEnableTest.setEnabled(false);
		}
		return new JMenuItem[]{bApplyTest, bUnApplyTest, bEnableTest, bDisableTest};
	}
	
	protected static JMenuItem[] getChangeTestGroupStateMenuItems(XArchFlatInterface xarch, TreeNode node){
		TronGUITreeDocumentNode docNode = TronGUIUtils.getDocumentNode(node);
		ObjRef xArchRef = null;
		if(docNode != null){
			xArchRef = docNode.getDocumentRef();
		}
		JMenuItem bApplyAll = new JMenuItem("Apply All Tests", TronGUITreeTestNode.APPLIED_FOLDER_ICON);
		JMenuItem bUnApplyAll = new JMenuItem("Un-apply All Tests", TronGUITreeTestNode.NOT_APPLIED_FOLDER_ICON);
		JMenuItem bDisableAll = new JMenuItem("Disable All Applied Tests", TronGUITreeTestNode.DISABLED_FOLDER_ICON);
		JMenuItem bEnableAll = new JMenuItem("Enable All Applied Tests", TronGUITreeTestNode.APPLIED_FOLDER_ICON);

		if(xArchRef != null){
			bApplyAll.addActionListener(new TronGUIUtils.ApplyAllTestsActionListener(xarch, xArchRef, node));
			bUnApplyAll.addActionListener(new TronGUIUtils.UnApplyAllTestsActionListener(xarch, xArchRef, node));
			bDisableAll.addActionListener(new TronGUIUtils.DisableAllTestsActionListener(xarch, xArchRef, node));
			bEnableAll.addActionListener(new TronGUIUtils.EnableAllTestsActionListener(xarch, xArchRef, node));
		}
		else{
			bApplyAll.setEnabled(false);
			bUnApplyAll.setEnabled(false);
			bDisableAll.setEnabled(false);
			bEnableAll.setEnabled(false);
		}
		
		return new JMenuItem[]{bApplyAll, bUnApplyAll, bEnableAll, bDisableAll};
	}
	
	protected static TronRunTestsMessage getRunTestsMessage(TronGUITreeDocumentNode docNode){
		ObjRef xArchRef = docNode.getDocumentRef();
		List docTestList = new ArrayList();
		
		TronGUIDocTest[] docTests = docNode.getDocTests();
		for(int i = 0; i < docTests.length; i++){
			if(docTests[i].isEnabled()){
				docTestList.add(docTests[i].getTestUID());
			}
		}
		String[] testUIDs = (String[])docTestList.toArray(new String[0]);
		TronRunTestsMessage trtm = new TronRunTestsMessage(xArchRef, testUIDs);
		return trtm;
	}

	public static class TestAllActionListener implements ActionListener{
		protected MessageSendProxy requestProxy;
		protected TronGUITreeDocumentNode[] docNodes;
		protected TronGUITestErrorBar errorBar;
		
		public TestAllActionListener(MessageSendProxy requestProxy, TronGUITreeDocumentNode[] docNodes,
		TronGUITestErrorBar errorBar){
			this.requestProxy = requestProxy;
			this.docNodes = docNodes;
			this.errorBar = errorBar;
		}
		
		public void actionPerformed(ActionEvent evt){
			if(errorBar != null){
				errorBar.hide();
			}
			for(int i = 0; i < docNodes.length; i++){
				TronRunTestsMessage trtm = getRunTestsMessage(docNodes[i]);  
				requestProxy.send(trtm);
			}
		}
	}
	
	public static class TestDocActionListener implements ActionListener{
		protected MessageSendProxy requestProxy;
		protected TronGUITreeDocumentNode docNode;
		protected TronGUITestErrorBar errorBar;
		
		public TestDocActionListener(MessageSendProxy requestProxy, TronGUITreeDocumentNode docNode,
		TronGUITestErrorBar errorBar){
			this.requestProxy = requestProxy;
			this.docNode = docNode;
			this.errorBar = errorBar;
		}
		
		public void actionPerformed(ActionEvent evt){
			if(errorBar != null){
				errorBar.hide();
			}
			TronRunTestsMessage trtm = getRunTestsMessage(docNode);  
			requestProxy.send(trtm);
		}
	}
	
	protected static JMenuItem[] getRunDocTestMenuItems(MessageSendProxy requestProxy, 
	TronGUITreeDocumentNode docNodes[], TronGUITestErrorBar errorBar){
		JMenuItem[] menuItems = new JMenuItem[docNodes.length];
		for(int i = 0; i < docNodes.length; i++){
			JMenuItem mi = new JMenuItem(docNodes[i].toString(), docNodes[i].getIcon());
			mi.addActionListener(new TestDocActionListener(requestProxy, docNodes[i], errorBar));
			menuItems[i] = mi;
		}
		return menuItems;
	}
	
	public static Icon getIconByURL(String href){
		java.io.InputStream is = null;
		try{
			href = href.trim();
			is = SystemUtils.openURL(href);
			if(is != null){
				Icon icon = WidgetUtils.getImageIcon(is);
				return icon;
			}
			return null;
		}
		catch(Exception e){
			return null;
		}
		finally{
			try{
				if(is != null) is.close();
			}
			catch(Exception e){}
		}
	}

	protected static JMenuItem[] getFocusEditorMenuItems(MessageSendProxy notificationProxy,
	ObjRef xArchRef, ObjRef elementRef, TronGUIEditorModel editorModel){
		String[] editorIDs = editorModel.getActiveEditorIDs();
		
		if(editorIDs.length == 0){
			JMenuItem mi = new JMenuItem("[No Editors Available]");
			mi.setEnabled(false);
			return new JMenuItem[]{mi};
		}
		JMenuItem[] menuItems = new JMenuItem[editorIDs.length];
		for(int i = 0; i < editorIDs.length; i++){
			menuItems[i] = new JMenuItem(editorIDs[i]);
			menuItems[i].setIcon(Resources.EDIT_ICON);
			menuItems[i].addActionListener(
				new FocusEditorActionListener(notificationProxy, xArchRef, elementRef, editorIDs[i]));
		}
		return menuItems;
	}

	public static class FocusEditorActionListener implements ActionListener{
		protected MessageSendProxy notificationProxy;
		protected ObjRef xArchRef;
		protected ObjRef elementRef;
		protected String editorID;
		
		public FocusEditorActionListener(MessageSendProxy notificationProxy,
		ObjRef xArchRef, ObjRef elementRef, String editorID){
			this.notificationProxy = notificationProxy;
			this.xArchRef = xArchRef;
			this.elementRef = elementRef;
			this.editorID = editorID;
		}
		
		public void actionPerformed(ActionEvent evt){
			FocusEditorMessage fem = new FocusEditorMessage(new String[]{editorID}, xArchRef, elementRef, 
				FocusEditorMessage.FOCUS_OPEN_EDITORS);
			notificationProxy.send(fem);
		}
	}
}

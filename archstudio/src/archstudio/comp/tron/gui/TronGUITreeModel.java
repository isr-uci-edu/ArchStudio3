package archstudio.comp.tron.gui;

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import c2.util.MessageSendProxy;

import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;
import archstudio.tron.TronAllTestsMessage;
import archstudio.tron.TronRemoveIssuesMessage;
import archstudio.tron.TronTest;
import archstudio.tron.TronTestListDiff;
import archstudio.tron.TronTestsChangedMessage;

import edu.uci.ics.widgets.IIconable;
import edu.uci.ics.widgets.WidgetUtils;
import edu.uci.ics.xadlutils.Resources;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchBulkQuery;
import edu.uci.ics.xarchutils.XArchBulkQueryResultProxy;
import edu.uci.ics.xarchutils.XArchBulkQueryResults;
import edu.uci.ics.xarchutils.XArchFileEvent;
import edu.uci.ics.xarchutils.XArchFlatEvent;
import edu.uci.ics.xarchutils.XArchFlatInterface;
import edu.uci.ics.xarchutils.XArchFlatQueryInterface;
import edu.uci.ics.xarchutils.XArchPath;

public class TronGUITreeModel extends DefaultTreeModel{

	protected XArchFlatTransactionsInterface xarch;
	protected MessageSendProxy requestProxy;
	
	//Maps UIDs to TronTests
	protected Map tronTestMap = new HashMap();

	protected TronTreeRootNode rootNode = new TronTreeRootNode("Tron");
	
	protected static class TronTreeRootNode extends DefaultMutableTreeNode implements IIconable{
		public TronTreeRootNode(Object userObject){
			super(userObject);
		}
		
		public boolean getAllowsChildren(){
			return true;
		}
		
		public javax.swing.Icon getIcon(){
			return Resources.FOLDER_ICON;
		}
	}
	
	public TronGUITreeModel(XArchFlatTransactionsInterface xarch, MessageSendProxy requestProxy){
		super(null, true);
		super.setRoot(rootNode);
		this.xarch = xarch;
		this.requestProxy = requestProxy;
	}
	
	
	private static TronGUITreeTestNode getTestNode(TronGUITreeDocumentNode docNode, TronTest test){
		String[] pathSegments = TronTest.getCategoryPathComponents(test.getCategory());
		MutableTreeNode currentNode = docNode;
		for(int i = 0; i < pathSegments.length; i++){
			boolean found = false;
			for(int j = 0; j < currentNode.getChildCount(); j++){
				Object childObject = currentNode.getChildAt(j);
				if(childObject instanceof TronGUITreeTestNode){
					TronGUITreeTestNode testNode = (TronGUITreeTestNode)childObject;
					if(testNode.getUserObject().toString().equals(pathSegments[i])){
						currentNode = testNode;
						found = true;
						break;
					}
				}
			}
			if(!found){
				//Fell off the tree!
				return null;
			}
		}
		if((currentNode != null) && (currentNode instanceof TronGUITreeTestNode)){
			return (TronGUITreeTestNode)currentNode;
		}
		return null;
	}
	
	protected synchronized TronGUITreeTestNode getOrCreateTestNode(TronGUITreeDocumentNode docNode, TronTest test){
		String[] pathSegments = TronTest.getCategoryPathComponents(test.getCategory());
		MutableTreeNode currentNode = docNode;
		TreeNode nodeToRefresh = docNode;
		for(int i = 0; i < pathSegments.length; i++){
			boolean found = false;
			for(int j = 0; j < currentNode.getChildCount(); j++){
				Object childObject = currentNode.getChildAt(j);
				if(childObject instanceof TronGUITreeTestNode){
					TronGUITreeTestNode testNode = (TronGUITreeTestNode)childObject;
					if(testNode.getUserObject().toString().equals(pathSegments[i])){
						currentNode = testNode;
						nodeToRefresh = currentNode;
						found = true;
						break;
					}
				}
			}
			if(!found){
				//Fell off the tree; create intermediate node and continue.
				TronGUITreeTestNode newTreeTestNode = new TronGUITreeTestNode(pathSegments[i]);
				WidgetUtils.addTreeNodeAlphabetically(currentNode, newTreeTestNode);
				currentNode = newTreeTestNode;
			}
		}
		if((currentNode != null) && (currentNode instanceof TronGUITreeTestNode)){
			((TronGUITreeTestNode)currentNode).setTest(test);
			((TronGUITreeTestNode)currentNode).setApplied(false);
			((TronGUITreeTestNode)currentNode).setEnabled(false);
			fireNodeShouldRefresh(nodeToRefresh);
			return (TronGUITreeTestNode)currentNode;
		}
		fireNodeShouldRefresh(nodeToRefresh);
		throw new RuntimeException("This shouldn't happen.");
	}
	
	protected synchronized void removeTestNode(TronGUITreeDocumentNode docNode, TronTest testToRemove){
		TronGUITreeTestNode nodeToRemove = getTestNode(docNode, testToRemove);
		if(nodeToRemove != null){
			//See if it has children
			if(nodeToRemove.getChildCount() != 0){
				//It's an intermediate node so we can't remove it.  This sucks
				//and really shouldn't happen but we have to deal with it.
				nodeToRemove.setTest(null);
			}
			else{
				//It's a leaf.
				TreeNode parentNode = nodeToRemove.getParent();
				if(parentNode instanceof MutableTreeNode){
					((MutableTreeNode)parentNode).remove(nodeToRemove);
					//Remove empty parents
					while((parentNode instanceof TronGUITreeTestNode) &&
						(((TronGUITreeTestNode)parentNode).getTest() == null) &&
						(((TronGUITreeTestNode)parentNode).getChildCount() == 0)){
						
						TreeNode grandparentNode = parentNode.getParent();
						((MutableTreeNode)grandparentNode).remove((MutableTreeNode)parentNode);
						parentNode = grandparentNode;
					}
					fireNodeShouldRefresh(parentNode);
				}
			}
		}
	}
	
	protected synchronized TronGUITreeTestNode getTestNode(TronGUITreeDocumentNode docNode, String testUID){
		for(int i = 0; i < docNode.getChildCount(); i++){
			Object child = docNode.getChildAt(i);
			if(child instanceof TronGUITreeTestNode){
				TronGUITreeTestNode foundInChildren = getTestNode((TronGUITreeTestNode)child, testUID);
				if(foundInChildren != null){
					return foundInChildren;
				}
			}
		}
		return null;
	}
	
	private synchronized TronGUITreeTestNode getTestNode(TronGUITreeTestNode startNode, String testUID){
		if(startNode.getTest() != null){
			if(startNode.getTest().getUID().equals(testUID)){
				return startNode;
			}
		}
		for(int i = 0; i < startNode.getChildCount(); i++){
			Object child = startNode.getChildAt(i);
			if(child instanceof TronGUITreeTestNode){
				TronGUITreeTestNode foundInChildren = getTestNode((TronGUITreeTestNode)child, testUID);
				if(foundInChildren != null){
					return foundInChildren;
				}
			}
		}
		return null;
	}
	
	protected void updateTestNodeStatus(TronGUITreeDocumentNode docNode, TronGUIDocTest docTest, 
		boolean isApplied, boolean isEnabled){
		String testUID = docTest.getTestUID();
		TronGUITreeTestNode testNode = getTestNode(docNode, testUID);
		if(testNode != null){
			testNode.setApplied(isApplied);
			testNode.setEnabled(isEnabled);
			if(testNode.isUnknown){
				updateUnknownTestNode(docNode, docTest, isApplied, isEnabled);
			}
			fireNodeShouldRefresh(testNode);
		}
		else{
			//There's a test enabled that we don't have.
			updateUnknownTestNode(docNode, docTest, isApplied, isEnabled);
		}
	}
	
	protected void updateUnknownTestNode(TronGUITreeDocumentNode docNode, TronGUIDocTest docTest,
	boolean isApplied, boolean isEnabled){
		TronGUITreeTestNode unknownTestsParentNode = null; 
		for(int i = 0; i < docNode.getChildCount(); i++){
			TronGUITreeTestNode childNode = (TronGUITreeTestNode)docNode.getChildAt(i);
			if(childNode.toString().equals("[Unknown Tests]")){
				unknownTestsParentNode = childNode;
				break;
			}
		}
		if(unknownTestsParentNode == null){
			unknownTestsParentNode = new TronGUITreeTestNode("[Unknown Tests]");
			WidgetUtils.addTreeNodeAlphabetically(docNode, unknownTestsParentNode);
		}
		
		String desc = docTest.getTestDescription();
		if(desc == null){
			desc = docTest.getTestUID();
		}
		
		TronGUITreeTestNode unknownTestNode = null;
		for(int i = 0; i < unknownTestsParentNode.getChildCount(); i++){
			TronGUITreeTestNode childNode = (TronGUITreeTestNode)unknownTestsParentNode.getChildAt(i);
			if(childNode.getTest() != null){
				if(childNode.getTest().getUID().equals(docTest.getTestUID())){
					unknownTestNode = childNode;
					break;
				}
			}
		}
		if(unknownTestNode == null){
			TronTest virtualTest = new TronTest(docTest.getTestUID(), "unknown", "[Unknown Tests]/" + docTest.getTestUID(), desc);
			unknownTestNode = new TronGUITreeTestNode(virtualTest, isApplied, isEnabled);
			unknownTestNode.setUnknown(true);
			WidgetUtils.addTreeNodeAlphabetically(unknownTestsParentNode, unknownTestNode);
			TronTest[] tronTests = docNode.getTronTests();
			TronTest[] newTronTests = new TronTest[tronTests.length + 1];
			System.arraycopy(tronTests, 0, newTronTests, 1, tronTests.length);
			newTronTests[0] = virtualTest;
			docNode.setTronTests(newTronTests);
		}
		
		if(!isApplied){
			unknownTestsParentNode.remove(unknownTestNode);
			if(unknownTestsParentNode.getChildCount() == 0){
				docNode.remove(unknownTestsParentNode);
			}
		}
		fireNodeShouldRefresh(docNode);
	}
	
	public synchronized void refreshDocumentNode(TronGUITreeDocumentNode docNode){
		//First sync up the test nodes under the doc node.
		TronTest[] oldTests = docNode.getTronTests();
		TronTest[] newTests = getAllTests();
		
		//Figure out what tests were added/removed from this node since
		//we last synced up.
		TronTestListDiff testListDiff = TronTestListDiff.diffLists(oldTests, newTests);
		TronTest[] testsToRemove = testListDiff.getTestsToRemove();
		TronTest[] testsToAdd = testListDiff.getTestsToAdd();
		boolean testsChanged = (testsToRemove.length + testsToAdd.length) > 0;
		
		//Okay, now we know all the changes to the tests in the tree.  Let's apply those.
		//First, remove all tests that have disappeared.
		for(int i = 0; i < testsToRemove.length; i++){
			removeTestNode(docNode, testsToRemove[i]);
		}
		//Now add all the new tests that have appeared.
		for(int i = 0; i < testsToAdd.length; i++){
			getOrCreateTestNode(docNode, testsToAdd[i]);
		}
		//Set the new test set in the node.
		docNode.setTronTests(newTests);
		//OK, we've synced tests for this node.
		
		//Now we have to sync up the test status...
		ObjRef docRef = docNode.getDocumentRef();
		TronGUIDocTest[] oldDocTests = docNode.getDocTests();
		TronGUIDocTest[] newDocTests = TronGUIUtils.loadDocTests(xarch, docRef);
		
		TronGUIDocTestListDiff docTestListDiff = TronGUIDocTestListDiff.diffLists(oldDocTests, newDocTests);
		TronGUIDocTest[] docTestsToRemove = docTestListDiff.getRemovedDocTests();
		TronGUIDocTest[] docTestsToUpdate = docTestListDiff.getChangedDocTestsAfter();
		TronGUIDocTest[] docTestsToAdd = docTestListDiff.getAddedDocTests();
		
		for(int i = 0; i < docTestsToRemove.length; i++){
			TronGUIDocTest docTest = docTestsToRemove[i];
			updateTestNodeStatus(docNode, docTest, false, false);
		}
		
		if(testsChanged){
			//If the underlying tests changed, we have to refresh the doc tests on those
			//nodes too even if the doc tests have not changed.
			for(int i = 0; i < newDocTests.length; i++){
				TronGUIDocTest docTest = newDocTests[i];
				updateTestNodeStatus(docNode, docTest, true, docTest.isEnabled());
			}
		}
		else{
			//Otherwise just update the doc tests that changed.
			for(int i = 0; i < docTestsToUpdate.length; i++){
				TronGUIDocTest docTest = docTestsToUpdate[i];
				updateTestNodeStatus(docNode, docTest, true, docTest.isEnabled());
			}
			for(int i = 0; i < docTestsToAdd.length; i++){
				TronGUIDocTest docTest = docTestsToAdd[i];
				updateTestNodeStatus(docNode, docTest, true, docTest.isEnabled());
			}
		}
		
		docNode.setDocTests(newDocTests);

		//Clear any issues related to this document
		TronRemoveIssuesMessage trim = new TronRemoveIssuesMessage(docNode.getDocumentRef());
		requestProxy.send(trim);
	}

	public void refreshTreeModel(){
		synchronized(this){
			//Get the new list of nodes.
			ObjRef[] docRoots = xarch.getOpenXArches();
			java.util.List newNodeList = new ArrayList();
			for(int i = 0; i < docRoots.length; i++){
				try{
					String docURI = xarch.getXArchURI(docRoots[i]);
					newNodeList.add(new TronGUITreeDocumentNode(docRoots[i], docURI));
				}
				catch(Exception e){
				}
			}
			TronGUITreeDocumentNode[] newNodes = 
				(TronGUITreeDocumentNode[])newNodeList.toArray(new TronGUITreeDocumentNode[0]);
			
			//Get the old list of nodes.
			TronGUITreeDocumentNode[] oldNodes = this.getDocumentNodes();
			
			//Find adds (stuff in the new list that's not in the old list)
			for(int i = 0; i < newNodes.length; i++){
				boolean found = false;
				for(int j = 0; j < oldNodes.length; j++){
					if(newNodes[i].getDocumentRef().equals(oldNodes[j].getDocumentRef())){
						found = true;
						//Fix up the URI and add it to the tree resorted
						if(!newNodes[i].getDocumentURI().equals(oldNodes[j].getDocumentURI())){
							((MutableTreeNode)getRoot()).remove(oldNodes[j]);
							oldNodes[j].setDocumentURI(newNodes[i].getDocumentURI());
							WidgetUtils.addTreeNodeAlphabetically(((MutableTreeNode)getRoot()), oldNodes[j], 
								TronGUITreeDocumentNode.COMPARATOR);
						}
					}
				}
				if(!found){
					//It's an add
					WidgetUtils.addTreeNodeAlphabetically(((MutableTreeNode)getRoot()), newNodes[i],
						TronGUITreeDocumentNode.COMPARATOR);
					refreshDocumentNode((TronGUITreeDocumentNode)newNodes[i]);
					fireNodeShouldRefresh((MutableTreeNode)getRoot());
				}
			}
			//Find removes (stuff in the old list that's not in the new list)
			for(int i = 0; i < oldNodes.length; i++){
				boolean found = false;
				for(int j = 0; j < newNodes.length; j++){
					if(oldNodes[i].getDocumentRef().equals(newNodes[j].getDocumentRef())){
						found = true;
					}
				}
				if(!found){
					//It's a remove
					((MutableTreeNode)getRoot()).remove(oldNodes[i]);
					fireNodeShouldRefresh((MutableTreeNode)getRoot());
				}
			}
		}
		this.checkDocumentNodeAbbreviations();
		fireNodeShouldRefresh((MutableTreeNode)getRoot());
	}
	
	public TronGUITreeDocumentNode getDocumentNode(ObjRef xArchRef){
		TronGUITreeDocumentNode[] docNodes = getDocumentNodes();
		for(int i = 0; i < docNodes.length; i++){
			if((docNodes[i].getDocumentRef() != null) &&
				(docNodes[i].getDocumentRef().equals(xArchRef))){
				return docNodes[i];
			}
		}
		return null;
	}
	
	public TronGUITreeDocumentNode[] getDocumentNodes(){
		java.util.List oldNodeList = new ArrayList(((MutableTreeNode)getRoot()).getChildCount());
		for(int i = 0; i < ((MutableTreeNode)getRoot()).getChildCount(); i++){
			Object childNode = ((MutableTreeNode)getRoot()).getChildAt(i);
			if(childNode instanceof TronGUITreeDocumentNode){
				oldNodeList.add((TronGUITreeDocumentNode)childNode);
			}
		}
		TronGUITreeDocumentNode[] oldNodes =
			(TronGUITreeDocumentNode[])oldNodeList.toArray(new TronGUITreeDocumentNode[0]);
		return oldNodes;
	}
	
	public void checkDocumentNodeAbbreviations(){
		synchronized(this){
			int iterations = 0;
			TronGUITreeDocumentNode[] docNodes = this.getDocumentNodes();
			for(int i = 0; i < docNodes.length; i++){
				docNodes[i].resetUserObjectSegments();
			}
			while(true){
				int[] colliders = checkDocumentNodeAbbreviationsCollision();
				if(colliders == null){
					return;
				}
				else{
					TronGUITreeDocumentNode[] docNodes2 = this.getDocumentNodes();
					docNodes2[colliders[0]].incrementUserObjectSegments();
					docNodes2[colliders[1]].incrementUserObjectSegments();
					fireNodeShouldRefresh((MutableTreeNode)getRoot());
				}
				//Stop an infinite loop in case something really weird happens.
				if(iterations++ == 20){
					//This shouldn't happen.
					System.err.println("Warning: Bad Mojo: Infinite loop in Tron::checkDocumentNodeAbbreviations");
					return;
				}
			}
		}
	}
	
	//See if any of the document nodes have the same abbreviated value.
	//If they do, we will expand their abbreviations.
	private int[] checkDocumentNodeAbbreviationsCollision(){
		synchronized(this){
			TronGUITreeDocumentNode[] docNodes = this.getDocumentNodes();
			for(int i = 0; i < docNodes.length; i++){
				for(int j = i + 1; j < docNodes.length; j++){
					if(docNodes[i].getUserObject().toString().equals(docNodes[j].getUserObject().toString())){
						int[] colliders = new int[2];
						colliders[0] = i;
						colliders[1] = j;
						return colliders;
					}
				}
			}
			return null;
		}
	}
	
	public synchronized void handleFileEvent(XArchFileEvent evt){
		switch(evt.getEventType()){
		case XArchFileEvent.XARCH_CREATED_EVENT:
		case XArchFileEvent.XARCH_OPENED_EVENT:
		case XArchFileEvent.XARCH_RENAMED_EVENT:
		case XArchFileEvent.XARCH_CLOSED_EVENT:
			refreshTreeModel();
		}
	}
	
	public synchronized void handleFlatEvent(XArchFlatEvent evt){
		XArchPath srcPath = evt.getSourcePath();
		String srcPathString = srcPath.toTagsOnlyString();
		if(srcPathString.startsWith("xArch/archAnalysis")){
			ObjRef src = evt.getSource();
			ObjRef xArchRef = xarch.getXArch(src);
			TronGUITreeDocumentNode docNode = getDocumentNode(xArchRef);
			if(docNode != null){
				refreshDocumentNode(docNode);
				return;
			}
		}
		XArchPath targetPath = evt.getTargetPath();
		if(targetPath != null){
			String targetPathString = targetPath.toTagsOnlyString();
			if(srcPathString.equals("xArch")){
				if((targetPathString != null) && (targetPathString.equals("archAnalysis"))){
					ObjRef xArchRef = xarch.getXArch(evt.getSource());
					TronGUITreeDocumentNode docNode = getDocumentNode(xArchRef);
					if(docNode != null){
						refreshDocumentNode(docNode);
						return;
					}
				}
			}		
		}
	}

	public synchronized void handleAllTests(TronAllTestsMessage m){
		TronTest[] newTests = m.getAllTests();
		tronTestMap.clear();
		for(int i = 0; i < newTests.length; i++){
			tronTestMap.put(newTests[i].getUID(), newTests[i]);
		}

		//Refresh all document nodes to sync up with new tests
		TronGUITreeDocumentNode[] docNodes = getDocumentNodes();
		for(int i = 0; i < docNodes.length; i++){
			refreshDocumentNode(docNodes[i]);
		}
	}

	public synchronized void handleTestsChanged(TronTestsChangedMessage m){
		TronTestListDiff listDiff = m.getTestListDiff();
		TronTest[] testsToRemove = listDiff.getTestsToRemove();
		TronTest[] testsToAdd = listDiff.getTestsToAdd();
		for(int i = 0; i < testsToRemove.length; i++){
			tronTestMap.remove(testsToRemove[i].getUID());
		}
		for(int i = 0; i < testsToAdd.length; i++){
			tronTestMap.put(testsToAdd[i].getUID(), testsToAdd[i]);
		}

		//Refresh all document nodes to sync up with new tests
		TronGUITreeDocumentNode[] docNodes = getDocumentNodes();
		for(int i = 0; i < docNodes.length; i++){
			refreshDocumentNode(docNodes[i]);
		}
	}
	
	public synchronized TronTest[] getAllTests(){
		return (TronTest[])tronTestMap.values().toArray(new TronTest[0]);
	}

	protected Map tronGUITreeModelListeners = new WeakHashMap();
	
	public void addTronGUITreeModelListener(TronGUITreeModelListener l){
		tronGUITreeModelListeners.put(l, null);
	}
	
	public void removeTronGUITreeModelListener(TronGUITreeModelListener l){
		tronGUITreeModelListeners.remove(l);
	}
	
	protected void fireNodeShouldRefresh(TreeNode n){
		synchronized(tronGUITreeModelListeners){
			for(Iterator it = tronGUITreeModelListeners.keySet().iterator(); it.hasNext(); ){
				((TronGUITreeModelListener)it.next()).nodeShouldRefresh(n);
			}
		}
	}

}

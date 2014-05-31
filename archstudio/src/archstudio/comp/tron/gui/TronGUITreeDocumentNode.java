package archstudio.comp.tron.gui;

import javax.swing.tree.DefaultMutableTreeNode;

import archstudio.tron.TronTest;

import edu.uci.ics.widgets.CompositeIcon;
import edu.uci.ics.widgets.IIconable;
import edu.uci.ics.xadlutils.Resources;
import edu.uci.ics.xarchutils.ObjRef;

class TronGUITreeDocumentNode extends DefaultMutableTreeNode implements IIconable{
	protected ObjRef documentRef;
	protected String documentURI;
	private int userObjectSegments = 1;

	//tronTests indicates all the TronTests that are displayed under this node.
	protected TronTest[] tronTests = new TronTest[0];
	//docTests indicates the status of tests (applied/enabled) under this node.
	protected TronGUIDocTest[] docTests = new TronGUIDocTest[0];
	
	public TronGUITreeDocumentNode(ObjRef documentRef, String documentURI){
		super();
		this.documentRef = documentRef;
		this.documentURI = documentURI;
		setupUserObject();
	}
	
	protected static final javax.swing.Icon XML_FOLDER_ICON =
		new CompositeIcon(Resources.XML_OVERLAY_ICON, Resources.FOLDER_ICON);
	
	public javax.swing.Icon getIcon(){
		return XML_FOLDER_ICON;
	}
	
	public TronGUIDocTest[] getDocTests(){
		return docTests;
	}
	
	public void setDocTests(TronGUIDocTest[] docTests){
		this.docTests = docTests;
	}
	
	public TronTest[] getTronTests(){
		return tronTests;
	}

	public void setTronTests(TronTest[] tronTests){
		this.tronTests = tronTests;
	}
	
	public void resetUserObjectSegments(){
		userObjectSegments = 1;
		setupUserObject();
	}
	
	public void incrementUserObjectSegments(){
		userObjectSegments++;
		setupUserObject();
	}
	
	public void setupUserObject(){
		String tempUserObject = documentURI;
		int slashIndex = documentURI.length();
		for(int i = 0; i < userObjectSegments; i++){
			slashIndex = tempUserObject.lastIndexOf('/');
			if(slashIndex == -1){
				setUserObject(documentURI);
				return;
			}
			tempUserObject = tempUserObject.substring(0, slashIndex);
		}
		setUserObject("..." + documentURI.substring(slashIndex));
	}
	
	public ObjRef getDocumentRef(){
		return documentRef;
	}
	
	public String getDocumentURI(){
		return documentURI;
	}
	
	public void setDocumentURI(String documentURI){
		this.documentURI = documentURI;
		setupUserObject();
	}
	
	public boolean getAllowsChildren(){
		return true;
	}
	
	public static TronGUITreeDocumentNodeComparator COMPARATOR =
		new TronGUITreeDocumentNodeComparator();

	static class TronGUITreeDocumentNodeComparator implements java.util.Comparator{
		public int compare(Object o1, Object o2){
			TronGUITreeDocumentNode tn1 = (TronGUITreeDocumentNode)o1;
			TronGUITreeDocumentNode tn2 = (TronGUITreeDocumentNode)o2;
			return tn1.getDocumentURI().compareToIgnoreCase(tn2.getDocumentURI());
		}
	}
	
	
} /* DocumentNode */

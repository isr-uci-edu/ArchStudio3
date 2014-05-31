package archstudio.comp.archipelago;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import edu.uci.ics.widgets.IconableTreeNode;
import edu.uci.ics.xarchutils.ObjRef;

public class ArchipelagoTreeNode extends IconableTreeNode{

	protected ObjRef objRef;

	public ArchipelagoTreeNode(){
		super();
	}

	public ArchipelagoTreeNode(Object arg0){
		super(arg0);
	}

	public ArchipelagoTreeNode(Object arg0, boolean arg1){
		super(arg0, arg1);
	}
	
	public void setObjRef(ObjRef objRef){
		this.objRef = objRef;
	}
	
	public ObjRef getObjRef(){
		return objRef;
	}
	
}

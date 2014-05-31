
package archstudio.comp.archipelago;

import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Vector;

import archstudio.comp.preferences.IPreferences;
import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeNode;

import c2.fw.Message;
import c2.fw.MessageListener;
import c2.util.MessageSendProxy;

import edu.uci.ics.bna.BNAComponent;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchFileEvent;
import edu.uci.ics.xarchutils.XArchFlatEvent;

public abstract class AbstractArchipelagoTreePlugin implements ArchipelagoTreePlugin {

	protected MessageSendProxy topIfaceSender;
	protected MessageSendProxy bottomIfaceSender;
	
	protected ArchipelagoFrame frame;
	protected ArchipelagoTree tree;
	protected XArchFlatTransactionsInterface xarch;
	protected IPreferences preferences;
	
	public AbstractArchipelagoTreePlugin(MessageSendProxy topIfaceSender, 
	MessageSendProxy bottomIfaceSender, ArchipelagoFrame frame, ArchipelagoTree tree,
	XArchFlatTransactionsInterface xarch, IPreferences preferences){
		setTopIfaceSender(topIfaceSender);
		setBottomIfaceSender(bottomIfaceSender);
		setArchipelagoFrame(frame);
		setArchipelagoTree(tree);
		setXArch(xarch);
		setPreferences(preferences);
	}
	

	public MessageSendProxy getBottomIfaceSender(){
		return bottomIfaceSender;
	}

	public void setBottomIfaceSender(MessageSendProxy bottomIfaceSender){
		this.bottomIfaceSender = bottomIfaceSender;
	}
	
	public MessageSendProxy getTopIfaceSender(){
		return topIfaceSender;
	}
	
	public void setTopIfaceSender(MessageSendProxy topIfaceSender){
		this.topIfaceSender = topIfaceSender;
	}
	
	public void setArchipelagoTree(ArchipelagoTree tree){
		this.tree = tree;
	}

	public ArchipelagoTree getArchipelagoTree(){
		return tree;
	}

	public void setArchipelagoFrame(ArchipelagoFrame frame){
		this.frame = frame;
	}

	public ArchipelagoFrame getArchipelagoFrame(){
		return frame;
	}
	
	public void setXArch(XArchFlatTransactionsInterface xarch){
		this.xarch = xarch;
	}
	
	public XArchFlatTransactionsInterface getXArch(){
		return xarch;
	}
	
	public void setPreferences(IPreferences preferences){
		this.preferences = preferences;
	}
	
	public IPreferences getPreferences(){
		return preferences;
	}

	//DND stuff
	public boolean shouldAllowDrag(TreeNode node){
		return false;
	}
	
	public DragInfo getDragInfo(TreeNode node){
		return null;
	}
	
	public JMenuItem[] getPopupMenuItems(TreeNode node){
		return null;
	}
	
	private ArrayList messageListeners = new ArrayList();
	
	public void addMessageListener(MessageListener l){
		synchronized(messageListeners){
			messageListeners.add(l);
		}
	}
	
	public void removeMessageListener(MessageListener l){
		synchronized(messageListeners){
			messageListeners.remove(l);
		}
	}
	
	public void fireMessageSent(Message m){
		MessageListener[] mls; 
		synchronized(messageListeners){
			mls = (MessageListener[])messageListeners.toArray(new MessageListener[0]);
		}
		for(int i = 0; i < mls.length; i++){
			mls[i].messageSent(m);
		}
	}
	
	public static void registerMappingLogic(BNAComponent c, MappingLogic l){
		Vector v = (Vector)c.getProperty("mappingLogicVector");
		if(v == null){
			v = new Vector();
		}
		v.addElement(l);
		c.setProperty("mappingLogicVector", v);
	}
	
	public static MappingLogic[] getAllMappingLogics(BNAComponent c){
		Vector v = (Vector)c.getProperty("mappingLogicVector");
		if(v == null){
			return new MappingLogic[0];
		}
		return (MappingLogic[])v.toArray(new MappingLogic[0]);
	}
	
	public static void unregisterMappingLogic(BNAComponent c, MappingLogic l){
		Vector v = (Vector)c.getProperty("mappingLogicVector");
		if(v == null){
			v = new Vector();
		}
		v.removeElement(l);
		c.setProperty("mappingLogicVector", v);
	}
	
	public void handle(Message m){}
	
	public void documentOpened(ObjRef xArchRef, ObjRef elementRef){}
	public void documentClosed(){}
	
	//These methods get called whenever the xArchADT contents change
	public void handleXArchFlatEvent(XArchFlatEvent evt){}
	public void handleXArchFileEvent(XArchFileEvent evt){}

	//These methods get called whenever the tree contents change
	public void treeNodesChanged(TreeModelEvent evt){}
	public void treeNodesInserted(TreeModelEvent evt){}
	public void treeNodesRemoved(TreeModelEvent evt){}
	public void treeStructureChanged(TreeModelEvent arg0){}

	//This method gets called whenever the tree selection changes
	public void valueChanged(TreeSelectionEvent evt){}

}

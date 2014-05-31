package archstudio.comp.archipelago;

import java.awt.datatransfer.Transferable;

import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchFlatListener;
import edu.uci.ics.xarchutils.XArchFileListener;
import edu.uci.ics.xarchutils.XArchPath;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;

import edu.uci.ics.widgets.navpanel.*;

import c2.fw.Message;
import c2.fw.MessageProvider;

import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;

public interface ArchipelagoTreePlugin extends XArchFlatListener, XArchFileListener, TreeModelListener, TreeSelectionListener, MessageProvider{

	//Called by Archipelago when a new document is opened in the window
	public void documentOpened(ObjRef xArchRef, ObjRef elementRef);
	
	//Called by Archipelago when the current document is closed in the window.
	public void documentClosed();
	
	//Tree plugins get the opportunity to handle any messages
	//recv'd by the component
	public void handle(Message m);

	public void setArchipelagoTree(ArchipelagoTree tree);
	public ArchipelagoTree getArchipelagoTree();

	public void setArchipelagoFrame(ArchipelagoFrame frame);
	public ArchipelagoFrame getArchipelagoFrame();

	public void setXArch(XArchFlatTransactionsInterface xarch);
	public XArchFlatTransactionsInterface getXArch();
	
	public boolean shouldAllowDrag(TreeNode node);
	public DragInfo getDragInfo(TreeNode node);
	
	public void fireMessageSent(c2.fw.Message m);
	
	public JMenuItem[] getPopupMenuItems(TreeNode node);
	
	public boolean navigateTo(NavigationItem navigationItem);
	public boolean showRef(ObjRef ref, XArchPath path);
	public ArchipelagoHintsInfo[] getHintsInfo();
}

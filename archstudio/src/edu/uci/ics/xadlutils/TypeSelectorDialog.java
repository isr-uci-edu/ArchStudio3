package edu.uci.ics.xadlutils;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

import edu.uci.ics.widgets.*;
import edu.uci.ics.xarchutils.*;

public class TypeSelectorDialog extends JDialog implements ActionListener{

	public static final int COMPONENT_TYPES = 1;
	public static final int CONNECTOR_TYPES = 2;
	public static final int INTERFACE_TYPES = 4;

	protected Frame f;
	protected int typeMask;
	protected XArchFlatInterface xarch;
	protected ObjRef xArchRef;

	protected ObjRef result = null;
	
	protected JTree tree;
	protected DefaultTreeModel treeModel;

	protected JButton bOK;
	protected JButton bCancel;

	public TypeSelectorDialog(Frame f, XArchFlatInterface xarch, ObjRef xArchRef, int typeMask){
		super(f, "Select Type", true);
		this.f = f;
		this.xarch = xarch;
		this.xArchRef = xArchRef;
		this.typeMask = typeMask;
		init();
	}
	
	public void closeWindow(){
		this.setVisible(false);
		this.dispose();
	}
	
	public void refreshTreeModel(){
		IconableTreeNode rootNode = (IconableTreeNode)treeModel.getRoot();
		ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
		ObjRef archTypesRef = xarch.getElement(typesContextRef, "archTypes", xArchRef);
		if(archTypesRef != null){
			String description = "ArchTypes";
			IconableTreeNode typesNode = new IconableTreeNode(description);
			typesNode.setOverrideIsLeaf(false);
			typesNode.setIcon(Resources.TYPES_ICON);
			
			if((typeMask & COMPONENT_TYPES) > 0){
				IconableTreeNode componentTypeNode = new IconableTreeNode("Component Types");
				componentTypeNode.setOverrideIsLeaf(false);
				ObjRef[] componentTypeRefs = xarch.getAll(archTypesRef, "componentType");
				for(int j = 0; j < componentTypeRefs.length; j++){
					String brickTypeDescription = XadlUtils.getDescription(xarch, componentTypeRefs[j]);
					if(brickTypeDescription == null){
						brickTypeDescription = "(Unnamed Component Type)";
					}
					TypeTreeNode bttn = new TypeTreeNode(brickTypeDescription);
					bttn.setRef(componentTypeRefs[j]);
					bttn.setIcon(Resources.COMPONENT_TYPE_ICON);
					WidgetUtils.addTreeNodeAlphabetically(componentTypeNode, bttn);
				}
				WidgetUtils.addTreeNodeAlphabetically(typesNode, componentTypeNode);
			}
			if((typeMask & CONNECTOR_TYPES) > 0){
				IconableTreeNode connectorTypeNode = new IconableTreeNode("Connector Types");
				connectorTypeNode.setOverrideIsLeaf(false);
				ObjRef[] connectorTypeRefs = xarch.getAll(archTypesRef, "connectorType");
				for(int j = 0; j < connectorTypeRefs.length; j++){
					String brickTypeDescription = XadlUtils.getDescription(xarch, connectorTypeRefs[j]);
					if(brickTypeDescription == null){
						brickTypeDescription = "(Unnamed Connector Type)";
					}
					TypeTreeNode bttn = new TypeTreeNode(brickTypeDescription);
					bttn.setRef(connectorTypeRefs[j]);
					bttn.setIcon(Resources.CONNECTOR_TYPE_ICON);
					WidgetUtils.addTreeNodeAlphabetically(connectorTypeNode, bttn);
				}
				WidgetUtils.addTreeNodeAlphabetically(typesNode, connectorTypeNode);
			}
			if((typeMask & INTERFACE_TYPES) > 0){
				IconableTreeNode interfaceTypeNode = new IconableTreeNode("Interface Types");
				interfaceTypeNode.setOverrideIsLeaf(false);
				ObjRef[] typeRefs = xarch.getAll(archTypesRef, "interfaceType");
				for(int j = 0; j < typeRefs.length; j++){
					String typeDescription = XadlUtils.getDescription(xarch, typeRefs[j]);
					if(typeDescription == null){
						typeDescription = "(Unnamed Interface Type)";
					}
					TypeTreeNode bttn = new TypeTreeNode(typeDescription);
					bttn.setRef(typeRefs[j]);
					bttn.setIcon(Resources.INTERFACE_TYPE_ICON);
					WidgetUtils.addTreeNodeAlphabetically(interfaceTypeNode, bttn);
				}
				WidgetUtils.addTreeNodeAlphabetically(typesNode, interfaceTypeNode);
			}
			
			WidgetUtils.addTreeNodeAlphabetically(rootNode, typesNode);
		}
		treeModel.nodeStructureChanged(rootNode);
		tree.validate();
		tree.repaint();
	}
	
	public void init(){
		IconableTreeNode rootNode = new IconableTreeNode("Choose Type");
		treeModel = new DefaultTreeModel(rootNode);
		tree = new JTree(treeModel);
		IconableTreeCellRenderer cellRenderer = new IconableTreeCellRenderer();
		cellRenderer.setCustomIconsOnlyForLeafs(false);
		tree.setCellRenderer(cellRenderer);
		tree.setRowHeight(18);
		tree.addMouseListener(new DoubleClickMouseAdapter());
		refreshTreeModel();
		
		bOK = new JButton("OK");
		bOK.addActionListener(this);
		bCancel = new JButton("Cancel");
		bCancel.addActionListener(this);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		bottomPanel.add(bOK);
		bottomPanel.add(bCancel);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add("Center", new JScrollPane(tree));
		mainPanel.add("South", bottomPanel);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add("Center", mainPanel);
		
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(
			new WindowAdapter(){
				public void windowClosing(WindowEvent evt){
					closeWindow();
				}
			}
		);
		
		int xSize = 300;
		int ySize = 270;
		setSize((int)xSize, (int)ySize);
		//setLocation((int)xPos, (int)yPos);
	}
	
	public void doPopup(){
		WidgetUtils.centerInFrame(f, this);
		this.setVisible(true);
		invalidate();
		validate();
		paint(getGraphics());
	}
	
	class DoubleClickMouseAdapter extends MouseAdapter{
		public void mouseClicked(MouseEvent e){
			if(e.getButton() == MouseEvent.BUTTON1){
				if(e.getClickCount() == 2){
					int selRow = tree.getRowForLocation(e.getX(), e.getY());
					TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
					if(selRow != -1) {
						Object o = selPath.getLastPathComponent();
						if((o != null) && (o instanceof TypeTreeNode)){
							TypeTreeNode treeNode = (TypeTreeNode)o;
							bOK.doClick();
						}
					}
				}
			}
		}
	}
	
	public void actionPerformed(ActionEvent evt){
		if(evt.getSource() == bCancel){
			result = null;
			closeWindow();
		}
		else if(evt.getSource() == bOK){
			TreePath selectionPath = tree.getSelectionPath();
			if(selectionPath == null){
				JOptionPane.showMessageDialog(f, 
					"Must select a type.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			Object lastPathComponent = selectionPath.getLastPathComponent();
			if(!(lastPathComponent instanceof TypeTreeNode)){
				JOptionPane.showMessageDialog(f, 
					"Must select a type.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
					
			TypeTreeNode btn = (TypeTreeNode)lastPathComponent;
			//System.out.println(btn);
			result = btn.getRef();
			closeWindow();
		}
	}
	
	public ObjRef getSelectedTypeRef(){
		return result;
	}
	
	
	static class TypeTreeNode extends IconableTreeNode{
		ObjRef ref = null;
		
		public TypeTreeNode(String label){
			super(label);
		}
		
		public void setRef(ObjRef ref){
			this.ref = ref;
		}
		
		public ObjRef getRef(){
			return ref;
		}
	}
}

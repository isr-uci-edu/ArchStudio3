package edu.uci.ics.xadlutils;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.*;
import javax.swing.tree.*;

import edu.uci.ics.widgets.*;
import edu.uci.ics.xarchutils.*;

public class StructureSelectorDialog extends JDialog implements ActionListener{

	protected Frame f;
	protected XArchFlatInterface xarch;
	protected ObjRef xArchRef;
	protected int modeMask;
	
	protected StructureSelectorComponent brickSelectorComponent;

	public static final int SHOW_COMPONENTS = 1;
	public static final int SHOW_CONNECTORS = 2;
	public static final int SHOW_INTERFACES = 4;

	public static final int SELECTABLE_STRUCTURES = 8;
	public static final int SELECTABLE_COMPONENTS = 16;
	public static final int SELECTABLE_CONNECTORS = 32;
	public static final int SELECTABLE_INTERFACES = 64;

	public StructureSelectorDialog(Frame f, XArchFlatInterface xarch, ObjRef xArchRef, int modeMask){
		super(f, "Select Element", true);
		this.f = f;
		this.xarch = xarch;
		this.xArchRef = xArchRef;
		this.modeMask = modeMask;
		
		init();
	}

	public static class StructureSelectorComponent extends JPanel implements ActionListener{
		protected XArchFlatInterface xarch;
		protected ObjRef xArchRef;
		protected ObjRef[] archStructureRefs;
		protected int modeMask;
		
		protected JTree tree;
		protected DefaultTreeModel treeModel;

		protected JButton bOK;
		protected JButton bCancel;

		protected ObjRef result = null;
	
		public StructureSelectorComponent(XArchFlatInterface xarch, ObjRef xArchRef, ObjRef[] archStructureRefs, int modeMask){
			super();
			this.xarch = xarch;
			this.xArchRef = xArchRef;
			this.archStructureRefs = archStructureRefs;
			this.modeMask = modeMask;

			if((modeMask & (SELECTABLE_STRUCTURES | SELECTABLE_COMPONENTS | SELECTABLE_CONNECTORS | SELECTABLE_INTERFACES)) == 0){
				throw new IllegalArgumentException("Nothing to select in selector");
			}

			init();
		}
		
		private Vector actionListeners = new Vector();
		
		public void addActionListener(ActionListener l){
			actionListeners.addElement(l);
		}
		
		public void removeActionListener(ActionListener l){
			actionListeners.removeElement(l);
		}
		
		int actionEventId = 1000;
		protected void fireActionEvent(){
			ActionEvent evt = new ActionEvent(this, actionEventId++, "done");
			ActionListener[] al = (ActionListener[])actionListeners.toArray(new ActionListener[0]);
			for(int i = 0; i < al.length; i++){
				al[i].actionPerformed(evt);
			}
		}

		public void init(){
			IconableTreeNode rootNode = new IconableTreeNode("Select Element");
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
		
			this.setLayout(new BorderLayout());
			this.add("Center", mainPanel);
		}

		public void refreshTreeModel(){
			IconableTreeNode rootNode = (IconableTreeNode)treeModel.getRoot();
			ObjRef typesContextRef = xarch.createContext(xArchRef, "types");
			if(archStructureRefs == null){
				archStructureRefs = xarch.getAllElements(typesContextRef, "archStructure", xArchRef);
			}
			for(int i = 0; i < archStructureRefs.length; i++){
				String description = XadlUtils.getDescription(xarch, archStructureRefs[i]);
				if(description == null){
					description = "(Unnamed Structure)";
				}
				StructureTreeNode structureNode = new StructureTreeNode(description);
				structureNode.setRef(archStructureRefs[i]);
				structureNode.setNodeType(StructureTreeNode.STRUCTURE);
				structureNode.setOverrideIsLeaf(false);
				structureNode.setIcon(Resources.STRUCTURE_ICON);
			
				if((modeMask & SHOW_COMPONENTS) != 0){
					IconableTreeNode componentNode = new IconableTreeNode("Components");
					componentNode.setOverrideIsLeaf(false);
					ObjRef[] componentRefs = xarch.getAll(archStructureRefs[i], "component");
					for(int j = 0; j < componentRefs.length; j++){
						String brickDescription = XadlUtils.getDescription(xarch, componentRefs[j]);
						if(brickDescription == null){
							brickDescription = "(Unnamed Component)";
						}
						StructureTreeNode btn = new StructureTreeNode(brickDescription);
						btn.setNodeType(StructureTreeNode.COMPONENT);
						btn.setRef(componentRefs[j]);
						btn.setIcon(Resources.COMPONENT_ICON);
					
						if((modeMask & SHOW_INTERFACES) != 0){
							ObjRef[] interfaceRefs = xarch.getAll(componentRefs[j], "interface");
							for(int k = 0; k < interfaceRefs.length; k++){
								String interfaceDescription = XadlUtils.getDescription(xarch, interfaceRefs[k]);
								if(interfaceDescription == null){
									interfaceDescription = "(Unnamed Interface)";
								}
								StructureTreeNode itn = new StructureTreeNode(interfaceDescription);
								itn.setNodeType(StructureTreeNode.INTERFACE);
								itn.setRef(interfaceRefs[k]);
								itn.setIcon(Resources.INTERFACE_ICON);
								WidgetUtils.addTreeNodeAlphabetically(btn, itn);
							}
						}
					
						WidgetUtils.addTreeNodeAlphabetically(componentNode, btn);
					}
					WidgetUtils.addTreeNodeAlphabetically(structureNode, componentNode);
				}
			
				if((modeMask & SHOW_CONNECTORS) != 0){
					IconableTreeNode connectorNode = new IconableTreeNode("Connectors");
					connectorNode.setOverrideIsLeaf(false);
					ObjRef[] connectorRefs = xarch.getAll(archStructureRefs[i], "connector");
					for(int j = 0; j < connectorRefs.length; j++){
						String brickDescription = XadlUtils.getDescription(xarch, connectorRefs[j]);
						if(brickDescription == null){
							brickDescription = "(Unnamed Connector)";
						}
						StructureTreeNode btn = new StructureTreeNode(brickDescription);
						btn.setNodeType(StructureTreeNode.CONNECTOR);
						btn.setRef(connectorRefs[j]);
						btn.setIcon(Resources.CONNECTOR_ICON);

						if((modeMask & SHOW_INTERFACES) != 0){
							ObjRef[] interfaceRefs = xarch.getAll(connectorRefs[j], "interface");
							for(int k = 0; k < interfaceRefs.length; k++){
								String interfaceDescription = XadlUtils.getDescription(xarch, interfaceRefs[k]);
								if(interfaceDescription == null){
									interfaceDescription = "(Unnamed Interface)";
								}
								StructureTreeNode itn = new StructureTreeNode(interfaceDescription);
								itn.setNodeType(StructureTreeNode.INTERFACE);
								itn.setRef(interfaceRefs[k]);
								itn.setIcon(Resources.INTERFACE_ICON);
								WidgetUtils.addTreeNodeAlphabetically(btn, itn);
							}
						}

						WidgetUtils.addTreeNodeAlphabetically(connectorNode, btn);
					}
					WidgetUtils.addTreeNodeAlphabetically(structureNode, connectorNode);
				}

				WidgetUtils.addTreeNodeAlphabetically(rootNode, structureNode);
			}
			treeModel.nodeStructureChanged(rootNode);
			tree.validate();
			tree.repaint();
		}

		public JButton getOKButton(){
			return bOK;
		}
		
		public JButton getCancelButton(){
			return bCancel;
		}

		class DoubleClickMouseAdapter extends MouseAdapter{
			public void mouseClicked(MouseEvent e){
				if(e.getButton() == MouseEvent.BUTTON1){
					if(e.getClickCount() == 2){
						int selRow = tree.getRowForLocation(e.getX(), e.getY());
						TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
						if(selRow != -1) {
							Object o = selPath.getLastPathComponent();
							if((o != null) && (o instanceof StructureTreeNode)){
								StructureTreeNode simTreeNode = (StructureTreeNode)o;
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
				fireActionEvent();
			}
			else if(evt.getSource() == bOK){
				TreePath selectionPath = tree.getSelectionPath();
				if(selectionPath == null){
					JOptionPane.showMessageDialog(this, 
						"Must select an element.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				Object lastPathComponent = selectionPath.getLastPathComponent();
			
				ArrayList allowableSelections = new ArrayList();
				if((modeMask & SELECTABLE_STRUCTURES) != 0){
					allowableSelections.add("a structure");
				}
				if((modeMask & SELECTABLE_COMPONENTS) != 0){
					allowableSelections.add("a component");
				}
				if((modeMask & SELECTABLE_CONNECTORS) != 0){
					allowableSelections.add("a connector");
				}
				if((modeMask & SELECTABLE_INTERFACES) != 0){
					allowableSelections.add("an interface");
				}
			
				String errorMsg = "Must select ";
				boolean firstThing = true;
				for(Iterator it = allowableSelections.iterator(); it.hasNext(); ){
					String s = (String)it.next();
					if(!firstThing){
						errorMsg += "or ";
					}
					errorMsg += s;
				}
				errorMsg += ".";
			
				if(!(lastPathComponent instanceof StructureTreeNode)){
					JOptionPane.showMessageDialog(this, 
						errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				boolean error = true;
				StructureTreeNode selectedNode = (StructureTreeNode)lastPathComponent;
				int nodeType = selectedNode.getNodeType();
				if(nodeType == StructureTreeNode.STRUCTURE){
					if((modeMask & SELECTABLE_STRUCTURES) != 0){
						error = false;
					}
				}
				else if(nodeType == StructureTreeNode.COMPONENT){
					if((modeMask & SELECTABLE_COMPONENTS) != 0){
						error = false;
					}
				}
				else if(nodeType == StructureTreeNode.CONNECTOR){
					if((modeMask & SELECTABLE_CONNECTORS) != 0){
						error = false;
					}
				}
				else if(nodeType == StructureTreeNode.INTERFACE){
					if((modeMask & SELECTABLE_INTERFACES) != 0){
						error = false;
					}
				}
				if(error){
					JOptionPane.showMessageDialog(this, 
						errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				result = selectedNode.getRef();
				fireActionEvent();
			}
		}
	
		public ObjRef getResult(){
			return result;
		}
		
	}
	
	public void closeWindow(){
		this.setVisible(false);
		this.dispose();
	}
	
	
	public void init(){
		brickSelectorComponent = new StructureSelectorComponent(xarch, xArchRef, null, modeMask);
		brickSelectorComponent.addActionListener(this);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add("Center", brickSelectorComponent);
		
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
		
	public void actionPerformed(ActionEvent evt){
		if(evt.getSource() == brickSelectorComponent){
			closeWindow();
		}
	}
	
	public ObjRef getResult(){
		return brickSelectorComponent.getResult();
	}
	
	
	static class StructureTreeNode extends IconableTreeNode{
		public static final int SUPPORT = 50;
		public static final int STRUCTURE = 75;
		public static final int COMPONENT = 100;
		public static final int CONNECTOR = 200;
		public static final int INTERFACE = 300;
		
		private int nodeType;
		private ObjRef ref = null;

		public StructureTreeNode(String label){
			super(label);
		}
		
		public void setRef(ObjRef ref){
			this.ref = ref;
		}
		
		public ObjRef getRef(){
			return ref;
		}
		
		public void setNodeType(int nodeType){
			this.nodeType = nodeType;
		}
		
		public int getNodeType(){
			return nodeType;
		}
	}
}

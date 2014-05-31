package archstudio.comp.pladiffdriver;

import javax.swing.event.*; 
import javax.swing.*; 
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import edu.uci.ics.xarchutils.*;


/**
 * @author Matt Critchlow <A HREF="mailto:critchlm@uci.com">(critchlm@uci.edu)</A>
 * A TypeVersionSelector altered for the ArchDiff. It is a JSplitPane which 
 * encompasses 2 JScrollPanes, each with a JList. This is displayed in the selectorDriver
 * and allows the user to select a type/version to begin selection on.
 */
public class TypeSelector extends JSplitPane 
{
   /**
	 * The parts that make up a selector.
	 */
	private JList            typeList;
	private DefaultListModel typeModel;
	private JList            versionList;
	private DefaultListModel versionModel;
	private HashMap          visited;
	private XArchFlatInterface xarch;
	private String url;
	private boolean hasTypes;
	
	/**
	 * @param xArchURL		The url which corresponds to the current selection in the driver.
	 * @param type			The type to select on, i.e ComponentType.
	 * @param arch			The arch object which corresponds to the current selection in the driver.
	 */
	public TypeSelector(String xArchURL, String type, XArchFlatInterface arch)
	{
		JScrollPane scrollPane;
		JPanel      left;
		JPanel      right;
		JLabel      label;
		
		xarch = arch;
		url = xArchURL;
		hasTypes = true;
		setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		setDividerSize(5);
		typeModel = new DefaultListModel();
		typeList = new JList(typeModel);
		typeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane = new JScrollPane(typeList);
		scrollPane.setPreferredSize(new Dimension(225, 150));
		scrollPane.setMinimumSize(new Dimension(225, 150));
		scrollPane.setHorizontalScrollBarPolicy(
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		left = new JPanel();
		left.setLayout(new BorderLayout());
		label = new JLabel(type);
		left.add(BorderLayout.NORTH, label);
		left.add(BorderLayout.CENTER, scrollPane);
		setLeftComponent(left);
		
		versionModel = new DefaultListModel();
		versionList = new JList(versionModel);
		versionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane = new JScrollPane(versionList);
		scrollPane.setPreferredSize(new Dimension(225,150));
		scrollPane.setMinimumSize(new Dimension(100,150));
		scrollPane.setHorizontalScrollBarPolicy(
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		right = new JPanel();
		right.setLayout(new BorderLayout());
		label = new JLabel("Revision");
		right.add(BorderLayout.NORTH, label);
		right.add(BorderLayout.CENTER, scrollPane);
		setRightComponent(right);
		
		ObjRef document = xarch.getOpenXArch(url);
		ObjRef typesContext = xarch.createContext(document, "types");
		ObjRef archTypes = xarch.getElement(typesContext, "ArchTypes", document);
		ObjRef[] types = null;
		if(archTypes != null)
			types = xarch.getAll(archTypes, type);
		else
		{
			typeModel.addElement("No Type Information Available");
			hasTypes = false;
		}
		visited = new HashMap();
		
		if(hasTypes == true)
		{
			for (int i = 0; i != types.length; ++i)
			{
				ObjRef description = (ObjRef) xarch.get(types[i], "Description");
				String name = (String) xarch.get(description, "Value");
				String id = (String) xarch.get(types[i], "Id");
				
				if (!visited.containsKey(name))
				{
					typeModel.addElement(name);
					visited.put(name, id);
				}
			}
			typeList.addMouseListener(new MouseAdapter()
				{
					public void mousePressed(MouseEvent e)
					{
						versionModel.clear();
						versionList.getSelectionModel().clearSelection();
						ObjRef openUrl = xarch.getOpenXArch(url);
						String s = (String) typeList.getSelectedValue();
						if (s != null)
						{
							String id = (String) visited.get(s);
							ObjRef object = xarch.getByID(openUrl, id);
							ObjRef document = xarch.getXArch(object);
							
							if(xarch.isInstanceOf(object, "edu.uci.isr.xarch.versions.VariantComponentTypeImplVersImpl" ))
							{
								ObjRef link = (ObjRef) xarch.get(object, "VersionGraphNode");
								
								if (link != null)
								{
									String href = (String) xarch.get(link, "Href");
									if(href != null)
									{
										ObjRef node = xarch.resolveHref(document, href);
										if(node != null)
										{
											ObjRef versionGraph = xarch.getParent(node);
											if(versionGraph != null)
											{
												ObjRef[] nodesInGraph = xarch.getAll(versionGraph, "Node");
												if(nodesInGraph.length != 0)
												{
													for (int i = 0; i != nodesInGraph.length; ++i)
													{
														String nodeId = (String) xarch.get(nodesInGraph[i], "Id");
														ObjRef versionId = (ObjRef) xarch.get(nodesInGraph[i], "VersionID");
														String versionNumber = (String) xarch.get(versionId, "Value");
														if(hasValidTypeID(s, versionNumber, nodeId))
														{
															versionModel.addElement(versionNumber);
															if (!visited.containsKey(s + "-" + versionNumber))
															{	
																visited.put(s + "-" + versionNumber, nodeId);
															}
														}
													}
												}
											}
										}
									}
								}
							}
						} 
					}
				});
		}
	}
	/**
	 * This method locates the ID of the selected type and version.
	 * @return	The starting point typeID for the currently selected structure.
	 */
	public String getTypeId()
	{
		String selectedType = (String) typeList.getSelectedValue();
		String selectedVersion = (String) versionList.getSelectedValue();
		String nodeId = (String) visited.get(selectedType + "-" + selectedVersion);
		
		ObjRef node = xarch.getByID(nodeId);
		ObjRef document = xarch.getXArch(node);
		ObjRef[] referers = xarch.getReferences(document, nodeId);
		
		for (int i = 0; i != referers.length; ++i)
		{
			ObjRef parent = xarch.getParent(referers[i]);
			String typeName = xarch.getType(parent);
			if (typeName.indexOf("Type") != -1)
			{
				String typeId = (String) xarch.get(parent, "Id");
				return typeId;
			}
		}
		return null;
	}
	/**
	 * This method calculates whether or not a version is currently associated with a type. If a version
	 * is no longer associated with the type, it is not included in the graphical representation.
	 * @param type		The type based on the user's selection.
	 * @param version	The version of the type based on the user's selection.
	 * @param nodeID	The id that is associated with the current version graph. 
	 * @return	A boolean value which is true is a valid typeID exists, and false otherwise.
	 */
	public boolean hasValidTypeID(String type, String version, String nodeID)
	{
		String selectedType = type;
		String selectedVersion = version;
		String nodeId = nodeID;
		
		ObjRef openUrl = xarch.getOpenXArch(url);
		ObjRef node = xarch.getByID(openUrl, nodeId);
		ObjRef document = xarch.getXArch(node);
		ObjRef[] referers = xarch.getReferences(document, nodeId);
		boolean hasValidID = false;
		for (int i = 0; i != referers.length; ++i)
		{
			ObjRef parent = xarch.getParent(referers[i]);
			String typeName = xarch.getType(parent);
			if (typeName.indexOf("Node") == -1)
			{
				if(xarch.isAttached(parent))
				{
					hasValidID = true;
				}
			}
		}
		return hasValidID;
	}
	/**
	 * @return	A string concatenation of the type and verion currently selection in the driver.
	 */
	public String getTypeVersionId()
	{
		String selectedType = (String) typeList.getSelectedValue();
		String selectedVersion = (String) versionList.getSelectedValue();
		String nodeId = (String) visited.get(selectedType + "-" + selectedVersion);
		return nodeId;
	}
	/**
	 * This method is used for error checking to ensure that the user has selected not only a type but a corresponding version as well.
	 * @return	True if there is a version selected, and false otherwise.
	 */
	public boolean isVersionSelected()
	{
		return(versionList.getSelectedValue() != null);
	}
	
}
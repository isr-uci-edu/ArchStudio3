package archstudio.comp.selectordriver;

import javax.swing.event.*; 
import javax.swing.*; 
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import edu.uci.ics.xarchutils.*;

/**
 * @author Matt Critchlow <A HREF="mailto:critchlm@uci.com">(critchlm@uci.edu)</A>
 * StructureSelector is a JScrollPane wich encompasses a JList which is diplayed in the
 * Selector Driver which allows the user to select a structure to begin selection on.
 */
public class StructureSelector extends JScrollPane
{
	protected JList	structureList;
	private DefaultListModel structureModel;
	private HashMap          visited;
	private XArchFlatInterface xarch;
	private String url;
	
	/**
	 * @param	xArchURL	The url which corresponds to the current selection in the driver.
	 * @param	arch		The arch object which corresponds to the current selection in the driver.
	 */
	public StructureSelector(String xArchURL, XArchFlatInterface arch)
	{
		JScrollPane pane;
		xarch = arch;
		url = xArchURL;
		structureModel = new DefaultListModel();
		structureList = new JList(structureModel);
		setViewportView(structureList);
		setHorizontalScrollBarPolicy(
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		setVerticalScrollBarPolicy(
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		ObjRef document = xarch.getOpenXArch(url);
		ObjRef typesContext = xarch.createContext(document, "types");
		ObjRef[] structures = xarch.getAllElements( typesContext, "archStructure", document );
		visited = new HashMap();
		
		if(structures.length == 0)
			structureModel.addElement("No Structure Information Available");
		else
		{
			for (int i = 0; i != structures.length; i++)
			{
				ObjRef description = (ObjRef) xarch.get(structures[i], "Description");
				String id = (String) xarch.get(structures[i], "Id");
				if( description != null)
				{
					String name = (String) xarch.get(description, "Value");
					structureModel.addElement(name);
					visited.put(name, id);
				}
				else
				{
					visited.put("unknown",id);
					structureModel.addElement(id);
				}
			}
		}
	}
	/**
	* @return	The starting point id for the currently selected structure.
	*/
	public String getStructureID()
	{
		String selectedStructure = (String) structureList.getSelectedValue();
		String struct = (String)visited.get(selectedStructure);
		return struct;
	}
}
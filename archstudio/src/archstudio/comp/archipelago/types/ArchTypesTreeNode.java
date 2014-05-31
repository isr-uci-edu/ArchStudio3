package archstudio.comp.archipelago.types;

import javax.swing.Icon;

import edu.uci.ics.bna.BNAModel;

import archstudio.comp.archipelago.ArchipelagoTreeNode;
import archstudio.comp.archipelago.ThingIDMap;

public class ArchTypesTreeNode extends ArchipelagoTreeNode {
	
	public static final int UNKNOWN = 0;
	public static final int COMPONENT_TYPE = 100;
	public static final int CONNECTOR_TYPE = 200;
	public static final int INTERFACE_TYPE = 300;
	
	protected int nodeType = UNKNOWN;
	
	protected static Icon componentTypeLeafIcon;
	protected static Icon connectorTypeLeafIcon;
	protected static Icon interfaceTypeLeafIcon;
	
	static{
		componentTypeLeafIcon = edu.uci.ics.widgets.WidgetUtils.getImageIcon("archstudio/comp/archipelago/res/component-type.gif");
		connectorTypeLeafIcon = edu.uci.ics.widgets.WidgetUtils.getImageIcon("archstudio/comp/archipelago/res/connector-type.gif");
		interfaceTypeLeafIcon = edu.uci.ics.widgets.WidgetUtils.getImageIcon("archstudio/comp/archipelago/res/interface-type.gif");
	}
	
	protected BNAModel bnaModel;
	protected ThingIDMap thingIDMap;
		
	public ArchTypesTreeNode() {
		super();
	}

	public ArchTypesTreeNode(Object arg0) {
		super(arg0);
	}

	public ArchTypesTreeNode(Object arg0, boolean arg1) {
		super(arg0, arg1);
	}

	public void setNodeType(int nodeType){
		this.nodeType = nodeType;
		if(nodeType == COMPONENT_TYPE){
			setIcon(componentTypeLeafIcon);
		}
		else if(nodeType == CONNECTOR_TYPE){
			setIcon(connectorTypeLeafIcon);
		}
		else if(nodeType == INTERFACE_TYPE){
			setIcon(interfaceTypeLeafIcon);
		}
		else{
			setIcon(null);
		}
	}
	
	public int getNodeType(){
		return nodeType;
	}

	
	public void setBNAModel(BNAModel m){
		this.bnaModel = m;
	}
	
	public BNAModel getBNAModel(){
		return bnaModel;
	}

	public void setThingIDMap(ThingIDMap thingIDMap){
		this.thingIDMap = thingIDMap;
	}
	
	public ThingIDMap getThingIDMap(){
		return thingIDMap;
	}

}

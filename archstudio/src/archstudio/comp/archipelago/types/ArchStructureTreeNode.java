package archstudio.comp.archipelago.types;

import javax.swing.Icon;

import edu.uci.ics.bna.BNAModel;

import archstudio.comp.archipelago.ArchipelagoTreeNode;
import archstudio.comp.archipelago.ThingIDMap;

public class ArchStructureTreeNode extends ArchipelagoTreeNode {

	protected BNAModel bnaModel;
	protected ThingIDMap thingIDMap;
	
	public ArchStructureTreeNode() {
		super();
		init();
	}

	public ArchStructureTreeNode(Object arg0) {
		super(arg0);
		init();
	}

	public ArchStructureTreeNode(Object arg0, boolean arg1) {
		super(arg0, arg1);
		init();
	}

	protected void init(){
		Icon leafIcon = edu.uci.ics.widgets.WidgetUtils.getImageIcon("archstudio/comp/archipelago/res/edit.gif");
		setIcon(leafIcon);			
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

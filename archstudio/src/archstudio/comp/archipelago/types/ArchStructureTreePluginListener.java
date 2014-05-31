package archstudio.comp.archipelago.types;

import edu.uci.ics.bna.BNAComponent;

public interface ArchStructureTreePluginListener {

	public void structureBNAComponentCreated(ArchStructureTreeNode node, 
		BNAComponent structureBNAComponent);
		
	public void structureBNAComponentDestroying(BNAComponent structureBNAComponent);
	public void structureBNAComponentDestroyed(BNAComponent structureBNAComponent);
}

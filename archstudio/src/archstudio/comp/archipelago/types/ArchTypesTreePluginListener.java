package archstudio.comp.archipelago.types;

import edu.uci.ics.bna.BNAComponent;

public interface ArchTypesTreePluginListener {

	public void typeBNAComponentCreated(ArchTypesTreeNode node, 
		BNAComponent typeBNAComponent);
		
	public void typeBNAComponentDestroying(BNAComponent typeBNAComponent);
	public void typeBNAComponentDestroyed(BNAComponent typeBNAComponent);
}

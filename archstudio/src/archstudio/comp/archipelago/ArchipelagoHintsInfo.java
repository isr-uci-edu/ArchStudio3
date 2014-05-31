package archstudio.comp.archipelago;

import edu.uci.ics.bna.BNAModel;
import edu.uci.ics.xarchutils.ObjRef;

public class ArchipelagoHintsInfo {

	protected ObjRef ref;
	protected BNAModel bnaModel;

	public ArchipelagoHintsInfo(ObjRef ref, BNAModel bnaModel){
		this.ref = ref;
		this.bnaModel = bnaModel;
	}
	
	public ObjRef getRef(){
		return ref;
	}
	
	public BNAModel getBNAModel(){
		return bnaModel;
	}

}

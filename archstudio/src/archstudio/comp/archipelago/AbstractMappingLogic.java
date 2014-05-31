package archstudio.comp.archipelago;

import edu.uci.ics.bna.*;
import edu.uci.ics.xarchutils.*;
import archstudio.comp.xarchtrans.*;

public abstract class AbstractMappingLogic implements MappingLogic{
	
	protected BNAModel[] bnaModels;
	protected XArchFlatTransactionsInterface xarch;
	
	public AbstractMappingLogic(BNAModel[] bnaModels, XArchFlatTransactionsInterface xarch){
		this.bnaModels = bnaModels;
		this.xarch = xarch;
	}

	public void handleXArchFlatEvent(XArchFlatEvent evt){
	}
	
	public void handleXArchFileEvent(XArchFileEvent evt){
	}
	
	public void bnaModelChanged(BNAModelEvent evt){
	}
	
	

}

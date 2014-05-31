package archstudio.comp.archipelago.types;

import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchPath;

import java.awt.datatransfer.*;

class ObjRefTransferable implements Transferable, java.io.Serializable{
	protected ObjRef objRef;
	protected XArchPath refPath;
	
	public static final DataFlavor OBJREF_DATA_FLAVOR = 
		new DataFlavor(ObjRefTransferable.class, "OBJREF_DATA_FLAVOR");
	
	public ObjRefTransferable(ObjRef objRef, XArchPath refPath){
		this.objRef = objRef;
		this.refPath = refPath;
	}
	
	public ObjRef getObjRef(){
		return objRef;
	}
	
	public XArchPath getXArchPath(){
		return refPath;
	}
	
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, java.io.IOException{
		if(flavor.equals(OBJREF_DATA_FLAVOR)){
			return this;
		}
		else if(flavor.equals(DataFlavor.stringFlavor)){
			return this.toString();
		}
		else{
			throw new UnsupportedFlavorException(flavor);
		}
	}
	
	public DataFlavor[] getTransferDataFlavors(){
		return new DataFlavor[]{
			DataFlavor.stringFlavor,
			OBJREF_DATA_FLAVOR
		};
	}
		
	public boolean isDataFlavorSupported(DataFlavor flavor){
		DataFlavor[] flavs = getTransferDataFlavors();
		for(int i = 0; i < flavs.length; i++){
			if((flavor != null) && (flavor.equals(flavs[i]))){
				return true;
			}
		}
		return false;
	}
	
}

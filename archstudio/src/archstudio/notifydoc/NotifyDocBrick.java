package archstudio.notifydoc;

import edu.uci.ics.xarchutils.ObjRef;

public interface NotifyDocBrick extends c2.fw.Brick{
	
	public void docSaving(ObjRef documentRef);
	public void docClosing(ObjRef documentRef);


}

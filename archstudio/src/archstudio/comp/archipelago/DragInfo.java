package archstudio.comp.archipelago;

import java.awt.Cursor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSourceListener;

public class DragInfo{
	
	protected Transferable transferable;
	protected Cursor cursor;
	protected DragSourceListener dragSourceListener;
	
	public DragInfo(){
	}

	public DragInfo(Transferable t, Cursor c, DragSourceListener dsl){
		this.transferable = t;
		this.cursor = c;
		this.dragSourceListener = dsl;
	}

	public Cursor getCursor() {
		return cursor;
	}

	public Transferable getTransferable(){
		return transferable;
	}

	public void setCursor(Cursor cursor){
		this.cursor = cursor;
	}

	public void setTransferable(Transferable transferable){
		this.transferable = transferable;
	}

	public DragSourceListener getDragSourceListener(){
		return dragSourceListener;
	}

	public void setDragSourceListener(DragSourceListener listener){
		dragSourceListener = listener;
	}

}

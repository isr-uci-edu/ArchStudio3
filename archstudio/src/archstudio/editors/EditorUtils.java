package archstudio.editors;

import c2.fw.*;

public class EditorUtils{

	protected EditorUtils(){}
	
	public static boolean appliesToEditor(String editorID, FocusEditorMessage fem){
		String[] editorIDs = fem.getEditorIDs();
		if(editorIDs == null){
			return true;
		}
		for(int i = 0; i < editorIDs.length; i++){
			if(editorIDs[i].equals(editorID)){
				return true;
			}
		}
		return false;
	}
	
	public static void registerEditor(DelegateBrick b, Interface iface, String editorID){
		EditorLifecycleProcessor lp = new EditorLifecycleProcessor(b, iface, editorID);
		b.addLifecycleProcessor(lp);
	}
	
	private static class EditorLifecycleProcessor extends LifecycleAdapter{
		private DelegateBrick b;
		private Interface iface;
		private String editorID;
		
		public EditorLifecycleProcessor(DelegateBrick b, Interface iface, String editorID){
			this.b = b;
			this.iface = iface;
			this.editorID = editorID;
		}
		
		public void begin(){
			ChangeEditorStatusMessage m = new ChangeEditorStatusMessage(editorID, 
				ChangeEditorStatusMessage.STATUS_ACTIVE);
			b.sendToAll(m, iface);
		}
		
		public void end(){
			ChangeEditorStatusMessage m = new ChangeEditorStatusMessage(editorID, 
				ChangeEditorStatusMessage.STATUS_INACTIVE);
			b.sendToAll(m, iface);
		}
	}
}

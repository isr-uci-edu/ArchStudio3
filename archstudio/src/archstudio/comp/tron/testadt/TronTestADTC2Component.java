package archstudio.comp.tron.testadt;

import archstudio.tron.*;

import java.util.*;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchEventProvider;
import edu.uci.ics.xarchutils.XArchFileEvent;
import edu.uci.ics.xarchutils.XArchFileListener;

public class TronTestADTC2Component extends AbstractC2DelegateBrick{

	protected TronTestStore testStore;
	
	public TronTestADTC2Component(Identifier id){
		super(id);
		testStore = new TronTestStore();
		
		addLifecycleProcessor(new TronTestADTLifecycleProcessor());
		addMessageProcessor(new TronTestADTMessageProcessor());
	}
	
	class TronTestADTLifecycleProcessor extends LifecycleAdapter{
		public void begin(){
			TronIssueADTStatusMessage sm = 
				new TronIssueADTStatusMessage(TronIssueADTStatusMessage.STATUS_ACTIVE);
			sendToAll(sm, bottomIface);
			
			TronTest[] allTests = testStore.getAllTests();
			TronAllTestsMessage taim = new TronAllTestsMessage(allTests);
			sendToAll(taim, bottomIface);
		}
		
		public void end(){
			TronIssueADTStatusMessage sm = 
				new TronIssueADTStatusMessage(TronIssueADTStatusMessage.STATUS_INACTIVE);
			sendToAll(sm, bottomIface);
		}
	}

	class TronTestADTMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof TronGetAllTestsMessage){
				handleGetAllTests((TronGetAllTestsMessage)m);
			}
			else if(m instanceof TronReplaceTestsMessage){
				handleReplaceTests((TronReplaceTestsMessage)m);
			}
		}
	}
	
	public void handleGetAllTests(TronGetAllTestsMessage m){
		TronTest[] allTests = testStore.getAllTests();
		TronAllTestsMessage taim = new TronAllTestsMessage(allTests);
		sendToAll(taim, bottomIface);
	}
	
	public void handleReplaceTests(TronReplaceTestsMessage m){
		synchronized(testStore){
			String toolID = m.getToolID();
			TronTest[] oldTests = testStore.getAllTests(toolID);
			TronTest[] newTests = m.getNewTests();
			
			TronTestListDiff testListDiff = TronTestListDiff.diffLists(oldTests, newTests);
			testStore.removeTests(testListDiff.getTestsToRemove());
			testStore.addTests(testListDiff.getTestsToAdd());
			TronTestsChangedMessage ttcm = new TronTestsChangedMessage(testListDiff);
			sendToAll(ttcm, bottomIface);
 		}
	}

}

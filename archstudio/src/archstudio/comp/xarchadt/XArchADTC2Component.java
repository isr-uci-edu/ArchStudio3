package archstudio.comp.xarchadt;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

import edu.uci.ics.xarchutils.*;

public class XArchADTC2Component extends AbstractC2DelegateBrick{
	private XArchFlatInterface xArchInterface = null;
	
	public XArchADTC2Component(Identifier id){
		super(id);
		//super(id, new XArchADTComponent(new SimpleIdentifier("XArchADTComponent")));
		xArchInterface = new XArchFlatImpl();
		EBIWrapperUtils.deployService(this, bottomIface, bottomIface, xArchInterface, 
			new Class[]{XArchFlatInterface.class}, new Class[]{ XArchFlatListener.class, XArchFileListener.class });		
		//addMessageProcessor(new DebugMessageProcessor());
	}

	class DebugMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			System.out.println(m);
			System.out.println();
		}
	}

}


package archstudio.comp.ms;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

public class ManagedSystemC2Component extends AbstractC2DelegateBrick{
	
	public ManagedSystemC2Component(Identifier id){
		super(id);
		this.addLifecycleProcessor(new ManagedSystemC2ComponentLifecycleProcessor());
		this.addMessageProcessor(new ManagedSystemC2ComponentMessageProcessor());
	}
	
	class ManagedSystemC2ComponentLifecycleProcessor extends LifecycleAdapter{
	}
	
	class ManagedSystemC2ComponentMessageProcessor implements MessageProcessor{
		public void handle(Message m){
		}
	}

}

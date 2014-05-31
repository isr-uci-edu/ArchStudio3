package archstudio.comp.shutdown;

import c2.fw.*;
import c2.legacy.*;

import java.util.*;

import archstudio.comp.aem.WrappedArchMessage;
import archstudio.comp.xarchtrans.*;

/**
 * This component lives in the ArchStudio 3 Bootstrapper.
 * The purpose of the component is to listen to when the boostrapped
 * architecture wants to shut down and do a System.exit() after an
 * ordered shutdown.
 * @author Eric M. Dashofy <A HREF="mailto:edashofy@ics.uci.edu">edashofy@ics.uci.edu</A>
 */
public class ShutdownC2Component extends AbstractC2DelegateBrick{

	public ShutdownC2Component(Identifier id){
		super(id);
		this.addMessageProcessor(new ShutdownMessageProcessor());
	}
	
	class ShutdownMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof WrappedArchMessage){
				Message om = ((WrappedArchMessage)m).getOriginalMessage();
				if(om instanceof ShutdownArchMessage){
					ShutdownArchMessage sam = (ShutdownArchMessage)om;
					System.exit(sam.getReturnCode());
				}
			}
		}
	}	
}

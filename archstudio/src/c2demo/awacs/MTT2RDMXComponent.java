package c2demo.awacs;

import java.util.*;

import c2.fw.*;

import edu.uci.isr.xarch.*;
import edu.uci.isr.xarch.types.*;

public class MTT2RDMXComponent extends SimpleAWACSBrick{

	public MTT2RDMXComponent(IComponent iComp){
		super(iComp);
	}

	public void handle(Message m){
		super.handle(m);
		if(m instanceof AWACSMessage){
			AWACSMessage am = (AWACSMessage)m;
			String payload = am.getPayload();
			if(payload.equals("MAINRDMX1 SIO 9")){
				sendAWACSMessage("MAINRDMX1", "SIO 9 Ack");
				workloadCPU(0.60d);
				int byteCountMtt = 200000;
				while(byteCountMtt > 0){
					sendAWACSMessage("SCSIRDMX1", "WRITE RMA2 SDS1");
					byteCountMtt -= 4000;  //!!!
				}
			}
			else{
				System.out.println(getIdentifier() + " absorbed message: " + payload);
			}
		}
	}
	
	public void tick(int timerCount){
		super.tick(timerCount);
	}

}

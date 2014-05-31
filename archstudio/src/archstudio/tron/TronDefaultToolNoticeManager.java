package archstudio.tron;

import archstudio.tron.*;
import c2.fw.*;
import c2.legacy.*;
import java.util.*;

public class TronDefaultToolNoticeManager{
	public static final int MAX_NOTICE_SIZE = 250;
	
	protected String toolID;
	protected AbstractC2DelegateBrick brick;
	
	protected List toolNoticeList = new ArrayList();
	
	public TronDefaultToolNoticeManager(String toolID, c2.legacy.AbstractC2DelegateBrick brick){
		this.toolID = toolID;
		this.brick = brick;
		brick.addMessageProcessor(new MyMessageProcessor());
	}
	
	class MyMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof TronGetAllToolNoticesMessage){
				sendNotices();
			}
		}
	}
	
	public void addNotices(String[] messages){
		TronToolNotice[] ttns = new TronToolNotice[messages.length];
		for(int i = 0; i < messages.length; i++){
			ttns[i] = new TronToolNotice(messages[i], null, null);
		}
		addNotices(ttns);
	}
	
	public void addNotice(String message){
		TronToolNotice ttn = new TronToolNotice(message, null, null);
		addNotice(ttn);
	}
	
	public void addNotice(String message, String additionalDetail){
		TronToolNotice ttn = new TronToolNotice(message, additionalDetail, null);
		addNotice(ttn);
	}
	
	public void addNotice(String message, Throwable error){
		TronToolNotice ttn = new TronToolNotice(message, null, error);
		addNotice(ttn);
	}
	
	public void addNotice(String message, String additionalDetail, Throwable error){
		TronToolNotice ttn = new TronToolNotice(message, additionalDetail, error);
		addNotice(ttn);
	}
	
	public synchronized void addNotice(TronToolNotice ttn){
		toolNoticeList.add(ttn);
		if(toolNoticeList.size() > MAX_NOTICE_SIZE){
			toolNoticeList.remove(0);
		}
		sendNotices();
	}
	
	public synchronized void addNotices(TronToolNotice[] ttns){
		for(int i = 0; i < ttns.length; i++){
			toolNoticeList.add(ttns[i]);
			if(toolNoticeList.size() > MAX_NOTICE_SIZE){
				toolNoticeList.remove(0);
			}
		}
		sendNotices();
	}
	
	protected synchronized void sendNotices(){
		TronToolNotice[] toolNotices = (TronToolNotice[])toolNoticeList.toArray(new TronToolNotice[0]);
		TronAllToolNoticesMessage tatnm = new TronAllToolNoticesMessage(toolID, toolNotices);
		brick.sendToAll(tatnm, brick.getInterface(AbstractC2DelegateBrick.BOTTOM_INTERFACE_ID));
	}
	
}

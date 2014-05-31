package archstudio.comp.criticartists.defaultartist;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import c2.fw.*;

public class MessagePassingJPanel extends JPanel implements MessageProvider, MessageListener, java.io.Serializable{

	public MessagePassingJPanel(java.awt.Component comp){
		super();
		this.setLayout(new BorderLayout());
		this.add("Center", comp);
	}
	
	private ArrayList messageListeners = new ArrayList();
	
	protected void fireMessageSent(Message m){
		synchronized(messageListeners){
			for(Iterator it = messageListeners.iterator(); it.hasNext(); ){
				MessageListener ml = (MessageListener)it.next();
				ml.messageSent(m);
			}
		}
	}
	
	public void addMessageListener(MessageListener l){
		synchronized(messageListeners){
			messageListeners.add(l);
		}
	}
	
	public void removeMessageListener(MessageListener l){
		synchronized(messageListeners){
			messageListeners.remove(l);
		}
	}
	
	public void messageSent(Message m){
		//System.out.println("mpjp got message: " + m);
		fireMessageSent(m);
	}
	

}

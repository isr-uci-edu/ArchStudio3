package archstudio.comp.criticartists.defaultartist;

import archstudio.editors.*;

import c2.fw.*;
import edu.uci.ics.xarchutils.*;
	
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class FocusEditorPanel extends JPanel implements ActionListener, MessageProvider, java.io.Serializable{
	
	private ObjRef xArchRef;
	private ObjRef[] refs;
	
	public FocusEditorPanel(ObjRef xArchRef, ObjRef ref){
		super();
		this.xArchRef = xArchRef;
		this.refs = new ObjRef[]{ref};
		init();
	}
	
	public FocusEditorPanel(ObjRef xArchRef, ObjRef[] refs){
		super();
		this.xArchRef = xArchRef;
		this.refs = refs;
		init();
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
	
	private void init(){
		this.setLayout(new FlowLayout(FlowLayout.CENTER));
		JButton b = new JButton("Focus Open Editors");
		b.addActionListener(this);
		this.add(b);
	}
	
	public void actionPerformed(ActionEvent evt){
		FocusEditorMessage fem = new FocusEditorMessage(null, xArchRef, refs, FocusEditorMessage.FOCUS_OPEN_DOCS);
		fireMessageSent(fem);
	}
	
}

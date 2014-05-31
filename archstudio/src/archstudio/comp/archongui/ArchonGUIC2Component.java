package archstudio.comp.archongui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

import archstudio.archon.*;
import archstudio.invoke.*;
import c2.fw.*;
import c2.legacy.*;
import c2.pcwrap.EBIWrapperUtils;
import edu.uci.ics.widgets.*;

public class ArchonGUIC2Component extends AbstractC2DelegateBrick implements InvokableBrick{
	public static final String PRODUCT_NAME = "Archon Editor";
	public static final String SERVICE_NAME = "Scripting/Archon Interactive" +
			"";

	protected static int interpreterNumber = 1;
	protected Vector openWindows;

	public ArchonGUIC2Component(Identifier id){
		super(id);
		openWindows = new Vector();
		this.addLifecycleProcessor(new ArchonGUILifecycleProcessor());
		this.addMessageProcessor(new ArchonGUIMessageProcessor());

		InvokeUtils.deployInvokableService(this, bottomIface, 
			SERVICE_NAME, 
			"The Python-based ArchStudio 3 Scripting Environment");
	}

	class ArchonGUILifecycleProcessor extends LifecycleAdapter{
		public void end(){
			for(int i = 0; i < openWindows.size(); i++){
				((ArchonGUIFrame)openWindows.elementAt(i)).close();
			}
		}
	}

	public void invoke(InvokeMessage im){
		if(im.getServiceName().equals(SERVICE_NAME)){
			newWindow();
		}
	}
	
	public static String generateInterpreterID(){
		return "Interactive Interpreter " + interpreterNumber++;
	}

	public ArchonGUIFrame newWindow(){
		synchronized(openWindows){
			String id = generateInterpreterID();
			createInterpreter(id);
			ArchonGUIFrame f = new ArchonGUIFrame(id);
			openWindows.addElement(f);
			return f;
		}
	}
	
	private ArchonGUIFrame getWindow(String interpreterID){
		synchronized(openWindows){
			int size = openWindows.size();
			for(int i = (size - 1); i >= 0; i--){
				ArchonGUIFrame f = (ArchonGUIFrame)openWindows.elementAt(i);
				String fID = f.getInterpreterID();
				if((fID != null) && (fID.equals(interpreterID))){
					return f;
				}
			}
		}
		return null;
	}
	
	class ArchonGUIMessageProcessor implements MessageProcessor{
		public void handle(Message m){
			if(m instanceof ArchonOutputMessage){
				ArchonOutputMessage aom = (ArchonOutputMessage)m;
				String id = aom.getInterpreterID();
				ArchonGUIFrame f = getWindow(id);
				if(f != null){
					f.appendOutput(aom.getOutput());
				}
			}
			else if(m instanceof ArchonInputReadyMessage){
				ArchonInputReadyMessage airm = (ArchonInputReadyMessage)m;
				String id = airm.getInterpreterID();
				ArchonGUIFrame f = getWindow(id);
				if(f != null){
					f.setReady(airm.getContinuing());
				}
			}
		}
	}
	
	protected void createInterpreter(String interpreterID){
		ArchonCreateInterpreterMessage acim = new ArchonCreateInterpreterMessage(interpreterID);
		sendToAll(acim, topIface);
	}
	
	protected void exec(String interpreterID, String line){
		ArchonExecMessage aem = new ArchonExecMessage(interpreterID, line);
		sendToAll(aem, topIface);
	}

	class ArchonGUIFrame extends JFrame{
		public static final int BUFFER_OUTS = 500;
		public static final int OVERFLOW_OUTS = 100;
		
		protected String interpreterID;
		protected JLabel lPrompt;
		protected JScrollPane taOutputScrollPane;
		protected JTextArea taOutput;
		protected JTextArea taInput;
		
		protected java.util.List outputList = new ArrayList(BUFFER_OUTS + OVERFLOW_OUTS);
		
		public ArchonGUIFrame(String interpreterID){
			super();
			this.interpreterID = interpreterID;
			
			archstudio.Branding.brandFrame(this);
			setTitle("Archon " + interpreterID);
			
			init();
			
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			this.addWindowListener(
				new WindowAdapter(){
					public void windowClosing(WindowEvent e){
						close();
					};
				}
			);
			
			setReady(false);
			this.setVisible(true);

			WidgetUtils.validateAndRepaintInAWTThread(this);
		}
		
		public void close(){
			openWindows.removeElement(this);
			this.setVisible(false);
			this.dispose();
			ArchonDestroyInterpreterMessage adim = 
				new ArchonDestroyInterpreterMessage(interpreterID);
			sendToAll(adim, topIface);
		}
		
		public String getInterpreterID(){
			return interpreterID;
		}
		
		public void init(){
			taOutput = new JTextArea();
			taOutput.setMargin(new Insets(2,2,2,2));
			taOutput.setFont(WidgetUtils.MONOSPACE_PLAIN_MEDIUM_FONT);
			taOutput.setEditable(false);
			
			taInput = new JTextArea();
			taInput.setBorder(new LineBorder(Color.BLACK, 1));
			Keymap newKeymap = JTextComponent.addKeymap("inputHandler", taInput.getKeymap());
			newKeymap.addActionForKeyStroke(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), new HandleInputAction());
			newKeymap.addActionForKeyStroke(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK),
				new AbstractAction(){
					public void actionPerformed(ActionEvent evt){
						StringBuffer buf = new StringBuffer(taInput.getText());
						int pos = taInput.getCaretPosition();
						buf.insert(pos, '\n');
						taInput.setText(buf.toString());
						taInput.setCaretPosition(pos+1);
					}
				}
			);
			taInput.setKeymap(newKeymap);
			taInput.setFont(WidgetUtils.MONOSPACE_PLAIN_MEDIUM_FONT);
			
			lPrompt = new JLabel(">>>");
			lPrompt.setFont(WidgetUtils.MONOSPACE_PLAIN_MEDIUM_FONT);
			
			JPanel inputPanel = new JPanel();
			inputPanel.setLayout(new BorderLayout());
			inputPanel.add("West", new JPanelIS(lPrompt, 3));
			inputPanel.add("Center", new JPanelIS(taInput, 3));
			
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			taOutputScrollPane = new JScrollPane(taOutput);
			mainPanel.add("Center", new JPanelIS(taOutputScrollPane, 3));
			mainPanel.add("South", inputPanel);
			
			this.getContentPane().setLayout(new BorderLayout());
			this.getContentPane().add("Center", mainPanel);
			setSize(500, 400);
			WidgetUtils.centerInScreen(this);
		}
		
		public void setPrompt(String prompt){
			lPrompt.setText(prompt);
			WidgetUtils.validateAndRepaintInAWTThread(this);
		}
		
		public void setReady(boolean continuing){
			if(continuing){
				setPrompt("...");
			}
			else{
				setPrompt(">>>");
			}
			WidgetUtils.validateAndRepaintInAWTThread(this);
		}
		
		public void appendOutput(String output){
			outputList.add(output);
			if(outputList.size() == BUFFER_OUTS + OVERFLOW_OUTS){
				outputList = new ArrayList(outputList.subList(OVERFLOW_OUTS, BUFFER_OUTS + OVERFLOW_OUTS));
			}
			StringBuffer sb = new StringBuffer(outputList.size() * 40);
			for(Iterator it = outputList.iterator(); it.hasNext(); ){
				sb.append(it.next().toString());
			}
			taOutput.setText(sb.toString());
			taOutput.setCaretPosition(taOutput.getDocument().getLength());
		}
		
		public String[] getLines(String multilines){
			try{
				java.util.List lineList = new ArrayList();
				BufferedReader br = new BufferedReader(new StringReader(multilines));
				while(true){
					String line = br.readLine();
					if(line != null){
						lineList.add(line);
					}
					else{
						break;
					}
				}
				return (String[])lineList.toArray(new String[0]);
			}
			catch(IOException wontHappen){}
			return null;
		}
		
		class HandleInputAction extends AbstractAction{
			public void actionPerformed(ActionEvent evt){
				String alllines = taInput.getText();
				String[] lines = getLines(alllines);
				taInput.setText("");
				String prompt = lPrompt.getText();
				if(lines.length == 0){
					//appendOutput(prompt + "" + "\n");
					exec(interpreterID, "");
				}
				for(int i = 0; i < lines.length; i++){
					//appendOutput(prompt + lines[i] + "\n");
					exec(interpreterID, lines[i]);
				}
			}
		}
	}
}

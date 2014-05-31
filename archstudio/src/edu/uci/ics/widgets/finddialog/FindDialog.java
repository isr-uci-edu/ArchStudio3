package edu.uci.ics.widgets.finddialog;

import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;

import edu.uci.ics.widgets.HorizontalLine;
import edu.uci.ics.widgets.JPanelIS;
import edu.uci.ics.widgets.WidgetUtils;

public class FindDialog extends JDialog implements ActionListener{
	
	protected Frame parentFrame;
	
	protected JTextField tfFind;
	
	protected JButton bFind;
	protected JButton bClose;
	
	protected JList resultsList;
	protected DefaultListModel resultsListModel;
	
	protected JButton bGoto;

	
	public static void main(String[] args){
		FindDialog fd = new FindDialog(null, "Find by Label...");
		fd.setLocation(50, 50);
		fd.setSize(300, 270);
		fd.setVisible(true);
		fd.setNoResults();
	}
	
	
	public FindDialog(Frame f, String title){
		super(f, (title == null) ? "Find..." : title);
		
		this.parentFrame = f;
		
		JLabel l = new JLabel("Find:");
		tfFind = new JTextField(15);
		tfFind.addActionListener(this);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		bFind = new JButton("Find");
		WidgetUtils.setMnemonic(bFind, 'F');
		bFind.addActionListener(this);
		
		bClose = new JButton("Close");
		WidgetUtils.setMnemonic(bClose, 'C');
		bClose.addActionListener(this);
		
		buttonPanel.add(bFind);
		buttonPanel.add(bClose);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JPanel tfPanel = new JPanel();
		tfPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		tfPanel.add(l);
		tfPanel.add(tfFind);
		mainPanel.add(new JPanelIS(tfPanel, 3));
		mainPanel.add(buttonPanel);
		
		JPanel resultsPanel = new JPanel();
		resultsPanel.setLayout(new BorderLayout());
		
		JPanel resultsHeader = new JPanel();
		resultsHeader.setLayout(new BoxLayout(resultsHeader, BoxLayout.Y_AXIS));
		resultsHeader.add(new HorizontalLine());
		resultsHeader.add(new JPanelIS(new JLabel("Results:"), 3));
		
		resultsListModel = new DefaultListModel();
		resultsList = new JList(resultsListModel);
		setNoResults();
		
		bGoto = new JButton("Go to...");
		WidgetUtils.setMnemonic(bGoto, 'G');
		bGoto.addActionListener(this);
		
		JPanel gotoPanel = new JPanel();
		gotoPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		gotoPanel.add(bGoto);
		
		resultsPanel.add("North", resultsHeader);
		resultsPanel.add("Center", new JPanelIS(new JScrollPane(resultsList), 3));
		resultsPanel.add("South", gotoPanel);
		
		Container c = this.getContentPane();
		c.setLayout(new BorderLayout());
		
		c.add("North", mainPanel);
		c.add("Center", resultsPanel);
		
		resultsList.setFocusable(true);
		resultsList.addMouseListener(new ListGotoMouseAdapter());
		resultsList.addKeyListener(new ListGotoKeyAdapter());
		
		EscKeyListener ekl = new EscKeyListener();
		WidgetUtils.addKeyListenerToAll(this, ekl);
		
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		//this.setDefaultCloseOperation(
		//	JDialog.DISPOSE_ON_CLOSE);

		this.addWindowListener(new FindDialogWindowAdapter());
	}
	
	class FindDialogWindowAdapter extends WindowAdapter{
		public void windowClosing(WindowEvent evt) {
			doClose();
		}
	}
	
	public void doClose(){
		fireIsClosing(this);
		this.setVisible(false);
		this.dispose();
		if(parentFrame != null){
			parentFrame.invalidate();
			parentFrame.repaint();
		}
	}
	
	boolean specialMode = false;

	public void setSearching(){
		resultsListModel.clear();
		resultsListModel.addElement("[Searching...]");
		resultsList.setEnabled(false);
		specialMode = true;
	}
	
	public void setNoResults(){
		resultsListModel.clear();
		resultsListModel.addElement("[No Results]");
		resultsList.setEnabled(false);
		specialMode = true;
	}
	
	public void setResults(Object[] results){
		resultsListModel.clear();
		for(int i = 0; i < results.length; i++){
			resultsListModel.addElement(results[i]);
		}
		resultsList.setEnabled(true);
		resultsList.requestFocus();
		if(results.length > 0){
			resultsList.setSelectedIndex(0);
		}
		specialMode = false;
	}

	public void addResult(Object result){
		if(specialMode == true){
			resultsListModel.clear();
		}
		boolean firstResult = false;
		if(resultsListModel.getSize() == 0){
			firstResult = true;
		}
		resultsListModel.addElement(result);
		resultsList.setEnabled(true);
		resultsList.requestFocus();
		if(firstResult){
			resultsList.setSelectedIndex(0);
		}
		specialMode = false;
	}

	public void actionPerformed(ActionEvent evt){
		Object src = evt.getSource();
		if(src == tfFind){
			bFind.doClick();
		}
		else if(src == bFind){
			String text = tfFind.getText();
			if(text.trim().equals("")){
				return;
			}
			else{
				fireDoFind(text);
			}
		}
		else if(src == bGoto){
			Object selectedObject = resultsList.getSelectedValue();
			fireDoGoto(selectedObject);
		}
		else if(src == bClose){
			doClose();
		}
	}
	
	class EscKeyListener extends KeyAdapter{
		public void keyReleased(KeyEvent e){
			if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
				bClose.doClick();
			}
		}
	}
	
	class ListGotoKeyAdapter extends KeyAdapter{
		public void keyPressed(KeyEvent key){
			if(key.getKeyCode() == KeyEvent.VK_ENTER){
				if(resultsList.getSelectedIndex() != -1){
					bGoto.doClick();
				}
			}
		}
	}
	
	class ListGotoMouseAdapter extends MouseAdapter{
		public void mouseClicked(MouseEvent evt){
			int clickCount = evt.getClickCount();
			if(resultsList.getSelectedIndex() != -1){
				if(clickCount == 2){
					bGoto.doClick();
				}
			}
		}
	}
	
	protected Vector findDialogListeners = new Vector();
	 
	public void addFindDialogListener(FindDialogListener fdl){
		synchronized(findDialogListeners){
			findDialogListeners.addElement(fdl);
		}
	}
	
	public void removeFindDialogListener(FindDialogListener fdl){
		synchronized(findDialogListeners){
			findDialogListeners.removeElement(fdl);
		}
	}
	
	protected void fireDoFind(String text){
		synchronized(findDialogListeners){
			for(Iterator it = findDialogListeners.iterator(); it.hasNext(); ){
				FindDialogListener fdl = (FindDialogListener)it.next();
				fdl.doFind(this, text);
			}
		}
	}
	
	protected void fireDoGoto(Object target){
		synchronized(findDialogListeners){
			for(Iterator it = findDialogListeners.iterator(); it.hasNext(); ){
				FindDialogListener fdl = (FindDialogListener)it.next();
				fdl.doGoto(this, target);
			}
		}
	}
	
	protected void fireIsClosing(Object target){
		synchronized(findDialogListeners){
			for(Iterator it = findDialogListeners.iterator(); it.hasNext(); ){
				FindDialogListener fdl = (FindDialogListener)it.next();
				fdl.isClosing(this);
			}
		}
	}
}

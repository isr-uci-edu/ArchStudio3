package edu.uci.ics.widgets;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Wizard extends JPanel{
	
	protected java.awt.Window enclosingWindow = null;
	protected JComponent marquee = null;
	protected java.util.List cardTitles = new ArrayList();
	protected java.util.List panels = new ArrayList();
	
	protected JButton nextButton;
	protected JButton prevButton;
	protected JButton exitButton;

	protected JComponent currentPanel = null;
	
	protected Map dataMap;
	
	public Wizard(){
		this.dataMap = new HashMap();
	}
	
	public void setEnclosingWindow(java.awt.Window enclosingWindow){
		this.enclosingWindow = enclosingWindow;
	}
	
	public void setMarquee(JComponent marquee){
		this.marquee = marquee;
	}
	
	public void setCards(String[] cardTitles, JComponent[] cards){
		if(cardTitles != null){
			if(cardTitles.length != cards.length){
				throw new IllegalArgumentException("cardTitles.length must == panels.length");
			}
			this.cardTitles.addAll(Arrays.asList(cardTitles));
		}
		else{
			this.cardTitles = null;
		}
		this.panels.addAll(Arrays.asList(cards));
		init();
	}

	protected JComponent getNextPanel(){
		if(currentPanel == null){
			return (JComponent)panels.get(0);
		}
		
		for(Iterator it = panels.iterator(); it.hasNext(); ){
			JComponent p = (JComponent)it.next();
			if(p == currentPanel){
				if(!it.hasNext()){
					return null;
				}
				return (JComponent)it.next();
			}
		}
		
		throw new RuntimeException("The current panel is not in the list of panels.");
	}
	
	protected JComponent getPreviousPanel(){
		if(currentPanel == null){
			return null;
		}
		
		for(ListIterator it = panels.listIterator(panels.size()); it.hasPrevious(); ){
			JComponent p = (JComponent)it.previous();
			if(p == currentPanel){
				if(!it.hasPrevious()){
					return null;
				}
				
				return (JComponent)it.previous();
			}
		}
		
		throw new RuntimeException("The current panel is not in the list of panels.");
	}
	
	protected JComponent getCurrentPanel(){
		return currentPanel;
	}
	
	protected void setCurrentPanel(JComponent panel){
		JComponent oldCurrentPanel = currentPanel;
		if(oldCurrentPanel != null){
			this.remove(oldCurrentPanel);
		}
		this.add("Center", panel);
		this.currentPanel = panel;
		if(cardTitles != null){
			int panelIndex = panels.indexOf(panel);
			String title = (String)cardTitles.get(panelIndex);
			if(enclosingWindow != null){
				if(enclosingWindow instanceof java.awt.Frame){
					((java.awt.Frame)enclosingWindow).setTitle(title);
				}
				else if(enclosingWindow instanceof java.awt.Dialog){
					((java.awt.Dialog)enclosingWindow).setTitle(title);
				}
			}
		}
		
		this.checkButtons();
		this.validate();
		this.repaint();
	}

	protected boolean shouldNextButtonBeEnabled(JComponent currentPanel){
		return true;
	}
	
	protected boolean isLastPanel(JComponent panel){
		return panel == panels.get(panels.size() - 1);
	}
	
	protected boolean isFirstPanel(JComponent panel){
		return panel == panels.get(0);
	}
	
	public void next(){
		JComponent nextPanel = getNextPanel();
		if(nextPanel == null){
			finish();
			return;
		}
		setCurrentPanel(nextPanel);
	}
	
	public void previous(){
		JComponent previousPanel = getPreviousPanel();
		if(previousPanel == null){
			return;
		}
		setCurrentPanel(previousPanel);
	}
	
	public void setExitButtonEnabled(boolean enabled){
		exitButton.setEnabled(enabled);
	}
	
	public boolean isExitButtonEnabled(){
		return exitButton.isEnabled();
	}
	
	protected void init(){
		this.removeAll();
		this.setLayout(new BorderLayout());
		if(marquee != null){
			this.add("West", marquee);
		}
		
		exitButton = new JButton("Exit");
		prevButton = new JButton("<- Prev");
		nextButton = new JButton("Next ->");
		
		nextButton.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					next();
				}
			}
		);
		
		prevButton.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					previous();
				}
			}
		);
		
		exitButton.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					exit();
				}
			}
		);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		
		JPanel exitButtonPanel = new JPanel();
		exitButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		exitButtonPanel.add(exitButton);
		
		JPanel prevNextButtonPanel = new JPanel();
		prevNextButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		prevNextButtonPanel.add(prevButton);
		prevNextButtonPanel.add(nextButton);
		
		buttonPanel.add("West", exitButtonPanel);
		buttonPanel.add("East", prevNextButtonPanel);
		
		this.add("South", buttonPanel);
		
		next();
		
		this.checkButtons();
		this.validate();
		this.repaint();
	}
	
	protected void checkButtons(){
		if(currentPanel == null){
			return;
		}
		
		if(isFirstPanel(currentPanel)){
			prevButton.setEnabled(false);
		}
		else{
			prevButton.setEnabled(true);
		}
		
		if(isLastPanel(currentPanel)){
			nextButton.setText("Finish");
		}
		else{
			nextButton.setText("Next ->");
		}
		
		nextButton.setEnabled(shouldNextButtonBeEnabled(currentPanel));
	}
	
	public void finish(){
	}

	public void exit(){
	}
	
	public void setData(String name, java.io.Serializable data){
		dataMap.put(name, data);
		checkButtons();
		fireWizardDataChanged(this, name, data);
	}
	
	public Object getData(String name){
		return dataMap.get(name);
	}
	
	protected Vector listeners = new Vector();
	
	public void addWizardDataChangeListener(WizardDataChangeListener l){
		listeners.addElement(l);
	}
	
	public void removeWizardDataChangeListener(WizardDataChangeListener l){
		listeners.removeElement(l);
	}
	
	protected void fireWizardDataChanged(Wizard w, String key, java.io.Serializable value){
		synchronized(listeners){
			for(Iterator it = listeners.iterator(); it.hasNext(); ){
				((WizardDataChangeListener)it.next()).wizardDataChanged(w, key, value);
			}
		}
	}
	
	
}

package archstudio.comp.criticartists.defaultartist;

import archstudio.comp.xarchtrans.XArchFlatTransactionsInterface;
import archstudio.critics.*;
import archstudio.invoke.*;

import c2.fw.*;
import c2.pcwrap.*;
import c2.legacy.*;

import edu.uci.ics.widgets.*;
import edu.uci.ics.xarchutils.*;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class DefaultCriticArtistC2Component extends AbstractC2DelegateBrick{

	protected XArchFlatTransactionsInterface xarch = null;
	
	public DefaultCriticArtistC2Component(Identifier id){
		super(id);
		xarch = (XArchFlatTransactionsInterface)EBIWrapperUtils.addExternalService(this, topIface, XArchFlatTransactionsInterface.class);
		addMessageProcessor(new CriticIssueMessageProcessor());
	}
	
	private static String breakWords(String s){
		StringBuffer sb = new StringBuffer();
		int len = s.length();
		int wordLen = 0;
		for(int i = 0; i < len; i++){
			char ch = s.charAt(i);
			if(ch == ' '){
				if(wordLen > 2){
					sb.append("<BR>");
					wordLen = 0;
				}
				else{
					sb.append(ch);
					wordLen++;
				}
			}
			else{
				sb.append(ch);
				wordLen++;
			}
		}
		return sb.toString();
	}
	
	class CriticIssueMessageProcessor implements MessageProcessor{
		private java.awt.Component[] getLabels(String s1, String s2){
			JLabel lab1 = new JLabel("<html><b>" + s1 + "</b></html>");
			lab1.setFont(WidgetUtils.SANSSERIF_BOLD_MEDIUM_FONT);
			JLabel lab2 = new JLabel("<html>" + s2 + "</html>");
			lab2.setFont(WidgetUtils.SANSSERIF_PLAIN_MEDIUM_FONT);
			
			java.awt.Component l1 = lab1;
			java.awt.Component l2 = new JPanelEWL(lab2);
			return new java.awt.Component[]{l1, l2};
		}
		
		private java.awt.Component[] getComponents(String lab, java.awt.Component comp){
			JLabel lab1 = new JLabel("<html><b>" + lab + "</b></html>");
			lab1.setFont(WidgetUtils.SANSSERIF_BOLD_MEDIUM_FONT);
			java.awt.Component l1 = lab1;
			return new java.awt.Component[]{l1, new JPanelUL(comp)};
		}
		
		public void handle(Message m){
			if(m instanceof CriticIssueMessage){
				CriticIssueMessage cim = (CriticIssueMessage)m;
				if(cim.getStatus() == CriticIssueMessage.ISSUE_OPEN){
					CriticIssue issue = cim.getIssue();
					ImageIcon categoryIcon = WidgetUtils.getImageIcon("archstudio/critics/res/" +
						issue.getCategoryID().toString() + ".gif");
					if(categoryIcon == null){
						categoryIcon = WidgetUtils.getImageIcon("archstudio/critics/res/warning.gif");
					}
					String labString = issue.getCategoryDescription();
					labString = "<html><center>" + breakWords(labString) + "</center></html>";
					
					JLabel lab = new JLabel(labString);
					lab.setIcon(categoryIcon);
					lab.setVerticalTextPosition(SwingConstants.BOTTOM);
					lab.setHorizontalTextPosition(SwingConstants.CENTER);
					
					JLabel headlineLabel = new JLabel("<html><b>" + issue.getHeadline() + "</b></html>");
					
					DefaultTableModel issueTableModel = new DefaultTableModel();
					issueTableModel.addColumn("1");
					issueTableModel.addColumn("2");
					
					String architectureURI = "[unknown]";
					try{
						architectureURI = xarch.getXArchURI(issue.getXArchRef());
					}
					catch(Exception e){
					}
					
					issueTableModel.addRow(getLabels("Information:", issue.getSpecificInfo()));
					issueTableModel.addRow(getLabels("Description:", issue.getIssueDescription()));
					issueTableModel.addRow(getLabels("Arch:", architectureURI));
					issueTableModel.addRow(getLabels("Critic:", issue.getCriticID().toString()));
					
					FocusEditorPanel focusPanel = new FocusEditorPanel(issue.getXArchRef(),
						issue.getAffectedElements());
					issueTableModel.addRow(getComponents("Focus:", focusPanel));
					
					JStaticTable st = new JStaticTable(issueTableModel, 5, 5);

					JPanelUL ip = new JPanelUL(st);
					
					JExpandableDataWidget edw = new JExpandableDataWidget(headlineLabel, ip);
					edw.validate();
					
					MessagePassingJPanel mpjp = new MessagePassingJPanel(edw);
					focusPanel.addMessageListener(mpjp);
						
					CriticArtistMessage cam = new CriticArtistMessage(issue, lab, mpjp, CriticArtistMessage.FIDELITY_LOW);
					sendToAll(cam, bottomIface);
				}
			}
		}
	}
	
}

package edu.uci.ics.widgets;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

public class TooltipComboBoxRenderer extends BasicComboBoxRenderer{
  public Component getListCellRendererComponent(JList list, 
  Object value, int index, boolean isSelected, boolean cellHasFocus){
  	Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    if (-1 < index) {
      list.setToolTipText(value.toString());
    }
    return c;
  }  
}


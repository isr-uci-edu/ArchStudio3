package archstudio.comp.tron.gui;

import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JPanel;

import edu.uci.ics.widgets.Colors;
import edu.uci.ics.widgets.JPanelGR;

public class TronGUIDescriptionPanel extends JPanelGR{

	public TronGUIDescriptionPanel(){
		super(java.awt.Color.WHITE, new java.awt.Color(0xdd, 0xdd, 0xdd));
	}

	public Insets getInsets(){
		return new Insets(3,5,3,5);
	}
}

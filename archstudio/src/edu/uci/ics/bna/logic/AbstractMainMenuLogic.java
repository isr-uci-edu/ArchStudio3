package edu.uci.ics.bna.logic;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import edu.uci.ics.bna.ThingLogic;
import edu.uci.ics.bna.ThingLogicAdapter;

public abstract class AbstractMainMenuLogic extends ThingLogicAdapter implements ThingLogic{
	
	protected JMenuBar mainMenu = null;
	
	public AbstractMainMenuLogic(JMenuBar mainMenu){
		this.mainMenu = mainMenu;
	}
	
	protected JMenuBar getMainMenu(){
		return mainMenu;
	}

}

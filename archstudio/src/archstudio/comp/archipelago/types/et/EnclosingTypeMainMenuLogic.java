package archstudio.comp.archipelago.types.et;

import archstudio.comp.archipelago.*;
import archstudio.comp.archipelago.types.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.logic.*;
import edu.uci.ics.widgets.WidgetUtils;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.ObjRef;
import edu.uci.ics.xarchutils.XArchFlatInterface;

public class EnclosingTypeMainMenuLogic extends AbstractMainMenuLogic implements ActionListener{
	//protected JMenuBar mainMenu;
	protected JMenu viewMenu;

	protected JMenuItem miSetEnclosingType;
	protected JMenuItem miClearEnclosingType;
	
	protected XArchFlatInterface xarch;
	protected ThingIDMap thingIDMap;
	protected ObjRef archStructureRef;
	
	//--
	protected ObjRef enclosingTypeRef = null;
	protected Thing enclosingTypeThing = null;
	
	public EnclosingTypeMainMenuLogic(JMenuBar mainMenu, ObjRef archStructureRef, ThingIDMap thingIDMap, XArchFlatInterface xarch){
		super(mainMenu);
		this.archStructureRef = archStructureRef;
		this.xarch = xarch;
		this.thingIDMap = thingIDMap;
		
		viewMenu = WidgetUtils.getSubMenu(mainMenu, "View");
		if(viewMenu == null){
			viewMenu = new JMenu("View");
			WidgetUtils.setMnemonic(viewMenu, 'V');
			mainMenu.add(viewMenu);
		}

		miSetEnclosingType = new JMenuItem("View Enclosing Type...");
		//miDoDotLayout.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.Event.CTRL_MASK));
		miSetEnclosingType.addActionListener(this);
		
		miClearEnclosingType = new JMenuItem("Clear Enclosing Type");
		//miDoDotLayout.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.Event.CTRL_MASK));
		miClearEnclosingType.addActionListener(this);
		
		viewMenu.add(miSetEnclosingType);
	}

	public void destroy(){
		viewMenu.remove(miSetEnclosingType);
		viewMenu.remove(miClearEnclosingType);
		if(viewMenu.getItemCount() == 0){
			mainMenu.remove(viewMenu);
		}
	}

	private static class EnclosingTypeChoice{
		public ObjRef typeRef;
		public String description;
		
		public EnclosingTypeChoice(ObjRef typeRef, String description){
			this.typeRef = typeRef;
			this.description = description;
		}
		
		public String toString(){
			return description;
		}
	}

	public void actionPerformed(ActionEvent e){
		if(e.getSource() == miSetEnclosingType){
			ObjRef xArchRef = xarch.getXArch(archStructureRef);
			ObjRef[] enclosingTypeRefs = EnclosingTypeUtils.getEnclosingTypes(xarch, xArchRef, archStructureRef);
			if(enclosingTypeRefs.length == 0){
				JOptionPane.showMessageDialog(getBNAComponent(), "This structure is not enclosed by any type.");
				return;
			}
			else{
				EnclosingTypeChoice[] choices = new EnclosingTypeChoice[enclosingTypeRefs.length];
				for(int i = 0; i < enclosingTypeRefs.length; i++){
					String description = XadlUtils.getDescription(xarch, enclosingTypeRefs[i]);
					if(description == null){
						description = "[Type Without Description]";
					}
					choices[i] = new EnclosingTypeChoice(enclosingTypeRefs[i], description);
				}
				EnclosingTypeChoice result = 
					(EnclosingTypeChoice)JOptionPane.showInputDialog(getBNAComponent(), "Select Enclosing Type", 
						"Select Enclosing Type", JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
				if(result == null){
					return;
				}
			}
		}
		else if(e.getSource() == miClearEnclosingType){
			
		}
	}
	
	public synchronized void setEnclosingType(ObjRef enclosingTypeRef){
		if(this.enclosingTypeRef != null){
			clearEnclosingType();
		}
		this.enclosingTypeRef = enclosingTypeRef;
	}
	

	public synchronized void clearEnclosingType(){
		//TODO this
	}
	
	public synchronized void updateEnclosingType(){
		BNAComponent c = getBNAComponent();
		if(c == null) return;
		BNAModel m = c.getModel();
		if(m == null) return;
		
		if((enclosingTypeRef == null) || (!xarch.isAttached(enclosingTypeRef))){
			if(enclosingTypeThing != null){
				m.removeThingAndChildren(enclosingTypeThing);
				enclosingTypeRef = null;
				enclosingTypeThing = null;
				return;
			}
		}
	}
	
}

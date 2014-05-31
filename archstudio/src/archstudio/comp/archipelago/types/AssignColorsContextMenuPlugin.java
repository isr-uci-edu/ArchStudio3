package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.*;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.contextmenu.*;
import edu.uci.ics.widgets.ColorPickerDialog;
import edu.uci.ics.widgets.ColorSchemes;
import edu.uci.ics.widgets.ColorTransferable;
import edu.uci.ics.widgets.WidgetUtils;
import edu.uci.ics.xarchutils.ObjRef;

public class AssignColorsContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{
	
	protected ArchStructureTreePlugin astp;
	protected ArchTypesTreePlugin attp;
	protected ThingIDMap thingIDMap;
	
	public AssignColorsContextMenuPlugin(BNAComponent c, ArchStructureTreePlugin astp, ArchTypesTreePlugin attp, ThingIDMap thingIDMap){
		super(c);
		this.astp = astp;
		this.attp = attp;
		this.thingIDMap = thingIDMap;
	}
	
	public boolean canAssignColorManually(Thing t){
		if(!(t instanceof IColored)){
			return false;
		}
		if(t instanceof BrickThing){
			BrickThing bt = (BrickThing)t;
			if(bt.getInheritColorFromType()){
				return false;
			}
		}
		return true;
	}

	//Returns true if any of the things can have its
	//color assigned manually
	public boolean canAssignColorManually(Thing[] t){
		for(int i = 0; i < t.length; i++){
			if(canAssignColorManually(t[i])){
				return true;
			}
		}
		return false;
	}
	
	public JPopupMenu addToContextMenu(JPopupMenu currentContextMenu, Thing[] selectedThingSet, Thing thingUnderCursor){
		List miList = new ArrayList();
		List brickThingList = new ArrayList();
		List componentThingList = new ArrayList();
		List connectorThingList = new ArrayList();
		List linkThingList = new ArrayList();
		
		List brickTypeThingList = new ArrayList();
		List componentTypeThingList = new ArrayList();
		List connectorTypeThingList = new ArrayList();
		
		for(int i = 0; i < selectedThingSet.length; i++){
			if(selectedThingSet[i] instanceof BrickThing){
				brickThingList.add(selectedThingSet[i]);
				if(selectedThingSet[i] instanceof ComponentThing){
					componentThingList.add(selectedThingSet[i]);
				}
				else if(selectedThingSet[i] instanceof ConnectorThing){
					connectorThingList.add(selectedThingSet[i]);
				}
			}
			else if(selectedThingSet[i] instanceof LinkThing){
				linkThingList.add(selectedThingSet[i]);
			}
			else if(selectedThingSet[i] instanceof BrickTypeThing){
				brickTypeThingList.add(selectedThingSet[i]);
				if(selectedThingSet[i] instanceof ComponentTypeThing){
					componentTypeThingList.add(selectedThingSet[i]);
				}
				else if(selectedThingSet[i] instanceof ConnectorTypeThing){
					connectorTypeThingList.add(selectedThingSet[i]);
				}
			}
		}
		
		if(brickThingList.size() > 0){
			Thing[] brickThings = (Thing[])brickThingList.toArray(new Thing[0]);
			String name = (brickThings.length == 1) ? "Brick" : "Bricks";
			InheritTypeColorsMenuItemSet mis2 = new InheritTypeColorsMenuItemSet(brickThings);
			JMenuItem[] miArray2 = mis2.getMenuItemSet();
			for(int i = 0; i < miArray2.length; i++){
				miList.add(miArray2[i]);
			}

			AssignColorsMenuItemSet mis = new AssignColorsMenuItemSet(brickThings, name);
			JMenuItem[] miArray = mis.getMenuItemSet();
			for(int i = 0; i < miArray.length; i++){
				miList.add(miArray[i]);
			}
		}
		if(componentThingList.size() > 0){
			Thing[] componentThings = (Thing[])componentThingList.toArray(new Thing[0]);
			String name = (componentThings.length == 1) ? "Component" : "Components";
			SetColorsMenuItemSet mis = new SetColorsMenuItemSet(componentThings, "Restore " + name + " to Default Color",
				astp.getDefaultComponentColor());
			JMenuItem[] miArray = mis.getMenuItemSet();
			for(int i = 0; i < miArray.length; i++){
				miList.add(miArray[i]);
			}
		}
		if(connectorThingList.size() > 0){
			Thing[] connectorThings = (Thing[])connectorThingList.toArray(new Thing[0]);
			String name = (connectorThings.length == 1) ? "Connector" : "Connectors";
			SetColorsMenuItemSet mis = new SetColorsMenuItemSet(connectorThings, "Restore " + name + " to Default Color",
				astp.getDefaultConnectorColor());
			JMenuItem[] miArray = mis.getMenuItemSet();
			for(int i = 0; i < miArray.length; i++){
				miList.add(miArray[i]);
			}
		}
		
		if(linkThingList.size() > 0){
			Thing[] linkThings = (Thing[])linkThingList.toArray(new Thing[0]);
			String name = (linkThings.length == 1) ? "Link" : "Links";
			AssignColorsMenuItemSet mis = new AssignColorsMenuItemSet(linkThings, name);
			JMenuItem[] miArray = mis.getMenuItemSet();
			for(int i = 0; i < miArray.length; i++){
				miList.add(miArray[i]);
			}
			SetColorsMenuItemSet mis2 = new SetColorsMenuItemSet(linkThings, "Restore " + name + " to Default Color",
				Color.BLACK);
			JMenuItem[] miArray2 = mis2.getMenuItemSet();
			for(int i = 0; i < miArray2.length; i++){
				miList.add(miArray2[i]);
			}
		}

		if(brickTypeThingList.size() > 0){
			Thing[] brickTypeThings = (Thing[])brickTypeThingList.toArray(new Thing[0]);
			String name = (brickTypeThings.length == 1) ? "Brick Type" : "Brick Types";
			AssignColorsMenuItemSet mis = new AssignColorsMenuItemSet(brickTypeThings, name);
			JMenuItem[] miArray = mis.getMenuItemSet();
			for(int i = 0; i < miArray.length; i++){
				miList.add(miArray[i]);
			}
		}
		if(componentTypeThingList.size() > 0){
			Thing[] componentTypeThings = (Thing[])componentTypeThingList.toArray(new Thing[0]);
			String name = (componentTypeThings.length == 1) ? "Component Type" : "Component Types";
			SetColorsMenuItemSet mis = new SetColorsMenuItemSet(componentTypeThings, "Restore " + name + " to Default Color",
				attp.getDefaultComponentTypeColor());
			JMenuItem[] miArray = mis.getMenuItemSet();
			for(int i = 0; i < miArray.length; i++){
				miList.add(miArray[i]);
			}
		}
		if(connectorTypeThingList.size() > 0){
			Thing[] connectorTypeThings = (Thing[])connectorTypeThingList.toArray(new Thing[0]);
			String name = (connectorTypeThings.length == 1) ? "Connector Type" : "Connector Types";
			SetColorsMenuItemSet mis = new SetColorsMenuItemSet(connectorTypeThings, "Restore " + name + " to Default Color",
				attp.getDefaultConnectorTypeColor());
			JMenuItem[] miArray = mis.getMenuItemSet();
			for(int i = 0; i < miArray.length; i++){
				miList.add(miArray[i]);
			}
		}
		
		if(selectedThingSet.length == 1){
			if(selectedThingSet[0] instanceof IColored){
				CopyColorMenuItemSet mis = new CopyColorMenuItemSet(selectedThingSet[0]);
				JMenuItem[] miArray = mis.getMenuItemSet();
				for(int i = 0; i < miArray.length; i++){
					miList.add(miArray[i]);
				}
			}
		}

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable t = clipboard.getContents(this);
		
		if((t != null) && (t.isDataFlavorSupported(ColorTransferable.COLOR_DATA_FLAVOR))){
			Color c = null;
			try{
				c = (Color)t.getTransferData(ColorTransferable.COLOR_DATA_FLAVOR);
				if(c != null){
					if(brickThingList.size() > 0){
						Thing[] brickThings = (Thing[])brickThingList.toArray(new Thing[0]);
						String name = (brickThings.length == 1) ? "Brick" : "Bricks";
						SetColorsMenuItemSet mis = new SetColorsMenuItemSet(brickThings, "Paste Color onto " + name, c);
						JMenuItem[] miArray = mis.getMenuItemSet();
						for(int i = 0; i < miArray.length; i++){
							miList.add(miArray[i]);
						}
					}
					if(linkThingList.size() > 0){
						Thing[] linkThings = (Thing[])linkThingList.toArray(new Thing[0]);
						String name = (linkThings.length == 1) ? "Link" : "Links";
						SetColorsMenuItemSet mis = new SetColorsMenuItemSet(linkThings, "Paste Color onto " + name, c);
						JMenuItem[] miArray = mis.getMenuItemSet();
						for(int i = 0; i < miArray.length; i++){
							miList.add(miArray[i]);
						}
					}
					if(brickTypeThingList.size() > 0){
						Thing[] brickTypeThings = (Thing[])brickTypeThingList.toArray(new Thing[0]);
						String name = (brickTypeThings.length == 1) ? "Brick Type" : "Brick Types";
						SetColorsMenuItemSet mis = new SetColorsMenuItemSet(brickTypeThings, "Paste Color onto " + name, c);
						JMenuItem[] miArray = mis.getMenuItemSet();
						for(int i = 0; i < miArray.length; i++){
							miList.add(miArray[i]);
						}
					}
				}
			}
			catch(UnsupportedFlavorException ufe){
			}
			catch(IOException ioe){
			}
		}
		
		if(miList.size() > 0){
			currentContextMenu.add(new JSeparator());
			for(java.util.Iterator it = miList.iterator(); it.hasNext(); ){
				currentContextMenu.add((JMenuItem)it.next());
			}
		}
		
		return currentContextMenu;
	}
	
	class AssignColorsMenuItemSet implements ActionListener{
		protected String name = null;
		protected Thing[] thingsToEdit;
		protected Color defaultColor = null;
		protected JMenuItem miAssignColor;
				
		public AssignColorsMenuItemSet(Thing[] thingsToEdit, String name){
			this.thingsToEdit = thingsToEdit;
			this.name = name;
			miAssignColor = new JMenuItem("Change " + name + " Color...");
			miAssignColor.addActionListener(this);
			if(thingsToEdit.length == 0){
				miAssignColor.setEnabled(false);
			}
			else if(thingsToEdit.length == 1){
				if(thingsToEdit[0] instanceof IColored){
					Color c = ((IColored)thingsToEdit[0]).getColor();
					miAssignColor.setIcon(WidgetUtils.getColorIcon(c, Color.BLACK, 16, 16));
					defaultColor = c;
				}
			}
			else{
				Set colors = new HashSet();
				for(int i = 0; i < thingsToEdit.length; i++){
					if(thingsToEdit[i] instanceof IColored){
						Color c = ((IColored)thingsToEdit[i]).getColor();
						colors.add(c);
					}
				}
				if(colors.size() == 1){
					Color[] colorArray = (Color[])colors.toArray(new Color[0]);
					miAssignColor.setIcon(WidgetUtils.getColorIcon(
						colorArray[0], Color.BLACK, 16, 16));
					defaultColor = colorArray[0];
				}
				else if(colors.size() > 1){
					Color[] colorArray = (Color[])colors.toArray(new Color[0]);
					miAssignColor.setIcon(WidgetUtils.getCyclicColorIcon(colorArray, Color.BLACK, 16, 16));
				}
			}
			if(!canAssignColorManually(thingsToEdit)){
				miAssignColor.setEnabled(false);
			}
		}
		
		public JMenuItem[] getMenuItemSet(){
			return new JMenuItem[]{miAssignColor};
		}
		
		public void actionPerformed(ActionEvent evt){
			Frame parentFrame = null;
			if(getBNAComponent() != null){
				parentFrame = WidgetUtils.getAncestorFrame(getBNAComponent());
			}
			Color newColor = ColorPickerDialog.showDialog(parentFrame, "Choose Color for " + name, 
				ColorSchemes.ALL_COLOR_SCHEMES, defaultColor);
			if(newColor == null){
				return;
			}
			for(int i = 0; i < thingsToEdit.length; i++){
				if(thingsToEdit[i] instanceof IColored){
					if(canAssignColorManually(thingsToEdit[i])){
						((IColored)thingsToEdit[i]).setColor(newColor);
					}
				}
			}
		}
	}

	class SetColorsMenuItemSet implements ActionListener{
		protected String text = null;
		protected Thing[] thingsToEdit;
		protected Color defaultColor = null;
		protected JMenuItem miAssignColor;
				
		public SetColorsMenuItemSet(Thing[] thingsToEdit, String text, Color defaultColor){
			this.thingsToEdit = thingsToEdit;
			this.text = text;
			this.defaultColor = defaultColor;
			
			boolean shouldEnable = false;
			for(int i = 0; i < thingsToEdit.length; i++){
				if(thingsToEdit[i] instanceof IColored){
					Color c = ((IColored)thingsToEdit[i]).getColor();
					if((c != null) && (!c.equals(defaultColor))){
						shouldEnable = true;
					}
				}
			}

			miAssignColor = new JMenuItem(text);
			miAssignColor.addActionListener(this);
			if(thingsToEdit.length == 0){
				miAssignColor.setEnabled(false);
			}
			else{
				miAssignColor.setIcon(WidgetUtils.getColorIcon(defaultColor, Color.BLACK, 16, 16));
			}
			miAssignColor.setEnabled(shouldEnable);

			if(!canAssignColorManually(thingsToEdit)){
				miAssignColor.setEnabled(false);
			}
		}
		
		public JMenuItem[] getMenuItemSet(){
			return new JMenuItem[]{miAssignColor};
		}
		
		public void actionPerformed(ActionEvent evt){
			for(int i = 0; i < thingsToEdit.length; i++){
				if(thingsToEdit[i] instanceof IColored){
					if(canAssignColorManually(thingsToEdit[i])){
						((IColored)thingsToEdit[i]).setColor(defaultColor);
					}
				}
			}
		}
	}

	class CopyColorMenuItemSet implements ActionListener, ClipboardOwner{
		protected Thing thingToEdit;
		protected JMenuItem miCopyColor = null;
		protected Color color = null;
		
		public CopyColorMenuItemSet(Thing thingToEdit){
			this.thingToEdit = thingToEdit;
			
			if(thingToEdit instanceof IColored){
				color = ((IColored)thingToEdit).getColor();
				if(color != null){
					miCopyColor = new JMenuItem("Copy Color");
					miCopyColor.addActionListener(this);
					miCopyColor.setIcon(WidgetUtils.getColorIcon(color, Color.BLACK, 16, 16));
				}
			}
		}
		
		public JMenuItem[] getMenuItemSet(){
			if(miCopyColor != null){
				return new JMenuItem[]{miCopyColor};
			}
			else{
				return new JMenuItem[0];
			}
		}
		
		public void actionPerformed(ActionEvent evt){
			if(color == null){
				return;
			}
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(new ColorTransferable(color), this);
		}
		
		public void lostOwnership(Clipboard clipboard, Transferable contents){
		}
	}

	class InheritTypeColorsMenuItemSet implements ActionListener{
		protected Thing[] thingsToEdit;

		protected JMenuItem miDoInheritColor = null;
		protected JMenuItem miDontInheritColor = null;
				
		public InheritTypeColorsMenuItemSet(Thing[] thingsToEdit){
			this.thingsToEdit = thingsToEdit;

			boolean someInheriting = false;
			boolean someNotInheriting = false;
			for(int i = 0; i < thingsToEdit.length; i++){
				if(thingsToEdit[i] instanceof BrickThing){
					BrickThing bt = (BrickThing)thingsToEdit[i];
					if(bt.getInheritColorFromType()){
						someInheriting = true;
					}
					else{
						someNotInheriting = true;
					}
				}
			}
			//Okay, now figure out what menu items to display
			if((!someInheriting) && (!someNotInheriting)){
				//This means there weren't any bricks!  Do nothing,
				//add nothing.
			}
			else if((!someNotInheriting) && (someInheriting)){
				//This means that all the bricks are currently inheriting
				//colors from their type.  Put a checkbox to turn them
				//all off.
				miDontInheritColor = new JMenuItem("Don't Inherit Color From Type");
				miDontInheritColor.addActionListener(this);
			}
			else if((someNotInheriting) && (!someInheriting)){
				//This means that none of the bricks are currently inheriting
				//colors from their type.  Put a checkbox to turn them
				//all on.
				miDoInheritColor = new JMenuItem("Inherit Color From Type");
				miDoInheritColor.addActionListener(this);
			}
			else{
				//This means some of the bricks are inheriting colors from
				//their types and some aren't.  Put two choices up.
				miDoInheritColor = new JMenuItem("Inherit Color From Type");
				miDoInheritColor.addActionListener(this);
				miDontInheritColor = new JMenuItem("Don't Inherit Color From Type");
				miDontInheritColor.addActionListener(this);
			}
		}
		
		public JMenuItem[] getMenuItemSet(){
			List miList = new ArrayList(3);
			if(miDoInheritColor != null) miList.add(miDoInheritColor);
			if(miDontInheritColor != null) miList.add(miDontInheritColor);
			return (JMenuItem[])miList.toArray(new JMenuItem[0]);
		}
		
		public void actionPerformed(ActionEvent evt){
			boolean doInheritColor;
			if(evt.getSource() == miDoInheritColor){
				doInheritColor = true;
			}
			else{
				doInheritColor = false;
			}
			
			BrickMappingLogic bml = null;
			MappingLogic[] mappingLogics = ArchStructureTreePlugin.getAllMappingLogics(c);
			for(int i = 0; i < mappingLogics.length; i++){
				if(mappingLogics[i] instanceof BrickMappingLogic){
					bml = (BrickMappingLogic)mappingLogics[i];
					break;
				}
			}
			
			for(int i = 0; i < thingsToEdit.length; i++){
				if(thingsToEdit[i] instanceof BrickThing){
					BrickThing bt = (BrickThing)thingsToEdit[i];
					boolean oldInherit = bt.getInheritColorFromType();
					if(oldInherit != doInheritColor){
						bt.setInheritColorFromType(doInheritColor);
						if(bml != null){
							ObjRef brickRef = thingIDMap.getXArchRef(bt.getID());
							if(brickRef != null){
								bml.updateInheritColorFromType(brickRef, bt);
							}
						}
					}
				}
			}
		}
	}
}

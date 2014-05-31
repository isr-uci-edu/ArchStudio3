package archstudio.comp.archipelago.types;

import archstudio.comp.archipelago.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.contextmenu.*;
import edu.uci.ics.bna.logic.TagThingTrackingLogic;
import edu.uci.ics.bna.logic.TaggingLogic;
import edu.uci.ics.bna.logic.MouseTrackingLogic;
import edu.uci.ics.bna.swingthing.*;
import edu.uci.ics.widgets.WidgetUtils;
import edu.uci.ics.widgets.windowheader.*;
import edu.uci.ics.xadlutils.Resources;

public class EndpointTagContextMenuPlugin extends AbstractSelectionBasedContextMenuPlugin{
	protected TaggingLogic tl;
	protected TagThingTrackingLogic ttl;
	protected MouseTrackingLogic mtl;
	
	public EndpointTagContextMenuPlugin(BNAComponent c, TaggingLogic tl, TagThingTrackingLogic ttl, MouseTrackingLogic mtl){
		super(c);
		this.tl = tl;
		this.mtl = mtl;
		this.ttl = ttl;
	}

	public JPopupMenu addToContextMenu(JPopupMenu currentContextMenu, Thing[] selectedThingSet, Thing thingUnderCursor){
		java.util.List thingsWeCareAbout = new ArrayList();
		
		if(selectedThingSet.length == 0){
			if(thingUnderCursor != null){
				if(thingUnderCursor instanceof BrickThing){
					thingsWeCareAbout.add(thingUnderCursor);
				}
				else if(thingUnderCursor instanceof BrickTypeThing){
					thingsWeCareAbout.add(thingUnderCursor);
				}
				else if(thingUnderCursor instanceof InterfaceThing){
					thingsWeCareAbout.add(thingUnderCursor);
				}
				else if(thingUnderCursor instanceof SignatureThing){
					thingsWeCareAbout.add(thingUnderCursor);
				}
			}
		}
		else{
			for(int i = 0; i < selectedThingSet.length; i++){
				if(selectedThingSet[i] instanceof BrickThing){
					thingsWeCareAbout.add(selectedThingSet[i]);
				}
				else if(selectedThingSet[i] instanceof BrickTypeThing){
					thingsWeCareAbout.add(selectedThingSet[i]);
				}
			}
		}
		if(thingsWeCareAbout.size() == 0){
			return currentContextMenu;
		}
		
		Thing[] ts = (Thing[])thingsWeCareAbout.toArray(new Thing[0]);
		
		EndpointTagMenuItemSet mis = new EndpointTagMenuItemSet(ts);
		JMenuItem[] miArray = mis.getMenuItemSet();
		for(int i = 0; i < miArray.length; i++){
			currentContextMenu.add(miArray[i]);
		}
		
		return currentContextMenu;
	}
	
	class EndpointTagMenuItemSet implements ActionListener{
		protected JMenu mEndpoints = null;
		protected JMenuItem miShowTag = null;
		protected JMenuItem miHideTag = null;
		protected JMenuItem miShowAllTags = null;
		protected JMenuItem miHideAllTags = null;
		protected JMenuItem miRotateAllTags = null;
		protected JMenuItem miArrangeAllTagsRotateNS = null;
		protected JMenuItem miArrangeAllTagsStackNS = null;
		protected Thing[] ts;
				
		public EndpointTagMenuItemSet(Thing[] ts){
			this.ts = ts;
			
			if((ts.length == 1) && ((ts[0] instanceof InterfaceThing) || (ts[0] instanceof SignatureThing))){
				TagThing tag = tl.findTagForThing(ts[0]);
				if(tag == null){
					miShowTag = new JMenuItem("Show Tag");
					miShowTag.addActionListener(this);
				}
				else{
					miHideTag = new JMenuItem("Hide Tag");
					miHideTag.addActionListener(this);
				}
			}
			else{
				boolean hasBricks = false;
				boolean hasBrickTypes = false;
				for(int i = 0; i < ts.length; i++){
					if(ts[i] instanceof BrickThing){
						hasBricks = true;
					}
					else if(ts[i] instanceof BrickTypeThing){
						hasBrickTypes = true;
					}
				}
				
				String menuName = null;
				if(hasBricks && hasBrickTypes){
					menuName = "Interfaces/Signature Tags";
				}
				else if((!hasBricks) && hasBrickTypes){
					menuName = "Signature Tags";
				}
				else if(hasBricks && (!hasBrickTypes)){
					menuName = "Interface Tags";
				}
				else{
					throw new IllegalArgumentException("This shouldn't happen.");
				}
				mEndpoints = new JMenu(menuName);
				mEndpoints.setIcon(Resources.INTERFACE_ICON);
				
				miShowAllTags = new JMenuItem("Show All Tags");
				miShowAllTags.addActionListener(this);
				
				miHideAllTags = new JMenuItem("Hide All Tags");
				miHideAllTags.addActionListener(this);
				
				miRotateAllTags = new JMenuItem("Rotate Visible Tags");
				miRotateAllTags.setIcon(WidgetUtils.getImageIcon("edu/uci/ics/bna/res/rotate.gif"));
				miRotateAllTags.addActionListener(this);
				
				miArrangeAllTagsRotateNS = new JMenuItem("Arrange Tags - Rotate N/S");
				miArrangeAllTagsRotateNS.addActionListener(this);
				
				miArrangeAllTagsStackNS = new JMenuItem("Arrange Tags - Stack N/S");
				miArrangeAllTagsStackNS.addActionListener(this);

				mEndpoints.add(miShowAllTags);
				mEndpoints.add(miHideAllTags);
				mEndpoints.add(miRotateAllTags);
				mEndpoints.add(miArrangeAllTagsRotateNS);
				mEndpoints.add(miArrangeAllTagsStackNS);
			}
			
		}
		
		public JMenuItem[] getMenuItemSet(){
			if(mEndpoints != null){
				return new JMenuItem[]{mEndpoints};
			}
			else if(miShowTag != null){
				return new JMenuItem[]{miShowTag};
			}
			else if(miHideTag != null){
				return new JMenuItem[]{miHideTag};
			}
			else{
				return new JMenuItem[0];
			}
		}
		
		int northStackIndex = 0;
		int southStackIndex = 0;
		
		private void arrangeTag(EndpointThing et, TagThing tt, boolean rotateNS){
			BNAModel m = getBNAComponent().getModel();
			
			int orientation = et.getOrientation();
			Rectangle etbb = et.getBoundingBox();
			int cwx = etbb.x + (etbb.width / 2);
			int cwy = etbb.y + (etbb.height / 2);
			
			switch(orientation){
			case EndpointThing.ORIENTATION_N:
				if(rotateNS){
					tt.setRotationAngle(315);
					tt.setAnchorPoint(new Point(cwx + 5, cwy - 10));
				}
				else{
					tt.setRotationAngle(0);
					int yoffset = 15 * northStackIndex;
					northStackIndex++;
					tt.setAnchorPoint(new Point(cwx + 5, cwy - 10 - yoffset));
				}
				break;
			case EndpointThing.ORIENTATION_E:
				tt.setRotationAngle(0);
				tt.setAnchorPoint(new Point(cwx + 10, cwy));
				break;
			case EndpointThing.ORIENTATION_S:
				{
					if(rotateNS){
						tt.setRotationAngle(315);
					}
					else{
						tt.setRotationAngle(0);
					}
					//Force the peer to update the bounding box for the Tag
					BNAUtils.fakeDraw(c, tt);
					Rectangle ttbb1 = tt.getBoundingBox();
					if(rotateNS){
						tt.setAnchorPoint(new Point(cwx - ttbb1.width, cwy + 10 + ttbb1.height));
					}
					else{
						int yoffset = 15 * southStackIndex;
						southStackIndex++;
						tt.setAnchorPoint(new Point(cwx - ttbb1.width, cwy + 10 + ttbb1.height + yoffset));
					}
				}
				break;
			case EndpointThing.ORIENTATION_W:
				{
					tt.setRotationAngle(0);
					//Force the peer to update the bounding box for the Tag
					BNAUtils.fakeDraw(c, tt);
					Rectangle ttbb2 = tt.getBoundingBox();
					tt.setAnchorPoint(new Point(cwx - 10 - ttbb2.width, cwy));
				}
				break;
			}
		}
		
		public void addTag(Thing t, String tagText){
			TagThing existingTag = tl.findTagForThing(t);
			if(existingTag != null) return;
			
			EndpointTagThing ett = (EndpointTagThing)EndpointTaggingLogic.createTag(t, tagText);
			
			BNAComponent c = getBNAComponent();
			if(c == null) return;
			BNAModel m = c.getModel();
			if(m == null) return;
			m.addThing(ett, t);
		}
		
		public void removeTag(Thing t){
			TagThing tt = tl.findTagForThing(t);
			if(tt != null){
				BNAComponent c = getBNAComponent();
				if(c == null) return;
				BNAModel m = c.getModel();
				if(m == null) return;
				m.removeThing(tt);
			}
		}
		
		public void actionPerformed(ActionEvent evt){
			BNAComponent c = getBNAComponent();
			if(c == null) return;
			BNAModel m = c.getModel();
			if(m == null) return;
			
			if((evt.getSource() == miShowTag) || (evt.getSource() == miHideTag)){
				boolean showTags = evt.getSource() == miShowTag;
				if(showTags){
					addTag(ts[0], ((EndpointThing)ts[0]).getToolTipText());
				}
				else{
					removeTag(ts[0]);
				}
			}
			else if((evt.getSource() == miRotateAllTags)){
				RotaterThing rt = new RotaterThing();
				if(ts.length == 1){
					Rectangle bb = ((IBoxBounded)ts[0]).getBoundingBox();
					rt.setAnchorPoint(new Point(bb.x + (bb.width / 2), bb.y + (bb.height /2)));
				}
				else{
					rt.setAnchorPoint(new Point(mtl.getLastWorldX(), mtl.getLastWorldY()));
				}

				Thing[] allThings = m.getAllThings();
				for(int i = 0; i < allThings.length; i++){
					if(allThings[i] instanceof EndpointThing){
						String targetThingId = ((EndpointThing)allThings[i]).getTargetThingID();
						if(targetThingId != null){
							for(int j = 0; j < ts.length; j++){
								if(ts[j] instanceof BrickThing){
									if(targetThingId.equals(ts[j].getID())){
										EndpointThing ep = (EndpointThing)allThings[i];
										TagThing t = tl.findTagForThing(ep);
										if(t != null){
											t.setRotationAngle(0);
											rt.addRotatedThingId(t.getID());
										}
									}
								}
								if(ts[j] instanceof BrickTypeThing){
									if(targetThingId.equals(ts[j].getID())){
										EndpointThing ep = (EndpointThing)allThings[i];
										TagThing t = tl.findTagForThing(ep);
										if(t != null){
											t.setRotationAngle(0);
											rt.addRotatedThingId(t.getID());
										}
									}
								}
							}
						}
					}
				}
				
				rt.setRotationAngle(0);
				m.addThing(rt);
			}
			else if((evt.getSource() == miArrangeAllTagsRotateNS) || (evt.getSource() == miArrangeAllTagsStackNS)){
				Thing[] allTagThings = ttl.getTrackedThings();
				for(int j = 0; j < ts.length; j++){
					if((ts[j] instanceof BrickThing) || (ts[j] instanceof BrickTypeThing)){
						northStackIndex = 0;
						southStackIndex = 0;
						for(int i = 0; i < allTagThings.length; i++){
							TagThing tt = (TagThing)allTagThings[i];
							String taggedThingID = tt.getTaggedThingId();
							if(taggedThingID != null){
								Thing taggedThing = m.getThing(taggedThingID);
								if((taggedThing != null) && (taggedThing instanceof EndpointThing)){
									EndpointThing ep = (EndpointThing)taggedThing;
									String endpointTargetThingID = ep.getTargetThingID();
									if(endpointTargetThingID != null){
										Thing endpointTargetThing = m.getThing(endpointTargetThingID);
										if((endpointTargetThing != null) && (endpointTargetThing == ts[j])){
											//The tag we found points at an endpoint that points at our box.
											arrangeTag(ep, tt, evt.getSource() == miArrangeAllTagsRotateNS);
										}
									}
								}
							}
						}
					}
				}
				/*
				Thing[] allThings = m.getAllThings();
				for(int i = 0; i < allThings.length; i++){
					if(allThings[i] instanceof EndpointThing){
						String targetThingId = ((EndpointThing)allThings[i]).getTargetThingID();
						if(targetThingId != null){
							for(int j = 0; j < ts.length; j++){
								if(ts[j] instanceof BrickThing){
									northStackIndex = 0;
									southStackIndex = 0;
									if(targetThingId.equals(ts[j].getID())){
										EndpointThing ep = (EndpointThing)allThings[i];
										TagThing t = tl.findTagForThing(ep);
										if(t != null){
											arrangeTag(ep, t, evt.getSource() == miArrangeAllTagsRotateNS);
										}
									}
								}
								if(ts[j] instanceof BrickTypeThing){
									northStackIndex = 0;
									southStackIndex = 0;
									if(targetThingId.equals(ts[j].getID())){
										EndpointThing ep = (EndpointThing)allThings[i];
										TagThing t = tl.findTagForThing(ep);
										if(t != null){
											arrangeTag(ep, t, evt.getSource() == miArrangeAllTagsRotateNS);
										}
									}
								}
							}
						}
					}
				}
				*/
				c.repaint();
			}
			else if((evt.getSource() == miShowAllTags) || (evt.getSource() == miHideAllTags)){
				boolean showTags = evt.getSource() == miShowAllTags;
				
				Thing[] allThings = m.getAllThings();
				for(int i = 0; i < allThings.length; i++){
					if(allThings[i] instanceof EndpointThing){
						String targetThingId = ((EndpointThing)allThings[i]).getTargetThingID();
						if(targetThingId != null){
							for(int j = 0; j < ts.length; j++){
								if(ts[j] instanceof BrickThing){
									if(targetThingId.equals(ts[j].getID())){
										EndpointThing ept = (EndpointThing)allThings[i];
										if(showTags){
											addTag(ept, ept.getToolTipText());
										}
										else{
											removeTag(ept);
										}
									}
								}
								if(ts[j] instanceof BrickTypeThing){
									if(targetThingId.equals(ts[j].getID())){
										EndpointThing ept = (EndpointThing)allThings[i];
										if(showTags){
											addTag(ept, ept.getToolTipText());
										}
										else{
											removeTag(ept);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

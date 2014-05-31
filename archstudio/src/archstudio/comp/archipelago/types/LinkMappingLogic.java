package archstudio.comp.archipelago.types;

import edu.uci.ics.bna.*;
import edu.uci.ics.bna.logic.MouseTrackingLogic;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.*;

import archstudio.comp.archipelago.*;
import archstudio.comp.xarchtrans.*;

import java.awt.*;
import java.util.*;

public class LinkMappingLogic extends AbstractMappingLogic{
	protected ThingIDMap thingIDMap;
	
	protected BNAModel structureBNAModel;
	
	protected ObjRef mainStructureRef;
	
	protected MouseTrackingLogic mtl = null;
	
	protected archstudio.comp.archipelago.RenderingHints renderingHints;
	
	protected NoThing linkParent;
	
	static int offset = 0;
	
	protected Vector linkMappingLogicListeners = new Vector();

	protected XArchBulkQuery xarchBulkQuery = null;
	protected XArchFlatQueryInterface xarchbulk = null;
	
	public LinkMappingLogic(ObjRef mainStructureRef, 
	BNAModel[] bnaModels, MouseTrackingLogic mtl,
	XArchFlatTransactionsInterface xarch, ThingIDMap thingIDMap, 
	archstudio.comp.archipelago.RenderingHints renderingHints){
		super(bnaModels, xarch);
		
		this.mainStructureRef = mainStructureRef;
		xarchBulkQuery = getBulkQuery(xarch.getXArch(mainStructureRef));
		this.structureBNAModel = bnaModels[0];
		this.mtl = mtl;
		this.thingIDMap = thingIDMap;
		this.renderingHints = renderingHints;
		
		linkParent = new NoThing("$$LinkParent");
		structureBNAModel.addThing(linkParent);
		
		runBulkQuery();
	}
	
	private void runBulkQuery(){
		//System.out.println("running bulk query");
		//new Exception().printStackTrace();
		XArchBulkQueryResults qr = xarch.bulkQuery(xarchBulkQuery);
		xarchbulk = new XArchBulkQueryResultProxy(xarch, qr);
	}
	
	public static XArchBulkQuery getBulkQuery(ObjRef xArchRef){
		XArchBulkQuery q = new XArchBulkQuery(xArchRef);
		q.addQueryPath("archStructure*/link*/id");
		q.addQueryPath("archStructure*/link*/description/value");
		q.addQueryPath("archStructure*/link*/point*/anchorOnInterface/type");
		q.addQueryPath("archStructure*/link*/point*/anchorOnInterface/href");

		q.addQueryPath("archStructure*/component*/id");
		q.addQueryPath("archStructure*/component*/description/value");
		q.addQueryPath("archStructure*/component*/interface*");
		q.addQueryPath("archStructure*/component*/interface*/id");
		q.addQueryPath("archStructure*/component*/interface*/description/value");
		q.addQueryPath("archStructure*/component*/interface*/direction/value");
		q.addQueryPath("archStructure*/component*/interface*/signature/type");
		q.addQueryPath("archStructure*/component*/interface*/signature/href");

		q.addQueryPath("archStructure*/connector*/id");
		q.addQueryPath("archStructure*/connector*/description/value");
		q.addQueryPath("archStructure*/connector*/interface*");
		q.addQueryPath("archStructure*/connector*/interface*/id");
		q.addQueryPath("archStructure*/connector*/interface*/description/value");
		q.addQueryPath("archStructure*/connector*/interface*/direction/value");
		q.addQueryPath("archStructure*/connector*/interface*/signature/type");
		q.addQueryPath("archStructure*/connector*/interface*/signature/href");

		return q;
	}


	public void addLinkMappingLogicListener(LinkMappingLogicListener l){
		linkMappingLogicListeners.addElement(l);
	}
	
	public void removeLinkMappingLogicListener(LinkMappingLogicListener l){
		linkMappingLogicListeners.removeElement(l);
	}
	
	protected void fireLinkUpdating(ObjRef linkRef, LinkThing lt){
		synchronized(linkMappingLogicListeners){
			for(Iterator it = linkMappingLogicListeners.iterator(); it.hasNext(); ){
				((LinkMappingLogicListener)it.next()).linkUpdating(linkRef, lt);
			}
		}
	}
	
	protected void fireLinkUpdated(ObjRef linkRef, LinkThing lt){
		synchronized(linkMappingLogicListeners){
			for(Iterator it = linkMappingLogicListeners.iterator(); it.hasNext(); ){
				((LinkMappingLogicListener)it.next()).linkUpdated(linkRef, lt);
			}
		}
	}

	protected void fireLinkRemoving(ObjRef linkRef, LinkThing lt){
		synchronized(linkMappingLogicListeners){
			for(Iterator it = linkMappingLogicListeners.iterator(); it.hasNext(); ){
				((LinkMappingLogicListener)it.next()).linkRemoving(linkRef, lt);
			}
		}
	}
	
	protected void fireLinkRemoved(ObjRef linkRef, LinkThing lt){
		synchronized(linkMappingLogicListeners){
			for(Iterator it = linkMappingLogicListeners.iterator(); it.hasNext(); ){
				((LinkMappingLogicListener)it.next()).linkRemoved(linkRef, lt);
			}
		}
	}

	public BNAModel getBNAModel(){
		return structureBNAModel;
	}

	public synchronized void handleXArchFlatEvent(XArchFlatEvent evt){
		XArchPath sourcePath = evt.getSourcePath();
		String sourcePathString = null;
		if(sourcePath != null) sourcePathString = sourcePath.toTagsOnlyString();
		
		XArchPath targetPath = evt.getTargetPath();
		String targetPathString = null;
		if(targetPath != null) targetPathString = targetPath.toTagsOnlyString();
		
		if((evt.getEventType() == XArchFlatEvent.CLEAR_EVENT) || (evt.getEventType() == XArchFlatEvent.REMOVE_EVENT)){
			if((sourcePathString != null) && (sourcePathString.equals("xArch/archStructure"))){
				if((targetPathString != null) && (targetPathString.equals("link"))){
					runBulkQuery();
					removeLink((ObjRef)evt.getTarget());
				}
			}
		}

		if(targetPath != null){
			if(targetPathString.equals("xArch/archStructure/link")){
				runBulkQuery();
				updateLink((ObjRef)evt.getTarget());
				return;
			}
		}
		
		if((sourcePathString != null) && (sourcePathString.startsWith("xArch/archStructure/link"))){
			//It's a link event
			ObjRef src = evt.getSource();
			if(src != null){
				ObjRef[] ancestors = xarch.getAllAncestors(src);
				ObjRef linkRef = ancestors[ancestors.length - 3];
				//System.out.println(xarch.getType(connectorRef));
				runBulkQuery();
				updateLink(linkRef);
				return;
			}
		}
	}
	
	public void handleXArchFileEvent(XArchFileEvent evt){
	}
	
	public void removeLink(ObjRef linkRef){
		ObjRef structureRef = xarch.getParent(linkRef);
		if(structureRef != null){
			if(!structureRef.equals(mainStructureRef)){
				return;
			}
		}
		
		String existingThingID = thingIDMap.getThingID(linkRef);
		if(existingThingID == null){
			//It's already gone. (?)
			return;
		}
		
		LinkThing lt = (LinkThing)structureBNAModel.getThing(existingThingID);
		fireLinkRemoving(linkRef, lt);
		structureBNAModel.removeThing(existingThingID);
		fireLinkRemoved(linkRef, lt);
	}
	
	protected void updateLink(ObjRef linkRef){
		try{
			structureBNAModel.beginBulkChange();
			structureBNAModel.fireStreamNotificationEvent("LinkMappingLogic:updatingLink|" + linkRef.toString());
			ObjRef structureRef = xarchbulk.getParent(linkRef);
			if(structureRef != null){
				if(!structureRef.equals(mainStructureRef)){
					return;
				}
			}

			//System.out.println("updateLink called.");
			LinkThing lt;
		
			String existingThingID = thingIDMap.getThingID(linkRef);
			if(existingThingID != null){
				try{
					lt = (LinkThing)structureBNAModel.getThing(existingThingID);
				}
				catch(ClassCastException cce){
					System.err.println("Warning; ID/type mismatch.");
					cce.printStackTrace();
					return;
				}
			}
			else{
				lt = new LinkThing();
			}
			
			fireLinkUpdating(linkRef, lt);
		
			String ltID = edu.uci.ics.xadlutils.XadlUtils.getID(xarchbulk, linkRef);
			lt.setXArchID(ltID);
			thingIDMap.mapRefToID(linkRef, lt.getID());
		
			String label = "(No Description)";
			String ltDesc = edu.uci.ics.xadlutils.XadlUtils.getDescription(xarchbulk, linkRef);
			if(ltDesc != null) label = ltDesc;
			lt.setToolTipText(label);
		
			if(existingThingID == null){
				//It hasn't been placed yet...
			
				//Set a default color
				lt.setColor(Color.BLACK);
			
				renderingHints.applyRenderingHints(ltID, lt);
			
				structureBNAModel.addThing(lt, linkParent);
			}
		
			//Let's make sure our link has at least two points so it can be displayed,
			//even if one of them is floating off in space.
			int numPoints = lt.getNumPoints();
			if(numPoints == 0){
				if(mtl.getLastWorldX() == -1){
					lt.addPoint(new Point((DefaultCoordinateMapper.DEFAULT_WORLD_WIDTH / 2) + offset, 
					(DefaultCoordinateMapper.DEFAULT_WORLD_HEIGHT / 2) + offset));
					lt.addPoint(new Point((DefaultCoordinateMapper.DEFAULT_WORLD_WIDTH / 2) + offset + 75, 
					(DefaultCoordinateMapper.DEFAULT_WORLD_HEIGHT / 2) + offset + 50));
					offset += 10;
				}
				else{
					lt.addPoint(new Point(mtl.getLastWorldX() - 25, 
						mtl.getLastWorldY() - 25));
					lt.addPoint(new Point(mtl.getLastWorldX() + 25, 
						mtl.getLastWorldY() + 25));
				}
			}
			else if(numPoints == 1){
				if(mtl.getLastWorldX() == -1){
					lt.addPoint(new Point((DefaultCoordinateMapper.DEFAULT_WORLD_WIDTH / 2) + offset + 75, 
					(DefaultCoordinateMapper.DEFAULT_WORLD_HEIGHT / 2) + offset + 50));
					offset += 10;
				}
				else{
					lt.addPoint(new Point(mtl.getLastWorldX() + 25, 
						mtl.getLastWorldY() + 25));
				}
			}
		
			XadlUtils.LinkInfo li = XadlUtils.getLinkInfo(xarchbulk, linkRef, true);
		
			if(li.getPoint1Ref() != null){
				ObjRef interfaceTarget = li.getPoint1Target();
				if(interfaceTarget != null){
					String thingID = thingIDMap.getThingID(interfaceTarget);
					if(thingID != null){
						Thing interfaceThing = structureBNAModel.getThing(thingID);
						if(interfaceThing instanceof InterfaceThing){
							Rectangle stickyBox = ((InterfaceThing)interfaceThing).getStickyBox();
							if(lt.getNumPoints() == 0){
								lt.addPoint(new Point(stickyBox.x, stickyBox.y));
							}
							else{
								lt.setPointAt(new Point(stickyBox.x, stickyBox.y), 0);
							}
							lt.setFirstEndpointStuckToID(thingID);
						}
					}
				}
			}
			if(li.getPoint2Ref() != null){
				ObjRef interfaceTarget = li.getPoint2Target();
				if(interfaceTarget != null){
					if(interfaceTarget != null){
						String thingID = thingIDMap.getThingID(interfaceTarget);
						if(thingID != null){
							Thing interfaceThing = structureBNAModel.getThing(thingID);
							if(interfaceThing instanceof InterfaceThing){
								Rectangle stickyBox = ((InterfaceThing)interfaceThing).getStickyBox();
								if(lt.getNumPoints() == 1){
									lt.addPoint(new Point(stickyBox.x, stickyBox.y));
								}
								else{
									lt.setPointAt(new Point(stickyBox.x, stickyBox.y), lt.getNumPoints() - 1);
								}
								lt.setSecondEndpointStuckToID(thingID);
							}
						}
					}
				}
			}
			fireLinkUpdated(linkRef, lt);
		}
		finally{
			structureBNAModel.fireStreamNotificationEvent("LinkMappingLogic:doneUpdatingLink|" + linkRef.toString());
			structureBNAModel.endBulkChange();
		}
	}
	
	private boolean syncingLinks = false;
	private Set linkUpdateNotifications = Collections.synchronizedSet(new HashSet());
	
	public synchronized void bnaModelChanged(BNAModelEvent evt){
		if(evt.getEventType() == BNAModelEvent.STREAM_NOTIFICATION_EVENT){
			String notification = evt.getStreamNotification();
			if(notification != null){
				/*
				 * The syncingLinks notifications are slipped into the stream when we do
				 * a full link sync.  When we sync links, we ignore the ThingEvents
				 * that will be generated when we attach the links initially to their
				 * endpoints as directed by the architecture model, so we don't
				 * do an extra roundtrip to xArchADT to figure out we're not gonna
				 * do anything anyway.
				 * 
				 * The updatingLink notifications are clipped into the stream when
				 * we are updating a link.  This prevents an infinite loop when
				 * updating a link sets the stuck-to endpoints or some other
				 * property - updateLink should not cause anything to change
				 * in the xArch model, even indirectly through a BNA model
				 * change.
				 */
				
				if(notification.equals("startSyncingLinks")){
					syncingLinks = true;
					return;
				}
				else if(notification.equals("endSyncingLinks")){
					syncingLinks = false;
					return;
				}
				else if(notification.startsWith("LinkMappingLogic:updatingLink|")){
					linkUpdateNotifications.add(notification.substring(notification.indexOf("|") + 1));
					return;
				}
				else if(notification.startsWith("LinkMappingLogic:doneUpdatingLink|")){
					linkUpdateNotifications.remove(notification.substring(notification.indexOf("|") + 1));
					return;
				}
			}
		}
		Thing t = evt.getTargetThing();
		if((t != null) && (t instanceof LinkThing)){
			if(syncingLinks) return;
			
			/* This code block ensures that we do not handle any BNA
			 * model events while we are updating the link.  updateLink()
			 * will ensure consistency between the BNA model and the
			 * xArch model. */
			ObjRef ltObjRef = thingIDMap.getXArchRef(((LinkThing)t).getID());
			if(ltObjRef != null){
				if(linkUpdateNotifications.contains(ltObjRef.toString())) return;
			}
			
			/*
			if(evt.getThingEvent() != null){
				System.out.println(evt.getThingEvent().getPropertyName());
				System.out.println(evt.getThingEvent().getOldPropertyValue());
				System.out.println(evt.getThingEvent().getNewPropertyValue());
			}
			*/
			
			LinkThing lt = (LinkThing)t;
			ThingEvent thingEvent = evt.getThingEvent();
			if(thingEvent != null){
				String propertyName = thingEvent.getPropertyName();
				if(propertyName != null){
					if(propertyName.equals(LinkThing.FIRST_ENDPOINT_STUCK_TO_ID_PROPERTY_NAME)){
						String oldValue = (String)thingEvent.getOldPropertyValue();
						String newValue = (String)thingEvent.getNewPropertyValue();
						if(oldValue == newValue){
							return;
						}
						else{
							if((oldValue != null) && (newValue != null)){
								if(oldValue.equals(newValue)){
									return;
								}
							}
						}
						//runBulkQuery();
						ObjRef linkRef = thingIDMap.getXArchRef(lt.getID());
						if(linkRef != null){
							XadlUtils.LinkInfo li = XadlUtils.getLinkInfo(xarch/*bulk*/, linkRef, false);
							
							//Two cases: either it was stuck to something or it was unstuck
							//from something
							String stuckToID = (String)thingEvent.getNewPropertyValue();
							if(stuckToID == null){
								String href = li.getAnchor1Href();
								if((href == null) || (href.equals(""))){
									//prevent infinite loop
									//System.out.println("stopping the madness1");
									return;
								}

								//Unhook the link
								ObjRef anchorRef = li.getAnchor1Ref();
								if(anchorRef != null){
									xarch.clear(anchorRef, "type");
									xarch.clear(anchorRef, "href");
								}
							}
							else{
								//Hook up the link
								ObjRef stuckToRef = thingIDMap.getXArchRef(stuckToID);
								if(stuckToRef != null){
									String stuckToRefID = XadlUtils.getID(xarch/*bulk*/, stuckToRef);
									if(stuckToRefID != null){
										String href = li.getAnchor1Href();
										if((href != null) && (href.endsWith("#" + stuckToRefID))){
											//prevent infinite loop
											//System.out.println("stopping the madness2");
											return;
										}
										ObjRef pointRef = li.getPoint1Ref();
										ObjRef anchorRef = li.getAnchor1Ref();
										ObjRef typesContextRef = null;
										if((pointRef == null) || (anchorRef == null)){
											ObjRef xArchRef = xarch.getXArch(linkRef);										
											typesContextRef = xarch.createContext(xArchRef, "types");
										}
										if(pointRef == null){
											//Add the point + anchor
											pointRef = xarch.create(typesContextRef, "point");
											xarch.add(linkRef, "point", pointRef);
										}
										if(anchorRef == null){
											//Add the anchor
											anchorRef = xarch.create(typesContextRef, "XMLLink");
											xarch.set(pointRef, "anchorOnInterface", anchorRef);
										}
										xarch.set(anchorRef, "type", "simple");
										xarch.set(anchorRef, "href", "#" + stuckToRefID);
									}
								}
							}
						}
					}
					else if(propertyName.equals(LinkThing.SECOND_ENDPOINT_STUCK_TO_ID_PROPERTY_NAME)){
						String oldValue = (String)thingEvent.getOldPropertyValue();
						String newValue = (String)thingEvent.getNewPropertyValue();
						if(oldValue == newValue){
							return;
						}
						else{
							if((oldValue != null) && (newValue != null)){
								if(oldValue.equals(newValue)){
									return;
								}
							}
						}

						//runBulkQuery();
						ObjRef linkRef = thingIDMap.getXArchRef(lt.getID());
						if(linkRef != null){
							XadlUtils.LinkInfo li = XadlUtils.getLinkInfo(xarch/*bulk*/, linkRef, false);
							
							//Two cases: either it was stuck to something or it was unstuck
							//from something
							String stuckToID = (String)thingEvent.getNewPropertyValue();
							if(stuckToID == null){
								
								String href = li.getAnchor2Href();
								if((href == null) || (href.equals(""))){
									//prevent infinite loop
									//System.out.println("stopping the madness3");
									return;
								}
								
								//Unhook the link
								ObjRef anchorRef = li.getAnchor2Ref();
								if(anchorRef != null){
									xarch.clear(anchorRef, "type");
									xarch.clear(anchorRef, "href");
								}
							}
							else{
								//Hook up the link
								ObjRef stuckToRef = thingIDMap.getXArchRef(stuckToID);
								if(stuckToRef != null){
									String stuckToRefID = XadlUtils.getID(xarch/*bulk*/, stuckToRef);
									if(stuckToRefID != null){
										String href = li.getAnchor2Href();
										if((href != null) && (href.endsWith("#" + stuckToRefID))){
											//prevent infinite loop
											//System.out.println("stopping the madness4");
											return;
										}

										ObjRef pointRef = li.getPoint2Ref();
										ObjRef anchorRef = li.getAnchor2Ref();
										ObjRef typesContextRef = null;
										if((pointRef == null) || (anchorRef == null)){
											ObjRef xArchRef = xarch.getXArch(linkRef);										
											typesContextRef = xarch.createContext(xArchRef, "types");
										}
										if(pointRef == null){
											//Add the point + anchor
											pointRef = xarch.create(typesContextRef, "point");
											xarch.add(linkRef, "point", pointRef);
										}
										if(anchorRef == null){
											//Add the anchor
											anchorRef = xarch.create(typesContextRef, "XMLLink");
											xarch.set(pointRef, "anchorOnInterface", anchorRef);
										}
										xarch.set(anchorRef, "type", "simple");
										xarch.set(anchorRef, "href", "#" + stuckToRefID);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	
	public static LinkMappingLogic getLinkMappingLogic(BNAComponent bnaComponent){
		MappingLogic[] mls = AbstractArchipelagoTreePlugin.getAllMappingLogics(bnaComponent);
		for(int i = 0; i < mls.length; i++){
			if(mls[i] instanceof LinkMappingLogic){
				return (LinkMappingLogic)mls[i];
			}
		}
		return null;
	}
}

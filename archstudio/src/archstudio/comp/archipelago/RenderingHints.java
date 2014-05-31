package archstudio.comp.archipelago;

import java.util.*;

import archstudio.comp.archipelago.hints.*;

import edu.uci.ics.bna.*;
import edu.uci.ics.xadlutils.XadlUtils;
import edu.uci.ics.xarchutils.*;
import archstudio.comp.xarchtrans.*;
import java.io.*;

import c2.util.ArrayUtils;

public class RenderingHints{
	
	protected static final String UNIQUE_PREFIX = "@@@";
	
	//Maps xArch IDs to Things (not actually used in the BNA)
	//that contain rendering hint properties
	protected Map hintsMap = Collections.synchronizedMap(new HashMap());
	
	public void applyRenderingHints(String xArchID, Thing t){
		if(xArchID == null){
			return;
		}
		List hintList = (List)hintsMap.get(xArchID);
		if(hintList != null){
			for(Iterator it = hintList.iterator(); it.hasNext(); ){
				SingleHint sh = (SingleHint)it.next();
				t.setProperty(sh.getName(), sh.getValue());
			}
		}
	}
	
	protected static boolean isHintedPropertyName(String name, Thing t){
		if(t instanceof IHinted){
			IHinted ht = (IHinted)t;
			String[] rhpns = ht.getRenderingHintPropertyNames();
			for(int i = 0; i < rhpns.length; i++){
				if(name.equals(rhpns[i])) return true;
			}
		}
		return false;
	}
	
	protected static Thing findThingByXArchID(BNAModel m, String xArchID){
		Thing[] allThings = m.getAllThings();
		for(int i = 0; i < allThings.length; i++){
			if(allThings[i] instanceof IHasXArchID){
				if(xArchID.equals(((IHasXArchID)allThings[i]).getXArchID())){
					return allThings[i];
				}
			}
		}
		return null;
	}
	
	protected static boolean isRefedPropertyName(String name, Thing t){
		if(t instanceof IIndependentHinted){
			IIndependentHinted ht = (IIndependentHinted)t;
			String[] refpns = ht.getRefPropertyNames();
			for(int i = 0; i < refpns.length; i++){
				if(name.equals(refpns[i])) return true;
			}
		}
		return false;
	}
	
	public void applyIndependentHints(BNAModel m){
		synchronized(hintsMap){
			String[] xArchIDs = (String[])hintsMap.keySet().toArray(new String[0]);
			for(int i = 0; i < xArchIDs.length; i++){
				if((xArchIDs[i] != null) && (xArchIDs[i].startsWith(UNIQUE_PREFIX))){
					boolean thingMissingParent = false;
					boolean thingAlreadyExists = false;
					String thingToCreateClassName = xArchIDs[i].substring(UNIQUE_PREFIX.length(), xArchIDs[i].indexOf('~'));
					Thing parentThing = null;
					try{
						Class thingClass = Class.forName(thingToCreateClassName);
						Thing t = (Thing)thingClass.newInstance();

						//applyRenderingHints(xArchIDs[i], t);
						List hintList = (List)hintsMap.get(xArchIDs[i]);
						if(hintList != null){
							for(Iterator it = hintList.iterator(); it.hasNext(); ){
								SingleHint sh = (SingleHint)it.next();
								String propertyName = sh.getName();
								//System.out.println("propertyName = " + propertyName);
								if(propertyName.equals("id")){
									//This is the old thing's Thing ID.  If we already have
									//this Thing in the model, we don't need to create it
									//again.
									String oldThingID = (String)sh.getValue();
									if(m.getThing(oldThingID) != null){
										thingAlreadyExists = true;
										break;
									}
									else{
										//Give the new thing the old thing's ID.  That
										//way, if we apply the hints many times, we won't
										//create many new things that all represent
										//the same old Thing
										t.setID(oldThingID);
									}
								}
								else if(propertyName.equals(UNIQUE_PREFIX + "parentRef")){
									String xArchID = (String)sh.getValue();
									if(xArchID != null){
										parentThing = findThingByXArchID(m, xArchID);
										if(parentThing == null){
											//System.out.println("thingMissingParent!!!");
											thingMissingParent = true;
										}
									}
								}
								else if(isRefedPropertyName(propertyName, t)){
									String xArchID = (String)sh.getValue();
									if(xArchID != null){
										Thing refedThing = findThingByXArchID(m, xArchID);
										if(refedThing != null){
											t.setProperty(propertyName, refedThing.getID());
										}
									}
								}
								else{
									t.setProperty(propertyName, sh.getValue());
								}
							}
						}
						if(!thingAlreadyExists){
							//System.out.println("Adding thing! " + t.getID());
							if(parentThing == null){
								if(!thingMissingParent){
									m.addThing(t);
								}
							}
							else{
								m.addThing(t, parentThing);
							}
						}
					}
					catch(ClassNotFoundException cnfe){}
					catch(IllegalAccessException iae){}
					catch(InstantiationException ie){}
				}
			}
		}
	}

	private static void addHint(RenderingHints rh, SingleHint sh){
		String xArchID = sh.getXArchID();
		List hintsList = (List)rh.hintsMap.get(xArchID);
		if(hintsList == null){
			hintsList = new ArrayList();
		}
		hintsList.add(sh);
		rh.hintsMap.put(xArchID, hintsList);
	}
	
	public static RenderingHints readHints(XArchFlatTransactionsInterface xarch, ObjRef xArchRef, ArchipelagoStatusBar statusBar) throws HintDecodingException{
		RenderingHints rh = new RenderingHints();
		//ObjRef xArchRef = xarch.getOpenXArch(uri);
		if(xArchRef == null){
			return rh;
		}
		ObjRef hintsContextRef = xarch.createContext(xArchRef, "Hints");
		ObjRef renderingHintsRef = xarch.getElement(hintsContextRef, "RenderingHints", xArchRef);
		if(renderingHintsRef == null){
			return rh;
		}
		
		ObjRef[] hintsRefs = xarch.getAll(renderingHintsRef, "Hints");
		
		int tid = 0;
		if(statusBar != null){
			statusBar.setMinimum(0);
			statusBar.setMaximum(hintsRefs.length);
		}
		Hints h = new Hints();
		for(int i = 0; i < hintsRefs.length; i++){
			ObjRef[] propertyHintRefs = xarch.getAll(hintsRefs[i], "PropertyHint");
			for(int j = 0; j < propertyHintRefs.length; j++){
				String name = (String)xarch.get(propertyHintRefs[j], "Name");
				if(name.equals("allHints")){
					String encodedHints = (String)xarch.get(propertyHintRefs[j], "Value");
					SingleHint[] hints = h.decodeHints(encodedHints);
					for(int k = 0; k < hints.length; k++){
						addHint(rh, hints[k]);
					}
				}
			}
			if(statusBar != null){
				statusBar.setValue(i);
			}
		}
		return rh;
	}
	
	private static ObjRef getHintsFor(XArchFlatTransactionsInterface xarch, ObjRef xArchRef, ObjRef renderingHintsRef, ObjRef hintedThing){
		String hintedThingID = XadlUtils.getID(xarch, hintedThing);
		ObjRef[] allHints = xarch.getAll(renderingHintsRef, "hints");
		for(int i = 0; i < allHints.length; i++){
			ObjRef hintedThingLinkRef = (ObjRef)xarch.get(allHints[i], "hintedThing");
			if(hintedThingLinkRef == null){
				if(hintedThingID == null){
					return allHints[i];
				}
			}
			String hintedThingLinkHref = XadlUtils.getHref(xarch, hintedThingLinkRef);
			if(hintedThingLinkHref != null){
				ObjRef currentHintedThing = xarch.resolveHref(xArchRef, hintedThingLinkHref);
				if(currentHintedThing != null){
					if(currentHintedThing.equals(hintedThing)){
						return allHints[i]; 
					}
					String currentHintedThingID = XadlUtils.getID(xarch, currentHintedThing);
					if(currentHintedThingID != null){
						if(currentHintedThingID.equals(hintedThingID)){
							return allHints[i];
						}
					}
				}
			}
		}
		return null;
	}
	
	public static void writeHints(XArchFlatTransactionsInterface xarch, ObjRef xArchRef, ObjRef[] thingsToHint, BNAModel[] models, ArchipelagoStatusBar statusBar) throws HintEncodingException{
		if(thingsToHint.length != models.length){
			throw new IllegalArgumentException("Things to hint must be in sync with models.");
		}
		if(xArchRef == null){
			//There's no document open
			return;
		}
		
		Transaction t = xarch.createTransaction(xArchRef);
		ObjRef hintsContextRef = xarch.createContext(xArchRef, "Hints");

		ObjRef renderingHintsRef = (ObjRef)xarch.getElement(hintsContextRef, "RenderingHints", xArchRef);
		if(renderingHintsRef == null){
			renderingHintsRef = xarch.createElement(hintsContextRef, "RenderingHints");
		}

		if(statusBar != null){
			statusBar.setMinimum(0);
			int totalThings = 0;
			for(int i = 0; i < models.length; i++){
				totalThings += models[i].getNumThings();
			}
			statusBar.setMaximum(totalThings + 10);
			//statusBar.setStringPainted(true);
			statusBar.setText("Storing Hints");
		}

		for(int i = 0; i < thingsToHint.length; i++){
			try{
				ObjRef hintsRef = writeHints(xarch, t, hintsContextRef, xArchRef, thingsToHint[i], models[i], statusBar);
				//Remove the old hints for that ref
				ObjRef oldHintsRef = getHintsFor(xarch, xArchRef, renderingHintsRef, thingsToHint[i]);
				if(oldHintsRef != null){
					xarch.remove(t, renderingHintsRef, "Hints", oldHintsRef);
				}
				//Add the main header to the set of all hints
				xarch.add(t, renderingHintsRef, "Hints", hintsRef);
			}
			catch(HintEncodingException hee){
				xarch.rollback(t);
				throw hee;
			}
		}

		statusBar.setText("Committing");
		statusBar.paint(statusBar.getGraphics());
		xarch.add(t, xArchRef, "Object", renderingHintsRef);
		xarch.commit(t);
		statusBar.setValue(statusBar.getMaximum());
	}
	
	private static int index = 0;
		
	private static ObjRef writeHints(XArchFlatTransactionsInterface xarch, Transaction t, ObjRef hintsContextRef, ObjRef xArchRef, ObjRef thingToHint, BNAModel model, ArchipelagoStatusBar statusBar) throws HintEncodingException{
		Thing[] things = model.getAllThings();

		List hintsList = new ArrayList();

		for(int i = 0; i < things.length; i++){
			if(things[i] instanceof EnvironmentPropertiesThing){
				//System.out.println("Writing EPT");
				String[] propertyNames = things[i].getAllPropertyNames();
				String xArchID = (String)things[i].getProperty(IHinted.XARCHID_PROPERTY_NAME);
				if(xArchID != null){
					//System.out.println("Still Writing EPT: " + xArchID);
					for(int j = 0; j < propertyNames.length; j++){
						String propertyName = propertyNames[j];
						if((propertyName != null) && (!propertyName.equals(IHinted.XARCHID_PROPERTY_NAME))){
							Object propertyValue = things[i].getProperty(propertyName);
							if(propertyValue != null){
								Class propertyValueClass = propertyValue.getClass();
							
								SingleHint sh = new SingleHint(xArchID, propertyName, propertyValueClass, propertyValue);
								hintsList.add(sh);
							}
						}
					}
				}
			}
			else if(things[i] instanceof IHinted){
				String xArchID = ((IHinted)things[i]).getXArchID();
				
				if(things[i] instanceof IIndependentHinted){
					xArchID = UNIQUE_PREFIX + things[i].getClass().getName() + "~" + index;
					index++;
				}
				if(xArchID == null){
					System.out.println("Warning: null xArchID: " + things[i]);
				}
				else{
					String[] rhpns = ((IHinted)things[i]).getRenderingHintPropertyNames();
					
					for(int j = 0; j < rhpns.length; j++){
						String propertyName = rhpns[j];
						if(!propertyName.endsWith("*")){
							Object propertyValue = things[i].getProperty(propertyName);
							if(propertyValue != null){
								Class propertyValueClass = propertyValue.getClass();
								
								SingleHint sh = new SingleHint(xArchID, propertyName, propertyValueClass, propertyValue);
								hintsList.add(sh);
							}
						}
						else{
							String prefix = propertyName.substring(0, propertyName.length() - 1);
							int index = 0;
							while(true){
								String pn = prefix + index;
								Object propertyValue = things[i].getProperty(pn);
								if(propertyValue != null){
									Class propertyValueClass = propertyValue.getClass();
									SingleHint sh = new SingleHint(xArchID, pn, propertyValueClass, propertyValue);
									hintsList.add(sh);
								}
								else{
									break;
								}
								index++;
							}
						}
					}
					if(things[i] instanceof IIndependentHinted){
						String id = things[i].getID();
						SingleHint shid = new SingleHint(xArchID, "id", java.lang.String.class, id);
						hintsList.add(shid);
						
						String parentrefpn = ((IIndependentHinted)things[i]).getParentRefPropertyName();
						if(parentrefpn != null){
							String parentID = (String)things[i].getProperty(parentrefpn);
							if(parentID != null){
								Thing parentThing = model.getThing(parentID);
								if((parentThing != null) && (parentThing instanceof IHasXArchID)){
									String parentXArchID = ((IHasXArchID)parentThing).getXArchID();
									SingleHint sh = new SingleHint(xArchID, UNIQUE_PREFIX + "parentRef", java.lang.String.class, parentXArchID);
									hintsList.add(sh);
								}
							}
						}
						String[] refpns = ((IIndependentHinted)things[i]).getRefPropertyNames();
						for(int r = 0; r < refpns.length; r++){
							String refedThingID = (String)things[i].getProperty(refpns[r]);
							if(refedThingID != null){
								Thing refedThing = model.getThing(refedThingID);
								if((refedThing != null) && (refedThing instanceof IHasXArchID)){
									String refedXArchID = ((IHasXArchID)refedThing).getXArchID();
									SingleHint sh = new SingleHint(xArchID, refpns[r], java.lang.String.class, refedXArchID);
									hintsList.add(sh);
								}
							}
						}
					}
				}
			}
			if(statusBar != null){
				statusBar.setValue(statusBar.getValue() + 1);
			}
		}
		
		SingleHint[] allHints = (SingleHint[])hintsList.toArray(new SingleHint[0]);
		
		//Encode all the hints for this BNAModel
		Hints h = new Hints();
		String encodedHints = h.encodeHints(allHints);
		
		//Create the main header for this set of hints
		ObjRef hintsRef = xarch.create(hintsContextRef, "Hints");
		
		//Link it to the hinted thing
		String hintedThingXArchID = XadlUtils.getID(xarch, thingToHint);
		if(hintedThingXArchID != null){
			ObjRef hintedThingLinkRef = xarch.create(hintsContextRef, "XMLLink");
			String href = "#" + hintedThingXArchID;
			xarch.set(t, hintedThingLinkRef, "type", "simple");
			xarch.set(t, hintedThingLinkRef, "href", href);
			xarch.set(t, hintsRef, "hintedThing", hintedThingLinkRef);
		}
		
		//Add the encoded hints
		ObjRef propertyHintRef = xarch.create(hintsContextRef, "PropertyHint");
		xarch.set(t, propertyHintRef, "name", "allHints");
		xarch.set(t, propertyHintRef, "value", encodedHints);
		
		//add the encoded hints propertyhint to the main hints
		xarch.add(t, hintsRef, "propertyHint", propertyHintRef);
		return hintsRef;
	}
}

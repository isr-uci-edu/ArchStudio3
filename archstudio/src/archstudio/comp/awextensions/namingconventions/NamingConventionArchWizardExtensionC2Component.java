// ****************************************************************************
// ** Class Name: NamingConventionArchWizardExtensionC2Component             **
// **                                                                        **
// ** Description: This is an ArchWizard extension that implements some of   **
// **              the naming conventions for xADL 2.0 documents: interface  **
// **              names for components and connectors, signature names      **
// **              for types, and link names.                                **
// **                                                                        **
// ** 2/26/2003 - John Georgas[jgeorgas@ics.uci.edu]                         **
// **             Initial development.                                       **
// ** 5/7/2003 - John Georgas[jgeorgas@ics.uci.edu]                          **
// **            Minor improvements to event detection.                      **
// **                                                                        **
// ** Copyright 2003, by the University of California, Irvine.               **
// ** ALL RIGHTS RESERVED.                                                   **
// ****************************************************************************

package archstudio.comp.awextensions.namingconventions;

// C2 imports
import c2.fw.*;
import c2.legacy.*;
import c2.pcwrap.*;

// xArch utilities imports
import edu.uci.isr.xarch.*;
import edu.uci.ics.xarchutils.*;
import archstudio.awextensions.*;

// Java imports
import java.util.*;

public class NamingConventionArchWizardExtensionC2Component extends AbstractArchWizardExtension {
	
	// Class constructor
	public NamingConventionArchWizardExtensionC2Component(Identifier id) {
		super(id);
	}
	
	protected String getDescription() {
		return "This is an ArchWizard extension that implements some of the naming conventions for xADL 2.0 " +
		       "documents: interface names for components and connectors, signature names for types, " +
		       "component and connector type suffixes, and link names.  It will be triggered when the id " +
		       "of a component or connector is updated, and when both points of a link are set.";
	}
	
	protected void handleStateChangeEvent(XArchFlatEvent evt) {
		if (isActive()) {
			if (evt.getIsAttached()) {
				if (evt.getEventType() == XArchFlatEvent.SET_EVENT) {
					if (evt.getSourceType() == XArchFlatEvent.ATTRIBUTE_CHANGED) {
						if ((evt.getTargetName()).equals("id")) {
							if (xarch.isInstanceOf(evt.getSource(), "edu.uci.isr.xarch.types.ComponentImpl") ||
							    xarch.isInstanceOf(evt.getSource(), "edu.uci.isr.xarch.types.ConnectorImpl")) {
								nameSubElements(evt.getSource(), "Interface", (String)evt.getTarget());
							} else if (xarch.isInstanceOf(evt.getSource(), "edu.uci.isr.xarch.types.ComponentTypeImpl") ||
							           xarch.isInstanceOf(evt.getSource(), "edu.uci.isr.xarch.types.ConnectorTypeImpl")) {
								nameSubElements(evt.getSource(), "Signature", (String)evt.getTarget());
								checkIncludeSuffix(evt.getSource(), (String)evt.getTarget());
							}	
						} else if ((evt.getTargetName()).equals("href")) {
							if ((xarch.getType(evt.getSource())).endsWith("XMLLinkImpl")) {
								ObjRef pointRef = xarch.getParent(evt.getSource());
								if (pointRef != null) {
									if ((xarch.getType(pointRef)).endsWith("PointImpl")) {
										ObjRef linkRef = xarch.getParent(pointRef);
										if (linkRef != null) {
											if ((xarch.getType(linkRef)).endsWith("LinkImpl")) {
												nameLink(linkRef);
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
	
	private void checkIncludeSuffix(ObjRef ref, String name) {
		if (!(name.endsWith("_type"))) {
			xarch.set(ref, "Id", name + "_type");
		}
	}
	
	private void nameSubElements(ObjRef ref, String what, String val) {
		if (val != null) {
			ObjRef[] elems = xarch.getAll(ref, what);
			if (elems.length >= 1) {
				if (what.equals("Interface")) {
					xarch.set(elems[0], "Id", val + ".IFACE_TOP");
				} else if (what.equals("Signature")) {
					xarch.set(elems[0], "Id", val + "_topSig");
				}
			}
			if (elems.length >= 2) {
				if (what.equals("Interface")) {
					xarch.set(elems[1], "Id", val + ".IFACE_BOTTOM");
				} else if (what.equals("Signature")) {
					xarch.set(elems[1], "Id", val + "_bottomSig");
				}
			}
		}
	}
	
	private void nameLink(ObjRef linkRef) {
		ObjRef[] points = xarch.getAll(linkRef, "Point");	
		if (points.length >= 2) {
			StringBuffer buf = new StringBuffer();
			String temp = getName(points[0]);
			if (temp != null) {
				buf.append(temp + "_to_");
				temp = getName(points[1]);
				if (temp != null) {
					buf.append(temp);
					xarch.set(linkRef, "Id", buf.toString());
				}
			}
		}
	}
	
	private String getName(ObjRef pointRef) {
		ObjRef anchorRef = (ObjRef)(xarch.get(pointRef, "AnchorOnInterface"));
		if (anchorRef != null) {
			String href = (String)(xarch.get(anchorRef, "Href"));
			if (href != null) {
				if (href.endsWith("IFACE_TOP") || href.endsWith("IFACE_BOTTOM")) {
					return href.substring(1, href.lastIndexOf('.'));
				}
			}
		}
		return null;
	}
}